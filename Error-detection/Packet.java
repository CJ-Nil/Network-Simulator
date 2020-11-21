package channel;
import java.util.*;
import java.io.Serializable;
public class Packet implements Serializable
{
    private static final long serialVersionUID = 1L;
    private byte [] data;
    private int length;
    private int coding_technic;
    private int divisor[];
    public Packet(){}
    public Packet(byte[] data,int l,int ct){
        this.data=data;
        length=l;
        coding_technic = ct;
    }
    public Packet(byte[] data,int l,int ct,int div[]){
        this.data=data;
        length=l;
        coding_technic = ct;
        divisor = div;
    }
    public void setData(byte[] data){
        this.data = data;
    }
    public byte[] getData(){
        return data;
    }
    public int getLength(){
        return length;
    }
    public int getCodingTechnique(){
        return coding_technic;
    }
    public int[] getDivisor(){
        return divisor;
    }
}