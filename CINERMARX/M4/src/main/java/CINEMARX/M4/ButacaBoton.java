package CINEMARX.M4;

import javax.swing.*;
import java.awt.*;

public class ButacaBoton extends JToggleButton {
    private boolean ocupada = false;
    private String seatId;

    public ButacaBoton(String seatId) {
        super();
        this.seatId = seatId;
        setPreferredSize(new Dimension(18, 18));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public String getSeatId() {
        return seatId;
    }

    public void setOcupada(boolean ocupada) {
        this.ocupada = ocupada;
        setEnabled(!ocupada);
        repaint();
    }

    public boolean isOcupada() {
        return ocupada;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (ocupada) {
            g2.setColor(new Color(100, 100, 100));
        } else if (isSelected()) {
            g2.setColor(new Color(220, 50, 50));
        } else {
            g2.setColor(Color.WHITE);
        }
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

        if (isSelected() && !ocupada) {
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
