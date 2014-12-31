package kandiru.netrunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JTextPane;

import net.sf.xomlite.Document;
import net.sf.xomlite.Element;
import net.sf.xomlite.XName;
import net.sf.xomlite.io.Builder;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.xml.sax.SAXException;

public class OCTGNXMLParser {
	String baseURL = "http://www.cardgamedb.com/forums/uploads/an/med_";
	String netrunnerUUID = "0f38e453-26df-4c04-9d67-6d43de939c77";
	String gameDatabasePath = ".";
	XName name = XName.get("name");
	XName id = XName.get("id");
	CardgameDB db = null;
	boolean debug = false;
	boolean overwrite = false;
	Security sec;
	Logger logger;

	public boolean testSet(File set) throws IOException {
		File[] setxml = set.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.equals("set.xml"))
					return true;
				return false;
			}
		});
		if (setxml.length != 1) {
			logger.logLine(set.getName() + " is not a real set");
			return false;
		}
		Builder builder = new Builder();
		Document doc;
		try {
			doc = builder.parse(setxml[0]);
		} catch (IOException e) {
			logger.logLine("Could not read " + set.getName());
			return false;
		} catch (SAXException e) {
			logger.logLine("Could not parse " + set.getName());
			return false;
		}
		String setName = doc.getRootElement().getAttribute(name).getValue();

		Map<String, URL> cardMap = db.downloadDB();
		Element cards = doc.getRootElement().getChildElements().get(0);
		List<Element> cardlist = cards.getChildElements(XName.get("card"));
		int count = 0;
		for (Element card : cardlist) {
			String cardname = StringEscapeUtils.unescapeHtml4(card.getAttribute(name).getValue()).toLowerCase();
			String cardid = card.getAttribute(id).getValue();
			List<Element> properties = card.getChildElements(XName.get("property"));
			String subtitle = "";
			for (Element element : properties) {
				if (element.getAttribute(name).getValue().equals("Subtitle")) {
					subtitle = element.getAttribute(XName.get("value")).getValue();
				}
			}
			URL url = getURLFromName(cardname, subtitle, cardMap);
			if (url != null && url.toString().length() > 30) {
				count++;
			}
		}
		if (count > 19) {
			return true;
		}
		logger.logLine(setName + " images missing");
		return false;
	}

	public void parseSet(File set, Security sec) throws IOException {
		File[] setxml = set.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.equals("set.xml"))
					return true;
				return false;
			}
		});
		if (setxml.length != 1) {
			return;
		}
		this.sec = sec;
		Builder builder = new Builder();
		Document doc;
		try {
			doc = builder.parse(setxml[0]);
		} catch (IOException e) {
			System.err.println("Could not read " + set.getName());
			return;
		} catch (SAXException e) {
			System.err.println("Could not parse " + set.getName());
			return;
		}
		String setName = doc.getRootElement().getAttribute(name).getValue();

		if ("Promos".equals(setName)) {
			return;
		} else if ("Markers".equals(setName)) {
			return;
		}
		System.out.println("---------" + setName + "---" + set.getName());
		logger.logLine("---------" + setName + "---" + set.getName());
		sec.setActive(true);
		Map<String, URL> cardMap = db.downloadDB();
		Element cards = doc.getRootElement().getChildElements().get(0);
		List<Element> cardlist = cards.getChildElements(XName.get("card"));
		for (Element card : cardlist) {
			String cardname = StringEscapeUtils.unescapeHtml4(card.getAttribute(name).getValue()).toLowerCase();
			String cardid = card.getAttribute(id).getValue();
			List<Element> properties = card.getChildElements(XName.get("property"));
			String subtitle = "";
			for (Element element : properties) {
				if (element.getAttribute(name).getValue().equals("Subtitle")) {
					subtitle = element.getAttribute(XName.get("value")).getValue();
				}
			}
			checkOrDownloadCard(cardname, subtitle, cardid, set, setName, cardMap);
			if (sec.isActive()) {
				return;
			}
		}
	}

	private void checkOrDownloadCard(String cardname, String subtitle, String cardid, File set, String setName, Map<String, URL> cardMap) {
		File dest = new File(set.getAbsolutePath().replace("GameDatabase", "ImageDatabase") + File.separator + "Cards" + File.separator + cardid + ".jpg");
		if (dest.exists()) {
			sec.setActive(false);
			if (!overwrite) {
				return;
			}
		}
		if (sec.isActive()) {
			try {
				if (!sec.doSecurity(set)) {
					return;
				} else {
					sec.setActive(false);
				}
			} catch (IOException e) {
			}
		}
		dest.getParentFile().mkdirs();
		URL url = getURLFromName(cardname, subtitle, cardMap);
		System.out.println("Downloading " + cardname);
		logger.logLine("Downloading " + cardname);
		try {
			url.openConnection();
			InputStream reader = url.openStream();
			FileOutputStream writer = new FileOutputStream(dest);
			byte[] buffer = new byte[153600];
			int bytesRead = 0;
			while ((bytesRead = reader.read(buffer)) > 0) {
				writer.write(buffer, 0, bytesRead);
				buffer = new byte[153600];
			}
			writer.close();
		} catch (IOException e) {
			System.err.println("ERROR downloading " + url);
			logger.logLine("ERROR downloading " + url + " for " + cardname);
			e.printStackTrace();
		}
	}

	private URL getURLFromName(String cardname, String subtitle, Map<String, URL> cardMap) {
		URL url = null;
		if (subtitle.equals("")) {
			url = cardMap.get(cardname.toLowerCase());
		} else {
			url = cardMap.get((cardname + ": " + subtitle).toLowerCase());
			if (url == null) {
				url = cardMap.get(cardname.toLowerCase());
			}
		}
		if (url == null) {
			String bestkey = "Card not found";
			Integer distance = 5;
			for (String key : cardMap.keySet()) {
				int dist;
				if (subtitle.equals("")) {
					dist = StringUtils.getLevenshteinDistance(key, cardname);
				} else {
					dist = StringUtils.getLevenshteinDistance(key, cardname + ": " + subtitle);
				}
				if (dist < distance) {
					bestkey = key;
					distance = dist;
				}
			}
			url = cardMap.get(bestkey);
			if (url == null) {
				if(debug)System.out.println(cardMap.keySet());
			}
		}
		return url;
	}

	private void checkOrDownloadCard(String cardname, String subtitle, String cardid, File set, String setname) throws MalformedURLException {
		File dest = new File(set.getAbsolutePath().replace("GameDatabase", "ImageDatabase") + File.separator + "Cards" + File.separator + cardid + ".jpg");
		if (dest.exists()) {
			// System.out.println("Found " + cardname);
			return;
		}
		dest.getParentFile().mkdirs();
		URL url = getURLFromName(cardname, subtitle, setname);
		if (debug) {
			System.out.println(url);
			return;
		}
		System.out.println("Downloading " + cardname);
		try {
			url.openConnection();
			InputStream reader = url.openStream();
			FileOutputStream writer = new FileOutputStream(dest);
			byte[] buffer = new byte[153600];
			int bytesRead = 0;
			while ((bytesRead = reader.read(buffer)) > 0) {
				writer.write(buffer, 0, bytesRead);
				buffer = new byte[153600];
			}
			writer.close();
		} catch (IOException e) {
			System.err.println("ERROR downloading " + url);
			e.printStackTrace();
		}

	}

	public URL getURLFromName(String cardname, String subtitle, String setname) throws MalformedURLException {
		if (subtitle.length() != 0)
			subtitle = "-" + subtitle;
		String newName;
		if (cardname.endsWith(".")) {
			newName = cardname.substring(0, cardname.length() - 1);
		}
		if ("Creation & Control".equals(setname)) {
			setname = "creation-and-control";
		}
		newName = cardname + subtitle + "-" + setname;
		newName = newName.replace('_', '-');
		newName = newName.replace(' ', '-');
		newName = newName.replace('&', '-');
		newName = newName.replaceAll("Mr\\.", "mr");
		newName = newName.replaceAll("'", "");
		newName = newName.replaceAll("\\.", "-");
		newName = newName.replaceAll("\"", "");
		newName = newName.replaceAll("!", "");
		newName = newName.toLowerCase();
		URL url = new URL(baseURL + newName + ".png");
		return url;
	}

	public String getNameOfSet(File set) {
		File[] setxml = set.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.equals("set.xml"))
					return true;
				return false;
			}
		});
		if (setxml.length != 1) {
			return "Error";
		}
		Builder builder = new Builder();
		Document doc;
		try {
			doc = builder.parse(setxml[0]);
		} catch (IOException e) {
			System.err.println("Could not read " + set.getName());
			return set.getName();
		} catch (SAXException e) {
			System.err.println("Could not parse " + set.getName());
			return set.getName();
		}
		String setName = doc.getRootElement().getAttribute(name).getValue();
		return setName;
	}

	public List<File> getSets() {
		String setspath = gameDatabasePath + File.separator + netrunnerUUID + File.separator + "Sets";
		File sets = new File(setspath);
		if (!sets.isDirectory() || !sets.exists()) {
			System.err.println(setspath + " is not present");
			System.out.println("Run me from inside your GameDatabase folder");
			if (logger != null) {
				logger.logLine(setspath + " is not present");
				logger.logLine("Run me from inside your GameDatabase folder");
				return new ArrayList<File>();
			}
				System.exit(1);
		}
		List<File> setList = new ArrayList<File>();
		for (File set : sets.listFiles()) {
			if (set.isDirectory()) {
				setList.add(set);
			}
		}
		return setList;
	}

	public String getResponse() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String input = br.readLine();
		return input;
	}

	public OCTGNXMLParser() throws MalformedURLException {
		logger = new Logger(null);
		db = new CardgameDB();
	}

	public OCTGNXMLParser(Logger logger) throws MalformedURLException {
		db = new CardgameDB(logger);
		this.logger = logger;
	}

	public void setOverwrite() {
		this.overwrite = true;
	}

	public static void main(String[] argsv) throws IOException {
		OCTGNXMLParser parser = new OCTGNXMLParser();
		parser.gameDatabasePath = ".";
		if (argsv.length == 1) {
			if (argsv[0].equalsIgnoreCase("overwrite")) {
				parser.setOverwrite();
			}
		}
		List<File> sets = parser.getSets();
		System.out.println(sets.size() + " sets found");
		Security sec = new HonestySecurity();
		// if (sec.getQuestions().size() > 1) {
		// System.out.println("Warning::Approaching " +
		// sec.getQuestions().size() + " ICE!!!");
		// }
		for (File set : sets) {
			parser.parseSet(set, sec);
		}
	}
}
