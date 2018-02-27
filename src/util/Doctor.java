package util;

public class Doctor {
	private Division division;
	private String name;
	
	public Doctor(String name, Division division) {
		this.division = division;
		this.name = name;
	}
	
	public String getDoctor() {
		return name;
	}
	
	public Division getDivision() {
		return division;
	}
}
