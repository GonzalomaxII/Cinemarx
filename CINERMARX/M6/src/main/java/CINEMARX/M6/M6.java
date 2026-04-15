package CINEMARX.M6;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

/**
 * Clase de prueba standalone para M6Panel
 * Solo para testing independiente del módulo
 */
public class M6 extends JFrame {
    
    private M6Panel panel;
    
    public M6(int dniUsuario) {
        setTitle("Panel de Gestión - Modo Prueba");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        
        // Crear el panel M6 con el DNI proporcionado
        panel = new M6Panel(dniUsuario);
        
        add(panel, BorderLayout.CENTER);
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (panel != null) {
                    panel.closeDatabaseConnection();
                }
            }
        });
    }
    
    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            // DNI de prueba para testing
            M6 frame = new M6(12345678);
            frame.setVisible(true);
        });
    }
}