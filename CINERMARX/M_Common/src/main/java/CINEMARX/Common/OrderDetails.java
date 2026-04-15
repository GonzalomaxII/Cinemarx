package CINEMARX.Common;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import CINEMARX.Common.Producto;
import CINEMARX.Common.Boleto;

public class OrderDetails {
    private int idCliente;
    private int idFuncion;
    private Set<Boleto> boletos;
    private List<OrderItem> itemsProducto;

    // Inner class to hold a product and its customization
    public static class OrderItem {
        public final Producto producto;
        public final String personalizacion;

        public OrderItem(Producto producto, String personalizacion) {
            this.producto = producto;
            this.personalizacion = personalizacion;
        }
    }

    // Constructor for product-only orders (like membership)
    public OrderDetails(int idCliente) {
        this.idCliente = idCliente;
        this.idFuncion = -1; // No function associated
        this.boletos = new HashSet<>();
        this.itemsProducto = new ArrayList<>();
    }

    // Constructor for orders with tickets
    public OrderDetails(int idCliente, int idFuncion) {
        this.idCliente = idCliente;
        this.idFuncion = idFuncion;
        this.boletos = new HashSet<>();
        this.itemsProducto = new ArrayList<>();
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void addBoleto(Boleto boleto) {
        this.boletos.add(boleto);
    }

    public Set<Boleto> getBoletos() {
        return boletos;
    }

    public void addProducto(Producto producto, String personalizacion) {
        this.itemsProducto.add(new OrderItem(producto, personalizacion));
    }

    public List<OrderItem> getProductItems() {
        return itemsProducto;
    }

    public int getIdFuncion() {
        return idFuncion;
    }
}