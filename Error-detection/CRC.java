package Algorithm;
import java.io.*;
import java.math.BigInteger;
import java.util.*; 
public class CRC
{
	int[] divide(int div[],int divisor[], int rem[])
    {
        int cur=0;
        while(true)
        {
            for(int i=0;i<divisor.length;i++)
                rem[cur+i]=(rem[cur+i]^divisor[i]);
           
            while(rem[cur]==0 && cur!=rem.length-1)
                cur++;
   
            if((rem.length-cur)<divisor.length)
                break;
        }
        return rem;
    }

	public byte [] crcGenerator(byte[] dataword,int divisor[]){
		int divisor_bits = divisor.length;
    BigInteger bi = new BigInteger(dataword);
    String s = bi.toString(2);
    int total_bits =s.length()+divisor_bits-1;
    int[] data = new int[total_bits];
    int[] rem = new int[total_bits];
    int[] crc = new int[total_bits];
  	for(int i=0; i<s.length(); i++) {
      rem[i] = data[i] = Integer.parseInt(String.valueOf(s.charAt(i)));
  	}
  	rem=divide(data, divisor, rem);
  	
  	for(int i=0;i<data.length;i++)           //append dividend and ramainder
    {
        crc[i]=(data[i]^rem[i]);
    }
  	String codebits = new String("");
  	for(int i=0;i<total_bits;i++){
  		codebits+= Integer.toString(crc[i]);
  	}
  	BigInteger val = new BigInteger(codebits, 2);
  	byte[] codeWord = val.toByteArray();

  	return codeWord;
	}

	public boolean crcChecker(byte[] codeWord,int divisor[]){
		BigInteger bi = new BigInteger(codeWord);
        String s = bi.toString(2);
        int total_bits=s.length();
        int[] rem = new int[total_bits];
        int[] crc = new int[total_bits];
        for(int i=0; i<total_bits; i++) {
          rem[i] = crc[i] = Integer.parseInt(String.valueOf(s.charAt(i)));
      	}
      	rem=divide(crc, divisor, rem);
      	boolean res = true;
      	for(int i=0;i<rem.length;i++)
      	{
      		if(rem[i]!=0){
      			res=false;
      			break;
      		}
      	}
      	return res;
	}

	public byte[] extractData(byte[] codeWord,int divisor[]){
		int divisor_bits = divisor.length;
        BigInteger bi = new BigInteger(codeWord);
        String s = bi.toString(2);
        int data_length = s.length() - (divisor_bits-1);
        String d = new String("");
        for(int i=0;i<data_length;i++){
        	d+=s.charAt(i);
        }
        BigInteger val = new BigInteger(d,2);
        byte[] data = val.toByteArray();
        return data;
	}
}