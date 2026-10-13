package electricity.billing.system;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON-file replacement for the MySQL "Bill_system" database.
 *
 * The rest of the application still talks to a java.sql.Connection / Statement / ResultSet,
 * exactly as before. This class hands out lightweight JDBC look-alikes that understand the
 * handful of SQL statements the application uses and keep all data in one JSON file:
 *
 *     select * from T [where col = 'v' [and col = 'v' ...]]
 *     insert into T value(s)('a','b',...)
 *     update T set col = 'v' [, col = 'v' ...] [where col = 'v' [and ...]]
 *
 * Data file: data/bill_system.json (override with -Dbilling.db=/path/to/file.json).
 * The file is re-read on every statement, so hand edits are picked up immediately.
 */
final class JsonDb {

    /** Table name -> ordered column names (the order matters for "insert ... values(...)"). */
    static final Map<String, List<String>> SCHEMA = new LinkedHashMap<>();

    static {
        SCHEMA.put("Signup", Arrays.asList("meter_no", "username", "name", "password", "usertype"));
        SCHEMA.put("new_customer", Arrays.asList("name", "meter_no", "address", "city", "state", "email", "phone_no"));
        SCHEMA.put("meter_info", Arrays.asList("meter_number", "meter_location", "meter_type", "phase_code", "bill_type", "Days"));
        SCHEMA.put("bill", Arrays.asList("meter_no", "month", "unit", "total_bill", "status"));
        SCHEMA.put("tax", Arrays.asList("cost_per_unit", "meter_rent", "service_charge", "service_tax", "swacch_bharat", "fixed_tax"));
    }

    private static final Object LOCK = new Object();
    private static boolean announced = false;

    private JsonDb() { }

    // ------------------------------------------------------------------ public entry point

    /** Equivalent of DriverManager.getConnection(...) in the old MySQL version. */
    static Connection connect() {
        return (Connection) newProxy(Connection.class, new ConnectionHandler());
    }

    static Path dataFile() {
        return Paths.get(System.getProperty("billing.db", "data/bill_system.json")).toAbsolutePath();
    }

    // ------------------------------------------------------------------ storage

    private static Map<String, List<Map<String, String>>> defaults() {
        Map<String, List<Map<String, String>>> d = new LinkedHashMap<>();
        for (String t : SCHEMA.keySet()) d.put(t, new ArrayList<Map<String, String>>());
        // A ready-to-use administrator (the Signup screen cannot create one, see README).
        d.get("Signup").add(row("Signup", "0", "admin", "Administrator", "admin123", "Admin"));
        // Sample tariff - edit freely in the JSON file.
        d.get("tax").add(row("tax", "9", "47", "22", "57", "6", "42"));
        return d;
    }

    private static Map<String, String> row(String table, String... values) {
        List<String> cols = SCHEMA.get(table);
        Map<String, String> r = new LinkedHashMap<>();
        for (int i = 0; i < cols.size(); i++) r.put(cols.get(i), i < values.length ? values[i] : "");
        return r;
    }

    private static Map<String, List<Map<String, String>>> load() throws SQLException {
        Path f = dataFile();
        if (!announced) {
            announced = true;
            System.out.println("[JsonDb] using data file: " + f);
        }
        try {
            if (!Files.exists(f)) {
                Map<String, List<Map<String, String>>> d = defaults();
                save(d);
                return d;
            }
            String text = new String(Files.readAllBytes(f), StandardCharsets.UTF_8);
            if (text.startsWith("\uFEFF")) text = text.substring(1); // Notepad adds a BOM sometimes
            Object root = new JsonParser(text, f.toString()).parse();
            if (!(root instanceof Map)) throw new SQLException("Invalid JSON in " + f + ": top level must be an object");
            Map<?, ?> rootMap = (Map<?, ?>) root;

            Map<String, List<Map<String, String>>> data = new LinkedHashMap<>();
            for (Map.Entry<String, List<String>> t : SCHEMA.entrySet()) {
                List<Map<String, String>> rows = new ArrayList<>();
                Object tv = rootMap.get(t.getKey());
                if (tv instanceof List) {
                    for (Object o : (List<?>) tv) {
                        if (!(o instanceof Map)) continue;
                        Map<?, ?> src = (Map<?, ?>) o;
                        Map<String, String> r = new LinkedHashMap<>();
                        for (String col : t.getValue()) {
                            Object v = src.get(col);
                            r.put(col, v == null ? "" : String.valueOf(v));
                        }
                        rows.add(r);
                    }
                }
                data.put(t.getKey(), rows);
            }
            return data;
        } catch (IOException e) {
            throw new SQLException("Cannot read " + f + ": " + e.getMessage(), e);
        }
    }

