package cluedo.game;

import java.awt.Point;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cluedo.game.objects.Room;
import cluedo.game.objects.Suspect;
import cluedo.loader.Loader;

public class Board {
	/**
	 * Exception thrown when a player is unable to move to a point
	 * (because it's not a corridor, it's outside the board, etc.)
	 */
	@SuppressWarnings("serial")
	public class UnableToMoveException extends Exception {
		public UnableToMoveException(String message) {
			super(message);
		}
	}

	/** Possible movement directions - passed to movePlayer() */
	public enum Direction {
		UP,
		RIGHT,
		DOWN,
		LEFT
	}

	private int width;
	private int height;

	// Contains (width * height) bits in 'blocks' of (size).
	// The bit at (x + width * y) is true if the point (x,y) is a corridor
	private BitSet corridors;
	private Map<Point, Door> doorLocations;

	private Map<Player, Point> playerLocations = new HashMap<Player, Point>();

	/**
	 * Construct a new board
	 * @param loader will be used to get corridor positions, suspect start locations,
	 * 				 door locations, etc.
	 */
	public Board(Loader loader) {
		this.width = loader.getBoardWidth();
		this.height = loader.getBoardHeight();
		this.corridors = loader.getCorridors();

		this.doorLocations = new HashMap<Point, Door>();
		for (Room room : loader.getRooms().values()) {
			for (Door door : room.getDoors()) {
				this.doorLocations.put(door.getLocation(), door);
			}
		}
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public boolean isCorridor(int x, int y) {
		return corridors.get(x + width * y);
	}

	/**
	 * Get the current location of a particular player
	 * @param player
	 * @return the location of the player, or null if the player is not on the board
	 */
	public Point getPlayerLocation(Player player) {
		return playerLocations.get(player);
	}

	/**
	 * Add a player to the game, placing them at the start point of their
	 * suspect token
	 * @param player
	 */
	public void addPlayer(Player player) {
		Point startLocation = player.getToken().getStartLocation();
		if (startLocation == null) {
			throw new RuntimeException("Player " + player + "'s token doesn't have a start position");
		}
		this.playerLocations.put(player, startLocation);
	}

	/**
	 * Removes all players from the game
	 */
	public void clearPlayers() {
		playerLocations.clear();
	}

	/**
	 * Move a player out of a room through a door, then along the specified path
	 * @param player player to move
	 * @param steps path to move the player along
	 * @param door door the player should start moving from TODO update comment with null
	 * @throws UnableToMoveException if the player tries to move to an invalid location,
	 * 								 or is trying to exit from a room they're not in
	 */
	public void movePlayer(Player player, List<Direction> steps, Door door) throws UnableToMoveException {
		if (!playerLocations.containsKey(player)) {
			throw new RuntimeException("Player " + player + " isn't on the board");
		}

		// Simple case - player is just moving along a corridor
		if (door == null) {
			if (player.getRoom() != null) {
				throw new UnableToMoveException("You must go out through a door");
			}
			movePlayerAlongPath(player, steps);
			return;
		}

		// Complex case - player is going out of a room
		if (player.getRoom() == null || !player.getRoom().equals(door.getRoom())) {
			throw new UnableToMoveException("You're not in that room");
		}
		for (Map.Entry<Point, Door> entry : doorLocations.entrySet()) {
			if (entry.getValue() == door) {
				playerLocations.put(player, door.getLocation());
				movePlayerAlongPath(player, steps);
				return;
			}
		}
		throw new RuntimeException("Door doesn't exist");
	}

	/**
	 * Move a player along the specified path
	 * @param player player to move
	 * @param steps path to move the player along
	 * @throws UnableToMoveException if the player tries to move to an invalid location
	 */
	private void movePlayerAlongPath(Player player, List<Direction> steps) throws UnableToMoveException {
		Point currentLocation = playerLocations.get(player);
		if (currentLocation == null) {
			throw new RuntimeException("Player " + player + " isn't on the board");
		}

		Direction finalStep = steps.remove(steps.size() - 1);
		Point newLocation = currentLocation;
		for (Direction step : steps) {
			newLocation = moveFrom(newLocation, step);
			checkInCorridor(newLocation);
		}

		newLocation = moveFrom(newLocation, finalStep);
		Door door = doorLocations.get(newLocation);
		if (door != null) {
//			TODO A CHECK IF IT'S HORIZONTAL OR VERTICAL
			player.setRoom(door.getRoom());
		}
		else {
			checkInCorridor(newLocation);
			player.setRoom(null);
		}
		playerLocations.put(player, newLocation);

	}

	private Point moveFrom(Point location, Direction step) {
		switch (step) {
		case UP:
			return new Point(location.x, location.y - 1);
		case RIGHT:
			return new Point(location.x + 1, location.y);
		case DOWN:
			return new Point(location.x, location.y + 1);
		case LEFT:
		default:
			return new Point(location.x - 1, location.y);
		}
	}

	private void checkInCorridor(Point location) throws UnableToMoveException {
		if (location.x < 0 || location.y < 0 ||
				location.x > width || location.y > height) {
			throw new UnableToMoveException("You're trying to go outside the board");
		}
		else if (!corridors.get(location.x + location.y * width)) {
			throw new UnableToMoveException("You're trying to move through a wall");
		}
	}
}
