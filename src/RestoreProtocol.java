import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public class RestoreProtocol extends Protocol{
	public static final String msgRestore = "GETCHUNK";
	public static final String msgRR = "CHUNK";
	public byte[] chunk;
	
	public RestoreProtocol(byte[] message, int messageLength){
		super(message,messageLength);
		chunk = body;
	}
	
	public RestoreProtocol(int version,int senderID, String fileID,int chunkN){
		super(version,senderID,fileID,chunkN,0);//kek
	}
	
	public boolean readChunk(String folder){
		File file = new File(folder,String.format("%06d", chunkN));
		if(!file.exists())return false;
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
			byte[] buffer = new byte[BackupFile.maxSize];
			int readValue = bis.read(buffer);
			chunk = Arrays.copyOfRange(buffer, 0, readValue);
			return true;
		}
		catch (IOException e) {
			System.err.println("Error: Reading chunk"+ e.getMessage());
		}
		return false;	
	}
	
	public byte[] answer(){
		byte[] header = request(msgRR).getBytes();
		byte[] request = new byte[header.length + chunk.length];
	    System.arraycopy(header, 0, request, 0, header.length);
	    System.arraycopy(chunk, 0, request, header.length, chunk.length);
	    return request;
	}
	
	public byte[] request(){
		return request(msgRestore).getBytes();
	}
}
