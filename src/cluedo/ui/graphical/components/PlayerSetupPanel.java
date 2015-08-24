package cluedo.ui.graphical.components;

import cluedo.game.Player;
import cluedo.game.objects.Suspect;
import cluedo.ui.graphical.controls.GridPanel;
import cluedo.ui.graphical.controls.PlaceholderTextField;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Panel used to enter player names and tokens
 */
public class PlayerSetupPanel extends GridPanel implements ActionListener, DocumentListener {
    /**
     * Convenience class to set the return value of a JOptionPane to an arbitrary button
     */
    private static class OptionActionListener implements ActionListener {
        private JButton attachee;

        public OptionActionListener(JButton attachee) {
            this.attachee = attachee;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JOptionPane pane = getOptionPane((JComponent)e.getSource());
            pane.setValue(attachee);
        }

        private JOptionPane getOptionPane(JComponent component) {
            if (component instanceof JOptionPane) {
                return (JOptionPane) component;
            }
            else {
                return getOptionPane((JComponent)component.getParent());
            }
        }
    }

    /**
     * Shows a dialog allowing the user to set up an arbitrary number of players
     *
     * @param parent dialog parent component
     * @param suspects a list of all possible tokens
     * @param count the number of players to return
     * @return a list of players built from what the user entered into the dialog, or null if the user cancelled the
     *         operation
     */
    public static List<Player> queryPlayers(Component parent, List<Suspect> suspects, int count) {
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new OptionActionListener(okButton));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new OptionActionListener(cancelButton));

        PlayerSetupPanel panel = new PlayerSetupPanel(suspects, count, okButton);
        panel.setPreferredSize(new Dimension(panel.getPreferredSize().width + 200, panel.getPreferredSize().height));

        int result = JOptionPane.showOptionDialog(parent, panel, "Add player details", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, new Object[] { okButton, cancelButton }, okButton);
        return (result == JOptionPane.OK_OPTION) ? panel.getPlayers() : null;
    }

    private List<Suspect> suspects;

    private List<JTextField> textFields = new ArrayList<>();
    private List<JComboBox> comboBoxes = new ArrayList<>();
    private JLabel errorLabel;
    private JButton okButton;

    /**
     * Construct a new player setup panel
     *
     * @param suspects list of all possible tokens
     * @param nPlayers number of players the user should input
     * @param okButton a button to enable or disable depending on whether the current input is valid
     */
    private PlayerSetupPanel(List<Suspect> suspects, int nPlayers, JButton okButton) {
        this.suspects = new ArrayList<>(suspects);
        this.okButton = okButton;
        okButton.setEnabled(false);

        String[] suspectStrings = new String[suspects.size() + 1];
        suspectStrings[0] = "Select a token ...";
        for (int i = 1; i < suspectStrings.length; i++) {
            suspectStrings[i] = suspects.get(i - 1).getName();
        }

        for (int i = 0; i < nPlayers; i++) {
            JLabel label = new JLabel("Player " + (i + 1));
            setup(label).pad(5).addToLayout();

            PlaceholderTextField textField = new PlaceholderTextField();
            textField.setPlaceholder("Name");
            textField.getDocument().addDocumentListener(this);
            textFields.add(textField);
            setup(textField).flexH().pad(5).addToLayout();

            JComboBox<String> comboBox = new JComboBox<>(suspectStrings);
            comboBox.addActionListener(this);
            comboBoxes.add(comboBox);
            setup(comboBox).pad(5).addToLayout();

            finishRow();
        }

        errorLabel = new JLabel("Please fill in the names of all players.");
        setup(errorLabel).spanH(3).center().pad(5).addToLayout();
    }

    /**
     * Builds and returns the list of players on this dialog. Assumes that the current input is valid
     *
     * @return the generated list
     */
    private List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < textFields.size(); i++) {
            Suspect token = suspects.get(comboBoxes.get(i).getSelectedIndex() - 1);
            players.add(new Player(textFields.get(i).getText(), token));
        }
        return players;
    }

    /**
     * Checks whether the current input is valid, enabling the OK button if it is and showing an error message if not.
     * Valid input means:
     * - All player name text fields contain text
     * - All player names are unique
     * - All token combo boxes have a valid value
     * - All chosen tokens are unique
     */
    private void maybeEnableOkButton() {
        // Check names exist and are unique
        Set<String> usedNames = new HashSet<>();
        for (JTextField field : textFields) {
            if (field.getText() == null || field.getText().isEmpty()) {
                disableOkButton("Please fill in the names of all players.");
                return;
            }
            else if (usedNames.contains(field.getText())) {
                disableOkButton("Please make sure all players have a unique name");
                return;
            }
            usedNames.add(field.getText());
        }
        // Check tokens exist and are unique
        Set<String> usedSuspects = new HashSet<>();
        for (JComboBox comboBox : comboBoxes) {
            String item = (String)comboBox.getSelectedItem();
            if (comboBox.getSelectedIndex() == 0) {
                disableOkButton("Please select a token for each player.");
                return;
            }
            else if (usedSuspects.contains(item)) {
                disableOkButton("Please make sure all players are using a unique token.");
                return;
            }
            usedSuspects.add(item);
        }
        // If nothing has failed by now, the input must be OK
        errorLabel.setText("You're good to go!");
        okButton.setEnabled(true);
    }

    /**
     * Disables the OK button, displaying an error message on the form
     *
     * @param reason error message to display
     */
    private void disableOkButton(String reason) {
        errorLabel.setText(reason);
        okButton.setEnabled(false);
    }

    // Check whether to enable the OK button whenever a text box or combo box value is changed

    @Override
    public void actionPerformed(ActionEvent e) {
        maybeEnableOkButton();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        maybeEnableOkButton();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        maybeEnableOkButton();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        maybeEnableOkButton();
    }
}
