package CINEMARX.M4.EditarBoleto;

import CINEMARX.M4.CustomDialog;
import CINEMARX.M4.M4;
import CINEMARX.M4.CinemarxTheme;
import CINEMARX.M4.StyledButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditarBoletoDialog extends JDialog {

    private int boletoId;
    private BoletoInfo boletoInfo;
    private int peliculaId;
    private String originalIdioma;
    private boolean guardado = false;

    private JPanel fechasGrid;
    private JPanel horariosGrid;
    private List<StyledButton> fechasBotones;
    private List<StyledButton> horariosBotones;
    
    private JComboBox<String> cbIdioma;
    private JComboBox<String> cbFormato;
    private String selectedIdioma;
    private String selectedFormato;
    
    private int nuevaFuncionId = -1;
    private Date selectedDate = null;

    public EditarBoletoDialog(Dialog owner, int boletoId) {
        super(owner, "Reprogramar Boleto", true);
        this.boletoId = boletoId;
        this.fechasBotones = new ArrayList<>();
        this.horariosBotones = new ArrayList<>();

        setUndecorated(true);
        setSize(900, 750);
        setLocationRelativeTo(owner);
        setBackground(new Color(0,0,0,0));

        JPanel mainPanel = new JPanel(new BorderLayout()) {
             @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CinemarxTheme.BG_MAIN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setBorder(CinemarxTheme.PADDING_CONTAINER);
        setContentPane(mainPanel);

        // --- Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("Reprogramar");
        titleLabel.setFont(CinemarxTheme.FONT_H1);
        titleLabel.setForeground(CinemarxTheme.TEXT_LIGHT);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        StyledButton closeButton = new StyledButton("X", StyledButton.ButtonStyle.TOGGLE);
        closeButton.addActionListener(e -> dispose());
        
        headerPanel.add(closeButton, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // --- Center Panel ---
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Info Entrada
        JLabel infoLabel = new JLabel();
        infoLabel.setFont(CinemarxTheme.FONT_INFO);
        infoLabel.setForeground(CinemarxTheme.TEXT_DARK);
        infoLabel.setBorder(new EmptyBorder(20, 0, 40, 0));
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(infoLabel);

        // Fechas
        JLabel fechasTitle = new JLabel("Fechas Disponibles:");
        fechasTitle.setFont(CinemarxTheme.FONT_H2);
        fechasTitle.setForeground(CinemarxTheme.TEXT_LIGHT);
        fechasTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(fechasTitle);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 24)));
        
        fechasGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 16));
        fechasGrid.setOpaque(false);
        
        JPanel fechasContainer = new JPanel(new BorderLayout());
        fechasContainer.setOpaque(false);
        fechasContainer.setPreferredSize(new Dimension(700, 80));
        fechasContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        fechasContainer.add(fechasGrid, BorderLayout.NORTH);
        fechasContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        centerPanel.add(fechasContainer);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        
        // Filtros de Idioma y Formato
        JPanel filtrosPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        filtrosPanel.setOpaque(false);
        filtrosPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        filtrosPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        cbIdioma = crearComboBox("Idioma");
        cbFormato = crearComboBox("Formato");

        cbIdioma.addActionListener(e -> {
            selectedIdioma = (String) cbIdioma.getSelectedItem();
            if (selectedDate != null) {
                actualizarHorarios();
            }
        });

        cbFormato.addActionListener(e -> {
            selectedFormato = (String) cbFormato.getSelectedItem();
            if (selectedDate != null) {
                actualizarHorarios();
            }
        });
        
        filtrosPanel.add(cbIdioma);
        filtrosPanel.add(cbFormato);
        centerPanel.add(filtrosPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Horarios
        JLabel horariosTitle = new JLabel("Horarios Disponibles:");
        horariosTitle.setFont(CinemarxTheme.FONT_H2);
        horariosTitle.setForeground(CinemarxTheme.TEXT_LIGHT);
        horariosTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(horariosTitle);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 24)));

        horariosGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 16));
        horariosGrid.setOpaque(false);
        
        JPanel horariosContainer = new JPanel(new BorderLayout());
        horariosContainer.setOpaque(false);
        horariosContainer.setPreferredSize(new Dimension(700, 120));
        horariosContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        horariosContainer.add(horariosGrid, BorderLayout.NORTH);
        horariosContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        centerPanel.add(horariosContainer);

        centerPanel.add(Box.createVerticalGlue());

        // --- Bottom Panel ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        StyledButton continueButton = new StyledButton("Continuar", StyledButton.ButtonStyle.GRADIENT);
        continueButton.addActionListener(this::guardarCambios);
        bottomPanel.add(continueButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        cargarInfoBoletoYFunciones(infoLabel);
    }

    private void cargarInfoBoletoYFunciones(JLabel infoLabel) {
        new SwingWorker<BoletoInfo, Void>() {
            @Override
            protected BoletoInfo doInBackground() throws Exception {
                String sql = "SELECT p.ID_Pelicula, p.Titulo, f.FechaFuncion, f.HoraFuncion, f.Idioma, b.NumeroButaca " +
                             "FROM Boleto b " +
                             "JOIN Funcion f ON b.ID_Funcion = f.ID_Funcion " +
                             "JOIN Pelicula p ON f.ID_Pelicula = p.ID_Pelicula " +
                             "WHERE b.ID_Boleto = ?";
                
                Connection conn = M4.getConexion();
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, boletoId);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        return new BoletoInfo(
                            rs.getInt("ID_Pelicula"),
                            rs.getString("Titulo"),
                            rs.getDate("FechaFuncion"),
                            rs.getTime("HoraFuncion"),
                            rs.getString("Idioma"),
                            rs.getString("NumeroButaca")
                        );
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    BoletoInfo info = get();
                    if (info != null) {
                        EditarBoletoDialog.this.boletoInfo = info;
                        peliculaId = info.peliculaId;
                        originalIdioma = info.idioma;
                        
                        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
                        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm'hs'");
                        infoLabel.setText(String.format("<html>Entrada: %s %s<br>%s %s</html>", 
                            info.titulo, info.idioma, sdfDate.format(info.fecha), sdfTime.format(info.hora)));
                        
                        cargarFechasDisponibles();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void cargarFechasDisponibles() {
        fechasGrid.removeAll();
        fechasBotones.clear();
        
        new SwingWorker<List<FechaInfo>, Void>() {
            @Override
            protected List<FechaInfo> doInBackground() throws Exception {
                List<FechaInfo> fechas = new ArrayList<>();
                String sql = "SELECT DISTINCT FechaFuncion FROM Funcion WHERE ID_Pelicula = ? ORDER BY FechaFuncion";
                Connection conn = M4.getConexion();
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, peliculaId);
                    ResultSet rs = pstmt.executeQuery();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");
                    
                    while (rs.next()) {
                        Date fecha = rs.getDate("FechaFuncion");
                        fechas.add(new FechaInfo(fecha, sdf.format(fecha)));
                    }
                }
                return fechas;
            }

            @Override
            protected void done() {
                try {
                    List<FechaInfo> fechas = get();
                    
                    for (FechaInfo fechaInfo : fechas) {
                        StyledButton fechaBtn = new StyledButton(fechaInfo.textoFormateado, StyledButton.ButtonStyle.TOGGLE);
                        fechaBtn.setPreferredSize(new Dimension(100, 50));
                        
                        fechaBtn.addActionListener(e -> {
                            nuevaFuncionId = -1;
                            selectedDate = fechaInfo.fecha;
                            
                            for (StyledButton btn : fechasBotones) {
                                btn.setSelected(false);
                            }
                            
                            fechaBtn.setSelected(true);
                            actualizarFiltros(fechaInfo.fecha);
                        });
                        
                        fechasBotones.add(fechaBtn);
                        fechasGrid.add(fechaBtn);
                    }
                    
                    if (!fechasBotones.isEmpty()) {
                        fechasBotones.get(0).doClick();
                    }
                    
                    fechasGrid.revalidate();
                    fechasGrid.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void actualizarFiltros(Date fecha) {
        new SwingWorker<Map<String, List<String>>, Void>() {
            @Override
            protected Map<String, List<String>> doInBackground() throws Exception {
                Map<String, List<String>> filtros = new HashMap<>();
                filtros.put("idiomas", new ArrayList<>());
                filtros.put("formatos", new ArrayList<>());

                String sqlLang = "SELECT DISTINCT Idioma FROM Funcion WHERE ID_Pelicula = ? AND FechaFuncion = ?";
                Connection conn = M4.getConexion();
                try (PreparedStatement psLang = conn.prepareStatement(sqlLang)) {
                    psLang.setInt(1, peliculaId);
                    psLang.setDate(2, new java.sql.Date(fecha.getTime()));
                    try (ResultSet rsLang = psLang.executeQuery()) {
                        while (rsLang.next()) {
                            filtros.get("idiomas").add(rsLang.getString("Idioma"));
                        }
                    }
                }

                String sqlFormat = "SELECT DISTINCT s.TipoDeSala FROM Sala s " +
                                 "INNER JOIN Funcion f ON s.ID_Sala = f.ID_Sala " +
                                 "WHERE f.ID_Pelicula = ? AND f.FechaFuncion = ?";
                Connection conn2 = M4.getConexion();
                try (PreparedStatement psFormat = conn2.prepareStatement(sqlFormat)) {
                    psFormat.setInt(1, peliculaId);
                    psFormat.setDate(2, new java.sql.Date(fecha.getTime()));
                    try (ResultSet rsFormat = psFormat.executeQuery()) {
                        while (rsFormat.next()) {
                            filtros.get("formatos").add(rsFormat.getString("TipoDeSala"));
                        }
                    }
                }
                return filtros;
            }

            @Override
            protected void done() {
                try {
                    Map<String, List<String>> filtros = get();
                    
                    // Remover todos los listeners temporalmente
                    for (ActionListener al : cbIdioma.getActionListeners()) {
                        cbIdioma.removeActionListener(al);
                    }
                    for (ActionListener al : cbFormato.getActionListeners()) {
                        cbFormato.removeActionListener(al);
                    }

                    // Limpiar y recargar
                    cbIdioma.removeAllItems();
                    cbFormato.removeAllItems();
                    
                    cbIdioma.addItem("Idioma");
                    cbFormato.addItem("Formato");

                    for (String idioma : filtros.get("idiomas")) {
                        cbIdioma.addItem(idioma);
                    }
                    for (String formato : filtros.get("formatos")) {
                        cbFormato.addItem(formato);
                    }

                    if (cbIdioma.getItemCount() > 1) {
                        cbIdioma.setSelectedIndex(1);
                    } else {
                        cbIdioma.setSelectedIndex(0);
                    }
                    if (cbFormato.getItemCount() > 1) {
                        cbFormato.setSelectedIndex(1);
                    } else {
                        cbFormato.setSelectedIndex(0);
                    }
                    selectedIdioma = (String) cbIdioma.getSelectedItem();
                    selectedFormato = (String) cbFormato.getSelectedItem();

                    // Reagregar los listeners
                    cbIdioma.addActionListener(e -> {
                        selectedIdioma = (String) cbIdioma.getSelectedItem();
                        if (selectedDate != null) {
                            actualizarHorarios();
                        }
                    });

                    cbFormato.addActionListener(e -> {
                        selectedFormato = (String) cbFormato.getSelectedItem();
                        if (selectedDate != null) {
                            actualizarHorarios();
                        }
                    });

                    actualizarHorarios();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void actualizarHorarios() {
        horariosGrid.removeAll();
        horariosBotones.clear();
        
        JLabel loadingLabel = new JLabel("Cargando...");
        loadingLabel.setForeground(CinemarxTheme.TEXT_LIGHT);
        loadingLabel.setFont(CinemarxTheme.FONT_INFO);
        horariosGrid.add(loadingLabel);
        horariosGrid.revalidate();
        horariosGrid.repaint();

        new SwingWorker<List<HorarioInfo>, Void>() {
            @Override
            protected List<HorarioInfo> doInBackground() throws Exception {
                List<HorarioInfo> horarios = new ArrayList<>();
                
                StringBuilder sqlBuilder = new StringBuilder(
                    "SELECT DISTINCT f.ID_Funcion, f.HoraFuncion FROM Funcion f " +
                    "JOIN Sala s ON f.ID_Sala = s.ID_Sala WHERE f.ID_Pelicula = ?");
                
                List<Object> params = new ArrayList<>();
                params.add(peliculaId);

                if (selectedDate != null) {
                    sqlBuilder.append(" AND f.FechaFuncion = ?");
                    params.add(new java.sql.Date(selectedDate.getTime()));
                }
                if (selectedIdioma != null && !"Idioma".equals(selectedIdioma)) {
                    sqlBuilder.append(" AND f.Idioma = ?");
                    params.add(selectedIdioma);
                }
                if (selectedFormato != null && !"Formato".equals(selectedFormato)) {
                    sqlBuilder.append(" AND s.TipoDeSala = ?");
                    params.add(selectedFormato);
                }

                sqlBuilder.append(" ORDER BY f.HoraFuncion");

                Connection conn = M4.getConexion();
                try (PreparedStatement ps = conn.prepareStatement(sqlBuilder.toString())) {
                    for (int i = 0; i < params.size(); i++) {
                        ps.setObject(i + 1, params.get(i));
                    }

                    try (ResultSet rs = ps.executeQuery()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                        while (rs.next()) {
                            int funcionId = rs.getInt("ID_Funcion");
                            Time hora = rs.getTime("HoraFuncion");
                            horarios.add(new HorarioInfo(funcionId, sdf.format(hora)));
                        }
                    }
                }
                return horarios;
            }

            @Override
            protected void done() {
                horariosGrid.removeAll();
                
                try {
                    List<HorarioInfo> horarios = get();
                    
                    if (horarios.isEmpty()) {
                        JLabel noHorariosLabel = new JLabel("No hay funciones disponibles");
                        noHorariosLabel.setForeground(CinemarxTheme.TEXT_DARK);
                        noHorariosLabel.setFont(CinemarxTheme.FONT_INFO);
                        horariosGrid.add(noHorariosLabel);
                    } else {
                        for (HorarioInfo horario : horarios) {
                            StyledButton horarioBtn = new StyledButton(horario.horaFormateada, StyledButton.ButtonStyle.TOGGLE);
                            horarioBtn.setPreferredSize(new Dimension(100, 50));
                            
                            horarioBtn.addActionListener(e -> {
                                nuevaFuncionId = horario.funcionId;
                                
                                for (StyledButton btn : horariosBotones) {
                                    btn.setSelected(false);
                                }
                                
                                horarioBtn.setSelected(true);
                            });
                            
                            horariosBotones.add(horarioBtn);
                            horariosGrid.add(horarioBtn);
                        }
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                horariosGrid.revalidate();
                horariosGrid.repaint();
            }
        }.execute();
    }
    
    private JComboBox<String> crearComboBox(String nombre) {
        JComboBox<String> cb = new JComboBox<String>();
        cb.setUI(new CINEMARX.M4.CustomComboBoxUI());
        cb.setOpaque(false);
        cb.setPreferredSize(new Dimension(200, 40));
        cb.setBackground(CinemarxTheme.BG_COMPONENT);
        cb.setForeground(CinemarxTheme.TEXT_LIGHT);
        cb.setFont(CinemarxTheme.FONT_BASE);
        
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                
                if (index == -1) {
                    setForeground(CinemarxTheme.TEXT_LIGHT);
                } else if (index == 0) {
                    setForeground(new Color(150, 150, 150));
                } else {
                    setForeground(CinemarxTheme.TEXT_LIGHT);
                }
                
                if (isSelected) {
                    setBackground(new Color(70, 70, 70));
                } else {
                    setBackground(CinemarxTheme.BG_COMPONENT);
                }
                
                return this;
            }
        });

        cb.addItem(nombre);
        return cb;
    }

    private void guardarCambios(ActionEvent e) {
        if (nuevaFuncionId == -1) {
            new CustomDialog((Frame)getOwner().getOwner(), 
                "Por favor, selecciona una nueva fecha y horario.", 
                CustomDialog.DialogType.INFO).setVisible(true);
            return;
        }

        SeatSelectionDialog seatDialog = new SeatSelectionDialog(this, nuevaFuncionId, boletoInfo.numeroButaca, boletoId);
        seatDialog.setVisible(true);
        String nuevoAsiento = seatDialog.getSelectedSeat();

        if (nuevoAsiento != null) {
            String sql = "UPDATE Boleto SET ID_Funcion = ?, NumeroButaca = ? WHERE ID_Boleto = ?";
            try (Connection conn = M4.getConexion();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, nuevaFuncionId);
                pstmt.setString(2, nuevoAsiento);
                pstmt.setInt(3, boletoId);
                int affectedRows = pstmt.executeUpdate();
                
                if (affectedRows > 0) {
                    this.guardado = true;
                    new CustomDialog((Frame)getOwner().getOwner(), 
                        "Boleto reprogramado con éxito.", 
                        CustomDialog.DialogType.INFO).setVisible(true);
                    dispose();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                new CustomDialog((Frame)getOwner().getOwner(), 
                    "Error al reprogramar el boleto.", 
                    CustomDialog.DialogType.INFO).setVisible(true);
            }
        }
    }

    public boolean seGuardo() {
        return guardado;
    }
    
    // Clases auxiliares
    private static class BoletoInfo {
        int peliculaId;
        String titulo;
        Date fecha;
        Time hora;
        String idioma;
        String numeroButaca;
        
        BoletoInfo(int peliculaId, String titulo, Date fecha, Time hora, String idioma, String numeroButaca) {
            this.peliculaId = peliculaId;
            this.titulo = titulo;
            this.fecha = fecha;
            this.hora = hora;
            this.idioma = idioma;
            this.numeroButaca = numeroButaca;
        }
    }
    
    private static class FechaInfo {
        Date fecha;
        String textoFormateado;
        
        FechaInfo(Date fecha, String textoFormateado) {
            this.fecha = fecha;
            this.textoFormateado = textoFormateado;
        }
    }
    
    private static class HorarioInfo {
        int funcionId;
        String horaFormateada;
        
        HorarioInfo(int funcionId, String horaFormateada) {
            this.funcionId = funcionId;
            this.horaFormateada = horaFormateada;
        }
    }
}
