package cluedo.ui.graphical;

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

public class PlayerSetupPanel extends GridPanel implements ActionListener, DocumentListener {
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

    public PlayerSetupPanel(List<Suspect> suspects, int nPlayers, JButton okButton) {
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
            addToLayout(new GridItemBuilder(label).pad(5));

            PlaceholderTextField textField = new PlaceholderTextField();
            textField.setPlaceholder("Name");
            textField.getDocument().addDocumentListener(this);
            textFields.add(textField);
            addToLayout(new GridItemBuilder(textField).flexH().pad(5));

            JComboBox<String> comboBox = new JComboBox<>(suspectStrings);
            comboBox.addActionListener(this);
            comboBoxes.add(comboBox);
            addToLayout(new GridItemBuilder(comboBox).pad(5));

            finishRow();
        }

        createErrorLabel(nPlayers + 1);
    }

    private void createErrorLabel(int index) {
        errorLabel = new JLabel("Please fill in the names of all players.");
        addLayoutRow(index, errorLabel);
    }

    private void addLayoutRow(int index, Component component) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridwidth = 3;
        constraints.gridy = index;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.insets = new Insets(5, 5, 5, 5);
        add(component, constraints);
    }

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

    private void maybeEnableOkButton() {
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
        errorLabel.setText("You're good to go!");
        okButton.setEnabled(true);
    }

    private void disableOkButton(String reason) {
        errorLabel.setText(reason);
        okButton.setEnabled(false);
    }

    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < textFields.size(); i++) {
            Suspect token = suspects.get(comboBoxes.get(i).getSelectedIndex() - 1);
            players.add(new Player(textFields.get(i).getText(), token));
        }
        return players;
    }
}
