//UDPFactServer.java 
import java.net.*; 
import java.io.*; 
public class UDPFactServer 
{  
	public static void main(String args[]) throws Exception 
	{       
		byte[] rbuf = new byte[1024], sbuf = new byte[1024];       
		//create a server socket at port 5000             
		DatagramSocket socket = new DatagramSocket(4000);       
		System.out.println("Server ready"); 
		DatagramPacket rpkt = new DatagramPacket(rbuf, rbuf.length);       
		//receive a packet from client       
		socket.receive(rpkt);       
		//extract data and client information from this packet     
		String packet_data = new String(rpkt.getData(),0,rpkt.getLength());
		int dataLength=rpkt.getLength();
		String data="";
		int j;
		for(j=0;j<packet_data.length();j++){
			if(packet_data.charAt(j)=='~')
				break;
			data+=packet_data.charAt(j);
		}
		j++;
		String check="";
		for(;j<packet_data.length();j++){
			check+=packet_data.charAt(j);
		}
		System.out.println("Data: "+packet_data+" checkSum: "+check);
		int checkSum=Integer.parseInt(check);
		byte[] buf=data.getBytes();
		int sum=0;
		int nob;
		for(int i=0;i<buf.length;i++){
			nob=(int)(Math.floor(Math.log(buf[i])/Math.log(2))) +1;
			sum+=(((1 << nob) - 1) ^ buf[i]);
		}
		nob=(int)(Math.floor(Math.log(checkSum)/Math.log(2))) +1;
		sum+=(((1 << nob) - 1) ^ checkSum);
		
		nob=(int)(Math.floor(Math.log(sum)/Math.log(2))) +1;
		sum=(((1 << nob) - 1) ^ sum);
		
		InetAddress addr = rpkt.getAddress();       
		int port = rpkt.getPort();  

		System.out.println("Received: " + data + " checkSum: " +checkSum+ " from " + addr + ":" + port);       
		System.out.println("Calculated checkSum is: "+sum);
		sbuf = String.valueOf(sum).getBytes();       
		DatagramPacket spkt = new DatagramPacket(sbuf, sbuf.length, addr, port);      
		//send result to the client       
		socket.send(spkt);       
		System.out.println("Sent Calculated checkSum " + sum); 
		socket.close();
   } 
}