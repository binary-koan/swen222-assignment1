package cluedo.game;

import java.awt.Point;

import cluedo.game.objects.Room;

/**
 * Represents a door on the board which leads to a room
 */
public class Door {

	private Room room;
	private boolean isVertical;
	private Point location;

	/**
	 * Construct a new door
	 * 
	 * @param room
	 *            room the door leads to
	 * @param location
	 *            the location of the door
	 * @param isVertical
	 *            whether the door can be accessed from left/right (true) or
	 *            top/bottom (false)
	 */
	public Door(Room room, Point location, Boolean isVertical) {
		this.room = room;
		this.location = location;
		this.isVertical = isVertical;
	}

	/**
	 * Returns the room this door leads to
	 */
	public Room getRoom() {
		return this.room;
	}

	/**
	 * Returns true if the door can be accessed from left/right, false if it can
	 * be accessed from top/bottom
	 */
	public boolean isVertical() {
		return isVertical;
	}

	/**
	 * Returns the location of the door on the board
	 */
	public Point getLocation() {
		return this.location;
	}

	/**
	 * Returns the point just outside this door (closest corridor tile)
	 */
	public Point getPointBeside() {
		Room.BoundingBox boundingBox = room.getBoundingBox();
		if (isVertical) {
			if (location.x == boundingBox.getMinX()) {
				return new Point(location.x - 1, location.y);
			}
			else {
				return new Point(location.x + 1, location.y);
			}
		}
		else {
			if (location.y == boundingBox.getMinY()) {
				return new Point(location.x, location.y - 1);
			}
			else {
				return new Point(location.x, location.y + 1);
			}
		}
	}
}
