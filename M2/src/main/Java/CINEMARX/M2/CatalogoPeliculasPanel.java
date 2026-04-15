package CINEMARX.M2;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import java.net.URL;
import java.io.IOException;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

// ✅ AGREGAR estos imports si no existen:
import java.awt.Window;
import javax.swing.SwingUtilities;

import CINEMARX.Common.NavigationHelper; 

/**
 * Panel que contiene todo el catálogo de películas del M2
 * Se integra dentro de VentanaPrincipal del M1
 */
public class CatalogoPeliculasPanel extends JPanel {
    
    private int idUsuario;
    
    // Componentes de Búsqueda
    private JTextField txtBusqueda;
    private JButton btnLimpiarBusqueda;
    private JPopupMenu sugerenciasPopup;
    private boolean actualizandoPorSugerencia = false;
    private JComboBox<String> cmbGenero, cmbClasificacion;
    
    // Componentes de Layout
    private CardLayout cardLayout;
    private JPanel panelContenedor;
    private JPanel panelPeliculasSecciones;
    private JPanel panelResultadosBusqueda;
    
    // Componentes para "SPOTLIGHT"
    private JPanel panelSpotlight;
    private JLabel lblSpotlightImagen;
    private JLabel lblSpotlightTitulo;
    private JTextArea txtSpotlightSinopsis;
    private Pelicula peliculaDestacada;

    // Conexión a la BD
    private PeliculaDAO peliculaDAO;
    
    public CatalogoPeliculasPanel(int idUsuario) {
        this.idUsuario = idUsuario;
        inicializarDatos();
        configurarPanel();
        crearComponentes();
        
        SwingUtilities.invokeLater(() -> {
            cargarSpotlight();
            cargarSecciones();
        });
    }
    
    private void inicializarDatos() {
        this.peliculaDAO = new PeliculaDAO();
        this.sugerenciasPopup = new JPopupMenu();
        this.sugerenciasPopup.setBackground(new Color(40, 40, 40));
        this.sugerenciasPopup.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
    }
    
