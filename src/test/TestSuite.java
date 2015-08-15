package test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   BoardTests.class,
   GameTests.class,
   LoaderTests.class,
   RoomTests.class
})
public class TestSuite { }
