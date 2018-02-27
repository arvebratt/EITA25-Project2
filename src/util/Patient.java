package util;

public class Patient {
	private String name;
	private String data;
	private Division division;
	
	public Patient(String name, String data, Division division) {
		this.name = name;
		this.data = data;
		this.division = division;
	}
	
	public String getPatient() {
		return name;
	}
	
	public String getPatientData() {
		return data;
	}
	
	public Division getPatientDivision() {
		return division;
	}
}
