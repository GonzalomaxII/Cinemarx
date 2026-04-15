package CINEMARX.M1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import CINEMARX.M2.CatalogoPeliculasPanel;
import CINEMARX.M3.BuffetPanel;
import CINEMARX.M4.M4Panel;
import CINEMARX.M5.VentanaFinalizarCompra;
import CINEMARX.Common.OrderDetails;
import CINEMARX.M6.M6Panel;
import CINEMARX.Common.NavigationHelper;
import CINEMARX.Common.UsuarioCliente;
import CINEMARX.Common.Producto;
import CINEMARX.M5.ConexionBD;

//files('../M4/libs/mariadb-java-client-3.5.6.jar')
public class VentanaPrincipal extends JFrame implements NavigationHelper {
    private Connection connection;
    private Logear sistemaLogin;
    private UsuarioCliente usuarioActual;

    private JPanel panelLateral;
    private JPanel panelContenido;
    
    // Sistema de puntos
    private int puntosUsuario = 0;
    private M6Panel panelAdmin;

    public VentanaPrincipal(Connection connection) {
        this.connection = connection;

        setTitle("CINEMARX");
        setSize(1366, 768);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(30, 30, 30));
        this.sistemaLogin = new Logear(connection);

        setVisible(false);
        mostrarLogin();
    }
    
        private void cerrarSesion() {
        // ✅ Si hay un panel de admin abierto, cerrar la conexión a BD
        if (panelAdmin != null) {
            System.out.println("Cerrando conexión del panel de administración...");
            panelAdmin.closeDatabaseConnection();
            panelAdmin = null;
        }

        usuarioActual = null;
        setVisible(false);
        mostrarLogin();
    }
        // ✅ NUEVO MÉTODO - Agregar después de cerrarSesion()
        private void mostrarCatalogo() {
            // ✅ REMOVER LA BARRA LATERAL SI EXISTE
            Component[] components = getContentPane().getComponents();
            for (Component comp : components) {
                if (comp == panelLateral) {
                    remove(panelLateral);
                    break;
                }
            }

            panelContenido.removeAll();

            // Crear el panel del catálogo de películas del M2
            CatalogoPeliculasPanel catalogoPanel = new CatalogoPeliculasPanel(usuarioActual.getDNI());

            panelContenido.setLayout(new BorderLayout());
            panelContenido.add(catalogoPanel, BorderLayout.CENTER);

            revalidate();
            repaint();
        }
        

        
        public void mostrarDetallePelicula(int idPelicula) {
            // Remover la barra lateral si existe
            Component[] components = getContentPane().getComponents();
            for (Component comp : components) {
                if (comp == panelLateral) {
                    remove(panelLateral);
                    break;
                }
            }

            panelContenido.removeAll();

            // Crear el panel M4 con el ID de la película
            M4Panel m4Panel = new M4Panel(idPelicula, usuarioActual, this);

            panelContenido.setLayout(new BorderLayout());
            panelContenido.add(m4Panel, BorderLayout.CENTER);

            revalidate();
            repaint();
        }

        @Override
        public void mostrarPagos(OrderDetails order) {
            panelContenido.removeAll();
            panelContenido.setLayout(new BorderLayout());
            VentanaFinalizarCompra panelPagos = new VentanaFinalizarCompra(order);
            panelContenido.add(panelPagos, BorderLayout.CENTER);
            panelContenido.revalidate();
            panelContenido.repaint();
        }

        @Override
        public void mostrarBuffet(OrderDetails order) {
            panelContenido.removeAll();
            BuffetPanel buffetPanel = new BuffetPanel(usuarioActual.getIdCliente(), this, order);
            panelContenido.add(buffetPanel, BorderLayout.CENTER);
            panelContenido.revalidate();
            panelContenido.repaint();
        }

    
    private void crearInterfazPrincipal() {
        getContentPane().removeAll();
        // CARGAR PUNTOS DESDE LA BASE DE DATOS
        puntosUsuario = sistemaLogin.getDbHelper().obtenerPuntosCliente(usuarioActual.getCorreo());

        // BARRA SUPERIOR
        JPanel panelSuperior = new JPanel();
        panelSuperior.setLayout(new BorderLayout());
        panelSuperior.setBackground(new Color(45, 45, 45));
        panelSuperior.setPreferredSize(new Dimension(getWidth(), 80)); // Taller
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Padding

        // Logo CINEMARX (izquierda) - CLICKEABLE
        JLabel lblLogo = new JLabel();
        try {
            java.net.URL url = new java.net.URL("https://gaelschenone.aguilucho.ar/source_cmx/index.php?preview=logos%2FCINEMARX%20logotipo.png");
            ImageIcon icon = new ImageIcon(url);
            Image img = icon.getImage();
            
            // Calculate new dimensions to maintain aspect ratio
            int anchoOriginal = icon.getIconWidth();
            int altoOriginal = icon.getIconHeight();
            int anchoDeseado = 220; // Larger
            int altoDeseado = (altoOriginal * anchoDeseado) / anchoOriginal;
            
            Image scaledImg = img.getScaledInstance(anchoDeseado, altoDeseado, Image.SCALE_SMOOTH);
            lblLogo.setIcon(new ImageIcon(scaledImg));
        } catch (Exception e) {
            lblLogo.setText("CINEMARX");
            lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 20));
            lblLogo.setForeground(new Color(220, 20, 60));
        }
        lblLogo.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20)); // Adjusted padding
        lblLogo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblLogo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mostrarCatalogo();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                lblLogo.setOpaque(true);
                lblLogo.setBackground(new Color(55, 55, 55));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                lblLogo.setOpaque(false);
            }
        });

        // Panel para los botones de la derecha (BUFFET, MEMBRESÍA, y USUARIO)
        JPanel panelRightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0)); // Aligned right
        panelRightButtons.setBackground(new Color(45, 45, 45));

        JButton btnBuffet = new JButton("BUFFET");
        btnBuffet.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBuffet.setForeground(Color.WHITE);
        btnBuffet.setBackground(new Color(45, 45, 45));
        btnBuffet.setFocusPainted(false);
        btnBuffet.setBorderPainted(false);
        btnBuffet.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBuffet.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        btnBuffet.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnBuffet.setBackground(new Color(55, 55, 55));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnBuffet.setBackground(new Color(45, 45, 45));
            }
        });

        btnBuffet.setEnabled(false); // Assuming this is still desired
        btnBuffet.addActionListener(e -> {});
        
        //Boton Membresía
        JButton btnMembresia = new JButton("MEMBRESÍA");
        btnMembresia.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnMembresia.setForeground(Color.WHITE);
        btnMembresia.setBackground(new Color(45, 45, 45));
        btnMembresia.setFocusPainted(false);
        btnMembresia.setBorderPainted(false);
        btnMembresia.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnMembresia.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        btnMembresia.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnMembresia.setBackground(new Color(55, 55, 55));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnMembresia.setBackground(new Color(45, 45, 45));
            }
        });

        // Misma funcionalidad que "Canjear puntos"
        btnMembresia.addActionListener(e -> {
            if (usuarioActual.esVIP()) {
                mostrarCanjePuntos();
            } else {
                mostrarPantallaComprarVIP();
            }
        });
        
        panelRightButtons.add(btnBuffet);
        panelRightButtons.add(btnMembresia);
        
        // Usuario (integrated into panelRightButtons)
        JButton btnUsuario = new JButton(usuarioActual.getNombreCompleto());
        try {
            java.net.URL userIconUrl = new java.net.URL("https://gaelschenone.aguilucho.ar/source_cmx/index.php?preview=botones%2Fuser.png");
            ImageIcon userIcon = new ImageIcon(userIconUrl);
            Image userImg = userIcon.getImage();
            Image scaledUserImg = userImg.getScaledInstance(24, 24, Image.SCALE_SMOOTH); // Adjust size as needed
            btnUsuario.setIcon(new ImageIcon(scaledUserImg));
        } catch (Exception e) {
            System.err.println("Error loading user icon: " + e.getMessage());
            // Fallback if icon fails to load
            btnUsuario.setIcon(createUserIcon()); // Use existing method as fallback
        }
        btnUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnUsuario.setForeground(Color.WHITE);
        btnUsuario.setBackground(new Color(45, 45, 45));
        btnUsuario.setFocusPainted(false);
        btnUsuario.setBorderPainted(false);
        btnUsuario.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnUsuario.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnUsuario.setHorizontalTextPosition(SwingConstants.LEFT); // Text to the left of icon

        // Menú desplegable del usuario (existing functionality)
        JPopupMenu menuUsuario = new JPopupMenu();
        menuUsuario.setBackground(new Color(30, 30, 30));
        menuUsuario.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));

        JMenuItem itemPerfil = crearItemMenu("Perfil");
        JMenuItem itemCompras = crearItemMenu("Mis compras");
        JMenuItem itemCanjear = crearItemMenu("Canjear puntos");
        menuUsuario.addSeparator();
        JMenuItem itemCerrarSesion = crearItemMenu("Cerrar sesión");

        itemPerfil.addActionListener(e -> mostrarSeccionPerfil());
        itemCompras.addActionListener(e -> mostrarHistorial());
        itemCanjear.addActionListener(e -> {
            if (usuarioActual.esVIP()) {
                mostrarCanjePuntos();
            } else {
                mostrarPantallaComprarVIP();
            }
        });
        itemCerrarSesion.addActionListener(e -> cerrarSesion());

        if (!usuarioActual.esVIP()) {
            itemCanjear.setEnabled(false);
            itemCanjear.setForeground(new Color(100, 100, 100));
        }

        menuUsuario.add(itemPerfil);
        menuUsuario.add(itemCompras);
        menuUsuario.add(itemCanjear);
        menuUsuario.add(itemCerrarSesion);

        btnUsuario.addActionListener(e -> {
            menuUsuario.show(btnUsuario, 0, btnUsuario.getHeight());
        });

        panelRightButtons.add(btnUsuario); // Add user button to panelRightButtons

        panelSuperior.add(lblLogo, BorderLayout.WEST);
        panelSuperior.add(panelRightButtons, BorderLayout.EAST);

        // Panel lateral y contenido
        panelLateral = crearPanelLateral();

        panelContenido = new JPanel();
        panelContenido.setBackground(new Color(30, 30, 30));
        panelContenido.setLayout(new BorderLayout());

        add(panelSuperior, BorderLayout.NORTH);
        add(panelContenido, BorderLayout.CENTER);

        revalidate();
        repaint();
    }
    
    //METODO NUEVO #1
    private JPanel crearPanelLateral() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(20, 20, 20));
        panel.setPreferredSize(new Dimension(250, getHeight()));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 0, 20, 0));

        // Cuenta Section
        JLabel lblCuentaTitulo = new JLabel("Cuenta");
        lblCuentaTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCuentaTitulo.setForeground(new Color(150, 150, 150));
        lblCuentaTitulo.setBorder(BorderFactory.createEmptyBorder(0, 25, 10, 0));

        JButton btnPerfil = crearBotonLateral("• Perfil");
        btnPerfil.addActionListener(e -> mostrarPerfil());

        // Pagos y recompensas Section
        JLabel lblPagosTitulo = new JLabel("Pagos y recompensas");
        lblPagosTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPagosTitulo.setForeground(new Color(150, 150, 150));
        lblPagosTitulo.setBorder(BorderFactory.createEmptyBorder(15, 25, 10, 0));

        JButton btnMisCompras = crearBotonLateral("• Mis compras");
        btnMisCompras.addActionListener(e -> mostrarHistorial());

        JButton btnCanjearPuntos = crearBotonLateral("• Canjear puntos");
        btnCanjearPuntos.addActionListener(e -> {
            if (usuarioActual.esVIP()) {
                mostrarCanjePuntos();
            } else {
                mostrarPantallaComprarVIP();
            }
        });

        if (!usuarioActual.esVIP()) {
            btnCanjearPuntos.setEnabled(false);
            btnCanjearPuntos.setForeground(new Color(100, 100, 100));
        }

        JSeparator separador = new JSeparator();
        separador.setForeground(new Color(60, 60, 60));
        separador.setMaximumSize(new Dimension(200, 1));
        separador.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnCerrarSesion = crearBotonLateral("• Cerrar sesión");
        btnCerrarSesion.setForeground(new Color(220, 20, 60));
        btnCerrarSesion.addActionListener(e -> cerrarSesion());

        panel.add(lblCuentaTitulo);
        panel.add(btnPerfil);
        panel.add(lblPagosTitulo);
        panel.add(btnMisCompras);
        panel.add(btnCanjearPuntos);
        panel.add(Box.createVerticalStrut(20));
        panel.add(separador);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnCerrarSesion);
        panel.add(Box.createVerticalGlue());

        return panel;
    }
    //FIN DEL METODO NUEVO #1
    
    //Metodo Nuevo #2
    private void mostrarSeccionPerfil() {
    // Remover el panel lateral si existe
        Component[] components = getContentPane().getComponents();
        for (Component comp : components) {
            if (comp == panelLateral) {
                remove(panelLateral);
                break;
            }
        }

        // Agregar el panel lateral
        add(panelLateral, BorderLayout.WEST);

        // Mostrar el perfil en el contenido
        mostrarPerfil();

        revalidate();
        repaint();
    }
    //Fin del metodo nuevo #2
    //Hola!! Soy camilo estoy cansado jajajajajajaj
    private JButton crearBotonLateral(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        boton.setForeground(Color.WHITE);
        boton.setBackground(new Color(20, 20, 20));
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setContentAreaFilled(false);
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setBorder(BorderFactory.createEmptyBorder(8, 25, 8, 25));
        boton.setMaximumSize(new Dimension(250, 40));
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                boton.setBackground(new Color(35, 35, 35));
                boton.setOpaque(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                boton.setBackground(new Color(20, 20, 20));
                boton.setOpaque(false);
            }
        });

        return boton;
    }

    private ImageIcon createUserIcon() {
        int size = 20;
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fillOval(0, 0, size, size);
        g2.dispose();
        return new ImageIcon(img);
    }
    
    private JMenuItem crearItemMenu(String texto) {
        JMenuItem item = new JMenuItem(texto);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        item.setForeground(Color.WHITE);
        item.setBackground(new Color(30, 30, 30));
        item.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        item.setOpaque(true);
        
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                item.setBackground(new Color(45, 45, 45));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                item.setBackground(new Color(30, 30, 30));
            }
        });
        
        return item;
    }

    private void mostrarBienvenida() {
        panelContenido.removeAll();
        panelContenido.setLayout(new GridBagLayout());

        JPanel panelCentral = new JPanel();
        panelCentral.setBackground(new Color(30, 30, 30));
        panelCentral.setLayout(new BoxLayout(panelCentral, BoxLayout.Y_AXIS));

        JLabel lblBienvenida = new JLabel("Bienvenido, " + usuarioActual.getNombreCompleto() + "!");
        lblBienvenida.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblBienvenida.setForeground(Color.WHITE);
        lblBienvenida.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblMensaje = new JLabel("Selecciona una opción del menú para comenzar");
        lblMensaje.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblMensaje.setForeground(new Color(150, 150, 150));
        lblMensaje.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelCentral.add(lblBienvenida);
        panelCentral.add(Box.createVerticalStrut(15));
        panelCentral.add(lblMensaje);

        panelContenido.add(panelCentral);
        panelContenido.revalidate();
        panelContenido.repaint();
    }
    
    // PROGRAMA ORIGINAL A PARTIR DE ACA
    
    
