package CINEMARX.M1;
import java.sql.*;
import CINEMARX.Common.UsuarioCliente;

public class Registrarse {
    private String nombre;
    private String apellido;
    private String correo;
    private String contrasena;
    private int DNI;
    private Date FechaNacimiento;
    

    public Registrarse(String nombre, String apellido, String correo, String contrasena, int DNI, Date FechaNacimiento) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.contrasena = contrasena;
        this.DNI = DNI;
        this.FechaNacimiento = FechaNacimiento;
    }

    public UsuarioCliente crearUsuario() {
        System.out.println("Usuario registrado correctamente: " + nombre);
        return new UsuarioCliente(nombre, apellido, correo, contrasena, DNI, FechaNacimiento);
    }
}