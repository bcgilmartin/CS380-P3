import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.io.OutputStream;
import java.util.Hashtable;
import java.lang.String;
import java.lang.Byte;
import java.nio.ByteBuffer;
import java.math.BigInteger;
import java.net.InetSocketAddress;



public final class Ipv4Client {

    public static void main(String[] args) throws Exception {
		
		
		//connecting to socket and setup io
        try (Socket socket = new Socket("codebank.xyz", 38003)) {
			System.out.println("\nConnected to server.");
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();
			InputStreamReader isr = new InputStreamReader(is, "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			
			
			
			//header variables
			String version = "0100";
			String HLen = "0101";
			String TOS = "00000000";
			int dataLength = 1; //number of bytes in data(starting at 2) plus header(20bytes)
			String Ident = "0000000000000000";
			String Flags = "010";
			String offset = "0000000000000";
			String TTL = "00110010";
			String protocol = "00000110";
			String Checksum = "0000000000000000";
			String sourceAddr = "11000110101111011111111111001101";
			
			
			
			//getting destination address
			byte[] bytes = socket.getInetAddress().getAddress();
			String destAddr = new BigInteger(1, bytes).toString(2);
			while(destAddr.length() < 32) {
				destAddr = "0" + destAddr;
			}
			
			String length = "";
			
			
			
			//loop for sending 12 packets
			for(int i = 1; i < 13; i++) {
				
				
				
				//get correct data length binary string
				dataLength *= 2;
				System.out.println("data length: " + dataLength);
				length = Integer.toBinaryString(dataLength + 20);
				while(length.length() < 16) {
					length = "0" + length;
				}
				
				
				
				//create header with 0's in checksum
				String message = version + HLen + TOS + length + Ident + Flags + offset + TTL + protocol + Checksum + sourceAddr + destAddr;
				
				
				
				//calculate checksum and place in binary message
				int check = (int)checksum(new BigInteger(message,2).toByteArray());
				check = check & 0x0000FFFF;
				String binChecksum = Integer.toBinaryString(check);
				while(binChecksum.length() < 16) {
					binChecksum = "0" + binChecksum;
				}
				String subMsg1, subMsg2;
				subMsg1 = message.substring(0, 80);
				subMsg2 = message.substring(96);
				message = subMsg1 + binChecksum + subMsg2;
				
				
				
				//add the binary data byte strings according to the size of the data
				for(int j = 0; j < dataLength; j++) {
					message += "00000000";
				}
				
				
				
				//create packet byte array 
				byte[] packets = new BigInteger(message, 2).toByteArray();
				
				
				
				//send packet byte array
				os.write(packets);
				
				
				
				//receive and print out reponse
				System.out.println(br.readLine() + "\n");
			}
        }
		System.out.println("Disconnected from server.");
    }
	
	
	
	//checksum methods takes in a byte array and returns the checksum as a short
	public static short checksum(byte[] b) {
		long sum = 0;
		int count = b.length;
		long byteComb;
		int i = 0;
		while(count > 1) {
			byteComb = (((b[i] << 8) & 0xFF00) | ((b[i + 1]) & 0xFF));
			sum += byteComb;
			if((sum & 0xFFFF0000) > 0 ) {
				sum &= 0xFFFF;
				sum += 1;
			}
			i += 2;
			count -= 2;
		}
		if(count > 0) {
			sum += (b[b.length-1] << 8 & 0xFF00);
			if ((sum & 0xFFFF0000) > 0) {
				sum = sum & 0xFFFF;
				sum += 1;
			}
		}
		sum = ~sum;
		sum = sum & 0xFFFF;
		return (short)sum;
	}
}