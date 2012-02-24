public class Channel {

	Message msg = null;

	public synchronized void send(Message v) throws InterruptedException {
		msg  = v;
		notifyAll();
	}
	
	public synchronized Message listen() throws InterruptedException{
		while (msg == null) wait();
		Message temp = msg;
		msg = null;
		//ThreadHelper.threadMessage("Channel has message: " + temp);
		return temp;
	}
	
}