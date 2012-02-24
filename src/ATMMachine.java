
public class ATMMachine implements Runnable {
	final Channel in, out;
	
	public ATMMachine(Channel in, Channel out) {
		this.in = in;
		this.out = out;
	}
	
	
	public String Name() {
		return "ATM";
		
	}
	
	@Override
	public void run() {
		String authInfo[] = {
				"Insert Card",
				"Enter pin",
				"Authenticate Card"
		};
		
		int i = 0;
		while (i < authInfo.length) 
			ThreadHelper.threadMessage(authInfo[i++]);
			
		try {
			out.send(new Message(Message.Type.AUTH));
			ThreadHelper.threadMessage("Sent Authentication Request");
			ThreadHelper.threadMessage("ATMMachine listening for ATM request result");
			ThreadHelper.threadMessage("ATM Received: " + in.listen());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		
	}
}
