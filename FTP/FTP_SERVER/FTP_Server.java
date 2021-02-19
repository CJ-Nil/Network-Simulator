import java.io.*;
import java.net.*;
import java.util.*;
public class FTP_Server 
{
	private ServerSocket serverSocket;
	private PrintWriter toClient;
	private Scanner fromClient;
	private Socket serverEnd;
    String takeData(String file)throws IOException{
		FileReader fr=null;
		String msg ="";
		try{
			fr = new FileReader(file);
			int c; 
		    while ((c=fr.read()) != -1) 
		    	msg+=(char)c;
		    fr.close();
		}catch(Exception ex){
			String empty = "'~`''`~'";
			msg+=empty;
			return msg;
		}
		msg+="\n\0";
		return msg;
	}
	String receiveData() throws IOException{
		String data = "";
		String s;
		do{
			s = fromClient.nextLine();
			if(!s.equals("\0"))
			data +=(s+"\n");
		}while(!s.equals("\0"));
		return data;
	}
	void createFile(String filename,String data)throws IOException{
		FileWriter myWriter = new FileWriter(filename);
	    myWriter.write(data);
	    myWriter.close();
	}
	void downFile(String filename)throws IOException{
		String data = takeData(filename);
		String empty = "'~`''`~'";
		if(data.equals(empty)){
			System.out.println("File does not exist!!");
			data += "\n\0";
		}
		else
			System.out.println("Sending file to client...");
		toClient.println(data);
	}
	void upFile(String filename)throws IOException{
		String data = receiveData();
		createFile(filename,data);
		System.out.println("Successfully uploaded file " + filename);
	}
	boolean deleteFile(String filename)throws IOException{
		
		try{
			File f =new File(filename);
			if(f.delete()){
				return true;
			}
			else
				return false;
		}catch(Exception ex){
			System.out.println("File does not exist!!");
			return false;
		}
	}
	public void start(){
		try{
			serverSocket = new ServerSocket(6789);
			System.out.println("Server is listening on port 6789");
			serverEnd = serverSocket.accept();
			System.out.println("Request accepted");
			fromClient=new Scanner(serverEnd.getInputStream());
			toClient = new PrintWriter(serverEnd.getOutputStream(), true);
			while(true){
				String type = fromClient.nextLine();
				int service = Integer.parseInt(type);
				String filename;
				filename = fromClient.nextLine();
				System.out.println("Received filename from client: " + filename);
				switch(service){
					case 1:
						downFile(filename);
						break;
					case 2:
						upFile(filename);
						break;
					case 3:
						if(deleteFile(filename))
						toClient.println("1");
						else
						toClient.println("0");
						break;
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
   public static void main(String argv[]) throws Exception {
	FTP_Server server = new FTP_Server();
	server.start();
	
  }
}