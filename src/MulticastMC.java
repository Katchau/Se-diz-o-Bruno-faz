import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastMC extends Thread{
	private MulticastSocket data;
	private InetAddress address;
	private int id;
	private int vrs; //version xpto
	private Listener l;
	private MulticastServer m;
	private DatagramPacket packet;
	
	public MulticastMC(Listener l, DatagramPacket packet) throws IOException{
		this.l = l;
		this.m = l.m;
		id = l.id;
		address = m.getMCaddress();
		vrs = m.getVersion();
		data = m.getMCdata();
		this.packet = packet;
		createResponse();
	}
	
	public MulticastMC(Listener l, byte[] buffer) throws IOException{
		this.l = l;
		this.m = l.m;
		id = l.id;
		address = m.getMCaddress();
		vrs = m.getVersion();
		data = m.getMCdata();
		this.packet = new DatagramPacket(buffer,buffer.length, address,data.getLocalPort());
		data.send(packet);
		System.out.println("Sent SAVECHUNK");
	}
		
	private void createResponse(){
		new Thread(new Runnable() {
		     public void run() {
				 Protocol p = new Protocol(packet.getData(), packet.getLength());
				 switch(p.subprotocol){
				 	case BackupProtocol.msgTypeStored:
				 		p = new BackupProtocol(packet.getData(),packet.getLength());
				 		System.out.println("Cenas");
				 		break;
				 	default:
				 		System.err.println("Error: Unrecognized Message received in MC");
				 		return;
				 }
		     }
		}).start();
	}
	
//	public MulticastMDB(Listener l, String path, int repDegree) throws IOException{
//		this.l = l;
//		this.m = l.m;
//		id = l.id;
//		address = l.address;
//		vrs = m.getVersion();
//		data = l.data;
//		
//		this.path = path;
//		this.repDegree = repDegree;
//	}
	
	public void run(){
//		readFile();
	}
}
