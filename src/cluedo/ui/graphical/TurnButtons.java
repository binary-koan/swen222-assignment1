package cluedo.ui.graphical;

import cluedo.game.Game;
import cluedo.game.Player;
import cluedo.game.Solution;
import cluedo.ui.graphical.controls.GridPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class TurnButtons extends GridPanel implements ActionListener {
    private List<ActionListener> actionListeners = new ArrayList<>();
    private JButton suggestButton;
    private JButton accuseButton;
    private JButton showHandButton;
    private JButton endTurnButton;

    private Game game;
    private Player currentPlayer;

    public TurnButtons(Game game) {
        this.game = game;

        suggestButton = addButton("Suggest", "player.suggest");
        accuseButton = addButton("Accuse", "player.accuse");
        showHandButton = addButton("Show hand", "player.showHand");
        endTurnButton = addButton("End turn", "player.endTurn");
    }

    public void startTurn(Player player) {
        currentPlayer = player;
        if (player == null) {
            suggestButton.setEnabled(false);
            accuseButton.setEnabled(false);
            showHandButton.setEnabled(false);
            endTurnButton.setEnabled(false);
        }
        else {
            suggestButton.setEnabled(player.getRoom() != null);
            accuseButton.setEnabled(true);
            showHandButton.setEnabled(true);
            endTurnButton.setEnabled(true);
        }
    }

    public void addActionListener(ActionListener listener) {
        actionListeners.add(listener);
    }

    private JButton addButton(String text, String actionCommand) {
        JButton button = new JButton(text);
        button.setActionCommand(actionCommand);
        button.addActionListener(this);
        setup(button).pad(5).addToLayout();
        return button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "player.suggest":
                suggest();
                break;
            case "player.accuse":
                accuse();
                break;
            case "player.showHand":
                Dialogs.showHand(this, currentPlayer);
                break;
            case "player.endTurn":
                emitAction("turn.next");
                break;
            default:
                throw new RuntimeException("Unknown action: " + e.getActionCommand());
        }
    }

    private void accuse() {
        Game.Suggestion accusation = Dialogs.getAccusation(this, currentPlayer, game.getData());
        Solution solution = game.getSolution();
        if (solution.checkAgainst(accusation)) {
            emitAction("turn.win");
            JOptionPane.showMessageDialog(this, "Your accusation is correct. You win!");
        }
        else {
            emitAction("turn.lose");
            currentPlayer.setInGame(false);
            JOptionPane.showMessageDialog(this, "Your accusation was wrong - sorry, you're out of the game.");

            List<Player> playersLeft = new ArrayList<>();
            for (Player p : game.getPlayers()) {
                if (p.isInGame()) {
                    playersLeft.add(p);
                }
            }
            if (playersLeft.size() == 1) {
                emitAction("turn.winByDefault");
                JOptionPane.showMessageDialog(
                        this, "<html><p><b>" + playersLeft.get(0).getName() + "</b> won by default - " +
                                "all other players have been eliminated.</p><p>The solution was:</p><p><b>" +
                                solution.getSuspect() + "</b> in the <b>" + solution.getRoom() + "</b> with the <b>" +
                                solution.getWeapon() + "</b>.</p>"
                );
            }
            else {
                emitAction("turn.next");
            }
        }
    }

    private void suggest() {
        Game.Suggestion suggestion = Dialogs.getSuggestion(this, currentPlayer, game.getData());
        if (suggestion == null) {
            return;
        }

        Game.Disprover disprover = game.disproveSuggestion(currentPlayer, suggestion);
        if (disprover == null) {
            JOptionPane.showMessageDialog(
                    this, "Your suggestion could not be disproved.", "Suggestion", JOptionPane.INFORMATION_MESSAGE
            );
        }
        else {
            JOptionPane.showMessageDialog(
                    this, "<html>Your suggestion was disproved by <b>" + disprover.getPlayer().getName() + "</b>." +
                            "<br />They are holding <b>" + disprover.getCard().getName() + "</b></html>",
                    "Suggestion disproved", JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private void emitAction(String command) {
        ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, command);
        for (ActionListener listener : actionListeners) {
            listener.actionPerformed(e);
        }
    }
}
