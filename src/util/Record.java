package util;

import java.security.AccessControlException;
import java.util.ArrayList;

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
	private ArrayList<Record> jurnals;

	public Record(Patient patient, Doctor doctor, Nurse nurse) {
		this.patient = patient;
		this.doctor = doctor;
		this.nurse = nurse;
		this.data = patient.getPatientData();
		this.division = doctor.getDivision();
		jurnals = new ArrayList<>();
	}
	
	public String patientRead(Patient patient) throws AccessControlException{
		if(patient.getPatient() == this.patient.getPatient()) {
			return data;
	}
		throw new AccessControlException("Not enough access to read.");
	}
	
	public String nurseRead(Nurse nurse) throws AccessControlException{
		if(nurse.getDivision() == this.division) {
			return data;
	}
		throw new AccessControlException("Not enough access to read.");
	}
	
	public void nurseWrite(Nurse nurse, String newData) throws AccessControlException{
		if(nurse.getNurse() == this.nurse.getNurse()) {
			data = newData;
	}
		throw new AccessControlException("Not enough access to write.");
	}
	
	public String doctorRead(Doctor doctor) throws AccessControlException{
		if(doctor.getDivision() == this.division) {
			return data;
	}
		throw new AccessControlException("Not enough access to read.");
	}
	
	public void doctorWrite(Doctor doctor, String newData) throws AccessControlException{
		if(doctor.getDoctor() == this.doctor.getDoctor()) {
			data = newData;
	}
		throw new AccessControlException("Not enough access to write.");
	}
	
	public void doctorNewRecord(Patient patient, Doctor doctor, Nurse nurse) {
		Record newRecord = new Record(patient, doctor, nurse);
		jurnals.add(newRecord);
	}
	
	public void govDelete(Record record) {
		record = new Record(null, null, null);
		jurnals.remove(record);
	}
	
	public String govRead(Record record) {
		return record.data;
	}
}






