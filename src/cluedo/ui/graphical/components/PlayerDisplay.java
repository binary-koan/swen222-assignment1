package cluedo.ui.graphical.components;

import cluedo.game.Player;
import cluedo.ui.graphical.controls.GridPanel;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Panel displaying information about the current player
 */
public class PlayerDisplay extends GridPanel implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals("movesRemaining")) {
            setRemainingMoves((int) e.getNewValue());
        }
    }

    public void unsetPlayer(String message) {
        currentPlayer.removePropertyChangeListener(this);

        currentPlayer = null;
        nameLabel.setText(message);
        movesRemainingLabel.setText("");
        tokenDisplay.repaint();
    }

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
    private JLabel movesRemainingLabel = new JLabel();

    public PlayerDisplay() {
        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, getFont().getSize()));

        setup(tokenDisplay).pad(5).center().addToLayout();
        setup(nameLabel).pad(5).flexH().addToLayout();
        setup(movesRemainingLabel).pad(5).addToLayout();
    }

    public void startTurn(Player player) {
        if (currentPlayer != null) {
            currentPlayer.removePropertyChangeListener(this);
        }
        currentPlayer = player;
        player.addPropertyChangeListener(this);

        tokenDisplay.setToolTipText(player.getToken().getName());
        nameLabel.setText(player.getName() + "'s turn");
        setRemainingMoves(player.getMovesRemaining());
        repaint();
    }

    private void setRemainingMoves(int moves) {
        movesRemainingLabel.setText("You have " + moves + " moves remaining. Click on the board to move.");
    }
}
