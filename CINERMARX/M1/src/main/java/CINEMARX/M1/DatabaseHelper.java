package CINEMARX.M1;

import java.sql.*;
import javax.swing.JOptionPane;
import CINEMARX.Common.UsuarioCliente;

/**
 * Clase auxiliar para operaciones de base de datos del módulo M1
 * Gestiona el registro, login y actualización de datos de clientes
 */
public class DatabaseHelper {
    
    private Connection connection;
    
    // Constructor que recibe la conexión
    public DatabaseHelper(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Registra un nuevo cliente en la base de datos
     * @return true si el registro fue exitoso, false si el correo ya existe
     */
    public boolean registrarCliente(String nombre, String apellido, String correo, String contrasena, int DNI, Date FechaNacimiento) {
        // Validar que el correo no exista
        if (existeCorreo(correo)) {
            return false;
        }
        
        // Validar que el DNI no exista
        if (existeDNI(DNI)) {
            return false;
        }
        
        try {
            connection.setAutoCommit(false);
            
            // 1. Crear el usuario en la tabla Usuario
            String sqlUsuario = "INSERT INTO Usuario (DNI, FechaNac, Nombre, Apellido) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sqlUsuario)) {
                pstmt.setInt(1, DNI);
                pstmt.setDate(2, FechaNacimiento);
                pstmt.setString(3, nombre);
                pstmt.setString(4, apellido); 
                pstmt.executeUpdate();
            }
            
            // 2. Crear el cliente vinculado al usuario CON 0 PUNTOS
            String sqlCliente = "INSERT INTO Cliente (DNI, Membresia, Mail, Contrasena, Puntos, PuntosGastados) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sqlCliente)) {
                pstmt.setInt(1, DNI);
                pstmt.setString(2, "NO VIP");
                pstmt.setString(3, correo);
                pstmt.setString(4, contrasena);
                pstmt.setInt(5, 0);
                pstmt.setInt(6, 0);
                pstmt.executeUpdate();
            }
            
            connection.commit();
            connection.setAutoCommit(true);
            return true;
            
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Verifica si un correo ya está registrado
     */
    private boolean existeCorreo(String correo) {
        String sql = "SELECT COUNT(*) FROM Cliente WHERE Mail = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, correo);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Verifica si un DNI ya está registrado
     */
    private boolean existeDNI(int DNI) {
        String sql = "SELECT COUNT(*) FROM Usuario WHERE DNI = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, DNI);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Valida las credenciales de inicio de sesión
     * @return UsuarioCliente si las credenciales son correctas, null si no
     */
    public UsuarioCliente validarCredenciales(String correo, String contrasena) {
        String sql = "SELECT c.ID_Cliente, c.DNI, c.Membresia, c.Mail, c.Contrasena, u.Nombre, u.Apellido, u.FechaNac " +
                     "FROM Cliente c " +
                     "INNER JOIN Usuario u ON c.DNI = u.DNI " +
                     "WHERE c.Mail = ? AND c.Contrasena = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, correo);
            pstmt.setString(2, contrasena);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int idCliente = rs.getInt("ID_Cliente");  // ✅ OBTENER ID
                    String nombre = rs.getString("Nombre");
                    String apellido = rs.getString("Apellido");
                    int dni = rs.getInt("DNI");
                    Date fechaNac = rs.getDate("FechaNac");
                    String membresia = rs.getString("Membresia");

                    UsuarioCliente usuario = new UsuarioCliente(
                        nombre,
                        apellido,
                        correo,
                        contrasena,
                        dni,
                        fechaNac,
                        membresia
                    );

                    usuario.setIdCliente(idCliente);  // ✅ ASIGNAR ID

                    return usuario;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
    public Connection getConnection() {
        return connection;
    }
    
    /**
     * Actualiza los datos de un cliente
     */
    public boolean actualizarDatosCliente(String correoActual, String nuevoNombre, String nuevoApellido,
                                         String nuevoCorreo, String nuevaContrasena, int nuevoDNI, Date nuevaFechaNac) {
        try {
            connection.setAutoCommit(false);
            
            int dniActual = -1;
            String sqlGetDNI = "SELECT DNI FROM Cliente WHERE Mail = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sqlGetDNI)) {
                pstmt.setString(1, correoActual);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        dniActual = rs.getInt("DNI");
                    } else {
                        connection.rollback();
                        connection.setAutoCommit(true);
                        return false;
                    }
                }
            }
            
            if (nuevoDNI != dniActual && existeDNI(nuevoDNI)) {
                connection.rollback();
                connection.setAutoCommit(true);
                return false;
            }
            
            if (!nuevoCorreo.equals(correoActual) && existeCorreo(nuevoCorreo)) {
                connection.rollback();
                connection.setAutoCommit(true);
                return false;
            }
            
            String sqlCliente = "UPDATE Cliente SET Mail = ?, Contrasena = ? WHERE DNI = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sqlCliente)) {
                pstmt.setString(1, nuevoCorreo);
                pstmt.setString(2, nuevaContrasena);
                pstmt.setInt(3, dniActual);
                pstmt.executeUpdate();
            }
            
            String sqlUsuario = "UPDATE Usuario SET Nombre = ?, Apellido = ?, FechaNac = ? WHERE DNI = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sqlUsuario)) {
                pstmt.setString(1, nuevoNombre);
                pstmt.setString(2, nuevoApellido);
                pstmt.setDate(3, nuevaFechaNac);
                pstmt.setInt(4, dniActual);
                pstmt.executeUpdate();
            }
            
            if (nuevoDNI != dniActual) {
                String sqlUpdateDNIUsuario = "UPDATE Usuario SET DNI = ? WHERE DNI = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(sqlUpdateDNIUsuario)) {
                    pstmt.setInt(1, nuevoDNI);
                    pstmt.setInt(2, dniActual);
                    pstmt.executeUpdate();
                }
                
                String sqlUpdateDNICliente = "UPDATE Cliente SET DNI = ? WHERE DNI = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(sqlUpdateDNICliente)) {
                    pstmt.setInt(1, nuevoDNI);
                    pstmt.setInt(2, dniActual);
                    pstmt.executeUpdate();
                }
            }
            
            connection.commit();
            connection.setAutoCommit(true);
            return true;
            
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Obtiene los puntos acumulados de un cliente desde la base de datos
     */
    public int obtenerPuntosCliente(String correo) {
        String sql = "SELECT Puntos FROM Cliente WHERE Mail = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, correo);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Puntos");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Obtiene el ID del cliente a partir de su correo
     */
    public int obtenerIdCliente(String correo) {
        String sql = "SELECT ID_Cliente FROM Cliente WHERE Mail = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, correo);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ID_Cliente");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * Suma 200 puntos al cliente por una compra realizada
     */
    public boolean sumarPuntosPorCompra(String correo) {
        String sql = "UPDATE Cliente SET Puntos = Puntos + 200 WHERE Mail = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, correo);
            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Obtiene los puntos asociados a un producto según su nombre
     */
    private int obtenerPuntosPorProducto(String nombreProducto) {
        switch (nombreProducto.toUpperCase()) {
            case "COMBO PANCHO":
                return 2000;
            case "COMBO POCHOCLO":
                return 5000;
            case "COMBO NACHOS":
                return 2000;
            case "GASEOSA":
                return 1500;
            default:
                return 0;
        }
    }
    
    /**
     * Registra un canje de puntos y actualiza los puntos del cliente
     */
    public boolean registrarCanje(String correo, String producto, int puntos) {
        try {
            connection.setAutoCommit(false);
            
            int idCliente = obtenerIdCliente(correo);
            if (idCliente == -1) {
                connection.rollback();
                connection.setAutoCommit(true);
                return false;
            }
            
            int puntosActuales = obtenerPuntosCliente(correo);
            if (puntosActuales < puntos) {
                connection.rollback();
                connection.setAutoCommit(true);
                return false;
            }
            
            String numComprobante = "CANJE-" + System.currentTimeMillis();
            String sqlComprobante = "INSERT INTO Comprobante (NumComprobante, ID_Cliente, FechaCompra, MetodoPago, Canjeado) " +
                                   "VALUES (?, ?, NOW(), 'Canje de puntos', 'SI')";
            int idComprobante;
            
            try (PreparedStatement pstmt = connection.prepareStatement(sqlComprobante, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, numComprobante);
                pstmt.setInt(2, idCliente);
                pstmt.executeUpdate();
                
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        idComprobante = rs.getInt(1);
                    } else {
                        connection.rollback();
                        connection.setAutoCommit(true);
                        return false;
                    }
                }
            }
            
            int idProducto = buscarOCrearProductoCanje(producto, puntos);
            if (idProducto == -1) {
                connection.rollback();
                connection.setAutoCommit(true);
                return false;
            }
            
            String sqlCompProducto = "INSERT INTO Comprobante_Producto (ID_Comprobante, ID_Prod, Cantidad) " +
                                    "VALUES (?, ?, 1)";
            try (PreparedStatement pstmt = connection.prepareStatement(sqlCompProducto)) {
                pstmt.setInt(1, idComprobante);
                pstmt.setInt(2, idProducto);
                pstmt.executeUpdate();
            }
            
            String sqlUpdate = "UPDATE Cliente SET Puntos = Puntos - ?, PuntosGastados = PuntosGastados + ? WHERE Mail = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sqlUpdate)) {
                pstmt.setInt(1, puntos);
                pstmt.setInt(2, puntos);
                pstmt.setString(3, correo);
                pstmt.executeUpdate();
            }
            
            connection.commit();
            connection.setAutoCommit(true);
            System.out.println("✅ Canje registrado exitosamente - Comprobante: " + numComprobante);
            return true;
            
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Busca un producto de canje o lo crea si no existe
     */
    private int buscarOCrearProductoCanje(String nombreProducto, int puntos) {
        String sqlBuscar = "SELECT ID_Prod FROM Producto WHERE Nombre = ? AND Categoria = 'Canje'";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sqlBuscar)) {
            pstmt.setString(1, nombreProducto);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ID_Prod");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        
        String sqlInsertar = "INSERT INTO Producto (Nombre, Precio, Categoria) VALUES (?, ?, 'Canje')";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sqlInsertar, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, nombreProducto);
            pstmt.setDouble(2, 0.00);
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * Obtiene el historial de compras de un cliente (BOLETOS + PRODUCTOS + CANJES)
     * CORREGIDO: Ahora muestra correctamente los puntos gastados según el producto
     */
    public ResultSet obtenerHistorialCompras(String correo) {
        String sql = 
            "SELECT " +
            "    comp.FechaCompra as Fecha, " +
            "    CONCAT(cb.Cantidad, 'x Entrada ', p.Titulo, ' - ', s.TipoDeSala) as Descripcion, " +
            "    (f.Precio * cb.Cantidad) as Precio, " +
            "    'BOLETO' as Tipo, " +
            "    0 as PuntosGastados " +
            "FROM Comprobante comp " +
            "INNER JOIN Cliente c ON comp.ID_Cliente = c.ID_Cliente " +
            "INNER JOIN Comprobante_Boleto cb ON comp.ID_Comprobante = cb.ID_Comprobante " +
            "INNER JOIN Boleto b ON cb.ID_Boleto = b.ID_Boleto " +
            "INNER JOIN Funcion f ON b.ID_Funcion = f.ID_Funcion " +
            "INNER JOIN Pelicula p ON f.ID_Pelicula = p.ID_Pelicula " +
            "INNER JOIN Sala s ON f.ID_Sala = s.ID_Sala " +
            "WHERE c.Mail = ? " +
            "UNION ALL " +
            "SELECT " +
            "    comp.FechaCompra as Fecha, " +
            "    CASE " +
            "        WHEN comp.Canjeado = 'SI' THEN CONCAT(cp.Cantidad, 'x ', prod.Nombre, ' (CANJEADO)') " +
            "        ELSE CONCAT(cp.Cantidad, 'x ', prod.Nombre) " +
            "    END as Descripcion, " +
            "    CASE " +
            "        WHEN comp.Canjeado = 'SI' THEN 0.00 " +
            "        ELSE (prod.Precio * cp.Cantidad) " +
            "    END as Precio, " +
            "    CASE " +
            "        WHEN comp.Canjeado = 'SI' THEN 'CANJE' " +
            "        ELSE 'PRODUCTO' " +
            "    END as Tipo, " +
            "    CASE " +
            "        WHEN comp.Canjeado = 'SI' THEN " +
            "            CASE " +
            "                WHEN UPPER(prod.Nombre) = 'COMBO PANCHO' THEN 2000 " +
            "                WHEN UPPER(prod.Nombre) = 'COMBO POCHOCLO' THEN 5000 " +
            "                WHEN UPPER(prod.Nombre) = 'COMBO NACHOS' THEN 2000 " +
            "                WHEN UPPER(prod.Nombre) = 'GASEOSA' THEN 1500 " +
            "                ELSE 0 " +
            "            END " +
            "        ELSE 0 " +
            "    END as PuntosGastados " +
            "FROM Comprobante comp " +
            "INNER JOIN Cliente c ON comp.ID_Cliente = c.ID_Cliente " +
            "INNER JOIN Comprobante_Producto cp ON comp.ID_Comprobante = cp.ID_Comprobante " +
            "INNER JOIN Producto prod ON cp.ID_Prod = prod.ID_Prod " +
            "WHERE c.Mail = ? " +
            "ORDER BY Fecha DESC";
        
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, correo);
            pstmt.setString(2, correo);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (!rs.isBeforeFirst()) {
                System.out.println("⚠️ No se encontraron compras para el correo: " + correo);
            } else {
                System.out.println("✅ Historial de compras cargado exitosamente para: " + correo);
            }
            
            return rs;
            
        } catch (SQLException e) {
            System.err.println("❌ Error al obtener historial de compras:");
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Método auxiliar para verificar si un cliente tiene compras registradas
     */
    public boolean tieneCompras(String correo) {
        String sql = "SELECT COUNT(*) as total FROM Comprobante comp " +
                     "INNER JOIN Cliente c ON comp.ID_Cliente = c.ID_Cliente " +
                     "WHERE c.Mail = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, correo);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    System.out.println("📊 Total de comprobantes encontrados: " + total);
                    return total > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Obtiene los datos personales del usuario
     */
    public String[] obtenerDatosPersonales(String correo) {
        String sql = "SELECT u.Nombre, u.Apellido FROM Usuario u " +
                     "INNER JOIN Cliente c ON u.DNI = c.DNI " +
                     "WHERE c.Mail = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, correo);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String nombre = rs.getString("Nombre");
                    String apellido = rs.getString("Apellido");
                    return new String[]{nombre, apellido};
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return new String[]{"", ""};
    }

    public boolean actualizarVIP(int dni) {
        String sql = "UPDATE Cliente SET Membresia = 'VIP' WHERE DNI = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, dni);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}