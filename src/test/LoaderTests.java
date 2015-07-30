package test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cluedo.loader.Loader;
import cluedo.loader.Loader.SyntaxException;

public class LoaderTests {
	private static final String TEST_ROOMS = "rooms:\n  A: Some Room\n";
	private static final String TEST_SUSPECTS = "suspects:\n  a: First Person [white]\n";
	private static final String TEST_WEAPONS = "weapons:\n  - Deadly Weapon\n";
	private static final String TEST_BOARD = "-----\nAAA. |\nAAA..|\n_A/..|\n.... |\n ... |\n-----";
	private static final String TEST_FULL_CONTENT = "---\n" + TEST_ROOMS + TEST_SUSPECTS + TEST_WEAPONS + TEST_BOARD;

	@After
	public void tearDown() {
		new File("test.txt").delete();
	}
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void testFailsWithNoFrontmatter() throws SyntaxException {
		expectedException.expect(SyntaxException.class);
		expectedException.expectMessage("header not found");
		testLoader("some content");
	}
	
	@Test
	public void testFailsWithNoGroupHeader() throws SyntaxException {
		expectedException.expect(SyntaxException.class);
		expectedException.expectMessage("Expected group header");
		testLoader("---\nnot a header");
	}
	
	@Test
	public void testFailsWithUnknownGroup() throws SyntaxException {
		expectedException.expect(SyntaxException.class);
		expectedException.expectMessage("Unrecognized group");
		testLoader("---\nnotheader:");
	}
	
	@Test
	public void testFailsWithoutRooms() throws SyntaxException {
		expectedException.expect(SyntaxException.class);
		expectedException.expectMessage("rooms definition");
		testLoader("---\n" + TEST_SUSPECTS + TEST_WEAPONS + TEST_BOARD);
	}
	
	@Test
	public void testFailsWithoutSuspects() throws SyntaxException {
		expectedException.expect(SyntaxException.class);
		expectedException.expectMessage("suspects definition");
		testLoader("---\n" + TEST_ROOMS + TEST_WEAPONS + TEST_BOARD);
	}
	
	@Test
	public void testFailsWithoutWeapons() throws SyntaxException {
		expectedException.expect(SyntaxException.class);
		expectedException.expectMessage("weapons definition");
		testLoader("---\n" + TEST_ROOMS + TEST_SUSPECTS + TEST_BOARD);
	}
	
	@Test
	public void testFailsWithoutBoard() throws SyntaxException {
		expectedException.expect(SyntaxException.class);
		expectedException.expectMessage("board definition");
		testLoader("---\n" + TEST_ROOMS + TEST_SUSPECTS + TEST_WEAPONS);
	}

	private Loader testLoader(String string) throws SyntaxException {
		try {
			PrintWriter out = new PrintWriter("test.txt");
			out.println(string);
			out.close();
			
			return new Loader("test.txt");
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
