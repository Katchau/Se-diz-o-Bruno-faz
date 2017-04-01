import java.io.File;

public class DeleteProtocol extends Protocol{
	public static final String msgDelete = "DELETE";
	
	public DeleteProtocol(byte[] message, int messageLength, String folderID){
		super(message, messageLength);
		deleteFile(folderID);
	}
	
	public void deleteFile(String folderID){
		File folder = new File(folderID);
		for(File file: folder.listFiles()) 
		        file.delete();
		if(!folder.delete())
			System.err.println("Error: Couldn't delete folder of file");
	}
}
