import java.util.*; 
import channel.Channel;
import channel.EthernetFrame;
import java.io.*;
public class Sender extends Thread { 
	private String srcAdd;
	private int datalength;
	private String destAdd;
	int ackRecive(int f)throws Throwable
	{
		int ack = -1;
		Channel medium = new Channel("file");
		boolean lock = false;
		while(!lock)
		{
			lock = medium.lockChannel();
		}
		ArrayList<byte[]> arr=medium.getMessage();
		medium.releaseChannel();
		for(byte[] frame:arr){
			EthernetFrame ef = new EthernetFrame(frame);
			String dest = ef.getDest();
			String src = ef.getSrc();
			String type = ef.getType();
			String fno = ef.getFrameNo();
			int no = Integer.parseInt(fno);
			byte[] data = ef.getData();
			if(srcAdd.equals(dest) && destAdd.equals(src) && type.equals("AK") && f==no){
				ack=no;
				arr.remove(frame);
				medium = new Channel("file");
				while(!medium.lockChannel());
				medium.putMessage(arr);
				medium.releaseChannel();
				break;
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
		}
		ArrayList<byte[]> arrFrame = medium.getMessage();
		medium.releaseChannel();
		if(arrFrame.size()>=10){
			//for(int i=0;i<3;i++)
			arrFrame.remove(0);
		}
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
			srcAdd = sc.nextLine();
			System.out.println("Enter Reciver Address(six length):");
			destAdd = sc.nextLine();
			System.out.println("Enter a Message to Send: ");
			String msg = sc.nextLine();
			byte [] bytedata = msg.getBytes();
			int n = bytedata.length;
			System.out.println("Enter Frame Size(min size 21 byte):: ");
			int length = Integer.parseInt(sc.nextLine());
			System.out.println("Enter Window size:");
			int w=Integer.parseInt(sc.nextLine());
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
				EthernetFrame RTSFrame = new EthernetFrame(destAdd,srcAdd,"RS",0,temp,length);
				System.out.println("Sending Request Frame to: "+destAdd);
				while(!sendingFrame(RTSFrame.getFrame()));
				//********************************************************************************//
				System.out.println("Numbers of Frame to be send:"+no_of_frame);
				sleep(500);
				//**********Reciving Allert ack**********************************************//
				boolean _Ack = false;
				Channel medium = new Channel("file");
				boolean lock = false;
				while(!lock){
					lock = medium.lockChannel();
				}
				System.out.println(lock);
				ArrayList<byte[]> arrFrame = medium.getMessage();
				for(int i=0;i<arrFrame.size();i++){
					byte[] frame = arrFrame.get(i);
					EthernetFrame ef = new EthernetFrame(frame);
					String dA = ef.getDest();
					String fT = ef.getType();
					int no = Integer.parseInt(ef.getFrameNo());
					if(srcAdd.equals(dA) && no==0 && fT.equals("AK")){
						_Ack = true;
						break;
					}
				}
				//**************************************************************************//
				//*******IF RECIVER IS READY SEND ONE BY ONE FRAME***************************//
				if(_Ack == true) 
				{
					System.out.println("Connection Established Successfully...");
					//*******************Making Frame array*******************************//
					int l=0,r=dlength;
					EthernetFrame[] flist = new EthernetFrame[no_of_frame];
					if(dlength>n)
						r=n;
					int fno=0;
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
						flist[fno] = new EthernetFrame(destAdd,srcAdd,"DS",fno+1,data,length);
						l=r;
						r+=dlength;
						if(r>n)
							r=n;
						fno++;
					}
					//***************************************************************************//
					//************** Sending Frame in Sliding Window thechnique******************//
					int wl=0;
					int wr=w-1;
					int ackno=0;
					int j=0;
					int ak;
					while(j<fno){
						while(j<fno && j>=wl && j<=wr){
							byte[] DataSendFrame = flist[j].getFrame();
							System.out.println("Sending Data Frame:"+(j+1)+"....");
							sendingFrame(DataSendFrame);
							sleep(700);
							ak= ackRecive(ackno+1);
							if(ak>0){
								System.out.println("Recive Acknoledgement Frame:"+(ackno+1));
								ackno++;
								wl=ackno;
								wr=(wl+w)-1;
								if(wr>=fno)
									wr=fno-1;
							}
							j++;
						}
						if(ackno<fno)
							j=wl;
					}
				}
				else
					System.out.println("Time out......");
				//***************************************************************************************//
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

