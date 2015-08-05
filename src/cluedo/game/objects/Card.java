package cluedo.game.objects;

/**
 * Represents a card (suspect, room or weapon) in the game. Cards are given out
 * to players and make up the game's solution.
 */
public interface Card {
	/**
	 * Returns a string identifying this card
	 */
	public String getName();
}
