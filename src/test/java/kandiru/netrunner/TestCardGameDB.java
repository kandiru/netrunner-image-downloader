package kandiru.netrunner;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

public class TestCardGameDB {

	@Test
	public void testCardGameDB() throws MalformedURLException {
		CardgameDB db = new CardgameDB();
		Map<String, URL> map = db.downloadDB();
		System.out.println(map);
		System.out.println(map.size());
		Assert.assertTrue(map.size()>540);
	}

	@Test
	public void testCardGameGetString() throws MalformedURLException {
		CardgameDB db = new CardgameDB();
		System.out.println(db.getDLRURL());
		Assert.assertTrue(db.getDLRURL().startsWith("http://www.cardgamedb.com/deckbuilders/androidnetrunner/database/anjson-cgdb-adn"));
	}
}
