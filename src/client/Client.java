package client;

import java.net.*;
import java.io.*;
import javax.net.ssl.*;
import util.*;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.*;
import java.util.Scanner;

/*
 * This example shows how to set up a key manager to perform client
 * authentication.
 *
 * This program assumes that the client is not inside a firewall.
 * The application can be modified to connect to a server outside
 * the firewall by following SSLSocketClientWithTunneling.java.
 */
public class Client {
	private static final int PORT = 5678;
	private static Scanner scan;

	public static void main(String[] args) throws Exception {

		System.setProperty("javax.net.ssl.trustStore", "./certificates/clienttruststore");

		SSLSocketFactory factory = null;
		SSLContext ctx = null;
		KeyManagerFactory kmf = null;
		KeyStore ks = null;
		TrustManagerFactory tmf = null;

		boolean notFound = true;
		FileInputStream stream = null;
		String pass = null;
		String username = null;

		while (notFound) {
			try {
				scan = new Scanner(System.in);
				System.out.print("Username: ");
				username = scan.nextLine();
				System.out.print("Certificate Passphrase: ");
				pass = scan.nextLine();

				stream = new FileInputStream("./certificates/clientkeystores/" + username + "keystore");
				notFound = false;
			} catch (FileNotFoundException e) {
				notFound = true;
				System.out.println("Wrong name or password, please try again");
			}
		}

		try {
			char[] passphrase = pass.toCharArray();

			ctx = SSLContext.getInstance("TLS");
			kmf = KeyManagerFactory.getInstance("SunX509");
			ks = KeyStore.getInstance("JKS");
			tmf = TrustManagerFactory.getInstance("SunX509");

			ks.load(stream, passphrase);
			kmf.init(ks, passphrase);
			tmf.init(ks);
			ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

			factory = ctx.getSocketFactory();

			SSLSocket client = (SSLSocket) factory.createSocket("localhost", PORT);

			client.setUseClientMode(true);
			client.startHandshake();
			System.out.println(client);

			PrintWriter toServer = new PrintWriter(new OutputStreamWriter(client.getOutputStream()), true);
			BufferedReader fromServer = new BufferedReader(new InputStreamReader(client.getInputStream()));

			NetworkUtility nu = new NetworkUtility(toServer, fromServer);

			nu.send(username);
			String status = null;
			do {
				System.out.println("Password: ");
				String pw = scan.nextLine();
				nu.send(pw);
				pw = null;

				status = nu.receive();
				if (status == null) {
					// Connection closed...
					System.exit(-1);
				}
			} while (!status.equals("accepted"));

			// Parse welcome message
			System.out.println("Welcome " + nu.receive());

			boolean run = true;
			while(run) {
				System.out.println(nu.receive());
				System.out.print("Enter command: ");
				nu.send(scan.nextLine());
				// Read - input from client -> server
				switch(Integer.parseInt(nu.receive())) {
				case 1:
					System.out.println(nu.receive());
					break;
				case 2:
					System.out.println(nu.receive());
					System.out.print("Choose a Patient: ");
					nu.send(scan.nextLine());
					System.out.print("Write new Data: ");
					nu.send(scan.nextLine());
					break;
				case 3:
					if(nu.receive().equals("bye")) {
						System.out.println("You must be a Doctor to write a new record");
						break;
					} else {
						System.out.println(nu.receive());
						System.out.print("Choose a Nurse: ");
						nu.send(scan.nextLine());
						System.out.print("Name Patient: ");
						nu.send(scan.nextLine());
						System.out.print("Write patient data: ");
						nu.send(scan.nextLine());
						System.out.println(nu.receive());
						System.out.print("Choose a Division: ");
						nu.send(scan.nextLine());
						System.out.print("Choose the patients password: ");
						nu.send(scan.nextLine());
						break;
					}
				case 4:
					if(nu.receive().equals("bye")) {
						System.out.println("You must be a goverment Agency to delete a record");
						break;
					} else {
						System.out.println(nu.receive());
						System.out.print("Choose a record to delete: ");
						nu.send(scan.nextLine());
						break;
					}					
				case 5:
					System.exit(-1);
					break;
				default:
					break;
				}
			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}
}