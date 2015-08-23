package cluedo.ui.graphical.components;

import cluedo.game.Game;
import cluedo.game.GameData;
import cluedo.game.Player;
import cluedo.game.Solution;
import cluedo.game.objects.Card;
import cluedo.game.objects.Room;
import cluedo.game.objects.Suspect;
import cluedo.game.objects.Weapon;
import cluedo.ui.graphical.controls.GridPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
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
                showHand(this, currentPlayer);
                break;
            case "player.endTurn":
                emitAction("turn.next");
                break;
            default:
                throw new RuntimeException("Unknown action: " + e.getActionCommand());
        }
    }

    private void accuse() {
        Game.Suggestion accusation = getAccusation(this, game.getData());
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
        Game.Suggestion suggestion = getSuggestion(this, currentPlayer, game.getData());
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

    private void showHand(Component parent, Player player) {
        String html = "<html><p>You are holding</p><ul>";
        for (Card card : player.getHand()) {
            html += "<li><b>" + card.getName() + "</b></li>";
        }
        html += "</ul></html>";
        JLabel label = new JLabel(html);

        JOptionPane.showMessageDialog(parent, label, player.getName() + "'s hand", JOptionPane.PLAIN_MESSAGE);
    }

    private Game.Suggestion getSuggestion(Component parent, Player player, GameData data) {
        if (player.getRoom() == null) {
            JOptionPane.showMessageDialog(parent, "You can only make a suggestion when in a room!", "Unable to suggest",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }

        GridPanel contentPane = new GridPanel();
        contentPane.setup(new JLabel(
                "<html>You suggest that the murder was committed in the <b>" + player.getRoom().getName() + "</b>"
        )).spanH(2).pad(5).addToLayout();
        contentPane.finishRow();

        ButtonGroup[] buttonGroups = addDataChoices(contentPane, data, false);

        int result = JOptionPane.showOptionDialog(parent, contentPane, "Suggestion", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, new Object[] { "OK", "Cancel" }, "OK");
        if (result == JOptionPane.OK_OPTION) {
            Suspect suspect = data.getSuspect(getSelectedButtonText(buttonGroups[0]));
            Weapon weapon = data.getWeapon(getSelectedButtonText(buttonGroups[1]));
            return new Game.Suggestion(suspect, weapon, player.getRoom());
        }
        else {
            return null;
        }
    }

    private Game.Suggestion getAccusation(Component parent, GameData data) {
        GridPanel contentPane = new GridPanel();
        contentPane.setup(new JLabel(
                "You are about to make an accusation. Remember, if it's wrong you will be out of the game!"
        )).spanH(3).pad(5).addToLayout();
        contentPane.finishRow();

        ButtonGroup[] buttonGroups = addDataChoices(contentPane, data, true);

        int result = JOptionPane.showOptionDialog(parent, contentPane, "Accusation", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, new Object[]{"OK", "Cancel"}, "OK");
        if (result == JOptionPane.OK_OPTION) {
            Room room = data.getRoom(getSelectedButtonText(buttonGroups[0]));
            Suspect suspect = data.getSuspect(getSelectedButtonText(buttonGroups[1]));
            Weapon weapon = data.getWeapon(getSelectedButtonText(buttonGroups[2]));
            return new Game.Suggestion(suspect, weapon, room);
        }
        else {
            return null;
        }
    }

    private ButtonGroup[] addDataChoices(GridPanel panel, GameData data, boolean includeRooms) {
        ButtonGroup suspectButtons = buildButtonGroup(panel, "Murderer", data.getSuspects());
        ButtonGroup weaponButtons = buildButtonGroup(panel, "Murder weapon", data.getWeapons());
        if (includeRooms) {
            ButtonGroup roomButtons = buildButtonGroup(panel, "Murder location", data.getRooms());
            return new ButtonGroup[] { suspectButtons, weaponButtons, roomButtons };
        }
        else {
            return new ButtonGroup[] { suspectButtons, weaponButtons };
        }
    }

    private ButtonGroup buildButtonGroup(GridPanel panel, String title, List<? extends Card> cards) {
        GridPanel optionsPanel = new GridPanel();
        optionsPanel.setup(new JLabel(title, SwingConstants.LEFT)).pad(5).addToLayout();
        optionsPanel.finishRow();

        ButtonGroup buttonGroup = new ButtonGroup();
        for (Card card : cards) {
            JRadioButton radioButton = new JRadioButton(card.getName());
            buttonGroup.add(radioButton);
            optionsPanel.setup(radioButton).pad(5).addToLayout();
            optionsPanel.finishRow();
        }

        panel.setup(optionsPanel).addToLayout();
        return buttonGroup;
    }

    private static String getSelectedButtonText(ButtonGroup buttonGroup) {
        for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();
            if (button.isSelected()) {
                return button.getText();
            }
        }
        return null;
    }
}
