import java.util.*; 
import channel.Channel;
import channel.EthernetFrame;
import java.io.*;
class Sender extends Thread 
{ 
	private String src,dest,dataFile;
	private int frameLength,totalNode;

	private int datalength;

	public Sender(int n,String s,String d,int fl,String df)
	{
		this.totalNode = n;
		this.src = s;
		this.dest = d;
		this.frameLength = fl;
		this.dataFile = df;
	}

	private boolean timeOut(Date start_time)
	{
		Date now = new Date();
		if(now.getTime() - start_time.getTime() >= 500)
		{
			return true;
		}
		return false;
	}

	private long timeTaken(Date start)
	{
		Date now = new Date();
		return (long)(now.getTime() - start.getTime());
	}
	private void refreshChannel(byte[] removeFrame) throws Throwable
	{
		Channel medium = new Channel("file");
		while(!medium.isIdle());
		medium.setBusy();
		ArrayList<Object> list = medium.getMessage();
		ArrayList<byte[]> arrFrame = (ArrayList<byte[]>)list.get(2);
		arrFrame.remove(removeFrame);
		list.set(2,arrFrame);
		medium = new Channel("file");
		medium.putMessage(list);
		medium.setIdle();
	}
	private int reciveAck(Date start_time,int frame_no)throws Throwable
	{
		boolean ack = false;
		byte[] reciveFrame = new byte[20];	
		int maxack = -1;
		while(!timeOut(start_time) && !ack)
		{
			Channel medium = new Channel("file");
			ArrayList<Object> list = medium.getMessage();
			ArrayList<byte[]> arr = (ArrayList<byte[]>)list.get(2);
			for(byte[] frame:arr)
			{
				EthernetFrame ef = new EthernetFrame(frame);
				String destAdd = ef.getDest();
				String srcAdd = ef.getSrc();
				String type = ef.getType();
				int no = Integer.parseInt(ef.getFrameNo());
				if(this.src.equals(destAdd) && this.dest.equals(srcAdd) &&  no > maxack)
				{
					maxack = no;
					reciveFrame = frame;
				}
			}
			if(maxack>=frame_no)
			ack = true;
		}
		if(ack){
			refreshChannel(reciveFrame);
		}
		return maxack;
	}

	private boolean sendingFrame(byte[] frame)throws Throwable
	{
		Channel medium = new Channel("file");
		while(!medium.isIdle());
		medium.setBusy();
		ArrayList<Object> list = medium.getMessage();
		ArrayList<byte[]> arrFrame = (ArrayList<byte[]>)list.get(1);
		if(arrFrame.size()>=50)
		{
			for (int i=0;i<25 ;i++ ) 
			arrFrame.remove(0);
		}
		arrFrame.add(frame); 
		list.set(1,arrFrame);
		medium = new Channel("file");
		medium.putMessage(list);
		medium.setIdle();
		return true;
	}
	private void mysleep()throws Throwable{
		sleep(10);
	}
	private String takeData(String file)
	{
		FileReader fr=null;
		String msg ="";
		try{
			fr = new FileReader(file);
			int c; 
		    while ((c=fr.read()) != -1) 
		    	msg+=(char)c;
		    fr.close();
		}catch(Exception ex){
			System.out.println("File not Found");
		}
		return msg;
	}

	private EthernetFrame[] generateFrameBuffer() 
	{
		String msg= takeData(this.dataFile);
		byte [] bytedata = msg.getBytes();
		int n = bytedata.length;
		int dlength = frameLength-20;
		int no_of_frame; 
		if(n%dlength==0)
			no_of_frame = n/dlength;
		else
			no_of_frame = n/dlength +1;
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
			flist[fno] = new EthernetFrame(this.dest,this.src,"DS",fno+1,data,this.frameLength);
			l=r;
			r+=dlength;
			if(r>n)
				r=n;
			fno++;
		}
		return flist;
	}

	private int creatingConnection(int frameCount) throws Throwable
	{
		String s = Integer.toString(frameCount);
		byte temp[] = s.getBytes();
		EthernetFrame RTSFrame = new EthernetFrame(dest,src,"RS",0,temp,frameLength);
		sendingFrame(RTSFrame.getFrame());
		System.out.println(src+" Sender sending request frame to reciver: "+dest);
		Date t1 = new Date();
		int rAckNo = reciveAck(t1,0);
		return rAckNo;
	}

	public void run() 
	{ 
		EthernetFrame flist[] = generateFrameBuffer();//generate frame buffer
		int frameCount = flist.length;
		try 
		{ 
			int r = -1;
			while(r<0){
				r = creatingConnection(frameCount); // creating connection between sender and reciver 
			}
			System.out.println("Connection established between Sender process:"+src+" Reciver process:"+dest);
			int count = frameCount;
			int f=1;
			//****core logic of 1-persistent protocol********//
			while(f <= frameCount)
			{
				EthernetFrame ef = flist[f-1];
				System.out.println(this.src+" Sender process sending Frame:"+f+" to reciver process "+this.dest);
				sendingFrame(ef.getFrame());
				mysleep();
				Date now = new Date();
				int rAckNo = reciveAck(now,f);
				if(rAckNo ==-1)
					continue;

				if(rAckNo==f)
				{
					System.out.println(this.src+" Sender process recive ack frame:"+f+" from reciver process "+this.dest);
					f++;
				}
				else{
					System.out.println(this.src+" Sender process recive ack frame:"+rAckNo+" from reciver process "+this.dest);
					f = rAckNo + 1;
				}
			}
			//***********************************************//
		}
		catch(Exception ex) 
		{
			System.out.println("Exception");
			ex.printStackTrace();
		} 
		catch (Throwable e) 
		{
			System.out.println("Throwable");
			e.printStackTrace();
		}
	} 
} 
public class SenderProcess
{
	public static String generateAddress(int a)
	{
		String s=Integer.toString(a);
		int c = 6-s.length();
		while(c-- >0){
			s = "0"+s;
		}
		return s;
	}
	public static void main(String args[])
	{
		int nodeNo,frameLength;
		String filename;
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter no of Sender node on the network:");
		nodeNo = Integer.parseInt(sc.nextLine());
		System.out.println("Enter name of the source data file:");
		filename = sc.nextLine();
		System.out.println("Enter frame length:");
		frameLength = Integer.parseInt(sc.nextLine());
		for(int i=1;i<=nodeNo;i++)
		{
			String src = generateAddress(i);
			String dest = generateAddress(i+1000);
			Sender sender = new Sender(nodeNo,src,dest,frameLength,filename); //Sender(int n,String s,String d,int fl,String df)
			sender.start();
		}
	}
}

