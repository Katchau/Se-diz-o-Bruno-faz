import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class BackupFile {
	private File file;
//	private String nameFile;
	private File tmpFolder;
	public static final int maxSize = 64 * 1000; //kek
	
	public BackupFile(String nameFile) throws IOException, Exception{
//		this.nameFile = nameFile;
		file = new File(nameFile);
		if(!file.exists())
			throw new FileNotFoundException();
		String folder = nameFile + "tmp";
		tmpFolder = new File(folder);
		tmpFolder.mkdir();
	}
	
	public float getSizeFile(){
		return file.length();
	}
	public static void assembleFile(String fileID, ArrayList<byte[]> chunks){
		File folder = new File("recovered");
		folder.mkdir();
		File f = new File(folder,new File(fileID).getName());
		try {
			f.createNewFile();
		} catch (IOException e) {
			System.err.println("Error creating assembly file");
		}
		try(FileOutputStream out = new FileOutputStream(f)){
			for(byte[] buffer: chunks ){			
				out.write(buffer, 0, buffer.length);
			}
		}
		catch(Exception e){
			System.err.println("Error assemblying file");
		}
	}
}
