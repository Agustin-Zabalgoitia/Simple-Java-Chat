package simpleJavaChatClient;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * This is a singleton class that manages the connection to the server
 */
public class Client 
{
	private static Client INSTANCE;
	
	private static int MESSAGES_QUEUE_SIZE = 100;
	
	private Socket s;
	private Scanner in;
	private PrintWriter out;
	private BlockingQueue<String> messageQueue;
	private boolean isClientRunning = false;
	
	private Client() {
		messageQueue = new ArrayBlockingQueue<String>(MESSAGES_QUEUE_SIZE);
	}
	
	/**
	 * Prevent initialization of other instances by forcing the use of getClient instead of Client()
	 * @return the only instance of the Client class
	 */
	public static Client getClient() {
		if(INSTANCE == null)
			INSTANCE = new Client();
		
		return INSTANCE;
	}
	
	/**
	 * Starts the connection to the server
	 */
	public void start(String serverInetAddress, int serverPort)
	{
		if(isClientRunning)
			return;
		else
			isClientRunning = true;
		
		try
		{	
			s = new Socket(serverInetAddress, serverPort);
			in = new Scanner(s.getInputStream(), StandardCharsets.UTF_8);
			out = new PrintWriter(s.getOutputStream(), true);
					
			Thread serverListener = new Thread(() -> listenToServer());
			
			serverListener.start();
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Closes the connection to the server
	 */
	public void stop() {
		isClientRunning = false;
		try {
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns any message received from the server.
	 * This method tries to take an item from a BlockingQueue, which
	 * may lead to a stop in the program execution. It is advisable to 
	 * call this method in a thread.
	 * @return a message received from the server
	 * @throws InterruptedException
	 */
	public String getMessage() throws InterruptedException {
		return messageQueue.take();
	}
	
	/**
	 * Sends a message to the server
	 * @param msg - message that'll be sent to the server
	 */
	public void sendMessage(String msg) {
		out.println(msg);
	}
	
	/**
	 * Returns the status of the client.
	 * @return if the client is running or not
	 */
	public boolean isRunning() {
		return isClientRunning;
	}
	
	/**
	 * Method intended to run in a secondary thread.
	 * It takes care of listening to the server, and store any
	 * messages it receives.
	 */
	private void listenToServer() {
		while(in.hasNextLine() && isClientRunning)
			messageQueue.add(in.nextLine());
		stop();
	}
}