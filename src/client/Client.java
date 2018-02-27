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

				stream = new FileInputStream("./certificates/clientkeystore" /*"./certificates/" + username + "/" + username + ".jks"**/);
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
			System.out.println("Welcome: " + nu.receive());

			// http://en.wikipedia.org/wiki/REPL
			String response = null;
			String userInput = null;
			do {
				System.out.print("Enter command: ");

				// Read - input from client -> server
				userInput = scan.nextLine();
				System.out.println("Input: " + userInput);
				nu.send(userInput);

				// Eval - output form server -> client
				response = nu.receive();
				System.out.println("Read Server:");
				System.out.println(response);

				// Print - print result
				// System.out.println("Handled: " + response);

				// Loop - repeat!
			} while (response != null);

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