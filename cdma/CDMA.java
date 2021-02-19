import java.util.*;
import java.math.BigInteger;
import java.io.*;
/*********************Sender Class**************************/
class Sender extends Thread 
{ 
	private String dataFile;
	private int pid;
	private int totalNode;
	private CDMA channel;
	private int[] sequence;
	public Sender(String df,int p,CDMA ch)
	{
		this.pid = p;
		this.dataFile = df;
		this.channel = ch;
		sequence = channel.getSequence(pid);
		totalNode = channel.getNodeCount();
	}

	public int[] generateBitData(String data){
		BigInteger bi = new BigInteger(data.getBytes());
    	String s = bi.toString(2);
    	int bitData[] = new int[s.length()];
    	for(int i=0;i<s.length();i++){
    		bitData[i] = ((int)(s.charAt(i)))-48;
    	}
    	return bitData;
	}

	public int[] encodeData(int bit){
		if(bit == 0)
			bit = -1;
		int codedData[] = new int[totalNode];
		for(int i=0;i<totalNode;i++){
			codedData[i] = sequence[i]*bit;
		}
		return codedData;
	}
	private void mysleep()throws Throwable{
		sleep(0);
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

	public void run() 
	{ 
		try 
		{ 
			String data = takeData(dataFile);
			System.out.println(pid+" Sender process sending data to "+pid+" process:"+data);
			int codedData[] ;
			int i=0;
			while(i < 8)
			{
				codedData = encodeData(data.charAt(i)-48);
				while(!channel.isReady(pid));
				channel.send(codedData);
				System.out.println(pid+" Sender sending bit "+data.charAt(i));
				i++;
				mysleep();
			}
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

/************************Reciver Class***********************/

class Reciver extends Thread 
{ 
	private int pid;
	private int totalNode;
	private CDMA channel;
	private int[] sequence;
	public Reciver(int p,CDMA ch)
	{
		this.pid = p;
		this.channel = ch;
		sequence = channel.getSequence(pid);
		totalNode = channel.getNodeCount();
	}

	private long timeTaken(Date start)
	{
		Date now = new Date();
		return (long)(now.getTime() - start.getTime());
	}
	
	private void mysleep()throws Throwable{
		sleep(0);
	}
	
	private String extarctData(String s)
	{
		System.out.println("Got bit stream:"+s);
		BigInteger val = new BigInteger(s, 2);
  		byte[] byteData = val.toByteArray();
  		String sd = new String(byteData);
  		return sd;
	}

	public void run() 
	{ 
		try 
		{ 
			long start = System.nanoTime();
			int i=0;
			int bit;
			String bitstring = new String("");
			while(i<8){
				while(channel.isReady(pid));
				bit = channel.receive(pid);
				bitstring+=String.valueOf(bit);
				i++;
				System.out.println(pid+" Reciver receive bit "+bit);
				channel.flush(pid,bit);
				mysleep();
			}
			long end = System.nanoTime();
			long total_time = end - start;
			float sec = (end - start);
			System.out.println(pid+" Reciver process got data from Sender process "+pid+" : "+bitstring+
				"\nTotal Time consume:"+sec+" Nano sec");
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


/*******Channel Class*************************/ 

public class CDMA { 
	private int[][] wtable; 
	private int num_stations;
	private int data[];

	public CDMA(int n){
		this.num_stations = n;
		data = new int[num_stations];
	}

	public int[] getSequence(int pid){
		int sequence[] = new int[num_stations];
		for(int i=0;i<num_stations;i++){
			sequence[i] = wtable[pid][i];
		}
		return sequence;
	}
	public int getNodeCount(){
		return num_stations;
	}

	public synchronized boolean isReady(int pid) throws Throwable{
        int innerProduct = 0; 
        
        for (int i = 0; i < num_stations; i++) { 
            // multiply channel sequence and source station code 
            innerProduct += data[i] * wtable[pid][i]; 
        }
        int bitData = innerProduct/num_stations;
        if(bitData==0)
            return true;
        else
            return false; 
    }

    public synchronized boolean send(int sendData[]) throws Throwable{
        for(int i=0;i<num_stations;i++){
            data[i] += sendData[i];
        }
        return true;
    }

    public int receive (int pid)throws Throwable{
        int innerProduct = 0; 
		
		for (int i = 0; i < num_stations; i++) { 
			// multiply channel sequence and source station code 
			innerProduct += wtable[pid][i] * data[i]; 
		} 
		int bitData = innerProduct/num_stations;
        if(bitData == -1)
            return 0;
        else 
            return 1;
    }

    public synchronized boolean flush(int pid,int bit) throws Throwable{
        if(bit==0)
            bit = -1;
        for(int i=0;i<num_stations;i++){
            data[i] -= wtable[pid][i]*bit;
        }
        return true;
    }

	public void setUp() 
	{ 
		wtable = new int[num_stations][num_stations]; 
		buildWalshTable(num_stations, 0, num_stations - 1, 0,num_stations - 1, false); 

		showWalshTable(num_stations); 
	} 

	public int buildWalshTable(int len, int i1, int i2, int j1, int j2, boolean isBar) 
	{ 
		// len = size of matrix. (i1, j1), (i2, j2) are 
		// starting and ending indices of wtable. 
		
		// isBar represents whether we want to add simple entry 
		// or complement(southeast submatrix) to wtable. 

		if (len == 2) { 

			if (!isBar) { 
				wtable[i1][j1] = 1; 
				wtable[i1][j2] = 1; 
				wtable[i2][j1] = 1; 
				wtable[i2][j2] = -1; 
			} 
			else { 
				wtable[i1][j1] = -1; 
				wtable[i1][j2] = -1; 
				wtable[i2][j1] = -1; 
				wtable[i2][j2] = +1; 
			} 

			return 0; 
		} 
		int midi = (i1 + i2) / 2; 
		int midj = (j1 + j2) / 2; 
		buildWalshTable(len / 2, i1, midi, j1, midj, isBar); 
		buildWalshTable(len / 2, i1, midi, midj + 1, j2, isBar); 
		buildWalshTable(len / 2, midi + 1, i2, j1, midj, isBar); 
		buildWalshTable(len / 2, midi + 1, i2, midj + 1, j2, !isBar); 
		return 0; 
	} 

	public void showWalshTable(int num_stations) 
	{ 
		System.out.println("-------------Walsh Table------------"); 
		System.out.println("Process Id\tSequence No"); 

		for (int i = 0; i < num_stations; i++) { 
			System.out.println(i+"\t\t"+Arrays.toString(wtable[i]));
		} 
		System.out.println("-------------------------------------"); 
	} 
	
	// Driver Code 
	public static void main(String[] args) 
	{ 
		int num_stations; 
		String datafilename;
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter no of Sender and Reciver node on the network:");
		num_stations = Integer.parseInt(sc.nextLine());
		System.out.println("Enter name of the source data file:");
		datafilename = sc.nextLine();
		CDMA channel = new CDMA(num_stations); 
		channel.setUp(); 
		try{
			for(int i=0;i<num_stations;i++)
			{
				Sender sender = new Sender(datafilename,i,channel);
				Reciver reciver = new Reciver(i,channel); 
				sender.start();
				reciver.start();
				System.out.println("Sender "+i+" is online....");
				System.out.println("Reciver "+i+" is online....");
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		catch (Throwable e) 
		{
			System.out.println("Throwable");
			e.printStackTrace();
		}
		
	}
} 
