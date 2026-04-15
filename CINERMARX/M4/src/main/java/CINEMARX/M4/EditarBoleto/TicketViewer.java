package CINEMARX.M4.EditarBoleto;

import CINEMARX.M4.CustomDialog;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream; // Added
import java.net.URL;        // Added
import java.sql.*;
import CINEMARX.M4.M4;
import java.awt.event.MouseAdapter; // Added
import java.awt.event.MouseEvent;   // Added
import java.util.HashMap;
import java.util.Map;
import CINEMARX.M4.StyledButton; // Added

public class TicketViewer extends JDialog {

    private JPanel ticketPanel;
    private JLabel qrLabel;
    private JLabel infoLabel;
    private int boletoId;
    private StyledButton saveBtn; // Made saveBtn a field

    public TicketViewer(Dialog owner, int boletoId) {
        super(owner, "Boleto #" + boletoId, true); // true for modal
        this.boletoId = boletoId;
        setUndecorated(true); // Make the window undecorated
        setBackground(new Color(0, 0, 0, 0)); // Make background transparent
        initUI();
        loadTicketData();
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Changed from JFrame.EXIT_ON_CLOSE
        setSize(420, 600);
        setLocationRelativeTo(owner); // Center relative to the owner
        // Ensure the window appears on top and gains focus
        this.toFront();
        this.requestFocus();
    }

