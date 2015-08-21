package test;

import static org.junit.Assert.*;

import java.awt.Point;
import java.io.File;
import java.util.Arrays;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cluedo.game.Board;
import cluedo.game.Board.Direction;
import cluedo.game.Board.UnableToMoveException;
import cluedo.game.Door;
import cluedo.game.Player;
import cluedo.game.objects.Room;
import cluedo.loader.Loader;

public class BoardTests {

	@After
	public void tearDown() {
		new File("test.txt").delete();
	}

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void testMoveOutsideBoardFails() throws UnableToMoveException {
		expectedException.expect(UnableToMoveException.class);
		expectedException.expectMessage("outside the board");
		Loader loader = LoaderTests.getTestLoader();
		testMoveFromStart(loader, new Direction[] { Direction.LEFT }, null, null);
	}

	@Test
	public void testMoveThroughWallFails() throws UnableToMoveException {
		expectedException.expect(UnableToMoveException.class);
		expectedException.expectMessage("through a wall");
		Loader loader = LoaderTests.getTestLoader();
		testMoveFromStart(loader, new Direction[] { Direction.DOWN }, null, null);
	}

	@Test
	public void testMoveThroughWrongDoorFails() throws UnableToMoveException {
		expectedException.expect(UnableToMoveException.class);
		expectedException.expectMessage("not in that room");
		Loader loader = LoaderTests.getTestLoader();
		Room room = loader.getRooms().values().iterator().next();
		testMoveFromStart(loader, new Direction[] { Direction.RIGHT },
				room.getDoor(0), new Direction[] { Direction.RIGHT });
	}

	@Test
	public void testMoveThroughDoorWrongWayFails() throws UnableToMoveException {
		expectedException.expect(UnableToMoveException.class);
		expectedException.expectMessage("can't enter this door that way");
		Loader loader = LoaderTests.getTestLoader();
		testMoveFromStart(loader, new Direction[] { Direction.RIGHT, Direction.RIGHT, Direction.UP }, null, null);
	}

	@Test
	public void testCanMoveThroughCorridor() throws UnableToMoveException {
		Loader loader = LoaderTests.getTestLoader();
		Board board = new Board(loader);
		Player player = new Player("Test", loader.getSuspects().values().iterator().next());

		board.addPlayer(player);
		board.movePlayer(player, Arrays.asList(new Direction[] {
				Direction.RIGHT, Direction.RIGHT, Direction.DOWN
		}), null);

		assertTrue(board.isCorridor(board.getPlayerLocation(player)));
	}

	@Test
	public void testCanMoveIntoRoom() throws UnableToMoveException {
		Loader loader = LoaderTests.getTestLoader();
		Player player = testMoveFromStart(loader, new Direction[] { Direction.UP }, null, null);
		assertNotNull(player.getRoom());
	}

	@Test
	public void testCanExitRoom() throws UnableToMoveException {
		Loader loader = LoaderTests.getTestLoader();
		Room room = loader.getRooms().values().iterator().next();
		Door exitDoor = room.getDoor(0);

		Player player = testMoveFromStart(loader,
				new Direction[] { Direction.UP }, exitDoor, new Direction[] { Direction.DOWN });
		assertNull(player.getRoom());
	}

	private Player testMoveFromStart(Loader loader, Direction[] before, Door door, Direction[] after)
				throws Board.UnableToMoveException {
		Board board = new Board(loader);
		Player player = new Player("Test", loader.getSuspects().values().iterator().next());
		board.addPlayer(player);

		board.movePlayer(player, Arrays.asList(before), null);

		if (door != null && after != null) {
			board.movePlayer(player, Arrays.asList(after), door);
		}
		return player;
	}

}
