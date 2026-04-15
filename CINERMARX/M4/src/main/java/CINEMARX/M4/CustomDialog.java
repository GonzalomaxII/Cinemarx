
package CINEMARX.M4;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JComponent;

public class CustomDialog extends JDialog {

    public enum DialogType {
        INFO, CONFIRMATION
    }

    private int result = JOptionPane.CLOSED_OPTION;

    public CustomDialog(Frame owner, String message) {
        this(owner, message, DialogType.INFO);
    }

    public CustomDialog(Frame owner, String message, DialogType type) {
        super(owner, true);
        init(owner, message, type);
    }

    public CustomDialog(Dialog owner, String message) {
        this(owner, message, DialogType.INFO);
    }

    public CustomDialog(Dialog owner, String message, DialogType type) {
        super(owner, true);
        init(owner, message, type);
    }
    
    private void init(Window owner, String message, DialogType type) {
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setSize(400, 200);
        setLocationRelativeTo(owner);

        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 30, 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setOpaque(false);
        setContentPane(panel);

        JLabel messageLabel = new JLabel("<html><div style='text-align: center;'>" + message + "</div></html>");
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 16));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(messageLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setOpaque(false);

        if (type == DialogType.INFO) {
            JButton okButton = createStyledButton("ENTENDIDO", new Color(220, 50, 50));
            okButton.addActionListener(e -> {
                result = JOptionPane.OK_OPTION;
                dispose();
            });
            buttonPanel.add(okButton);
        } else if (type == DialogType.CONFIRMATION) {
            JButton yesButton = createStyledButton("SI", new Color(50, 200, 50));
            yesButton.addActionListener(e -> {
                result = JOptionPane.YES_OPTION;
                dispose();
            });
            buttonPanel.add(yesButton);

            JButton noButton = createStyledButton("NO", new Color(220, 50, 50));
            noButton.addActionListener(e -> {
                result = JOptionPane.NO_OPTION;
                dispose();
            });
            buttonPanel.add(noButton);
        }
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Make dialog draggable
        Point initialClick = new Point();
        panel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                initialClick.setLocation(e.getPoint());
            }
        });

        panel.addMouseMotionListener(new MouseAdapter() {
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
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void installUI(JComponent c) {
                super.installUI(c);
                AbstractButton button = (AbstractButton) c;
                button.setOpaque(false);
            }

            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                AbstractButton button = (AbstractButton) c;
                ButtonModel model = button.getModel();

                if (model.isPressed()) {
                    g2.setColor(bgColor.darker().darker());
                } else if (model.isRollover()) {
                    g2.setColor(bgColor.darker());
                } else {
                    g2.setColor(button.getBackground());
                }
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 15, 15); // Rounded corners

                g2.dispose();
                super.paint(g, c);
            }
        });
        return button;
    }

    public int getResult() {
        return result;
    }
}
