import java.io.File;
//import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Random;

public class BackupProtocol extends Protocol{
	
	//header <MessageType> <Version> <SenderID> <FileID> <ChunkNo> <ReplicationDeg> <CRLF>
	public static final String msgTypeSend = "PUTCHUNK";//enviar ficheiro
	public static final String msgTypeStored = "STORED"; //STORED <Version> <SenderID> <FileID> <ChunkNo> <CRLF> <CRLF>
	public static final int MAXDELAY = 400;
	public static final int TRIES = 5;
	public int delay = 0; //ms
	public byte[] chunk;
	public int sizeFile;
	
	public BackupProtocol(int version,int senderID, String fileID,int chunkN, int repDegree, byte[] chunk, int size){
		super(version,senderID,fileID,chunkN,repDegree);
		this.chunk = chunk;
		this.subprotocol = msgTypeSend;
		this.sizeFile = size;
	}
	
	public BackupProtocol(byte[] message, int messageLength){
		super(message, messageLength);
		
		switch(subprotocol){
			case msgTypeSend:
				state = 0;
				Random r = new Random();
				delay = r.nextInt(MAXDELAY);
				break;
			case msgTypeStored:
				state = 1;
				break;
			default:
				state = -1;
				System.out.println("Error: Protocol of message unknown");
				return;
		}
		
		if(new File(fileID,String.format("%06d", chunkN)).exists()){
			state = -1;
			return;
		}
		
		if(body != null){
			chunk = body; //dude what dorgas?
		}
		else if(state == 0 && body == null){
			chunk = "".getBytes();
		}
	}
	
	public void storeChunk(String folder){
		File newFile = new File(folder,String.format("%06d", chunkN));
		try {
			FileOutputStream out = new FileOutputStream(newFile);
			out.write(chunk, 0, chunk.length);
			out.close();
		} catch (Exception e) {
			System.err.println("Error: Write File");
		}
		
	}
	
	public byte[] request(){
		byte[] header = request(msgTypeSend).getBytes();
		byte[] request = new byte[header.length + chunk.length];
	    System.arraycopy(header, 0, request, 0, header.length);
	    System.arraycopy(chunk, 0, request, header.length, chunk.length);
	    return request;
	}
	
	public byte[] storeAnswer(){
		return answer(msgTypeStored).getBytes();
	}

}
