/*      |-----|==atmChannelOut=>
 *      | ATM |  
 *      |-----|<=atomChannelIn==
 */

public class ATMMachine implements Runnable, Identification{
	final public Channel rightIn, rightOut;
	final private int id, user;
	private Message lastMsgSentOnRight_, r_in;

	public ATMMachine(int id, int user, Channel in, Channel out) {
		this.id = id;
		this.user = user;
		this.rightIn = in;
		this.rightOut = out;
	}

	@Override
	public void run() {

		//atmstart:
		//while (true) {
		String authInfo[] = {
				"Insert Card",
				"Enter pin",
				"Authenticate Card"
		};

		int i = 0;

		while (i < authInfo.length)
			try {
				ThreadHelper.threadMessage(authInfo[i++], getId());
				Thread.sleep(500);
			} catch (InterruptedException e1) {}


		try {
			Message authMsg = new Message(Message.Type.AUTH, user);
			ThreadHelper.threadMessage("Sending Authentication Request", getId());
			lastMsgSentOnRight_ = authMsg; 
			rightOut.send(authMsg);

			while (true) {
				/// Listen for AUTHOK ///
				//ThreadHelper.threadMessage("ATM: Listening for Cloud messages");
				r_in = rightIn.listen();
				//ThreadHelper.threadMessage("ATM: Listened for Cloud messages");
				switch (r_in.getType()) {
				case AUTHOK:
					ThreadHelper.threadMessage("User has been authenticated");
					ThreadHelper.threadMessage("Sending withdrawal request");
					Message withMsg = new Message(Message.Type.WITHDRAW, user);
					lastMsgSentOnRight_ = withMsg;
					rightOut.send(withMsg);

					// Send withdraw or cancel
					break;
				case WITHDRAWOK:
					ThreadHelper.threadMessage("Withdrawal Complete!", getId());
					//continue atmstart;
					break;
				case TIMEOUT:
					ThreadHelper.threadMessage("Timeout Incurred", getId());
					// cancel
					break;
				case FAILURE:
					ThreadHelper.threadMessage("Failure occured, resending last message", getId());
					rightOut.send(lastMsgSentOnRight_);
					break;
				default:
					ThreadHelper.threadMessage("Received "+r_in, getId());
				} 
			}


		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//}

	}

	@Override
	public String getId() {
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i< id+1; i++)
			sb.append("\t\t\t\t\t");
		sb.append("ATM");
		sb.append(id);
		sb.append(",");
		sb.append(user);
		return sb.toString();
	}
}
