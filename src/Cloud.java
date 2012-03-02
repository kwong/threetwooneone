
/* 
 * =cloudLChannelOut==>|-------|==cloudRChannelOut==>
 *                     | Cloud |
 * <==cloudLChannelIn==|-------|<==cloudRChannelIn===
 * 
 */

public class Cloud implements Runnable,Identification {
	final public Channel leftIn, leftOut, rightIn, rightOut;
		
	private Message lastMsgSentOnLeft_, lastMsgSentOnRight_;
	private int id ;
	final private String name;
	
	public Cloud(int id, Channel leftIn, Channel leftOut,
			Channel rightIn, Channel rightOut) {
		this.id = id;
		this.leftIn = leftIn;
		this.leftOut = leftOut;
		this.rightIn = rightIn;
		this.rightOut = rightOut;
		name = this.getId();
	}
	
	private class LeftInterface implements Runnable {
		@Override
		public void run() {
			try {
				while (true) {
					
					Message l_in = leftIn.listen();
					//ThreadHelper.threadMessage("Cloud: Listening for ATM messages");
					//ThreadHelper.threadMessage(""+l_in);
					switch (l_in.getType()) {
					case AUTH:
						ThreadHelper.threadMessage("Received AUTH request from ATM", name);
						Message authMsg = new Message(Message.Type.GETPIN);
						ThreadHelper.threadMessage("Requesting PIN from DB", name);
						ThreadHelper.threadMessage("Sending PIN Request over network", name);
						lastMsgSentOnRight_ = authMsg; 
						rightOut.send(authMsg);
						
						
						break;
					case WITHDRAW:
						ThreadHelper.threadMessage("Received WITHDRAW request from ATM", name);
						ThreadHelper.threadMessage("Requesting Balance from DB", name);
						Message withMsg = new Message(Message.Type.RETRIEVERECORD);
						
						lastMsgSentOnRight_ = withMsg;
						rightOut.send(withMsg);
						break;
					case TIMEOUT: 
						ThreadHelper.threadMessage("Network Timeout when sending message to ATM", name);
						ThreadHelper.threadMessage("Relay Timeout to DB", name);
						rightOut.send(l_in); // relay
						break;
					case FAILURE:
						ThreadHelper.threadMessage("Failure -- resending last request :" + lastMsgSentOnLeft_, name);
						ThreadHelper.threadMessage("Resending", name);
						leftOut.send(lastMsgSentOnLeft_);
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
					//ThreadHelper.threadMessage("Cloud: Listening for DB messages");
					Message r_in = rightIn.listen();
					//ThreadHelper.threadMessage("Cloud: Listened for DB messages");
					switch (r_in.getType()) {
					case GETPINOK:
						ThreadHelper.threadMessage("User has been Verified!", name);
						ThreadHelper.threadMessage("Notifying ATM that we are verified", name);
						Message authOKMsg = new Message(Message.Type.AUTHOK);
						lastMsgSentOnLeft_ = authOKMsg; 
						leftOut.send(authOKMsg); // relay
						
						
						
						break;
					case WITHDRAWOK:
						ThreadHelper.threadMessage("Withdraw OK!", name);
						ThreadHelper.threadMessage("Notifying ATM that we have successfully withdrawn", name);
						lastMsgSentOnLeft_ = r_in;
						leftOut.send(r_in); // relay						
						
						
						break;
					case RETRIEVERECORDOK:
						ThreadHelper.threadMessage("RECORD RECEIVED OK!", name);
						ThreadHelper.threadMessage("Sending withdraw request", name);
						lastMsgSentOnRight_ = new Message(Message.Type.WITHDRAW);
						rightOut.send(lastMsgSentOnRight_); // relay		
						break;
					case TIMEOUT: 
						ThreadHelper.threadMessage("Network Timeout", name);
						ThreadHelper.threadMessage("Relaying Timeout To ATM!" + r_in, name);
						lastMsgSentOnLeft_ = r_in;
						leftOut.send(r_in); // relay
						
						
						break;
					case FAILURE:
						// Do we resend timeout?!
						ThreadHelper.threadMessage("Last message failed, resending " + lastMsgSentOnRight_, name);
						rightOut.send(lastMsgSentOnRight_);
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
	
	@Override
	public String getId() {
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i< id+1; i++)
			sb.append("\t\t\t\t\t");
		sb.append("Cloud");
		sb.append(id);
		return sb.toString();
	}
	
	
}
