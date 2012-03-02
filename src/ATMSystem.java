// ATMSystem

/* 
 * Encompasses the entire architecture of the ATM System
 */


public class ATMSystem {

	public static void main(String args[]) throws InterruptedException {
		
		ModelConstructor mc = new ModelConstructor(2);
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
}
