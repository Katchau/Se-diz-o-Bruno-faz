import java.rmi.Remote; 
import java.rmi.RemoteException; 
 
public interface ClientInterface extends Remote{ 
	void sendChunk(int repDegree, String fileID, int n, byte[] buffer, int size) throws RemoteException;
} 