package cluedo.game.objects;

import java.awt.Color;
import java.awt.Point;

/**
 * Represents a suspect (token or card) in the game. Players take the role of
 * suspects, and one (from the full list) is randomly selected as the murderer.
 */
public class Suspect implements Card {
	private final char id;
	private final String name;
	private final Color color;
	private Point startLocation;

	/**
	 * Creates a new suspect
	 * 
	 * @param id
	 *            a single character to identify the suspect on the board
	 * @param name
	 *            the full name of the suspect
	 * @param color
	 *            the color to use when drawing the suspect (not currently used)
	 */
	public Suspect(char id, String name, Color color) {
		this.id = id;
		this.name = name;
		this.color = color;
	}

	/**
	 * Returns a single character identifying the suspect
	 */
	public char getIdentifier() {
		return id;
	}

	/**
	 * Returns the full name of the suspect
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the start location of the suspect (where a player using this suspect
	 * as their token will start) in board coordinates
	 * 
	 * @param x
	 * @param y
	 */
	public void setStartLocation(int x, int y) {
		this.startLocation = new Point(x, y);
	}

	/**
	 * Returns the start location of the suspect (where a player using this
	 * suspect as their token will start)
	 */
	public Point getStartLocation() {
		return this.startLocation;
	}

	public Color getColor() {
		return color;
	}

	@Override
	public String toString() {
		return name;
	}
}
