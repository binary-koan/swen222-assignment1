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
    /**
     * Component displaying the current player's token (as a coloured circle)
     */
    private class TokenDisplay extends JPanel {
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(32, 32);
        }

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
    }

    private Player currentPlayer;

    private TokenDisplay tokenDisplay = new TokenDisplay();
    private JLabel nameLabel = new JLabel("");
    private JLabel movesRemainingLabel = new JLabel();

    /**
     * Create a new player display
     */
    public PlayerDisplay() {
        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, getFont().getSize()));

        setup(tokenDisplay).pad(5).center().addToLayout();
        setup(nameLabel).pad(5).flexH().addToLayout();
        setup(movesRemainingLabel).pad(5).addToLayout();
    }

    /**
     * Sets the player currently displayed on this component
     *
     * @param player new current player
     */
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

    /**
     * Stops this component from displaying the current player and starts showing the given message instead
     *
     * @param message message to display
     */
    public void unsetPlayer(String message) {
        currentPlayer.removePropertyChangeListener(this);

        currentPlayer = null;
        nameLabel.setText(message);
        movesRemainingLabel.setText("");
        tokenDisplay.repaint();
    }

    /**
     * Updates the remaining moves ticker with a "movesRemaining" property change
     */
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals("movesRemaining")) {
            setRemainingMoves((int) e.getNewValue());
        }
    }

    /**
     * Sets the "moves remaining" label to display the given number of remaining moves
     */
    private void setRemainingMoves(int moves) {
        movesRemainingLabel.setText("You have " + moves + " moves remaining. Click on the board to move.");
    }
}
