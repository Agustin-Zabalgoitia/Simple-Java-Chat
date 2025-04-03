# Simple Java Chat
A Java chat program that allows multiple people to communicate in real time.

The program consist of two parts:
- A client, which is used to send and receive text messages
- A server, which takes care of communicating and managing connections between clients.

## Requirements
To run this program you must have installed at least Java 21.

## Usage
To use this program, a server must be running for clients to connect to.

In case you wish to host a server, or simply use the client, follow these steps: 
1. Clone the repository:
```bash
git clone https://github.com/Agustin-Zabalgoitia/Simple-Java-Chat
```
2. Navigate into the project directory:
```bash
cd Simple-Java-Chat/bin
```
### Server
If you want to run a server, run the following command:
```bash
java simpleJavaChatServer.Server
```

By default, the server will run on port 8189, but you can change this by passing a number when running the above command like this:
```bash
java simpleJavaChatServer.Server 1234
```
### Client
If you want to use the client, run the following command:
```bash
java simpleJavaChatClient.GUIClient
```
Don't confuse this with `simpleChatClient.Client`, that's one of the internal classes of the program.

By default, the client will try to connect to a server running at localhost:8189. If you want to connect to a different server, make sure to pass a valid address after the command. 
In case the server is running in a different port than the default one, pass the port number after the address.

For example, if a server is running in a computer with the address 192.168.100.156 and it's running on the 7732 port, then run the following command:
```bash
java simpleJavaChatClient.GUIClient 192.168.100.156 7732
```

Make sure that a server is running, otherwise the client won't run properly.

