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
public class Channel
{ 
	private String fileName;
    private FileChannel fchannel;
    private FileLock flock;
    private RandomAccessFile file;

	public Channel(String fname)throws Throwable
    {
		fileName=fname;
        file = new RandomAccessFile(fileName, "rw");
        fchannel = file.getChannel();
	}

	public void putMessage(ArrayList<byte[]> frameArr) throws Throwable
	{ 
        ByteBuffer map = fchannel.map(FileChannel.MapMode.READ_WRITE, 0, 2000);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(2000);
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(frameArr);
        map.put(baos.toByteArray());
        oos.close();
        file.close();
	}

	public ArrayList<byte[]> getMessage() throws Throwable 
	{ 
        ArrayList<byte[]> frameArr = new ArrayList<byte[]>();
        if(file.length() == 0)
            return frameArr;
        MappedByteBuffer map = fchannel.map(FileChannel.MapMode.READ_ONLY, 0, 2000);
        byte[] buf = new byte[2000];
        map.get(buf);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buf));
        frameArr = (ArrayList<byte[]>)ois.readObject();
        ois.close();
        file.close();
        return frameArr;
	} 
    public boolean lockChannel(){
        boolean lock=false;
        try{
            flock = fchannel.tryLock();
        } catch (OverlappingFileLockException e){
            e.printStackTrace();
        }
        catch(IOException ex){
            System.out.println("IOException");
        }
        if(flock == null)
            return false;
        else
            return true;
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