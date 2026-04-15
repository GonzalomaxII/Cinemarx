package CINEMARX.M4;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import java.sql.*;
import CINEMARX.Common.UsuarioCliente;

import CINEMARX.Common.NavigationHelper;
import CINEMARX.Common.OrderDetails;

/**
 * Panel contenedor del M4 que se integra en VentanaPrincipal del M1
 * Gestiona la navegación interna entre las pantallas del módulo
 */
public class M4Panel extends JPanel {
    
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private Map<String, JPanel> panelCache;
    private int idPeliculaActual;
    private UsuarioCliente usuario;
    private static boolean javaFXInitialized = false;
    private OrderDetails currentOrder;
    private NavigationHelper navHelper;
    
    public M4Panel(int idPelicula, UsuarioCliente usuario, NavigationHelper navHelper) {
        this.idPeliculaActual = idPelicula;
        this.usuario = usuario;
        this.navHelper = navHelper;
        this.panelCache = new HashMap<>();
        
        
        // Inicializar JavaFX si no está inicializado
        inicializarJavaFX();
        
        configurarPanel();
        crearComponentes();
        
        // Cargar la pantalla de película
        SwingUtilities.invokeLater(() -> mostrarPantallaPelicula(idPelicula));
    }

    public OrderDetails getOrCreateOrderDetails(int idFuncion) {
        if (currentOrder == null || currentOrder.getIdFuncion() != idFuncion) {
            currentOrder = new OrderDetails(usuario.getIdCliente(), idFuncion);
        }
        return currentOrder;
    }
    
    private void inicializarJavaFX() {
        if (!javaFXInitialized) {
            // Inicializar JavaFX en el EDT de Swing
            new JFXPanel(); // Esto inicializa el toolkit de JavaFX
            Platform.setImplicitExit(false); // Evita que JavaFX cierre la aplicación
            javaFXInitialized = true;
            System.out.println("✅ JavaFX inicializado para M4Panel");
        }
    }
    
    private void configurarPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 30));
    }
    
    private void crearComponentes() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(new Color(30, 30, 30));
        
        // Panel de carga inicial
        JPanel loadingPanel = crearPanelCarga();
        contentPanel.add(loadingPanel, "loading");
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Mostrar el loading inicialmente
        cardLayout.show(contentPanel, "loading");
    }
    
    private JPanel crearPanelCarga() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(30, 30, 30));
        
        JLabel lblCargando = new JLabel("Cargando...");
        lblCargando.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblCargando.setForeground(Color.WHITE);
        
        panel.add(lblCargando);
        
        return panel;
    }
    
    /**
     * Muestra la pantalla de película
     */
    public void mostrarPantallaPelicula(int idPelicula) {
        this.idPeliculaActual = idPelicula;
        
        // Mostrar loading
        cardLayout.show(contentPanel, "loading");
        
        SwingWorker<PantallaPelicula, Void> worker = new SwingWorker<PantallaPelicula, Void>() {
            @Override
            protected PantallaPelicula doInBackground() throws Exception {
                String key = "pelicula";
                PantallaPelicula panel;
                
                if (!panelCache.containsKey(key)) {
                    panel = new PantallaPelicula(idPelicula, M4Panel.this);
                    panelCache.put(key, panel);
                } else {
                    panel = (PantallaPelicula) panelCache.get(key);
                    panel.cargarPelicula(idPelicula);
                }
                
                return panel;
            }
            
            @Override
            protected void done() {
                try {
                    PantallaPelicula panel = get();
                    String key = "pelicula";
                    
                    // Agregar al contenedor si no existe
                    if (!esPanelAgregado(key)) {
                        contentPanel.add(panel, key);
                    }
                    
                    cardLayout.show(contentPanel, key);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(M4Panel.this,
                        "Error al cargar la película: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Muestra la pantalla de butacas
     */
    public void mostrarPantallaButacas(int idFuncion, int idSala, int idPelicula) {
        // Mostrar loading
        cardLayout.show(contentPanel, "loading");
        
        SwingWorker<PantallaButacas, Void> worker = new SwingWorker<PantallaButacas, Void>() {
            @Override
            protected PantallaButacas doInBackground() throws Exception {
                // No cacheamos butacas para que se actualice siempre
                String key = "butacas-" + idFuncion;
                PantallaButacas panel = new PantallaButacas(idFuncion, idSala, idPelicula, M4Panel.this, usuario.getIdCliente());
                return panel;
            }
            
            @Override
            protected void done() {
                try {
                    PantallaButacas panel = get();
                    String key = "butacas-" + idFuncion;
                    
                    // Remover panel anterior de butacas si existe
                    Component[] components = contentPanel.getComponents();
                    for (Component comp : components) {
                        // Buscar componentes que sean PantallaButacas
                        if (comp instanceof PantallaButacas) {
                            contentPanel.remove(comp);
                        }
                    }

                    contentPanel.add(panel, key);
                    cardLayout.show(contentPanel, key);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(M4Panel.this,
                        "Error al cargar la pantalla de butacas: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }


    
    /**
     * Muestra el loading
     */
    public void mostrarCarga() {
        cardLayout.show(contentPanel, "loading");
    }
    
    /**
     * Verifica si un panel ya está agregado
     */
    private boolean esPanelAgregado(String key) {
        Component[] components = contentPanel.getComponents();
        for (Component comp : components) {
            if (comp.getName() != null && comp.getName().equals(key)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Obtiene el ID de la película actual
     */
    public int getIdPeliculaActual() {
        return idPeliculaActual;
    }
    public static Connection getConexion() {
        try {
            String URL = "jdbc:mariadb://br1.aguilucho.ar:25584/Cinemarx";
            String USER = "cnx_admin";
            String PASSWORD = "CnxAdmin!620";
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public NavigationHelper getNavHelper() {
        return navHelper;
    }

    public JPanel getcontentPanel() {
        return(contentPanel);
    }
}