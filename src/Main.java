import java.io.IOException;
import java.util.Scanner;


//// TODO Auto-generated method stub
//System.out.println("ola");
//BackupFile lmao = null;
//try {
//	lmao = new BackupFile("cebas.txt");
//} catch (IOException e) {
//	System.err.println("ups i dit it ty bruno");
//}
//catch(Exception e){
//	System.err.println("file doesn't exist");
//}
//
//lmao.createChunks();
//
//System.out.println("Welcome to the Jungle. We got fun and games");
//
//lmao.assembleFile();

//1.0 69 nao_e_preciso	MDB 230.0.0.1 4445

public class Main {

	public static void main(String[] args) {
		try{
			MulticastServer try1 = new MulticastServer(args);
			Scanner scani = new Scanner(System.in);
			String banana = scani.nextLine();
			scani.close();
			if(banana.equals("banana")){
				try1.clientTest();
			}
		}
		catch(IOException e){
			System.err.println("fodeu xp");
		}
	}
}
