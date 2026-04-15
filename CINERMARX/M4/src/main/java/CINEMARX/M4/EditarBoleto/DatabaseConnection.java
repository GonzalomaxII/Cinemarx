package CINEMARX.M4.EditarBoleto;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mariadb://br1.aguilucho.ar:25584/Cinemarx";
    private static final String USER = "cnx_admin";
    private static final String PASSWORD = "CnxAdmin!620";
    
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MariaDB no encontrado", e);
        }
    }
}