private void mostrarCanjePuntos() {
    // ✅ REMOVER LA BARRA LATERAL SI EXISTE
    Component[] components = getContentPane().getComponents();
    for (Component comp : components) {
        if (comp == panelLateral) {
            remove(panelLateral);
            break;
        }
    }
    // Mostrar indicador de carga PRIMERO
    panelContenido.removeAll();
    panelContenido.setLayout(new GridBagLayout());
    
    JLabel lblCargando = new JLabel("Cargando productos...");
    lblCargando.setFont(new Font("Segoe UI", Font.BOLD, 18));
    lblCargando.setForeground(Color.WHITE);
    panelContenido.add(lblCargando);
    panelContenido.revalidate();
    panelContenido.repaint();
    
    // Cargar el contenido en un hilo separado para no bloquear la UI
    SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
        @Override
        protected Void doInBackground() throws Exception {
            // Recargar puntos desde la BD en segundo plano
            puntosUsuario = sistemaLogin.getDbHelper().obtenerPuntosCliente(usuarioActual.getCorreo());
            return null;
        }
        
        @Override
        protected void done() {
            // Una vez cargados los puntos, mostrar la interfaz
            mostrarInterfazCanjePuntos();
        }
    };
    
    worker.execute();
}

// NUEVO MÉTODO AUXILIAR
private void mostrarInterfazCanjePuntos() {
    panelContenido.removeAll();
    panelContenido.setLayout(new BorderLayout());

    JPanel panelPrincipal = new JPanel();
    panelPrincipal.setLayout(new BorderLayout());
    panelPrincipal.setBackground(new Color(30, 30, 30));

    // Panel superior con título y puntos
    JPanel panelSuperior = new JPanel();
    panelSuperior.setBackground(new Color(30, 30, 30));
    panelSuperior.setLayout(new BorderLayout());
    panelSuperior.setBorder(BorderFactory.createEmptyBorder(30, 40, 20, 40));

    JPanel panelIzquierdo = new JPanel();
    panelIzquierdo.setLayout(new BoxLayout(panelIzquierdo, BoxLayout.Y_AXIS));
    panelIzquierdo.setBackground(new Color(30, 30, 30));

    JLabel lblTitulo = new JLabel("Canjea tus puntos:");
    lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
    lblTitulo.setForeground(Color.WHITE);
    lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

    panelIzquierdo.add(lblTitulo);

    JPanel panelDerecho = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    panelDerecho.setBackground(new Color(30, 30, 30));

    JLabel lblPuntosDisponibles = new JLabel("Tus puntos Cinemax: " + puntosUsuario);
    lblPuntosDisponibles.setFont(new Font("Segoe UI", Font.BOLD, 16));
    lblPuntosDisponibles.setForeground(Color.WHITE);
    lblPuntosDisponibles.setBackground(new Color(220, 20, 60));
    lblPuntosDisponibles.setOpaque(true);
    lblPuntosDisponibles.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    panelDerecho.add(lblPuntosDisponibles);

    panelSuperior.add(panelIzquierdo, BorderLayout.WEST);
    panelSuperior.add(panelDerecho, BorderLayout.EAST);

    // Panel central con productos - GRID MÁS ESPACIADO
    JPanel panelProductos = new JPanel();
    panelProductos.setLayout(new GridLayout(2, 2, 30, 30)); // Menos espaciado
    panelProductos.setBackground(new Color(30, 30, 30));
    panelProductos.setBorder(BorderFactory.createEmptyBorder(20, 30, 40, 30)); // Mejor distribución

    // Productos disponibles con sus precios
    String[][] productos = {
        {"COMBO PANCHO", "2000"},
        {"COMBO POCHOCLO", "5000"},
        {"COMBO NACHOS", "2000"},
        {"GASEOSA", "1500"}
    };

    ButtonGroup grupo = new ButtonGroup();
    JRadioButton[] radios = new JRadioButton[4];

    for (int i = 0; i < productos.length; i++) {
        JPanel panelProducto = crearPanelProducto(productos[i][0], productos[i][1], true);
        radios[i] = (JRadioButton) panelProducto.getComponent(0);
        grupo.add(radios[i]);
        panelProductos.add(panelProducto);
    }

    // Panel inferior con botón de canje
    JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER));
    panelInferior.setBackground(new Color(30, 30, 30));
    panelInferior.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

    JButton btnCanjear = new JButton("CANJEAR PUNTOS");
    btnCanjear.setPreferredSize(new Dimension(250, 50));
    btnCanjear.setMaximumSize(new Dimension(250, 50));
    btnCanjear.setBackground(new Color(220, 20, 60));
    btnCanjear.setForeground(Color.WHITE);
    btnCanjear.setFont(new Font("Segoe UI", Font.BOLD, 16));
    btnCanjear.setFocusPainted(false);
    btnCanjear.setBorderPainted(false);
    btnCanjear.setCursor(new Cursor(Cursor.HAND_CURSOR));

    // Panel para mensajes
    JPanel panelMensajes = new JPanel(new FlowLayout(FlowLayout.CENTER));
    panelMensajes.setBackground(new Color(30, 30, 30));
    panelMensajes.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
    panelMensajes.setVisible(false);

    JLabel lblMensaje = new JLabel();
    lblMensaje.setFont(new Font("Segoe UI", Font.BOLD, 13));
    lblMensaje.setForeground(new Color(255, 87, 51));
    panelMensajes.add(lblMensaje);

    btnCanjear.addActionListener(e -> {
        int seleccionado = -1;
        for (int i = 0; i < radios.length; i++) {
            if (radios[i].isSelected()) {
                seleccionado = i;
                break;
            }
        }

        if (seleccionado == -1) {
            lblMensaje.setText("Selecciona un producto para canjear");
            lblMensaje.setForeground(new Color(255, 152, 0));
            panelMensajes.setVisible(true);
            
            Timer timer = new Timer(3000, evt -> panelMensajes.setVisible(false));
            timer.setRepeats(false);
            timer.start();
            return;
        }

        String productoNombre = productos[seleccionado][0];
        int puntosRequeridos = Integer.parseInt(productos[seleccionado][1]);

        if (puntosUsuario >= puntosRequeridos) {
            // Mostrar indicador de procesamiento
            btnCanjear.setEnabled(false);
            btnCanjear.setText("PROCESANDO...");
            
            // Procesar en segundo plano
            SwingWorker<Boolean, Void> canjeWorker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return sistemaLogin.getDbHelper().registrarCanje(usuarioActual.getCorreo(), productoNombre, puntosRequeridos);
                }
                
                @Override
                protected void done() {
                    try {
                        if (get()) {
                            puntosUsuario -= puntosRequeridos;
                            mostrarPantallaCanjeoExitoso(productoNombre, puntosRequeridos);
                            grupo.clearSelection();
                        } else {
                            lblMensaje.setText("Error al procesar el canje. Intenta de nuevo.");
                            lblMensaje.setForeground(new Color(244, 67, 54));
                            panelMensajes.setVisible(true);
                            btnCanjear.setEnabled(true);
                            btnCanjear.setText("CANJEAR PUNTOS");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        btnCanjear.setEnabled(true);
                        btnCanjear.setText("CANJEAR PUNTOS");
                    }
                }
            };
            
            canjeWorker.execute();
            
        } else {
            lblMensaje.setText("No tienes suficientes puntos para canjear este producto");
            lblMensaje.setForeground(new Color(255, 152, 0));
            panelMensajes.setVisible(true);

            Timer timer = new Timer(4000, evt -> panelMensajes.setVisible(false));
            timer.setRepeats(false);
            timer.start();
        }
    });

    panelInferior.add(panelMensajes);
    panelInferior.add(btnCanjear);

    panelPrincipal.add(panelSuperior, BorderLayout.NORTH);
    panelPrincipal.add(panelProductos, BorderLayout.CENTER);
    panelPrincipal.add(panelInferior, BorderLayout.SOUTH);

    panelContenido.add(panelPrincipal);
    panelContenido.revalidate();
    panelContenido.repaint();
}

