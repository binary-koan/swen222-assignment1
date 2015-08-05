package cluedo;

import java.io.IOException;

import cluedo.game.Game;
import cluedo.loader.Loader;
import cluedo.loader.Loader.SyntaxException;
import cluedo.ui.console.ConsoleRenderer;

/**
 * Main class - starts a game of Cluedo, rendering with a CLI
 */
public class Main {

	public static void main(String[] args) {
		String filename = ConsoleRenderer.queryFilename("data/standard.txt");
		try {
			Loader loader = new Loader(filename);
			new ConsoleRenderer(new Game(loader)).run();
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
}
