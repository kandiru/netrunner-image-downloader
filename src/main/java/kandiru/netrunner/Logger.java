package kandiru.netrunner;

import javax.swing.JTextPane;

public class Logger {
	JTextPane pane;
	int MAX_LINES = 30;
	int count = 0;

	public Logger(JTextPane pane) {
		this.pane = pane;
	}

	public void clear(String text){
		count=0;
		pane.setText(text+"\n");
	}
	
	public void logLine(String string) {
		if (pane == null) {
			return;
		}
		String current = this.pane.getText();
		if (count < MAX_LINES) {
			pane.setText(current + '\n' + string);
			count++;
		} else {
			current=current.substring(current.indexOf("\n")+1);
			pane.setText(current+ '\n' + string);
		}
	}
}
