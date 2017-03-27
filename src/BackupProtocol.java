import java.io.File;
//import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Random;

public class BackupProtocol extends Protocol{
	
	//header <MessageType> <Version> <SenderID> <FileID> <ChunkNo> <ReplicationDeg> <CRLF>
	public final String msgTypeSend = "PUTCHUNK";//enviar ficheiro
	public final String msgTypeStored = "STORED"; //STORED <Version> <SenderID> <FileID> <ChunkNo> <CRLF> <CRLF>
	public static final int MAXDELAY = 400;
	public static final int TRIES = 5;
	public int delay; //ms
	public int state; //-1 = fail 0 received 1 sending 2 last
	public byte[] chunk;
	public int sizeFile;
	
	public BackupProtocol(int version,int senderID, String fileID,int chunkN, int repDegree, byte[] chunk, int size){
		this.version = version;
		this.id = senderID;
		this.fileID = fileID;
		this.chunkN = chunkN;
		this.repDegree = repDegree;
		this.chunk = chunk;
		this.subprotocol = msgTypeSend;
		this.sizeFile = size;
	}
	
	public BackupProtocol(String message){
		String[] headerbody = message.split(MulticastServer.CRLF + MulticastServer.CRLF);
		if(headerbody[0].equals(message)){
			state = -1;
			System.out.println("Error: Message damaged!");
			return;
		}
		System.out.println(headerbody[0]);
		getHeader(headerbody[0]);
		
		switch(subprotocol){
			case msgTypeSend:
				state = 0;
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
		
		if(headerbody.length == 2){
			chunk = headerbody[1].getBytes();
		}
		else if(state == 0 && headerbody.length == 1){
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
	
	public String request(){
		return request(msgTypeSend) + new String(chunk, 0, sizeFile);
	}
	
	public String storeAnswer(){
		Random r = new Random();
		delay = r.nextInt(MAXDELAY);
//		sleep((long)1000);???
		return answer(msgTypeStored);
	}

}
