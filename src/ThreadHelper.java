import java.util.Random;


public class ThreadHelper {

	final static Random ran = new Random();
	public static void threadMessage(String message) {
		String threadName = Thread.currentThread().getName();
		System.out.format("%s: %s\n", threadName, message);
	}
	
	public static int getRandom(int range) {
		return ran.nextInt(range);
	}
}
