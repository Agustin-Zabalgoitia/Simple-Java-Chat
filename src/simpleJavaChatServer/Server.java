package simpleJavaChatServer;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import general.Constants;

public class Server
{  
	private static final int MESSAGES_QUEUE_SIZE = 100;
	private static final int NICKNAME_MIN_LENGHT = 3;
	private static final int NICKNAME_MAX_LENGHT = 12;
	private static final int DEFAULT_PORT_NUMBER = 8189;
	
	private static boolean serverIsRunning = true;
	private static BlockingQueue<String> messagesToSend = new ArrayBlockingQueue<String>(MESSAGES_QUEUE_SIZE);
	private static List<User> connectedClientsWriters = Collections.synchronizedList(new ArrayList<User>());
	
	/**
	 * Executes the main thread and listens for new connections
	 * 
	 * @param args
	 * @throws IOException
	 */
   public static void main(String[] args) throws IOException
   {  
	  int port = DEFAULT_PORT_NUMBER;
	  if(args != null && args.length > 0 && Integer.parseInt(args[0]) > 0 && Integer.parseInt(args[0]) < 65536)
		  port = Integer.parseInt(args[0]);
	   
      try (var s = new ServerSocket(port))
      {  
    	  System.out.println(" === Server Started ===");
         int i = 1;
         ExecutorService service = Executors.newVirtualThreadPerTaskExecutor();
         
         service.submit(() -> sendMessages());
         
         while (true)
         {  
            Socket incoming = s.accept();
            System.out.println("Spawning " + i);
            service.submit(() -> listenToClient(incoming));
            i++;
         }
      }
   }
   
