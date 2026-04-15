package CINEMARX.M3;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import CINEMARX.Common.NavigationHelper;
import CINEMARX.Common.Producto;
import CINEMARX.Common.OrderDetails;
import java.text.Normalizer;

public class BuffetPanel extends JPanel {
    private JPanel panelContenido;
    private Map<Integer, List<String>> carrito = new HashMap<>();
    private Map<Integer, JLabel> contadoresProductos = new HashMap<>();
    private int idUsuario;
    private NavigationHelper navHelper;
    private OrderDetails orderDetails;
    
    private String categoriaSeleccionada = "Todos";
    private static final String[] CATEGORIAS_FILTRO = {"Todos", "Combos", "Bebidas", "Snacks", "Comida", "Otros"};
    private String modoVisualizacion = "CUADRICULA";
    
    private JTextField txtBuscador;

    private static final String BASE_IMAGE_URL = 
        "https://gaelschenone.aguilucho.ar/source_cmx/index.php?preview=M7%2F";
        
    private static final String LOGO_URL = 
        "https://gaelschenone.aguilucho.ar/source_cmx/index.php?preview=M7%2F02.png";

    public BuffetPanel(int idUsuario, NavigationHelper navHelper, OrderDetails orderDetails) {
        this.idUsuario = idUsuario;
        this.navHelper = navHelper;
        this.orderDetails = orderDetails;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(45, 45, 45));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(45, 45, 45));

        JPanel header = createHeader();
        mainPanel.add(header, BorderLayout.NORTH);

        JPanel controlPanel = createControlPanel();
        
        JPanel centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));
        centerContainer.setBackground(new Color(45, 45, 45));
        
        centerContainer.add(controlPanel);

        panelContenido = new JPanel();
        panelContenido.setLayout(new BoxLayout(panelContenido, BoxLayout.Y_AXIS));
        panelContenido.setBackground(new Color(45, 45, 45));
        panelContenido.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JScrollPane scroll = new JScrollPane(panelContenido);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBorder(null);
        scroll.setBackground(new Color(45, 45, 45));
        scroll.getViewport().setBackground(new Color(45, 45, 45));

        centerContainer.add(scroll);
        
        mainPanel.add(centerContainer, BorderLayout.CENTER);
        add(mainPanel);

        cargarProductosPorCategoria(null);
    }
    
    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBackground(new Color(45, 45, 45));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(15, 50, 15, 50));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 10);
        gbc.fill = GridBagConstraints.NONE;

        // Filtro por categoría
        JComboBox<String> cmbFiltro = new JComboBox<>(CATEGORIAS_FILTRO);
        cmbFiltro.setFont(new Font("Arial", Font.PLAIN, 16));
        cmbFiltro.setBackground(new Color(60, 60, 60));
        cmbFiltro.setForeground(Color.WHITE);
        cmbFiltro.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cmbFiltro.setPreferredSize(new Dimension(150, 45));

        cmbFiltro.addActionListener(e -> {
            categoriaSeleccionada = (String) cmbFiltro.getSelectedItem();
            cargarProductosPorCategoria(txtBuscador.getText());
        });
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        controlPanel.add(cmbFiltro, gbc);
        
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        controlPanel.add(Box.createRigidArea(new Dimension(0, 0)), gbc);
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        
        // Buscador
        txtBuscador = new JTextField();
        txtBuscador.setFont(new Font("Arial", Font.PLAIN, 16));
        txtBuscador.setForeground(Color.WHITE);
        txtBuscador.setBackground(new Color(60, 60, 60));
        txtBuscador.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.ipadx = 150;
        gbc.ipady = 5;
        controlPanel.add(txtBuscador, gbc);
        
        // Botón buscar
        JButton btnBuscar = new JButton("BUSCAR");
        btnBuscar.setFont(new Font("Arial", Font.BOLD, 14));
        btnBuscar.setForeground(Color.WHITE);
        btnBuscar.setBackground(new Color(80, 80, 80));
        btnBuscar.setPreferredSize(new Dimension(80, 45));
        btnBuscar.setFocusPainted(false);
        btnBuscar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnBuscar.addActionListener(e -> {
            cargarProductosPorCategoria(txtBuscador.getText());
        });
        gbc.gridx = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        controlPanel.add(btnBuscar, gbc);

        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 4;
        controlPanel.add(Box.createRigidArea(new Dimension(0, 0)), gbc);
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        
        gbc.anchor = GridBagConstraints.EAST;
        
        // Botones de visualización
        JButton btnCuadricula = new JButton("Cuadrícula");
        btnCuadricula.setFont(new Font("Arial", Font.PLAIN, 14));
        btnCuadricula.setForeground(Color.WHITE);
        btnCuadricula.setBackground(new Color(80, 80, 80));
        btnCuadricula.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCuadricula.setFocusPainted(false);
        btnCuadricula.addActionListener(e -> {
            modoVisualizacion = "CUADRICULA";
            cargarProductosPorCategoria(txtBuscador.getText());
        });
        gbc.gridx = 5;
        controlPanel.add(btnCuadricula, gbc);
        
        JButton btnLista = new JButton("Lista");
        btnLista.setFont(new Font("Arial", Font.PLAIN, 14));
        btnLista.setForeground(Color.WHITE);
        btnLista.setBackground(new Color(80, 80, 80));
        btnLista.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLista.setFocusPainted(false);
        btnLista.addActionListener(e -> {
            modoVisualizacion = "LISTA";
            cargarProductosPorCategoria(txtBuscador.getText());
        });
        gbc.gridx = 6;
        controlPanel.add(btnLista, gbc);
        
        return controlPanel;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(45, 45, 45));
        header.setBorder(BorderFactory.createEmptyBorder(15, 50, 15, 50));

        // Panel izquierdo - vacío (el botón volver ya está en la topbar principal)
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        leftPanel.setBackground(new Color(45, 45, 45));
        header.add(leftPanel, BorderLayout.WEST);

        // Panel derecho - Botón Continuar
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(new Color(45, 45, 45));
        
        JButton btnContinuar = new JButton("Continuar");
        btnContinuar.setFont(new Font("Arial", Font.BOLD, 16));
        btnContinuar.setForeground(Color.WHITE);
        btnContinuar.setBackground(new Color(220, 50, 50));
        btnContinuar.setBorder(BorderFactory.createEmptyBorder(12, 35, 12, 35));
        btnContinuar.setFocusPainted(false);
        btnContinuar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnContinuar.addActionListener(e -> continuarCompra());
        
        rightPanel.add(btnContinuar);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }
    
    private void cargarLogo(String logoUrl, JLabel lblLogo) {
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                return new ImageIcon(new java.net.URL(logoUrl));
            }

            @Override
            protected void done() {
                try {
                    ImageIcon originalIcon = get();
                    if (originalIcon != null && originalIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                        Image originalImage = originalIcon.getImage();
                        int width = 350;
                        int height = 55;
                        Image scaledImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                        
                        lblLogo.setIcon(new ImageIcon(scaledImage));
                        lblLogo.setText(null);
                    } else {
                        lblLogo.setText("CINEMARX");
                        lblLogo.setFont(new Font("Arial", Font.BOLD, 32));
                        lblLogo.setForeground(Color.WHITE);
                    }
                } catch (Exception e) {
                    lblLogo.setText("CINEMARX");
                    lblLogo.setFont(new Font("Arial", Font.BOLD, 32));
                    lblLogo.setForeground(Color.WHITE);
                }
            }
        }.execute();
    }

    private void cargarProductosPorCategoria(String filtroBusqueda) {
        panelContenido.removeAll();
        
        JLabel lblTitulo = new JLabel("Acompaña tu película con algo para picar:");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 28));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        panelContenido.add(lblTitulo);
        
        Map<String, List<Producto>> productosPorCategoria = new HashMap<>();

        final String filtroNormalizado = normalizarTexto(filtroBusqueda);
        
        Connection con = ConexionBD.getConexion();
        try {
            if (con == null) {
                throw new Exception("No se pudo conectar a la base de datos.");
            }

            String query = "SELECT ID_Prod, Nombre, Precio FROM Producto WHERE ID_Prod != 20 AND Categoria != 'Membresia' ORDER BY Nombre";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                Producto p = new Producto(
                    rs.getInt("ID_Prod"),
                    rs.getString("Nombre"),
                    rs.getDouble("Precio"),
                    determinarCategoria(rs.getString("Nombre")),
                    ""
                );

                if (!filtroNormalizado.isEmpty()) {
                    String nombreNormalizado = normalizarTexto(p.getNombre());
                    if (!nombreNormalizado.contains(filtroNormalizado)) {
                        continue;
                    }
                }
                
                String categoria = p.getCategoria();
                if (!productosPorCategoria.containsKey(categoria)) {
                    productosPorCategoria.put(categoria, new ArrayList<>());
                }
                productosPorCategoria.get(categoria).add(p);
            }

        } catch (Exception e) {
            System.err.println("❌ Error al cargar productos: " + e.getMessage());
            e.printStackTrace();
        }

        mostrarProductosPorCategoria(productosPorCategoria);
    }

    private String normalizarTexto(String texto) {
        if (texto == null || texto.isEmpty()) {
            return "";
        }
        String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD);
        normalizado = normalizado.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalizado.toLowerCase();
    }

    private String determinarCategoria(String nombreProducto) {
        String nombre = nombreProducto.toLowerCase();
        
        if (nombre.contains("combo") || nombre.contains("pack") || nombre.contains("pareja")) {
            return "Combos";
        } else if (nombre.contains("coca") || nombre.contains("pepsi") || 
                    nombre.contains("sprite") || nombre.contains("agua") || 
                    nombre.contains("jugo") || nombre.contains("gaseosa")) {
            return "Bebidas";
        } else if (nombre.contains("pochoclo") || nombre.contains("nachos") || 
                    nombre.contains("palomitas") || nombre.contains("papas") || 
                    nombre.contains("fritas") || nombre.contains("maní") ||
                    nombre.contains("hot dog") || nombre.contains("pancho")) {
            return "Snacks";
        } else if (nombre.contains("chocolate") || nombre.contains("caramelo") || 
                   nombre.contains("gomita") || nombre.contains("dulce") ||
                   nombre.contains("pizza") || nombre.contains("hamburguesa") ||
                   nombre.contains("nugget") || nombre.contains("galleta")) {
            return "Comida";
        }
        
        return "Otros";
    }

    private void mostrarProductosPorCategoria(Map<String, List<Producto>> productosPorCategoria) {
        String[] ordenCategorias = {"Combos", "Bebidas", "Snacks", "Comida", "Otros"};
        
        boolean filtrarPorCategoria = !categoriaSeleccionada.equals("Todos");

        for (String categoria : ordenCategorias) {
            
            if (filtrarPorCategoria && !categoria.equals(categoriaSeleccionada)) {
                continue;
            }
            
            if (productosPorCategoria.containsKey(categoria)) {
                
                JLabel lblCategoria = new JLabel("— " + categoria.toUpperCase() + " —");
                lblCategoria.setFont(new Font("Arial", Font.BOLD, 22));
                lblCategoria.setForeground(new Color(220, 50, 50));
                lblCategoria.setAlignmentX(Component.LEFT_ALIGNMENT);
                lblCategoria.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
                panelContenido.add(lblCategoria);
                
                List<Producto> productos = productosPorCategoria.get(categoria);

                JPanel panelCategoria = new JPanel();
                panelCategoria.setBackground(new Color(45, 45, 45));
                panelCategoria.setAlignmentX(Component.LEFT_ALIGNMENT);
                panelCategoria.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

                if (modoVisualizacion.equals("CUADRICULA")) {
                    panelCategoria.setLayout(new FlowLayout(FlowLayout.LEFT, 25, 25));
                } else if (modoVisualizacion.equals("LISTA")) {
                    panelCategoria.setLayout(new BoxLayout(panelCategoria, BoxLayout.Y_AXIS));
                }

                for (Producto p : productos) {
                    JPanel card = crearTarjetaProducto(p);
                    panelCategoria.add(card);
                }

                panelContenido.add(panelCategoria);
            }
        }

        panelContenido.revalidate();
        panelContenido.repaint();
    }
    
    private JPanel crearControlPanel(Producto p) {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        controlPanel.setBackground(new Color(45, 45, 45));
        controlPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnRestar = new JButton("-");
        btnRestar.setFont(new Font("Arial", Font.BOLD, 24));
        btnRestar.setForeground(Color.WHITE);
        btnRestar.setBackground(new Color(80, 80, 80));
        btnRestar.setPreferredSize(new Dimension(45, 45));
        btnRestar.setFocusPainted(false);
        btnRestar.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 2));
        btnRestar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRestar.addActionListener(e -> restarDelCarrito(p));

        int cantidadActual = carrito.containsKey(p.getId()) ? carrito.get(p.getId()).size() : 0;
        JLabel lblContador = new JLabel(String.valueOf(cantidadActual));
        
        lblContador.setFont(new Font("Arial", Font.BOLD, 18));
        lblContador.setForeground(Color.WHITE);
        lblContador.setPreferredSize(new Dimension(30, 45));
        lblContador.setHorizontalAlignment(SwingConstants.CENTER);
        contadoresProductos.put(p.getId(), lblContador);

        JButton btnAgregar = new JButton("+");
        btnAgregar.setFont(new Font("Arial", Font.BOLD, 24));
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setBackground(new Color(220, 50, 50));
        btnAgregar.setPreferredSize(new Dimension(45, 45));
        btnAgregar.setFocusPainted(false);
        btnAgregar.setBorder(BorderFactory.createLineBorder(new Color(220, 50, 50), 2));
        btnAgregar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnAgregar.addActionListener(e -> {
            // Obtener el Frame padre para el diálogo
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            CustomizationDialog dialog = new CustomizationDialog(parentFrame, p);
            dialog.setVisible(true);
            
            if (dialog.isAceptado()) {
                manejarPersonalizacion(p, dialog.getOpcionSeleccionada());
            }
        });

        controlPanel.add(btnRestar);
        controlPanel.add(lblContador);
        controlPanel.add(btnAgregar);
        
        return controlPanel;
    }
    
    private JPanel crearTarjetaProducto(Producto p) {
        JPanel card = new JPanel();
        card.setBackground(new Color(45, 45, 45));
        
        if (modoVisualizacion.equals("CUADRICULA")) {
            card.setLayout(new BorderLayout());
            card.setPreferredSize(new Dimension(260, 400));
            card.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1));
        } else {
            card.setLayout(new BorderLayout(20, 0));
            card.setPreferredSize(new Dimension(800, 120));
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
        }

        JPanel imgPanel = new JPanel(new BorderLayout());
        imgPanel.setBackground(new Color(235, 225, 210));
        
        if (modoVisualizacion.equals("CUADRICULA")) {
            imgPanel.setPreferredSize(new Dimension(260, 280));
        } else {
            imgPanel.setPreferredSize(new Dimension(100, 100));
        }
        
        JLabel lblPlaceholder = new JLabel("Cargando...", SwingConstants.CENTER);
        lblPlaceholder.setFont(new Font("Arial", Font.BOLD, 16));
        lblPlaceholder.setForeground(new Color(80, 80, 80));
        imgPanel.add(lblPlaceholder, BorderLayout.CENTER);
        
        card.add(imgPanel, (modoVisualizacion.equals("CUADRICULA") ? BorderLayout.CENTER : BorderLayout.WEST));
        
        String imageUrl = BASE_IMAGE_URL + p.getId() + ".png";
        cargarImagenDesdeURL(imageUrl, imgPanel, lblPlaceholder);
        
        JPanel info = new JPanel();
        info.setBackground(new Color(45, 45, 45));
        
        JLabel lblNombre = new JLabel(p.getNombre().toUpperCase());
        lblNombre.setFont(new Font("Arial", Font.BOLD, 13));
        lblNombre.setForeground(Color.WHITE);
        lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblPrecio = new JLabel("$" + String.format("%.0f", p.getPrecio()));
        lblPrecio.setFont(new Font("Arial", Font.PLAIN, 13));
        lblPrecio.setForeground(Color.WHITE);
        lblPrecio.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel controlPanel = crearControlPanel(p);

        if (modoVisualizacion.equals("LISTA")) {
            info.setLayout(new BorderLayout());

            JPanel textoPanel = new JPanel(new GridLayout(2, 1));
            textoPanel.setBackground(new Color(45, 45, 45));
            textoPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
            textoPanel.add(lblNombre);
            textoPanel.add(lblPrecio);

            info.add(textoPanel, BorderLayout.CENTER);
            info.add(controlPanel, BorderLayout.EAST);
            
            card.add(info, BorderLayout.CENTER);
            
        } else {
            info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
            info.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));
            
            JPanel textoPanel = new JPanel();
            textoPanel.setLayout(new BoxLayout(textoPanel, BoxLayout.Y_AXIS));
            textoPanel.setBackground(new Color(45, 45, 45));
            textoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            textoPanel.add(lblNombre);
            textoPanel.add(Box.createVerticalStrut(5));
            textoPanel.add(lblPrecio);

            info.add(textoPanel);
            info.add(Box.createVerticalStrut(10));
            info.add(controlPanel);

            card.add(info, BorderLayout.SOUTH);
        }
        
        Color baseColor = new Color(45, 45, 45);
        Color hoverColor = new Color(65, 65, 65);
        Color baseBorder = new Color(60, 60, 60);
        Color hoverBorder = new Color(255, 180, 0);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBackground(hoverColor);
                info.setBackground(hoverColor);
                controlPanel.setBackground(hoverColor);
                card.setBorder(BorderFactory.createLineBorder(hoverBorder, 2));
                
                for (Component comp : info.getComponents()) {
                    if (comp instanceof JPanel) {
                        ((JPanel) comp).setBackground(hoverColor);
                    }
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBackground(baseColor);
                info.setBackground(baseColor);
                controlPanel.setBackground(baseColor);
                card.setBorder(BorderFactory.createLineBorder(baseBorder, 1));
                
                for (Component comp : info.getComponents()) {
                    if (comp instanceof JPanel) {
                        ((JPanel) comp).setBackground(baseColor);
                    }
                }
            }
        });

        return card;
    }

    private void manejarPersonalizacion(Producto producto, String opcion) {
        agregarAlCarrito(producto, opcion);
        
        String detalle = opcion != null && !opcion.isEmpty() 
                         ? "\n   [Añadidos: " + opcion.replace(";", ", ") + "]" 
                         : " (Sin personalización)";

        System.out.println("Agregado al carrito: " + producto.getNombre() + detalle);
        JOptionPane.showMessageDialog(this,
            producto.getNombre() + detalle + " añadido al carrito.",
            "Personalización Aplicada",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void cargarImagenDesdeURL(String imagenRuta, JPanel contenedor, JLabel etiquetaPlaceholder) {
        if (imagenRuta == null || imagenRuta.trim().isEmpty()) {
            etiquetaPlaceholder.setText("[IMAGEN NO DISPONIBLE]");
            etiquetaPlaceholder.setFont(new Font("Arial", Font.BOLD, 14));
            etiquetaPlaceholder.setForeground(new Color(80, 80, 80));
            return;
        }

        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                return new ImageIcon(new java.net.URL(imagenRuta));
            }

            @Override
            protected void done() {
                try {
                    ImageIcon originalIcon = get();
                    if (originalIcon != null && originalIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                        Image originalImage = originalIcon.getImage();
                        int width = contenedor.getPreferredSize().width;
                        int height = contenedor.getPreferredSize().height;
                        Image scaledImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                        
                        ImageIcon scaledIcon = new ImageIcon(scaledImage);
                        
                        JLabel lblImagenFinal = new JLabel(scaledIcon, SwingConstants.CENTER);
                        contenedor.remove(etiquetaPlaceholder);
                        contenedor.add(lblImagenFinal, BorderLayout.CENTER);
                        
                    } else {
                        etiquetaPlaceholder.setText("[IMAGEN NO DISPONIBLE]");
                        etiquetaPlaceholder.setFont(new Font("Arial", Font.BOLD, 14));
                        etiquetaPlaceholder.setForeground(new Color(80, 80, 80));
                    }
                    
                } catch (Exception e) {
                    etiquetaPlaceholder.setText("[ERROR DE CARGA]");
                    etiquetaPlaceholder.setFont(new Font("Arial", Font.BOLD, 14));
                    etiquetaPlaceholder.setForeground(new Color(80, 80, 80));
                } finally {
                    contenedor.revalidate();
                    contenedor.repaint();
                }
            }
        }.execute();
    }

    private void agregarAlCarrito(Producto producto, String personalizacion) {
        int productoId = producto.getId();
        
        carrito.putIfAbsent(productoId, new ArrayList<>());
        carrito.get(productoId).add(personalizacion);

        int cantidadActual = carrito.get(productoId).size();
        JLabel lblContador = contadoresProductos.get(productoId);
        if (lblContador != null) {
            lblContador.setText(String.valueOf(cantidadActual));
        }
    }
    
    private void restarDelCarrito(Producto producto) {
        int productoId = producto.getId();
        
        if (carrito.containsKey(productoId)) {
            List<String> personalizaciones = carrito.get(productoId);
            
            if (!personalizaciones.isEmpty()) {
                personalizaciones.remove(personalizaciones.size() - 1);
                
                int cantidadActual = personalizaciones.size();
                JLabel lblContador = contadoresProductos.get(productoId);
                if (lblContador != null) {
                    lblContador.setText(String.valueOf(cantidadActual));
                }

                if (cantidadActual == 0) {
                    carrito.remove(productoId);
                }
            }
        }
    }
    
    private void continuarCompra() {
        // The check for an empty cart has been removed as per user request.

        for (Map.Entry<Integer, List<String>> entry : carrito.entrySet()) {
            int productoId = entry.getKey();
            Producto p = getProductById(productoId); 
            if (p != null) {
                for (String custom : entry.getValue()) {
                    orderDetails.addProducto(p, custom);
                }
            }
        }

        System.out.println("--- DEBUG: OrderDetails after Buffet ---");
        System.out.println("ID de Función: " + orderDetails.getIdFuncion());
        System.out.println("Boletos: " + orderDetails.getBoletos().size());
        System.out.println("Productos: " + orderDetails.getProductItems().size());
        for (OrderDetails.OrderItem item : orderDetails.getProductItems()) {
            System.out.println("  Producto: " + item.producto.getNombre() + " | Extra: " + item.personalizacion);
        }
        System.out.println("---------------------------------------");

        navHelper.mostrarPagos(orderDetails);
    }

    private Producto getProductById(int id) {
        Connection con = ConexionBD.getConexion();
        try {
            String query = "SELECT * FROM Producto WHERE ID_Prod = ?";
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return new Producto(
                        rs.getInt("ID_Prod"),
                        rs.getString("Nombre"),
                        rs.getDouble("Precio")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
