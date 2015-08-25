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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * A panel containing buttons the player can use to perform actions within their turn
 */
public class ActionButtons extends GridPanel implements ActionListener, PropertyChangeListener {
    private List<ActionListener> actionListeners = new ArrayList<>();

    private JButton passageButton;
    private JButton suggestButton;
    private JButton accuseButton;
    private JButton showHandButton;
    private JButton endTurnButton;

    private Game game;
    private Player currentPlayer;
    private boolean passageTaken = false;

    /**
     * Construct a new set of turn buttons
     *
     * @param game game that these buttons control
     */
    public ActionButtons(Game game) {
        setGame(game);

        passageButton = addButton("Take passage", "player.takePassage");
        suggestButton = addButton("Suggest", "player.suggest");
        accuseButton = addButton("Accuse", "player.accuse");
        showHandButton = addButton("Show hand", "player.showHand");
        endTurnButton = addButton("End turn", "player.endTurn");
    }

    /**
     * Sets the game that these buttons are connected to
     *
     * @param game game to connect to
     */
    public void setGame(Game game) {
        this.game = game;
    }

    /**
     * Starts the specified player's turn
     *
     * @param player current player, or null if no one is playing
     */
    public void startTurn(Player player) {
        if (currentPlayer != null) {
            currentPlayer.removePropertyChangeListener(this);
        }

        if (player == null) {
            passageButton.setEnabled(false);
            suggestButton.setEnabled(false);
            accuseButton.setEnabled(false);
            showHandButton.setEnabled(false);
            endTurnButton.setEnabled(false);
        }
        else {
            currentPlayer = player;
            player.addPropertyChangeListener(this);
            passageTaken = false;

            maybeEnableTakePassage(player.getRoom());
            suggestButton.setEnabled(player.getRoom() != null);
            accuseButton.setEnabled(true);
            showHandButton.setEnabled(true);
            endTurnButton.setEnabled(true);
        }
    }

