//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Student name: Lance Baker 
// Course: SENG6400 (Network & Distributed Computing)
// Student number: c3128034
// Assignment title: SENG6400 Assignment 1 
// File name: SoundClient
// Created: 17-08-2013
// Last Change: 25-08-2013
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

import java.io.*;
import java.net.*;
import java.util.regex.*;

public class SoundClient extends Socket {
	
	private static final String SEPARATOR = ": ";
	private static final String CONFIRM = SEPARATOR + "OK";
	private static final String INIT_REQUEST = "SOUND";
	private static final String INIT_CONFIRM = INIT_REQUEST + CONFIRM;
	private static final String BYE_CONFIRM = "BYE" + CONFIRM;
	private static final String END_CONFIRM = "END" + CONFIRM;
	private static final String QUERY_CONFIRM = "QUERY" + CONFIRM;
	private static final String SERVER = "SERVER" + SEPARATOR;
	private static final String CLIENT = "CLIENT" + SEPARATOR;
	private static final String ERR_SERVER_NOT_RESPONDING = "SERVER NOT RESPONDING";
	private static final String MENU = "Enter command (STORE, QUERY, BYE, END) or animal" + SEPARATOR;
	private static final String REGEX_IPADDRESS = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
	private static final String INVALID_PORT = "Please enter a valid port (must be an integer value)";
	private static final String INVALID_IPADDRESS = "Please enter a valid ipv4 address ([0-255].[0-255].[0-255].[0-255])";
	private static final String INVALID_ARGS = "You are required to specify an ip address, or a port.";
	private static final String INDENTED_GAP = "	";
	private static final String DEFAULT_SERVER = "127.0.0.1";
	private static final String NEW_LINE = System.getProperty("line.separator");
	private static final String STORE = "STORE";
	private static final String QUERY = "QUERY";
	
	private static final int DEFAULT_PORT = 8080;

	private PrintWriter out; // Used to send output to the server
	private BufferedReader in; // The output from the server
	private BufferedReader clientInput;
	
	/** 
	* The constructor to instantiate the SoundClient object.
	* It receives the hostname, and also the port.
	*/
	public SoundClient(String hostname, int port) throws Exception {
		super(hostname, port); // Instantiates the socket super class.
		this.out = new PrintWriter(this.getOutputStream(), true); // Grabs the output stream from the Socket (which is this object)
		this.in = new BufferedReader(new InputStreamReader(this.getInputStream())); // Gets the input stream from the Socket, and places it in a stream reader - then to a buffer.
		this.promptMenu(); // Starts the client system; by entering a continuous keyboard input loop.
	}

	/**
	* The isEstablished method sends the intial SOUND message to the server. It then fetches
	* the response from the server, and displays to the user. It returns a boolean value indicating
	* whether the message was SOUND: OK.
	*/
	private boolean isEstablished() throws Exception {
		out.println(INIT_REQUEST); // Sends 'SOUND' to the server
		System.out.println(CLIENT + INIT_REQUEST); // Displays sent 'SOUND' message to client.
		String response = this.in.readLine(); // Grabs the response output from the server
		if (response != null) { // If the response was something other than nothing
			System.out.println(SERVER + response); // Displays output response to the client.
			System.out.print(NEW_LINE);
			return (response.equals(INIT_CONFIRM)); // Returns a boolean value indicating whether the response was SOUND: OK
		} else {
			// The response from the server was nothing, and hence the server isn't responding.
			System.out.println(ERR_SERVER_NOT_RESPONDING);
			return false; // Returns false indicating the connection was not established.
		}
	}
	
