import java.io.IOException;
import java.util.ArrayList;


public class ModelConstructor {

	final private int numATM_;
	public ArrayList<ATMMachine> atmMachines = new ArrayList<ATMMachine>();
	public ArrayList<Cloud> cloudProcessors = new ArrayList<Cloud>();
	public ArrayList<BadNetwork> badNetworks1 =  new ArrayList<BadNetwork>();
	public ArrayList<BadNetwork> badNetworks2 = new ArrayList<BadNetwork>();
		
	public Database db ;
	
	public ModelConstructor(int numATM) throws IOException {
		numATM_ = numATM;
		db = new Database();
		//numUsers_ = numUsers;
	}
	
	final private void constructATM() {	
		for (int i=0; i<numATM_; i++){
			atmMachines.add(new ATMMachine(i, new Channel(), new Channel())); 
		}
	}
	
	final private void constructCloud() {
		for (int i=0; i<numATM_; i++)
			cloudProcessors.add(new Cloud(i, new Channel(), new Channel(), new Channel(), new Channel()));
	}
	
	final private void connectATMToCloud() {
		Channel atmRightIn, atmRightOut, cloudLeftIn, cloudLeftOut; 
		for (int i=0; i<numATM_; i++) {
			atmRightIn = atmMachines.get(i).rightIn;
			atmRightOut = atmMachines.get(i).rightOut;
			cloudLeftIn = cloudProcessors.get(i).leftIn_;
			cloudLeftOut = cloudProcessors.get(i).leftOut_;
			
			// joined by badnetwork
			BadNetwork bn = new BadNetwork(atmRightOut, atmRightIn, cloudLeftOut, cloudLeftIn);
			badNetworks1.add(bn);
		}
	}
	
	final private void connectCloudToDB() {
		Channel cloudRightIn, cloudRightOut;
		
		for(int i=0; i<numATM_; i++) {
			cloudRightIn = cloudProcessors.get(i).rightIn_;
			cloudRightOut = cloudProcessors.get(i).rightOut_;
			Channel dbLeftIn = new Channel();
			Channel dbLeftOut = new Channel();
			db.connectDB(dbLeftIn, dbLeftOut);
			
			// joined by badnetwork
			BadNetwork bn = new BadNetwork(cloudRightOut, cloudRightIn, dbLeftOut, dbLeftIn);
			badNetworks2.add(bn);
			
		}
	}
	
	final private void constructAll() {
		constructATM();
		constructCloud();
		connectATMToCloud();
		connectCloudToDB();
	}
	
	final public void startSimulation() {
		constructAll();
		
		(new Thread(db)).start();
		for(BadNetwork bn : badNetworks2)
			(new Thread(bn)).start();
		
		for(Cloud cloud: cloudProcessors) 
			(new Thread(cloud)).start();
		
		for(BadNetwork bn : badNetworks1)
			(new Thread(bn)).start();
		
		for(ATMMachine atm: atmMachines)
			(new Thread(atm)).start();

	}
	
	
}
