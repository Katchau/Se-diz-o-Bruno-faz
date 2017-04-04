
public class RestoreProtocol extends Protocol{
	public static final String msgRestore = "GETCHUNK";
	public static final String msgRR = "CHUNK";
	private byte[] chunk;
	
	public RestoreProtocol(byte[] message, int messageLength){
		super(message,messageLength);
		
	}
	
	public RestoreProtocol(int version,int senderID, String fileID,int chunkN){
		super(version,senderID,fileID,chunkN,0);
	}

}
