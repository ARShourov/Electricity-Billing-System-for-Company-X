package net.proteanit.sql;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Vector;

/**
 * Minimal stand-in for the rs2xml.jar library (net.proteanit.sql.DbUtils), so the project
 * needs no external jar. Turns a ResultSet into a read-only Swing TableModel.
 */
public class DbUtils {

    public static TableModel resultSetToTableModel(ResultSet rs) {
        try {
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            Vector<String> columnNames = new Vector<String>();
            for (int c = 1; c <= columnCount; c++) {
                columnNames.add(meta.getColumnLabel(c));
            }

            Vector<Vector<Object>> rows = new Vector<Vector<Object>>();
            while (rs.next()) {
                Vector<Object> row = new Vector<Object>();
                for (int c = 1; c <= columnCount; c++) {
                    row.add(rs.getObject(c));
                }
                rows.add(row);
            }

            return new DefaultTableModel(rows, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
            return new DefaultTableModel();
        }
    }
}
