
/*
 *    --leftIn-->|============|--rightOut-->
 *               |Bad Network |
 *    <-leftOut--|============|<--rightIn---  
 *     
 */
public class BadNetwork implements Runnable {

	final private Channel leftIn_, leftOut_, rightIn_, rightOut_;


	public BadNetwork(Channel leftIn, Channel leftOut, 
			Channel rightIn, Channel rightOut) {
		leftIn_ = leftIn;
		leftOut_ = leftOut;
		rightIn_ = rightIn;
		rightOut_ = rightOut;
	}


	// Simulates bad network conditions but randomly sending 
	final private void simulate(Channel leftIn, Channel leftOut, 
			Channel rightIn, Channel rightOut) throws InterruptedException {
		int val = -1;

		while (true) {

			Message recvMsg= leftIn.listen();

			if (recvMsg.getType() == Message.Type.TIMEOUT || 
					recvMsg.getType() == Message.Type.FAILURE) {
				val = 0;
			} else {
				val = ThreadHelper.getRandom(1);
			}


			//ThreadHelper.threadMessage("BN has "+val);
			if (val < 9) { // success
				//ThreadHelper.threadMessage("BN sent "+recvMsg);
				rightOut.send(recvMsg); // Relay message

			} else if(val == 9) { // failure
				leftOut.send(new Message(Message.Type.FAILURE));
			} else { // timeout

				Thread.sleep(3000);
				ThreadHelper.threadMessage("TIMEOUT OCCURED!");
				leftOut.send(new Message(Message.Type.TIMEOUT));
			}
		}
	}

	final private class LeftInterface implements Runnable {
		// Listen on Left In	
		@Override
		public void run() {

			try {
				ThreadHelper.threadMessage("Network listening from ATM");
				simulate(leftIn_, leftOut_, rightIn_, rightOut_);
			} catch (InterruptedException e1) {}
		}

	}


	final public class RightInterface implements Runnable {
		@Override
		public void run() {
			try {
				simulate(rightIn_, rightOut_, leftIn_, leftOut_);
			} catch (InterruptedException e1) {}			
		}

	}



	@Override
	public void run() {

		Thread leftInterface = new Thread(new LeftInterface());
		Thread rightInterface = new Thread(new RightInterface());
		leftInterface.start();
		rightInterface.start();


	}


}
