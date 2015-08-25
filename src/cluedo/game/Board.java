package cluedo.game;

import java.awt.Point;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cluedo.game.objects.Room;
import cluedo.loader.Loader;

/**
 * Represents the game board. This class also tracks the location of objects
 * such as players and doors, and handles player movement.
 */
public class Board {
    /**
	 * Exception thrown when a player is unable to move to a point (because it's
	 * not a corridor, it's outside the board, etc.)
	 */
	@SuppressWarnings("serial")
	public class UnableToMoveException extends Exception {
		public UnableToMoveException(String message) {
			super(message);
		}
	}

	/** Possible movement directions - passed to movePlayer() */
	public enum Direction {
		UP, RIGHT, DOWN, LEFT
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
	 *
	 * @param loader
	 *            will be used to get corridor positions, suspect start
	 *            locations, door locations, etc.
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

	/**
	 * Returns the width of the board (in tiles)
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Returns the height of the board (in tiles)
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Returns true if the given point is a corridor, false otherwise
	 *
	 * @param point
	 *            point to check (in tiles)
	 */
	public boolean isCorridor(Point point) {
		if (point.x < 0 || point.x >= width || point.y < 0 || point.y >= height) {
            return false;
        }
		return corridors.get(point.x + width * point.y);
	}

    /**
     * Returns true if the given point is a door, false otherwise
     * @param point
     *            point to check (in tiles)
     */
    public boolean isDoor(Point point) {
        return doorLocations.containsKey(point);
    }

	/**
	 * Returns true if the given point is a door and can be entered from `beside`, false otherwise
	 *
     * @param doorLocation
     *            point to check (in tiles)
     * @param beside
     *            point checking from (ensures that this returns false if trying
     *            to go through a vertical door horizontally etc.)
     */
	public boolean canEnterDoor(Point doorLocation, Point beside) {
        Door door = doorLocations.get(doorLocation);
        if (door == null) {
            return false;
        }
        else if (doorLocation.getX() == beside.getX() && door.isVertical()) {
            return false;
        }
        else if (doorLocation.getY() == beside.getY() && !door.isVertical()) {
            return false;
        }
        return true;
	}

	/**
	 * Get the current location of a particular player
	 *
	 * @param player
	 * @return the location of the player, or null if the player is not on the
	 *         board
	 */
	public Point getPlayerLocation(Player player) {
		return playerLocations.get(player);
	}

	/**
	 * Add a player to the board, placing them at the start point of their
	 * suspect token
	 *
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
	 * Remove all players from the board
	 */
	public void clearPlayers() {
		playerLocations.clear();
	}

	/**
	 * Move a player out of a room through a door, then along the specified path
	 *
	 * @param player
	 *            player to move
	 * @param steps
	 *            path to move the player along
	 * @param door
	 *            door the player should start moving from TODO update comment
	 *            with null
	 * @throws UnableToMoveException
	 *             if the player tries to move to an invalid location, or is
	 *             trying to exit from a room they're not in
	 */
	public void movePlayer(Player player, List<Direction> steps, Door door) throws UnableToMoveException {
		if (!playerLocations.containsKey(player)) {
			throw new RuntimeException("Player " + player + " isn't on the board");
		}
		List<Direction> stepsCopy = new ArrayList<>(steps);

		// Simple case - player is just moving along a corridor
		if (door == null) {
			if (player.getRoom() != null) {
				throw new UnableToMoveException("You must go out through a door");
			}
			movePlayerAlongPath(player, stepsCopy);
			return;
		}

		// Complex case - player is going out of a room
		if (player.getRoom() == null
				|| !player.getRoom().equals(door.getRoom())) {
			throw new UnableToMoveException("You're not in that room");
		}
		for (Map.Entry<Point, Door> entry : doorLocations.entrySet()) {
			if (entry.getValue() == door) {
				playerLocations.put(player, door.getPointBeside());
				player.setRoom(null);
				movePlayerAlongPath(player, stepsCopy);
				return;
			}
		}
		throw new RuntimeException("Door doesn't exist");
	}

	/**
	 * Move a player along the specified path
	 *
	 * @param player
	 *            player to move
	 * @param steps
	 *            path to move the player along
	 * @throws UnableToMoveException
	 *             if the player tries to move to an invalid location
	 */
	private void movePlayerAlongPath(Player player, List<Direction> steps) throws UnableToMoveException {
		if (steps.size() == 0) {
            return;
        }

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
			if (door.isVertical() &&
					(finalStep == Direction.UP || finalStep == Direction.DOWN)) {
				throw new UnableToMoveException("You can't enter this door that way");
			}
			else if (!door.isVertical() &&
					(finalStep == Direction.LEFT || finalStep == Direction.RIGHT)) {
				throw new UnableToMoveException("You can't enter this door that way");
			}
			player.setRoom(door.getRoom());
		}
		else {
			checkInCorridor(newLocation);
			if (playerLocations.values().contains(newLocation)) {
				throw new UnableToMoveException("You can't move onto the same square as another player!");
			}
			player.setRoom(null);
		}
		playerLocations.put(player, newLocation);
	}

	/**
	 * Moves one step from the given point in the given direction
	 *
	 * @param location
	 *            point to move from
	 * @param step
	 *            direction to move in
	 * @return a new point created by moving one step in the direction
	 */
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

	/**
	 * Checks whether a location is a corridor (ie. a valid place to move) or
	 * not
	 *
	 * @param location
	 *            point to check
	 * @throws UnableToMoveException
	 *             if the point is outside the board or is not a corridor
	 */
	private void checkInCorridor(Point location) throws UnableToMoveException {
		if (location.x < 0 || location.y < 0 || location.x > width
				|| location.y > height) {
			throw new UnableToMoveException(
					"You're trying to go outside the board");
		}
		else if (!corridors.get(location.x + location.y * width)) {
			throw new UnableToMoveException(
					"You're trying to move through a wall");
		}
	}
}
