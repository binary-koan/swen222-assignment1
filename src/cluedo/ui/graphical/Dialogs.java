package cluedo.ui.graphical;

import cluedo.game.Game;
import cluedo.game.GameData;
import cluedo.game.Player;
import cluedo.game.objects.Card;
import cluedo.game.objects.Room;
import cluedo.game.objects.Suspect;
import cluedo.game.objects.Weapon;
import cluedo.ui.graphical.controls.GridPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Enumeration;
import java.util.List;

public class Dialogs extends GridPanel {
    public static void showHand(Component parent, Player player) {
        String html = "<html><p>You are holding</p><ul>";
        for (Card card : player.getHand()) {
            html += "<li><b>" + card.getName() + "</b></li>";
        }
        html += "</ul></html>";
        JLabel label = new JLabel(html);

        JOptionPane.showMessageDialog(parent, label, player.getName() + "'s hand", JOptionPane.PLAIN_MESSAGE);
    }

    public static Game.Suggestion getSuggestion(Component parent, Player player, GameData data) {
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

    public static Game.Suggestion getAccusation(Component parent, Player player, GameData data) {
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

    private static ButtonGroup[] addDataChoices(GridPanel panel, GameData data, boolean includeRooms) {
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

    private static ButtonGroup buildButtonGroup(GridPanel panel, String title, List<? extends Card> cards) {
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
