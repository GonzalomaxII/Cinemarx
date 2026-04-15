package CINEMARX.M4.EditarBoleto;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class HistorialComprasExample extends JFrame {

    private JPanel mainPanel;

    public HistorialComprasExample() {
        setTitle("Historial de Compras");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Panel principal
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(26, 26, 26));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(scrollPane);

        loadPurchaseHistory();
    }

    private void loadPurchaseHistory() {
        new SwingWorker<List<PurchaseInfo>, Void>() {
            @Override
            protected List<PurchaseInfo> doInBackground() throws Exception {
                List<PurchaseInfo> purchases = new ArrayList<>();
                String sql = "SELECT c.ID_Comprobante, c.FechaCompra, " +
                             "GROUP_CONCAT(p.Titulo SEPARATOR ', ') as Peliculas, " +
                             "SUM(f.Precio) as Total " +
                             "FROM Comprobante c " +
                             "JOIN Comprobante_Boleto cb ON c.ID_Comprobante = cb.ID_Comprobante " +
                             "JOIN Boleto b ON cb.ID_Boleto = b.ID_Boleto " +
                             "JOIN Funcion f ON b.ID_Funcion = f.ID_Funcion " +
                             "JOIN Pelicula p ON f.ID_Pelicula = p.ID_Pelicula " +
                             "GROUP BY c.ID_Comprobante, c.FechaCompra " +
                             "ORDER BY c.FechaCompra DESC";

                try (Connection conn = CINEMARX.M4.M4.getConexion();
                     PreparedStatement pstmt = conn.prepareStatement(sql);
                     ResultSet rs = pstmt.executeQuery()) {

                    while (rs.next()) {
                        Timestamp ts = rs.getTimestamp("FechaCompra");
                        String formattedDate = new java.text.SimpleDateFormat("dd/MM/yyyy").format(ts);
                        purchases.add(new PurchaseInfo(
                            rs.getInt("ID_Comprobante"),
                            formattedDate,
                            rs.getString("Peliculas"),
                            rs.getDouble("Total")
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
                    for (PurchaseInfo purchase : purchases) {
                        addPurchaseItem(mainPanel, purchase.idComprobante, purchase.date, purchase.movieInfo, String.format(java.util.Locale.US, "$%.2f", purchase.price));
                    }
                    mainPanel.revalidate();
                    mainPanel.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void addPurchaseItem(JPanel panel, int idComprobante, String date, String movieInfo, String price) {
        JPanel ticket = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        ticket.setOpaque(false);
        ticket.setBackground(new Color(45, 45, 45));
        ticket.setBorder(new EmptyBorder(16, 24, 16, 24));
        ticket.setMaximumSize(new Dimension(900, 100));
        ticket.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 10, 0, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel dateLabel = new JLabel(date);
        dateLabel.setForeground(Color.WHITE);
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.weightx = 0;
        ticket.add(dateLabel, gbc);

        JLabel eventInfoLabel = new JLabel(movieInfo);
        eventInfoLabel.setForeground(new Color(224, 224, 224));
        eventInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        gbc.gridx = 1;
        gbc.weightx = 1;
        ticket.add(eventInfoLabel, gbc);

        JLabel priceLabel = new JLabel(price);
        priceLabel.setForeground(Color.WHITE);
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        ticket.add(priceLabel, gbc);

        // Hover effect
        ticket.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                ticket.setBackground(new Color(53, 53, 53));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ticket.setBackground(new Color(45, 45, 45));
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                PurchaseSummaryDialog dialog = new PurchaseSummaryDialog(
                    (Frame) SwingUtilities.getWindowAncestor(panel),
                    idComprobante
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
        String date;
        String movieInfo;
        double price;

        PurchaseInfo(int id, String d, String m, double p) {
            idComprobante = id;
            date = d;
            movieInfo = m;
            price = p;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new HistorialComprasExample().setVisible(true);
        });
    }
}