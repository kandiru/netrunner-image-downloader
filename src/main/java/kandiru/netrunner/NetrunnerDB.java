package kandiru.netrunner;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NetrunnerDB {

	String baseurl = "http://netrunnerdb.com/";

	Map<String, String> setdata;
	JSONArray sets;

	public NetrunnerDB() throws MalformedURLException {
		init();
	}

	private void parseSets() {
		setdata = new HashMap<String, String>();
		for (int x = 0; x < sets.length(); x++) {
			JSONObject set = sets.getJSONObject(x);
			setdata.put(set.getString("name"), set.getString("code"));
		}
	}

	public Map<String, URL> getSet(String set) throws MalformedURLException {
		String bestkey = "Set Not Found";
		Integer distance = 6;
		for (String key : setdata.keySet()) {
			int dist = StringUtils.getLevenshteinDistance(key, set);
			if (dist < distance) {
				bestkey = key;
				distance = dist;
			}
		}
		URL url = new URL(baseurl+"api/set/"+setdata.get(bestkey));
		try {
			url.openConnection();
			InputStream reader = url.openStream();
			BufferedReader buff = new BufferedReader(new InputStreamReader(reader));
			StringBuilder bob = new StringBuilder();
			for (String line = buff.readLine(); line != null; line = buff.readLine()) {
				bob.append(line);
			}
			JSONArray json = new JSONArray(bob.toString());
			return parseJsonToMap(json);
		} catch (Exception e) {
			System.err.println("ERROR downloading " + url);
			e.printStackTrace();
			return null;
		}
	}

	private Map<String, URL> parseJsonToMap(JSONArray json) throws MalformedURLException, JSONException {
		Map<String,URL> cardMap=new HashMap<String, URL>(json.length());
		for(int x=0;x<json.length();x++){
			JSONObject card = json.getJSONObject(x);
			String name=StringEscapeUtils.unescapeJava(card.getString("title")).toLowerCase();
			String path=card.getString("largeimagesrc");
			if(path.length()<3){
				path=card.getString("imagesrc");
			}
			URL url=new URL(baseurl+path);
			cardMap.put(name, url);
		}
		return cardMap;
	}

	private void init() throws MalformedURLException {
		URL url = new URL(baseurl + "api/sets");
		try {
			url.openConnection();
			InputStream reader = url.openStream();
			BufferedReader buff = new BufferedReader(new InputStreamReader(reader));
			StringBuilder bob = new StringBuilder();
			for (String line = buff.readLine(); line != null; line = buff.readLine()) {
				bob.append(line);
			}
			JSONArray json = new JSONArray(bob.toString());
			sets = json;
		} catch (Exception e) {
			System.err.println("ERROR downloading " + url);
			e.printStackTrace();
		}
		parseSets();
	}
}
