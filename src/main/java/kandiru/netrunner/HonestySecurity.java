package kandiru.netrunner;

import java.io.File;
import java.io.IOException;

public class HonestySecurity extends Security {
	
	
	public boolean doSecurity(File set) throws IOException {
		System.out.println("Do you own this set of cards y/n? Caprice is watching you...");
		if (getResponse().equalsIgnoreCase("y")) {
			System.out.println("Response confirmed, access granted");
			return super.doSecurity(set);
		} else {
			System.out.println("jacking-out");
			return false;
		}
	}
}
