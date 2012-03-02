
public class Message {
	final Type msg_;
	final int user_;
	
	public Message(Type msg, int user) {
		msg_ = msg;
		user_ = user;
	}
	
	public Message(Type msg) {
		msg_ = msg;
		user_ = 1;
	}
	
	public Type getType() {
		return msg_;
	}
	
	public String toString() {
		return msg_.toString();
	}
	
	public enum Type {
		AUTH, 
		AUTHOK,
		AUTHFAILURE,
		AUTHTIMEOUT,
		GETPIN,
		GETPINOK,
		GETPINFAILURE,
		GETPINTIMEOUT,
		WITHDRAW,
		WITHDRAWOK,
		WITHDRAWFAILURE,
		WITHDRAWTIMEOUT,
		RETRIEVEBALANCE,
		RETRIEVEBALANCEOK,
		RETRIEVEBALANCEFAILURE,
		RETRIEVEBALANCETIMEOUT,
		RETRIEVERECORD,
		RETRIEVERECORDOK,
		SETBALANCE,
		SETBALANCEOK,
		RECORD,
		FAILURE,
		TIMEOUT,
		
	}
}
