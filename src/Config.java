import java.awt.Dimension;


public class Config {
		
	// 0 for linear, 1 for random, 2 for all-the-same
	//public static int populationType = 2;
	
	// 1 for equal chances of a timeout/failure/success. The higher the value, the more chances of success
	public static int fairnessFactor = 0;

	// Handles delay time of events
	public static int delay = 1000;
	
	
	/* CONSTANTS */
	
	
	// Size of Panels
	final public static Dimension ENTITY_SIZE = new Dimension(120, 80);
	final public static Dimension NET_SIZE = new Dimension(120, 80);
	//final public static Dimension DB_SIZE = new Dimension(120, 80);
	

}
