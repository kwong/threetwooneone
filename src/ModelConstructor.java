import java.util.ArrayList;


public class ModelConstructor {

	final private int numATM_;
	private ArrayList<ATMMachine> atmMachines = new ArrayList<ATMMachine>();
	private ArrayList<Cloud> cloudProcessors = new ArrayList<Cloud>();
	private ArrayList<BadNetwork> badNetworks1 =  new ArrayList<BadNetwork>();
	private ArrayList<BadNetwork> badNetworks2 = new ArrayList<BadNetwork>();
		
	private Database db ;
	
	public ModelConstructor(int numATM) {
		numATM_ = numATM;
		db = new Database();
		//numUsers_ = numUsers;
	}
	
	final private void constructATM() {	
		int userId = -1;
		for (int i=0; i<numATM_; i++){
			switch (Configuration.populationType){
			case 0: 
				userId = i;
				break;
			case 1:
				userId = ThreadHelper.getRandom(numATM_+1);
				break;
			case 2:
				userId = 0;
				break;
			}
			atmMachines.add(new ATMMachine(i, userId, new Channel(), new Channel())); 
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
			cloudLeftIn = cloudProcessors.get(i).leftIn;
			cloudLeftOut = cloudProcessors.get(i).leftOut;
			
			// joined by badnetwork
			BadNetwork bn = new BadNetwork(atmRightOut, atmRightIn, cloudLeftOut, cloudLeftIn);
			badNetworks1.add(bn);
		}
	}
	
	final private void connectCloudToDB() {
		Channel cloudRightIn, cloudRightOut;
		
		for(int i=0; i<numATM_; i++) {
			cloudRightIn = cloudProcessors.get(i).rightIn;
			cloudRightOut = cloudProcessors.get(i).rightOut;
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
