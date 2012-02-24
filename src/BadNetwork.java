
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
		int val = ThreadHelper.getRandom(5);
		
		Message recvMsg= leftIn.listen();
		
		switch (val) {
		case 0: // SUCCESS
		case 1:
		case 2:
				rightOut.send(recvMsg); // Relay message
				break;
		case 3: // FAILURE
				leftOut.send(new Message(Message.Type.FAILURE));
				break;
		case 4: // TIMEOUT
				leftOut.send(new Message(Message.Type.TIMEOUT));
				break;
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
