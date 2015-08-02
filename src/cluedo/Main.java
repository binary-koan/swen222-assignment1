package cluedo;

import java.io.IOException;

import cluedo.game.Game;
import cluedo.loader.Loader;
import cluedo.loader.Loader.SyntaxException;
import cluedo.ui.ConsoleRenderer;

public class Main {

	public static void main(String[] args) {
		String filename = ConsoleRenderer.getFilename("data/standard.txt");
		try {
			new Game(new Loader(filename), new ConsoleRenderer()).run();
		} catch (IOException e) {
			System.out.println("\nFailed to read game file! Please ensure that it exists and is accessible.");
			e.printStackTrace();
		} catch (SyntaxException e) {
			System.out.println("\nUnknown syntax found in game file! See below for details.");
			e.printStackTrace();
		}
	}

}