    private void initUI() {
        ticketPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 30, 30)); // Dark background for the custom window
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); // Rounded corners
                g2.dispose();
            }
        };
        ticketPanel.setOpaque(false); // Make panel transparent to show custom painting
        ticketPanel.setLayout(new BorderLayout(10, 10));
        ticketPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Make dialog draggable
        Point initialClick = new Point();
        ticketPanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                initialClick.setLocation(e.getPoint());
            }
        });

        ticketPanel.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                int thisX = getLocation().x;
                int thisY = getLocation().y;

                int xMoved = e.getX() - initialClick.x;
                int yMoved = e.getY() - initialClick.y;

                int X = thisX + xMoved;
                int Y = thisY + yMoved;
                setLocation(X, Y);
            }
        });

        // Header Panel for Logo and Close Button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        // Add Cinemarx Logo
        JLabel logoLabel = new JLabel();
        try {
            URL imageUrl = new URL("https://gaelschenone.aguilucho.ar/source_cmx/index.php?preview=logos%2FCINEMARX%20imagotipo.png"); // New logo URL
            InputStream in = imageUrl.openStream();
            BufferedImage originalImage = ImageIO.read(in);
            in.close();
            
            if (originalImage != null) {
                int newHeight = 30; // Smaller height
                int newWidth = (originalImage.getWidth() * newHeight) / originalImage.getHeight();
                Image logo = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                logoLabel.setIcon(new ImageIcon(logo));
                logoLabel.setHorizontalAlignment(SwingConstants.LEFT); // Align to left
            } else {
                throw new IOException("Logo image could not be read from URL.");
            }
        } catch (IOException ex) { // Catch IOException for URL.openStream() and ImageIO.read()
            ex.printStackTrace();
            logoLabel.setText("CINEMAR X");
            logoLabel.setForeground(Color.WHITE);
            logoLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        }
        headerPanel.add(logoLabel, BorderLayout.WEST);

        // Add Close Button
        JButton closeButton = new JButton();
        closeButton.setOpaque(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        try {
            URL closeIconUrl = new URL("https://gaelschenone.aguilucho.ar/source_cmx/index.php?preview=botones%2Fm4%2Ficonx-mark.svg");
            InputStream in = closeIconUrl.openStream();
            BufferedImage closeImage = ImageIO.read(in);
            in.close();
            if (closeImage != null) {
                Image scaledCloseIcon = closeImage.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                closeButton.setIcon(new ImageIcon(scaledCloseIcon));
            } else {
                // Fallback to text if image cannot be read
                closeButton.setText("X");
                closeButton.setForeground(Color.WHITE);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            closeButton.setText("X");
            closeButton.setForeground(Color.WHITE);
        }
        closeButton.addActionListener(e -> dispose());
        headerPanel.add(closeButton, BorderLayout.EAST);
        
        ticketPanel.add(headerPanel, BorderLayout.NORTH);

        qrLabel = new JLabel();
        qrLabel.setHorizontalAlignment(SwingConstants.CENTER);

        infoLabel = new JLabel();
        infoLabel.setVerticalAlignment(SwingConstants.TOP);
        infoLabel.setHorizontalAlignment(SwingConstants.LEFT); // Align info to left
        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        infoLabel.setForeground(Color.LIGHT_GRAY); // Changed to LIGHT_GRAY for better contrast on dark background

        saveBtn = new StyledButton("Descargar PNG", StyledButton.ButtonStyle.GRADIENT); // Made saveBtn a field
        saveBtn.addActionListener(e -> {
            // Retrieve data for filename
            Map<String, String> currentData = fetchTicketData(boletoId);
            if (currentData != null) {
                String movieTitle = currentData.getOrDefault("Pelicula", "Boleto");
                String functionDate = currentData.getOrDefault("FechaFuncion", "");
                String functionTime = currentData.getOrDefault("HoraFuncion", "").replace(":", "-"); // Sanitize time
                
                // Sanitize movie title for filename
                String sanitizedMovieTitle = movieTitle.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
                String filename = String.format("%s_%s_%s.png", sanitizedMovieTitle, functionDate, functionTime);

                try {
                    // Temporarily hide the button
                    saveBtn.setVisible(false);
                    saveComponentAsPNG(ticketPanel, filename);
                    // Show the button again
                    saveBtn.setVisible(true);

                    // Get user's Downloads directory for the message
                    String userHome = System.getProperty("user.home");
                    File downloadsDir = new File(userHome, "Downloads");
                    String fullPath = new File(downloadsDir, filename).getAbsolutePath();

                    new CustomDialog((Dialog) this.getOwner(), "Boleto guardado exitosamente en:<br>" + fullPath, CustomDialog.DialogType.INFO).setVisible(true);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    new CustomDialog((Dialog) this.getOwner(), "Error al guardar PNG: " + ex.getMessage(), CustomDialog.DialogType.INFO).setVisible(true);
                }
            } else {
                new CustomDialog((Dialog) this.getOwner(), "No se pudo obtener la información del boleto para guardar.", CustomDialog.DialogType.INFO).setVisible(true);
            }
        });

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(infoLabel, BorderLayout.NORTH); // Info at NORTH of bottom panel
        bottom.add(qrLabel, BorderLayout.CENTER); // QR at CENTER of bottom panel
        bottom.add(saveBtn, BorderLayout.SOUTH);

        ticketPanel.add(bottom, BorderLayout.CENTER); // bottom panel now takes CENTER of ticketPanel
        add(ticketPanel);
    }

    private void loadTicketData() {
        Map<String, String> data = fetchTicketData(boletoId);
        if (data == null) {
            new CustomDialog((Dialog) this.getOwner(), "No se encontró el boleto #" + boletoId, CustomDialog.DialogType.INFO).setVisible(true);
            return;
        }

        // --- Texto plano para el QR ---
        String qrText = String.format(
            "{\"boleto\":%s,\"cliente\":\"%s %s\",\"pelicula\":\"%s\",\"sala\":\"%s\",\"asiento\":\"%s\",\"fecha\":\"%s\",\"hora\":\"%s\"}",
            data.get("ID_Boleto"),
            escapeJson(data.get("ClienteNombre")),
            escapeJson(data.get("ClienteApellido")),
            escapeJson(data.get("Pelicula")),
            data.get("SalaNumero"),
            data.get("NumeroButaca"),
            data.get("FechaFuncion"),
            data.get("HoraFuncion")
        );

        try {
            BufferedImage qrImg = generateQRCodeImage(qrText, 250, 250); // Smaller QR code
            qrLabel.setIcon(new ImageIcon(qrImg));
        } catch (WriterException | IOException ex) {
            ex.printStackTrace();
            qrLabel.setText("Error generando QR");
        }

        // --- Info debajo del QR ---
        String html = "<html><div style='text-align:left;'>" // Align info to left
            + "<b>" + data.get("ClienteNombre") + " " + data.get("ClienteApellido") + "</b><br/>"
            + "<b>Película:</b> " + data.get("Pelicula") + "<br/>"
            + "<b>Sala:</b> " + data.get("SalaNumero")
            + " &nbsp;&nbsp; <b>Asiento:</b> " + data.get("NumeroButaca") + "<br/>"
            + "<b>Fecha:</b> " + data.get("FechaFuncion")
            + " &nbsp;&nbsp; <b>Hora:</b> " + data.get("HoraFuncion")
            + "<br/><small>" + data.getOrDefault("CineNombre", "") + "</small>"
            + "</div></html>";
        infoLabel.setText(html);
    }

    private Map<String, String> fetchTicketData(int idBoleto) {
        String sql = "SELECT b.ID_Boleto, b.NumeroButaca, f.FechaFuncion, f.HoraFuncion, "
                   + "s.Numero AS SalaNumero, p.Titulo AS Pelicula, "
                   + "c.Nombre AS CineNombre, u.Nombre AS ClienteNombre, u.Apellido AS ClienteApellido "
                   + "FROM Boleto b "
                   + "JOIN Funcion f ON b.ID_Funcion = f.ID_Funcion "
                   + "JOIN Sala s ON f.ID_Sala = s.ID_Sala "
                   + "JOIN Pelicula p ON f.ID_Pelicula = p.ID_Pelicula "
                   + "JOIN Cine c ON s.ID_Cine = c.ID_Cine "
                   + "LEFT JOIN Cliente cl ON b.ID_Cliente = cl.ID_Cliente "
                   + "LEFT JOIN Usuario u ON cl.DNI = u.DNI "
                   + "WHERE b.ID_Boleto = ?";

        try (Connection conn = M4.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idBoleto);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Map<String, String> map = new HashMap<>();
                map.put("ID_Boleto", String.valueOf(rs.getInt("ID_Boleto")));
                map.put("NumeroButaca", rs.getString("NumeroButaca"));
                map.put("FechaFuncion", rs.getDate("FechaFuncion").toString());
                map.put("HoraFuncion", rs.getTime("HoraFuncion").toString());
                map.put("SalaNumero", rs.getString("SalaNumero"));
                map.put("Pelicula", rs.getString("Pelicula"));
                map.put("CineNombre", rs.getString("CineNombre"));
                map.put("ClienteNombre", rs.getString("ClienteNombre") != null ? rs.getString("ClienteNombre") : "");
                map.put("ClienteApellido", rs.getString("ClienteApellido") != null ? rs.getString("ClienteApellido") : "");
                return map;
            }
        } catch (SQLException ex) {
            ex.printStackTrace(System.err); // Print full stack trace to console
            SwingUtilities.invokeLater(() ->
                new CustomDialog((Dialog) this.getOwner(), "Error en la base de datos:\n" + ex.getMessage(), CustomDialog.DialogType.INFO).setVisible(true)
            );
            return null;
        }
    }

    private BufferedImage generateQRCodeImage(String text, int width, int height)
            throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    private void saveComponentAsPNG(Component comp, String filename) throws IOException {
        Dimension size = comp.getSize();
        if (size.width == 0 || size.height == 0) {
            comp.doLayout();
            size = comp.getPreferredSize();
        }
        BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        comp.paint(g2);
        g2.dispose();

        // Get user's Downloads directory
        String userHome = System.getProperty("user.home");
        File downloadsDir = new File(userHome, "Downloads");
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs(); // Create Downloads directory if it doesn't exist
        }
        File outputFile = new File(downloadsDir, filename);
        try {
            ImageIO.write(image, "png", outputFile);
        } catch (IOException ex) {
            ex.printStackTrace(System.err); // Print full stack trace to console
            throw new IOException("Error al guardar la imagen en " + outputFile.getAbsolutePath() + ": " + ex.getMessage(), ex);
        }
    }

    private String escapeJson(String s) {
        return (s == null) ? "" : s.replace("\"", "\\\"");
    }

    // Para prueba local
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TicketViewer(null, 1).setVisible(true));
    }
}
