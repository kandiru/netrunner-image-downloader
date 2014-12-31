package kandiru.netrunner;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextPane;

public class MainGui {

	private JFrame frame;
	JTextPane console;
	OCTGNXMLParser parser;
	Logger logger;
	Map<JCheckBox, File> setsCheckBoxes;
	JPanel center;
	JCheckBox chckbxAllSets;
	static boolean test=false;
	JCheckBox chckbxOverwrite;
	Thread downloadThread;
	JButton btnDownloadAll;
	
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		if(args.length>1 && args[0].equals("test")){
			test=true;
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainGui window = new MainGui(test);
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * @throws MalformedURLException 
	 * @throws IOException 
	 */
	public MainGui(final boolean test) throws MalformedURLException {
		initialize();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					parser = new OCTGNXMLParser(logger);
				} catch (MalformedURLException e) {
					logger.logLine("Error trying to parse XML");
					logger.logLine("Please run me from your gameDatabase folder");
					e.printStackTrace();
				}
				if(test==true){
					parser.gameDatabasePath = "src/test/resources/";
				}
				addSets(parser.getSets());
				
			}
		}).start();
		logger.logLine(MessageOfTheDay.getMessage());
	}

	private void addSets(List<File> sets) {
		for(File set:sets){
			String name=parser.getNameOfSet(set);
			JCheckBox check = new JCheckBox(name);
			setsCheckBoxes.put(check, set);
			try {
				if(!parser.testSet(set)){
					check.setEnabled(false);
				}
			} catch (IOException e) {
				logger.logLine("Problem with set "+set.getName()+":"+name);
			}
			if(name.equals("Markers")||name.equals("Promos")){
				check.setEnabled(false);
			}
			center.add(check);
		}
		
	}


	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 750, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		btnDownloadAll = new JButton("Download Selected");
		frame.getContentPane().add(btnDownloadAll, BorderLayout.SOUTH);
		btnDownloadAll.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent event) {
				downloadThread=new Thread(new Runnable() {
					
					@Override
					public void run() {
						btnDownloadAll.setEnabled(false);
						btnDownloadAll.setText("Download in progress");
						logger.clear("Downloading:");
					for(Entry<JCheckBox, File> entry:setsCheckBoxes.entrySet()){
						if(entry.getKey().isEnabled() && entry.getKey().isSelected()){
							try {
								parser.parseSet(entry.getValue(), new DummySecurity());
							} catch (IOException e) {
								logger.logLine("Problem downloading "+entry.getKey().getText());
								e.printStackTrace();
							}
						}
					}
						logger.logLine("...finished");
						btnDownloadAll.setEnabled(true);
						btnDownloadAll.setText("Download Selected");
					}
				});
				downloadThread.start();
			}
		});
		
		JTextPane txtpnConsole = new JTextPane();
		txtpnConsole.setText("Console");
		console=txtpnConsole;
		frame.getContentPane().add(txtpnConsole, BorderLayout.EAST);
		logger = new Logger(console);
		
		JPanel center = new JPanel();
		this.center=center;
		frame.getContentPane().add(center, BorderLayout.CENTER);
		
		chckbxOverwrite = new JCheckBox("Overwrite?");
		frame.getContentPane().add(chckbxOverwrite, BorderLayout.NORTH);
		chckbxOverwrite.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent event) {
				parser.overwrite=chckbxOverwrite.isSelected();
				
			}
		});
		
		setsCheckBoxes = new LinkedHashMap<JCheckBox, File>(20);
		center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
		chckbxAllSets = new JCheckBox("All Sets");
		chckbxAllSets.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent event) {
				for(JCheckBox box:setsCheckBoxes.keySet()){
					if(!box.isEnabled()){
						continue;
					}
					box.setSelected(chckbxAllSets.isSelected());		
				}
			}
		});
		center.add(chckbxAllSets);
	}

}
