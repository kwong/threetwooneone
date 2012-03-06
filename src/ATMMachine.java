import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/*      |-----|==atmChannelOut=>
 *      | ATM |  
 *      |-----|<=atomChannelIn==
 */

public class ATMMachine extends JPanel implements Runnable, Identification{
	
	private static final long serialVersionUID = 1432703282437710704L;
	
	/* ATM components */
	final public Channel rightIn, rightOut;
	final private int atmId_;

	private int user_;
	private Message lastMsgSentOnRight_, r_in;
	private Status currentState;
	
	/* Swing */
	final private JLabel atmLbl;
	final private JLabel atmUserLbl;
	final private JButton atmBtn;
	final private JButton atmCancelBtn;
	SpinnerModel model = new SpinnerNumberModel (1, 1, 99, 1);
	final private JSpinner atmSpn ;
	
	
	/* Flags to control ActionListener behavior */
	private enum Status{
		PRE_INSERT_CARD,
		PRE_ENTER_PIN,
		PRE_SEND_AUTH,
		PRE_AUTH,
		AUTHED,
		WITHDRAWN,
		FAILURE,
		TIMEOUT
		
	}
	

	public ATMMachine(int id, Channel in, Channel out) {
		this.atmId_ = id;
		this.user_ = 0;
		this.rightIn = in;
		this.rightOut = out;
		currentState = Status.PRE_ENTER_PIN;
		
		/* Swing parts */
		atmLbl = new JLabel("ATM"+id);
		atmUserLbl = new JLabel("Serving AccountID");
		atmBtn = new JButton("Insert Card");
		atmCancelBtn = new JButton("Cancel");
		atmSpn = new JSpinner(model);
		
		//atmLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
		atmBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		atmCancelBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		atmCancelBtn.setEnabled(false);
		
		
		/* Config panel */
		
		JPanel atmConf = new JPanel();
		//atmConf.setLayout(new GridLayout(1,3));
		atmConf.add(atmLbl);
		atmConf.add(atmUserLbl);
		atmConf.add(atmSpn);
		
		
		add(atmConf);
		add(atmBtn);
		add(atmCancelBtn);
		
		/* Pane config */
		//setPreferredSize(Config.ENTITY_SIZE);
		//setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setLayout(new GridLayout(3,1 ));
		setBorder(BorderFactory.createMatteBorder(1, 2, 1, 1,Color.black));
		
		/* ActionListeners */
		ChangeListener listener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				user_ = (Integer) atmSpn.getValue();
             }
        };
        atmSpn.addChangeListener(listener);
		
		atmCancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				atmBtn.setText("Remove Card");
				
				//atmBtn.setEnabled(false);
				
				Message deauthMsg = new Message(Message.Type.DEAUTH, user_, atmId_);
				try {
					//System.out.println(currentState);
					ThreadHelper.threadMessage("Cancel", getId());
					
					if (currentState != Status.PRE_ENTER_PIN
							&& currentState != Status.PRE_INSERT_CARD
							&& currentState != Status.PRE_SEND_AUTH
							&& currentState != Status.PRE_AUTH){
						ThreadHelper.threadMessage("Sending DEAUTH to Cloud"+atmId_, getId());
						rightOut.send(deauthMsg);
						
						// ATM pending message from Cloud.
						atmCancelBtn.setEnabled(false);
						atmBtn.setEnabled(false);
						
					}
					currentState = Status.PRE_INSERT_CARD;
					atmCancelBtn.setEnabled(false);
				} catch (InterruptedException e1) {	
					e1.printStackTrace();
				}
				//atmBtn.setEnabled(false);
			}
		});
		
		atmBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					switch(currentState) {
					
					/* The Java implementation has to keep track of "happens-before" events
					 * in order to execute the correct sequence of events according to specific input events
					 */
					
					/* We can insert card in the PRE_INSERT_CARD State */
					case PRE_INSERT_CARD:
						ThreadHelper.threadMessage("Remove Card", getId());
						
						atmBtn.setText("Insert Card");
						currentState = Status.PRE_ENTER_PIN;	
						
						// Ensure we don't change accountIDs midway during transaction
						atmSpn.setEnabled(true);
						break;
						
					/* We can enter pin in the PRE_ENTER_PIN State */	
					case PRE_ENTER_PIN:
						ThreadHelper.threadMessage("Insert Card", getId());
						atmBtn.setText("Insert Pin");
						
						atmCancelBtn.setEnabled(true);
						currentState = Status.PRE_SEND_AUTH;
						atmSpn.setEnabled(false);
						break;
						
					/* We can Send an AUTH request in the PRE_SEND_AUTH State */	
					case PRE_SEND_AUTH:
						ThreadHelper.threadMessage("Insert Pin", getId());
						
						atmBtn.setText("Send AUTH");
						currentState = Status.PRE_AUTH;
						// After AUTHOK received by ATM, state is set to AUTHED.
						break;
						
					/* Event handling between Sending AUTH request and getting AUTHED */	
					case PRE_AUTH:
						ThreadHelper.threadMessage("Send AUTH", getId());
						Message authMsg = new Message(Message.Type.AUTH, user_, atmId_);						
						// Forbid canceling while message is in channel
						atmCancelBtn.setEnabled(false);
						lastMsgSentOnRight_ = authMsg; 
						rightOut.send(authMsg);
						atmBtn.setEnabled(false);
						break;
						
					/* Event handling after being AUTHED */	
					case AUTHED:
						ThreadHelper.threadMessage("Send WITHDRAW", getId());
						// Input WITHDRAW to channel
						Message withMsg = new Message(Message.Type.WITHDRAW, user_, atmId_);
						lastMsgSentOnRight_ = withMsg;
						rightOut.send(withMsg);
						atmBtn.setEnabled(false);
						atmCancelBtn.setEnabled(false);
						break;
						
					/* Event handling after a successful WITHDRAW */
					case WITHDRAWN:
						atmBtn.setText("Remove Card");
						
						currentState = Status.PRE_INSERT_CARD;
						
						break;
						
					
					/* Event handling during a FAILURE */	
					case FAILURE:
						ThreadHelper.threadMessage("FAILURE: Resending "+lastMsgSentOnRight_, getId());
						rightOut.send(lastMsgSentOnRight_);
						atmBtn.setEnabled(false);
						break;
						
					/* Event handling during a TIMEOUT */	
					case TIMEOUT:
						atmBtn.setText("Remove Card");
						currentState = Status.PRE_INSERT_CARD; // reset our ATM to PRE_INSERT_CARD state
						break;
						
					default:
						atmBtn.setText("Unknown Error");
						atmBtn.setEnabled(false);
						break;							
					}
				} catch(InterruptedException f) {}	
			}
		});
		
		
	}

	@Override
	public void run() {
	
		try {
				listen:
				while (true) {
					
				r_in = rightIn.listen();
	
				switch (r_in.getType()) {
				
				/* Event handling with similar behavior to PAT Implementation */
				
				/* An AUTHOK can be followed by
				 *  1. A request to withdraw
				 *  2. A request to cancel
				 */
				case AUTHOK: 
					/* Swing */
					ThreadHelper.threadMessage("Received AUTHOK", getId());
					
					atmBtn.setText("Withdraw $");
					atmBtn.setEnabled(true);
					
					
					// Allow session to be canceled
					atmCancelBtn.setEnabled(true);
					currentState = Status.AUTHED;
					break;
					
				/* A DEAUTHOK can be followed by
				 *  1. Removing Card
				 */
				case DEAUTHOK:
					ThreadHelper.threadMessage("Received DEAUTHOK", getId());
					
					atmBtn.setEnabled(true);
					
					// Can't cancel a second time, silly
					atmCancelBtn.setEnabled(false);
					
					break;
					
				/* A WITHDRAWOK can be followed by
				 *  1. Removing Card
				 */
				case WITHDRAWOK:
					ThreadHelper.threadMessage("Received WITHDRAWOK", getId());
					atmBtn.setText("Withdraw Successful");
					atmBtn.setEnabled(true);
					currentState = Status.WITHDRAWN;

					break;
					
					
				/* A TIMEOUT can be followed by
				 * 	1. Removing Card
				 */
				case TIMEOUT:
					ThreadHelper.threadMessage("TIMEOUT", getId());
					atmBtn.setText("TIMEOUT");
					atmBtn.setEnabled(true);
					currentState = Status.TIMEOUT;
					ThreadHelper.threadMessage("TIMEOUT", getId());
					// cancel
					break;
					
				/* A FAILURE can be followed by
				 *  1. Resending last message
				 */
				case FAILURE:
					
					//rightOut.send(lastMsgSentOnRight_);
					ThreadHelper.threadMessage("FAILURE: Resending "+lastMsgSentOnRight_ , getId());
					atmBtn.setText("Resend "+lastMsgSentOnRight_);
					atmBtn.setEnabled(true);
					currentState = Status.FAILURE;
					break;
				default:
					ThreadHelper.threadMessage("Received "+r_in, getId());
					break;
				} 
			}


		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//}

	}

	
	public String getId() {
		StringBuffer sb = new StringBuffer();
		sb.append("ATM");
		sb.append(atmId_);
		sb.append(",");
		sb.append(user_);
		return sb.toString();
	}
}
