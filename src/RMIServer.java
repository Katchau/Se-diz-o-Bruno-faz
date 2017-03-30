import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMIServer implements ClientInterface {
  
 public RMIServer(){
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
}