    private void configurarPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 30));
    }
    
    private void crearComponentes() {
        add(crearPanelCentral(), BorderLayout.CENTER);
    }
    
    private JPanel crearPanelCentral() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        panel.add(crearPanelBusqueda(), BorderLayout.NORTH);
        
        JPanel panelContenidoPrincipal = new JPanel(new BorderLayout(0, 15));
        panelContenidoPrincipal.setBackground(new Color(30, 30, 30));

        panelContenidoPrincipal.add(crearPanelSpotlight(), BorderLayout.NORTH);
        
        cardLayout = new CardLayout();
        panelContenedor = new JPanel(cardLayout);
        panelContenedor.setBackground(new Color(30, 30, 30));

        panelPeliculasSecciones = new JPanel();
        panelPeliculasSecciones.setLayout(new BoxLayout(panelPeliculasSecciones, BoxLayout.Y_AXIS));
        panelPeliculasSecciones.setBackground(new Color(30, 30, 30));
        
        JScrollPane scrollSecciones = new JScrollPane(panelPeliculasSecciones);
        scrollSecciones.setBorder(null);
        scrollSecciones.getVerticalScrollBar().setUnitIncrement(16);
        scrollSecciones.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        panelResultadosBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        panelResultadosBusqueda.setBackground(new Color(30, 30, 30));
        
        JScrollPane scrollBusqueda = new JScrollPane(panelResultadosBusqueda);
        scrollBusqueda.setBorder(null);
        scrollBusqueda.getVerticalScrollBar().setUnitIncrement(16);

        panelContenedor.add(scrollSecciones, "SECCIONES");
        panelContenedor.add(scrollBusqueda, "BUSQUEDA");
        
        panelContenidoPrincipal.add(panelContenedor, BorderLayout.CENTER);
        panel.add(panelContenidoPrincipal, BorderLayout.CENTER);
        
        cardLayout.show(panelContenedor, "SECCIONES");
        
        return panel;
    }

    private JPanel crearPanelBusqueda() {
        JPanel panelBusquedaCompleto = new JPanel(new BorderLayout(0, 15));
        panelBusquedaCompleto.setBackground(new Color(30, 30, 30));
        
        // FILA 1: Campo de búsqueda
        JPanel panelBusqueda = new JPanel(new BorderLayout(10, 0));
        panelBusqueda.setBackground(new Color(30, 30, 30));
        
        txtBusqueda = new JTextField("🔍 Buscar película...");
        txtBusqueda.setBackground(new Color(50, 50, 50));
        txtBusqueda.setForeground(new Color(160, 160, 160));
        txtBusqueda.setCaretColor(Color.WHITE);
        txtBusqueda.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        txtBusqueda.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        txtBusqueda.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtBusqueda.getText().equals("🔍 Buscar película...")) {
                    txtBusqueda.setText("");
                    txtBusqueda.setForeground(Color.WHITE);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (txtBusqueda.getText().isEmpty()) {
                    txtBusqueda.setText("🔍 Buscar película...");
                    txtBusqueda.setForeground(new Color(160, 160, 160));
                }
            }
        });

        txtBusqueda.addActionListener(e -> {
            sugerenciasPopup.setVisible(false);
            ejecutarBusquedaConFiltros();
        });
        
        txtBusqueda.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { manejarSugerencias(); }
            @Override public void removeUpdate(DocumentEvent e) { manejarSugerencias(); }
            @Override public void changedUpdate(DocumentEvent e) { manejarSugerencias(); }
        });
        
        btnLimpiarBusqueda = new JButton("✖");
        btnLimpiarBusqueda.setPreferredSize(new Dimension(45, 45));
        btnLimpiarBusqueda.setBackground(new Color(220, 20, 60));
        btnLimpiarBusqueda.setForeground(Color.WHITE);
        btnLimpiarBusqueda.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLimpiarBusqueda.setFocusPainted(false);
        btnLimpiarBusqueda.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnLimpiarBusqueda.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLimpiarBusqueda.setVisible(false);
        btnLimpiarBusqueda.addActionListener(e -> limpiarBusqueda());
        
        panelBusqueda.add(txtBusqueda, BorderLayout.CENTER);
        panelBusqueda.add(btnLimpiarBusqueda, BorderLayout.EAST);
        
        // FILA 2: Filtros
        JPanel panelFiltros = new JPanel(new GridLayout(1, 2, 15, 0));
        panelFiltros.setBackground(new Color(30, 30, 30));
        
        List<String> generosDB = peliculaDAO.obtenerGenerosUnicos();
        List<String> generosConTodos = new ArrayList<>();
        generosConTodos.add("Todos los géneros");
        generosConTodos.addAll(generosDB);
        cmbGenero = new JComboBox<>(generosConTodos.toArray(new String[0]));
        aplicarEstiloComboBox(cmbGenero);
        cmbGenero.addActionListener(e -> ejecutarBusquedaConFiltros());
        
        List<String> clasificacionesDB = peliculaDAO.obtenerClasificacionesUnicas();
        List<String> clasificacionesConTodas = new ArrayList<>();
        clasificacionesConTodas.add("Todas las edades");
        clasificacionesConTodas.addAll(clasificacionesDB);
        cmbClasificacion = new JComboBox<>(clasificacionesConTodas.toArray(new String[0]));
        aplicarEstiloComboBox(cmbClasificacion);
        cmbClasificacion.addActionListener(e -> ejecutarBusquedaConFiltros());
        
        panelFiltros.add(cmbGenero);
        panelFiltros.add(cmbClasificacion);
        
        panelBusquedaCompleto.add(panelBusqueda, BorderLayout.NORTH);
        panelBusquedaCompleto.add(panelFiltros, BorderLayout.CENTER);
        
        return panelBusquedaCompleto;
    }
    
    private void aplicarEstiloComboBox(JComboBox<?> combo) {
        combo.setBackground(new Color(50, 50, 50));
        combo.setForeground(Color.WHITE);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
    }
    
    private JPanel crearPanelSpotlight() {
        panelSpotlight = new JPanel(new BorderLayout(15, 0));
        panelSpotlight.setBackground(new Color(40, 40, 40));
        panelSpotlight.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80)),
            new EmptyBorder(15, 15, 15, 15)
        ));
        panelSpotlight.setPreferredSize(new Dimension(0, 250));
        
        lblSpotlightImagen = new JLabel();
        lblSpotlightImagen.setPreferredSize(new Dimension(150, 220));
        lblSpotlightImagen.setBackground(new Color(60, 60, 60));
        lblSpotlightImagen.setOpaque(true);
        lblSpotlightImagen.setHorizontalAlignment(SwingConstants.CENTER);
        lblSpotlightImagen.setForeground(new Color(160, 160, 160));
        lblSpotlightImagen.setText("Cargando...");
        panelSpotlight.add(lblSpotlightImagen, BorderLayout.WEST);
        
        JPanel panelInfo = new JPanel(new BorderLayout(0, 10));
        panelInfo.setOpaque(false);
        
        lblSpotlightTitulo = new JLabel("Cargando película destacada...");
        lblSpotlightTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblSpotlightTitulo.setForeground(Color.WHITE);
        panelInfo.add(lblSpotlightTitulo, BorderLayout.NORTH);
        
        txtSpotlightSinopsis = new JTextArea("Cargando sinopsis...");
        txtSpotlightSinopsis.setEditable(false);
        txtSpotlightSinopsis.setLineWrap(true);
        txtSpotlightSinopsis.setWrapStyleWord(true);
        txtSpotlightSinopsis.setOpaque(false);
        txtSpotlightSinopsis.setForeground(new Color(160, 160, 160));
        txtSpotlightSinopsis.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSpotlightSinopsis.setFocusable(false);
        
        JScrollPane scrollSinopsis = new JScrollPane(txtSpotlightSinopsis);
        scrollSinopsis.setBorder(null);
        scrollSinopsis.setOpaque(false);
        scrollSinopsis.getViewport().setOpaque(false);
        
        panelInfo.add(scrollSinopsis, BorderLayout.CENTER);
        
        JButton btnRefresh = new JButton("🔄");
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        btnRefresh.setBackground(new Color(60, 60, 60));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setPreferredSize(new Dimension(50, 50));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.setToolTipText("Mostrar otra película aleatoria");
        btnRefresh.addActionListener(e -> cargarSpotlight());
        
        panelSpotlight.add(panelInfo, BorderLayout.CENTER);
        panelSpotlight.add(btnRefresh, BorderLayout.EAST);
        
        return panelSpotlight;
    }
    
    private void cargarSpotlight() {
        lblSpotlightTitulo.setText("Buscando película...");
        txtSpotlightSinopsis.setText("");
        lblSpotlightImagen.setIcon(null);
        lblSpotlightImagen.setText("Cargando...");
        
        SwingWorker<Pelicula, Void> worker = new SwingWorker<Pelicula, Void>() {
            @Override
            protected Pelicula doInBackground() throws Exception {
                return peliculaDAO.obtenerPeliculaAleatoriaEnCartelera();
            }
            
            @Override
            protected void done() {
                try {
                    peliculaDestacada = get();
                    if (peliculaDestacada != null) {
                        lblSpotlightTitulo.setText(peliculaDestacada.getTitulo());
                        String sinopsis = peliculaDestacada.getSinopsis();
                        txtSpotlightSinopsis.setText(sinopsis != null ? sinopsis : "Sin sinopsis disponible.");
                        cargarImagenSpotlight(peliculaDestacada.getImagen());
                    } else {
                        lblSpotlightTitulo.setText("No hay películas destacadas");
                        txtSpotlightSinopsis.setText("Prueba de nuevo más tarde.");
                        lblSpotlightImagen.setText("N/A");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
    
    private void cargarImagenSpotlight(String urlImagen) {
        final int POSTER_WIDTH = 150;
        final int POSTER_HEIGHT = 220;
        
        if (urlImagen != null && !urlImagen.trim().isEmpty()) {
            SwingWorker<ImageIcon, Void> workerImg = new SwingWorker<ImageIcon, Void>() {
                @Override
                protected ImageIcon doInBackground() throws Exception {
                    URL url = new URL(urlImagen);
                    Image image = ImageIO.read(url);
                    if (image != null) {
                        Image scaledImage = image.getScaledInstance(POSTER_WIDTH, POSTER_HEIGHT, Image.SCALE_SMOOTH);
                        return new ImageIcon(scaledImage);
                    }
                    return null;
                }
                
                @Override
                protected void done() {
                    try {
                        ImageIcon imageIcon = get();
                        if (imageIcon != null) {
                            lblSpotlightImagen.setIcon(imageIcon);
                            lblSpotlightImagen.setText(null);
                        } else {
                            lblSpotlightImagen.setText("Sin Imagen");
                        }
                    } catch (Exception e) {
                        lblSpotlightImagen.setText("Error Img");
                    }
                }
            };
            workerImg.execute();
        } else {
            lblSpotlightImagen.setIcon(null);
            lblSpotlightImagen.setText("Sin Imagen");
        }
    }
    
    private void manejarSugerencias() {
        if (actualizandoPorSugerencia) return;
        String termino = txtBusqueda.getText();
        if (termino.isEmpty() || termino.equals("🔍 Buscar película...") || termino.length() < 2) {
            sugerenciasPopup.setVisible(false);
            return;
        }
        
        SwingWorker<List<String>, Void> worker = new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return peliculaDAO.obtenerTitulosQueCoinciden(termino, 5);
            }
            @Override
            protected void done() {
                try {
                    List<String> titulos = get();
                    sugerenciasPopup.removeAll();
                    if (titulos.isEmpty()) {
                        sugerenciasPopup.setVisible(false);
                        return;
                    }
                    for (String titulo : titulos) {
                        JMenuItem item = new JMenuItem(titulo);
                        item.setBackground(new Color(40, 40, 40));
                        item.setForeground(Color.WHITE);
                        item.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                        item.setBorder(new EmptyBorder(8, 10, 8, 10));
                        item.addActionListener(e -> {
                            actualizandoPorSugerencia = true;
                            txtBusqueda.setText(titulo);
                            sugerenciasPopup.setVisible(false);
                            ejecutarBusquedaConFiltros();
                            actualizandoPorSugerencia = false;
                        });
                        sugerenciasPopup.add(item);
                    }
                    sugerenciasPopup.show(txtBusqueda, 0, txtBusqueda.getHeight());
                    txtBusqueda.requestFocusInWindow();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
    
    private void ejecutarBusquedaConFiltros() {
        String termino = txtBusqueda.getText();
        String genero = (String) cmbGenero.getSelectedItem();
        String clasificacion = (String) cmbClasificacion.getSelectedItem();
        
        if ((termino.isEmpty() || termino.equals("🔍 Buscar película...")) && 
            genero.equals("Todos los géneros") && 
            clasificacion.equals("Todas las edades")) {
            limpiarBusqueda();
            return;
        }
        
        List<Pelicula> resultados;
        
        if (!termino.isEmpty() && !termino.equals("🔍 Buscar película...")) {
            resultados = peliculaDAO.buscarPorTitulo(termino);
        } else {
            resultados = peliculaDAO.obtenerTodas();
        }
        
        resultados = filtrarLocalmente(resultados, genero, clasificacion);
        
        panelResultadosBusqueda.removeAll();
        
        if (resultados.isEmpty()) {
            JLabel noResultados = new JLabel("No se encontraron resultados");
            noResultados.setFont(new Font("Segoe UI", Font.BOLD, 18));
            noResultados.setForeground(new Color(160, 160, 160));
            panelResultadosBusqueda.add(noResultados);
        } else {
            for (Pelicula peli : resultados) {
                panelResultadosBusqueda.add(crearCardPelicula(peli));
            }
        }
        
        panelResultadosBusqueda.revalidate();
        panelResultadosBusqueda.repaint();
        btnLimpiarBusqueda.setVisible(true);
        cardLayout.show(panelContenedor, "BUSQUEDA");
    }
    
    private List<Pelicula> filtrarLocalmente(List<Pelicula> peliculas, String genero, String clasificacion) {
        List<Pelicula> filtradas = new ArrayList<>();
        
        for (Pelicula peli : peliculas) {
            boolean cumpleGenero = genero.equals("Todos los géneros") || peli.getGenero().equals(genero);
            boolean cumpleClasificacion = clasificacion.equals("Todas las edades") || peli.getClasificacionEdad().equals(clasificacion);
            
            if (cumpleGenero && cumpleClasificacion) {
                filtradas.add(peli);
            }
        }
        
        return filtradas;
    }
    
    private void limpiarBusqueda() {
        txtBusqueda.setText("🔍 Buscar película...");
        txtBusqueda.setForeground(new Color(160, 160, 160));
        cmbGenero.setSelectedIndex(0);
        cmbClasificacion.setSelectedIndex(0);
        btnLimpiarBusqueda.setVisible(false);
        sugerenciasPopup.setVisible(false);
        cardLayout.show(panelContenedor, "SECCIONES");
    }
    
    private void cargarSecciones() {
        panelPeliculasSecciones.removeAll();
        panelPeliculasSecciones.add(Box.createRigidArea(new Dimension(0, 10)));
        int LIMITE_POR_FILA = 10;
        
        List<Pelicula> masTaquilleras = peliculaDAO.obtenerPeliculasMasTaquilleras(LIMITE_POR_FILA);
        if (!masTaquilleras.isEmpty()) {
            panelPeliculasSecciones.add(crearFilaSeccion("🏆 Películas más taquilleras", masTaquilleras));
            panelPeliculasSecciones.add(Box.createRigidArea(new Dimension(0, 30)));
        }

        List<Pelicula> enCartelera = peliculaDAO.obtenerPeliculasConFiltro(null, "En Cartelera", LIMITE_POR_FILA);
        if (!enCartelera.isEmpty()) {
            panelPeliculasSecciones.add(crearFilaSeccion("🎬 En Cartelera", enCartelera));
            panelPeliculasSecciones.add(Box.createRigidArea(new Dimension(0, 30)));
        }
        
        List<Pelicula> proximamente = peliculaDAO.obtenerPeliculasConFiltro(null, "Próximamente", LIMITE_POR_FILA);
        if (!proximamente.isEmpty()) {
            panelPeliculasSecciones.add(crearFilaSeccion("🍿 Próximamente", proximamente));
            panelPeliculasSecciones.add(Box.createRigidArea(new Dimension(0, 30)));
        }
        
        List<String> generosDB = peliculaDAO.obtenerGenerosUnicos();
        
        Map<String, String> emojis = new HashMap<>();
        emojis.put("Acción", "💥");
        emojis.put("Terror", "👻");
        emojis.put("Comedia", "😂");
        emojis.put("Drama", "🎭");
        emojis.put("Ciencia Ficción", "🚀");
        emojis.put("Animación", "🎨");
        emojis.put("Romance", "💕");
        emojis.put("Superhéroes", "🦸");
        emojis.put("Aventura", "🗺️");
        emojis.put("Documental", "📹");
        
        for (String genero : generosDB) {
            List<Pelicula> peliculasGenero = peliculaDAO.obtenerPeliculasConFiltro(genero, "En Cartelera", LIMITE_POR_FILA);
            if (!peliculasGenero.isEmpty()) {
                String emoji = emojis.getOrDefault(genero, "🎬");
                panelPeliculasSecciones.add(crearFilaSeccion(emoji + " " + genero, peliculasGenero));
                panelPeliculasSecciones.add(Box.createRigidArea(new Dimension(0, 30)));
            }
        }
        
        panelPeliculasSecciones.revalidate();
        panelPeliculasSecciones.repaint();
    }
    
    private JPanel crearFilaSeccion(String titulo, List<Pelicula> peliculas) {
        JPanel panelFila = new JPanel(new BorderLayout(0, 10));
        panelFila.setBackground(new Color(30, 30, 30));
        
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setBorder(new EmptyBorder(0, 0, 5, 0));
        panelFila.add(lblTitulo, BorderLayout.NORTH);
        
        JPanel panelTarjetas = new JPanel();
        panelTarjetas.setLayout(new BoxLayout(panelTarjetas, BoxLayout.X_AXIS));
        panelTarjetas.setBackground(new Color(30, 30, 30));
        
        for (Pelicula peli : peliculas) {
            panelTarjetas.add(crearCardPelicula(peli));
            panelTarjetas.add(Box.createRigidArea(new Dimension(15, 0)));
        }
        
        JScrollPane scrollTarjetas = new JScrollPane(panelTarjetas);
        scrollTarjetas.setBorder(null);
        scrollTarjetas.setBackground(new Color(30, 30, 30));
        scrollTarjetas.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollTarjetas.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollTarjetas.getHorizontalScrollBar().setUnitIncrement(16);
        
        scrollTarjetas.setMinimumSize(new Dimension(500, 370));
        scrollTarjetas.setPreferredSize(new Dimension(1100, 370));
        scrollTarjetas.setMaximumSize(new Dimension(Integer.MAX_VALUE, 370));
        
        panelFila.add(scrollTarjetas, BorderLayout.CENTER);
        
        return panelFila;
    }
    
    private JPanel crearCardPelicula(Pelicula peli) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(40, 40, 40));
        card.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        card.setPreferredSize(new Dimension(200, 350));
        card.setMinimumSize(new Dimension(200, 350));
        card.setMaximumSize(new Dimension(200, 350));
        
        final int POSTER_WIDTH = 200;
        final int POSTER_HEIGHT = 280;
        
        JLabel lblPoster = new JLabel();
        lblPoster.setPreferredSize(new Dimension(POSTER_WIDTH, POSTER_HEIGHT));
        lblPoster.setBackground(new Color(60, 60, 60));
        lblPoster.setOpaque(true);
        lblPoster.setHorizontalAlignment(SwingConstants.CENTER);
        lblPoster.setForeground(new Color(160, 160, 160));
        lblPoster.setText("Cargando...");
        
        String urlImagen = peli.getImagen();
        String tituloPeli = peli.getTitulo();

        if (urlImagen != null && !urlImagen.trim().isEmpty()) {
            SwingWorker<ImageIcon, Void> worker = new SwingWorker<ImageIcon, Void>() {
                @Override
                protected ImageIcon doInBackground() throws Exception {
                    try {
                        URL url = new URL(urlImagen);
                        Image image = ImageIO.read(url);
                        if (image != null) {
                            Image scaledImage = image.getScaledInstance(POSTER_WIDTH, POSTER_HEIGHT, Image.SCALE_SMOOTH);
                            return new ImageIcon(scaledImage);
                        }
                    } catch (IOException e) {
                        System.err.println("Error al cargar imagen: " + urlImagen);
                    }
                    return null;
                }
                
                @Override
                protected void done() {
                    try {
                        ImageIcon imageIcon = get();
                        if (imageIcon != null) {
                            lblPoster.setIcon(imageIcon);
                            lblPoster.setText(null);
                        } else {
                            lblPoster.setText(String.valueOf(tituloPeli.charAt(0)));
                            lblPoster.setFont(new Font("Segoe UI", Font.BOLD, 72));
                        }
                    } catch (Exception e) {
                        lblPoster.setText(String.valueOf(tituloPeli.charAt(0)));
                        lblPoster.setFont(new Font("Segoe UI", Font.BOLD, 72));
                    }
                }
            };
            worker.execute();
        } else {
            lblPoster.setText(String.valueOf(tituloPeli.charAt(0)));
            lblPoster.setFont(new Font("Segoe UI", Font.BOLD, 72));
        }
        
        JLabel badge = new JLabel(peli.estaEnCartelera() ? "EN CARTELERA" : "PRÓXIMAMENTE");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 9));
        badge.setForeground(Color.WHITE);
        badge.setOpaque(true);
        badge.setBackground(peli.estaEnCartelera() ? new Color(220, 20, 60) : new Color(234, 179, 8));
        badge.setBorder(new EmptyBorder(4, 8, 4, 8));
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setOpaque(false);
        topPanel.add(badge);
        
        JPanel posterWrapper = new JPanel(new BorderLayout());
        posterWrapper.add(lblPoster, BorderLayout.CENTER);
        posterWrapper.add(topPanel, BorderLayout.NORTH);
        
        JPanel info = new JPanel(new BorderLayout());
        info.setBackground(new Color(40, 40, 40));
        info.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel lblTitulo = new JLabel("<html>" + peli.getTitulo() + "</html>");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitulo.setForeground(Color.WHITE);
        
        JLabel lblFormato = new JLabel(peli.getClasificacionEdad());
        lblFormato.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblFormato.setForeground(new Color(160, 160, 160));
        
        info.add(lblTitulo, BorderLayout.NORTH);
        info.add(lblFormato, BorderLayout.SOUTH);
        
        card.add(posterWrapper, BorderLayout.CENTER);
        card.add(info, BorderLayout.SOUTH);
        
        // CÓDIGO NUEVO (reemplazar con esto):
        card.addMouseListener(new MouseAdapter() {
            @Override 
            public void mouseEntered(MouseEvent e) { 
                card.setBorder(BorderFactory.createLineBorder(new Color(220, 20, 60), 2)); 
            }
            @Override 
            public void mouseExited(MouseEvent e) { 
                card.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80))); 
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                mostrarDetallePelicula(peli.getIdPelicula());
            }
        });
        
        return card;
    }
    private void mostrarDetallePelicula(int idPelicula) {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof NavigationHelper) {
            NavigationHelper navegacion = (NavigationHelper) window;
            navegacion.mostrarDetallePelicula(idPelicula);
        }
    }
}