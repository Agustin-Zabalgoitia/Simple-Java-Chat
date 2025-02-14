package simpleJavaChatClient;
 
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import general.Constants;        

public class GUIClient extends JPanel{
    private static final long serialVersionUID = 1L;

    private ReceiveMessagesTask rmt;
	private Client client;
	private static String serverInetAddress = "localhost";
	private static int serverPort = 8189;
	
    private MessagePanel msgPanel;
    private UserPanel userPanel;
    private InputPanel inputPanel;
    
    /**
     * Creates a new GUI Client.
     */
	public GUIClient() {
		setLayout(new BorderLayout());
		
		client = Client.getClient();
		client.start(serverInetAddress, serverPort);
		
		msgPanel = new MessagePanel();
		userPanel = new UserPanel();
		inputPanel = new InputPanel();
		
        //SplitPanes
        JSplitPane upperPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, msgPanel, userPanel);
        JSplitPane mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upperPane, inputPanel);
        
        add(mainPane);
	}
	
	/**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Chat Window");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        GUIClient w = new GUIClient();
        frame.add(w);
        frame.addWindowListener(w.new OpenAndClosingWindowListener());
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
 
    public static void main(String[] args) {
    	
    	if(args != null)
    	{
    		//Check if Server Inet Address is provided
    		if(args.length > 0)
    			serverInetAddress = args[0];
    		
    		//Check if Server Port is provided
    		if(args.length > 1)
    			serverPort = Integer.parseInt(args[1]);
    	}
    	
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
    
    /**
     * Task responsible for receiving messages from the server.
     */
    private class ReceiveMessagesTask extends SwingWorker<Void, String> {

		@Override
		protected Void doInBackground() throws Exception {
			
			while(client.isRunning()) {
				String msg = client.getMessage(); 
				msgPanel.addNewMessage(msg);
				userPanel.updateUsers(msg);
				inputPanel.setNicknameLabel(msg);
			}
			return null;
		}
	}
    
    /**
     * Listener to detect when the main window opens or is closing.
     * It starts a new ReceiveMessagesTask once the window is opened
     * and it cancels it and closes the client when the window is closing.
     */
    private class OpenAndClosingWindowListener extends WindowAdapter{
        @Override
     	public void windowOpened(WindowEvent e) {
     		if(rmt == null)
     			(rmt = new ReceiveMessagesTask()).execute();
     	}

     	@Override
     	public void windowClosing(WindowEvent e) {
     		rmt.cancel(true);
     		rmt = null;
     		client.stop();
     	}
    }
    
}

/**
 * This is the panel where the messages that are 
 * received from the server will be shown.
 */
class MessagePanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private static final int MESSAGE_PANE_PREFERRED_SIZE_X = 400;
	private static final int MESSAGE_PANE_PREFERRED_SIZE_Y = 250;
	
	private static JLabel infoLabel = new JLabel("Chatroomname");
	private static JTextPane messageTextArea = new JTextPane();
	private static JScrollPane messageScrollPane = new JScrollPane(messageTextArea);
	
	private static SimpleAttributeSet boldStyle = new SimpleAttributeSet();
	
	private static String receivedMessages = new String("");
	
	public MessagePanel() {
		setLayout(new BorderLayout());
		
		//Set up messages area        
        //messageTextArea.setLineWrap(true);
        messageTextArea.setEditable(false);
        ((DefaultCaret)messageTextArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE); //Sets autoscroll
        messageTextArea.setContentType("text/html");
        
        //Set up styles
        StyleConstants.setBold(boldStyle, true);
        
        //Add scroll        
        messageScrollPane.setPreferredSize(new Dimension(MESSAGE_PANE_PREFERRED_SIZE_X, MESSAGE_PANE_PREFERRED_SIZE_Y));
        
        add(infoLabel, BorderLayout.NORTH);
        add(messageScrollPane);
	}
	
	/**
	 * This function adds a new messages to the panel, without clearing it.
	 * @param msg - the message to be shown on the panel
	 */
	public void addNewMessage(String msg) {
		if(msg.startsWith(Constants.SERVER_FIRST_SYMBOL))
			return;
		
		receivedMessages += msg + "<br>";
		messageTextArea.setText(receivedMessages);
	}
}

/**
 * This panel shows the connected users in the chatroom
 */
class UserPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final int USER_PANE_PREFERRED_SIZE_X = 100;
	private static final int USER_PANE_PREFERRED_SIZE_Y = 250;
	
	private JTextPane userTextPane;
	
	public UserPanel() {
		setLayout(new BorderLayout());
		
		//Set up info label
		JLabel infoLabel = new JLabel("Users");
		
		//Set up messages area
        userTextPane = new JTextPane();
        //userTextArea.setLineWrap(true);
        userTextPane.setEditable(false);
        userTextPane.setContentType("text/html");

        //Add scroll
        JScrollPane userScrollPane = new JScrollPane(userTextPane);
        userScrollPane.setPreferredSize(new Dimension(USER_PANE_PREFERRED_SIZE_X, USER_PANE_PREFERRED_SIZE_Y));
        
        add(infoLabel, BorderLayout.NORTH);
        add(userScrollPane);
	}
	
	/**
	 * Updates the users lists with the users given by msg
	 * @param msg - message from the server listing all the connected users
	 */
	public void updateUsers(String msg) {
		if(!msg.startsWith(Constants.SERVER_FIRST_SYMBOL) || !msg.contains(Constants.USER_LIST_MESSAGE))
			return;
		
		clearText();
		
		String users[] = msg.substring(msg.indexOf(Constants.SERVER_SEPARATOR_SYMBOL)+1).split(Constants.LIST_SEPARATOR);
		String userString = new String();
        clearText();
		for (String string : users)
			userString += string+"<br>";
		
		userTextPane.setText(userString);
	}
	
	/**
	 * Clears the user list
	 */
	public void clearText() {
		userTextPane.setText("");
	}
	
}

/**
 * Panel that contains all the elements to allow an user to write and send messages
 */
class InputPanel extends JPanel{

	private static final long serialVersionUID = 1L;
	private static final int PANE_PREFERRED_SIZE_X = 250;
	private static final int PANE_PREFERRED_SIZE_Y = 40;
	
	private JLabel nicknameLabel;
	private JTextArea inputTextArea;
	private JScrollPane inputAreaScrollPane;
	private JButton sendButton;
	
	private Client client;
	
	public InputPanel() {
		setLayout(new BorderLayout());
		
		client = Client.getClient();
		
		//Set up nickname
		nicknameLabel = new JLabel();
		
        //Set up text input
        inputTextArea = new JTextArea();
        inputTextArea.setLineWrap(true);
        //Add scroll
        inputAreaScrollPane = new JScrollPane(inputTextArea);
        inputAreaScrollPane.setPreferredSize(new Dimension(PANE_PREFERRED_SIZE_X, PANE_PREFERRED_SIZE_Y));
        
        sendButton = new JButton("Send");
        sendButton.addActionListener(new SendButtonListener());
        inputTextArea.addKeyListener(new EnterKeyListener());
        
        add(nicknameLabel, BorderLayout.WEST);
        add(inputAreaScrollPane, BorderLayout.CENTER);
        add(sendButton, BorderLayout.EAST);
	}

	/**
	 * Sets the label on the left of the text area to be the nickname approved by the server
	 * @param msg - message from the server with an approved nickname
	 */
	public void setNicknameLabel(String msg) {
		if(!msg.startsWith(Constants.SERVER_FIRST_SYMBOL) || !msg.contains(Constants.ACCEPTED_NICKNAME_MESSAGE))
			return;
		nicknameLabel.setText(msg.substring(msg.indexOf(Constants.SERVER_SEPARATOR_SYMBOL)+1));
	}
	
	/**
	 * Sends a message to the server. This function is intended to be used by event listeners
	 */
	private void onSend() {
		if(!client.isRunning())
			return;
		String msg = inputTextArea.getText();
		if(!msg.isBlank())
		{
			msg = msg.substring(0, (msg.indexOf(Constants.NEW_LINE) > -1) ? msg.indexOf(Constants.NEW_LINE) : msg.length());
			client.sendMessage(msg);
		}
		inputTextArea.setText("");
	}
	
	/**
	 * Listener that listens to the enter key, and if it's released,
	 * sends a message to the server. 
	 */
	private class EnterKeyListener extends KeyAdapter{
		@Override
		public void keyReleased(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_ENTER)
				onSend();
		}	
	}
	
	/**
	 * Listener that listens to an action and sends a message to the server.
	 * It is attached to the send button.
	 */
	private class SendButtonListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			onSend();
		}
	}
}