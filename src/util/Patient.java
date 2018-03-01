package util;

public class Patient extends Person {
	private String data;
	
	public Patient(String name, String data, Division division) {
		super(name, division, Type.PATIENT);
		this.data = data;
	}
	
	public String getData() {
		return data;
	}
}
