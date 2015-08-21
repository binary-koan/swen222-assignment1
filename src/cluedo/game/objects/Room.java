package cluedo.game.objects;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cluedo.game.Door;

/**
 * Represents a room on the board. This class encapsulates the location of the
 * room on the board, its properties and its current contents.
 */
public class Room implements Card {
	/**
	 * A rectangle defined by its minimum and maximum X and Y. Used to calculate
	 * (among other things) the center point of the room.
	 */
	public class BoundingBox {
		private int minX = Integer.MAX_VALUE;
		private int maxX = Integer.MIN_VALUE;
		private int minY = Integer.MAX_VALUE;
		private int maxY = Integer.MIN_VALUE;

		public int getMinX() {
			return minX;
		}

		public int getMaxX() {
			return maxX;
		}

		public int getMinY() {
			return minY;
		}

		public int getMaxY() {
			return maxY;
		}
	}

	private final String name;
	private Weapon weapon;
	private Room passageExit;
	private List<Door> doors = new ArrayList<Door>();

	private Set<Point> points = new HashSet<Point>();
	private BoundingBox boundingBox = new BoundingBox();

	/**
	 * Constructs a new room with the specified name
	 *
	 * @param name
	 *            a string to identify the room
	 */
	public Room(String name) {
		this.name = name;
	}

	/**
	 * Returns a string identifying the room, as passed to the constructor
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Returns the weapon currently in the room, or null if there isn't one
	 */
	public Weapon getWeapon() {
		return this.weapon;
	}

	/**
	 * Sets the weapon in the room to the specified weapon
	 */
	public void setWeapon(Weapon weapon) {
		this.weapon = weapon;
	}

	/**
	 * Gets the endpoint of the secret passage leading from this room, or null
	 * if this room has no passage
	 */
	public Room getPassageExit() {
		return this.passageExit;
	}

	/**
	 * Sets the endpoint of the secret passage leading from this room
	 */
	public void setPassageExit(Room room) {
		this.passageExit = room;
	}

	/**
	 * Returns the collection of doors which lead to this room
	 */
	public List<Door> getDoors() {
		return this.doors;
	}

	/**
	 * Returns a set of all points contained in the room
	 */
	public Set<Point> getPoints() {
		return this.points;
	}

	/**
	 * Returns the bounding box of the room
	 */
	public BoundingBox getBoundingBox() {
		return boundingBox;
	}

	/**
	 * Creates a door at the specified location (in board coordinates) which
	 * leads to this room
	 *
	 * @param point
	 *            location to place the door
	 * @param isVertical
	 *            true if the door can be accessed from the left or right, false
	 *            if it can be accessed from above or below
	 */
	public void addDoor(Point point, boolean isVertical) {
		doors.add(new Door(this, point, isVertical));
	}

	/**
	 * Returns the door at the specified index in the room's doors. Allows the
	 * player to specify which door to use when leaving a room
	 *
	 * @param index
	 *            the index of the door to return
	 * @return the door at the specified position, or null if there isn't one
	 */
	public Door getDoor(int index) {
		return doors.get(index);
	}

	/**
	 * Adds the point (x, y) to the collection of points this room contains. x
	 * and y should be in board (tile) coordinates
	 *
	 * @param x
	 *            x-coordinate of the point to addToLayout
	 * @param y
	 *            y-coordinate of the point to addToLayout
	 */
	public void addPoint(int x, int y) {
		points.add(new Point(x, y));

		if (x < boundingBox.minX) {
			boundingBox.minX = x;
		}
		if (x > boundingBox.maxX) {
			boundingBox.maxX = x;
		}
		if (y < boundingBox.minY) {
			boundingBox.minY = y;
		}
		if (y > boundingBox.maxY) {
			boundingBox.maxY = y;
		}
	}

	/**
	 * Returns the point at the center of the room's bounding box
	 */
	public Point2D.Float getCenterPoint() {
		float x = (boundingBox.minX + boundingBox.maxX + 1) / 2.0f;
		float y = (boundingBox.minY + boundingBox.maxY + 1) / 2.0f;
		return new Point2D.Float(x, y);
	}

	@Override
	public String toString() {
		return name;
	}

}