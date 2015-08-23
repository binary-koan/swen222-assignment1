package cluedo.ui.graphical;

import cluedo.game.Game;
import cluedo.game.Player;
import cluedo.ui.graphical.controls.GridPanel;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;

public class GUIRenderer extends JFrame implements ActionListener {
    private final Game game;
    private BoardCanvas boardCanvas;
    private PlayerDisplay playerDisplay;
    private TurnButtons turnButtons;

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

        boardCanvas.setEnabled(true);
        currentPlayerIndex = -1;
        nextTurn();
    }

    private void nextTurn() {
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % game.getPlayers().size();
        } while (!getCurrentPlayer().isInGame());

        Player player = getCurrentPlayer();
        int dieRoll = (int) (Math.random() * 6 + 1);
        player.setMovesRemaining(dieRoll);
        boardCanvas.startTurn(player);
        playerDisplay.startTurn(player);
        turnButtons.startTurn(player);
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

        turnButtons = new TurnButtons(game);
        turnButtons.addActionListener(this);
        panel.setup(turnButtons).center().addToLayout();
        panel.finishRow();

        setContentPane(panel);
        stopGame();
    }

    private void stopGame() {
        boardCanvas.setEnabled(false);
        turnButtons.startTurn(null);
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
            case "turn.next":
                nextTurn();
                break;
            case "turn.lose":
                boardCanvas.repaint();
                break;
            case "turn.win":
                playerDisplay.unsetPlayer(getCurrentPlayer().getName() + " wins!");
                stopGame();
                break;
            case "turn.winByDefault":
                for (Player player : game.getPlayers()) {
                    if (player.isInGame()) {
                        playerDisplay.unsetPlayer(player.getName() + " wins by default.");
                        stopGame();
                        break;
                    }
                }
                break;
            default:
                throw new RuntimeException("Unknown action: " + e.getActionCommand());
        }
    }

    private Player getCurrentPlayer() {
        return game.getPlayers().get(currentPlayerIndex);
    }
}
