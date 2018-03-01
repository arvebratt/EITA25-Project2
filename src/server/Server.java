package server;

import java.io.*;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import javax.net.ssl.*;
import util.*;

public class Server {

	// Initiate system before starting the server
	private static HashMap<Integer, Division> divisions;
	private static ArrayList<Doctor> doctors;
	private static ArrayList<Nurse> nurses;
	private static ArrayList<Patient> patients;
	private static ArrayList<Record> records;
	private static GovAgency govAgency;
	private static PasswordGenVal pgv = new PasswordGenVal();
	private static ArrayList<Log> logger;
	private static final int PORT = 5678;

	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException{
		System.setProperty("javax.net.ssl.trustStore", "./certificates/servertruststore");

		//Creating Divisions from http://www.childrenshospital.org/centers-and-services/departments-and-divisions
		divisions = new HashMap<>();
		divisions.put(0, new Division("Department of Medicine"));
		divisions.put(1, new Division("Department of Neurology"));
		divisions.put(2, new Division("Department of Neurosurgery"));
		divisions.put(3, new Division("Department of Plastic And Oral Surgery"));
		divisions.put(4, new Division("Department of Radiology"));
		divisions.put(5, new Division("Division of Infectious Diseases"));
		divisions.put(6, new Division("Division of Newborn Medicine"));

		//Creating Doctors with Divisions
		doctors = new ArrayList<>();
		doctors.add(new Doctor("doctor0", divisions.get(0)));
		doctors.add(new Doctor("doctor1", divisions.get(1)));
		//Creating Nurses with Divisions
		nurses = new ArrayList<>();
		nurses.add(new Nurse("nurse0", divisions.get(0)));
		nurses.add(new Nurse("nurse1", divisions.get(1)));
		//Creating Patients with Data and Divisions
		patients = new ArrayList<>();
		patients.add(new Patient("patient0","Broken nail", divisions.get(0)));
		patients.add(new Patient("patient1","Left foot fell off", divisions.get(1)));
		patients.add(new Patient("patient2","Broken hair", divisions.get(0)));
		patients.add(new Patient("patient3","green eye", divisions.get(1)));
		//		Creating a GovermentAgency
		govAgency = new GovAgency("govermentAgency", divisions.get(0));

		// creating hashed password for all persons
		pgv.createUser("doctor0", "doc0");
		pgv.createUser("doctor1", "doc1");
		pgv.createUser("nurse0", "nur0");
		pgv.createUser("nurse1", "nur1");
		pgv.createUser("patient0", "pat0");
		pgv.createUser("patient1", "pat1");
		pgv.createUser("patient2", "pat2");
		pgv.createUser("patient3", "pat3");
		pgv.createUser("govermentAgency", "gov");

		//Creating Records with Patient, Doctor, Nurse and data
		records = new ArrayList<>();
		records.add(new Record(patients.get(0), doctors.get(0), nurses.get(0)));
		records.add(new Record(patients.get(1), doctors.get(1), nurses.get(1)));
		records.add(new Record(patients.get(2), doctors.get(1), nurses.get(0)));
		records.add(new Record(patients.get(3), doctors.get(1), nurses.get(1)));
		
		//Creating the Server Logger
		logger = new ArrayList<>();

		// Start the server
		Server s = new Server();
		s.run();
	}