private JPanel crearPanelProducto(String nombre, String puntos, boolean mostrarPuntos) {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.setBackground(new Color(30, 30, 30));
    panel.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 50), 2));
    panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
    
// Tamaño JUSTO al tamaño de la imagen + texto
int anchoPanel, altoPanel;

if (nombre.equals("COMBO PANCHO") || nombre.equals("COMBO POCHOCLO")) {
    // Combos: imagen 160x160 + radio + texto = 170x230
    anchoPanel = 170;
    altoPanel = 230;
} else {
    // Películas: imagen 130x240 + radio + texto = 140x310
    anchoPanel = 140;
    altoPanel = 310;
}

panel.setPreferredSize(new Dimension(anchoPanel, altoPanel));
panel.setMinimumSize(new Dimension(anchoPanel, altoPanel));
panel.setMaximumSize(new Dimension(anchoPanel, altoPanel));

    panel.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            panel.setBorder(BorderFactory.createLineBorder(new Color(220, 20, 60), 2));
        }
        
        @Override
        public void mouseExited(MouseEvent e) {
            panel.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 50), 2));
        }
    });

    // Radio button pequeño arriba
    JRadioButton radio = new JRadioButton();
    radio.setBackground(new Color(30, 30, 30));
    radio.setFocusPainted(false);
    radio.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));

    // Panel para la imagen - que se vea COMPLETA
    JPanel panelImagenContainer = new JPanel();
    panelImagenContainer.setBackground(new Color(30, 30, 30));
    panelImagenContainer.setLayout(new GridBagLayout()); // Para centrar
    panelImagenContainer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
    JLabel lblImagen = new JLabel();
    lblImagen.setHorizontalAlignment(SwingConstants.CENTER);
    lblImagen.setVerticalAlignment(SwingConstants.CENTER);

    String urlImagen = "";
    int anchoImg = 160;
    int altoImg = 240;
    
    switch(nombre) {
        case "COMBO PANCHO":
            urlImagen = "https://gaelschenone.aguilucho.ar/source_cmx/index.php?preview=M7%2F8.png";
            anchoImg = 155;
            altoImg = 155;
            break;
        case "COMBO POCHOCLO":
            urlImagen = "https://gaelschenone.aguilucho.ar/source_cmx/index.php?preview=M7%2F9.png";
            anchoImg = 155;
            altoImg = 155;
            break;
        case "COMBO NACHOS":
            urlImagen = "https://gaelschenone.aguilucho.ar/source_cmx/index.php?preview=M7%2F7.png";
            anchoImg = 155;
            altoImg = 155;
            break;
        case "GASEOSA":
            urlImagen = "https://gaelschenone.aguilucho.ar/source_cmx/index.php?preview=M7%2F5.png";
            anchoImg = 160;
            altoImg = 160;
            break;
    }

    if (!urlImagen.isEmpty()) {
        try {
            java.net.URL url = new java.net.URL(urlImagen);
            ImageIcon icon = new ImageIcon(url);
            // Redimensionar manteniendo proporción
            Image img = icon.getImage().getScaledInstance(anchoImg, altoImg, Image.SCALE_SMOOTH);
            lblImagen.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            lblImagen.setText("🍿");
            lblImagen.setFont(new Font("Segoe UI", Font.PLAIN, 40));
            lblImagen.setForeground(Color.WHITE);
        }
    }

    panelImagenContainer.add(lblImagen);

    // Panel de información abajo
    JPanel panelInfo = new JPanel();
    panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS));
    panelInfo.setBackground(new Color(30, 30, 30));
    panelInfo.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));

    JLabel lblNombre = new JLabel(nombre);
    lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 13));
    lblNombre.setForeground(Color.WHITE);
    lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);
    panelInfo.add(lblNombre);

    if (mostrarPuntos) {
        panelInfo.add(Box.createVerticalStrut(3));
        JLabel lblPuntos = new JLabel(puntos + " puntos");
        lblPuntos.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPuntos.setForeground(new Color(220, 20, 60));
        lblPuntos.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelInfo.add(lblPuntos);
    }

    panel.add(radio, BorderLayout.NORTH);
    panel.add(panelImagenContainer, BorderLayout.CENTER);
    panel.add(panelInfo, BorderLayout.SOUTH);

    // Click en toda la tarjeta
    MouseAdapter clickListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            radio.setSelected(true);
        }
        
        @Override
        public void mouseEntered(MouseEvent e) {
            panel.setBackground(new Color(35, 35, 35));
            panelImagenContainer.setBackground(new Color(35, 35, 35));
            panelInfo.setBackground(new Color(35, 35, 35));
            radio.setBackground(new Color(35, 35, 35));
        }
        
        @Override
        public void mouseExited(MouseEvent e) {
            panel.setBackground(new Color(30, 30, 30));
            panelImagenContainer.setBackground(new Color(30, 30, 30));
            panelInfo.setBackground(new Color(30, 30, 30));
            radio.setBackground(new Color(30, 30, 30));
        }
    };

    panel.addMouseListener(clickListener);
    panelImagenContainer.addMouseListener(clickListener);
    lblImagen.addMouseListener(clickListener);
    panelInfo.addMouseListener(clickListener);

    return panel;
}

