package cluedo;

import java.io.IOException;
import java.util.ArrayList;

import cluedo.game.Game;
import cluedo.game.Player;
import cluedo.loader.Loader;
import cluedo.loader.Loader.SyntaxException;
import cluedo.ui.ConsoleRenderer;

public class Main {

	public static void main(String[] args) {
		String filename = ConsoleRenderer.getFilename("data/standard.txt");
		try {
			Loader loader = new Loader(filename);
			new ConsoleRenderer().run(new Game(loader));
		} catch (IOException e) {
			System.out.println("\nFailed to read game file! Please ensure that it exists and is accessible.");
			e.printStackTrace();
		} catch (SyntaxException e) {
			System.out.println("\nUnknown syntax found in game file! See below for details.");
			e.printStackTrace();
		}
	}
}
