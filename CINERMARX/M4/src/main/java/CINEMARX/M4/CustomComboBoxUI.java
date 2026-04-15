package CINEMARX.M4;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;

public class CustomComboBoxUI extends BasicComboBoxUI {

    @Override
    protected JButton createArrowButton() {
        JButton button = new JButton("▼");
        button.setBackground(CinemarxTheme.BG_COMPONENT);
        button.setForeground(CinemarxTheme.TEXT_LIGHT);
        button.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        button.setFocusPainted(false);
        return button;
    }

    @Override
    protected ComboPopup createPopup() {
        BasicComboPopup popup = new BasicComboPopup(comboBox) {
            @Override
            protected JScrollPane createScroller() {
                JScrollPane scroller = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                scroller.setBorder(BorderFactory.createEmptyBorder());
                scroller.getVerticalScrollBar().setUI(new CustomScrollBarUI()); // Apply custom scrollbar UI
                return scroller;
            }

            @Override
            public void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CinemarxTheme.BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10); // Rounded border
                g2.dispose();
            }
        };
        popup.setBorder(BorderFactory.createEmptyBorder()); // Remove default border
        return popup;
    }

    @Override
    public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
        // Do nothing, we want a transparent background for the current value
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Paint the background of the combobox itself
        g2.setColor(CinemarxTheme.BG_COMPONENT);
        g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 10, 10); // Rounded background
        
        // Paint the border (already handled by the JComboBox's paintBorder override)
        // g2.setColor(CinemarxTheme.BORDER_COLOR);
        // g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 10, 10);

        g2.dispose();
        super.paint(g, c);
    }
}
