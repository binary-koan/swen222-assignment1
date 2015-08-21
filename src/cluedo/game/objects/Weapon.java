package cluedo.game.objects;

/**
 * Represents a possible murder weapon in the game
 */
public class Weapon implements Card {
	private String name;

	/**
	 * Creates a new weapon
	 * 
	 * @param name
	 *            a string identifying the weapon
	 */
	public Weapon(String name) {
		this.name = name;
	}

	/**
	 * Returns the name of this weapon
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
}