private void mostrarHistorial() {
    // ✅ REMOVER LA BARRA LATERAL SI EXISTE
    Component[] components = getContentPane().getComponents();
    for (Component comp : components) {
        if (comp == panelLateral) {
            remove(panelLateral);
            break;
        }
    }
    
    panelContenido.removeAll();
    panelContenido.setLayout(new BorderLayout());

    // Usar el nuevo panel VerHistorial
    VerHistorial panelHistorial = new VerHistorial(usuarioActual.getIdCliente());
    
    panelContenido.add(panelHistorial, BorderLayout.CENTER);
    panelContenido.revalidate();
    panelContenido.repaint();
}

    private void mostrarPerfil() {
        panelContenido.removeAll();
        panelContenido.setLayout(new BorderLayout());

        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
        panelPrincipal.setBackground(new Color(30, 30, 30));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel lblTitulo = new JLabel("Mi Perfil");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubtitulo = new JLabel("Gestiona los datos de tu cuenta.");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitulo.setForeground(new Color(180, 180, 180));
        lblSubtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSeccion1 = new JLabel("Información personal");
        lblSeccion1.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSeccion1.setForeground(Color.WHITE);
        lblSeccion1.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSeccion1.setBorder(BorderFactory.createEmptyBorder(25, 0, 10, 0));

        JPanel panelCampos = new JPanel();
        panelCampos.setLayout(new BoxLayout(panelCampos, BoxLayout.Y_AXIS));
        panelCampos.setBackground(new Color(30, 30, 30));
        panelCampos.setAlignmentX(Component.LEFT_ALIGNMENT);

        // NOMBRE
        JLabel lblNombreLabel = new JLabel("Nombre");
        lblNombreLabel.setForeground(new Color(180, 180, 180));
        lblNombreLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNombreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField txtNombre = new JTextField(usuarioActual.getNombre());
        txtNombre.setMaximumSize(new Dimension(500, 45));
        txtNombre.setBackground(new Color(50, 50, 50));
        txtNombre.setForeground(Color.WHITE);
        txtNombre.setCaretColor(Color.WHITE);
        txtNombre.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNombre.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 70)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        txtNombre.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtNombre.setEditable(false);

        panelCampos.add(lblNombreLabel);
        panelCampos.add(Box.createVerticalStrut(5));
        panelCampos.add(txtNombre);
        panelCampos.add(Box.createVerticalStrut(15));

        // APELLIDO
        JLabel lblApellidoLabel = new JLabel("Apellido");
        lblApellidoLabel.setForeground(new Color(180, 180, 180));
        lblApellidoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblApellidoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String apellidoActual = usuarioActual.getApellido() != null ? usuarioActual.getApellido() : "";
        JTextField txtApellido = new JTextField(apellidoActual);
        txtApellido.setMaximumSize(new Dimension(500, 45));
        txtApellido.setBackground(new Color(50, 50, 50));
        txtApellido.setForeground(Color.WHITE);
        txtApellido.setCaretColor(Color.WHITE);
        txtApellido.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtApellido.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 70)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        txtApellido.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtApellido.setEditable(false);

        panelCampos.add(lblApellidoLabel);
        panelCampos.add(Box.createVerticalStrut(5));
        panelCampos.add(txtApellido);
        panelCampos.add(Box.createVerticalStrut(15));

        // DNI
        JLabel lblDNILabel = new JLabel("DNI");
        lblDNILabel.setForeground(new Color(180, 180, 180));
        lblDNILabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDNILabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField txtDNI = new JTextField(String.valueOf(usuarioActual.getDNI()));
        txtDNI.setMaximumSize(new Dimension(500, 45));
        txtDNI.setBackground(new Color(50, 50, 50));
        txtDNI.setForeground(Color.WHITE);
        txtDNI.setCaretColor(Color.WHITE);
        txtDNI.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtDNI.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 70)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        txtDNI.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtDNI.setEditable(false);

        panelCampos.add(lblDNILabel);
        panelCampos.add(Box.createVerticalStrut(5));
        panelCampos.add(txtDNI);
        panelCampos.add(Box.createVerticalStrut(15));

        // FECHA DE NACIMIENTO
        JLabel lblFechaNacLabel = new JLabel("Fecha de Nacimiento");
        lblFechaNacLabel.setForeground(new Color(180, 180, 180));
        lblFechaNacLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFechaNacLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        java.sql.Date fechaNac = usuarioActual.getFechaNacimiento();
        String fechaNacStr = "";
        if (fechaNac != null) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(fechaNac);
            fechaNacStr = String.format("%02d/%02d/%04d", 
                cal.get(java.util.Calendar.DAY_OF_MONTH),
                cal.get(java.util.Calendar.MONTH) + 1,
                cal.get(java.util.Calendar.YEAR));
        }

        JTextField txtFechaNac = new JTextField(fechaNacStr);
        txtFechaNac.setMaximumSize(new Dimension(500, 45));
        txtFechaNac.setBackground(new Color(50, 50, 50));
        txtFechaNac.setForeground(Color.WHITE);
        txtFechaNac.setCaretColor(Color.WHITE);
        txtFechaNac.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtFechaNac.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 70)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        txtFechaNac.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtFechaNac.setEditable(false);

        panelCampos.add(lblFechaNacLabel);
        panelCampos.add(Box.createVerticalStrut(5));
        panelCampos.add(txtFechaNac);
        panelCampos.add(Box.createVerticalStrut(15));

        JLabel lblSeccion2 = new JLabel("Información de la cuenta");
        lblSeccion2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSeccion2.setForeground(Color.WHITE);
        lblSeccion2.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSeccion2.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        panelCampos.add(lblSeccion2);

        // CORREO
        JLabel lblCorreoLabel = new JLabel("Dirección de correo electrónico");
        lblCorreoLabel.setForeground(new Color(180, 180, 180));
        lblCorreoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCorreoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField txtCorreo = new JTextField(usuarioActual.getCorreo());
        txtCorreo.setMaximumSize(new Dimension(500, 45));
        txtCorreo.setBackground(new Color(50, 50, 50));
        txtCorreo.setForeground(Color.WHITE);
        txtCorreo.setCaretColor(Color.WHITE);
        txtCorreo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtCorreo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 70)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        txtCorreo.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtCorreo.setEditable(false);

        panelCampos.add(lblCorreoLabel);
        panelCampos.add(Box.createVerticalStrut(5));
        panelCampos.add(txtCorreo);
        panelCampos.add(Box.createVerticalStrut(15));

        // CONTRASEÑA
        JLabel lblPassInfoLabel = new JLabel("Contraseña");
        lblPassInfoLabel.setForeground(new Color(180, 180, 180));
        lblPassInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblPassInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPasswordField txtPassInfo = new JPasswordField(usuarioActual.getContrasena());
        txtPassInfo.setMaximumSize(new Dimension(500, 45));
        txtPassInfo.setBackground(new Color(50, 50, 50));
        txtPassInfo.setForeground(Color.WHITE);
        txtPassInfo.setCaretColor(Color.WHITE);
        txtPassInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassInfo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 70)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        txtPassInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtPassInfo.setEditable(false);

        panelCampos.add(lblPassInfoLabel);
        panelCampos.add(Box.createVerticalStrut(5));
        panelCampos.add(txtPassInfo);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 20));
        panelBotones.setBackground(new Color(30, 30, 30));
        panelBotones.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnEditar = new JButton("Editar Datos");
        btnEditar.setPreferredSize(new Dimension(150, 40));
        btnEditar.setMaximumSize(new Dimension(150, 40));
        btnEditar.setBackground(new Color(220, 20, 60));
        btnEditar.setForeground(Color.WHITE);
        btnEditar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnEditar.setFocusPainted(false);
        btnEditar.setBorderPainted(false);
        btnEditar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnGuardar = new JButton("Guardar Cambios");
        btnGuardar.setPreferredSize(new Dimension(150, 40));
        btnGuardar.setMaximumSize(new Dimension(150, 40));
        btnGuardar.setBackground(new Color(220, 20, 60));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnGuardar.setFocusPainted(false);
        btnGuardar.setBorderPainted(false);
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGuardar.setVisible(false);

        panelBotones.add(btnEditar);
        panelBotones.add(btnGuardar);

        btnEditar.addActionListener(e -> {
            txtNombre.setEditable(true);
            txtApellido.setEditable(true);
            txtDNI.setEditable(true);
            txtFechaNac.setEditable(true);
            txtCorreo.setEditable(true);
            txtPassInfo.setEditable(true);
            txtPassInfo.setEchoChar((char) 0);
            btnEditar.setVisible(false);
            btnGuardar.setVisible(true);
        });

        // Variable para el mensaje de confirmación
        final JLabel[] lblMensajeExito = new JLabel[1];
        final JLabel[] lblMensajeError = new JLabel[1];

        btnGuardar.addActionListener(e -> {
            String nuevoNombre = txtNombre.getText().trim();
            String nuevoApellido = txtApellido.getText().trim();
            String nuevoDNIStr = txtDNI.getText().trim();
            String nuevaFechaStr = txtFechaNac.getText().trim();
            String nuevoCorreo = txtCorreo.getText().trim();
            String nuevaPass = new String(txtPassInfo.getPassword()).trim();

            // Ocultar mensajes previos
            if (lblMensajeExito[0] != null) lblMensajeExito[0].setVisible(false);
            if (lblMensajeError[0] != null) lblMensajeError[0].setVisible(false);

            if (nuevoNombre.isEmpty() || nuevoApellido.isEmpty() || nuevoDNIStr.isEmpty() || 
                nuevaFechaStr.isEmpty() || nuevoCorreo.isEmpty() || nuevaPass.isEmpty()) {
                mostrarMensajeError("Todos los campos son obligatorios", lblMensajeError, panelBotones);
                return;
            }
            if (!validarSoloLetras(nuevoNombre)) {
            mostrarMensajeError("El nombre solo puede contener letras", lblMensajeError, panelBotones);
            return;
            }   
            if (!validarSoloLetras(nuevoApellido)) {
            mostrarMensajeError("El apellido solo puede contener letras", lblMensajeError, panelBotones);
            return;
            }
            if (nuevaPass.length() < 6) {
                mostrarMensajeError("La contraseña debe tener al menos 6 caracteres", lblMensajeError, panelBotones);
                return;
            }
            
            int nuevoDNI;
            try {
                nuevoDNI = Integer.parseInt(nuevoDNIStr);
                if (nuevoDNIStr.length() != 8) {
                    mostrarMensajeError("El DNI debe tener exactamente 8 dígitos", lblMensajeError, panelBotones);
                    return;
                }
            } catch (NumberFormatException ex) {
                mostrarMensajeError("El DNI debe contener solo números", lblMensajeError, panelBotones);
                return;
            }

            java.sql.Date nuevaFechaNac;
            try {
                String[] partes = nuevaFechaStr.split("/");
                if (partes.length != 3) {
                    mostrarMensajeError("La fecha debe tener el formato DD/MM/AAAA", lblMensajeError, panelBotones);
                    return;
                }
                int dia = Integer.parseInt(partes[0]);
                int mes = Integer.parseInt(partes[1]);
                int anio = Integer.parseInt(partes[2]);

                // Validar rangos básicos
                if (dia < 1 || dia > 31) {
                    mostrarMensajeError("El día debe estar entre 1 y 31", lblMensajeError, panelBotones);
                    return;
                }
                if (mes < 1 || mes > 12) {
                    mostrarMensajeError("El mes debe estar entre 1 y 12", lblMensajeError, panelBotones);
                    return;
                }
                if (anio < 1920 || anio > 2024) {
                    mostrarMensajeError("El año debe estar entre 1920 y 2024", lblMensajeError, panelBotones);
                    return;
                }
                
                // Validar días según el mes
                if (mes == 2) { // Febrero
                    boolean esBisiesto = (anio % 4 == 0 && anio % 100 != 0) || (anio % 400 == 0);
                    if (dia > (esBisiesto ? 29 : 28)) {
                        mostrarMensajeError("Febrero no puede tener más de " + (esBisiesto ? "29" : "28") + " días", lblMensajeError, panelBotones);
                        return;
                    }
                } else if (mes == 4 || mes == 6 || mes == 9 || mes == 11) { // Meses de 30 días
                    if (dia > 30) {
                        mostrarMensajeError("Este mes no puede tener más de 30 días", lblMensajeError, panelBotones);
                        return;
                    }
                }

                String fechaSQL = String.format("%04d-%02d-%02d", anio, mes, dia);
                nuevaFechaNac = java.sql.Date.valueOf(fechaSQL);
            } catch (NumberFormatException ex) {
                mostrarMensajeError("La fecha debe contener solo números", lblMensajeError, panelBotones);
                return;
            } catch (Exception ex) {
                mostrarMensajeError("La fecha ingresada no es válida", lblMensajeError, panelBotones);
                return;
            }

            if (!nuevoCorreo.toLowerCase().endsWith("@gmail.com")) {
                mostrarMensajeError("El correo debe terminar en @gmail.com", lblMensajeError, panelBotones);
                return;
            }

            DatabaseHelper dbHelper = sistemaLogin.getDbHelper();

            if (dbHelper.actualizarDatosCliente(usuarioActual.getCorreo(), nuevoNombre, nuevoApellido,
                                                nuevoCorreo, nuevaPass, nuevoDNI, nuevaFechaNac)) {
                usuarioActual.setNombre(nuevoNombre);
                usuarioActual.setApellido(nuevoApellido);
                usuarioActual.setCorreo(nuevoCorreo);
                usuarioActual.setContrasena(nuevaPass);
                usuarioActual.setDNI(nuevoDNI);
                usuarioActual.setFechaNacimiento(nuevaFechaNac);

                // Mostrar mensaje de éxito elegante
                if (lblMensajeExito[0] == null) {
                    lblMensajeExito[0] = new JLabel("Datos guardados correctamente");
                    lblMensajeExito[0].setFont(new Font("Segoe UI", Font.BOLD, 14));
                    lblMensajeExito[0].setForeground(new Color(76, 175, 80));
                    lblMensajeExito[0].setAlignmentX(Component.LEFT_ALIGNMENT);
                    lblMensajeExito[0].setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
                    panelBotones.add(lblMensajeExito[0]);
                }
                lblMensajeExito[0].setVisible(true);
                panelBotones.revalidate();
                panelBotones.repaint();

                // Deshabilitar edición
                txtNombre.setEditable(false);
                txtApellido.setEditable(false);
                txtDNI.setEditable(false);
                txtFechaNac.setEditable(false);
                txtCorreo.setEditable(false);
                txtPassInfo.setEditable(false);
                txtPassInfo.setEchoChar('•');
                btnEditar.setVisible(true);
                btnGuardar.setVisible(false);

                // Ocultar mensaje después de 3 segundos
                Timer timer = new Timer(3000, evt -> {
                    if (lblMensajeExito[0] != null) {
                        lblMensajeExito[0].setVisible(false);
                    }
                });
                timer.setRepeats(false);
                timer.start();
            } else {
                mostrarMensajeError("Error: El correo o DNI ya están registrados", lblMensajeError, panelBotones);
            }
        });

        panelPrincipal.add(lblTitulo);
        panelPrincipal.add(Box.createVerticalStrut(5));
        panelPrincipal.add(lblSubtitulo);
        panelPrincipal.add(lblSeccion1);
        panelPrincipal.add(Box.createVerticalStrut(10));
        panelPrincipal.add(panelCampos);
        panelPrincipal.add(panelBotones);

        JScrollPane scrollPane = new JScrollPane(panelPrincipal);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(30, 30, 30));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        panelContenido.add(scrollPane, BorderLayout.CENTER);
        panelContenido.revalidate();
        panelContenido.repaint();
    }

