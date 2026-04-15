package CINEMARX.M1;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import CINEMARX.Common.Boleto;
import CINEMARX.Common.OrderDetails;
import CINEMARX.Common.Producto;
import CINEMARX.M5.Cliente;
import CINEMARX.M5.ConexionBD;
import CINEMARX.M5.ReciboViewer;
import CINEMARX.M4.EditarBoleto.PurchaseSummaryDialog;

public class VerHistorial extends JPanel {

    private JPanel mainPanel;
    private int idCliente;
    private Cliente cliente;

    public VerHistorial(int idCliente) {
        this.idCliente = idCliente;
        this.cliente = Cliente.obtenerClientePorId(idCliente); // Fetch client details

        setLayout(new BorderLayout());
        setBackground(new Color(26, 26, 26));

        // Title
        JLabel lblTitle = new JLabel("Historial de Compras", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 28));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBorder(new EmptyBorder(20, 0, 20, 0));
        add(lblTitle, BorderLayout.NORTH);

        // Main content panel
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(26, 26, 26));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(new Color(26, 26, 26));
        
        add(scrollPane, BorderLayout.CENTER);

        if (this.cliente != null) {
            loadPurchaseHistory();
        } else {
            // Handle case where client is not found
            JLabel lblError = new JLabel("No se pudo encontrar el historial para el cliente.", SwingConstants.CENTER);
            lblError.setFont(new Font("Arial", Font.BOLD, 18));
            lblError.setForeground(Color.RED);
            mainPanel.add(lblError);
        }
    }

    private void loadPurchaseHistory() {
        new SwingWorker<List<PurchaseInfo>, Void>() {
            @Override
            protected List<PurchaseInfo> doInBackground() throws Exception {
                List<PurchaseInfo> purchases = new ArrayList<>();
                String sql = "SELECT c.ID_Comprobante, c.NumComprobante, c.FechaCompra, c.MetodoPago, " +
                             "((SELECT IFNULL(SUM(f.Precio), 0) FROM Comprobante_Boleto cb JOIN Boleto b ON cb.ID_Boleto = b.ID_Boleto JOIN Funcion f ON b.ID_Funcion = f.ID_Funcion WHERE cb.ID_Comprobante = c.ID_Comprobante) + " +
                             " (SELECT IFNULL(SUM(p.Precio * cp.Cantidad), 0) FROM Comprobante_Producto cp JOIN Producto p ON cp.ID_Prod = p.ID_Prod WHERE cp.ID_Comprobante = c.ID_Comprobante AND p.ID_Prod != 20)) AS TotalComprobante " +
                             "FROM Comprobante c " +
                             "WHERE c.ID_Cliente = ? " +
                             "ORDER BY c.FechaCompra DESC";

                Connection conn = ConexionBD.obtenerConexion();
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    
                    pstmt.setInt(1, idCliente);
                    ResultSet rs = pstmt.executeQuery();

                    while (rs.next()) {
                        Timestamp ts = rs.getTimestamp("FechaCompra");
                        String formattedDate = new java.text.SimpleDateFormat("dd/MM/yyyy").format(ts);
                        purchases.add(new PurchaseInfo(
                            rs.getInt("ID_Comprobante"),
                            rs.getString("NumComprobante"),
                            formattedDate,
                            rs.getDouble("TotalComprobante"),
                            rs.getString("MetodoPago")
                        ));
                    }
                }
                return purchases;
            }

            @Override
            protected void done() {
                try {
                    List<PurchaseInfo> purchases = get();
                    mainPanel.removeAll();
                    if (purchases.isEmpty()) {
                        mainPanel.setLayout(new GridBagLayout()); // Use GridBagLayout to center
                        JLabel lblEmpty = new JLabel("No hay compras en tu historial.", SwingConstants.CENTER);
                        lblEmpty.setFont(new Font("Arial", Font.BOLD, 18));
                        lblEmpty.setForeground(Color.WHITE);
                        mainPanel.add(lblEmpty);
                    } else {
                        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS)); // Restore layout
                        for (PurchaseInfo purchase : purchases) {
                            addPurchaseItem(mainPanel, purchase);
                        }
                    }
                    mainPanel.revalidate();
                    mainPanel.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void addPurchaseItem(JPanel panel, PurchaseInfo purchase) {
        RoundedPanel ticket = new RoundedPanel(15, new Color(0x2B2B2B));
        ticket.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 10)); // Increased horizontal gap
        ticket.setMaximumSize(new Dimension(900, 80));
        ticket.setAlignmentX(Component.CENTER_ALIGNMENT);
        ticket.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel dateLabel = new JLabel(purchase.date);
        dateLabel.setForeground(Color.WHITE);
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        ticket.add(dateLabel);

        JLabel eventInfoLabel = new JLabel(purchase.numComprobante);
        eventInfoLabel.setForeground(new Color(224, 224, 224));
        eventInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        eventInfoLabel.setPreferredSize(new Dimension(300, 30)); // Give it some space
        ticket.add(eventInfoLabel);

        JLabel priceLabel = new JLabel(String.format(java.util.Locale.US, "$%.2f", purchase.price));
        priceLabel.setForeground(Color.WHITE);
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        ticket.add(priceLabel);

        // --- Main click action to edit tickets ---
        ticket.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                PurchaseSummaryDialog dialog = new PurchaseSummaryDialog(
                    (Frame) SwingUtilities.getWindowAncestor(panel),
                    purchase.idComprobante
                );
                dialog.setVisible(true);
                loadPurchaseHistory();
            }
        });

        panel.add(ticket);
        panel.add(Box.createVerticalStrut(10));
    }

    private static class PurchaseInfo {
        int idComprobante;
        String numComprobante;
        String date;
        double price;
        String metodoPago;

        PurchaseInfo(int id, String num, String d, double p, String mp) {
            idComprobante = id;
            numComprobante = num;
            date = d;
            price = p;
            metodoPago = mp;
        }
    }

    // Custom panel with rounded corners
    private static class RoundedPanel extends JPanel {
        private int cornerRadius;
        private Color backgroundColor;

        public RoundedPanel(int radius, Color bgColor) {
            super();
            this.cornerRadius = radius;
            this.backgroundColor = bgColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Dimension arcs = new Dimension(cornerRadius, cornerRadius);
            int width = getWidth();
            int height = getHeight();
            Graphics2D graphics = (Graphics2D) g;
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draws the rounded panel with borders.
            graphics.setColor(backgroundColor);
            graphics.fillRoundRect(0, 0, width-1, height-1, arcs.width, arcs.height);
            graphics.setColor(getForeground());
            // graphics.drawRoundRect(0, 0, width-1, height-1, arcs.width, arcs.height); // Optional border
        }
    }

    // Custom button with rounded corners
    private static class RoundedButton extends JButton {
        public RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Paint background
            if (getModel().isArmed()) {
                g2.setColor(getBackground().darker());
            } else {
                g2.setColor(getBackground());
            }
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

            // Paint text
            g2.setColor(getForeground());
            FontMetrics metrics = g2.getFontMetrics(getFont());
            int x = (getWidth() - metrics.stringWidth(getText())) / 2;
            int y = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent();
            g2.drawString(getText(), x, y);
            
            g2.dispose();
        }
    }
}