import java.util.*; 
import channel.Channel;
import channel.EthernetFrame;
import java.io.*;
class ExecutionTime{
	private long etime;
	ExecutionTime(){
		etime = 0;
	}
	public synchronized void setETime(long t){
		etime += t;
	}
	public long getEtime(){
		return etime;
	}
}
class Reciver extends Thread 
{ 
	private String address,srcAdd;
	private int totalNode,frameLength;
	private int total_frame_no;
	public Reciver(int n,String s)
	{
		this.totalNode = n;
		this.address = s;
	}

	private long timeTaken(Date start)
	{
		Date now = new Date();
		return (long)(now.getTime() - start.getTime());
	}
	public int getFrameCount(){
		return this.total_frame_no;
	}
	public int getFrameSize(){
		return this.frameLength;
	}
	private int reciveFrame(ArrayList<byte[]> arrFrame,int fno)throws Throwable
	{
		int pos = -1;
		int curFrameNo=-1;
		for(int i=0;i<arrFrame.size();i++)
		{
			byte[] frame = arrFrame.get(i);
			EthernetFrame ef = new EthernetFrame(frame);
			String dest = ef.getDest();
			String src = ef.getSrc();
			String type = ef.getType();
			int no = Integer.parseInt(ef.getFrameNo());
			if(dest.equals(this.address) && this.srcAdd.equals(src) )
			{
				if(no>curFrameNo)
				{
					pos = i;
					curFrameNo = no;
				}
			}
		}
		return pos;
	}
	private void refreshChannel(byte[] removeFrame) throws Throwable
	{
		Channel medium = new Channel("file");
		while(!medium.isIdle());
		medium.setBusy();
		ArrayList<Object> list = medium.getMessage();
		ArrayList<byte[]> arrFrame = (ArrayList<byte[]>)list.get(1);
		arrFrame.remove(removeFrame);
		list.set(1,arrFrame);
		medium = new Channel("file");
		medium.putMessage(list);
		medium.setIdle();
	}
	private EthernetFrame recivingFrame(int fno)throws Throwable
	{
		byte[] frame = new byte[1];
		int pos = -1;
		while(pos<0)
		{
			Channel medium = new Channel("file");
			ArrayList<Object> list = medium.getMessage();
			ArrayList<byte[]> arrFrame = (ArrayList<byte[]>)list.get(1);
			pos = reciveFrame(arrFrame,fno);
			if(pos>=0)
			{
				frame = arrFrame.get(pos);
			}
		}
		refreshChannel(frame);
		//sleep(100);
		return new EthernetFrame(frame);
	}

	private int recivingRequest()throws Throwable
	{
		byte[] frame = new byte[1];
		boolean recive = false;
		while(!recive)
		{
			Channel medium = new Channel("file");
			ArrayList<Object> list = medium.getMessage();
			ArrayList<byte[]> arrFrame = (ArrayList<byte[]>)list.get(1);
			for(byte[] curFrame:arrFrame)
			{
				EthernetFrame curEF = new EthernetFrame(curFrame);
				String dest = curEF.getDest();
				String type = curEF.getType();
				if(address.equals(dest) && type.equals("RS"))
				{
					frame = curFrame;
					recive = true;
					break;
				}
			}
		}
		refreshChannel(frame);
		EthernetFrame ef = new EthernetFrame(frame);
		this.srcAdd = ef.getSrc();
		byte[] data = ef.getData();
		int frame_no = Integer.parseInt(new String(data));
		this.total_frame_no = frame_no;
		this.frameLength = ef.getLength();
		System.out.println(address+" Reciver process recive request of "+frame_no+" no of frame from "+srcAdd);
		sendingAck(new EthernetFrame(this.srcAdd,this.address,"AK",0,data,this.frameLength));
		return frame_no;
	}
	private void mysleep()throws Throwable{
		sleep(20);
	}
	private boolean sendingAck(EthernetFrame ef)throws Throwable
	{
		Channel medium = new Channel("file");
		while(!medium.isIdle());
		medium.setBusy();
		ArrayList<Object> list = medium.getMessage();
		ArrayList<byte[]> arrFrame = (ArrayList<byte[]>)list.get(2);
		if(arrFrame.size()>=50)
		{
			for (int i=0;i<25 ;i++ ) 
			arrFrame.remove(0);
		}
		arrFrame.add(ef.getFrame());
		list.set(2,arrFrame);
		medium = new Channel("file");
		medium.putMessage(list);
		medium.setIdle();
		return true;
	}

