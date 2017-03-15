import java.io.IOException;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("ola");
		BackupFile lmao = null;
		try {
			lmao = new BackupFile("cebas.txt");
		} catch (IOException e) {
			System.err.println("ups i dit it ty bruno");
		}
		catch(Exception e){
			System.err.println("file doesn't exist");
		}
		
		lmao.createChunks();
		
		System.out.println("Welcome to the Jungle. We got fun and games");
		
		lmao.assembleFile();
	}
}
