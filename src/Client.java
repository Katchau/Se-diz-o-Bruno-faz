import java.rmi.registry.LocateRegistry; 
import java.rmi.registry.Registry; 
 
public class Client { 
  private static int peer_ap; 
  private static String sub_protocol; 
 
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
        ClientInterface stub = (ClientInterface) registry.lookup("ClientInterface"); 
         
      } catch (Exception e){ 
            System.err.println("Error: Client exception: " + e.toString()); 
      } 
    } 
} 