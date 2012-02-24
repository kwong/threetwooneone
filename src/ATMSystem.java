// ATMSystem

/* 
 * Encompasses the entire architecture of the ATM System
 */


public class ATMSystem {

	public static void main(String args[]) throws InterruptedException {
		
	
		
		/*		|-----|==atmChannelOut=>
		 *      | ATM |  
		 *      |-----|<=atomChannelIn==
		 */
		Channel atmChannelIn, atmChannelOut;
		atmChannelIn = new Channel();
		atmChannelOut = new Channel();
		Thread atm = new Thread(new ATMMachine(atmChannelIn, atmChannelOut));
		
		
		/*
		 * =atmChannelOut==>|---------|==cloudLChannelOut==>
		 *    		 		| Bad Net |
		 * <==atmChannelIn==|---------|<==cloudLChannelIn==
		 */
		
		Channel cloudLeftChannelIn, cloudLeftChannelOut;
		cloudLeftChannelIn = new Channel();
		cloudLeftChannelOut = new Channel();
		BadNetwork badNetwork1 = new BadNetwork(atmChannelOut, atmChannelIn, cloudLeftChannelOut, cloudLeftChannelIn);
		Thread network = new Thread(badNetwork1);
		
		
		/* 
		 * =cloudLChannelOut==>|-------|==cloudRChannelOut==>
		 * 					   | Cloud |
		 * <==cloudLChannelIn==|-------|<==cloudRChannelIn===
		 * 
		 */
		
		Channel cloudRightChannelIn, cloudRightChannelOut;
		cloudRightChannelIn = new Channel();
		cloudRightChannelOut = new Channel();
		Thread cloud = new Thread(new Cloud(cloudLeftChannelOut, cloudLeftChannelIn, cloudRightChannelOut, cloudRightChannelIn));
			
		
		/*** Start System ***/
		atm.start();
		network.start();
		cloud.start();
		
		
		
		
		
	}
}
