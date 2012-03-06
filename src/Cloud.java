import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;



/* 
 * =cloudLChannelOut==>|-------|==cloudRChannelOut==>
 *                     | Cloud |
 * <==cloudLChannelIn==|-------|<==cloudRChannelIn===
 * 
 */

public class Cloud extends JPanel implements Runnable,Identification {

	private static final long serialVersionUID = -6726594659815499296L;

	final public Channel leftIn_, leftOut_, rightIn_, rightOut_;
		
	private Message lastMsgSentOnLeft_, lastMsgSentOnRight_;
	private int id_ ;
	private int servedUser=-1;
	final private String name;
	
	/* Swing */
	final private JLabel cloudLbl;
	final private JButton cloudBtn;
	
	
	/* Flags to control ActionListener */
	private Status currentState;
	private enum Status{
		PRE_AUTHED,
		AUTHED,
		RECORDFETCHED,
		WITHDRAWN,
		FAILURELEFT,
		FAILURERIGHT,
		FAILUREFROMLEFT,
		FAILUREFROMRIGHT,
		PRE_WITHDRAW,
		TIMEOUT,
		CANCELED
	}
	
	public Cloud(int id, Channel leftIn, Channel leftOut,
			Channel rightIn, Channel rightOut) {
		this.id_ = id;
		this.leftIn_ = leftIn;
		this.leftOut_ = leftOut;
		this.rightIn_ = rightIn;
		this.rightOut_ = rightOut;
		name = this.getId();
		currentState = Status.PRE_AUTHED;
		
		/* Swing parts */
		cloudLbl = new JLabel("Cloud"+id);
		cloudBtn = new JButton("Waiting");
		cloudBtn.setEnabled(false);
		
		add(cloudLbl);
		add(cloudBtn);
		
		cloudLbl.setAlignmentX(Component.CENTER_ALIGNMENT);	
		cloudBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		
		
		/* Pane Config */
		setPreferredSize(Config.ENTITY_SIZE);
		setLayout(new GridLayout(3,1 ));
		//setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createMatteBorder(1, 2, 1, 1,Color.black));
		
		
		cloudBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					switch (currentState) {
					
					/* The Java implementation keeps track of "happens-before" events
					 * in order to execute the correct sequence of events according to specific input events
					 */
					
					/* We have to send a GETPIN request to verify the user in the PRE_AUTHED State */
					case PRE_AUTHED:
						ThreadHelper.threadMessage("Send GETPIN to DB", getId());
						Message authMsg = new Message(Message.Type.GETPIN, servedUser, id_);
						//ThreadHelper.threadMessage("Requesting PIN from DB", name);
						//ThreadHelper.threadMessage("Sending PIN Request over network", name);
						lastMsgSentOnRight_ = authMsg; 
						rightOut_.send(authMsg);
						cloudBtn.setEnabled(false);
						break;
						
					/* We can send a AUTHOK to the ATM after being AUTHED */	
					case AUTHED:
						ThreadHelper.threadMessage("Send AUTHOK to ATM"+id_, getId());
						Message authOKMsg = new Message(Message.Type.AUTHOK, servedUser, id_);	
						lastMsgSentOnLeft_ = authOKMsg;
						leftOut_.send(authOKMsg);
						cloudBtn.setEnabled(false);
						break;
					
					/* 	We can Send a DEAUTHOK message back to the ATM after handling a CANCEL request */
					case CANCELED:
						ThreadHelper.threadMessage("Send DEAUTHOK to ATM"+getId(), getId());
						Message deauthOKMsg = new Message(Message.Type.DEAUTHOK, servedUser, id_);
						lastMsgSentOnLeft_ = deauthOKMsg;
						leftOut_.send(deauthOKMsg);
						cloudBtn.setEnabled(false);
						break;
						
					/* We have to RETRIEVERECORD from the Database before actually WITHDRAWING */	
					case PRE_WITHDRAW:
						ThreadHelper.threadMessage("Send GETBALANCE("+servedUser+") to DB", getId());
						Message recMsg = new Message(Message.Type.GETBALANCE, servedUser, id_);	
						lastMsgSentOnRight_ = recMsg;
						rightOut_.send(recMsg);
						cloudBtn.setEnabled(false);
						break;
						
					/* We can send a SETBALANCE request upon receiving a record */	
					case RECORDFETCHED:
						ThreadHelper.threadMessage("Send SETBALANCE("+servedUser+") to DB", getId());
						lastMsgSentOnRight_ = new Message(Message.Type.SETBALANCE, servedUser, id_);
						rightOut_.send(lastMsgSentOnRight_); // relay		
						cloudBtn.setEnabled(false);
						break;

					/* We can send a WITHDRAWOK to the ATM after a successful SETBALANCE */	
					case WITHDRAWN:		
						ThreadHelper.threadMessage("Send WITHDRAWOK to ATM"+id_, getId());
						lastMsgSentOnLeft_ = new Message(Message.Type.WITHDRAWOK, servedUser, id_);
						leftOut_.send(lastMsgSentOnLeft_); // relay	
						cloudBtn.setEnabled(false);
						break;
						
					/* A FAILURE message from the channel between ATM-Cloud is dealt with by a resend */	
					case FAILUREFROMLEFT:
						ThreadHelper.threadMessage("Resend "+lastMsgSentOnLeft_+" to ATM"+getId(), getId());
						leftOut_.send(lastMsgSentOnLeft_);
						cloudBtn.setEnabled(false);
						break;
						
						
					/* A FAILURE message from the channel between Cloud-DB is dealt with by a resend */		
					case FAILUREFROMRIGHT:
						ThreadHelper.threadMessage("Resend "+lastMsgSentOnRight_+" to DB"+getId(), getId());
						rightOut_.send(lastMsgSentOnRight_);
						cloudBtn.setEnabled(false);
						break;
					}
				} catch(Exception ie){}
			}
		});
		
	}
	
	
	/*************************************************\
	 *  Simulates Listening on the ATM2Cloud Network *
	\*************************************************/
	private class LeftInterface implements Runnable {
		@Override
		public void run() {
			try {
				while (true) {
					
					
					Message l_in = leftIn_.listen();
					//ThreadHelper.threadMessage("Cloud: Listening for ATM messages");
					//ThreadHelper.threadMessage(""+l_in);
					switch (l_in.getType()) {
					
					/* Event handling with similar behavior to PAT Implementation */
					
					/* An AUTH can be followed by:
					 *  1. Sending GETPIN to DB
					 */
					case AUTH:
						ThreadHelper.threadMessage("Received AUTH", getId());
						/*ThreadHelper.threadMessage("Received AUTH request from ATM", name);
						Message authMsg = new Message(Message.Type.GETPIN, l_in.user_);
						ThreadHelper.threadMessage("Requesting PIN from DB", name);
						ThreadHelper.threadMessage("Sending PIN Request over network", name);
						lastMsgSentOnRight_ = authMsg; 
						rightOut_.send(authMsg);	*/
						currentState = Status.PRE_AUTHED;
						servedUser = l_in.user_;
						//System.out.println(servedUser);
						cloudBtn.setText("GETPIN from DB");
						cloudBtn.setEnabled(true);			
						break;
						
					/* A DEAUTH can be followed by:
					 *  1. Confirming DEAUTH 
					 */
					case DEAUTH:
						ThreadHelper.threadMessage("Received DEAUTH", getId());
						currentState = Status.CANCELED;
						cloudBtn.setText("Confirm DEAUTH");
						cloudBtn.setEnabled(true);
						break;
						
						
					/* A WITHDRAW request can be followed by:
					 * 	 1. Sending RETRIEVERECORD to DB
					 */
					case WITHDRAW:
						/*ThreadHelper.threadMessage("Received WITHDRAW request from ATM", name);
						ThreadHelper.threadMessage("Requesting Balance from DB", name);
						Message withMsg = new Message(Message.Type.RETRIEVERECORD, l_in.user_);
						*/
						ThreadHelper.threadMessage("Received WITHDRAW request", getId());
						cloudBtn.setText("Send DB GETBALANCE");
						currentState = Status.PRE_WITHDRAW;
						cloudBtn.setEnabled(true);
						
						break;
						
						
					/* A TIMEOUT is followed by:
					 * 	1. Propagating it to Cloud2DB (could be a message sent from DB that Timeout at ATM2Cloud)
					 */
					case TIMEOUT: 
						ThreadHelper.threadMessage("TIMEOUT", getId());
						//ThreadHelper.threadMessage("Network Timeout when sending message to ATM", name);
						//ThreadHelper.threadMessage("Relay Timeout to DB", name);
						rightOut_.send(l_in); // relay
						break;
						
					/* A FAILURE is followed by
					 * 	1. Resending the last sent message
					 */
					case FAILURE:
						//ThreadHelper.threadMessage("Failure -- resending last request :" + lastMsgSentOnLeft_, name);
						//ThreadHelper.threadMessage("Resending", name);
						//leftOut_.send(lastMsgSentOnLeft_);
						ThreadHelper.threadMessage("FAILURE: Resending "+lastMsgSentOnLeft_, getId());
						currentState = Status.FAILUREFROMLEFT;
						cloudBtn.setText("Resend: "+lastMsgSentOnLeft_);
						cloudBtn.setEnabled(true);
						break;
					}
				}
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			} catch (NullPointerException npe) {
				npe.printStackTrace();
			}
		}
	}
	
	
	/***********************************************\
	 *  Simulates Listening on the Cloud2DB Network *
	\***********************************************/
	
	private class RightInterface implements Runnable {
		@Override
		public synchronized void run() {
			try {
				while (true) {
					
					Message r_in = rightIn_.listen();
					switch (r_in.getType()) {
					
					/* A GETPINOK can be followed by 
					 *  1. Sending a AUTHED to ATM to confirm authentication
					 */
					case GETPINOK:
						ThreadHelper.threadMessage("Received GETPINOK", getId());
						currentState = Status.AUTHED;
					
						cloudBtn.setText("Send ATM AUTHOK");
						cloudBtn.setEnabled(true);
						break;
						
					/* A WITHDRAWOK can be followed by
					 * 	1. Sending a WITHDRAWOK to ATM to inform that WITHDRAW has succeeded
					 */
					case SETBALANCEOK:
						ThreadHelper.threadMessage("Received SETBALANCEOK", getId());
						currentState = Status.WITHDRAWN;
						cloudBtn.setText("Send ATM WITHDRAWOK");
						cloudBtn.setEnabled(true);
						
						break;
						
					/* A RETRIEVERECORDOK can be followed by
					 * 	1. Sending the intended WITHDRAW request to DB 
					 */
					case GETBALANCEOK:
						ThreadHelper.threadMessage("Received GETBALANCEOK", getId());					
						currentState = Status.RECORDFETCHED;
						cloudBtn.setText("Send DB SETBALANCE");
						cloudBtn.setEnabled(true);
						break;
						
					/* A TIMEOUT is followed by:
					 * 	1. Propagating it to Cloud2ATM (could be a message sent from ATM that Timedout at Cloud2DB)
					 */
					case TIMEOUT:
						ThreadHelper.threadMessage("TIMEOUT", getId());
						lastMsgSentOnLeft_ = r_in;
						leftOut_.send(r_in); // relay
						
						
						break;
						
					/* A FAILURE is followed by
					 * 	1. Resending the last sent message
					 */	
					case FAILURE:
						ThreadHelper.threadMessage("FAILURE: Resending "+lastMsgSentOnRight_, getId());
						
						currentState = Status.FAILUREFROMRIGHT;
						cloudBtn.setText("Resend: "+lastMsgSentOnRight_);
						cloudBtn.setEnabled(true);
						break;
					}
				}
				
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	

	@Override
	public void run() {
		Thread leftInterface = new Thread(new LeftInterface());
		Thread rightInterface = new Thread(new RightInterface());
		leftInterface.start();
		rightInterface.start();
	}
	
	@Override
	public String getId() {
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i< id_+1; i++)
		sb.append("Cloud");
		sb.append(id_);
		return sb.toString();
	}
	
	
}
