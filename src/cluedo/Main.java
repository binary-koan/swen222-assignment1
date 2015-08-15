package cluedo;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;

import cluedo.game.Game;
import cluedo.loader.Loader;
import cluedo.loader.Loader.SyntaxException;
import cluedo.ui.console.ConsoleRenderer;
import cluedo.ui.graphical.GUIRenderer;

import javax.swing.*;

/**
 * Main class - starts a game of Cluedo, rendering with a CLI
 */
public class Main {

	public static void main(String[] args) {
		try {
			if (Arrays.asList(args).contains("--cli")) {
				startCLI();
			}
			else {
				startGUI();
			}
		}
		catch (IOException e) {
			System.out.println("\nFailed to read game file! Please ensure that it exists and is accessible.");
			e.printStackTrace();
		}
		catch (SyntaxException e) {
			System.out.println("\nUnknown syntax found in game file! See below for details.");
			e.printStackTrace();
		}
	}

	private static void startGUI() throws IOException, SyntaxException {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ignored) {
        }
		// The user can change the setup later from the game menu
		final Loader loader = new Loader("data/standard.txt");
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				new GUIRenderer(new Game(loader)).setVisible(true);
			}
		});
	}

	private static void startCLI() throws IOException, SyntaxException {
		String filename = ConsoleRenderer.queryFilename("data/standard.txt");
		Loader loader = new Loader(filename);
		new ConsoleRenderer(new Game(loader)).run();
	}
}
