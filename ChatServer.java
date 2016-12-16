/* Some codes in this project has been taken from http://makemobiapps.blogspot.com/p/multiple-client-server-chat-programming.html */
import java.util.*;
import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

/*
 * A chat server that delivers public and private messages.
 */
public class ChatServer {

  // The server socket.
  private static ServerSocket serverSocket = null;
  // The client socket.
  private static Socket clientSocket = null;

  // This chat server can accept up to maxClientsCount clients' connections.
  private static final int maxClientsCount = 100;
  private static final clientThread[] threads = new clientThread[maxClientsCount];


  public static void main(String args[]) {

    // The default port number.
    int portNumber = 9715;
    if (args.length < 1) {
      System.out.println("Usage: java ChatServer <portNumber>\n"
          + "Now using port number=" + portNumber);
    } else {
      portNumber = Integer.valueOf(args[0]).intValue();
    }

    /*
     * Open a server socket on the portNumber (default 2222). Note that we can
     * not choose a port less than 1023 if we are not privileged users (root).
     */
    try {
      serverSocket = new ServerSocket(portNumber);
      System.out.println("ChatServer ready at " + portNumber);
    } catch (IOException e) {
      System.out.println(e);
    }

    /*
     * Create a client socket for each connection and pass it to a new client
     * thread.
     */
    while (true) {
      try {
        clientSocket = serverSocket.accept();
        int i = 0;
        for (i = 0; i < maxClientsCount; i++) {
          if (threads[i] == null) {
            (threads[i] = new clientThread(clientSocket, threads)).start();
            break;
          }
        }
        if (i == maxClientsCount) {
          PrintStream os = new PrintStream(clientSocket.getOutputStream());
          os.println("Server too busy. Try later.");
          os.close();
          clientSocket.close();
        }
      } catch (IOException e) {
        System.out.println(e);
      }
    }
  }
}

/*
 * The chat client thread. This client thread opens the input and the output
 * streams for a particular client, ask the client's name, informs all the
 * clients connected to the server about the fact that a new client has joined
 * the chat room, and as long as it receive data, echos that data back to all
 * other clients. The thread broadcast the incoming messages to all clients and
 * routes the private message to the particular client. When a client leaves the
 * chat room this thread informs also all the clients about that and terminates.
 */
class clientThread extends Thread {

  private String clientName = null;
  private DataInputStream is = null;
  private PrintStream os = null;
  private Socket clientSocket = null;
  private final clientThread[] threads;
  private int maxClientsCount;

  public clientThread(Socket clientSocket, clientThread[] threads) {
    this.clientSocket = clientSocket;
    this.threads = threads;
    maxClientsCount = threads.length;
  }

