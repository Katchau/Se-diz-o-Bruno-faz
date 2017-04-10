import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry; 
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays; 
import rmi.*;
import system.*;

public class Client { 
  private static int peer_ap; 
  private static String sub_protocol; 
  private static String filePath;
  private static int rep_degree;
  private static ClientInterface stub;
 
    private Client() {} 
     
    public static void main(String[] args){ 
      if(args.length < 2 && args.length > 4){ 
        System.err.println("Error: Wrong number of argument"); 
        return; 
      } 
       
      peer_ap = Integer.parseInt(args[0]); 
      sub_protocol = args[1]; 
        
      try{ 
    	  	Registry registry = LocateRegistry.getRegistry(peer_ap); 
    	  	stub = (ClientInterface) registry.lookup("ClientInterface");
    	  	switch(sub_protocol){
    	      case "BACKUP":
    	          filePath = args[2];
    	          rep_degree = Integer.parseInt(args[3]);
    	          readFile();
    	          break;
    	      case "BACKUPENH": //isto ñ faz sentido xD
    	          filePath = args[2];
    	          rep_degree = Integer.parseInt(args[3]);
    	          readFile();
    	          break;
    	      case "RESTORE":
    	          filePath = args[2];
    	          restoreFile(filePath);
    	    	  break;
    	      case "DELETE":
    	    	  filePath = args[2];
    	    	  stub.deleteFile(filePath);
    	    	  break;
    	      case "DELETEENH":
    	    	  filePath = args[2];
    	    	  stub.deleteFile(filePath);
    	    	  break;
    	      case "RECLAIM":
    	    	  stub.setMaxSize(args[2]);
    	    	  break;
    	      case "STATE":
    	    	  System.out.println(stub.getState());
    	    	  break;
    	      default: 
                  System.err.println("Error in sub-protocol"); 
                  break;
    	      }
      } catch (Exception e){ 
    	  
            System.err.println("Error: Client exception: "  ); 
            e.printStackTrace();
      } 
    } 
    
    private static void restoreFile(String filePath) throws RemoteException{
    	ArrayList<byte[]> chunks = stub.restoreFile(filePath);
    	BackupFile.assembleFile(filePath, chunks);
    }
    
	private static void readFile(){
		int partCounter = 0;
		byte[] buffer = new byte[BackupFile.maxSize];
		File file = new File(filePath);
		if(!file.exists()){
			System.out.println("The following path " + filePath + " doesn't exist!");
			return;
		}
		String hashname = createHash(file);
		if(hashname.equals(""))return;
		
		try {
			stub.saveFileInfo(filePath, hashname, rep_degree, file);
		} catch (RemoteException e1) {
			System.err.println("Error:" + e1.getMessage());
		}
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
			int readValue = 0;
			while ((readValue = bis.read(buffer)) > 0) {
				byte[] message = Arrays.copyOfRange(buffer, 0, readValue);
				stub.sendChunk(rep_degree, hashname,++partCounter, message, readValue, filePath, file);
			}
			if(file.length()%BackupFile.maxSize == 0){
				stub.sendChunk(rep_degree, hashname,++partCounter, "".getBytes(),0, filePath, file);
			}
		}catch(IOException e){
			System.err.println("Ups i did it again");
		}
	}
	
	private static String createHash(File file){
		try {
  		 	 MessageDigest md = MessageDigest.getInstance("SHA-256");
	    	 md.update(filePath.getBytes("UTF-16"));
	    	 md.update(Integer.toString((int)file.length()).getBytes());
	    	 md.update(Integer.toString((int)file.lastModified()).getBytes());
	    	 byte[] byteData = md.digest();
	    	 StringBuffer sb = new StringBuffer();
	    	 for (int i = 0; i < byteData.length; i++) {
	             sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
	    	 }
	    	 return sb.toString();
		} catch (Exception e1) {
			System.err.println("this shouldn't happen");
			return "";
		}
	}
} 