	private void run() {

		try {
			// Create SSL Socket which will wait and listen for a request.
			// BufferedReader and -Writer are instantiated
			// for transmitting and receiving data.
			SSLSocket client;

			char[] passphrase = "password".toCharArray();

			SSLContext ctx = SSLContext.getInstance("TLS");
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			KeyStore ks = KeyStore.getInstance("JKS");
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");

			ks.load(new FileInputStream("./certificates/serverkeystore"), passphrase);

			kmf.init(ks, passphrase);
			tmf.init(ks);
			ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

			SSLServerSocketFactory factory = ctx.getServerSocketFactory();
			SSLServerSocket ss = (SSLServerSocket) factory.createServerSocket(PORT);
			ss.setNeedClientAuth(true);

			System.out.println("Running server ...");
			System.out.println(ss);
			System.out.println("Server is listening on port " + PORT);

			while (true) {

				client = (SSLSocket) ss.accept();
				printSocketInfo(client);
				System.out.println("Client connected ...");

				PrintWriter toClient = new PrintWriter(new PrintWriter(client.getOutputStream()));
				BufferedReader fromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));

				NetworkUtility nu = new NetworkUtility(toClient, fromClient);

				String username = nu.receive();

				String password = null;
				int maxCount = 4;
				int count = 1;
				boolean authenticated = false;
				while (!authenticated && count <= maxCount) {
					password = nu.receive();
					authenticated = pgv.authenticate(username, password);
					if (!authenticated) {
						nu.send("fail");
						count++;
					}
				}

				// Terminate connection due to too many failed attempts
				if (count > maxCount) {
					toClient.close();
					fromClient.close();
					client.close();
					
					// Log the failed attempt
					logger.add(new Log(username, Operation.LOGIN, Calendar.getInstance().getTime().toString(), false));
					continue;
				}

				nu.send("accepted");
				// Log the successful attempt
				logger.add(new Log(username, Operation.LOGIN, Calendar.getInstance().getTime().toString(), true));

				System.out.println("Client connected ...\nLogging in client ...");

				//Matching the Username to the right Person
				boolean find = false;
				Person current = null;
				while(!find) {
					for(Patient a: patients) {
						if(username.equals(a.getName())) {
							current = a;
							find = true;
						}
					}
					for(Nurse a: nurses) {
						if(username.equals(a.getName())) {
							current = a;
							find = true;
						}
					}
					for(Doctor a: doctors) {
						if(username.equals(a.getName())) {
							current = a;
							find = true;
						}
					}
					if(username.equals(govAgency.getName())) {
						current = govAgency;
						find = true;
					}
				}
				
				//Logging if the Person exists
				if(current == null) {
					logger.add(new Log(username, Operation.PERSON_EXISTS, Calendar.getInstance().getTime().toString(), false));
				} else {
					logger.add(new Log(username, Operation.PERSON_EXISTS, Calendar.getInstance().getTime().toString(), true));
				}

				nu.send(current.toString());				

				//Running all choices for the user
				int choice = 10;
				while(true) {
					choice = 10;
					nu.send("(1) List Record: \n(2) Write to Record: \n(3) Write new Record: "
							+ "\n(4) Delete Record: \n(5) Exit: ");
					String get = nu.receive();
					try{
						choice = Integer.parseInt(get);
					}
					catch (NumberFormatException ex) {
						choice = 10;     
					}
					switch(choice) {
					case 1:
						nu.send("1"); 
						nu.send(printList(listRead(current, records)));
						break;
					case 2:
						nu.send("2");
						nu.send(printList(listWrite(current, records)));
						int num = Integer.parseInt(nu.receive()) - 1;
						String newData = nu.receive();
						listWrite(current, records).get(num).writeToRecord(newData);
						break;
					case 3:
						nu.send("3");
						if(!current.getType().equals(Type.DOCTOR)) {
							nu.send("bye");
							break;
						} else {
							nu.send("hi");
							nu.send(listNurses(nurses));
							int nurseNum = Integer.parseInt(nu.receive()) - 1;
							String patientName = nu.receive();
							String patientData = nu.receive();
							nu.send(listDivisions(divisions));
							int divNum = Integer.parseInt(nu.receive()) - 1;
							patients.add(new Patient(patientName, patientData, divisions.get(divNum)));
							pgv.createUser(patientName, nu.receive());
							records.add(new Record(patients.get(patients.size() - 1), 
									getDoctor(username, doctors), nurses.get(nurseNum)));
							break;
						}
					case 4:
						nu.send("4");
						if(!current.getType().equals(Type.GOVERMENTAGENCY)) {
							nu.send("bye");
							break;
						} else {
							nu.send(printList(records));
							int recordNum = Integer.parseInt(nu.receive()) - 1;
							records.remove(recordNum);
							break;
						}
					case 5:
						nu.send("5");
						break;
					default: 
						nu.send("You must choose a number between 1 and 5.");
						break;
					}
				}


			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private ArrayList<Record> listRead(Person person, ArrayList<Record> records) {
		ArrayList<Record> recordList = new ArrayList<>();
		for(Record a: records) {
			if(a.read(person)) {
				recordList.add(a);
			}
		}
		return recordList;
	}

	private ArrayList<Record> listWrite(Person person, ArrayList<Record> records) {
		ArrayList<Record> recordList = new ArrayList<>();
		for(Record a: records) {
			if(a.write(person)) {
				recordList.add(a);
			}
		}
		return recordList;
	}

	private String printList(ArrayList<Record> records) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < records.size(); i++) { 			
			sb.append("(" + (i+1) + ") " + records.get(i).toString() + "\n");
		} 
		return sb.toString();
	}

	private String listNurses(ArrayList<Nurse> nurses) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < nurses.size(); i++) { 			
			sb.append("(" + (i+1) + ") " + nurses.get(i).getName() + "\n");
		} 
		return sb.toString();
	}

	private String listDivisions(HashMap<Integer, Division> divisions) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < divisions.size(); i++) { 			
			sb.append("(" + (i+1) + ") " + divisions.get(i).toString() + "\n");
		} 
		return sb.toString();
	}

	private Doctor getDoctor(String doctor, ArrayList<Doctor> doctors) {
		for(int i = 0; i < doctors.size(); i++) { 			
			if(doctors.get(i).getName().equals(doctor)) {
				return doctors.get(i);
			}
		} 
		return null;
	}
	
	private static void printSocketInfo(SSLSocket s) {
		System.out.println("-> New client connecting:");
		System.out.println("Socket class: " + s.getClass());
		System.out.println("   Remote address = "
				+ s.getInetAddress().toString());
		System.out.println("   Remote port = " + s.getPort());
		System.out.println("   Local socket address = "
				+ s.getLocalSocketAddress().toString());
		System.out.println("   Local address = "
				+ s.getLocalAddress().toString());
		System.out.println("   Local port = " + s.getLocalPort());
		System.out.println("   Need client authentication = "
				+ s.getNeedClientAuth());
		SSLSession ss = s.getSession();
		System.out.println("   Cipher suite = " + ss.getCipherSuite());
		System.out.println("   Protocol = " + ss.getProtocol());
	}
}












