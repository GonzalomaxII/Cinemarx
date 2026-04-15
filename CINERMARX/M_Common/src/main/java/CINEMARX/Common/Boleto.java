package CINEMARX.Common;

public class Boleto {
    private int idBoleto;
    private String numeroButaca;
    private int idFuncion;
    private Integer idCliente;

    public Boleto(String numeroButaca, int idFuncion, Integer idCliente) {
        this.numeroButaca = numeroButaca;
        this.idFuncion = idFuncion;
        this.idCliente = idCliente;
    }

    public int getIdBoleto() {
        return idBoleto;
    }

    public void setIdBoleto(int idBoleto) {
        this.idBoleto = idBoleto;
    }

    public String getNumeroButaca() {
        return numeroButaca;
    }

    public int getIdFuncion() {
        return idFuncion;
    }

    public Integer getIdCliente() {
        return idCliente;
    }
}