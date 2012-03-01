
/* 
 * =cloudLChannelOut==>|-------|==cloudRChannelOut==>
 * 					   | Cloud |
 * <==cloudLChannelIn==|-------|<==cloudRChannelIn===
 * 
 */

public class Cloud implements Runnable {
	final private Channel leftIn_, leftOut_, rightIn_, rightOut_;
		
	private Message lastMsgSentOnLeft_, lastMsgSentOnRight_;
	
	
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
				while (true) {
					
					Message l_in = leftIn_.listen();
					ThreadHelper.threadMessage("Cloud: Listening for ATM messages");
					//ThreadHelper.threadMessage(""+l_in);
					switch (l_in.getType()) {
					case AUTH:
						ThreadHelper.threadMessage("Cloud: Received AUTH request from ATM");
						Message authMsg = new Message(Message.Type.GETPIN);
						ThreadHelper.threadMessage("Cloud: Requesting PIN from DB");
						ThreadHelper.threadMessage("Cloud: Sending PIN Request over network");
						lastMsgSentOnRight_ = authMsg; 
						rightOut_.send(authMsg);
						
						
						break;
					case WITHDRAW:
						ThreadHelper.threadMessage("Cloud: Received WITHDRAW request from ATM");
						ThreadHelper.threadMessage("Cloud: Requesting Balance from DB");
						Message withMsg = new Message(Message.Type.RETRIEVEBALANCE);
						
						lastMsgSentOnRight_ = withMsg;
						rightOut_.send(withMsg);
						
						break;
					case TIMEOUT: 
						ThreadHelper.threadMessage("CLOUD: Network Timeout when sending message to ATM");
						ThreadHelper.threadMessage("CLOUD: Relay Timeout to DB");
						rightOut_.send(l_in); // relay
			
						break;
					case FAILURE:
						ThreadHelper.threadMessage("Cloud: Failure -- resending last request :" + lastMsgSentOnLeft_);
						ThreadHelper.threadMessage("Cloud: Resending");
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
	
	private class RightInterface implements Runnable {
		@Override
		public synchronized void run() {
			try {
				while (true) {
					ThreadHelper.threadMessage("Cloud: Listening for DB messages");
					Message r_in = rightIn_.listen();
					ThreadHelper.threadMessage("Cloud: Listened for DB messages");
					switch (r_in.getType()) {
					case GETPINOK:
						ThreadHelper.threadMessage("CLOUD: User has been Verified!");
						ThreadHelper.threadMessage("CLOUD: Notifying ATM that we are verified");
						Message authOKMsg = new Message(Message.Type.AUTHOK);
						lastMsgSentOnLeft_ = authOKMsg; 
						leftOut_.send(authOKMsg); // relay
						
						
						
						break;
					case WITHDRAWOK:
						ThreadHelper.threadMessage("CLOUD: Withdraw OK!");
						ThreadHelper.threadMessage("CLOUD: Notifying ATM that we have successfully withdrawn");
						lastMsgSentOnLeft_ = r_in;
						leftOut_.send(r_in); // relay						
						
						
						break;
					case TIMEOUT: 
						ThreadHelper.threadMessage("CLOUD: Network Timeout");
						ThreadHelper.threadMessage("CLOUD: Relaying Timeout To ATM!" + r_in);
						lastMsgSentOnLeft_ = r_in;
						leftOut_.send(r_in); // relay
						
						
						break;
					case FAILURE:
						// Do we resend timeout?!
						ThreadHelper.threadMessage("CLOUD: Last message failed, resending " + lastMsgSentOnRight_);
						rightOut_.send(lastMsgSentOnRight_);
						break;
					}
				}
				
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
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
