import java.io.IOException;
import channels.MulticastServer;


public class Main {

	public static void main(String[] args) {
		try{
			new MulticastServer(args);
		}
		catch(IOException e){
			System.err.println("Error: Starting Server");
		}
	}
}
