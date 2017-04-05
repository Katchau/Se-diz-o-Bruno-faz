import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry; 
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.util.Arrays; 
 
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
    	      case "RESTORE":
    	          filePath = args[2];
    	    	  break;
    	      case "DELETE":
    	    	  filePath = args[2];
    	    	  stub.deleteFile(filePath);
    	    	  break;
    	      case "STATE":
    	    	  stub.getState();
    	    	  break;
    	      }
      } catch (Exception e){ 
            System.err.println("Error: Client exception: " + e.toString()); 
      } 
    } 
    
	private static void readFile(){
		int partCounter = 0;
		byte[] buffer = new byte[BackupFile.maxSize];
		File file = new File(filePath);
		if(!file.exists()){
			System.out.println("Oh meu ganda burro, " + filePath + " não existe!");
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
				for(int i = 0; i < rep_degree; i++)
					stub.sendChunk(rep_degree, hashname,++partCounter, message, readValue); //TODO falta por o timer
			}
			if(file.length()%BackupFile.maxSize == 0){
				for(int i = 0; i < rep_degree; i++)
					stub.sendChunk(rep_degree, hashname,++partCounter, "".getBytes(),0);
			}
		}catch(IOException e){
			System.err.println("Ups i did it again");
		}
	}
	
	private static String createHash(File file){
		try {
  		 	 MessageDigest md = MessageDigest.getInstance("SHA-256");
	    	 md.update(filePath.getBytes("UTF-8"));
	    	 md.update(Integer.toString((int)file.length()).getBytes());
	    	 md.update(Integer.toString((int)file.lastModified()).getBytes());
	    	 return new String(md.digest());
		} catch (Exception e1) {
			System.err.println("this shouldn't happen");
			return "";
		}
	}
} 