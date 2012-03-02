/*      |-----|==atmChannelOut=>
 *      | ATM |  
 *      |-----|<=atomChannelIn==
 */

public class ATMMachine implements Runnable {
	final private Channel in, out;
	private Message lastMsgSentOnRight_, r_in;

	public ATMMachine(Channel in, Channel out) {
		this.in = in;
		this.out = out;
	}

	@Override
	public synchronized void run() {

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
				ThreadHelper.threadMessage(authInfo[i++]);
			} catch (InterruptedException e1) {}


		try {
			Message authMsg = new Message(Message.Type.AUTH);
			ThreadHelper.threadMessage("Sending Authentication Request");
			lastMsgSentOnRight_ = authMsg; 
			out.send(authMsg);


			ThreadHelper.threadMessage("ATMMachine listening for ATM request result");
			while (true) {
				/// Listen for AUTHOK ///
				//ThreadHelper.threadMessage("ATM: Listening for Cloud messages");
				r_in = in.listen();
				//ThreadHelper.threadMessage("ATM: Listened for Cloud messages");
				switch (r_in.getType()) {
				case AUTHOK:
					ThreadHelper.threadMessage("ATM: User has been authenticated");
					ThreadHelper.threadMessage("ATM: Sending withdrawal request");
					Message withMsg = new Message(Message.Type.WITHDRAW);
					lastMsgSentOnRight_ = withMsg;
					out.send(withMsg);

					// Send withdraw or cancel
					break;
				case WITHDRAWOK:
					ThreadHelper.threadMessage("ATM: Withdrawal Complete!");
					//continue atmstart;
					break;
				case TIMEOUT:
					ThreadHelper.threadMessage("ATM: Timeout Incurred");
					// cancel
					break;
				case FAILURE:
					ThreadHelper.threadMessage("ATM: Failure occured, resending last message");
					out.send(lastMsgSentOnRight_);
					break;
				default:
					ThreadHelper.threadMessage("ATM: Received "+r_in);
				} 
			}


		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//}

	}
}
