package kandiru.netrunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class MessageOfTheDay {
	static String path = "http://pastebin.com/raw.php?i=rhELL3XY";

	public static String getMessage() {
		StringBuilder bob=new StringBuilder();
		try {
			URL url = new URL(path);
			url.openConnection();
			InputStream reader = url.openStream();
			BufferedReader buff = new BufferedReader(new InputStreamReader(reader));
			for (String line = buff.readLine(); line != null; line = buff.readLine()) {
				bob.append(line);
				bob.append('\n');
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bob.toString();
	}
}
