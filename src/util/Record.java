package util;

import util.Division;
import util.Doctor;
import util.Nurse;
import util.Patient;

public class Record {
	private Patient patient;
	private Doctor doctor;
	private Nurse nurse;
	private Division division;
	private String data;

	public Record(Patient patient, Doctor doctor, Nurse nurse) {
		this.patient = patient;
		this.doctor = doctor;
		this.nurse = nurse;
		this.data = patient.getData();
		this.division = doctor.getDivision();
	}
	
	public boolean read(Person person) {
		if(person.getName() == this.patient.getName() || 
				person.getType() == Type.NURSE && person.getDivision() == this.division || 
				person.getType() == Type.DOCTOR && person.getDivision() == this.division 
				|| person.getType() == Type.GOVERMENTAGENCY) {
			return true;
	}
		return false;
	}
	
	public boolean write(Person person) {
		if(person.getName() == this.nurse.getName() || 
				person.getName() == this.doctor.getName()) {
			return true;
	}
		return false;
	}
	
	public void writeToRecord(String newData) {
			data = newData;
	}
	
	public String toString() {
		return "Patient: " + patient.getName() + " Nurse: " + nurse.getName() + " Doctor: "
				+ doctor.getName() + " Division: " + division.toString() + " Data: " + data;
	}
}






