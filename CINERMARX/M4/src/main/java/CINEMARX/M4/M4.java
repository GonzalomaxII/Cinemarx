package CINEMARX.M4;

import java.sql.*;

/**
 * Clase de utilidades del M4 - Maneja la conexión a la base de datos
 */
public class M4 {
    private static Connection conexion;
    private static final String URL = "jdbc:mariadb://br1.aguilucho.ar:25584/Cinemarx";
    private static final String USER = "cnx_admin";
    private static final String PASSWORD = "CnxAdmin!620";
    
    private static void conectarBaseDatos() {
        try {
            conexion = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✓ Conexión exitosa a la base de datos (M4)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static Connection getConexion() {
        try {
            if (conexion == null || conexion.isClosed()) {
                conectarBaseDatos();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conexion;
    }
    

    
    public static void cerrarConexion() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                System.out.println("✓ Conexión cerrada (M4)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(M4::cerrarConexion));
    }
}