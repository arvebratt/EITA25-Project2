package util;

public class Person {
	private String name;
	private Division division;
	protected Type type;
	
	public Person(String name, Division division, Type type) {
		this.name = name;
		this.division = division;
		this.type = type;
	}
	
	public String getName() {
		return name;
	}
	
	public Division getDivision() {
		return division;
	}
	
	public Type getType() {
		return type;
	}
	
	public String toString() {
		return getName() + ", you are logged in as a " + getType() + " and are in the division of " + division.toString() + ".";
	}
}
