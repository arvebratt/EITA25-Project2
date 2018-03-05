package util;

public class Log {
	private String person;
	private Operation operation;
	private String time;
	private boolean status;
	
	public Log(String person, Operation operation, String time, boolean status) {
		this.person = person;
		this.operation = operation;
		this.time = time;
		this.status = status;
	}
	
	public String toString() {
		return "Person: " + person + "\nOperation: " + operation.toString() + 
				"\nTime: " + time + "\nStatus: " + status;
	}
