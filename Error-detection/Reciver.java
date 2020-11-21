import channel.Channel;
import channel.Packet;
import Algorithm.CheckSum;
import Algorithm.CRC;
import Algorithm.LRC;
import Algorithm.VRC;
public class Reciver extends Thread { 
	public void run() 
	{ 
		try 
		{ 
			while(true){
				Channel medium = new Channel("file");
				while(!medium.lockChannel()){
					System.out.println("Wating.....");
				}

				Packet pkt = medium.getMessage();
				byte data[] = pkt.getData();
				int length = pkt.getLength();
				int coding_technique = pkt.getCodingTechnique();
				byte[] extracted_data;
				switch(coding_technique){
					case 1:
						byte originaldata[] = new byte[length-1];
						for(int i=0;i<length-1;i++)
						{
							originaldata[i]=data[i];
						}
						CheckSum cs = new CheckSum(data);
						byte sum = cs.getCheckSum();
						int nob=(int)((Math.floor(Math.log(sum)/Math.log(2))) +1);
						long csum = (((1 << nob) - 1) ^ sum);
						if(csum==0)
							System.out.println("Got Message: "+new String(originaldata));
						else
							System.out.println("Data is Curropted!!");
						break;
					case 2:
						int[] divisor = pkt.getDivisor();
						CRC crc = new CRC();
						if(crc.crcChecker(data,divisor))
						{
							extracted_data = crc.extractData(data,divisor);
							System.out.println("Got Message: "+new String(extracted_data));
						}
						else
							System.out.println("Data is Curropted!!");

						break;
					case 3:
						VRC vrc = new VRC();
						if(vrc.vrcChecker(data))
						{
							extracted_data = vrc.extractData(data);
							System.out.println("Got Message: "+new String(extracted_data));
						}
						else
							System.out.println("Data is Curropted!!");
						break;
					case 4:
						LRC lrc = new LRC();
						if(lrc.lrcChecker(data))
						{
							extracted_data = lrc.extractData(data);
							System.out.println("Got Message: "+new String(extracted_data));
						}
						else
							System.out.println("Data is Curropted!!");
						break;
					default:
						System.out.println("Data is Curropted!!");
						break;
				}		
				medium.releaseChannel();
				sleep(2000);
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
