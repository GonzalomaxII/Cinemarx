package CINEMARX.Common;

import CINEMARX.Common.OrderDetails;

public interface NavigationHelper {
    void mostrarDetallePelicula(int idPelicula);
    void mostrarPagos(OrderDetails order);
    void mostrarBuffet(OrderDetails order);
}