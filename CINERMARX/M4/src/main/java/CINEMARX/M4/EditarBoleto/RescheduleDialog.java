package CINEMARX.M4.EditarBoleto;

import CINEMARX.M4.CustomDialog;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RescheduleDialog extends JDialog {
    private TicketInfo ticket;
    private boolean successful = false;

    private JPanel datesPanel;
    private JPanel timesPanel;
    private JComboBox<String> idiomaCombo;
    private JComboBox<String> formatoCombo;

    private Map<JButton, FunctionOption> mapaFunciones;
    private FunctionOption selectedFunction;
    private JButton selectedDateButton;
    private JButton selectedTimeButton;
    
    private Date selectedDate;
    private String selectedIdioma;
    private String selectedFormato;


    private static class FunctionOption {
        int idFuncion;
        String fecha;
        String hora;
        String idioma;
        String formato;
        int idSala;

        FunctionOption(int idFuncion, String fecha, String hora, String idioma, int idSala) {
            this.idFuncion = idFuncion;
            this.fecha = fecha;
            this.hora = hora;
            this.idioma = idioma;
            this.idSala = idSala;
            this.formato = getSalaType(idSala);
        }

        private static String getSalaType(int idSala) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("SELECT TipoDeSala FROM Sala WHERE ID_Sala = ?")) {
                pstmt.setInt(1, idSala);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getString("TipoDeSala");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return "Standard";
        }
    }

    public RescheduleDialog(Frame owner, TicketInfo ticket) {
        super(owner, true);
        this.ticket = ticket;
        this.mapaFunciones = new HashMap<>();

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setSize(800, 700);
        setLocationRelativeTo(owner);

        initComponents();
        cargarFechas();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(31, 31, 31));
        mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));
        setContentPane(mainPanel);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Reprogramar");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Content
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Ticket Info
        JLabel infoLabel = new JLabel(String.format(
            "<html>Entrada: %s %s<br>%s %s</html>",
            ticket.tituloMovie, ticket.idioma, ticket.fecha, ticket.hora));
        infoLabel.setForeground(new Color(176, 176, 176));
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(infoLabel);
        contentPanel.add(Box.createVerticalStrut(50));

        // Fechas
        JLabel fechasLabel = new JLabel("Fechas Disponibles:");
        fechasLabel.setForeground(Color.WHITE);
        fechasLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        fechasLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(fechasLabel);
        contentPanel.add(Box.createVerticalStrut(24));

        datesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        datesPanel.setOpaque(false);
        datesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(datesPanel);
        contentPanel.add(Box.createVerticalStrut(40));

        // Selectors
        JPanel selectorsPanel = new JPanel();
        selectorsPanel.setOpaque(false);
        selectorsPanel.setLayout(new BoxLayout(selectorsPanel, BoxLayout.X_AXIS));
        selectorsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        idiomaCombo = createStyledComboBox(new String[]{"Idioma"});
        formatoCombo = createStyledComboBox(new String[]{"Formato"});
        
        selectorsPanel.add(idiomaCombo);
        selectorsPanel.add(Box.createHorizontalStrut(20));
        selectorsPanel.add(formatoCombo);
        contentPanel.add(selectorsPanel);
        contentPanel.add(Box.createVerticalStrut(60));

        // Horarios
        JLabel horariosLabel = new JLabel("Horarios Disponibles:");
        horariosLabel.setForeground(Color.WHITE);
        horariosLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        horariosLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(horariosLabel);
        contentPanel.add(Box.createVerticalStrut(24));

        timesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        timesPanel.setOpaque(false);
        timesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(timesPanel);
        
        contentPanel.add(Box.createVerticalGlue());

        // Bottom button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottomPanel.setOpaque(false);
        JButton continueButton = new JButton("Continuar");
        styleContinueButton(continueButton);
        continueButton.addActionListener(e -> confirmReschedule());
        bottomPanel.add(continueButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        idiomaCombo.addActionListener(e -> {
            selectedIdioma = (String) idiomaCombo.getSelectedItem();
            actualizarHorarios();
        });
        formatoCombo.addActionListener(e -> {
            selectedFormato = (String) formatoCombo.getSelectedItem();
            actualizarHorarios();
        });
    }

    private void cargarFechas() {
        datesPanel.removeAll();

        new SwingWorker<Map<Date, java.util.List<String>>, Void>() {
            @Override
            protected Map<Date, java.util.List<String>> doInBackground() throws Exception {
                Map<Date, java.util.List<String>> fechas = new LinkedHashMap<>();
                String sql = "SELECT DISTINCT FechaFuncion FROM Funcion WHERE ID_Pelicula = (SELECT ID_Pelicula FROM Pelicula WHERE Titulo = ?) AND FechaFuncion >= CURDATE() AND ID_Funcion != ? ORDER BY FechaFuncion";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, ticket.tituloMovie);
                    ps.setInt(2, ticket.idFuncion);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            fechas.put(rs.getDate("FechaFuncion"), new ArrayList<>());
                        }
                    }
                }
                return fechas;
            }

            @Override
            protected void done() {
                try {
                    Map<Date, java.util.List<String>> fechas = get();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");

                    for (Date fecha : fechas.keySet()) {
                        JButton btnFecha = createChoiceButton(sdf.format(fecha), "date");
                        btnFecha.addActionListener(e -> {
                            selectedDate = fecha;
                            updateButtonSelection((JButton)e.getSource(), "date");
                            actualizarFiltros(fecha);
                        });
                        datesPanel.add(btnFecha);
                    }

                    if (datesPanel.getComponentCount() > 0) {
                        Component firstButton = datesPanel.getComponent(0);
                        if (firstButton instanceof JButton) {
                            ((JButton) firstButton).doClick();
                        }
                    }
                    datesPanel.revalidate();
                    datesPanel.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }
    
    private void actualizarFiltros(Date fecha) {
        new SwingWorker<Map<String, java.util.List<String>>, Void>() {
            @Override
            protected Map<String, java.util.List<String>> doInBackground() throws Exception {
                Map<String, java.util.List<String>> filtros = new HashMap<>();
                filtros.put("idiomas", new ArrayList<>());
                filtros.put("formatos", new ArrayList<>());

                String sqlLang = "SELECT DISTINCT Idioma FROM Funcion WHERE ID_Pelicula = (SELECT ID_Pelicula FROM Pelicula WHERE Titulo = ?) AND FechaFuncion = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement psLang = conn.prepareStatement(sqlLang)) {
                    psLang.setString(1, ticket.tituloMovie);
                    psLang.setDate(2, new java.sql.Date(fecha.getTime()));
                    try (ResultSet rsLang = psLang.executeQuery()) {
                        while (rsLang.next()) {
                            filtros.get("idiomas").add(rsLang.getString("Idioma"));
                        }
                    }
                }

                String sqlFormat = "SELECT DISTINCT s.TipoDeSala FROM Sala s INNER JOIN Funcion f ON s.ID_Sala = f.ID_Sala WHERE f.ID_Pelicula = (SELECT ID_Pelicula FROM Pelicula WHERE Titulo = ?) AND f.FechaFuncion = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement psFormat = conn.prepareStatement(sqlFormat)) {
                    psFormat.setString(1, ticket.tituloMovie);
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
                    Map<String, java.util.List<String>> filtros = get();
                    ActionListener idiomaListener = idiomaCombo.getActionListeners().length > 0 ? idiomaCombo.getActionListeners()[0] : null;
                    ActionListener formatoListener = formatoCombo.getActionListeners().length > 0 ? formatoCombo.getActionListeners()[0] : null;
                    if (idiomaListener != null) idiomaCombo.removeActionListener(idiomaListener);
                    if (formatoListener != null) formatoCombo.removeActionListener(formatoListener);

                    idiomaCombo.removeAllItems();
                    formatoCombo.removeAllItems();
                    
                    idiomaCombo.addItem("Idioma");
                    formatoCombo.addItem("Formato");

                    for (String idioma : filtros.get("idiomas")) {
                        idiomaCombo.addItem(idioma);
                    }
                    for (String formato : filtros.get("formatos")) {
                        formatoCombo.addItem(formato);
                    }

                    idiomaCombo.setSelectedIndex(0);
                    formatoCombo.setSelectedIndex(0);
                    selectedIdioma = (String) idiomaCombo.getSelectedItem();
                    selectedFormato = (String) formatoCombo.getSelectedItem();

                    if (idiomaListener != null) idiomaCombo.addActionListener(idiomaListener);
                    if (formatoListener != null) formatoCombo.addActionListener(formatoListener);

                    actualizarHorarios();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }
    
    private void actualizarHorarios() {
        timesPanel.removeAll();
        mapaFunciones.clear();

        new SwingWorker<Map<JButton, FunctionOption>, Void>() {
            @Override
            protected Map<JButton, FunctionOption> doInBackground() throws Exception {
                Map<JButton, FunctionOption> funciones = new LinkedHashMap<>();
                StringBuilder sqlBuilder = new StringBuilder(
                    "SELECT DISTINCT f.ID_Funcion, f.ID_Sala, f.HoraFuncion, f.Idioma, s.TipoDeSala FROM Funcion f JOIN Sala s ON f.ID_Sala = s.ID_Sala WHERE f.ID_Pelicula = (SELECT ID_Pelicula FROM Pelicula WHERE Titulo = ?)");
                
                java.util.List<Object> params = new ArrayList<>();
                params.add(ticket.tituloMovie);

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

                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sqlBuilder.toString())) {
                    for (int i = 0; i < params.size(); i++) {
                        ps.setObject(i + 1, params.get(i));
                    }

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int idFuncion = rs.getInt("ID_Funcion");
                            if (hasAvailableSeats(idFuncion, ticket.numeroButaca)) {
                                String hora = rs.getString("HoraFuncion");
                                JButton btnHora = createChoiceButton(hora.substring(0, 5), "time");
                                
                                FunctionOption info = new FunctionOption(
                                    idFuncion,
                                    selectedDate.toString(),
                                    hora,
                                    rs.getString("Idioma"),
                                    rs.getInt("ID_Sala")
                                );
                                funciones.put(btnHora, info);
                            }
                        }
                    }
                }
                return funciones;
            }

            @Override
            protected void done() {
                try {
                    Map<JButton, FunctionOption> funciones = get();
                    mapaFunciones.putAll(funciones);
                    for (Map.Entry<JButton, FunctionOption> entry : funciones.entrySet()) {
                        JButton btnHora = entry.getKey();
                        FunctionOption info = entry.getValue();
                        btnHora.addActionListener(e -> {
                            selectedFunction = info;
                            updateButtonSelection((JButton)e.getSource(), "time");
                        });
                        timesPanel.add(btnHora);
                    }

                    if (timesPanel.getComponentCount() > 0) {
                        Component firstButton = timesPanel.getComponent(0);
                        if (firstButton instanceof JButton) {
                            ((JButton) firstButton).doClick();
                        }
                    }

                    timesPanel.revalidate();
                    timesPanel.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private boolean hasAvailableSeats(int idFuncion, String preferredSeat) {
        String query = "SELECT COUNT(*) as count FROM Boleto WHERE ID_Funcion = ? AND NumeroButaca = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idFuncion);
            pstmt.setString(2, preferredSeat);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") == 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void confirmReschedule() {
        if (selectedFunction == null) {
            new CustomDialog((Frame) getOwner(), "Por favor seleccione una nueva fecha y hora").setVisible(true);
            return;
        }

        String updateQuery = "UPDATE Boleto SET ID_Funcion = ? WHERE ID_Boleto = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
            pstmt.setInt(1, selectedFunction.idFuncion);
            pstmt.setInt(2, ticket.idBoleto);
            pstmt.executeUpdate();
            successful = true;
            dispose();
        } catch (SQLException e) {
            e.printStackTrace();
            new CustomDialog((Frame) getOwner(), "Error al reprogramar la entrada").setVisible(true);
        }
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        combo.setBackground(new Color(45, 45, 45));
        combo.setForeground(Color.WHITE);
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(64, 64, 64), 2),
            new EmptyBorder(16, 18, 16, 18)
        ));
        combo.setUI(new CustomComboBoxUI());
        return combo;
    }

    private JButton createChoiceButton(String text, String type) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 20));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(31, 31, 31));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(64, 64, 64), 2),
            new EmptyBorder(18, 30, 18, 30)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                if (!button.isSelected()) {
                    button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(255, 71, 87)),
                        new EmptyBorder(18, 30, 18, 30)
                    ));
                }
            }
            public void mouseExited(MouseEvent evt) {
                if (!button.isSelected()) {
                     button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(64, 64, 64), 2),
                        new EmptyBorder(18, 30, 18, 30)
                    ));
                }
            }
        });
        return button;
    }
    
    private void updateButtonSelection(JButton button, String type) {
        if (type.equals("date")) {
            if (selectedDateButton != null) {
                selectedDateButton.setSelected(false);
                selectedDateButton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(64, 64, 64), 2),
                    new EmptyBorder(18, 30, 18, 30)
                ));
            }
            selectedDateButton = button;
        } else {
            if (selectedTimeButton != null) {
                selectedTimeButton.setSelected(false);
                selectedTimeButton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(64, 64, 64), 2),
                    new EmptyBorder(18, 30, 18, 30)
                ));
            }
            selectedTimeButton = button;
        }
        
        button.setSelected(true);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 71, 87), 2),
            new EmptyBorder(18, 30, 18, 30)
        ));
    }

    private void styleContinueButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 18));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(255, 71, 87));
        button.setBorder(new EmptyBorder(16, 48, 16, 48));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(new Color(232, 65, 78));
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(new Color(255, 71, 87));
            }
        });
    }

    private String formatDate(String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            return localDate.format(DateTimeFormatter.ofPattern("dd/MM"));
        } catch (Exception e) {
            return date;
        }
    }

    public boolean wasSuccessful() {
        return successful;
    }
    
    private static class CustomComboBoxUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            JButton button = new JButton("▼");
            button.setBackground(new Color(45, 45, 45));
            button.setForeground(Color.WHITE);
            button.setBorder(new EmptyBorder(0,0,0,10));
            return button;
        }
    }
}
