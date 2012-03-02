import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

// ATMSystem

/* 
 * Encompasses the entire architecture of the ATM System
 */


public class ATMSystem {

	
	private static BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
	private static int numATMs;
	
	public static void main(String args[]) throws InterruptedException {
		requestConfig();
		ModelConstructor mc = new ModelConstructor(numATMs);
		
		System.out.println("SIMULATION STARTED:");
		mc.startSimulation();
	
		
		/*      |-----|==atmChannelOut=>
		 *      | ATM |  
		 *      |-----|<=atomChannelIn==
		 */
	/*	Channel atmChannelIn, atmChannelOut;
		atmChannelIn = new Channel();
		atmChannelOut = new Channel();
		Thread atm = new Thread(new ATMMachine(0, atmChannelIn, atmChannelOut));
		
	*/	
		/*
		 * =atmChannelOut==>|---------|<==cloudLChannelOut==
		 *                  | Bad Net |
		 * <==atmChannelIn==|---------|==cloudLChannelIn==>
		 */
		
		/*Channel cloudLeftChannelIn, cloudLeftChannelOut;
		cloudLeftChannelIn = new Channel();
		cloudLeftChannelOut = new Channel();
		BadNetwork badNetwork1 = new BadNetwork(atmChannelOut, atmChannelIn, cloudLeftChannelOut, cloudLeftChannelIn);
		Thread bnetwork1 = new Thread(badNetwork1);
		*/
		
		/* 
		 *   =cloudLChannelIn==>|-------|==cloudRChannelOut==>
		 *                      | Cloud |
		 * <==cloudLChannelOut==|-------|<==cloudRChannelIn===
		 * 
		 */
		/*
		Channel cloudRightChannelIn, cloudRightChannelOut;
		cloudRightChannelIn = new Channel();
		cloudRightChannelOut = new Channel();
		Thread cloud = new Thread(new Cloud(cloudLeftChannelIn, cloudLeftChannelOut, cloudRightChannelIn, cloudRightChannelOut));
		*/	
		
		/*
		 * =cloudRChannelOut==>|---------|<==dbLChannelOut==
		 *                     | Bad Net |
		 * <==cloudRChannelIn==|---------|==dbLChannelIn==>
		 */
		/*Channel dbLeftChannelOut, dbLeftChannelIn;
		dbLeftChannelIn = new Channel();
		dbLeftChannelOut = new Channel();
		BadNetwork badNetwork2 = new BadNetwork(cloudRightChannelOut, cloudRightChannelIn, dbLeftChannelOut, dbLeftChannelIn);
		Thread bnetwork2 = new Thread(badNetwork2);
		*/
		/*
		 * <=dbLChannelOut==|----|
		 *                  | DB |
		 *  ==dbLChannelIn=>|----|
		 */
		
		//Thread database = new Thread(new Database(dbLeftChannelIn, dbLeftChannelOut));
		
		/*** Start System ***/
		/*
		bnetwork1.start();
		bnetwork2.start();
		database.start();
		cloud.start();
		atm.start();
		*/
				
	}
	
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
		
	}
	
}
