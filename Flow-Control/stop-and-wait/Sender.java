import java.util.*; 
import channel.Channel;
import channel.EthernetFrame;
import java.io.*;
public class Sender extends Thread { 
	private String addr;
	private int datalength;
	boolean TimeOut(Date start_time)
	{
		Date now = new Date();
		if(now.getTime() - start_time.getTime() >=20000){
			//System.out.println(now.getTime() - start_time.getTime());
			return true;
		}
		return false;
	}
	boolean reciveAck(Date start_time,int frame_no)throws Throwable
	{
		boolean ack = false;		
		Channel medium = new Channel("file");
		boolean lock = false;
		while(!lock && !TimeOut(start_time))
		{
			lock = medium.lockChannel();
			if(frame_no==0)
				System.out.println("Wating for Reciver connection...");
			else
				System.out.println("Wating for acknoledgement:"+frame_no+"...");
			sleep(500);
		}
		if(lock)
		{
			ArrayList<byte[]> arr = medium.getMessage();
			medium.releaseChannel();
			for(byte[] frame:arr){
				EthernetFrame ef = new EthernetFrame(frame);
				String destAdd = ef.getDest();
				String srcAdd = ef.getSrc();
				String type = ef.getType();
				String fno = ef.getFrameNo();
				int no = Integer.parseInt(fno);
				byte[] data = ef.getData();
				if(addr.equals(destAdd) && type.equals("AK") &&  frame_no==no){
					ack=true;
					arr.remove(frame);
					medium = new Channel("file");
					while(!medium.lockChannel());
					medium.putMessage(arr);
					medium.releaseChannel();
					break;
				}
			}
		}
		return ack;
	}
	boolean sendingFrame(byte[] frame)throws Throwable
	{
		Channel medium = new Channel("file");
		boolean lock = false;
		while(!lock)
		{
			lock = medium.lockChannel();
			sleep(500);
		}
		ArrayList<byte[]> arrFrame = medium.getMessage();
		medium.releaseChannel();
		if(arrFrame.size()>=5)
			return false;
		arrFrame.add(frame); 
		medium = new Channel("file");
		lock = false;
		while(!lock)
		{
			lock = medium.lockChannel();
		}
		medium.putMessage(arrFrame);
		medium.releaseChannel();
		return true;
	}
	public void run() 
	{ 
		while(true)
		{
			Scanner sc = new Scanner(System.in);
			System.out.println("Enter Your machine Address(six length):");
			String src = sc.nextLine();
			addr = src;
			System.out.println("Enter Reciver Address(six length):");
			String dest = sc.nextLine();
			System.out.println("Enter a Message to Send: ");
			String msg = sc.nextLine();
			byte [] bytedata = msg.getBytes();
			int n = bytedata.length;
			System.out.println("Enter Frame data Size(min size 115 byte):: ");
			int length = Integer.parseInt(sc.nextLine());
			int dlength = length-20;
			int no_of_frame; 
			if(n%dlength==0)
				no_of_frame = n/dlength;
			else
				no_of_frame = n/dlength +1;
			try 
			{ 
				//****************SENDING ALLERT FOR NO OF FARME ARE COMMING FROM SENDER*************//
				String _st = Integer.toString(no_of_frame);
				byte temp[] = _st.getBytes();
				EthernetFrame RTSFrame = new EthernetFrame(dest,src,"RS",0,temp,length);
				System.out.println("Sending Request Frame to: "+dest);
				//********************************************************************************//

				while(!sendingFrame(RTSFrame.getFrame())){
					sleep(500);
				}
				System.out.println("Numbers of Frame to be send:"+no_of_frame);
				sleep(1000);
				//**********Reciving Allert ack**********************************************//
				Date start_time = new Date();
				boolean _Ack = false;
				Channel medium = new Channel("file");
				boolean lock = false;
				while(!lock){
					lock = medium.lockChannel();
					System.out.println("Waiting for connection.....");
					sleep(500);
				}
				System.out.println(lock);
				ArrayList<byte[]> arrFrame = medium.getMessage();
				for(int i=0;i<arrFrame.size();i++){
					byte[] frame = arrFrame.get(i);
					EthernetFrame ef = new EthernetFrame(frame);
					String dA = ef.getDest();
					String sA = ef.getSrc();
					String fT = ef.getType();
					int no = Integer.parseInt(ef.getFrameNo());
					if(src.equals(dA) && no==0 && fT.equals("AK")){
						_Ack = true;
						break;
					}
				}
				//**************************************************************************//
				//*******IF RECIVER IS READY SEND ONE BY ONE FRAME***************************//
				if(_Ack == true) 
				{
					System.out.println("Connection Established Successfully...");
					int l=0;
					int r=dlength;
					if(dlength>n)
						r=n;
					int FrameNo=1;
					while(l<n)
					{
						byte data[] = new byte[r-l+1];
						int i=0;
						while(l<r)
						{
							data[i]= bytedata[l];
							l++;
							i++;
						}
						//*****sending frame***************************************//
						EthernetFrame frm = new EthernetFrame(dest,src,"DS",FrameNo,data,length);
						byte[] DataSendFrame = frm.getFrame();
						while(!sendingFrame(DataSendFrame)){
							sleep(500);
						}
						//*********************************************************//
						System.out.println("Sending Frame:"+FrameNo+"...");
						sleep(500);
						//********reciving acknoledgement*************************//
						Date now = new Date();
						boolean ack = reciveAck(now,FrameNo);
						//********************************************************//
						if(ack)
						{
							System.out.println("Recive Acknoledgement:"+FrameNo);
							l=r;
							r+=dlength;
							if(r>n)
								r=n;
							FrameNo++;
						}
						else
							System.out.println("Time out...");
					}
				}
				else
					System.out.println("Time out..."); 
				
			}
			catch(Exception ex) {
				System.out.println("Exception");
				ex.printStackTrace();
			} catch (Throwable e) {
				System.out.println("Throwable");
				e.printStackTrace();
			}
		}
		
	} 
	public static void main(String args[]){
		Sender sender = new Sender();
		sender.start();
	}
} 

