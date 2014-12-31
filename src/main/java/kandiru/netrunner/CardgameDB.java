package kandiru.netrunner;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CardgameDB {

	String mainpage = "http://www.cardgamedb.com/index.php/androidnetrunner/android-netrunner-deckbuilder";
	String searchString = "http://www.cardgamedb.com/deckbuilders/androidnetrunner/database/";

	String dburl = "http://www.cardgamedb.com/deckbuilders/androidnetrunner/database/anjson-cgdb-adn19.js";
	String baseurl = "http://www.cardgamedb.com/forums/uploads/an/med_";
	Map<String, URL> dataMap = null;
	Logger logger;
	
	public CardgameDB() throws MalformedURLException{
		init();
	}
	
	public CardgameDB(Logger logger) throws MalformedURLException{
		this.logger=logger;
		init();
	}
	
	private void init() throws MalformedURLException{
		dburl=getDLRURL();
	}
	
	protected String getDLRURL() throws MalformedURLException {
		URL url = new URL(mainpage);
		String result=null;
		try {
			url.openConnection();
			InputStream reader = url.openStream();
			BufferedReader buff = new BufferedReader(new InputStreamReader(reader));
			StringBuilder bob = new StringBuilder();
			for (String line = buff.readLine(); line != null; line = buff.readLine()) {
				bob.append(line);
			}
			String working=bob.toString().substring(bob.toString().indexOf(searchString));
			result=working.substring(0, working.indexOf(".jgz"))+".js";
		}catch (Exception e) {
			System.err.println("ERROR downloading " + url);
			e.printStackTrace();
			return null;
		}
		return result;
	}

	public Map<String, URL> downloadDB() throws MalformedURLException {
		if (dataMap != null) {
			return dataMap;
		}
		URL url = new URL(dburl);
		try {
			url.openConnection();
			InputStream reader = url.openStream();
			BufferedReader buff = new BufferedReader(new InputStreamReader(reader));
			StringBuilder bob = new StringBuilder();
			for (String line = buff.readLine(); line != null; line = buff.readLine()) {
				bob.append(line);
			}
			String jsonstring = bob.toString();
			for (int x = 0; x < bob.toString().length(); x++) {
				if (bob.toString().charAt(x) == '[') {
					jsonstring = bob.toString().substring(x - 1);
					break;
				}
			}
			JSONArray json = new JSONArray(jsonstring);
			this.dataMap = parseJsonToMap(json);
			return this.dataMap;
		} catch (Exception e) {
			System.err.println("ERROR downloading " + url);
			e.printStackTrace();
			return null;
		}
	}

	private Map<String, URL> parseJsonToMap(JSONArray json) throws MalformedURLException, JSONException {
		Map<String, URL> cardMap = new HashMap<String, URL>(json.length());
		for (int x = 0; x < json.length(); x++) {
			JSONObject card = json.getJSONObject(x);
			String name = StringEscapeUtils.unescapeHtml4(card.getString("name")).toLowerCase();
			name=name.replace("â€œ", "\"");
			String path = card.getString("img");
			if (path.equals("")) {
				path = card.getString("furl");
			}
			URL url = new URL(baseurl + path + ".png");
			cardMap.put(name, url);
		}
		return cardMap;
	}
}
