package cluedo.game.objects;

import java.awt.Color;
import java.awt.Point;

public class Suspect implements Card {

	private final String id;
	private final String name;
	private final Color color;
	private Point startLocation;
	private Point location;

	public Suspect(String id, String name, Color color){
		this.id = id;
		this.name = name;
		this.color = color;
	}

	public String getName() {
		return name;
	}

	public void setStartLocation(int x, int y){
		this.startLocation = new Point(x, y);
	}

	public Point getStartLocation(){
		return this.startLocation;
	}

	public void setLocation(Point point){
		this.location = point;
	}

	public Point getLocation(){
		return this.location;
	}

}