private void mostrarPantallaCanjeoExitoso(String producto, int puntos) {
    panelContenido.removeAll();
    panelContenido.setLayout(new GridBagLayout());

    JPanel panelExito = new JPanel();
    panelExito.setLayout(new BoxLayout(panelExito, BoxLayout.Y_AXIS));
    panelExito.setBackground(new Color(30, 30, 30));
    panelExito.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

    // Panel para la imagen del producto con borde verde
    JPanel panelImagenContainer = new JPanel();
    panelImagenContainer.setLayout(new BorderLayout());
    panelImagenContainer.setBackground(new Color(30, 30, 30));
    panelImagenContainer.setBorder(BorderFactory.createLineBorder(new Color(76, 175, 80), 4));
    panelImagenContainer.setMaximumSize(new Dimension(200, 200));
    panelImagenContainer.setPreferredSize(new Dimension(200, 200));
    panelImagenContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

    JLabel lblImagenProducto = new JLabel();
    lblImagenProducto.setHorizontalAlignment(SwingConstants.CENTER);
    lblImagenProducto.setVerticalAlignment(SwingConstants.CENTER);

    // Obtener la URL de la imagen según el producto
    String urlImagen = "";
    int anchoImg = 180;
    int altoImg = 180;
    
    switch(producto.toUpperCase()) {
        case "COMBO PANCHO":
            urlImagen = "https://gaelschenone.aguilucho.ar/source_cmx/index.php?preview=M7%2F8.png";
            break;
        case "COMBO POCHOCLO":
            urlImagen = "https://gaelschenone.aguilucho.ar/source_cmx/index.php?preview=M7%2F9.png";
            break;
        case "COMBO NACHOS":
            urlImagen = "https://gaelschenone.aguilucho.ar/source_cmx/index.php?preview=M7%2F7.png";
            break;
        case "GASEOSA":
            urlImagen = "https://gaelschenone.aguilucho.ar/source_cmx/index.php?preview=M7%2F5.png";
            break;
        default:
            System.out.println("Producto no reconocido: " + producto);
    }

    System.out.println("Intentando cargar imagen para: " + producto);
    System.out.println("URL: " + urlImagen);

    if (!urlImagen.isEmpty()) {
        try {
            java.net.URL url = new java.net.URL(urlImagen);
            ImageIcon icon = new ImageIcon(url);
            
            // Verificar si la imagen se cargó correctamente
            if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                Image img = icon.getImage().getScaledInstance(anchoImg, altoImg, Image.SCALE_SMOOTH);
                lblImagenProducto.setIcon(new ImageIcon(img));
                System.out.println("Imagen cargada exitosamente");
            } else {
                System.out.println("Error al cargar la imagen");
                lblImagenProducto.setText(" ");
                lblImagenProducto.setFont(new Font("Segoe UI", Font.BOLD, 80));
                lblImagenProducto.setForeground(new Color(76, 175, 80));
            }
        } catch (Exception e) {
            System.out.println("Excepción al cargar imagen: " + e.getMessage());
            e.printStackTrace();
            lblImagenProducto.setText("");
            lblImagenProducto.setFont(new Font("Segoe UI", Font.BOLD, 80));
            lblImagenProducto.setForeground(new Color(76, 175, 80));
        }
    } else {
        lblImagenProducto.setText("✓");
        lblImagenProducto.setFont(new Font("Segoe UI", Font.BOLD, 80));
        lblImagenProducto.setForeground(new Color(76, 175, 80));
    }

    panelImagenContainer.add(lblImagenProducto, BorderLayout.CENTER);

    JLabel lblTitulo = new JLabel("¡Canje exitoso!");
    lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 32));
    lblTitulo.setForeground(Color.WHITE);
    lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

    JLabel lblProducto = new JLabel(producto);
    lblProducto.setFont(new Font("Segoe UI", Font.PLAIN, 18));
    lblProducto.setForeground(new Color(180, 180, 180));
    lblProducto.setAlignmentX(Component.CENTER_ALIGNMENT);

    JLabel lblPuntos = new JLabel(puntos + " puntos utilizados");
    lblPuntos.setFont(new Font("Segoe UI", Font.BOLD, 16));
    lblPuntos.setForeground(new Color(220, 20, 60));
    lblPuntos.setAlignmentX(Component.CENTER_ALIGNMENT);

    JLabel lblRestantes = new JLabel("Puntos restantes: " + puntosUsuario);
    lblRestantes.setFont(new Font("Segoe UI", Font.PLAIN, 15));
    lblRestantes.setForeground(new Color(150, 150, 150));
    lblRestantes.setAlignmentX(Component.CENTER_ALIGNMENT);

    JButton btnVolver = new JButton("Volver a canjes");
    btnVolver.setPreferredSize(new Dimension(200, 45));
    btnVolver.setMaximumSize(new Dimension(200, 45));
    btnVolver.setBackground(new Color(220, 20, 60));
    btnVolver.setForeground(Color.WHITE);
    btnVolver.setFont(new Font("Segoe UI", Font.BOLD, 14));
    btnVolver.setFocusPainted(false);
    btnVolver.setBorderPainted(false);
    btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btnVolver.setAlignmentX(Component.CENTER_ALIGNMENT);
    btnVolver.addActionListener(e -> mostrarCanjePuntos());

    panelExito.add(panelImagenContainer);
    panelExito.add(Box.createVerticalStrut(25));
    panelExito.add(lblTitulo);
    panelExito.add(Box.createVerticalStrut(15));
    panelExito.add(lblProducto);
    panelExito.add(Box.createVerticalStrut(10));
    panelExito.add(lblPuntos);
    panelExito.add(Box.createVerticalStrut(10));
    panelExito.add(lblRestantes);
    panelExito.add(Box.createVerticalStrut(30));
    panelExito.add(btnVolver);

    panelContenido.add(panelExito);
    panelContenido.revalidate();
    panelContenido.repaint();
}
    
    private void mostrarLogin() {
        mostrarPantallaLogin();
    }

    private void mostrarPantallaLogin() {
        JDialog dialog = new JDialog((Frame)null, "Iniciar Sesión", true);
        dialog.setSize(1366, 768);
        dialog.setLocationRelativeTo(null);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.getContentPane().setBackground(new Color(50, 50, 50));
        dialog.setLayout(new BorderLayout());

        JPanel panelContenedor = new JPanel(new CardLayout());
        panelContenedor.setBackground(new Color(50, 50, 50));

        JPanel panelLogin = crearPanelLogin(dialog, panelContenedor);
        JPanel panelRegistro = crearPanelRegistro(dialog, panelContenedor);

        panelContenedor.add(panelLogin, "LOGIN");
        panelContenedor.add(panelRegistro, "REGISTRO");

        dialog.add(panelContenedor);

        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                System.exit(0);
            }
        });

        dialog.setVisible(true);
    }

private JPanel crearPanelLogin(JDialog dialog, JPanel panelContenedor) {
    JPanel panelPrincipal = new JPanel();
    panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
    panelPrincipal.setBackground(new Color(15, 15, 15));
    panelPrincipal.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));

    // LOGO ELEGANTE
