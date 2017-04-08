import java.io.File;
import java.rmi.Remote; 
import java.rmi.RemoteException; 
 
public interface ClientInterface extends Remote{ 
	void sendChunk(int repDegree, String fileID, int n, byte[] buffer, int size) throws RemoteException;
	void saveFileInfo(String path, String fileHash , int rep_degree, File file) throws RemoteException;
	void getState() throws RemoteException;
	void deleteFile(String fileID) throws RemoteException;
	void restoreFile(String fileID) throws RemoteException;
} 