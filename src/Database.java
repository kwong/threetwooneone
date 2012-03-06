import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.concurrent.Semaphore;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;



/* 
 * =dbLChannelIn===>|-------|
 *                  |  DB   |
 * <==dbLChannelOut=|-------|
 * 
 */

public class Database extends JPanel implements Runnable {

	private ArrayList<Interface> interfaces = new ArrayList<Interface>();
	private BitSet readingRecord = new BitSet();
	private final Semaphore mutex = new Semaphore(1, true);
	
	private JTable eventTable;
	private DefaultTableModel dModel;
	private JLabel dbLbl;
	private int count =0;
	private JScrollPane dbSPane; 

	public Database() throws IOException {
		
		/* Swing */
		dbLbl = new JLabel("Database");
	
		dModel = new DefaultTableModel(new String[][] {{}}, new String[] {"#","Database Event"}) {

			private static final long serialVersionUID = 2680053310700606735L;

			public boolean isCellEditable(int row, int col){
				return false;
			}			

		};
		dModel.removeRow(0);
		//setPreferredSize(Config.DB_SIZE);
	
		eventTable = new JTable(dModel);	
		eventTable.getColumnModel().getColumn(1).setPreferredWidth(1000);
							
		dbLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		dbSPane = new JScrollPane(eventTable);
		dbSPane.setAutoscrolls(true);
				
		dbSPane.getVerticalScrollBar().setValue(dbSPane.getVerticalScrollBar().getMaximum());
		
	
		add(dbLbl);
		add(dbSPane);
		
		//setPreferredSize(Config.ENTITY_SIZE);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createMatteBorder(1, 2, 1, 1,Color.black));
		
	}

	public void connectDB(Channel leftIn, Channel leftOut) {
		interfaces.add(new Interface(leftIn, leftOut));
	}
	
	private synchronized void flipBit(int n) {
		readingRecord.flip(n);
	}
	
	private synchronized String incEventNum() {
		StringBuilder n = new StringBuilder();
		return n.append(count ++).toString();
	}
	
