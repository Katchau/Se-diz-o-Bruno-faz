import java.io.File;

public class ReclaimProtocol extends Protocol{
	public final static String msgRemoved = "REMOVED";
	public long removedSize;
	
	public ReclaimProtocol(int version,int senderID, String fileID){
		super(version,senderID,fileID);
		removedSize = 0;
	}
	
	public boolean removeChunk(String serverID){
		File folder = new File(serverID);
		int nChunk = 0;
		for(File f : folder.listFiles()){
			nChunk = Integer.parseInt(f.getName());
			removedSize = f.length();
			f.delete();
			break;
		}
		chunkN = nChunk;
		boolean deleteF = folder.listFiles().length == 0;
		if(deleteF)folder.delete();
		return deleteF;
	}
	
	public byte[] request(){
		return request(msgRemoved).getBytes();
	}
}