  public void run() {
    int maxClientsCount = this.maxClientsCount;
    clientThread[] threads = this.threads;

    try {
      /*
       * Create input and output streams for this client.
       */
      is = new DataInputStream(clientSocket.getInputStream());
      os = new PrintStream(clientSocket.getOutputStream());
      String name;
      while (true) {
        os.println("Enter your name.");
        name = is.readLine().trim();
        if (name.indexOf('@') == -1) {
          break;
        } else {
          os.println("S: The name should not contain '@' character.");
        }
      }

      /* Welcome the new the client. */
      os.println("Welcome " + name
          + " to our chat room.\nTo leave enter /Quit in a new line.");
      os.println("\nCommand starts with / and simple text without / is treated as /POST command by default.");
      synchronized (this) {
        for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] != null && threads[i] == this) {
            clientName = "@" + name;
            break;
          }
        }
        for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] != null && threads[i] != this) {
            threads[i].os.println("S: *** A new user " + name
                + " entered the chat room !!! ***");
          }
        }
      }
      /* Start the conversation. */
      while (true) {
        String line = is.readLine();

         if (line.startsWith("/")){



			if (line.startsWith("/Quit") || line.startsWith("/QUIT") || line.startsWith("/quit") ) {
			  break;
			}

			/* Display Help */
			else if (line.startsWith("/Help") || line.startsWith("/HELP") || line.startsWith("/help") ) {
			  os.println("S: *** List of commands ***");
			  os.println("S; /USER NAME : Change username. E.g. /USER Carl changes name to Carl");
			  os.println("S: /POST MESSAGE: Send Message to all users. e.g. /POST hi ");
			  os.println("S: /WHO : List of users");
			  os.println("S: /QUIT : Exit chat");
			  os.println("S: @USER MESSAGE: Send private message to USER. For e.g. @Maniz Private msg.");
			  os.println("\nS: Note - Command starts with /.");
			  os.println("S: As default text without / are treated a message, i.e. text: hello is treated as /POST hello .");
			  continue;
			}



			 /* Change Name */
			 else if (line.startsWith("/USER") || line.startsWith("/user") || line.startsWith("/User") ) {
			   String[] text = line.split(" ");
			   if (text.length < 2 || text.length > 2){
			   os.println("S: Invalid Syntax: /USER NAME ");
			   continue;
			   }
			   String prevName = name;
			   name = text[1];
			   synchronized (this) {
				 for (int i = 0; i < maxClientsCount; i++) {
				   if (threads[i] != null && threads[i] == this) {
					 clientName = "@" + name;
					 os.println("S: *** Username " + prevName + " changed to " + name + " ***");
					 break;
				   }
				 }
			   }
			   synchronized (this) {
				 for (int i = 0; i < maxClientsCount; i++) {
				   if (threads[i] != null && threads[i] != this && threads[i].clientName != null) {
					 threads[i].os.println("S: *** Username " + prevName + " changed to " + name + " ***");
				   }
				 }
			   }
			   continue;

			 }

      else if (line.startsWith("/post") || line.startsWith("/POST") || line.startsWith("/Post")){
        String msg = line.substring(5);
        /* The message is public, broadcast it to all other clients. */
        synchronized (this) {
          for (int i = 0; i < maxClientsCount; i++) {
            if (threads[i] != null && threads[i].clientName != null) {
              threads[i].os.println("<" + name + "> " + msg);
            }
          }
        }

      }

			/* List of User*/
			else if (line.startsWith("/who") || line.startsWith("/Who") || line.startsWith("/WHO")) {
			  os.println("S: *** List of Users ***");
			  for (int j = 0; j < maxClientsCount; j++) {
				if (threads[j] != null) {
					os.println("S: " + threads[j].clientName);
				}
			  }
			  continue;
			}

          else{
              os.println("S: Error: Invalid Command. Use /HELP for request commands.");
              continue;
          }
        }
        else{



        /* If the message is private sent it to the given client. */
        if (line.startsWith("@")) {
          String[] words = line.split("\\s", 2);
          if (words.length > 1 && words[1] != null) {
            words[1] = words[1].trim();
            if (!words[1].isEmpty()) {
              synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                  if (threads[i] != null && threads[i] != this
                      && threads[i].clientName != null
                      && threads[i].clientName.equals(words[0])) {
                    threads[i].os.println("<" + name + "> " + words[1]);
                    /*
                     * Echo this message to let the client know the private
                     * message was sent.
                     */
                    this.os.println(">" + name + "> " + words[1]);
                    break;
                  }
                }
              }
            }
          }
        }
        else {
          /* The message is public, broadcast it to all other clients. */
          synchronized (this) {
            for (int i = 0; i < maxClientsCount; i++) {
              if (threads[i] != null && threads[i].clientName != null) {
                threads[i].os.println("<" + name + "> " + line);
              }
            }
          }
        }
      }
      }
      synchronized (this) {
        for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] != null && threads[i] != this
              && threads[i].clientName != null) {
            threads[i].os.println("S: *** The user " + name
                + " is leaving the chat room !!! ***");
          }
        }
      }
      os.println("*** Disconnected ***");
      os.println("*** Bye " + name + " ***");

      /*
       * Clean up. Set the current thread variable to null so that a new client
       * could be accepted by the server.
       */
      synchronized (this) {
        for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] == this) {
            threads[i] = null;
          }
        }
      }
      /*
       * Close the output stream, close the input stream, close the socket.
       */
      is.close();
      os.close();
      clientSocket.close();
    } catch (IOException e) {
    }
  }
}