	/* Dirty Autoscroll hack */
	private synchronized void autoScroll() {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				eventTable.scrollRectToVisible(eventTable.getCellRect(eventTable.getRowCount()-1, eventTable.getColumnCount(), true));
			}
		});
	}

	/*************************************\
	 * Simulates Listening on Cloud2DB    *
	\*************************************/
	
	private class Interface implements Runnable {
		
		
		final private Channel leftIn_, leftOut_;
		private Message lastMsgSentOnLeft_;
		private int idServed;
		public Interface(Channel leftIn, Channel leftOut) {
			leftIn_ = leftIn;
			leftOut_ = leftOut;
			
			
		}
		@Override
		public void run() {
			try {
				while (true) {

					String info;
					Message l_in = leftIn_.listen();
					
					
					
					
					switch (l_in.getType()) {
					
					/* When we receive GETPIN, we can send GETPINOK to Cloud */ 
					case GETPIN:
						idServed = l_in.atmId_;
						info = "Received GETPIN <AccountID:"+l_in.user_+">";
						dModel.addRow(new String[]{ incEventNum(), info});
						ThreadHelper.threadMessage(info, "DB");
						
						Message pinOKMsg = new Message(Message.Type.GETPINOK, l_in.user_, l_in.atmId_);
						
						info = "Sending GETPINOK <AccountID:"+l_in.user_+"> to Cloud"+idServed;
						dModel.addRow(new String[]{ incEventNum(), info});
						ThreadHelper.threadMessage(info, "DB");
						
						
						
						autoScroll();
																	
						
						lastMsgSentOnLeft_ = pinOKMsg;
						
						leftOut_.send(pinOKMsg);


						break;
						
					/* When we receive a WITHDRAW request, we can send a WITHDRAWOK to confirm withdrawal */	
					case SETBALANCE:

						//ThreadHelper.threadMessage("DB: Received WITHDRAW request from Cloud");
						info = "Received WITHDRAW <AccountID:"+l_in.user_+">";
						dModel.addRow(new String[]{ incEventNum(), info});
						ThreadHelper.threadMessage(info, "DB");
						
						Message withOKMsg = new Message(Message.Type.SETBALANCEOK, l_in.user_, idServed);
						readingRecord.clear(l_in.user_);
						
						info = "Performed SETBALANCE <AccountID:"+l_in.user_+"> for Cloud"+idServed;
						dModel.addRow(new String[]{ incEventNum(), info});
						ThreadHelper.threadMessage(info, "DB");
						
						info = "Sending SETBALANCEOK <AccountID:"+l_in.user_+"> to Cloud"+idServed;
						dModel.addRow(new String[]{ incEventNum(), info});
						ThreadHelper.threadMessage(info, "DB");
						
						autoScroll();
						lastMsgSentOnLeft_ = withOKMsg;
						leftOut_.send(withOKMsg);					

						break;
						
					/* When we receive a RETRIEVERECORD request, we can send a RETRIEVERECORDOK 
					 * (which means, we are sending the record back to cloud) */
					case GETBALANCE:
						mutex.acquire();
						if (readingRecord.get(l_in.user_) == false) {
							flipBit(l_in.user_);
						} else {
							
							info = "Received RETRIEVERECORD <AccountID:"+l_in.user_+"> from Cloud"+idServed;
							dModel.addRow(new String[]{ incEventNum(), info});
							ThreadHelper.threadMessage(info, "DB");
							
							
							info = "Another user <AccountID:"+ l_in.user_ + "> sending GETBALANCE from ATM"+idServed;
							dModel.addRow(new String[]{ "!", info});
							ThreadHelper.threadMessage(info, "DB");
							
							
						}
						mutex.release();
						
						info = "Received GETBALANCE <AccountID:"+l_in.user_+">";
						dModel.addRow(new String[]{ incEventNum(), info});
						ThreadHelper.threadMessage(info, "DB");
						
						info = "Performed GETBALANCE <AccountID:"+l_in.user_+">";
						dModel.addRow(new String[]{ incEventNum(), info});
						ThreadHelper.threadMessage(info, "DB");
						
						Message retrieveOKMsg = new Message(Message.Type.GETBALANCEOK, l_in.user_, idServed);
						
						info = "Sending GETBALANCEOK <AccountID:"+l_in.user_+"> to ATM"+idServed;
						dModel.addRow(new String[]{ incEventNum(), info});
						ThreadHelper.threadMessage(info, "DB");
						
						autoScroll();
						lastMsgSentOnLeft_ = retrieveOKMsg;
						leftOut_.send(retrieveOKMsg);					

						break;
						
					/* When we receive a TIMEOUT, we relay the timeout back 
					 * into the network (which eventually propagates to ATM)
					 */
					case TIMEOUT: 
						
						info = "Received TIMEOUT ";
						dModel.addRow(new String[]{ incEventNum(), info});
						ThreadHelper.threadMessage(info, "DB");
						//ThreadHelper.threadMessage("DB: Network Timeout");

						lastMsgSentOnLeft_ = l_in;
						
						info = "Warning ATM"+l_in.atmId_+ " of TIMEOUT";
						dModel.addRow(new String[]{ incEventNum(), info});
						ThreadHelper.threadMessage(info, "DB");
						
						leftOut_.send(l_in); // relay


						break;
						
						
					/*	When we receive a FAILURE, we resend the previously sent message
					 * 	
					 */
					case FAILURE:
						
						info = "Network failure";
						dModel.addRow(new String[]{ incEventNum(), info});
						ThreadHelper.threadMessage(info, "DB");
						
						info = "Resending "+lastMsgSentOnLeft_ + " to Cloud"+idServed;
						dModel.addRow(new String[]{ incEventNum(), info});
						ThreadHelper.threadMessage(info, "DB");
						
						autoScroll();
						leftOut_.send(lastMsgSentOnLeft_);
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

	@Override
	public void run() {
		//Thread leftInterface = new Thread(new Interface());
		//leftInterface.start();
		for (Interface inter : interfaces) {
			Thread t = new Thread(inter);
			t.start();
		}

	}

}