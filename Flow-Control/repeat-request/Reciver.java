import channel.Channel;
import channel.EthernetFrame;
import java.util.*;
public class Reciver extends Thread { 
	private String myIP;
	int reciveFrame(int fno,String srcAdd,ArrayList<byte[]> arrFrame,String frameType)throws Throwable
	{
		int pos = -1;
		for(int i=0;i<arrFrame.size();i++){
			byte[] frame = arrFrame.get(i);
			EthernetFrame ef = new EthernetFrame(frame);
			String dest = ef.getDest();
			String src = ef.getSrc();
			String type = ef.getType();
			int no = Integer.parseInt(ef.getFrameNo());
			if(dest.equals(myIP) && srcAdd.equals(src) && no==fno && frameType.equals(type)){
				pos = i;
				break;
			}
		}
		return pos;
	}
	byte[] recivingFrame(int fno,String srcAdd, String type)throws Throwable
	{
		byte[] frame = new byte[1];
		Channel medium = new Channel("file");
		while(!medium.lockChannel());
		ArrayList<byte[]> arrFrame = medium.getMessage();
		medium.releaseChannel();
		int pos = reciveFrame(fno,srcAdd,arrFrame,type);
		if(pos>=0){
			frame = arrFrame.get(pos);
			arrFrame.remove(pos);
			medium = new Channel("file");
			while(!medium.lockChannel());
			medium.putMessage(arrFrame);
			medium.releaseChannel();
		}
		return frame;
	}
	byte[] recivingRequest()throws Throwable
	{
		byte[] frame = new byte[1];
		Channel medium = new Channel("file");
		while(!medium.lockChannel());
		ArrayList<byte[]> arrFrame = medium.getMessage();
		medium.releaseChannel();
		for(byte[] curFrame:arrFrame){
			EthernetFrame curEF = new EthernetFrame(curFrame);
			String dest = curEF.getDest();
			String type = curEF.getType();
			if(myIP.equals(dest) && type.equals("RS"))
			{
				frame = curFrame;
				arrFrame.remove(curFrame);
				medium = new Channel("file");
				while(!medium.lockChannel());
				medium.putMessage(arrFrame);
				medium.releaseChannel();
				break;
			}
		}
		return frame;
	}
	boolean sendingFrame(EthernetFrame ef)throws Throwable
	{
		Channel medium = new Channel("file");
		while(!medium.lockChannel());
		ArrayList<byte[]> arrFrame = medium.getMessage();
		medium.releaseChannel();
		if(arrFrame.size()>=10)
		{
			arrFrame.remove(0);
		}
		arrFrame.add(ef.getFrame());
		medium = new Channel("file");
		while(!medium.lockChannel());
		medium.putMessage(arrFrame);
		medium.releaseChannel();
		return true;
	}
	public void run() 
	{ 
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter machine ip address(6 digits length): ");
		myIP = sc.nextLine();
		try 
		{ 
			while(true)
			{
				byte [] data;
				String s;
				int frame_no,length;
				byte[] frame;
				//*************Reciving connection allert**************//
				frame = recivingRequest();
				//****************************************************//
				if(frame.length>1)
				{
					//************sending acceptance ack********************//
					EthernetFrame ef = new EthernetFrame(frame);
					System.out.println("Recive request from "+ef.getSrc());
					length = ef.getLength();
					int dataLength = length-20;
					String srcAdd = ef.getSrc();
					data = ef.getData();
					EthernetFrame sendAck = new EthernetFrame(srcAdd,myIP,"AK",0,data,ef.getLength());
 					if(sendingFrame(sendAck))
 					{
						frame_no = Integer.parseInt(new String(data));
						System.out.println("connection established for reciving "+frame_no+" numbers of frame.....");
						sleep(500);
						//******Collecting all Packets*************//
						int total_no_of_frame = frame_no;
						EthernetFrame[] flist = new EthernetFrame[frame_no];
						int fno=1;
						while(fno<=frame_no)
						{
							frame = recivingFrame(fno,srcAdd,"DS");
							sleep(700);
							if(frame.length >1)
							{
								EthernetFrame curFrame = new EthernetFrame(frame);
								int no = Integer.parseInt(curFrame.getFrameNo());
								if(fno==no && flist[fno-1]==null)
								{
									System.out.println("Recive Frame: "+no);
									flist[fno-1]=curFrame;
									fno++; 
									sendAck = new EthernetFrame(srcAdd,myIP,"AK",no,data,length);
									sendingFrame(sendAck);
									System.out.println("Sending Acknoledgement: "+no+"....");
									sleep(500);
								}
								else if(fno==no && flist[fno-1]!=null){
									System.out.println("Discarded Frame:"+no);
									fno++;
									sendAck = new EthernetFrame(srcAdd,myIP,"AK",no,data,length);
									sendingFrame(sendAck);
									System.out.println("Sending Acknoledgement: "+no+"....");
									sleep(500);
								}
								else if(fno!=no && flist[fno-1]==null){
									if(fno<no)
									{
										sendAck = new EthernetFrame(srcAdd,myIP,"NK",fno,data,length);
										System.out.println("Sending Nack: "+no+"....");
										sendingFrame(sendAck);
									}
									System.out.println("Recive Frame: "+no);
									flist[no-1]=curFrame;
									sleep(700);
								}
								else{
									System.out.println("Discarded Frame:"+no);
								}
							}
						}
						//***********Collection all Packets*******************//
						byte total_data[] = new byte[dataLength*frame_no];
						int f=0;
						for(int i=0;i<frame_no;i++){
							ef = flist[i];
							data = ef.getData();
							for(int j=0;j<data.length;j++)
							{
								total_data[f]=data[j];
								f++;
							}
						}
						String  msg = new String(total_data);
						System.out.println("Got Message: "+msg);
		 			}
				}
			}	
		} 
		catch(Exception ex) {
			System.out.println("Exception");
			ex.printStackTrace();
		} catch (Throwable e) {
			System.out.println("Throwable");
			e.printStackTrace();
		}	

	}
	public static void main(String args[]) {
		Reciver reciver = new Reciver();
		reciver.start();
	}

} 
