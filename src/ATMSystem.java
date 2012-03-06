import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

// ATMSystem

/* 
 * Encompasses the entire architecture of the ATM System
 */


public class ATMSystem {

	
	private static BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
	private static int numATMs = 1;
	private static JFrame frame;
	private static JPanel mainPane;
	
	private static ModelConstructor mc;
	
	private static void init() {
		frame = new JFrame("CS3211 Group 5 - ATM Simulation");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.setVisible(true);
		frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
	}
	
	private static void createSimulationGUI() throws InterruptedException {
				
		mainPane = new JPanel();
		JPanel atmPane = new JPanel();
		JPanel net1Pane = new JPanel();
		JPanel cloudPane = new JPanel();
		JPanel net2Pane = new JPanel();
		JPanel dbPane = new JPanel();
		
		
		
		mainPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		mainPane.setLayout(new GridLayout(5,1));
		atmPane.setLayout(new GridLayout(1,2,-2,-2));
		net1Pane.setLayout(new GridLayout(1,5,-2,-2));
		cloudPane.setLayout(new GridLayout(1,5,-2,-2));
		net2Pane.setLayout(new GridLayout(1,5,-2,-2));
		dbPane.setLayout(new GridLayout(1,1,2,2));
		//dbPane.setPreferredSize(Config.DB_SIZE);	
		//dbPane.setPreferredSize(Config.DB_SIZE);
		
		
		for(ATMMachine atm: mc.atmMachines)
			atmPane.add(atm);
			
		for(BadNetwork bn: mc.badNetworks1)
			net1Pane.add(bn);
		
		for(Cloud cloud: mc.cloudProcessors)
			cloudPane.add(cloud);
		
		for(BadNetwork bn: mc.badNetworks2)
			net2Pane.add(bn);
		
		
		dbPane.add(mc.db);
		mainPane.add(atmPane);
		mainPane.add(net1Pane);
		mainPane.add(cloudPane);
		mainPane.add(net2Pane);
		mainPane.add(dbPane);
		
		
	}
	
	public static void createConfigPane() {
		/* Option Pane */
		JPanel optionPane = new JPanel();
		JButton confirmBtn = new JButton("Create");
		JLabel optionLbl = new JLabel("Select number of ATMs");
		SpinnerModel model = new SpinnerNumberModel (1, 1, 10, 1);
		final JSpinner spinner= new JSpinner(model);
		
		optionPane.add(optionLbl);
		optionPane.add(spinner);
		optionPane.add(confirmBtn);
		
		
		 ChangeListener listener = new ChangeListener() {
             public void stateChanged(ChangeEvent e) {
                numATMs = (Integer) spinner.getValue();
             }
          };
          spinner.addChangeListener(listener);
          
          
        frame.setContentPane(optionPane);
        frame.pack();
        frame.setVisible(true);
        
        
        confirmBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					mc = (new ModelConstructor(numATMs));
					mc.startSimulation(); 
					createSimulationGUI();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException ioe) {
					// TODO Auto-generated catch block
					ioe.printStackTrace();
				}
				frame.setPreferredSize(new Dimension(numATMs*240, 600));
				frame.setContentPane(mainPane);
				frame.pack();        //Resizes the window to its natural size.
			    frame.setVisible(true);
			}
		});
	}
	
	
	public static void main(String args[]) throws InterruptedException {
		//requestConfig();
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				init();
				createConfigPane();
				System.out.println("SIMULATION STARTED:");
			}
		});
		
				
	}
	
	/*
	private static int readInput(BufferedReader input) {
		System.out.print("  > ");
        
        int cmd = -1;
        try {
            cmd = Integer.parseInt(input.readLine().trim());
        } catch (NumberFormatException nfe) {
        	System.err.println("Input Error: Must be an integer");
        } catch (IOException e) {}
        
        return cmd;
	}
	
	private static void requestConfig() {
		//System.out.println("Enter number of ATM Users");
		//numUsers = readInput(input);
		System.out.println("Enter number of ATMs");
		numATMs = readInput(input);
		
		System.out.println("Enter from (0-9, higher=>more chance), the likelihood of a successful messages transmission occuring");
		Config.fairnessFactor = readInput(input);
		
		System.out.println("Enter delay time (milliseconds) of event traces");
		Config.delay = readInput(input);
		
		System.out.format("Enter 0 for unique users (no duplicates)\n" +
				"      1 for random users (possible duplicates)\n" +
				"      2 for non-unique users (all atm with the same users)\n");
		Config.populationType = readInput(input);
		
	}*/
	
}
