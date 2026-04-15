package CINEMARX.Common;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.sql.Date;
// import CINEMARX.M1.RolVIP;
// import CINEMARX.M1.VerHistorial;

public class UsuarioCliente {
    private int idCliente;  // ✅ AGREGADO
    private String nombre;
    private String apellido;
    private String correo;
    private String contrasena;
    private int DNI;
    private Date fechaNacimiento;
    private String fechaRegistro;
    private boolean esVIP;
    private String membresia;

    // private RolVIP rolVIP;
    // private VerHistorial historial;

    // ==================== CONSTRUCTOR COMPLETO ====================
    public UsuarioCliente(String nombre, String apellido, String correo, String contrasena, 
                          int DNI, Date fechaNacimiento) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.contrasena = contrasena;
        this.DNI = DNI;
        this.fechaNacimiento = fechaNacimiento;
        this.membresia = "NO VIP";
        
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        this.fechaRegistro = LocalDate.now().format(formato);
        this.esVIP = false;
    }

    // ==================== CONSTRUCTOR CON MEMBRESÍA ====================
    public UsuarioCliente(String nombre, String apellido, String correo, String contrasena, 
                          int DNI, Date fechaNacimiento, String membresia) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.contrasena = contrasena;
        this.DNI = DNI;
        this.fechaNacimiento = fechaNacimiento;
        this.membresia = membresia;

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        this.fechaRegistro = LocalDate.now().format(formato);
        this.esVIP = "VIP".equalsIgnoreCase(membresia);
    }

    // ==================== CONSTRUCTOR SIMPLE (para login) ====================
    public UsuarioCliente(String nombre, String correo, String contrasena, String fechaRegistro) {
        this.nombre = nombre;
        this.correo = correo;
        this.contrasena = contrasena;

        if (fechaRegistro == null || fechaRegistro.isEmpty()) {
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            this.fechaRegistro = LocalDate.now().format(formato);
        } else {
            this.fechaRegistro = fechaRegistro;
        }

        this.esVIP = false;
        this.membresia = "NO VIP";
    }

    // ==================== GETTERS ====================
    public int getIdCliente() { return idCliente; }  // ✅ AGREGADO
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getNombreCompleto() { 
        if (apellido != null && !apellido.isEmpty()) {
            return nombre + " " + apellido;
        }
        return nombre;
    }
    public String getCorreo() { return correo; }
    public String getContrasena() { return contrasena; }
    public int getDNI() { return DNI; }
    public Date getFechaNacimiento() { return fechaNacimiento; }
    public boolean isEsVIP() { return esVIP; }
    // public RolVIP getRolVIP() { return rolVIP; }
    // public VerHistorial getHistorial() { return historial; }
    public String getFechaRegistro() { return fechaRegistro; }
    public String getMembresia() { return membresia; }

    // ==================== SETTERS ====================
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }  // ✅ AGREGADO
    public void setNombre(String nuevoNombre) { this.nombre = nuevoNombre; }
    public void setApellido(String nuevoApellido) { this.apellido = nuevoApellido; }
    public void setCorreo(String nuevoCorreo) { this.correo = nuevoCorreo; }
    public void setContrasena(String nuevaPass) { this.contrasena = nuevaPass; }
    public void setDNI(int DNI) { this.DNI = DNI; }
    public void setFechaNacimiento(Date fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public void setMembresia(String membresia) { 
        this.membresia = membresia;
        this.esVIP = "VIP".equalsIgnoreCase(membresia);
    }

    // ==================== MÉTODOS ====================
    // public void activarVIP(RolVIP rolVIP) {
    //     this.rolVIP = rolVIP;
    //     this.esVIP = true;
    //     this.membresia = "VIP";
    // }

    // public void setHistorial(VerHistorial historial) {
    //     this.historial = historial;
    // }

    public boolean esVIP() {
        return "VIP".equalsIgnoreCase(membresia);
    }
}
