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

	public void putMessage(ArrayList<Object> frameList) throws Throwable
	{ 
        ByteBuffer map = fchannel.map(FileChannel.MapMode.READ_WRITE, 0, 100000);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(100000);
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(frameList);
        map.put(baos.toByteArray());
        oos.close();
	}

	public ArrayList<Object> getMessage() throws Throwable 
	{
        ArrayList<Object> list = new ArrayList<Object>();
        list.add(0);
        list.add(new ArrayList<byte[]>());
        list.add(new ArrayList<byte[]>());
        if(file.length() == 0)
            return list;
        try{
            MappedByteBuffer map = fchannel.map(FileChannel.MapMode.READ_ONLY, 0, 100000);
            byte[] buf = new byte[100000];
            map.get(buf);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buf));
            list = (ArrayList<Object>)ois.readObject();
            ois.close();
        }
        catch(OptionalDataException ex){
            System.out.println("caugth OptionalDataException");
            putMessage(list);
        }
        catch(StreamCorruptedException ex){
            System.out.println("StreamCorruptedException");
        }
        return list;
	} 
    public boolean isIdle() throws Throwable 
    {
        if(file.length() == 0)
            return true;
        ArrayList<Object> list = getMessage();
        Integer it = (Integer)list.get(0);
        if(it.intValue() <= 0)
            return true;
        else 
            return false;
    }
    public void setIdle() throws Throwable
    {
         ArrayList<Object> list = getMessage();
         //int x = (Integer)list.get(0);
         list.set(0,0);
         putMessage(list);
    }
    public void setBusy() throws Throwable
    {
         ArrayList<Object> list = getMessage();
         /*int x = (Integer)list.get(0);
         x++;*/
         list.set(0,1);
         putMessage(list);
    }
} 