    /**
     * Adds a listener to this panel's list of action listeners. The listener will be notified when:
     * - A player's turn ends ("turn.next")
     * - A player is out of the game ("turn.lose")
     * - A player wins the game ("turn.win")
     * - All except one player is knocked out ("turn.winByDefault")
     *
     * @param listener action listener to add
     */
    public void addActionListener(ActionListener listener) {
        actionListeners.add(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // Only respond to actions from the buttons on this panel
        switch (e.getActionCommand()) {
            case "player.takePassage":
                passageTaken = true;
                currentPlayer.setRoom(currentPlayer.getRoom().getPassageExit());
                break;
            case "player.suggest":
                suggest();
                break;
            case "player.accuse":
                accuse();
                break;
            case "player.showHand":
                showHand();
                break;
            case "player.endTurn":
                emitAction("turn.next");
                break;
            default:
                throw new RuntimeException("Unknown action: " + e.getActionCommand());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        // Make sure Take Passage button stays in sync with the player's room
        if (e.getSource().equals(currentPlayer) && e.getPropertyName().equals("room")) {
            Room newRoom = (Room)e.getNewValue();
            maybeEnableTakePassage(newRoom);
        }
    }

    /**
     * Adds a button to the panel
     *
     * @param text button text
     * @param actionCommand action command to emit when the button is clicked
     * @return the created button
     */
    private JButton addButton(String text, String actionCommand) {
        JButton button = new JButton(text);
        button.setActionCommand(actionCommand);
        button.addActionListener(this);
        setup(button).pad(5).addToLayout();
        return button;
    }

    /**
     * Enables or disables the "Take Passage" button depending on whether the given room has a passage and whether a
     * passage has already been taken
     *
     * @param room room to check
     */
    private void maybeEnableTakePassage(Room room) {
        if (room != null) {
            suggestButton.setEnabled(true);
            if (room.getPassageExit() != null) {
                passageButton.setText("Take passage to " + room.getPassageExit().getName());
                passageButton.setEnabled(!passageTaken);
            }
            else {
                passageButton.setText("Take passage");
                passageButton.setEnabled(false);
            }
        }
        else {
            suggestButton.setEnabled(false);
            passageButton.setText("Take passage");
            passageButton.setEnabled(false);
        }
    }

    /**
     * Shows a dialog to get an accusation from the player
     */
    private void accuse() {
        Game.Suggestion accusation = getAccusation();
        if (accusation == null) {
            return;
        }

        Solution solution = game.getSolution();
        if (solution.checkAgainst(accusation)) {
            emitAction("turn.win");
            JOptionPane.showMessageDialog(getParent(), "Your accusation is correct. You win!");
        }
        else {
            emitAction("turn.lose");
            currentPlayer.setInGame(false);
            JOptionPane.showMessageDialog(getParent(), "Your accusation was wrong - sorry, you're out of the game.");

            // Check whether someone won by default (because all except one player was knocked out)
            List<Player> playersLeft = new ArrayList<>();
            for (Player p : game.getPlayers()) {
                if (p.isInGame()) {
                    playersLeft.add(p);
                }
            }
            if (playersLeft.size() == 1) {
                emitAction("turn.winByDefault");
                JOptionPane.showMessageDialog(
                        getParent(), "<html><p><b>" + playersLeft.get(0).getName() + "</b> won by default - " +
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

    /**
     * Shows a dialog to get a suggestion from the player
     */
    private void suggest() {
        Game.Suggestion suggestion = getSuggestion();
        if (suggestion == null) {
            return;
        }

        Game.Disprover disprover = game.disproveSuggestion(currentPlayer, suggestion);
        if (disprover == null) {
            JOptionPane.showMessageDialog(getParent(),
                    "Your suggestion could not be disproved.", "Suggestion", JOptionPane.INFORMATION_MESSAGE
            );
        }
        else {
            JOptionPane.showMessageDialog(getParent(),
                    "<html>Your suggestion was disproved by <b>" + disprover.getPlayer().getName() + "</b>." +
                            "<br />They are holding <b>" + disprover.getCard().getName() + "</b></html>",
                    "Suggestion disproved", JOptionPane.INFORMATION_MESSAGE
            );
        }
        suggestButton.setEnabled(false);
    }

    /**
     * Shows a dialog containing all cards in the player's hand
     */
    private void showHand() {
        String html = "<html><p>You are holding</p><ul>";
        for (Card card : currentPlayer.getHand()) {
            html += "<li><b>" + card.getName() + "</b></li>";
        }
        html += "</ul></html>";

        JOptionPane.showMessageDialog(
                getParent(), html, currentPlayer.getName() + "'s hand", JOptionPane.PLAIN_MESSAGE
        );
    }

    /**
     * Shows a dialog requesting a suggestion
     *
     * @return the suggestion that was entered (or null if the dialog was cancelled)
     */
    private Game.Suggestion getSuggestion() {
        Room room = currentPlayer.getRoom();
        if (room == null) {
            JOptionPane.showMessageDialog(
                    getParent(), "You can only make a suggestion when in a room!",
                    "Unable to suggest", JOptionPane.WARNING_MESSAGE
            );
            return null;
        }

        GameData data = game.getData();

        GridPanel contentPane = new GridPanel();
        contentPane.setup(new JLabel(
            "<html>You suggest that the murder was committed in the <b>" + room.getName() + "</b>"
        )).spanH(2).pad(5).addToLayout();
        contentPane.finishRow();

        ButtonGroup[] buttonGroups = addDataChoices(contentPane, false);

        int result = JOptionPane.showOptionDialog(
                getParent(), contentPane, "Suggestion", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, new Object[]{"OK", "Cancel"}, "OK"
        );
        if (result == JOptionPane.OK_OPTION) {
            Suspect suspect = data.getSuspect(getSelectedButtonText(buttonGroups[0]));
            Weapon weapon = data.getWeapon(getSelectedButtonText(buttonGroups[1]));
            return new Game.Suggestion(suspect, weapon, room);
        }
        else {
            return null;
        }
    }

    /**
     * Shows a dialog requesting an accusation
     *
     * @return a Suggestion object representing the accusation, or null if the dialog was cancelled
     */
    private Game.Suggestion getAccusation() {
        GridPanel contentPane = new GridPanel();
        contentPane.setup(new JLabel(
                "You are about to make an accusation. Remember, if it's wrong you will be out of the game!"
        )).spanH(3).pad(5).addToLayout();
        contentPane.finishRow();

        GameData data = game.getData();

        ButtonGroup[] buttonGroups = addDataChoices(contentPane, true);

        int result = JOptionPane.showOptionDialog(
                getParent(), contentPane, "Accusation", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, new Object[]{"OK", "Cancel"}, "OK"
        );
        if (result == JOptionPane.OK_OPTION) {
            Suspect suspect = data.getSuspect(getSelectedButtonText(buttonGroups[0]));
            Weapon weapon = data.getWeapon(getSelectedButtonText(buttonGroups[1]));
            Room room = data.getRoom(getSelectedButtonText(buttonGroups[2]));
            return new Game.Suggestion(suspect, weapon, room);
        }
        else {
            return null;
        }
    }

    /**
     * Adds lists of radio buttons representing suspects, weapons and (optionally) rooms to a panel
     *
     * @param panel parent panel to add the lists to
     * @param includeRooms true if radio buttons for rooms should be added to the panel, false otherwise
     * @return an array of the button groups created
     */
    private ButtonGroup[] addDataChoices(GridPanel panel, boolean includeRooms) {
        GameData data = game.getData();
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

    /**
     * Creates a list of radio buttons (one for each of the given cards) and wraps them in a ButtonGroup.
     * Adds the buttons to a panel
     *
     * @param panel parent panel to add the lists to
     * @param title title of the list
     * @param cards list of cards to add radio buttons for
     * @return the new button group
     */
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
        // Ensure that one element is always selected
        buttonGroup.getElements().nextElement().setSelected(true);

        panel.setup(optionsPanel).anchorTopLeft().flexH().addToLayout();
        return buttonGroup;
    }

    /**
     * Returns the text of the first selected button in the specified group, or null if no button is selected
     *
     * @param buttonGroup button group to search
     */
    private static String getSelectedButtonText(ButtonGroup buttonGroup) {
        for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();
            if (button.isSelected()) {
                return button.getText();
            }
        }
        return null;
    }

    /**
     * Sends each of this panel's action listeners the specified action command
     */
    private void emitAction(String command) {
        ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, command);
        for (ActionListener listener : actionListeners) {
            listener.actionPerformed(e);
        }
    }
}
