package cluedo.game;

import java.awt.Point;

import cluedo.game.objects.Room;

public class Door {

	private Room room;
	private boolean isVertical;
	private Point location;
	private int displayNumber;

	public Door(Room room, Point location, Boolean isVertical){
		this.room = room;
		this.location = location;
		this.isVertical = isVertical;
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

	public void setDisplayNumber(int i) {
		displayNumber = i;
	}

	public int getDisplayNumber(){
		return this.displayNumber;
	}

}
