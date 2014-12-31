package kandiru.netrunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Security {

	boolean active =true;
	Map<String, String> questionmap = null;

	String path = "http://pastebin.com/raw.php?i=RmLgWuLc";

	
	public boolean isActive(){
		return active;
	}
	
	public void setActive(boolean active){
		this.active=active;
	}
	
	public String getResponse() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String input=br.readLine();
		return input;
	}
	
	public Map<String, String> getQuestions() throws MalformedURLException {
		if (questionmap != null) {
			return questionmap;
		}
		Map<String, String> questions = new HashMap<String, String>();
		URL url = new URL(path);
		try {
			url.openConnection();
			InputStream reader = url.openStream();
			BufferedReader buff = new BufferedReader(new InputStreamReader(reader));
			for (String line = buff.readLine(); line != null; line = buff.readLine()) {
				int split = line.indexOf(':');
				questions.put(line.substring(0, split), line.substring(split + 1));
			}
		} catch (Exception e) {
			System.err.println("ERROR downloading " + url);
			e.printStackTrace();
		}
		this.questionmap = questions;
		return questions;
	}

	public boolean doSecurity(File set) throws IOException{
		String questionAnswear = this.getQuestions().get(set.getName());
		if (questionAnswear != null) {
			System.out.println(questionAnswear.substring(0, questionAnswear.indexOf('?'))+"?");
			if (getResponse().equalsIgnoreCase(questionAnswear.substring(questionAnswear.indexOf('?') + 1))) {
				System.out.println("Response confirmed, subroutine broken");
				return true;
			}else{
				System.out.println("Response is incorrect, jacking-out");
				return false;
			}
		} else {
			System.out.println("ICE is not rezzed");
			return true;
		}
	}
	
}
