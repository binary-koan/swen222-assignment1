package cluedo.game;

import java.awt.Point;

import cluedo.game.objects.Room;

public class Door {

	private Room room;
	private boolean isVertical;
	private Point location;
	private int number;

	public Door(Room room, Boolean isVertical){
		this.room = room;
		this.isVertical = isVertical;
		room.addDoor(this);						// /S/Just to make it easier to get doors for drawing individual rooms
	}

	public Room getRoom(){
		return this.room;
	}

	public Point getLocation(){
		return this.location;
	}

	public boolean isVertical() {
		return isVertical;
	}

}
