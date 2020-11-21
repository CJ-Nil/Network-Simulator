import java.util.*; 
import channel.Channel;
import channel.Packet;
import Algorithm.CheckSum;
import Algorithm.CRC;
import Algorithm.LRC;
import Algorithm.VRC;
import java.io.*; 
public class Sender extends Thread { 
	public void run() 
	{ 
		Scanner sc = new Scanner(System.in);
		try { 
			while (true) { 
				Channel medium = new Channel("file");
				while(!medium.lockChannel())
					System.out.println("Wating.....");

				System.out.println("Enter File name for input data: ");
				String filename = sc.nextLine();
				String msg="";
				FileReader fr=null;
				try{
					fr = new FileReader(filename);
					int c; 
				    while ((c=fr.read()) != -1) 
				    	msg+=(char)c;
				    fr.close();
				}catch(Exception ex){
					System.out.println("File not Found");
					medium.releaseChannel();
					continue;
				}
				byte [] data = msg.getBytes();
				System.out.println("1.CheckSum code Word\n2.CRC code Word\n3.VRC code Word\n4.LRC code Word");
				System.out.println("Enter Your Choice Of Data Converting Technique:");
				int ch = Integer.parseInt(sc.nextLine());
				byte codeword[];
				Packet pkt;
				switch(ch){
					case 1:
						CheckSum cs = new CheckSum(data);
						codeword = cs.codeWord();
						pkt = new Packet(codeword,codeword.length,1);
						medium.putMessage(pkt); 
						break;
					case 2:
						System.out.println("Enter number of bits in divisor : ");
				        int divisor_bits=Integer.parseInt(sc.nextLine());
				        int divisor[]=new int[divisor_bits];
				        System.out.println("Enter Divisor bits : ");
				        for(int i=0; i<divisor_bits; i++)
				            divisor[i]=Integer.parseInt(sc.nextLine());
				        CRC crc = new CRC();
				        codeword = crc.crcGenerator(data,divisor);
				        pkt = new Packet(codeword,codeword.length,2,divisor);
						medium.putMessage(pkt); 
				        break;
				    case 3:
				    	VRC vrc = new VRC();
				    	codeword = vrc.vrcGenerator(data);
				    	pkt = new Packet(codeword,codeword.length,3);
				    	medium.putMessage(pkt);
				    	break;
				    case 4:
				    	LRC lrc = new LRC();
				    	codeword = lrc.lrcGenerator(data);
				    	pkt = new Packet(codeword,codeword.length,4);
				    	medium.putMessage(pkt);
				    	break;
				    default:
				    	System.out.println("Invalid Choice!!");
				    	break;
				}
				medium.releaseChannel(); 
				sleep(1000); 
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
		Sender sender = new Sender();
		sender.start();
	}
} 

