package kandiru.netrunner;

import java.io.File;

public class DummySecurity extends Security {

	public boolean doSecurity(File set){
		return true;
	}
}
