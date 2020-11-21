package channel;
import java.nio.channels.FileChannel;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.io.*;
import java.util.*;
import java.math.BigInteger;
class DataCorrupt
{
    public Packet corruptData(Packet pkt,int bits)
    {
        byte[] data = pkt.getData();
        BigInteger bi = new BigInteger(data);
        String s = bi.toString(2);
        System.out.println("Before Error data Bits:"+s);
        int length = s.length();
        char st[]=s.toCharArray();
        int rindex=(int)((Math.random() * (length-(bits+1))));
        int b=0;
        while(b<bits)
        {
            if(st[rindex+b]=='0')
                st[rindex+b]='1';
            else
                st[rindex+b]='0';
            b++;
        }
        s=new String(st);
        System.out.println("After Error data bits :"+s);
        BigInteger val = new BigInteger(s, 2);
        byte[] codeWord = val.toByteArray();
        pkt.setData(codeWord);
        return pkt;
    }
}
public class Channel
{ 
	private String fileName;
    private FileChannel fchannel=null;
    private FileLock flock=null;
    private RandomAccessFile file =null;

	public Channel(String fname)throws Exception
    {
		fileName=fname;
        file = new RandomAccessFile(fileName, "rw");
        fchannel = file.getChannel();
	}

	public void putMessage(Packet pkt) throws Throwable
	{ 
        Scanner sc = new Scanner(System.in);
        byte[] data = pkt.getData();
        DataCorrupt dc = new DataCorrupt();
        System.out.println("Enter number of bits corrupt:");
        int bits = Integer.parseInt(sc.nextLine());
        Packet newpkt = dc.corruptData(pkt,bits);
        ByteBuffer map = fchannel.map(FileChannel.MapMode.READ_WRITE, 0, 1000);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(newpkt);
        map.put(baos.toByteArray());
        oos.close();
        file.close();
	}
    
	public Packet getMessage() throws Throwable 
	{ 
        MappedByteBuffer map = fchannel.map(FileChannel.MapMode.READ_ONLY, 0, 1000);
        byte[] buf = new byte[1000];
        map.get(buf);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buf));
        Packet p = (Packet)ois.readObject();
        ois.close();
        file.close();
        return p;
	} 

    public boolean lockChannel(){
        boolean lock=false;
        try{
            flock = fchannel.lock();
            lock= true;
        } catch (OverlappingFileLockException e){
            lock = false;
        }
        catch(IOException ex){
            System.out.println("IOException");
            return false;
        }
        return lock;
    }

    public void releaseChannel(){
        try{
            fchannel.close();
        }
        catch(Exception ex){
            System.out.println("FileLock release error....");
            ex.printStackTrace();
        }
    }
} 