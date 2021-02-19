package channel;
import java.io.*;
import java.util.*;
import java.math.BigInteger;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
public class EthernetFrame 
{
	private byte[] data;
	private byte[] src = new byte[6];
	private byte[] dest = new byte[6];
	private byte[] frame;
	private byte[] type = new byte[2];
	private int length;
	private String No;
	public EthernetFrame(String d,String s,String ty,int FrameNo,byte[] data,int l){
		this.src = s.getBytes();
		this.dest = d.getBytes();
		this.length = l;
		this.type = ty.getBytes();
		this.data = data;
		this.No = Integer.toString(FrameNo);
		this.makeFrame(l);
	}
	public EthernetFrame(byte[] frame){
		this.frame = frame;
		this.length = frame.length;
		int flength = 0;
		for(int i=0;i<6;i++)
			this.dest[i]=frame[flength++];
		for(int i=0;i<6;i++)
			this.src[i]=frame[flength++];
		for(int i=0;i<2;i++)
			this.type[i]=frame[flength++];
		byte[] fno;
		if(frame[flength]==0)
		{
			fno = new byte[1];
			flength++;
			fno[0] = frame[flength++];
		}
		else{
			fno = new byte[2];
			fno[0] = frame[flength++];
			fno[1] = frame[flength++];
		}
		this.No = new String(fno);
		int i = flength;
		int dlength =0;
		while(i<this.length && frame[i]!=0){
			i++;
			dlength++;
		}
		i=0;
		this.data = new byte[dlength];
		while(i<dlength){
			data[i] = frame[flength];
			flength++;
			i++;
		}
	}
	public void makeFrame(int framelength){
		this.frame = new byte[framelength];
		int flength = 0;
		for(int i=0;i<6;i++)
			frame[flength++] = this.dest[i];
		for(int i=0;i<6;i++)
			frame[flength++] = this.src[i];
		frame[flength++] = this.type[0];
		frame[flength++] = this.type[1];
		byte[] fno = No.getBytes();
		if(fno.length==1){
			frame[flength++] = 0;
			frame[flength++] = fno[0];
		}
		else{
			frame[flength++] = fno[0];
			frame[flength++] = fno[1];
		}
		for(int i=0;i<data.length;i++)
			frame[flength++] = this.data[i];
		while(flength<length)
			frame[flength++] = 0;
	}
	public byte[] getData(){
		return this.data;
	} 
	public String getSrc(){
		return new String(this.src);
	}
	public String getDest(){
		return new String(this.dest);	
	}
	public int getLength(){
		return length;
	}
	public byte[] getFrame(){
		return frame;
	}
	public String getType(){
		return new String(this.type);
	}
	public String getFrameNo(){
		return this.No;
	}
	public String toString(){
		String s = "Dest:"+new String(dest)+" Src:"+new String(src)+" Type:"+new String(type)+" Frame No:"+No;
		return s;
	}
}