    private static void save(Map<String, List<Map<String, String>>> data) throws SQLException {
        Path f = dataFile();
        try {
            Path parent = f.getParent();
            if (parent != null) Files.createDirectories(parent);
            Path tmp = f.resolveSibling(f.getFileName() + ".tmp");
            Files.write(tmp, toJson(data).getBytes(StandardCharsets.UTF_8));
            Files.move(tmp, f, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new SQLException("Cannot write " + f + ": " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------ SQL execution

    private static ResultSet query(String sql) throws SQLException {
        synchronized (LOCK) {
            Parser p = new Parser(sql);
            p.expectKeyword("select");
            if (!p.acceptPunct("*")) throw unsupported(sql, "only 'select * from <table>' is supported");
            p.expectKeyword("from");
            String table = table(p.word());
            List<String[]> where = parseWhere(p, table);
            p.expectEnd();

            List<String> cols = SCHEMA.get(table);
            List<List<String>> out = new ArrayList<>();
            for (Map<String, String> r : load().get(table)) {
                if (matches(r, where)) {
                    List<String> line = new ArrayList<>();
                    for (String c : cols) line.add(r.get(c));
                    out.add(line);
                }
            }
            return (ResultSet) newProxy(ResultSet.class, new ResultSetHandler(table, cols, out));
        }
    }

    private static int update(String sql) throws SQLException {
        synchronized (LOCK) {
            Parser p = new Parser(sql);
            if (p.acceptKeyword("insert")) return insert(p, sql);
            if (p.acceptKeyword("update")) return updateRows(p);
            throw unsupported(sql, "only INSERT and UPDATE can be executed as updates");
        }
    }

    private static int insert(Parser p, String sql) throws SQLException {
        p.expectKeyword("into");
        String table = table(p.word());
        if (!(p.acceptKeyword("values") || p.acceptKeyword("value"))) {
            throw unsupported(sql, "expected VALUES(...) right after the table name");
        }
        p.expectPunct("(");
        List<String> values = new ArrayList<>();
        do { values.add(p.literal()); } while (p.acceptPunct(","));
        p.expectPunct(")");
        p.expectEnd();

        List<String> cols = SCHEMA.get(table);
        if (values.size() != cols.size()) {
            throw new SQLException("Column count doesn't match value count for table '" + table
                    + "' (expected " + cols.size() + ", got " + values.size() + ")");
        }
        Map<String, List<Map<String, String>>> data = load();
        data.get(table).add(row(table, values.toArray(new String[0])));
        save(data);
        return 1;
    }

    private static int updateRows(Parser p) throws SQLException {
        String table = table(p.word());
        p.expectKeyword("set");
        List<String[]> sets = new ArrayList<>();
        do {
            String col = column(table, p.word(), "field list");
            p.expectPunct("=");
            sets.add(new String[]{col, p.literal()});
        } while (p.acceptPunct(","));
        List<String[]> where = parseWhere(p, table);
        p.expectEnd();

        Map<String, List<Map<String, String>>> data = load();
        int changed = 0;
        for (Map<String, String> r : data.get(table)) {
            if (matches(r, where)) {
                for (String[] s : sets) r.put(s[0], s[1]);
                changed++;
            }
        }
        if (changed > 0) save(data);
        return changed;
    }

    private static List<String[]> parseWhere(Parser p, String table) throws SQLException {
        List<String[]> conds = new ArrayList<>();
        if (!p.acceptKeyword("where")) return conds;
        do {
            String col = column(table, p.word(), "where clause");
            p.expectPunct("=");
            conds.add(new String[]{col, p.literal()});
        } while (p.acceptKeyword("and"));
        return conds;
    }

    private static boolean matches(Map<String, String> row, List<String[]> where) {
        for (String[] c : where) if (!row.get(c[0]).equals(c[1])) return false;
        return true;
    }

    private static String table(String name) throws SQLException {
        for (String t : SCHEMA.keySet()) if (t.equalsIgnoreCase(name)) return t;
        throw new SQLException("Table 'Bill_system." + name + "' doesn't exist");
    }

    private static String column(String table, String name, String where) throws SQLException {
        for (String c : SCHEMA.get(table)) if (c.equalsIgnoreCase(name)) return c;
        throw new SQLException("Unknown column '" + name + "' in '" + where + "'");
    }

    private static SQLException unsupported(String sql, String why) {
        return new SQLException("JSON database: " + why + ". Statement was: " + sql);
    }

    // ------------------------------------------------------------------ tiny SQL tokenizer/parser

    private static final class Parser {
        private final String sql;
        private final List<String> text = new ArrayList<>();
        private final List<Character> kind = new ArrayList<>(); // 'S' string literal, 'W' word, 'P' punctuation
        private int pos = 0;

        Parser(String sql) throws SQLException {
            this.sql = sql;
            int i = 0, n = sql.length();
            while (i < n) {
                char c = sql.charAt(i);
                if (Character.isWhitespace(c)) {
                    i++;
                } else if (c == '\'') {
                    StringBuilder sb = new StringBuilder();
                    i++;
                    boolean closed = false;
                    while (i < n) {
                        char d = sql.charAt(i);
                        if (d == '\'') {
                            if (i + 1 < n && sql.charAt(i + 1) == '\'') { sb.append('\''); i += 2; }
                            else { closed = true; i++; break; }
                        } else {
                            sb.append(d);
                            i++;
                        }
                    }
                    if (!closed) throw new SQLException("You have an error in your SQL syntax (unterminated quote): " + sql);
                    text.add(sb.toString());
                    kind.add('S');
                } else if (Character.isLetterOrDigit(c) || c == '_' || c == '.') {
                    int s = i;
                    while (i < n && (Character.isLetterOrDigit(sql.charAt(i)) || sql.charAt(i) == '_' || sql.charAt(i) == '.')) i++;
                    text.add(sql.substring(s, i));
                    kind.add('W');
                } else {
                    text.add(String.valueOf(c));
                    kind.add('P');
                    i++;
                }
            }
        }

        private boolean isWord(String w) {
            return pos < text.size() && kind.get(pos) == 'W' && text.get(pos).equalsIgnoreCase(w);
        }

        boolean acceptKeyword(String w) { if (isWord(w)) { pos++; return true; } return false; }

        void expectKeyword(String w) throws SQLException { if (!acceptKeyword(w)) throw syntax("expected '" + w + "'"); }

        boolean acceptPunct(String s) {
            if (pos < text.size() && kind.get(pos) == 'P' && text.get(pos).equals(s)) { pos++; return true; }
            return false;
        }

        void expectPunct(String s) throws SQLException { if (!acceptPunct(s)) throw syntax("expected '" + s + "'"); }

        String word() throws SQLException {
            if (pos < text.size() && kind.get(pos) == 'W') return text.get(pos++);
            throw syntax("expected a table or column name");
        }

        /** A value: 'quoted text' or a bare number/word. */
        String literal() throws SQLException {
            if (pos < text.size() && (kind.get(pos) == 'S' || kind.get(pos) == 'W')) return text.get(pos++);
            throw syntax("expected a value");
        }

        void expectEnd() throws SQLException {
            while (acceptPunct(";")) { /* trailing semicolons are fine */ }
            if (pos < text.size()) {
                throw syntax("unexpected '" + text.get(pos) + "' (this JSON database only supports "
                        + "'col = value' conditions joined by AND)");
            }
        }

        private SQLException syntax(String why) {
            return new SQLException("You have an error in your SQL syntax: " + why + ". Statement was: " + sql);
        }
    }

    // ------------------------------------------------------------------ JDBC look-alikes

    private static Object newProxy(Class<?> type, InvocationHandler h) {
        return Proxy.newProxyInstance(JsonDb.class.getClassLoader(), new Class<?>[]{type}, h);
    }

    private static Object objectMethod(Object proxy, Method m, Object[] a, String label) {
        switch (m.getName()) {
            case "toString": return label;
            case "hashCode": return System.identityHashCode(proxy);
            case "equals": return proxy == a[0];
            default: return null;
        }
    }

    private static boolean isObjectMethod(Method m) {
        return m.getDeclaringClass() == Object.class;
    }

    private static final class ConnectionHandler implements InvocationHandler {
        private boolean closed = false;

        @Override
        public Object invoke(Object proxy, Method m, Object[] a) throws Throwable {
            if (isObjectMethod(m)) return objectMethod(proxy, m, a, "JsonDb.Connection");
            switch (m.getName()) {
                case "createStatement": return newProxy(Statement.class, new StatementHandler());
                case "close": closed = true; return null;
                case "isClosed": return closed;
                case "getAutoCommit": return true;
                case "setAutoCommit":
                case "commit":
                case "rollback": return null;
                default: throw new SQLFeatureNotSupportedException("Connection." + m.getName() + " is not supported by the JSON database");
            }
        }
    }

    private static final class StatementHandler implements InvocationHandler {
        private boolean closed = false;

        @Override
        public Object invoke(Object proxy, Method m, Object[] a) throws Throwable {
            if (isObjectMethod(m)) return objectMethod(proxy, m, a, "JsonDb.Statement");
            switch (m.getName()) {
                case "executeQuery": return query((String) a[0]);
                case "executeUpdate": return update((String) a[0]);
                case "close": closed = true; return null;
                case "isClosed": return closed;
                default: throw new SQLFeatureNotSupportedException("Statement." + m.getName() + " is not supported by the JSON database");
            }
        }
    }

    private static final class ResultSetHandler implements InvocationHandler {
        private final String table;
        private final List<String> cols;
        private final List<List<String>> rows;
        private int cursor = -1;
        private boolean closed = false;

        ResultSetHandler(String table, List<String> cols, List<List<String>> rows) {
            this.table = table;
            this.cols = cols;
            this.rows = rows;
        }

        private int index(Object key) throws SQLException {
            if (key instanceof Integer) {
                int i = (Integer) key - 1;
                if (i < 0 || i >= cols.size()) throw new SQLException("Column index out of range: " + key);
                return i;
            }
            for (int i = 0; i < cols.size(); i++) if (cols.get(i).equalsIgnoreCase(String.valueOf(key))) return i;
            throw new SQLException("Column '" + key + "' not found.");
        }

        private String cell(Object key) throws SQLException {
            if (cursor < 0 || cursor >= rows.size()) throw new SQLException("Illegal operation on empty result set.");
            return rows.get(cursor).get(index(key));
        }

        @Override
        public Object invoke(Object proxy, Method m, Object[] a) throws Throwable {
            if (isObjectMethod(m)) return objectMethod(proxy, m, a, "JsonDb.ResultSet(" + table + ")");
            switch (m.getName()) {
                case "next":
                    if (cursor < rows.size()) cursor++;
                    return cursor < rows.size();
                case "getString":
                case "getObject":
                    if (a != null && a.length == 1) return cell(a[0]);
                    break;
                case "getInt":
                    try {
                        return Integer.parseInt(cell(a[0]).trim());
                    } catch (NumberFormatException e) {
                        throw new SQLException("Value is not an integer: " + cell(a[0]));
                    }
                case "findColumn": return index(a[0]) + 1;
                case "wasNull": return false;
                case "getMetaData": return newProxy(ResultSetMetaData.class, new MetaHandler(table, cols));
                case "close": closed = true; return null;
                case "isClosed": return closed;
                default: break;
            }
            throw new SQLFeatureNotSupportedException("ResultSet." + m.getName() + " is not supported by the JSON database");
        }
    }

    private static final class MetaHandler implements InvocationHandler {
        private final String table;
        private final List<String> cols;

        MetaHandler(String table, List<String> cols) {
            this.table = table;
            this.cols = cols;
        }

        @Override
        public Object invoke(Object proxy, Method m, Object[] a) throws Throwable {
            if (isObjectMethod(m)) return objectMethod(proxy, m, a, "JsonDb.ResultSetMetaData(" + table + ")");
            switch (m.getName()) {
                case "getColumnCount": return cols.size();
                case "getColumnLabel":
                case "getColumnName": return cols.get((Integer) a[0] - 1);
                case "getColumnType": return Types.VARCHAR;
                case "getColumnTypeName": return "VARCHAR";
                case "getColumnClassName": return "java.lang.String";
                case "getTableName": return table;
                default: throw new SQLFeatureNotSupportedException("ResultSetMetaData." + m.getName() + " is not supported by the JSON database");
            }
        }
    }

    // ------------------------------------------------------------------ JSON writer

    private static String toJson(Map<String, List<Map<String, String>>> data) {
        StringBuilder sb = new StringBuilder("{\n");
        int t = 0;
        for (Map.Entry<String, List<Map<String, String>>> e : data.entrySet()) {
            sb.append("  ").append(quote(e.getKey())).append(": [");
            List<Map<String, String>> rows = e.getValue();
            if (rows.isEmpty()) {
                sb.append("]");
            } else {
                sb.append("\n");
                for (int r = 0; r < rows.size(); r++) {
                    sb.append("    {\n");
                    int c = 0, cn = rows.get(r).size();
                    for (Map.Entry<String, String> cell : rows.get(r).entrySet()) {
                        sb.append("      ").append(quote(cell.getKey())).append(": ").append(quote(cell.getValue()));
                        sb.append(++c < cn ? ",\n" : "\n");
                    }
                    sb.append("    }").append(r + 1 < rows.size() ? ",\n" : "\n");
                }
                sb.append("  ]");
            }
            sb.append(++t < data.size() ? ",\n" : "\n");
        }
        return sb.append("}\n").toString();
    }

    private static String quote(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        return sb.append('"').toString();
    }

    // ------------------------------------------------------------------ JSON reader

    private static final class JsonParser {
        private final String s;
        private final String source;
        private int i = 0;

        JsonParser(String s, String source) {
            this.s = s;
            this.source = source;
        }

        Object parse() throws SQLException {
            ws();
            Object v = value();
            ws();
            if (i != s.length()) throw fail("unexpected content after the JSON value");
            return v;
        }

        private SQLException fail(String why) {
            return new SQLException("Invalid JSON in " + source + " at character " + i + ": " + why);
        }

        private void ws() {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        }

        private Object value() throws SQLException {
            if (i >= s.length()) throw fail("unexpected end of file");
            char c = s.charAt(i);
            if (c == '{') return object();
            if (c == '[') return array();
            if (c == '"') return string();
            if (s.startsWith("true", i)) { i += 4; return "true"; }
            if (s.startsWith("false", i)) { i += 5; return "false"; }
            if (s.startsWith("null", i)) { i += 4; return null; }
            int st = i;
            while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
            if (st == i) throw fail("unexpected character '" + c + "'");
            return s.substring(st, i);
        }

        private Map<String, Object> object() throws SQLException {
            Map<String, Object> m = new LinkedHashMap<>();
            i++; // {
            ws();
            if (i < s.length() && s.charAt(i) == '}') { i++; return m; }
            while (true) {
                ws();
                if (i >= s.length() || s.charAt(i) != '"') throw fail("expected a quoted property name");
                String k = string();
                ws();
                if (i >= s.length() || s.charAt(i) != ':') throw fail("expected ':' after property name");
                i++;
                ws();
                m.put(k, value());
                ws();
                if (i < s.length() && s.charAt(i) == ',') { i++; continue; }
                if (i < s.length() && s.charAt(i) == '}') { i++; return m; }
                throw fail("expected ',' or '}'");
            }
        }

        private List<Object> array() throws SQLException {
            List<Object> l = new ArrayList<>();
            i++; // [
            ws();
            if (i < s.length() && s.charAt(i) == ']') { i++; return l; }
            while (true) {
                ws();
                l.add(value());
                ws();
                if (i < s.length() && s.charAt(i) == ',') { i++; continue; }
                if (i < s.length() && s.charAt(i) == ']') { i++; return l; }
                throw fail("expected ',' or ']'");
            }
        }

        private String string() throws SQLException {
            StringBuilder sb = new StringBuilder();
            i++; // opening quote
            while (i < s.length()) {
                char c = s.charAt(i++);
                if (c == '"') return sb.toString();
                if (c != '\\') { sb.append(c); continue; }
                if (i >= s.length()) break;
                char e = s.charAt(i++);
                switch (e) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'u':
                        if (i + 4 > s.length()) throw fail("bad \\u escape");
                        try {
                            sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                        } catch (NumberFormatException ex) {
                            throw fail("bad \\u escape");
                        }
                        i += 4;
                        break;
                    default: throw fail("bad escape \\" + e);
                }
            }
            throw fail("unterminated string");
        }
    }
}
