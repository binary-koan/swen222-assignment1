package test;

import static org.junit.Assert.*;

import java.awt.Point;

import org.junit.Test;

import cluedo.game.objects.Room;

public class RoomTests {
	@Test
	public void testBoundingBox() {
		Room room = mockRoom(new Point[] {
				new Point(1, 2), new Point(1, 3), new Point(4, 2), new Point(2, 3)
		});
		assertEquals(1, room.getBoundingBox().getMinX());
		assertEquals(4, room.getBoundingBox().getMaxX());
		assertEquals(2, room.getBoundingBox().getMinY());
		assertEquals(3, room.getBoundingBox().getMaxY());
	}

	@Test
	public void testCenterPoint() {
		Room room = mockRoom(new Point[] {
				new Point(1, 2), new Point(1, 3), new Point(4, 2), new Point(2, 3)
		});
		assertEquals(3, room.getCenterPoint().x);
		assertEquals(3, room.getCenterPoint().y);
	}

	private Room mockRoom(Point[] points) {
		Room room = new Room("Test room");
		for (Point point : points) {
			room.addPoint(point.x, point.y);
		}
		return room;
	}
}
