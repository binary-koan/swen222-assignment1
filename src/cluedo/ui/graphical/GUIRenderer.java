package cluedo.ui.graphical;

import cluedo.game.Game;
import cluedo.game.Player;
import cluedo.ui.graphical.controls.GridPanel;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;

public class GUIRenderer extends JFrame implements ActionListener {
    private final Game game;
    private BoardCanvas boardCanvas;
    private PlayerDisplay playerDisplay;
    private GridPanel actionsPanel;

    private int currentPlayerIndex;

    public GUIRenderer(Game game) {
        this.game = game;
        setupWindow();
    }

    private void newGame() {
        game.reset();

        List<Player> players = PlayerSetupPanel.queryPlayers(this, game.getData().getSuspects(), queryPlayerCount());
        if (players == null) {
            return;
        }

        for (Player player : players) {
            game.addPlayer(player);
        }
        game.distributeCards();

        enableGameControls();
        currentPlayerIndex = 0;
        startTurn();
    }

    private void startTurn() {
        Player player = getCurrentPlayer();
        int dieRoll = (int) (Math.random() * 6 + 1);
        player.startTurn(dieRoll);
        boardCanvas.setPlayer(player);
        playerDisplay.setPlayer(player);
    }

    private int queryPlayerCount() {
        int maxPlayers = game.getData().getSuspects().size();
        Object[] options = new Object[maxPlayers - 2];
        for (int i = 3; i <= maxPlayers; i++) {
            options[i - 3] = Integer.toString(i);
        }
        int result = JOptionPane.showOptionDialog(this, "How many players?", "Number of players",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        return result + 3;
    }

    private void setupWindow() {
        setTitle("Cluedo");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        setupGameMenu(menuBar);

        GridPanel panel = new GridPanel();

        boardCanvas = new BoardCanvas(game);
        panel.setup(boardCanvas).flexH().flexV().addToLayout();
        panel.finishRow();

        playerDisplay = new PlayerDisplay(boardCanvas);
        panel.setup(playerDisplay).flexH().addToLayout();
        panel.finishRow();

        actionsPanel = new GridPanel();
        addButton("Suggest", "player.suggest", actionsPanel);
        addButton("Accuse", "player.accuse", actionsPanel);
        addButton("Show hand", "player.showHand", actionsPanel);
        addButton("End turn", "player.endTurn", actionsPanel);
        panel.setup(actionsPanel).center().addToLayout();
        panel.finishRow();

        setContentPane(panel);
        stopGame();
    }

    private void addButton(String text, String actionCommand, GridPanel layout) {
        JButton button = new JButton(text);
        button.setActionCommand(actionCommand);
        button.addActionListener(this);
        layout.setup(button).pad(5).addToLayout();
    }

    private void enableGameControls() {
        boardCanvas.setEnabled(true);
        for (Component component : actionsPanel.getComponents()) {
            component.setEnabled(true);
        }
    }

    private void stopGame() {
        boardCanvas.setEnabled(false);
        for (Component component : actionsPanel.getComponents()) {
            component.setEnabled(false);
        }
    }

    private void addMenuItem(JMenu menu, String title, String action, KeyStroke accelerator) {
        JMenuItem item = new JMenuItem(title);
        item.setActionCommand(action);
        if (accelerator != null) {
            item.setAccelerator(accelerator);
        }
        item.addActionListener(this);
        menu.add(item);
    }

    private void setupGameMenu(JMenuBar menuBar) {
        JMenu gameMenu = new JMenu("Game");
        gameMenu.setMnemonic(KeyEvent.VK_G);
        menuBar.add(gameMenu);

        addMenuItem(gameMenu, "New game", "game.new", KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
        addMenuItem(gameMenu, "Change setup", "game.setFile", null);
        gameMenu.addSeparator();
        addMenuItem(gameMenu, "Quit", "file.quit", KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "game.new":
                newGame();
                break;
            case "game.setFile":
                throw new NotImplementedException();
            case "file.quit":
                System.exit(0);
            case "player.suggest":
                suggest();
                break;
            case "player.accuse":
                accuse();
                break;
            case "player.showHand":
                Dialogs.showHand(this, getCurrentPlayer());
                break;
            case "player.endTurn":
                currentPlayerIndex = (currentPlayerIndex + 1) % game.getPlayers().size();
                startTurn();
                break;
            default:
                throw new RuntimeException("Unknown action: " + e.getActionCommand());
        }
    }

    private void accuse() {
        Dialogs.getAccusation(this, getCurrentPlayer(), game.getData());
    }

    private void suggest() {
        Dialogs.getSuggestion(this, getCurrentPlayer(), game.getData());
    }

    private Player getCurrentPlayer() {
        return game.getPlayers().get(currentPlayerIndex);
    }
}
