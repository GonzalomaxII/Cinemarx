package CINEMARX.M4;

import javax.swing.*;
import java.awt.*;

/**
 * Panel placeholder para el reproductor de video
 * Nota: JavaFX no está completamente funcional en este contexto,
 * por lo que mostramos un panel simple
 */
public class VideoPlayerPanel extends JPanel {
    
    private String videoUrl;
    private JLabel lblPlaceholder;
    
    public VideoPlayerPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(20, 20, 20));
        setPreferredSize(new Dimension(350, 200));
        
        lblPlaceholder = new JLabel("▶ Trailer", SwingConstants.CENTER);
        lblPlaceholder.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblPlaceholder.setForeground(new Color(160, 160, 160));
        
        add(lblPlaceholder, BorderLayout.CENTER);
    }
    
    public void loadVideo(String url) {
        this.videoUrl = url;
        if (url != null && !url.isEmpty()) {
            lblPlaceholder.setText("▶ Ver trailer");
            lblPlaceholder.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Agregar listener para abrir el trailer en el navegador
            lblPlaceholder.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    try {
                        Desktop.getDesktop().browse(new java.net.URI(url));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    lblPlaceholder.setForeground(new Color(220, 20, 60));
                }
                
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    lblPlaceholder.setForeground(new Color(160, 160, 160));
                }
            });
        } else {
            lblPlaceholder.setText("Sin trailer disponible");
            lblPlaceholder.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }
}