package Algorithm;
import java.io.*;
import java.util.*;
import java.math.BigInteger;
public class VRC
{
	public byte[] vrcGenerator(byte[] dataword){
		BigInteger bi = new BigInteger(dataword);
        String s = bi.toString(2);
        int c=0;
        for(int i=0;i<s.length();i++){
        	if(s.charAt(i)=='1')
        		c++;
        }
        if(c%2==0)
        	s+='0';
        else
        	s+='1';
        BigInteger val = new BigInteger(s, 2);
  		byte[] codeWord = val.toByteArray();

  		return codeWord;
	}
	public boolean vrcChecker(byte[] codeWord){
		BigInteger bi = new BigInteger(codeWord);
        String s = bi.toString(2);
        int c=0;
        for(int i=0;i<s.length();i++){
        	if(s.charAt(i)=='1')
        		c++;
        }
        if(c%2==0)
        	return true;
        else
        	return false;
	}
	public byte[] extractData(byte[] codeWord){
		BigInteger bi = new BigInteger(codeWord);
        String s = bi.toString(2);
		String st = s.substring(0,s.length()-1);
		BigInteger val = new BigInteger(st, 2);
  		byte[] data = val.toByteArray();

  		return data;
	}
}