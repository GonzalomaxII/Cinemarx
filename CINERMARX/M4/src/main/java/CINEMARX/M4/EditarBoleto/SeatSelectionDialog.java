package CINEMARX.M4.EditarBoleto;

import CINEMARX.M4.CinemarxTheme;
import CINEMARX.M4.M4;
import CINEMARX.M4.StyledButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;

public class SeatSelectionDialog extends JDialog {

    private int idFuncion;
    private String originalSeat;
    private int boletoId;
    private String selectedSeat;

    private int cantButacas;
    private Set<String> butacasOcupadas = new HashSet<>();
    private ButtonGroup seatGroup = new ButtonGroup();
    private StyledButton confirmButton;

    public SeatSelectionDialog(Dialog owner, int idFuncion, String originalSeat, int boletoId) {
        super(owner, "Seleccionar Butaca", true);
        this.idFuncion = idFuncion;
        this.originalSeat = originalSeat;
        this.boletoId = boletoId;

        setUndecorated(true);
        setSize(800, 600);
        setLocationRelativeTo(owner);
        setBackground(new Color(0, 0, 0, 0));

        JPanel loadingPanel = new JPanel(new BorderLayout());
        loadingPanel.setBackground(CinemarxTheme.BG_MAIN);
        JLabel loadingLabel = new JLabel("Cargando butacas...");
        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loadingLabel.setFont(CinemarxTheme.FONT_H2);
        loadingLabel.setForeground(CinemarxTheme.TEXT_LIGHT);
        loadingPanel.add(loadingLabel, BorderLayout.CENTER);
        setContentPane(loadingPanel);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                cargarDatosSala();
                return null; // UI is created in done()
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    JPanel mainPanel = inicializarUI();
                    setContentPane(mainPanel);
                    revalidate();
                    repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                    // Handle error, maybe show an error message
                }
            }
        }.execute();
    }

    private void cargarDatosSala() throws SQLException {
        String sqlSala = "SELECT CantButacas FROM Sala WHERE ID_Sala = (SELECT ID_Sala FROM Funcion WHERE ID_Funcion = ?)";
        Connection conn = M4.getConexion();
        try (PreparedStatement pstmt = conn.prepareStatement(sqlSala)) {
            pstmt.setInt(1, idFuncion);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    this.cantButacas = rs.getInt("CantButacas");
                }
            }
        }

        String sqlButacas = "SELECT NumeroButaca FROM Boleto WHERE ID_Funcion = ? AND ID_Boleto != ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sqlButacas)) {
            pstmt.setInt(1, idFuncion);
            pstmt.setInt(2, boletoId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    butacasOcupadas.add(rs.getString("NumeroButaca"));
                }
            }
        }
    }

    private JPanel inicializarUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createLineBorder(CinemarxTheme.BORDER_COLOR, 2, true));

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(CinemarxTheme.BG_MAIN);
        contentPanel.setBorder(CinemarxTheme.PADDING_CONTAINER);
        mainPanel.add(contentPanel);

        JLabel titleLabel = new JLabel("Seleccione una nueva butaca");
        titleLabel.setFont(CinemarxTheme.FONT_H2);
        titleLabel.setForeground(CinemarxTheme.TEXT_LIGHT);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        
        JPanel panelPantalla = crearPanelPantalla();
        centerPanel.add(panelPantalla, BorderLayout.NORTH);

        confirmButton = new StyledButton("Confirmar", StyledButton.ButtonStyle.GRADIENT);
        confirmButton.setEnabled(false);
        confirmButton.addActionListener(e -> {
            if (selectedSeat != null) {
                dispose();
            }
        });

        JPanel panelButacas = crearPanelButacas();
        JScrollPane scrollPane = new JScrollPane(panelButacas);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(CinemarxTheme.BG_MAIN);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        contentPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        bottomPanel.add(confirmButton);
        
        JPanel legendPanel = crearPanelLeyenda();
        
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);
        southPanel.add(legendPanel, BorderLayout.CENTER);
        southPanel.add(bottomPanel, BorderLayout.EAST);
        
        contentPanel.add(southPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private JPanel crearPanelPantalla() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0x2B2B2B));
        panel.setBorder(new javax.swing.border.EmptyBorder(20, 0, 30, 0));

        try {
            URL imageUrl = new URL("https://gaelschenone.aguilucho.ar/source_cmx/index.php?preview=M4%2FPantallaSala.png");
            InputStream in = imageUrl.openStream();
            BufferedImage originalImage = ImageIO.read(in);
            in.close();

            if (originalImage != null) {
                int newWidth = 600;
                int newHeight = (originalImage.getHeight() * newWidth) / originalImage.getWidth();
                Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
                panel.add(imageLabel, BorderLayout.CENTER);
            } else {
                throw new Exception("Screen image could not be read.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JLabel lblPantalla = new JLabel("PANTALLA (Error al cargar imagen)", JLabel.CENTER);
            lblPantalla.setFont(new Font("Arial", Font.BOLD, 14));
            lblPantalla.setForeground(Color.RED);
            panel.add(lblPantalla, BorderLayout.CENTER);
        }

        return panel;
    }

    private JPanel crearPanelButacas() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(0x2B2B2B));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);

        int numMainRows = (cantButacas > 9) ? (cantButacas - 9) / 17 : 0;

        // Fila frontal (A)
        gbc.gridy = 0;
        for (int i = 0; i < 9; i++) {
            gbc.gridx = i + 5; // Centered
            String id = "A" + (i + 1);
            agregarButaca(panel, id, gbc);
        }

        // Separador
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 19;
        panel.add(Box.createVerticalStrut(15), gbc);
        gbc.gridwidth = 1;

        // Filas principales
        for (int row = 0; row < numMainRows; row++) {
            gbc.gridy++;
            char letra = (char) ('B' + row);

            for (int i = 0; i < 3; i++) {
                gbc.gridx = i;
                String id = letra + "" + (i + 1);
                agregarButaca(panel, id, gbc);
            }
            gbc.gridx = 3;
            panel.add(Box.createHorizontalStrut(18), gbc);
            for (int i = 0; i < 11; i++) {
                gbc.gridx = i + 4;
                String id = letra + "" + (i + 4);
                agregarButaca(panel, id, gbc);
            }
            gbc.gridx = 15;
            panel.add(Box.createHorizontalStrut(18), gbc);
            for (int i = 0; i < 3; i++) {
                gbc.gridx = i + 16;
                String id = letra + "" + (i + 15);
                agregarButaca(panel, id, gbc);
            }
        }
        return panel;
    }

    private JPanel crearPanelLeyenda() {
        JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 10));
        leyenda.setOpaque(false);
        
        leyenda.add(crearItemLeyenda("Disponible", Color.WHITE, false));
        leyenda.add(crearItemLeyenda("No disponible", new Color(100, 100, 100), false));
        leyenda.add(crearItemLeyenda("Seleccionado", new Color(220, 50, 50), true));
        
        return leyenda;
    }

    private JPanel crearItemLeyenda(String texto, Color color, boolean isSelected) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        item.setOpaque(false);
        
        LeyendaButaca circulo = new LeyendaButaca(color, isSelected);
        
        JLabel label = new JLabel(texto);
        label.setFont(CinemarxTheme.FONT_BASE);
        label.setForeground(CinemarxTheme.TEXT_LIGHT);
        
        item.add(circulo);
        item.add(label);
        
        return item;
    }
    
    class LeyendaButaca extends JPanel {
        private Color color;
        private boolean isSelected;

        LeyendaButaca(Color color, boolean isSelected) {
            this.color = color;
            this.isSelected = isSelected;
            setPreferredSize(new Dimension(25, 25));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

            if (isSelected) {
                g2.setColor(new Color(0x6C0002));
                g2.setStroke(new BasicStroke(2));
                int w = getWidth();
                int h = getHeight();
                g2.drawLine(w / 4, h / 2, w / 2, h * 3 / 4);
                g2.drawLine(w / 2, h * 3 / 4, w * 3 / 4, h / 4);
            }
            g2.dispose();
        }
    }

    private void agregarButaca(JPanel panel, String id, GridBagConstraints gbc) {
        CINEMARX.M4.ButacaBoton btn = new CINEMARX.M4.ButacaBoton(id);
        if (butacasOcupadas.contains(id)) {
            btn.setOcupada(true);
        } else {
            btn.addActionListener(e -> {
                selectedSeat = btn.getSeatId();
                confirmButton.setEnabled(true);
            });
        }
        seatGroup.add(btn);
        panel.add(btn, gbc);

        if (id.equals(originalSeat) && !btn.isOcupada()) {
            btn.doClick();
        }
    }

    public String getSelectedSeat() {
        return selectedSeat;
    }
}
