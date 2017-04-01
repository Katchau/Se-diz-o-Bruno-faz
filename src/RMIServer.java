import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.io.IOException;
import java.rmi.server.UnicastRemoteObject;

public class RMIServer implements ClientInterface {
	
	private MulticastServer ms;
	
	public RMIServer(MulticastServer ms){
		this.ms = ms;
		try {
			ClientInterface stub = (ClientInterface) UnicastRemoteObject.exportObject(this, 0); //:)

			// Bind the remote object's stub in the registry
			Registry registry = LocateRegistry.getRegistry(1009);
			registry.bind("ClientInterface", stub);

			System.out.println("RMI Server ready");
		} catch (Exception e) {
			System.err.println("Error: RMI Server exception: " + e.toString());
			e.printStackTrace();
		}
	}
	
	public void sendChunk(int repDegree, String fileID, int n, byte[] buffer, int size){
		try {
			MulticastMDB mdb = new MulticastMDB(this.ms, repDegree);
			mdb.sendChunk(fileID, n, buffer, size);
		} catch (IOException e) {
			System.err.println("Error: Sending chunk");
		}
	}
}