package CINEMARX.M4;

import javax.swing.*;
import java.awt.*;
import java.net.URI;

public class VideoPlayerDialog {

    public static void openVideo(Frame parent, String videoUrl) {
        try {
            // Intentar abrir en el navegador predeterminado
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(videoUrl));
                
                // Mostrar confirmación
                CustomDialog dialog = new CustomDialog(parent, "Trailer abierto en tu navegador.");
                dialog.setVisible(true);
            } else {
                // Si no se puede abrir automáticamente, mostrar la URL
                mostrarURLManual(parent, videoUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarURLManual(parent, videoUrl);
        }
    }
    
    private static void mostrarURLManual(Frame parent, String videoUrl) {
        JTextArea textArea = new JTextArea(3, 50);
        textArea.setText(videoUrl);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        
        JOptionPane.showMessageDialog(parent,
            new Object[] {
                "No se pudo abrir el navegador automáticamente.",
                "Copia esta URL para ver el tráiler:",
                new JScrollPane(textArea)
            },
            "URL del tráiler",
            JOptionPane.INFORMATION_MESSAGE);
    }
}