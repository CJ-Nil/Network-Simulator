//TCPFactServer.java
import java.io.*;
import java.net.*;
import java.util.*;
public class TCPFactServer {
   public static void main(String argv[]) throws Exception {
	//create a server socket at port 6789
	ServerSocket serverSocket = new ServerSocket(6789);
	 while(true) { 
			//wait for incoming connection
			System.out.println("Server is listening on port 6789");
			Socket serverEnd = serverSocket.accept();
			System.out.println("Request accepted");
			//get streams
			//BufferedReader fromClient = new BufferedReader(new
			//InputStreamReader(serverEnd.getInputStream()));
			Scanner fromClient=new Scanner(serverEnd.getInputStream());
			PrintWriter toClient = new PrintWriter(serverEnd.getOutputStream(), true);
			//receive data from client
			String n = fromClient.nextLine();
			System.out.println("Received from client: " + n);
			
			toClient.println(n);
			System.out.println("Sent to client: " + n);
		}
  }
}