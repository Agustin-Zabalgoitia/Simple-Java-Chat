package simpleJavaChatServer;

import java.io.PrintWriter;

public class User {
	private PrintWriter writer;
	private String nickname;
	
	public User(PrintWriter writer, String nickname) {
		this.writer = writer;
		this.nickname = nickname;
	}

	public PrintWriter getWriter() {
		return writer;
	}
	
	public String getNickname() {
		return nickname;
	}
}
