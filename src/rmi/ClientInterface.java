package rmi;
import java.io.File;
import java.rmi.Remote; 
import java.rmi.RemoteException;
import java.util.ArrayList; 
 
public interface ClientInterface extends Remote{ 
	void sendChunk(int repDegree, String fileID, int n, byte[] buffer, int size, String path, File file) throws RemoteException;
	void saveFileInfo(String path, String fileHash , int rep_degree, File file) throws RemoteException;
	String getState() throws RemoteException;
	void setMaxSize(String size) throws RemoteException;
	void deleteFile(String fileID) throws RemoteException;
	ArrayList<byte[]> restoreFile(String fileID) throws RemoteException;
} 