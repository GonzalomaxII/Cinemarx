package CINEMARX.M4;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PantallaPeliculaPlaceholder extends JPanel {

    public PantallaPeliculaPlaceholder() {
        setLayout(new BorderLayout());
        setBackground(new Color(0x2B2B2B));
        inicializarUI();
    }

    private void inicializarUI() {
        // Main scrollable content container
        JPanel mainScrollableContent = new JPanel();
        mainScrollableContent.setLayout(new BoxLayout(mainScrollableContent, BoxLayout.Y_AXIS));
        mainScrollableContent.setBackground(new Color(0x2B2B2B));
        mainScrollableContent.setBorder(new EmptyBorder(20, 40, 20, 40));

        // Two-column section
        JPanel twoColumnPanel = new JPanel(new BorderLayout(20, 20));
        twoColumnPanel.setBackground(new Color(0x2B2B2B));
        twoColumnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 800));

        JPanel leftPanel = crearPanelIzquierdo();
        twoColumnPanel.add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = crearPanelDerecho();
        twoColumnPanel.add(rightPanel, BorderLayout.CENTER);

        mainScrollableContent.add(twoColumnPanel);
        mainScrollableContent.add(Box.createRigidArea(new Dimension(0, 30)));

        // Separator
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(new Color(70, 70, 70));
        separator.setBackground(new Color(50, 50, 50));
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        mainScrollableContent.add(separator);
        mainScrollableContent.add(Box.createRigidArea(new Dimension(0, 30)));

        // "Otras Peliculas" Section
        JPanel otrasPeliculasSection = crearPanelOtrasPeliculas();
        mainScrollableContent.add(otrasPeliculasSection);

        // Wrap in JScrollPane
        JScrollPane masterScrollPane = new JScrollPane(mainScrollableContent);
        masterScrollPane.setBorder(null);
        masterScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        masterScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        personalizarScrollBar(masterScrollPane.getVerticalScrollBar());

        add(masterScrollPane, BorderLayout.CENTER);
    }

    private JPanel crearPanelIzquierdo() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(0x2B2B2B));
        panel.setPreferredSize(new Dimension(300, 0));
        panel.setMaximumSize(new Dimension(300, Integer.MAX_VALUE));

        JPanel posterPlaceholder = new JPanel();
        posterPlaceholder.setPreferredSize(new Dimension(300, 450));
        posterPlaceholder.setMaximumSize(new Dimension(300, 450));
        posterPlaceholder.setMinimumSize(new Dimension(300, 450));
        posterPlaceholder.setBackground(new Color(50, 50, 50));
        posterPlaceholder.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(posterPlaceholder);

        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel sinopsisPlaceholder = new JPanel();
        sinopsisPlaceholder.setBackground(new Color(50, 50, 50));
        sinopsisPlaceholder.setPreferredSize(new Dimension(300, 150));
        sinopsisPlaceholder.setMaximumSize(new Dimension(300, 150));
        sinopsisPlaceholder.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(sinopsisPlaceholder);

        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel duracionPlaceholder = new JPanel();
        duracionPlaceholder.setBackground(new Color(50, 50, 50));
        duracionPlaceholder.setPreferredSize(new Dimension(100, 20));
        duracionPlaceholder.setMaximumSize(new Dimension(100, 20));
        duracionPlaceholder.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(duracionPlaceholder);

        return panel;
    }

    private JPanel crearPanelDerecho() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(0x2B2B2B));

        JPanel tituloPlaceholder = new JPanel();
        tituloPlaceholder.setBackground(new Color(50, 50, 50));
        tituloPlaceholder.setPreferredSize(new Dimension(400, 40));
        tituloPlaceholder.setMaximumSize(new Dimension(400, 40));
        tituloPlaceholder.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(tituloPlaceholder);

        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        JPanel fechasPlaceholder = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        fechasPlaceholder.setBackground(new Color(0x2B2B2B));
        fechasPlaceholder.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (int i = 0; i < 4; i++) {
            JPanel fecha = new JPanel();
            fecha.setBackground(new Color(50, 50, 50));
            fecha.setPreferredSize(new Dimension(80, 40));
            fechasPlaceholder.add(fecha);
        }
        panel.add(fechasPlaceholder);

        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel filtrosPlaceholder = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        filtrosPlaceholder.setBackground(new Color(0x2B2B2B));
        filtrosPlaceholder.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (int i = 0; i < 2; i++) {
            JPanel filtro = new JPanel();
            filtro.setBackground(new Color(50, 50, 50));
            filtro.setPreferredSize(new Dimension(200, 40));
            filtrosPlaceholder.add(filtro);
        }
        panel.add(filtrosPlaceholder);

        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        JPanel horariosPlaceholder = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        horariosPlaceholder.setBackground(new Color(0x2B2B2B));
        horariosPlaceholder.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (int i = 0; i < 5; i++) {
            JPanel horario = new JPanel();
            horario.setBackground(new Color(50, 50, 50));
            horario.setPreferredSize(new Dimension(90, 45));
            horariosPlaceholder.add(horario);
        }
        panel.add(horariosPlaceholder);

        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        JPanel botonPlaceholder = new JPanel();
        botonPlaceholder.setBackground(new Color(50, 50, 50));
        botonPlaceholder.setPreferredSize(new Dimension(400, 50));
        botonPlaceholder.setMaximumSize(new Dimension(400, 50));
        botonPlaceholder.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(botonPlaceholder);

        return panel;
    }

    private JPanel crearPanelOtrasPeliculas() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0x2B2B2B));
        panel.setBorder(new EmptyBorder(15, 0, 15, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));

        JPanel titulo = new JPanel();
        titulo.setBackground(new Color(50, 50, 50));
        titulo.setPreferredSize(new Dimension(200, 20));
        titulo.setMaximumSize(new Dimension(200, 20));
        panel.add(titulo, BorderLayout.NORTH);

        JPanel panelTarjetas = new JPanel();
        panelTarjetas.setLayout(new BoxLayout(panelTarjetas, BoxLayout.X_AXIS));
        panelTarjetas.setBackground(new Color(0x2B2B2B));

        for (int i = 0; i < 10; i++) {
            JPanel card = new JPanel();
            card.setBackground(new Color(50, 50, 50));
            card.setPreferredSize(new Dimension(180, 320));
            card.setMinimumSize(new Dimension(180, 320));
            card.setMaximumSize(new Dimension(180, 320));
            panelTarjetas.add(card);
            panelTarjetas.add(Box.createRigidArea(new Dimension(15, 0)));
        }

        JScrollPane scrollPane = new JScrollPane(panelTarjetas);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(20);
        scrollPane.setPreferredSize(new Dimension(0, 350));
        personalizarScrollBarOculto(scrollPane.getHorizontalScrollBar());

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void personalizarScrollBar(JScrollBar scrollBar) {
        scrollBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(60, 60, 60);
                this.trackColor = new Color(0x2B2B2B);
            }
            
            @Override
            protected JButton createDecreaseButton(int orientation) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                return button;
            }
            
            @Override
            protected JButton createIncreaseButton(int orientation) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                return button;
            }
            
            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 10, 10);
                g2.dispose();
            }
            
            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(trackColor);
                g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
                g2.dispose();
            }
        });
    }

    private void personalizarScrollBarOculto(JScrollBar scrollBar) {
        scrollBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(0x2B2B2B);
                this.trackColor = new Color(0x2B2B2B);
            }
            
            @Override
            protected JButton createDecreaseButton(int orientation) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                return button;
            }
            
            @Override
            protected JButton createIncreaseButton(int orientation) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                return button;
            }
            
            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 10, 10);
                g2.dispose();
            }
            
            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(trackColor);
                g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
                g2.dispose();
            }
        });
    }
}
