package server;

import java.io.*;
import java.net.*;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.*;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;

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

	private ServerSocket serverSocket = null;
	private static final int PORT = 5678;
	private static int numConnectedClients = 0;

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
		doctors.add(new Doctor("Doctor0", divisions.get(0)));
		doctors.add(new Doctor("Doctor1", divisions.get(1)));

		// creating the hashed passwords for Doctors
		pgv.createUser("Doctor0", "doc0");
		pgv.createUser("Doctor1", "doc1");

		//Creating Nurses with Divisions
		nurses = new ArrayList<>();
		nurses.add(new Nurse("Nurse0", divisions.get(0)));
		nurses.add(new Nurse("Nurse1", divisions.get(1)));

		// creating the hashed passwords for Nurses
		pgv.createUser("Nurse0", "nur0");
		pgv.createUser("Nurse1", "nur1");

		//Creating Patients with Data and Divisions
		patients = new ArrayList<>();
		patients.add(new Patient("Patient0","Broken nail", divisions.get(0)));
		patients.add(new Patient("Patient1","Left foot fell off", divisions.get(1)));

		// creating the hashed passwords for Patients
		pgv.createUser("Patient0", "pat0");
		pgv.createUser("Patient1", "pat1");

		//Creating a GovermentAgency
		govAgency = new GovAgency("GovermentAgency");

		// creating hashed password for the GovermentAgency
		pgv.createUser("GovermentAgency", "gov");

		//Creating Records with Patient, Doctor, Nurse and data
		records = new ArrayList<>();
		records.add(new Record(patients.get(0), doctors.get(0), nurses.get(0)));
		records.add(new Record(patients.get(1), doctors.get(1), nurses.get(1)));

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
			String readLine = null;

			char[] passphrase = "password".toCharArray();

			SSLContext ctx = SSLContext.getInstance("TLS");
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			KeyStore ks = KeyStore.getInstance("JKS");
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");

			ks.load(new FileInputStream("./certificates/serverkeystore"), passphrase); //Fix

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
				//printSocketInfo(client);//Fix
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


				if (count > maxCount) {
					// Terminate connection due to too many failed attempts
					toClient.close();
					fromClient.close();
					client.close();
					continue;
				}

				nu.send("accepted");

				System.out.println("Client connected ...");

				System.out.println("Logging in client ...");

				System.out.println("Welcome " + username);
				
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}