JLabel lblLogo = new JLabel();
lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
try {
    java.net.URL url = new java.net.URL("https://gaelschenone.aguilucho.ar/source_cmx/index.php?preview=logos%2FCINEMARX%20logotipo.png");
    ImageIcon icon = new ImageIcon(url);
    
    // ✅ CORRECCIÓN: Calcular altura proporcional
    int anchoOriginal = icon.getIconWidth();
    int altoOriginal = icon.getIconHeight();
    int anchoDeseado = 220;
    int altoDeseado = (altoOriginal * anchoDeseado) / anchoOriginal;
    
    Image img = icon.getImage().getScaledInstance(anchoDeseado, altoDeseado, Image.SCALE_SMOOTH);
    lblLogo.setIcon(new ImageIcon(img));
} catch (Exception e) {
    lblLogo.setText("CINEMARX");
    lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 22));
    lblLogo.setForeground(new Color(220, 20, 60));
}
lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
lblLogo.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

    // PANEL PRINCIPAL CON DISEÑO PREMIUM
    JPanel panelContenido = new JPanel();
    panelContenido.setBackground(new Color(25, 25, 28));
    panelContenido.setLayout(new BoxLayout(panelContenido, BoxLayout.Y_AXIS));
    panelContenido.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(40, 40, 45), 1),
        BorderFactory.createEmptyBorder(45, 60, 45, 60)
    ));
    panelContenido.setMaximumSize(new Dimension(480, 450));
    panelContenido.setAlignmentX(Component.CENTER_ALIGNMENT);

    // TÍTULO Y SUBTÍTULO
    JLabel lblTitulo = new JLabel("Bienvenido de vuelta");
    lblTitulo.setForeground(Color.WHITE);
    lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
    lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

    JLabel lblSubtitulo = new JLabel("Ingresa a tu cuenta para continuar");
    lblSubtitulo.setForeground(new Color(160, 160, 165));
    lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

    panelContenido.add(lblTitulo);
    panelContenido.add(Box.createVerticalStrut(8));
    panelContenido.add(lblSubtitulo);
    panelContenido.add(Box.createVerticalStrut(35));

    // CORREO ELECTRÓNICO
    JLabel lblCorreoLabel = new JLabel("Correo electrónico");
    lblCorreoLabel.setForeground(new Color(200, 200, 205));
    lblCorreoLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
    lblCorreoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

    JTextField txtCorreoLogin = new JTextField();
    txtCorreoLogin.setMaximumSize(new Dimension(360, 48));
    txtCorreoLogin.setBackground(new Color(18, 18, 20));
    txtCorreoLogin.setForeground(Color.WHITE);
    txtCorreoLogin.setCaretColor(new Color(220, 20, 60));
    txtCorreoLogin.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    txtCorreoLogin.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(50, 50, 55), 1),
        BorderFactory.createEmptyBorder(10, 16, 10, 16)));
    txtCorreoLogin.setAlignmentX(Component.CENTER_ALIGNMENT);

    txtCorreoLogin.addFocusListener(new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
            txtCorreoLogin.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 20, 60), 2),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));
        }
        @Override
        public void focusLost(FocusEvent e) {
            txtCorreoLogin.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 50, 55), 1),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));
        }
    });

    panelContenido.add(lblCorreoLabel);
    panelContenido.add(Box.createVerticalStrut(8));
    panelContenido.add(txtCorreoLogin);
    panelContenido.add(Box.createVerticalStrut(20));

    // CONTRASEÑA
    JLabel lblPassLabel = new JLabel("Contraseña");
    lblPassLabel.setForeground(new Color(200, 200, 205));
    lblPassLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
    lblPassLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

    JPasswordField txtPassLogin = new JPasswordField();
    txtPassLogin.setMaximumSize(new Dimension(360, 48));
    txtPassLogin.setBackground(new Color(18, 18, 20));
    txtPassLogin.setForeground(Color.WHITE);
    txtPassLogin.setCaretColor(new Color(220, 20, 60));
    txtPassLogin.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    txtPassLogin.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(50, 50, 55), 1),
        BorderFactory.createEmptyBorder(10, 16, 10, 16)));
    txtPassLogin.setAlignmentX(Component.CENTER_ALIGNMENT);

    txtPassLogin.addFocusListener(new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
            txtPassLogin.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 20, 60), 2),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));
        }
        @Override
        public void focusLost(FocusEvent e) {
            txtPassLogin.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 50, 55), 1),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));
        }
    });

    panelContenido.add(lblPassLabel);
    panelContenido.add(Box.createVerticalStrut(8));
    panelContenido.add(txtPassLogin);
    panelContenido.add(Box.createVerticalStrut(30));

    // BOTÓN INICIAR SESIÓN - PREMIUM
    JButton btnIniciar = new JButton("Iniciar sesión");
    btnIniciar.setMaximumSize(new Dimension(360, 50));
    btnIniciar.setAlignmentX(Component.CENTER_ALIGNMENT);
    btnIniciar.setBackground(new Color(220, 20, 60));
    btnIniciar.setForeground(Color.WHITE);
    btnIniciar.setFont(new Font("Segoe UI", Font.BOLD, 15));
    btnIniciar.setFocusPainted(false);
    btnIniciar.setBorderPainted(false);
    btnIniciar.setCursor(new Cursor(Cursor.HAND_CURSOR));

    btnIniciar.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            btnIniciar.setBackground(new Color(200, 15, 50));
        }
        @Override
        public void mouseExited(MouseEvent e) {
            btnIniciar.setBackground(new Color(220, 20, 60));
        }
    });

    panelContenido.add(btnIniciar);
    panelContenido.add(Box.createVerticalStrut(20));

    // SEPARADOR CON "O"
    JPanel panelSeparador = new JPanel();
    panelSeparador.setLayout(new BoxLayout(panelSeparador, BoxLayout.X_AXIS));
    panelSeparador.setBackground(new Color(25, 25, 28));
    panelSeparador.setMaximumSize(new Dimension(360, 20));
    panelSeparador.setAlignmentX(Component.CENTER_ALIGNMENT);

    JSeparator sep1 = new JSeparator();
    sep1.setForeground(new Color(50, 50, 55));
    sep1.setMaximumSize(new Dimension(150, 1));

    JLabel lblO = new JLabel("  o  ");
    lblO.setForeground(new Color(120, 120, 125));
    lblO.setFont(new Font("Segoe UI", Font.PLAIN, 12));

    JSeparator sep2 = new JSeparator();
    sep2.setForeground(new Color(50, 50, 55));
    sep2.setMaximumSize(new Dimension(150, 1));

    panelSeparador.add(sep1);
    panelSeparador.add(lblO);
    panelSeparador.add(sep2);

    panelContenido.add(panelSeparador);
    panelContenido.add(Box.createVerticalStrut(20));

    // TEXTO "¿NO TIENES CUENTA?"
    JPanel panelNoTienesCuenta = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
    panelNoTienesCuenta.setBackground(new Color(25, 25, 28));
    panelNoTienesCuenta.setMaximumSize(new Dimension(360, 30));
    panelNoTienesCuenta.setAlignmentX(Component.CENTER_ALIGNMENT);

    JLabel lblNoTienesCuenta = new JLabel("¿No tienes cuenta?");
    lblNoTienesCuenta.setForeground(new Color(160, 160, 165));
    lblNoTienesCuenta.setFont(new Font("Segoe UI", Font.PLAIN, 13));

    JButton btnRegistrarse = new JButton("Regístrate");
    btnRegistrarse.setForeground(new Color(220, 20, 60));
    btnRegistrarse.setFont(new Font("Segoe UI", Font.BOLD, 13));
    btnRegistrarse.setFocusPainted(false);
    btnRegistrarse.setBorderPainted(false);
    btnRegistrarse.setContentAreaFilled(false);
    btnRegistrarse.setCursor(new Cursor(Cursor.HAND_CURSOR));

    btnRegistrarse.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            btnRegistrarse.setForeground(new Color(200, 15, 50));
        }
        @Override
        public void mouseExited(MouseEvent e) {
            btnRegistrarse.setForeground(new Color(220, 20, 60));
        }
    });

    panelNoTienesCuenta.add(lblNoTienesCuenta);
    panelNoTienesCuenta.add(btnRegistrarse);

    panelContenido.add(panelNoTienesCuenta);

    panelPrincipal.add(lblLogo);
    panelPrincipal.add(panelContenido);

    // EVENTOS
    btnRegistrarse.addActionListener(e -> {
        CardLayout cl = (CardLayout) panelContenedor.getLayout();
        cl.show(panelContenedor, "REGISTRO");
    });

    btnIniciar.addActionListener(e -> {
        String correo = txtCorreoLogin.getText().trim();
        String pass = new String(txtPassLogin.getPassword()).trim();

        if (correo.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, 
                "Por favor, completa todos los campos", 
                "Campos vacíos", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnIniciar.setEnabled(false);
        btnIniciar.setText("Iniciando...");
        btnIniciar.setBackground(new Color(150, 150, 150));

        // ✅ VERIFICAR PRIMERO SI ES ADMIN
        if (sistemaLogin.iniciarSesionAdmin(correo, pass)) {
            // ESADMIN - Mostrar panel de administración
            usuarioActual = sistemaLogin.getUsuarioActual();
            dialog.dispose();

            System.out.println("✓ Admin detectado - Mostrando panel M6");
            crearInterfazAdmin();
            setVisible(true);

        } else if (sistemaLogin.iniciarSesion(correo, pass)) {
            // ES CLIENTE NORMAL
            usuarioActual = sistemaLogin.getUsuarioActual();
            dialog.dispose();

            System.out.println("✓ Cliente detectado - Mostrando catálogo");
            // Mostrar la interfaz principal con el catálogo del M2
            crearInterfazPrincipal();
            mostrarCatalogo();
            setVisible(true);

        } else {
            JOptionPane.showMessageDialog(dialog, 
                "Correo o contraseña incorrectos.\n\nVerifica tus datos e intenta nuevamente.", 
                "Error de inicio de sesión", 
                JOptionPane.ERROR_MESSAGE);

            btnIniciar.setEnabled(true);
            btnIniciar.setText("Iniciar sesión");
            btnIniciar.setBackground(new Color(220, 20, 60));
        }
    });

    // Enter para iniciar sesión
    txtPassLogin.addActionListener(e -> btnIniciar.doClick());

    return panelPrincipal;
}
private void crearInterfazAdmin() {
    getContentPane().removeAll();
    
    // Crear el panel de administración (M6)
    // No incluimos la topbar de VentanaPrincipal porque M6 tiene la suya propia
    panelAdmin = new M6Panel(usuarioActual.getDNI());
    
    // Agregar el panel al contenedor principal ocupando todo el espacio
    add(panelAdmin, BorderLayout.CENTER);
    
    revalidate();
    repaint();
    
    System.out.println("✓ Panel de administración (M6) cargado correctamente");
}
private JPanel crearPanelRegistro(JDialog dialog, JPanel panelContenedor) {
    JPanel panelPrincipal = new JPanel();
    panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
    panelPrincipal.setBackground(new Color(15, 15, 15));
    panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

    // LOGO ELEGANTE
JLabel lblLogo = new JLabel();
lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
try {
    java.net.URL url = new java.net.URL("https://gaelschenone.aguilucho.ar/source_cmx/index.php?preview=logos%2FCINEMARX%20logotipo.png");
    ImageIcon icon = new ImageIcon(url);
    
    // ✅ CORRECCIÓN: Calcular altura proporcional
    int anchoOriginal = icon.getIconWidth();
    int altoOriginal = icon.getIconHeight();
    int anchoDeseado = 250;
    int altoDeseado = (altoOriginal * anchoDeseado) / anchoOriginal;
    
    Image img = icon.getImage().getScaledInstance(anchoDeseado, altoDeseado, Image.SCALE_SMOOTH);
    lblLogo.setIcon(new ImageIcon(img));
} catch (Exception e) {
    lblLogo.setText("CINEMARX");
    lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 24));
    lblLogo.setForeground(new Color(220, 20, 60));
}
lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
lblLogo.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

    // PANEL PRINCIPAL CON SOMBRA
    JPanel panelContenido = new JPanel();
    panelContenido.setBackground(new Color(25, 25, 28));
    panelContenido.setLayout(new BoxLayout(panelContenido, BoxLayout.Y_AXIS));
    panelContenido.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(40, 40, 45), 1),
        BorderFactory.createEmptyBorder(35, 50, 35, 50)
    ));

    // TÍTULO Y SUBTÍTULO
    JLabel lblTitulo = new JLabel("Crear cuenta");
    lblTitulo.setForeground(Color.WHITE);
    lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
    lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

    JLabel lblSubtitulo = new JLabel("Únete a la mejor experiencia cinematográfica");
    lblSubtitulo.setForeground(new Color(160, 160, 165));
    lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

    panelContenido.add(lblTitulo);
    panelContenido.add(Box.createVerticalStrut(5));
    panelContenido.add(lblSubtitulo);
    panelContenido.add(Box.createVerticalStrut(30));

    // GRID DE 2 COLUMNAS PARA NOMBRE Y APELLIDO
    JPanel panelNombreApellido = new JPanel(new GridLayout(1, 2, 15, 0));
    panelNombreApellido.setBackground(new Color(25, 25, 28));
    panelNombreApellido.setMaximumSize(new Dimension(450, 70));
    panelNombreApellido.setAlignmentX(Component.CENTER_ALIGNMENT);

    // NOMBRE
    JPanel panelNombre = new JPanel();
    panelNombre.setLayout(new BoxLayout(panelNombre, BoxLayout.Y_AXIS));
    panelNombre.setBackground(new Color(25, 25, 28));

    JLabel lblNombreLabel = new JLabel("Nombre");
    lblNombreLabel.setForeground(new Color(200, 200, 205));
    lblNombreLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
    lblNombreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    JTextField txtNombre = new JTextField();
    txtNombre.setMaximumSize(new Dimension(450, 42));
    txtNombre.setBackground(new Color(18, 18, 20));
    txtNombre.setForeground(Color.WHITE);
    txtNombre.setCaretColor(new Color(220, 20, 60));
    txtNombre.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    txtNombre.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(50, 50, 55), 1),
        BorderFactory.createEmptyBorder(8, 14, 8, 14)));

    txtNombre.addFocusListener(new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
            txtNombre.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 20, 60), 2),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        }
        @Override
        public void focusLost(FocusEvent e) {
            txtNombre.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 50, 55), 1),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        }
    });

    panelNombre.add(lblNombreLabel);
    panelNombre.add(Box.createVerticalStrut(6));
    panelNombre.add(txtNombre);

    // APELLIDO
    JPanel panelApellido = new JPanel();
    panelApellido.setLayout(new BoxLayout(panelApellido, BoxLayout.Y_AXIS));
    panelApellido.setBackground(new Color(25, 25, 28));

    JLabel lblApellidoLabel = new JLabel("Apellido");
    lblApellidoLabel.setForeground(new Color(200, 200, 205));
    lblApellidoLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
    lblApellidoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    JTextField txtApellido = new JTextField();
    txtApellido.setMaximumSize(new Dimension(450, 42));
    txtApellido.setBackground(new Color(18, 18, 20));
    txtApellido.setForeground(Color.WHITE);
    txtApellido.setCaretColor(new Color(220, 20, 60));
    txtApellido.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    txtApellido.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(50, 50, 55), 1),
        BorderFactory.createEmptyBorder(8, 14, 8, 14)));

    txtApellido.addFocusListener(new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
            txtApellido.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 20, 60), 2),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        }
        @Override
        public void focusLost(FocusEvent e) {
            txtApellido.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 50, 55), 1),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        }
    });

    panelApellido.add(lblApellidoLabel);
    panelApellido.add(Box.createVerticalStrut(6));
    panelApellido.add(txtApellido);

    panelNombreApellido.add(panelNombre);
    panelNombreApellido.add(panelApellido);

    panelContenido.add(panelNombreApellido);
    panelContenido.add(Box.createVerticalStrut(18));

    // CORREO ELECTRÓNICO (ANCHO COMPLETO)
    JLabel lblCorreoLabel = new JLabel("Correo electrónico");
    lblCorreoLabel.setForeground(new Color(200, 200, 205));
    lblCorreoLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
    lblCorreoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

    JTextField txtCorreo = new JTextField();
    txtCorreo.setMaximumSize(new Dimension(450, 42));
    txtCorreo.setBackground(new Color(18, 18, 20));
    txtCorreo.setForeground(Color.WHITE);
    txtCorreo.setCaretColor(new Color(220, 20, 60));
    txtCorreo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    txtCorreo.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(50, 50, 55), 1),
        BorderFactory.createEmptyBorder(8, 14, 8, 14)));
    txtCorreo.setAlignmentX(Component.CENTER_ALIGNMENT);

    txtCorreo.addFocusListener(new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
            txtCorreo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 20, 60), 2),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        }
        @Override
        public void focusLost(FocusEvent e) {
            txtCorreo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 50, 55), 1),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        }
    });

    panelContenido.add(lblCorreoLabel);
    panelContenido.add(Box.createVerticalStrut(6));
    panelContenido.add(txtCorreo);
    panelContenido.add(Box.createVerticalStrut(18));

    // DNI Y FECHA EN 2 COLUMNAS
    JPanel panelDNIFecha = new JPanel(new GridLayout(1, 2, 15, 0));
    panelDNIFecha.setBackground(new Color(25, 25, 28));
    panelDNIFecha.setMaximumSize(new Dimension(450, 70));
    panelDNIFecha.setAlignmentX(Component.CENTER_ALIGNMENT);

    // DNI
    JPanel panelDNI = new JPanel();
    panelDNI.setLayout(new BoxLayout(panelDNI, BoxLayout.Y_AXIS));
    panelDNI.setBackground(new Color(25, 25, 28));

    JLabel lblDNILabel = new JLabel("DNI");
    lblDNILabel.setForeground(new Color(200, 200, 205));
    lblDNILabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
    lblDNILabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    JTextField txtDNI = new JTextField();
    txtDNI.setMaximumSize(new Dimension(450, 42));
    txtDNI.setBackground(new Color(18, 18, 20));
    txtDNI.setForeground(Color.WHITE);
    txtDNI.setCaretColor(new Color(220, 20, 60));
    txtDNI.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    txtDNI.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(50, 50, 55), 1),
        BorderFactory.createEmptyBorder(8, 14, 8, 14)));

    txtDNI.addFocusListener(new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
            txtDNI.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 20, 60), 2),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        }
        @Override
        public void focusLost(FocusEvent e) {
            txtDNI.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 50, 55), 1),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        }
    });

    panelDNI.add(lblDNILabel);
    panelDNI.add(Box.createVerticalStrut(6));
    panelDNI.add(txtDNI);

    // FECHA DE NACIMIENTO - CORREGIDO CON 3 CAMPOS
    JPanel panelFechaCompleto = new JPanel();
    panelFechaCompleto.setLayout(new BoxLayout(panelFechaCompleto, BoxLayout.Y_AXIS));
    panelFechaCompleto.setBackground(new Color(25, 25, 28));

    JLabel lblFechaLabel = new JLabel("Fecha de Nacimiento");
    lblFechaLabel.setForeground(new Color(200, 200, 205));
    lblFechaLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
    lblFechaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    JPanel panelFechaContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    panelFechaContainer.setBackground(new Color(25, 25, 28));
    panelFechaContainer.setMaximumSize(new Dimension(450, 42));

    JTextField txtDia = new JTextField();
    txtDia.setPreferredSize(new Dimension(50, 42));
    txtDia.setBackground(new Color(18, 18, 20));
    txtDia.setForeground(Color.WHITE);
    txtDia.setCaretColor(new Color(220, 20, 60));
    txtDia.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    txtDia.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(50, 50, 55), 1),
        BorderFactory.createEmptyBorder(8, 8, 8, 8)));
    txtDia.setHorizontalAlignment(JTextField.CENTER);

    JLabel lblBarra1 = new JLabel("/");
    lblBarra1.setForeground(new Color(100, 100, 105));
    lblBarra1.setFont(new Font("Segoe UI", Font.PLAIN, 18));

    JTextField txtMes = new JTextField();
    txtMes.setPreferredSize(new Dimension(50, 42));
    txtMes.setBackground(new Color(18, 18, 20));
    txtMes.setForeground(Color.WHITE);
    txtMes.setCaretColor(new Color(220, 20, 60));
    txtMes.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    txtMes.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(50, 50, 55), 1),
        BorderFactory.createEmptyBorder(8, 8, 8, 8)));
    txtMes.setHorizontalAlignment(JTextField.CENTER);

    JLabel lblBarra2 = new JLabel("/");
    lblBarra2.setForeground(new Color(100, 100, 105));
    lblBarra2.setFont(new Font("Segoe UI", Font.PLAIN, 18));

    JTextField txtAnio = new JTextField();
    txtAnio.setPreferredSize(new Dimension(70, 42));
    txtAnio.setBackground(new Color(18, 18, 20));
    txtAnio.setForeground(Color.WHITE);
    txtAnio.setCaretColor(new Color(220, 20, 60));
    txtAnio.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    txtAnio.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(50, 50, 55), 1),
        BorderFactory.createEmptyBorder(8, 8, 8, 8)));
    txtAnio.setHorizontalAlignment(JTextField.CENTER);

    panelFechaContainer.add(txtDia);
    panelFechaContainer.add(lblBarra1);
    panelFechaContainer.add(txtMes);
    panelFechaContainer.add(lblBarra2);
    panelFechaContainer.add(txtAnio);

    panelFechaCompleto.add(lblFechaLabel);
    panelFechaCompleto.add(Box.createVerticalStrut(6));
    panelFechaCompleto.add(panelFechaContainer);

    panelDNIFecha.add(panelDNI);
    panelDNIFecha.add(panelFechaCompleto);

    panelContenido.add(panelDNIFecha);
    panelContenido.add(Box.createVerticalStrut(18));

    // CONTRASEÑAS EN 2 COLUMNAS
    JPanel panelPasswords = new JPanel(new GridLayout(1, 2, 15, 0));
    panelPasswords.setBackground(new Color(25, 25, 28));
    panelPasswords.setMaximumSize(new Dimension(450, 70));
    panelPasswords.setAlignmentX(Component.CENTER_ALIGNMENT);

    // CONTRASEÑA
    JPanel panelPass = new JPanel();
    panelPass.setLayout(new BoxLayout(panelPass, BoxLayout.Y_AXIS));
    panelPass.setBackground(new Color(25, 25, 28));

    JLabel lblPassLabel = new JLabel("Contraseña");
    lblPassLabel.setForeground(new Color(200, 200, 205));
    lblPassLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
    lblPassLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    JPasswordField txtPass = new JPasswordField();
    txtPass.setMaximumSize(new Dimension(450, 42));
    txtPass.setBackground(new Color(18, 18, 20));
    txtPass.setForeground(Color.WHITE);
    txtPass.setCaretColor(new Color(220, 20, 60));
    txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    txtPass.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(50, 50, 55), 1),
        BorderFactory.createEmptyBorder(8, 14, 8, 14)));

    txtPass.addFocusListener(new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
            txtPass.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 20, 60), 2),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        }
        @Override
        public void focusLost(FocusEvent e) {
            txtPass.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 50, 55), 1),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        }
    });

    panelPass.add(lblPassLabel);
    panelPass.add(Box.createVerticalStrut(6));
    panelPass.add(txtPass);

    // CONFIRMAR CONTRASEÑA
    JPanel panelConfirmar = new JPanel();
    panelConfirmar.setLayout(new BoxLayout(panelConfirmar, BoxLayout.Y_AXIS));
    panelConfirmar.setBackground(new Color(25, 25, 28));

    JLabel lblConfirmarLabel = new JLabel("Confirmar Contraseña");
    lblConfirmarLabel.setForeground(new Color(200, 200, 205));
    lblConfirmarLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
    lblConfirmarLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    JPasswordField txtConfirmar = new JPasswordField();
    txtConfirmar.setMaximumSize(new Dimension(450, 42));
    txtConfirmar.setBackground(new Color(18, 18, 20));
    txtConfirmar.setForeground(Color.WHITE);
    txtConfirmar.setCaretColor(new Color(220, 20, 60));
    txtConfirmar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    txtConfirmar.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(50, 50, 55), 1),
        BorderFactory.createEmptyBorder(8, 14, 8, 14)));

    txtConfirmar.addFocusListener(new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
            txtConfirmar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 20, 60), 2),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        }
        @Override
        public void focusLost(FocusEvent e) {
            txtConfirmar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 50, 55), 1),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        }
    });

    panelConfirmar.add(lblConfirmarLabel);
    panelConfirmar.add(Box.createVerticalStrut(6));
    panelConfirmar.add(txtConfirmar);

    panelPasswords.add(panelPass);
    panelPasswords.add(panelConfirmar);

    panelContenido.add(panelPasswords);
    panelContenido.add(Box.createVerticalStrut(28));

    // BOTÓN CREAR CUENTA - PREMIUM
    JButton btnCrear = new JButton("Crear cuenta");
    btnCrear.setMaximumSize(new Dimension(450, 48));
    btnCrear.setAlignmentX(Component.CENTER_ALIGNMENT);
    btnCrear.setBackground(new Color(220, 20, 60));
    btnCrear.setForeground(Color.WHITE);
    btnCrear.setFont(new Font("Segoe UI", Font.BOLD, 14));
    btnCrear.setFocusPainted(false);
    btnCrear.setBorderPainted(false);
    btnCrear.setCursor(new Cursor(Cursor.HAND_CURSOR));

    btnCrear.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            btnCrear.setBackground(new Color(200, 15, 50));
        }
        @Override
        public void mouseExited(MouseEvent e) {
            btnCrear.setBackground(new Color(220, 20, 60));
        }
    });

    panelContenido.add(btnCrear);
    panelContenido.add(Box.createVerticalStrut(15));

    // TEXTO "¿YA TIENES CUENTA?"
    JPanel panelYaTienesCuenta = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
    panelYaTienesCuenta.setBackground(new Color(25, 25, 28));
    panelYaTienesCuenta.setMaximumSize(new Dimension(450, 30));
    panelYaTienesCuenta.setAlignmentX(Component.CENTER_ALIGNMENT);

    JLabel lblYaTienesCuenta = new JLabel("¿Ya tienes cuenta?");
    lblYaTienesCuenta.setForeground(new Color(160, 160, 165));
    lblYaTienesCuenta.setFont(new Font("Segoe UI", Font.PLAIN, 12));

    JButton btnVolverReg = new JButton("Inicia sesión");
    btnVolverReg.setForeground(new Color(220, 20, 60));
    btnVolverReg.setFont(new Font("Segoe UI", Font.BOLD, 12));
    btnVolverReg.setFocusPainted(false);
    btnVolverReg.setBorderPainted(false);
    btnVolverReg.setContentAreaFilled(false);
    btnVolverReg.setCursor(new Cursor(Cursor.HAND_CURSOR));

    btnVolverReg.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            btnVolverReg.setForeground(new Color(200, 15, 50));
        }
        @Override
        public void mouseExited(MouseEvent e) {
            btnVolverReg.setForeground(new Color(220, 20, 60));
        }
    });

    panelYaTienesCuenta.add(lblYaTienesCuenta);
    panelYaTienesCuenta.add(btnVolverReg);

    panelContenido.add(panelYaTienesCuenta);

    JScrollPane scrollPane = new JScrollPane(panelContenido);
    scrollPane.setPreferredSize(new Dimension(560, 600));
    scrollPane.setMaximumSize(new Dimension(560, 600));
    scrollPane.setBorder(null);
    scrollPane.setBackground(new Color(15, 15, 15));
    scrollPane.getViewport().setBackground(new Color(15, 15, 15));
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);

    panelPrincipal.add(lblLogo);
    panelPrincipal.add(Box.createVerticalStrut(5));
    panelPrincipal.add(scrollPane);

    // EVENTOS
    btnVolverReg.addActionListener(e -> {
        CardLayout cl = (CardLayout) panelContenedor.getLayout();
        cl.show(panelContenedor, "LOGIN");
    });

    btnCrear.addActionListener(e -> {
        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String dniStr = txtDNI.getText().trim();
        String dia = txtDia.getText().trim();
        String mes = txtMes.getText().trim();
        String anio = txtAnio.getText().trim();
        String correo = txtCorreo.getText().trim();
        String pass = new String(txtPass.getPassword()).trim();
        String confirmar = new String(txtConfirmar.getPassword()).trim();

        if (nombre.isEmpty() || apellido.isEmpty() || dniStr.isEmpty() || 
            dia.isEmpty() || mes.isEmpty() || anio.isEmpty() || 
            correo.isEmpty() || pass.isEmpty() || confirmar.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Por favor, completa todos los campos", "Campos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validarSoloLetras(nombre)) {
            JOptionPane.showMessageDialog(dialog, 
                "El nombre solo puede contener letras", 
                "Nombre inválido", 
                JOptionPane.ERROR_MESSAGE);
            return;
}
        if (!validarSoloLetras(apellido)) {
            JOptionPane.showMessageDialog(dialog, 
            "El apellido solo puede contener letras", 
            "Apellido inválido", 
            JOptionPane.ERROR_MESSAGE);
        return;
}

        int DNI;
        try {
            DNI = Integer.parseInt(dniStr);
            if (DNI <= 0 || dniStr.length() != 8) {
                JOptionPane.showMessageDialog(dialog, "El DNI debe tener exactamente 8 dígitos", "DNI inválido", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(dialog, "El DNI debe contener solo números", "DNI inválido", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int diaInt, mesInt, anioInt;
        try {
            diaInt = Integer.parseInt(dia);
            mesInt = Integer.parseInt(mes);
            anioInt = Integer.parseInt(anio);

            if (diaInt < 1 || diaInt > 31) {
                JOptionPane.showMessageDialog(dialog, "Fecha inválida", "Fecha inválida", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (mesInt < 1 || mesInt > 12) {
                JOptionPane.showMessageDialog(dialog, "Fecha inválida", "Fecha inválida", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (anioInt < 1920 || anioInt > 2024) {
                JOptionPane.showMessageDialog(dialog, "Fecha inválida", "Fecha inválida", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (mesInt == 2) {
                boolean esBisiesto = (anioInt % 4 == 0 && anioInt % 100 != 0) || (anioInt % 400 == 0);
                if (diaInt > (esBisiesto ? 29 : 28)) {
                    JOptionPane.showMessageDialog(dialog, "Febrero no puede tener más de " + (esBisiesto ? "29" : "28") + " días", "Fecha inválida", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else if (mesInt == 4 || mesInt == 6 || mesInt == 9 || mesInt == 11) {
                if (diaInt > 30) {
                    JOptionPane.showMessageDialog(dialog, "Este mes no puede tener más de 30 días", "Fecha inválida", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(dialog, "La fecha debe contener solo números", "Fecha inválida", JOptionPane.ERROR_MESSAGE);
            return;
        }

        java.sql.Date fechaNacimiento;
        try {
            String fechaStr = String.format("%04d-%02d-%02d", anioInt, mesInt, diaInt);
            fechaNacimiento = java.sql.Date.valueOf(fechaStr);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(dialog, "La fecha ingresada no es válida", "Fecha inválida", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!correo.toLowerCase().endsWith("@gmail.com")) {
            JOptionPane.showMessageDialog(dialog, "El correo debe terminar en @gmail.com", "Correo inválido", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!pass.equals(confirmar)) {
            JOptionPane.showMessageDialog(dialog, "Las contraseñas no coinciden", "Error de contraseña", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (pass.length() < 6) {
            JOptionPane.showMessageDialog(dialog, "La contraseña debe tener al menos 6 caracteres", "Contraseña débil", JOptionPane.ERROR_MESSAGE);
            return;
        }

        btnCrear.setEnabled(false);
        btnCrear.setText("Creando cuenta...");
        btnCrear.setBackground(new Color(150, 150, 150));

        if (sistemaLogin.registrarUsuario(nombre, apellido, correo, pass, DNI, fechaNacimiento)) {
            JOptionPane.showMessageDialog(dialog, 
                "¡Cuenta creada exitosamente!\n\nYa puedes iniciar sesión con tu correo y contraseña.", 
                "Registro exitoso", 
                JOptionPane.INFORMATION_MESSAGE);

            txtNombre.setText("");
            txtApellido.setText("");
            txtDNI.setText("");
            txtDia.setText("");
            txtMes.setText("");
            txtAnio.setText("");
            txtCorreo.setText("");
            txtPass.setText("");
            txtConfirmar.setText("");

            CardLayout cl = (CardLayout) panelContenedor.getLayout();
            cl.show(panelContenedor, "LOGIN");
        } else {
            JOptionPane.showMessageDialog(dialog, 
                "No se pudo crear la cuenta.\n\nEl correo o DNI ya están registrados.", 
                "Error en el registro", 
                JOptionPane.ERROR_MESSAGE);
            
            btnCrear.setEnabled(true);
            btnCrear.setText("Crear cuenta");
            btnCrear.setBackground(new Color(220, 20, 60));
        }
    });

    return panelPrincipal;
}

    private void mostrarMensajeError(String mensaje, JLabel[] lblMensajeError, JPanel panelBotones) {
        if (lblMensajeError[0] == null) {
            lblMensajeError[0] = new JLabel();
            lblMensajeError[0].setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblMensajeError[0].setForeground(new Color(244, 67, 54));
            lblMensajeError[0].setAlignmentX(Component.LEFT_ALIGNMENT);
            lblMensajeError[0].setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
            panelBotones.add(lblMensajeError[0]);
        }
        lblMensajeError[0].setText(" " + mensaje);
        lblMensajeError[0].setVisible(true);
        panelBotones.revalidate();
        panelBotones.repaint();

        Timer timer = new Timer(5000, evt -> {
            if (lblMensajeError[0] != null) {
                lblMensajeError[0].setVisible(false);
            }
        });
        timer.setRepeats(false);
        timer.start();
    }
    private boolean validarSoloLetras(String texto) {
        // Permitir letras (incluyendo acentos), espacios y guiones
        return texto.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ \\-]+");
    }
    private void mostrarPantallaComprarVIP() {
        panelContenido.removeAll();
        panelContenido.setLayout(new BorderLayout()); // Keep BorderLayout

        JPanel panelMensaje = new JPanel();
        panelMensaje.setLayout(new BoxLayout(panelMensaje, BoxLayout.Y_AXIS));
        panelMensaje.setBackground(new Color(30, 30, 30));
        panelMensaje.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        panelMensaje.setOpaque(false); // Make it transparent to see the wrapper

        JLabel lblIcono = new JLabel("⭐");
        lblIcono.setFont(new Font("Segoe UI", Font.BOLD, 80));
        lblIcono.setForeground(new Color(255, 193, 7));
        lblIcono.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitulo = new JLabel("Conviértete en Miembro VIP");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblMensaje = new JLabel("<html><center>Actualmente no eres un miembro VIP.<br>¡Únete para disfrutar de beneficios exclusivos!</center></html>");
        lblMensaje.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblMensaje.setForeground(new Color(180, 180, 180));
        lblMensaje.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblMensaje.setHorizontalAlignment(SwingConstants.CENTER);

        JButton btnComprar = new JButton("Obtener Membresía");
        btnComprar.setPreferredSize(new Dimension(220, 50));
        btnComprar.setMaximumSize(new Dimension(220, 50));
        btnComprar.setBackground(new Color(255, 193, 7));
        btnComprar.setForeground(Color.BLACK);
        btnComprar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnComprar.setFocusPainted(false);
        btnComprar.setBorderPainted(false);
        btnComprar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnComprar.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        btnComprar.addActionListener(e -> {
            Producto membresiaProduct = getProductoById(20);

            if (membresiaProduct == null) {
                JOptionPane.showMessageDialog(this, "El producto de membresía no está disponible actualmente. Contacte a soporte.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            OrderDetails orderDetails = new OrderDetails(usuarioActual.getIdCliente());
            orderDetails.addProducto(membresiaProduct, "Activación de Membresía VIP");

            mostrarPagos(orderDetails);
        });

        panelMensaje.add(lblIcono);
        panelMensaje.add(Box.createVerticalStrut(20));
        panelMensaje.add(lblTitulo);
        panelMensaje.add(Box.createVerticalStrut(15));
        panelMensaje.add(lblMensaje);
        panelMensaje.add(Box.createVerticalStrut(30));
        panelMensaje.add(btnComprar);

        // Wrapper panel to center the message panel
        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.setBackground(new Color(30, 30, 30));
        wrapperPanel.add(panelMensaje);

        panelContenido.add(wrapperPanel, BorderLayout.CENTER);
        panelContenido.revalidate();
        panelContenido.repaint();
    }

    private Producto getProductoById(int id) {
        Connection con = ConexionBD.obtenerConexion();
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
