//UDPFactClient.java 
import java.net.*; 
import java.io.*; 
class DataCorrupt
{
	public byte[] corruptData(byte data[]){
		int length=data.length;
		int rindex=(int) ((Math.random() * (length -1)));
		int bindex=(int) ((Math.random() * 5));
		System.out.println("rindex: "+rindex+" bindex: "+bindex);
		String s = Integer.toBinaryString((int)data[rindex]);
		System.out.println(s);
		char st[]=s.toCharArray();
		if(st[bindex]=='0')
			st[bindex]='1';
		else
			st[bindex]='0';
		s=new String(st);
		System.out.println("After corrupted: "+s);
		data[rindex]=(byte)(int)Integer.valueOf(s,2);
		return data;
	}
}
public class UDPFactClient 
{    
	public static void main(String args[]) throws Exception 
	{       
		byte[] rbuf = new byte[1024], sbuf = new byte[1024],buf= new byte[1024];       
		BufferedReader fromUser = new BufferedReader(new InputStreamReader(System.in));       
		DatagramSocket socket = new DatagramSocket(1234);       
		InetAddress addr = InetAddress.getByName(args[0]);       
		//get an integer from user       
		System.out.print("Enter a Message to send: ");       
		String data = fromUser.readLine();    

		buf = data.getBytes(); 
		int checkSum=0;
		//calculating checksum of the message
		for(int i=0;i<buf.length;i++){
			int nob=(int)(Math.floor(Math.log(buf[i])/Math.log(2))) +1;
			checkSum+=(((1 << nob) - 1) ^ buf[i]);
		}
		int dataLength=buf.length;
		System.out.println("Sent to server: " + new String(buf)); 
		DataCorrupt dc=new DataCorrupt();
		buf=dc.corruptData(buf);
		data=new String(buf);
		data=data+"~"+String.valueOf(checkSum);
		sbuf=data.getBytes();
		DatagramPacket spkt = new DatagramPacket(sbuf,sbuf.length, addr, 4000);       
		//send it to server       
		socket.send(spkt);         
		DatagramPacket rpkt = new DatagramPacket(rbuf, rbuf.length);       
		//retrieve result       
		socket.receive(rpkt);       
		data = new String(rpkt.getData(), 0, rpkt.getLength());       
		System.out.println("Received from server: " + data);       
		//close the socket       
		socket.close();    
	} 
}