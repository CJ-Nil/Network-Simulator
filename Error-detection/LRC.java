package Algorithm;
import java.io.*;
import java.util.*;
public class LRC
{
	public byte[] lrcGenerator(byte[] dataword){
		byte lrc = 0;
	    for (int i = 0; i < dataword.length; i++)
	    {
	        lrc ^= dataword[i];
	    }
	    byte[] codeword = new byte[dataword.length+1];
	    int i=0;
	    for(i=0;i<codeword.length-1;i++){
	    	codeword[i]=dataword[i];
	    }
	    codeword[i]=lrc;
	    return codeword;
	}
	public boolean lrcChecker(byte[] codeword){
		byte lrc=0;
		for (int i = 0; i < codeword.length; i++)
	    {
	        lrc ^= codeword[i];
	    }
	    if(lrc==0)
	    	return true;
	    else
	    	return false;
	}
	public byte[] extractData(byte[] codeword){
		int length=codeword.length-1;
		byte[] data = new byte[length];
		for(int i=0;i<length;i++)
			data[i] = codeword[i];
		return data;
	}

}