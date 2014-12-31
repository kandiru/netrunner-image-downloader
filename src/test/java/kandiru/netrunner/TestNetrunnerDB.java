package kandiru.netrunner;

import static org.junit.Assert.*;

import java.net.MalformedURLException;



import org.junit.Ignore;
import org.junit.Test;

public class TestNetrunnerDB {

	@Ignore
	@Test
	public void test() throws MalformedURLException {
		NetrunnerDB db = new NetrunnerDB();
		System.out.println(db.getSet("Core"));
	}

}
