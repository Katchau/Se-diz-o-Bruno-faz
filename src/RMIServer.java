import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class RMIServer implements ClientInterface {
	
	private MulticastServer ms;
	
	public RMIServer(MulticastServer ms, int peer_ap){
		this.ms = ms;
		try {
			ClientInterface stub = (ClientInterface) UnicastRemoteObject.exportObject(this, 0); //:)

			// Bind the remote object's stub in the registry
			Registry registry = LocateRegistry.getRegistry(peer_ap);
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
	
	public String getHash(boolean delete,String fileID){
		String folderPath = ms.getId() + "/files";
		File folder = new File(folderPath);
		String value = "";
		String ret = "";
		File f = null;
		for(File file: folder.listFiles()){
			String name = file.getName();
			String[] parts = name.split("_");
			String realName = "";
			for(int i = 0; i < parts.length - 1; i++){
				realName += parts[i];
			}
			if (realName.equals(fileID)){
				try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
					byte[] buffer = new byte[1024];
					int readValue = 0;
					while((readValue = bis.read(buffer)) > 0){
						value += new String(buffer,0,readValue);
					}
					String[] separate = value.split("\r\n"); //separar por new lines??	
					f = file;
					ret = separate[1];
					break;
				} catch (FileNotFoundException e) {
					System.err.println("Error, file not found: "+ e.getMessage());
				} catch (IOException e) {
					System.err.println("Error: "+ e.getMessage());
				}
				break;
			}
		}
		if(delete)f.delete();
		return ret;
	}
	
	public void deleteFile(String fileID){
		String hash = getHash(true,fileID);
		if(hash.equals("")) return;
		try {
			new MulticastMC(this.ms, DeleteProtocol.msgDelete, hash);
		} catch (IOException e) {
			System.err.println("Error: Deleting File");
		}
	}
	
	public void setMaxSize(String size){
		ms.maxSize = Integer.parseInt(size);
		if(ms.currSize > ms.maxSize){
			try {
				System.out.println("wtf");
				new MulticastMC(this.ms,ReclaimProtocol.msgRemoved, "");
			} catch (IOException e) {
				System.err.println("Error: Creating Reclaim Protocol");
			}
		}
	}
	
	public ArrayList<byte[]> restoreFile(String fileID){
		String hash = getHash(false,fileID);
		ArrayList<byte[]> chunks = new ArrayList<byte[]>();
		if(hash.equals("")) return chunks;
		try {
			chunks = new MulticastMC(this.ms, RestoreProtocol.msgRestore, hash).restoreFile(hash);
		} catch (IOException e) {
			System.err.println("Error: Deleting File");
		}
		return chunks;
	}
	
	public void saveFileInfo(String path, String fileHash ,int rep_degree, File file){
		String[] filePath = path.split("/");
		String fileID = filePath[filePath.length - 1];
		String folderPath = ms.getId() + "/files";
		String filename = fileID + "_" + file.lastModified();
		File f = new File(folderPath, filename);
		try (FileOutputStream out = new FileOutputStream(f,true)) {
			byte[] buffer = (path + "\r\n" + fileHash + "\r\n" + rep_degree + "\r\n").getBytes();
			out.write(buffer, 0, buffer.length);
		}
		catch(IOException e){
			System.err.println("Error: Making Info File " + e.getMessage());
		}
		
	}
	
	public void getState(){
		String folderPath = ms.getId() + "/files";
		File folder = new File(folderPath);
		String value = "";
		for(File file: folder.listFiles()){
			try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
				byte[] buffer = new byte[1024];
				int readValue = 0;
				while((readValue = bis.read(buffer)) > 0){
					value += new String(buffer,0,readValue);
				}
				String[] separate = value.split("\r\n"); //separar por new lines
				
				if(separate.length != 3){
					System.err.println("Error: Reading File");
					return;
				}
				
				System.out.println("File: " + file.getName());
				System.out.println(" path: " + separate[0]);
				System.out.println(" fileID: " + separate[1]);
				System.out.println(" Replication Degree: " + separate[2]);
				System.out.println("-----------------------------------");
				
			} catch (FileNotFoundException e) {
				System.err.println("Error, file not found: "+ e.getMessage());
			} catch (IOException e) {
				System.err.println("Error: "+ e.getMessage());
			}
		}
	}
}