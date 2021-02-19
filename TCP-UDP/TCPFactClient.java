//TCPFactClient.java
import java.io.*;
import java.net.*;
import java.util.*;
public class TCPFactClient {
  public static void main(String argv[]) throws Exception {
	String fact;
	//create a socket to the server
	Socket clientEnd = new Socket("localhost", 6789);
	System.out.println("Connected to localhost at port 6789");
	//get streams
	PrintWriter toServer = new PrintWriter(clientEnd.getOutputStream(), true);
	/*BufferedReader fromServer = new BufferedReader(new
	InputStreamReader(clientEnd.getInputStream()));*/
	Scanner fromServer=new Scanner(clientEnd.getInputStream());
	Scanner fromUser=new Scanner(System.in);
	/*BufferedReader fromUser = new BufferedReader
(new InputStreamReader(System.in));*/
	//get an integer from user
	System.out.print("Enter an message: ");
	String cmsg = fromUser.nextLine();
	//send it to server
	toServer.println(cmsg);
	System.out.println("Sent to server: " + cmsg);
	//retrieve result
	String smsg = fromServer.nextLine();
	System.out.println("Received from server: " + smsg);
	//close the socket
	clientEnd.close();
   }
}