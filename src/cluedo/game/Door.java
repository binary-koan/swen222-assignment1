package cluedo.game;

import java.awt.Point;

import cluedo.game.objects.Room;

public class Door {

	private Room room;
	private boolean isVertical;
	private Point location;


	public Door(Room room, Boolean isVertical){
		this.room = room;
		this.isVertical = isVertical;
	}
	//??
	public Door(String name) {
		// TODO Auto-generated constructor stub
	}

	public Room getRoom(){
		return this.room;
	}

	public Point getLocation(){
		return this.location;
	}

}
