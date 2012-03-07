
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;


/*
 *    --leftIn-->|============|--rightOut-->
 *               |Bad Network |
 *    <-leftOut--|============|<--rightIn---  
 *     
 */
public class BadNetwork extends JPanel implements Runnable {

	private static final long serialVersionUID = -8252513516524482921L;
	
	final private Channel leftIn_, leftOut_, rightIn_, rightOut_;
	final private JLabel netLbl;
	final private JButton netBtnSuccess, netBtnFail, netBtnTimeout;
	private Status currentState;
	private Message transitionLeftMsg, transitionRightMsg;

	
	private enum Status{
		MSGLEFT, 
		MSGRIGHT,
		NOMSG
	}
	
	public BadNetwork(Channel leftIn, Channel leftOut, 
			Channel rightIn, Channel rightOut) {
		leftIn_ = leftIn;
		leftOut_ = leftOut;
		rightIn_ = rightIn;
		rightOut_ = rightOut;
		currentState = Status.NOMSG;
		
		
		/* Swing */
		netLbl = new JLabel("Network");
		netBtnFail = new JButton("Failure");
		netBtnSuccess = new JButton("Success");
		netBtnTimeout = new JButton("Timeout");
		disableButtons();
		add(netLbl);
		add(netBtnSuccess);
		add(netBtnFail);
		add(netBtnTimeout);
		netLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
		netBtnFail.setAlignmentX(Component.CENTER_ALIGNMENT);
		netBtnSuccess.setAlignmentX(Component.CENTER_ALIGNMENT);
		netBtnTimeout.setAlignmentX(Component.CENTER_ALIGNMENT);
		setPreferredSize(Config.NET_SIZE);
		setLayout(new GridLayout(4,3));
		//setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createMatteBorder(1, 2, 1, 1,Color.black));
		
		
		/* The Java implementation has to keep track of "happens-before" events
		 * in order to execute the correct sequence of events according to specific input events
		 */
		
		/* In the event that FAILURE occurs */
		netBtnFail.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					disableButtons();
					switch (currentState) {
					
					case MSGLEFT:
						leftOut_.send(new Message(Message.Type.FAILURE));
						break;
					
					case MSGRIGHT:
						rightOut_.send(new Message(Message.Type.FAILURE));
						break;
					}
					
				} catch (InterruptedException ie) {}
			}
		});
		
		/* In the event that SUCCESS occurs */
		netBtnSuccess.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					disableButtons();
					switch (currentState) {
					case MSGLEFT:	
						rightOut_.send(transitionLeftMsg);
						break;
					case MSGRIGHT:
						leftOut_.send(transitionRightMsg);
						break;
					}
					
				} catch (InterruptedException ie) {}
			}
		});
		
		/* In the event that TIMEOUT occurs */
		netBtnTimeout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					disableButtons();
					switch (currentState) {
					case MSGLEFT:
						leftOut_.send(new Message(Message.Type.TIMEOUT));
						break;
					case MSGRIGHT:
						rightOut_.send(new Message(Message.Type.TIMEOUT));
						break;
					}
					
				} catch (InterruptedException ie) {}
			}
		});
		
		
	}
	
	final synchronized private void disableButtons() {
		
		netBtnFail.setEnabled(false);
		netBtnSuccess.setEnabled(false);
		netBtnTimeout.setEnabled(false);
	}
	
	final synchronized private void enableButtons() {
		netBtnFail.setEnabled(true);
		netBtnSuccess.setEnabled(true);
		netBtnTimeout.setEnabled(true);
	}
	

	// Simulates bad network conditions but randomly sending 
	final private void simulate(Channel leftIn, Channel leftOut, 
			Channel rightIn, Channel rightOut) throws InterruptedException {
		int val;

		while (true) {

			Message recvMsg= leftIn.listen();

			if (recvMsg.getType() == Message.Type.TIMEOUT || 
					recvMsg.getType() == Message.Type.FAILURE) {
				val = 0;
			} else {
				val = ThreadHelper.getRandom(Config.fairnessFactor + 2);
			}


			//ThreadHelper.threadMessage("BN has "+val);
			
			if (val < Config.fairnessFactor || Config.fairnessFactor == 0) { // success
				rightOut.send(recvMsg); // Relay message
				
			} else if(val == Config.fairnessFactor) { // failure
				leftOut.send(new Message(Message.Type.FAILURE));
			} else { // timeout

				Thread.sleep(1000);
				ThreadHelper.threadMessage("TIMEOUT OCCURED!");
				leftOut.send(new Message(Message.Type.TIMEOUT ));
			}
		}
	}

	final private class LeftInterface implements Runnable {
		// Listen on Left In	
		@Override
		public void run() {

			try {
				//simulate(leftIn_, leftOut_, rightIn_, rightOut_);
				while (true) {
					transitionLeftMsg= leftIn_.listen();
					if (transitionLeftMsg.msg_.equals(Message.Type.TIMEOUT)) {
						rightOut_.send(transitionLeftMsg);
					} else {
						currentState = Status.MSGLEFT;
						enableButtons();
					}
				}
				
			} catch (InterruptedException e1) {}
		}

	}


	final public class RightInterface implements Runnable {
		@Override
		public void run() {
			try {
				while(true) {
					transitionRightMsg = rightIn_.listen();
					if (transitionRightMsg.msg_.equals(Message.Type.TIMEOUT)) {
						
						leftOut_.send(transitionRightMsg);
					} else {
						currentState = Status.MSGRIGHT;
						enableButtons();
					}
				}
				//simulate(rightIn_, rightOut_, leftIn_, leftOut_);
			} catch (InterruptedException e1) {}			
		}

	}



	@Override
	public void run() {

		Thread leftInterface = new Thread(new LeftInterface());
		Thread rightInterface = new Thread(new RightInterface());
		leftInterface.start();
		rightInterface.start();


	}


}