   /**
    * Function to be ran by a thread. It sends any message added to the messagesToSend Queue to all the connected clients.
    */
   public static void sendMessages() {
	   String message;
	   while(serverIsRunning)
	   {
		   try {
				message = messagesToSend.take();
				
				synchronized (connectedClientsWriters) {
					for (User user : connectedClientsWriters) {
						   user.getWriter().println(message);
					   }	
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	   }
   }
   
   /**
    * Function to be ran by a thread. It takes care of the communication between the server and one client.
    * @param incoming - An accepted connection made by a socket
    */
   public static void listenToClient(Socket incoming)
   {
      try (var in = new Scanner(incoming.getInputStream());         
         var out = new PrintWriter(incoming.getOutputStream(), 
            true /* autoFlush */))
      {  
         out.println("Enter your nickname: ");
         
         String nickname = in.nextLine();
         String validationResult = validateNickname(nickname);
         while( validationResult != "")
         {
        	 out.println("!!! Invalid nickname !!!"+Constants.NEW_LINE+ validationResult+Constants.NEW_LINE+"\nEnter your nickname: ");
        	 nickname = in.nextLine();
        	 validationResult = validateNickname(nickname);
         }
         out.println(Constants.ACCEPTED_NICKNAME_MESSAGE+nickname);
         
         System.out.println(nickname + " has joined");
         String nicknameColor = getColorCodeFromString(nickname);
         String nicknameHtmlComponent = "<b style='color:"+nicknameColor+";'>"+nickname+"</b>";
         
         String greeting = "Welcome " + nickname; 
         String underscore = "";
         for (int i = 0; i < greeting.length(); i++)
             underscore += "=";
         out.println(greeting);
         out.println(underscore);
         out.println("");
         
         messagesToSend.add("<b>&gt&gt&gt&gt&gt "+nicknameHtmlComponent+" is <i style='color:green'>online</i> &lt&lt&lt&lt&lt</b>");
         User currentUser = new User(out, nicknameHtmlComponent);
         String userListString = Constants.USER_LIST_MESSAGE;
         
         synchronized (connectedClientsWriters) {
        	 connectedClientsWriters.add(currentUser);
        	 
        	 for (User u : connectedClientsWriters) {
				userListString = userListString + u.getNickname()+Constants.LIST_SEPARATOR;
			}
        	 
         }
         userListString = userListString.substring(0, userListString.length()-1);
    	 messagesToSend.add(userListString);
    	 
         while (in.hasNext())
         {  
            String line = in.nextLine();
            
        	messagesToSend.add(nicknameHtmlComponent + Constants.USERNAME_SEPARATOR+ " " + line);
         }
         
         synchronized (connectedClientsWriters) {
        	 connectedClientsWriters.remove(currentUser);
        	 messagesToSend.add("<b>&lt&lt&lt&lt&lt " + nicknameHtmlComponent + " is <i style='color:red'>offline</i> &gt&gt&gt&gt&gt</b>");
        	 System.out.println(nickname + " has disconnected");
        	 userListString = Constants.USER_LIST_MESSAGE;
        	 for (User u : connectedClientsWriters) {
 				userListString = userListString + u.getNickname()+Constants.LIST_SEPARATOR;
 			}
        	 userListString = userListString.substring(0, userListString.length()-1);
        	 messagesToSend.add(userListString);
         }
      }
      catch (IOException e)
      {  
         e.printStackTrace();
      }              
   }
   
   /**
    * Function to get an RGB color code from any given string
    * @param str - Any string that'll be used as a seed for an RGB color code
    * @return An RGB color code, including an # at the beginning.
    */
   private static String getColorCodeFromString(String str) {
	   
	   if(str.isEmpty())
		   return "#000000";
	 
	   int charAmount = Math.round((float)str.length()/6);
	   
	   String[] colorCodeParts = new String[6];
	   
	   for(int i = 0 ; i<6 ; i++)
	   {
		   if(str.length() >= 6)
			   colorCodeParts[i] = str.substring(i*charAmount, charAmount*(i+1) > str.length() ? str.length() : charAmount*(i+1) );
		   else
		   {
			   int aux = i%str.length() >= str.length() ? 0 : i%str.length();
			   colorCodeParts[i] = str.substring(aux, aux+1);
		   }
		   
		   int charCodeSum = 0;
		   for(int j = 0 ; j < colorCodeParts[i].length() ; j++)
			   charCodeSum += (int) colorCodeParts[i].charAt(j);
			
		   colorCodeParts[i] =  Integer.toHexString(charCodeSum % 16).toUpperCase();
	   }
		   
	   String ret = "#";
	   for (String string : colorCodeParts) {
		   ret += string;
	   }
	   
	   return ret;
   }
   
   /**
    * Function to validate the nickname provided by the clients
    * @param nickname - A nickname provided by a client
    * @return An empty string if the nickname is valid, or a message explaining why the nickname is not valid if it's not. 
    */
   private static String validateNickname(String nickname) {
	   
	   String response = "";
	   
	   if(!isNicknameContentValid(nickname))
		   response = "nickname cannot contain the next invalid characters " + Constants.SERVER_FIRST_SYMBOL + " " + Constants.USERNAME_SEPARATOR+ " " +Constants.LIST_SEPARATOR;
	   
	   if(!isNicknameCorrectLenght(nickname, NICKNAME_MIN_LENGHT, NICKNAME_MAX_LENGHT))
	       response = "nickname must have at least three characters long, and must not be over 12 characters long."; 
	       
	   if(doesNicknameExist(nickname))
		   response = "nickname already exists";
	       
	   return response;
   }
   
   /**
    * Checks if the given string is free of any invalid characters
    * @param str - The nickname given by a client
    * @return false if str contains any invalid character, true otherwise
    */
   private static boolean isNicknameContentValid(String str) {
	   return !str.contains(Constants.SERVER_FIRST_SYMBOL) && !str.contains(Constants.USERNAME_SEPARATOR) && !str.contains(Constants.LIST_SEPARATOR);
   }
   
   /**
    * Checks if the length of a given string is between min and max inclusive.
    * @param str - The nickname given by a client
    * @param min - The minimum amount of characters the nickname must have 
    * @param max - The maximum amount of characters the nickname must have
    * @return true if length of str is between min and max, false otherwise
    */
   private static boolean isNicknameCorrectLenght(String str, int min, int max) {
	   return str.length() >= min && str.length() <= max;
   }
   
   /**
    * Checks if the given string is already assigned to a connected client
    * @param str - A nickname given by a client
    * @return true if the given string is assigned already, false otherwise
    */
   private static boolean doesNicknameExist(String str)
   {
	   synchronized (connectedClientsWriters) {
      	 for (User u : connectedClientsWriters) {
				if(u.getNickname().equalsIgnoreCase(str))
					return true;
			}
       }
	   return false;
   }
}