	private String extarctData(EthernetFrame [] arr) throws Throwable
	{
		int dataLength = this.frameLength-20;
		int frame_no = arr.length;
		byte total_data[] = new byte[dataLength*frame_no];
		int flength=0;
		for(int i=0;i<frame_no;i++)
		{
			EthernetFrame curFrame = arr[i];
			byte[] curData = curFrame.getData();
			for(int j=0;j<curData.length;j++)
				total_data[flength++] = curData[j];
		}
		return new String(total_data);
	}

	public void run() 
	{ 
		try 
		{ 
			long start = System.currentTimeMillis();
			int frameCount = recivingRequest();
			EthernetFrame flist[] = new EthernetFrame[frameCount];
			System.out.println("Connection established between Sender process:"+srcAdd+" Reciver process:"+address);
			int f=1;
			while(f<=frameCount)
			{
				EthernetFrame ef = recivingFrame(f);
				int fno = Integer.parseInt(ef.getFrameNo());
				if(fno==f)
				{
					System.out.println(this.address+" Reciver process recive Frame: "+f+" from sender:"+this.srcAdd);
					flist[f-1] = ef;
					sendingAck(new EthernetFrame(this.srcAdd,this.address,"AK",f,ef.getData(),this.frameLength));
					System.out.println(this.address+" Reciver process sending Ack: "+f+" to sender:"+this.srcAdd);
					f++;
					mysleep();
				}
				else if(fno==0)
				{
					System.out.println(this.address+" Reciver process recive Frame: "+fno+" from sender:"+this.srcAdd);
					sendingAck(new EthernetFrame(this.srcAdd,this.address,"AK",0,ef.getData(),this.frameLength));
					mysleep();
				}
				else 
				{
					System.out.println(this.address+" Reciver process recive Frame: "+fno+" from sender:"+this.srcAdd);
					System.out.println(this.address+" Reciver process Resend Ack: "+(f-1)+" to sender:"+this.srcAdd);
					sendingAck(new EthernetFrame(this.srcAdd,this.address,"AK",f-1,ef.getData(),this.frameLength));
					mysleep();
				}
			}
			 long end = System.currentTimeMillis();
			 long total_time = end - start;
			 float sec = (end - start) / 1000;
			 float throughput = frameCount/sec;
			 float bandwidth = throughput*frameLength*8;
			String data = extarctData(flist);
			System.out.println(address+" Reciver process got data from Sender "+srcAdd+" : "+data+
				"\nTotal Time consume:"+sec+" sec"+
				"\nThroughput:"+throughput+"/sec"+
				"\nBandwidth:"+bandwidth+" bits/sec");
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
public class ReciverProcess
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
	private static long timeTaken(Date start)
	{
		Date now = new Date();
		return (long)(now.getTime() - start.getTime());
	}
	private static void startAllReceiver(int nodeno){
		for(int i=1;i<=nodeno;i++)
		{
			String add = generateAddress(i+1000);
			Reciver reciver = new Reciver(nodeno,add); 
			reciver.start();
			//frame_no = reciver.getFrameCount();
			//frame_size = reciver.getFrameSize();
		}

	}
	public static void main(String args[])
	{
		int nodeNo=0;
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter no of Reciver node on the network:");
		nodeNo = Integer.parseInt(sc.nextLine());
		startAllReceiver(nodeNo);
	}
}