	/**
	* The promptMenu method firstly checks whether the connection is established. It will then
	* repeatedly prompt the user for input, which is then forwarded onto the server. The response
	* is then outputted back to the user.
	*/
	private void promptMenu() {
		try {
			// Checks whether the client-server connection is established; by sending the SOUND request.
			if (this.isEstablished()) {
				String input;
				this.clientInput = new BufferedReader(new InputStreamReader(System.in));
				
				do {
					// Prompts the user for input.
					System.out.print(MENU);
					input = this.clientInput.readLine();
					try {
						// It then converts the received input to uppercase, sending the request to the server.
						String response = this.request(input.toUpperCase());
						System.out.print(SERVER);
						System.out.println(response); // Outputs the response.
						// If the response was a bye, or an end confirmation then it should exit from the while loop - ending the program.
						if (response.equals(BYE_CONFIRM) || response.equals(END_CONFIRM)) {
							break;
						}
						System.out.print(NEW_LINE);
					} catch (Exception ex) {
						System.out.println(ex.getMessage());
					}
				} while (input != null); // Iterates while there is input.
			}
			// If the loop has ended, then the application has;
			// so the connections will need to be closed.
			this.out.close();
			this.in.close();
			this.close(); // Closes itself (since this object is a Socket).
			
		} catch (Exception ex) {
			// Outputs any exceptions. 
            System.err.println(ex.getMessage());
        }
	}
	
	/**
	* The request method sends the request to the server and receives a response.
	* The STORE request additionally prompts for the animal, and also sound; transmitting both to the server.
	* The QUERY command receives all the sounds recorded.
	*/ 
	private String request(String request) throws Exception {
		System.out.println(CLIENT + request); // Shows input to client
		this.out.println(request); // Sends the request message to server
		
		StringBuilder response = new StringBuilder();
		switch(request) {
			case STORE:
				// Prints the indented gap so its aligned with Server.
				// Prompts for input for both the animal & sound.
				System.out.print(INDENTED_GAP); 
				String animal = this.clientInput.readLine();
				System.out.print(INDENTED_GAP);
				String sound = this.clientInput.readLine();
				// Converts to uppercase and sends to server.
				this.out.println(animal.toUpperCase());
				this.out.println(sound.toUpperCase());
				// Appends the response.
				response.append(this.in.readLine());
				break;
			case QUERY:
				String value;
				int counter = 0;
				do {
					// Fetches the value from the Server.
					value = this.in.readLine();
					if (counter > 0) { // Appends an indented gap after the first one. So the next line is aligned with Server.
						response.append(INDENTED_GAP);
					}
					response.append(value); // Appends to the response.
					if (!value.equals(QUERY_CONFIRM)) { // If its not end of message append a new line.
						response.append(NEW_LINE);
					}
					counter++;
				} while (!value.equals(QUERY_CONFIRM)); // Iterates until end of message.
				
				break;
			default:
				response.append(this.in.readLine());
				break;
		}
		
		return response.toString(); // Returns the Response from server
	}
	
	/**
	* The isInteger method is a method used to determine whether a String value is a Integer.
	*/
	private static boolean isInteger(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} catch (Exception ex) {}
		return false;
	}

    public static void main(String[] args) throws IOException {
		try {
			Pattern pattern = Pattern.compile(REGEX_IPADDRESS);
			String ipAddress = DEFAULT_SERVER;
			int portNumber = DEFAULT_PORT;
			
			// If the argument length is one, it will either be an ip address or port.
			if (args.length == 1) {
				if (isInteger(args[0])) { // Checks whether its a valid integer.
					// If so, it sets the value to the port.
					portNumber = Integer.parseInt(args[0]);
				} else {
					// Otherwise, if the argument matches an ip address pattern
					if (pattern.matcher(args[0]).matches()) {
						// It will then have the new ip addresss.
						ipAddress = args[0];
					} else {
						// Invalid arguments, therefore throws an Exception to end.
						throw new Exception(INVALID_ARGS);
					}
				}
			// If the argument length is two, it will supply both.
			} else if (args.length == 2) {
				// If the first argument matches an ip address.
				if (pattern.matcher(args[0]).matches()) {
					// It will then check whether the second matches a integer.
					if (isInteger(args[1])) {
						// If both are valid, it will have its new values.
						ipAddress = args[0];
						portNumber = Integer.parseInt(args[1]);
					} else {
						throw new Exception(INVALID_PORT);
					}
				} else {
					throw new Exception(INVALID_IPADDRESS);
				}
			}
			
			// Instantiates the client with the ip address, and port.
			new SoundClient(ipAddress, portNumber);
			
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }
}