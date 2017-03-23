import java.util.Random;

public class BackupProtocol {

//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//
//	}
	
	//header <MessageType> <Version> <SenderID> <FileID> <ChunkNo> <ReplicationDeg> <CRLF>
	private final String msgTypeSend = "PUTCHUNK";//enviar ficheiro
	private final String msgTypeStored = "STORED"; //STORED <Version> <SenderID> <FileID> <ChunkNo> <CRLF> <CRLF>
	private int delay; //ms
	
	public BackupProtocol(String version,int senderID, String fileID,int chunkN, int repDegree){
		Random r = new Random();
		delay = r.nextInt(400);
	}

}
