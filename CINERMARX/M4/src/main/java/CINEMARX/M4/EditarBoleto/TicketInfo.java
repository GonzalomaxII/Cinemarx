package CINEMARX.M4.EditarBoleto;

public class TicketInfo {
    int idBoleto;
    String numeroButaca;
    int idFuncion;
    String tituloMovie;
    String fecha;
    String hora;
    String idioma;
    double precio;

    TicketInfo(int idBoleto, String numeroButaca, int idFuncion, String titulo,
               String fecha, String hora, String idioma, double precio) {
        this.idBoleto = idBoleto;
        this.numeroButaca = numeroButaca;
        this.idFuncion = idFuncion;
        this.tituloMovie = titulo;
        this.fecha = fecha;
        this.hora = hora;
        this.idioma = idioma;
        this.precio = precio;
    }
}
