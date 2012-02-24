
public class Message {
	final Type msg_;
	
	public Message(Type msg) {
		msg_ = msg;
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
		FAILURE,
		TIMEOUT,
		
	}
}
