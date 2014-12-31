package kandiru.netrunner;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

public class TestDownload {

	
	@Test
	public void testDownload() throws IOException{
		OCTGNXMLParser parser = new OCTGNXMLParser();
		parser.gameDatabasePath = "src/test/resources/";
		parser.debug=true;
		List<File> sets=parser.getSets();
		System.out.println(sets);
		Security sec=new DummySecurity();
		for (File set : sets) {
			parser.parseSet(set, sec);
		}
	}
}
