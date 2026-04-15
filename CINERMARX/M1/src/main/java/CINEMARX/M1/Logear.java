package CINEMARX.M1;

import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import CINEMARX.Common.UsuarioCliente;

public class Logear {
    private ArrayList<UsuarioCliente> usuariosRegistrados;
    private UsuarioCliente usuarioActual;
    private DatabaseHelper dbHelper;
    
    public Logear(Connection connection) {
        usuariosRegistrados = new ArrayList<>();
        this.dbHelper = new DatabaseHelper(connection);
    }

    // ================= REGISTRAR USUARIO =================
    public boolean registrarUsuario(String nombre, String apellido, String correo, String contrasena, int DNI, Date fechaNacimiento) {
        // Validar que sea @gmail.com
        if (!correo.toLowerCase().endsWith("@gmail.com")) {
            return false; // correo no válido
        }
        
        // Intentar registrar en la base de datos
        return dbHelper.registrarCliente(nombre, apellido, correo, contrasena, DNI, fechaNacimiento);
    }

    // ================= INICIAR SESIÓN =================
    public boolean iniciarSesion(String correo, String contrasena) {
        // Validar credenciales contra la base de datos
        UsuarioCliente usuario = dbHelper.validarCredenciales(correo, contrasena);
        
        if (usuario != null) {
            usuarioActual = usuario;
            return true;
        }
        
        return false;
    }

    public UsuarioCliente getUsuarioActual() {
        return usuarioActual;
    }
    
    public DatabaseHelper getDbHelper() {
        return dbHelper;
    }
    //---------------------------------
    public boolean iniciarSesionAdmin(String correo, String contrasena) {
        try {
            // Verificar si existe en la tabla Administrador con JOIN a Usuario
            String sqlAdmin = "SELECT a.ID_Admin, a.DNI, a.Mail, a.Contrasena, " +
                             "u.Nombre, u.Apellido, u.FechaNac " +
                             "FROM Administrador a " +
                             "INNER JOIN Usuario u ON a.DNI = u.DNI " +
                             "WHERE a.Mail = ? AND a.Contrasena = ?";

            java.sql.PreparedStatement stmtAdmin = dbHelper.getConnection().prepareStatement(sqlAdmin);
            stmtAdmin.setString(1, correo);
            stmtAdmin.setString(2, contrasena);

            java.sql.ResultSet rsAdmin = stmtAdmin.executeQuery();

            if (rsAdmin.next()) {
                // Es un administrador válido
                int dni = rsAdmin.getInt("DNI");
                String nombre = rsAdmin.getString("Nombre");
                String apellido = rsAdmin.getString("Apellido");
                java.sql.Date fechaNac = rsAdmin.getDate("FechaNac");
                String mail = rsAdmin.getString("Mail");

                // Crear objeto UsuarioCliente (aunque sea admin, para mantener compatibilidad)
                usuarioActual = new UsuarioCliente(
                    nombre,
                    apellido,
                    mail, 
                    contrasena,
                    dni,
                    fechaNac,
                    "VIP" // VIP = true para admins (para que tenga todos los permisos)
                );

                rsAdmin.close();
                stmtAdmin.close();

                System.out.println("✓ Login exitoso como ADMINISTRADOR: " + nombre + " " + apellido);
                return true;
            }

            rsAdmin.close();
            stmtAdmin.close();

        } catch (java.sql.SQLException e) {
            System.err.println("Error al verificar admin: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }
    public boolean esAdministrador(String correo) {
        try {
            String sql = "SELECT COUNT(*) as total FROM Administrador WHERE Mail = ?";
            java.sql.PreparedStatement stmt = dbHelper.getConnection().prepareStatement(sql);
            stmt.setString(1, correo);

            java.sql.ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int total = rs.getInt("total");
                rs.close();
                stmt.close();
                return total > 0;
            }

            rs.close();
            stmt.close();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}