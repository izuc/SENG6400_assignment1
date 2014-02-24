//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Student name: Lance Baker 
// Course: SENG6400 (Network & Distributed Computing)
// Student number: c3128034
// Assignment title: SENG6400 Assignment 1 
// File name: SoundServer
// Created: 17-08-2013
// Last Change: 25-08-2013
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

import java.net.*;
import java.io.*;
import java.text.*;
import java.util.*;

public class SoundServer implements Runnable {
	private static final String SEPARATOR = ": ";
	private static final String REQUEST = "Request" + SEPARATOR;
	private static final String RESPONSE = "Response" + SEPARATOR;
	private static final String SOUND = "SOUND";
	private static final String OKAY = SEPARATOR + "OK";
	private static final String SOUND_OK = SOUND + OKAY;
	private static final String ERROR_MESSAGE = "ERR";
	private static final String BYE_OK = "BYE" + OKAY;
	private static final String END_OK = "END" + OKAY;
	private static final String STORE_OK = "STORE" + OKAY;
	private static final String QUERY_OK = "QUERY" + OKAY;
	private static final String SHUTDOWN_MESSAGE = "Server shutting down.";
	private static final String NEW_LINE = System.getProperty("line.separator");
	private static final String WAIT_MESSAGE = "Waiting on port: ";
	private static final String INVALID_PORT = "Please enter a valid port (must be an integer value)";
	private static final String INVALID_ARGS_AMOUNT = "You must specify a port to listen on.";
	private static final String SOUND_RESPONSE = "A %s SAYS %s";
	private static final String SOUND_NO_RESPONSE = "I DON'T KNOW %s";
	private static final int DEFAULT_PORT = 8080;
	private static final int MEMORY_LIMIT = 15;
	
	public enum Command {START, STORE, QUERY, BYE, END};
	private ServerSocket server;
	private Socket client;
	private PrintWriter out;
	private BufferedReader input; 
	private Command command;
	private HashMap<String, String> sounds; // Stores the sounds in a HashMap.
	
	public SoundServer(ServerSocket server, Socket client) throws Exception {
		this.server = server;
		this.client = client;
		this.input = new BufferedReader(new InputStreamReader(client.getInputStream()));
		this.out = new PrintWriter(client.getOutputStream(), true);
		this.command = Command.START;
		this.sounds = new HashMap<String, String>();
	}
	
	/**
	* The getCommand method receives a String and iterates throughout the commands enumerated type 
	* comparing each value. Once a match has been found, it will then return the enumerated type.
	*/
	private Command getCommand(String value) {
		for (Command command : Command.values()) {
			if (command.name().equalsIgnoreCase(value)) {
				return command;
			}
		}
		return null;
	}
	
	/**
	* The run method is executed by the Thread. It iterates throughout each request by the client,
	* and performs the command. It then transmits the response back to the client. 
	* If the request is the initial command (SOUND) then it will transmit a SOUND: OK response back to
	* the user. Otherwise, it will attempt to match the request with a command and then carry out that
	* operation. If the request isn't equal to a command then it will assume that the request is for a sound.
	*/
	public void run() {
		try {
			String request;
			while ((request = this.input.readLine()) != null) {
				System.out.println(REQUEST + request); 
				if ((this.command == Command.START) && (request.equals(SOUND))) {
					this.transmit(SOUND_OK);
				} else {
					Command command = this.getCommand(request);
					if (command != null) {
						this.doCommand(command);
					} else {
						if (this.sounds.containsKey(request)) {
							this.transmit(String.format(SOUND_RESPONSE, request, this.sounds.get(request)));
						} else {
							this.transmit(String.format(SOUND_NO_RESPONSE, request));
						}
					}
				}
			}
		} catch (Exception ex) {}
	}
	
	/**
	* The doCommand method receives a command (QUERY, STORE, BYE, END) and carries out the action.
	* The QUERY command will iterate and transmit the sounds recorded to the client.
	* The STORE command reads two String values from the client, and adds the values to the sound.
	* The BYE command terminates the current client connection.
	* The END command closes the client connection, and also terminates the server.
	*/
	private void doCommand(Command command) throws Exception {
		// The switch statement, used to determine which case to select.
		switch(command) {
			case QUERY:
				// Uses a StringBuilder to append the response.
				StringBuilder response = new StringBuilder();
				
				// Iterates for each element in the HashMap
				Set set = this.sounds.entrySet();
				Iterator i = set.iterator(); 
				while(i.hasNext()) { 
					Map.Entry me = (Map.Entry)i.next();
					// Outputs the keys
					response.append(me.getKey() + NEW_LINE);
				}
				// Appends QUERY: OK to inform the end of message.
				response.append(QUERY_OK);
				
				// Transmits the message to the client.
				this.transmit(response.toString());
				break;
			case STORE:
				// Receives the two inputs from the client.
				String animal = this.input.readLine();
				String sound = this.input.readLine();
				// Checks whether there is enough room
				if (this.sounds.size() < MEMORY_LIMIT) {
					// Puts the animal & sound into the HashMap.
					this.sounds.put(animal, sound);
				}
				// Transmits an Okay.
				this.transmit(STORE_OK);
				break;
			case BYE:
				// Transmits an Okay.
				this.transmit(BYE_OK);
				// Closes the client connection.
				this.client.close();
				this.out.close();
				this.input.close();
				break;
			case END:
				// Transmits an Okay.
				this.transmit(END_OK);
				// Closes the client connection.
				this.client.close();
				this.out.close();
				this.input.close();
				this.server.close(); // Closes the server socket.
				System.out.println(SHUTDOWN_MESSAGE);
				break;
		}
	}
	
	/**
	* The transmit method accepts a String value, 
	* which is then transmitted to the connected client.
	*/
	private void transmit(String data) {
		this.out.println(data);
		System.out.println(RESPONSE + data); // The sent data is also printed to the Server.
	}
	
	/**
	* The isInteger method is a method used to determine whether a String value is a Integer.
	*/
	private static boolean isInteger(String input) {
		try {
			Integer.parseInt(input); // Atempts to convert the input
			return true;
		} catch (Exception ex) {} // Skips if unsuccessful
		return false;
	}
	
	/**
	* The main method, which is the default method that gets called first.
	* It receives the arguments sent via the CLI. It instantiates a ServerSocket on the received port 
	* and iterates for each client connection accepted. With each connection 
	* received, it will then instantiate a SoundServer object in a new Thread.
	*/
    public static void main(String[] args) throws IOException {
		try {
			int port = DEFAULT_PORT;
			if (args.length == 1) {
				if (isInteger(args[0])) {
					port = Integer.parseInt(args[0]);
				} else {
					System.out.println(INVALID_PORT);
					throw new Exception();
				}
			}
			
			ServerSocket server = new ServerSocket(port);
			System.out.println(WAIT_MESSAGE + server.getLocalPort());
			while(true) { // Once the server is closed, it will throw an Exception that exits the iteration.
				// Receives the client connection.
				Socket client = server.accept();
				// Instantiates a new SoundServer object, passing in both the server instance and the client.
				// Wraps the instance in a Thread. Starts the thread, which will then wait until a new connection.
				Thread thread = new Thread(new SoundServer(server, client));
				thread.start();
			}
		} catch (Exception ex) {}
    }
	
}