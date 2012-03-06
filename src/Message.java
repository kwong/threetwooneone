
public class Message {
	final Type msg_;
	final int user_;
	final int atmId_;
	
	public Message(Type msg, int user, int atmId) {
		msg_ = msg;
		user_ = user;
		atmId_ = atmId;
	}
	
	public Message(Type msg) {
		msg_ = msg;
		user_ = 1;
		atmId_ = 1;
	}
	
	public Type getType() {
		return msg_;
	}
	
	public String toString() {
		return msg_.toString();
	}
	
	
	/* Emulates parameterized channels with one simplification,
	 * that is, TIMEOUTs, and FAILUREs of all message types share
	 * a single common name whereas PAT implementation has different
	 * name for each type of timeout/failure (e.g. AUTHFAILURE)
	 * */
	public enum Type {
		AUTH, 
		AUTHOK,
	
		GETPIN,
		GETPINOK,
	
		WITHDRAW,
		WITHDRAWOK,
		
		GETBALANCE,
		GETBALANCEOK,
		
		SETBALANCE,
		SETBALANCEOK,
	
		FAILURE,
		TIMEOUT,
		CANCEL,
		
		DEAUTH,
		DEAUTHOK,
		
	}
}
