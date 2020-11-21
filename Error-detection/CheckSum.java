package Algorithm;
import java.io.*; 
public class CheckSum
{
	private byte data[];
	private byte checksum;
	public CheckSum(byte[] data){
		this.data=data;
		int len=data.length;
		int crc = 0x00;
	  	for (int pos = 0; pos < len; pos++) {
	    	crc ^= (int)data[pos];   // XOR byte into byte of crc

		    for (int i = 8; i != 0; i--) {    // Loop over each bit
			      if ((crc & 0x01) != 0) {      // If the LSB is set
			        crc >>= 1;                    // Shift right and XOR 0x0A
			        crc ^= 0x0A;
			      }
			      else                            // Else LSB is not set
			        crc >>= 1;                    // Just shift right
		    }
	    }
	    checksum=(byte)crc;
	}
	public byte getCheckSum(){
		return checksum;
	}
	public byte[]codeWord(){
		byte[] codeword = new byte[data.length+1];
		int i=0;
		for(i=0;i<data.length;i++){
			codeword[i]=data[i];
		}
		codeword[i]=checksum;
		return codeword;
	}
}
