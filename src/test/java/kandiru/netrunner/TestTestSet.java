package kandiru.netrunner;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import junit.framework.Assert;

import org.junit.Test;

public class TestTestSet {

	@Test
	public void testTestSet() throws IOException {
		OCTGNXMLParser parser = new OCTGNXMLParser();
		parser.gameDatabasePath = "src/test/resources/";
		//parser.debug=true;
		Assert.assertTrue(parser.testSet(new File("src/test/resources/0f38e453-26df-4c04-9d67-6d43de939c77/Sets/975fe9ee-7c7c-4d05-bd35-d159f92a1294")));
		//Assert.assertTrue(parser.testSet(new File("src/test/resources/0f38e453-26df-4c04-9d67-6d43de939c77/Sets/006012c6-2fc1-49b3-835d-33c378a97b8c")));
	}
	
}
