package electricity.billing.system;

import java.sql.*;

/**
 * Same class, same fields, same constructor as before - so Login, Signup, calculate_bill ...
 * keep working untouched. The only difference: instead of opening a MySQL connection it
 * opens the JSON-file database (see JsonDb.java and data/bill_system.json).
 */
public class database {
    Connection connection;
    Statement statement;
    database(){
        try {
            connection = JsonDb.connect();
            statement = connection.createStatement();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
