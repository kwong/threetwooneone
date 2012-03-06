public class Channel {

	private Message msg_ = null;

	public synchronized void send(Message v) throws InterruptedException {
		//ThreadHelper.threadMessage("Wrote message: " + v);
		//Thread.sleep(1000);
		msg_ = v;
		notifyAll();
	}
	
	// Busy waits when there's nothing on the channel 
	public synchronized Message listen() throws InterruptedException{
		while (msg_ == null) wait();
		Message temp = msg_;
		msg_ = null;
		//ThreadHelper.threadMessage("Read message: " + temp);
		return temp;
	}
	
}