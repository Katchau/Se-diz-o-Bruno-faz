import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class BackupFile {
	private File file;
//	private String nameFile;
	private File tmpFolder;
	private final int maxSize = 64 * 1000;
	private int numChunks;
	
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
	
	public void createChunks(){
		int partCounter = 1;
		byte[] buffer = new byte[maxSize];
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
			String name = file.getName();
			int readSize = 0;
			while ((readSize = bis.read(buffer)) > 0) {
				File newFile = new File(tmpFolder, name + "."+ String.format("%03d", ++partCounter));
				try (FileOutputStream out = new FileOutputStream(newFile)) {
					out.write(buffer, 0, readSize);//tmp is chunk size
				}
			}
			if(getSizeFile()%maxSize == 0){
				//File = new File()
			}
		}catch(IOException e){
			System.err.println("Ups i did it again");
		}
		numChunks = partCounter;
	}
	public void assembleFile(){
		File f = new File("bananas.txt");
		try {
			f.createNewFile();
		} catch (IOException e) {
			System.err.println("Error creating assembly file");
		}
		try(FileOutputStream out = new FileOutputStream(f)){
			for(int i = 2; i <= numChunks; i++){			
				File backFile = new File(tmpFolder, file.getName() + "." + String.format("%03d", i));
				byte[] buffer = new byte[maxSize];
				int readSize = 0;
				try(BufferedInputStream in = new BufferedInputStream(new FileInputStream(backFile))){
					readSize = in.read(buffer);
					out.write(buffer, 0, readSize);
				}
			}
		}
		catch(Exception e){
			System.err.println("Error assemblying file");
		}
	}
}
