
/* 
 * =cloudLChannelOut==>|-------|==cloudRChannelOut==>
 * 					   | Cloud |
 * <==cloudLChannelIn==|-------|<==cloudRChannelIn===
 * 
 */

public class Cloud implements Runnable {
	final private Channel leftIn_, leftOut_, rightIn_, rightOut_;
	final private Thread leftInterface = new Thread(new LeftInterface());
	final private Thread rightInterface = new Thread(new RightInterface());
	
	private Message lastMsgSentOnLeft, lastMsgSentOnRight;
	
	
	public Cloud(Channel leftIn, Channel leftOut,
			Channel rightIn, Channel rightOut) {
		this.leftIn_ = leftIn;
		this.leftOut_ = leftOut;
		this.rightIn_ = rightIn;
		this.rightOut_ = rightOut;
	}
	
	private class LeftInterface implements Runnable {
		@Override
		public void run() {
			try {
				ThreadHelper.threadMessage("Cloud: Listening for ATM messages");
				Message l_in = leftIn_.listen();
				switch (l_in.getType()) {
				case AUTH:
					ThreadHelper.threadMessage("Cloud: Received AUTH request from ATM");
					Message authMsg = new Message(Message.Type.GETPIN);
					ThreadHelper.threadMessage("Cloud: Requesting PIN from DB");
					rightOut_.send(authMsg);
					ThreadHelper.threadMessage("Cloud: Sending PIN Request over network");
					lastMsgSentOnLeft = authMsg; 
					break;
				case WITHDRAW:
					ThreadHelper.threadMessage("Cloud: Received WITHDRAW request from ATM");
					Message withMsg = new Message(Message.Type.RETRIEVEBALANCE);
					ThreadHelper.threadMessage("Cloud: Requesting Balance from DB");
					rightOut_.send(withMsg);
					ThreadHelper.threadMessage("Cloud: Sending Balance Request over network");
					lastMsgSentOnLeft = withMsg;
					break;
				case TIMEOUT: 
					ThreadHelper.threadMessage("CLOUD: Network Timeout");
					leftOut_.send(l_in); // relay
					ThreadHelper.threadMessage("CLOUD: Relay Timeout to ATM");
					break;
				case FAILURE:
					ThreadHelper.threadMessage("Cloud: ATM-CLOUD Network Timeout");
					leftOut_.send(lastMsgSentOnLeft);
					ThreadHelper.threadMessage("Cloud: Resending");
					break;
				}
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			} catch (NullPointerException npe) {
				npe.printStackTrace();
			}
		}
	}
	
	private class RightInterface implements Runnable {
		@Override
		public void run() {
			try {
				ThreadHelper.threadMessage("Cloud: Listening for DB messages");
				Message r_in = rightIn_.listen();
				switch (r_in.getType()) {
				case GETPINOK:
					ThreadHelper.threadMessage("CLOUD: Verified!");
					leftOut_.send(r_in); // relay
					ThreadHelper.threadMessage("CLOUD: Notifying ATM that we are verified");
					break;
				case WITHDRAWOK:
					ThreadHelper.threadMessage("CLOUD: Withdraw OK!");	
					leftOut_.send(r_in); // relay
					ThreadHelper.threadMessage("CLOUD: Notifying ATM that we have successfully withdrawn");
					break;
				case TIMEOUT: 
					ThreadHelper.threadMessage("CLOUD: Network Timeout");
					leftOut_.send(r_in); // relay
					ThreadHelper.threadMessage("CLOUD: Relaying Timeout To DB!");
					break;
				case FAILURE:
					ThreadHelper.threadMessage("CLOUD: Network Timeout");
					break;
				}
				
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	

	@Override
	public void run() {
		// TODO Auto-generated method stub

		leftInterface.start();
		rightInterface.start();
	}
	
	
}
