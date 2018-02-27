package util;

public class Nurse {
	private Division division;
	private String name;
	
	public Nurse(String name, Division division) {
		this.division = division;
		this.name = name;
	}
	
	public String getNurse() {
		return name;
	}
	
	public Division getDivision() {
		return division;
	}
}
