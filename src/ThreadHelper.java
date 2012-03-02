import java.util.Random;


public class ThreadHelper {

	final static Random ran = new Random();
	public static void threadMessage(String message, String id) throws InterruptedException {
		//Thread.sleep(1000);
		//String threadName = i.getId();

		System.out.format("%s: %s\n", id, message);
	}
	
	public static void threadMessage(String message) throws InterruptedException {
		//Thread.sleep(1000);
		String threadName = Thread.currentThread().getName();
		System.out.format("%s\n", message);
	}
	
	public static int getRandom(int range) {
		return ran.nextInt(range);
	}
}
