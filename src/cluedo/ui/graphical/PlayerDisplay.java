package cluedo.ui.graphical;

import cluedo.game.Player;
import cluedo.ui.graphical.controls.GridPanel;

import javax.swing.*;
import java.awt.*;

public class PlayerDisplay extends GridPanel {
    private class TokenDisplay extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            if (PlayerDisplay.this.currentPlayer == null) {
                return;
            }

            if (g instanceof Graphics2D) {
                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }

            g.setColor(Color.BLACK);
            g.fillOval(0, 0, getWidth(), getHeight());
            g.setColor(PlayerDisplay.this.currentPlayer.getToken().getColor());
            g.fillOval(2, 2, getWidth() - 4, getHeight() - 4);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(32, 32);
        }
    }

    private Player currentPlayer;

    private TokenDisplay tokenDisplay = new TokenDisplay();
    private JLabel nameLabel = new JLabel("No players");
    private JLabel dieRollLabel = new JLabel();

    public PlayerDisplay() {
        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, getFont().getSize()));

        setup(tokenDisplay).pad(5).center().addToLayout();
        setup(nameLabel).pad(5).flexH().addToLayout();
        setup(dieRollLabel).pad(5).addToLayout();
    }

    public void setPlayer(Player player) {
        currentPlayer = player;
        tokenDisplay.setToolTipText(player.getToken().getName());
        nameLabel.setText(player.getName() + "'s turn");
        dieRollLabel.setText("You rolled a " + player.getDieRoll() + ". Click on the board to move.");
        repaint();
    }
}
