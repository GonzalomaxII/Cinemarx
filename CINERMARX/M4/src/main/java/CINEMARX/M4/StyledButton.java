package CINEMARX.M4;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class StyledButton extends JButton {

    public enum ButtonStyle {
        GRADIENT,
        TOGGLE
    }

    private ButtonStyle style;
    private boolean selected;

    public StyledButton(String text, ButtonStyle style) {
        super(text);
        this.style = style;
        this.selected = false;
        setDefaults();
    }

    private void setDefaults() {
        setBorderPainted(false);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setForeground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (style == ButtonStyle.GRADIENT) {
            g2.setColor(new Color(255, 71, 87));
        } else if (style == ButtonStyle.TOGGLE) {
            if (selected) {
                g2.setColor(new Color(255, 71, 87));
            } else {
                g2.setColor(new Color(45, 45, 45));
            }
        }

        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
        g2.dispose();

        super.paintComponent(g);
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;
        repaint();
    }
}
