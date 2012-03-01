

/* 
 * =dbLChannelIn===>|-------|
 * 					|  DB   |
 * <==dbLChannelOut=|-------|
 * 
 */

public class Database implements Runnable {
	final private Channel leftIn_, leftOut_;
	
	
	private Message lastMsgSentOnLeft_;
	
	public Database(Channel leftIn, Channel leftOut) {
		leftIn_ = leftIn;
		leftOut_ = leftOut;
	}
	
	private class LeftInterface implements Runnable {
		@Override
		public synchronized void run() {
			try {
				while (true) {
					
					
					Message l_in = leftIn_.listen();
					ThreadHelper.threadMessage("DB: Listening for Cloud");
					switch (l_in.getType()) {
					case GETPIN:
						ThreadHelper.threadMessage("DB: Received GETPIN request from Cloud");
						Message pinOKMsg = new Message(Message.Type.GETPINOK);
						ThreadHelper.threadMessage("DB: Replying Cloud with PIN");
						lastMsgSentOnLeft_ = pinOKMsg;
						ThreadHelper.threadMessage("DB: Sending PIN over network");
						leftOut_.send(pinOKMsg);
						
						
						break;
					case RETRIEVEBALANCE:
						
						ThreadHelper.threadMessage("DB: Received WITHDRAW request from Cloud");
						Message withOKMsg = new Message(Message.Type.WITHDRAWOK);
						ThreadHelper.threadMessage("DB: Confirming Withdrawal, sending WITHDRAWOK");
						lastMsgSentOnLeft_ = withOKMsg;
						leftOut_.send(withOKMsg);					
						
						break;
					case TIMEOUT: 
						ThreadHelper.threadMessage("DB: Network Timeout");
						
						lastMsgSentOnLeft_ = l_in;
						ThreadHelper.threadMessage("DB: Relay Timeout to Cloud" + l_in);
						leftOut_.send(l_in); // relay
						
						
						break;
					case FAILURE:
						ThreadHelper.threadMessage("DB: Network failure");
						leftOut_.send(lastMsgSentOnLeft_);
						break;
					}
				}
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			} catch (NullPointerException npe) {
				npe.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		Thread leftInterface = new Thread(new LeftInterface());
		leftInterface.start();
	}
	
}