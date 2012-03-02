import java.util.ArrayList;



/* 
 * =dbLChannelIn===>|-------|
 *                  |  DB   |
 * <==dbLChannelOut=|-------|
 * 
 */

public class Database implements Runnable {
	
	private ArrayList<Interface> interfaces = new ArrayList<Interface>();

	
	
	/*public Database(Channel leftIn, Channel leftOut) {
		leftIn_ = leftIn;
		leftOut_ = leftOut;
	}
*/
	public void connectDB(Channel leftIn, Channel leftOut) {
		interfaces.add(new Interface(leftIn, leftOut));
	}
	
	private class Interface implements Runnable {
		final private Channel leftIn_, leftOut_;
		private Message lastMsgSentOnLeft_;
		public Interface(Channel leftIn, Channel leftOut) {
			leftIn_ = leftIn;
			leftOut_ = leftOut;
		}
		@Override
		public synchronized void run() {
			try {
				while (true) {


					Message l_in = leftIn_.listen();
					//ThreadHelper.threadMessage("DB: Listening for Cloud");
					switch (l_in.getType()) {
					case GETPIN:
						ThreadHelper.threadMessage("DB: Received GETPIN request from Cloud");
						Message pinOKMsg = new Message(Message.Type.GETPINOK);
						ThreadHelper.threadMessage("DB: Replying Cloud with PIN");
						lastMsgSentOnLeft_ = pinOKMsg;
						ThreadHelper.threadMessage("DB: Sending PIN over network");
						leftOut_.send(pinOKMsg);


						break;
					case WITHDRAW:

						ThreadHelper.threadMessage("DB: Received WITHDRAW request from Cloud");
						Message withOKMsg = new Message(Message.Type.WITHDRAWOK);
						ThreadHelper.threadMessage("DB: Confirming Withdrawal, sending WITHDRAWOK");
						lastMsgSentOnLeft_ = withOKMsg;
						leftOut_.send(withOKMsg);					

						break;
					case RETRIEVERECORD:

						ThreadHelper.threadMessage("DB: Received RETRIEVERECORD request from Cloud");
						Message retrieveOKMsg = new Message(Message.Type.RETRIEVERECORDOK);
						ThreadHelper.threadMessage("DB: Confirming RETRIEVERECORD, sending WITHDRAWOK");
						lastMsgSentOnLeft_ = retrieveOKMsg;
						leftOut_.send(retrieveOKMsg);					

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
		//Thread leftInterface = new Thread(new Interface());
		//leftInterface.start();
		for (Interface inter : interfaces) {
			Thread t = new Thread(inter);
			t.start();
		}
			
	}

}
