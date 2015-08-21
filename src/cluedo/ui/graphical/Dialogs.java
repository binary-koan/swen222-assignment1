package cluedo.ui.graphical;

import cluedo.game.GameData;
import cluedo.game.Player;
import cluedo.game.objects.Card;
import cluedo.game.objects.Room;
import cluedo.game.objects.Suspect;
import cluedo.game.objects.Weapon;
import cluedo.ui.graphical.controls.GridPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

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

    public static Card[] getSuggestion(Component parent, Player player, GameData data) {
        if (player.getRoom() == null) {
            JOptionPane.showMessageDialog(parent, "You can only make a suggestion when in a room!", "Unable to suggest",
                    JOptionPane.WARNING_MESSAGE);
        }

        GridPanel contentPane = new GridPanel();
        contentPane.setup(new JLabel(
                "<html>You suggest that the murder was committed in the <b>" + player.getRoom().getName() + "</b>"
        )).spanH(2).pad(5).addToLayout();
        contentPane.finishRow();

        JComboBox[] comboBoxes = addDataChoices(contentPane, data, false);

        int result = JOptionPane.showOptionDialog(parent, contentPane, "Suggestion", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, new Object[] { "OK", "Cancel" }, "OK");
        if (result == JOptionPane.OK_OPTION) {
            Suspect suspect = (Suspect)comboBoxes[0].getSelectedItem();
            Weapon weapon = (Weapon)comboBoxes[1].getSelectedItem();
            return new Card[] { suspect, weapon };
        }
        else {
            return null;
        }
    }

    public static Card[] getAccusation(Component parent, Player player, GameData data) {
        GridPanel contentPane = new GridPanel();
        contentPane.setup(new JLabel(
                "You are about to make an accusation. Remember, if it's wrong you will be out of the game!"
        )).spanH(2).pad(5).addToLayout();
        contentPane.finishRow();

        JComboBox[] comboBoxes = addDataChoices(contentPane, data, true);

        int result = JOptionPane.showOptionDialog(parent, contentPane, "Accusation", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, new Object[] { "OK", "Cancel" }, "OK");
        if (result == JOptionPane.OK_OPTION) {
            Room room = (Room)comboBoxes[0].getSelectedItem();
            Suspect suspect = (Suspect)comboBoxes[1].getSelectedItem();
            Weapon weapon = (Weapon)comboBoxes[2].getSelectedItem();
            return new Card[] { room, suspect, weapon };
        }
        else {
            return null;
        }
    }

    private static JComboBox[] addDataChoices(GridPanel panel, GameData data, boolean includeRooms) {
        panel.setup(new JLabel("Murderer:", SwingConstants.LEFT)).pad(5).addToLayout();
        Suspect[] suspects = data.getSuspects().toArray(new Suspect[data.getSuspects().size()]);
        JComboBox<Suspect> suspectBox = new JComboBox<>(suspects);
        panel.setup(suspectBox).flexH().pad(5).addToLayout();

        panel.finishRow();

        panel.setup(new JLabel("Murder weapon:", SwingConstants.LEFT)).pad(5).addToLayout();
        Weapon[] weapons = data.getWeapons().toArray(new Weapon[data.getWeapons().size()]);
        JComboBox<Weapon> weaponBox = new JComboBox<>(weapons);
        panel.setup(weaponBox).flexH().pad(5).addToLayout();

        if (includeRooms) {
            panel.finishRow();

            panel.setup(new JLabel("Murder location:", SwingConstants.LEFT)).pad(5).addToLayout();
            Room[] rooms = data.getRooms().toArray(new Room[data.getWeapons().size()]);
            JComboBox<Room> roomBox = new JComboBox<>(rooms);
            panel.setup(roomBox).flexH().pad(5).addToLayout();

            return new JComboBox[] { roomBox, suspectBox, weaponBox };
        }
        else {
            return new JComboBox[] { suspectBox, weaponBox };
        }
    }
}
