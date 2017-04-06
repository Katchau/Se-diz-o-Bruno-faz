import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastMDR {
	private MulticastSocket data;
	private InetAddress address;
	private int id;
	private int vrs; //version xpto
	private Listener l;
	private MulticastServer m;
	private DatagramPacket packet;
	
	public MulticastMDR(Listener l) throws IOException{
		this.l = l;
		this.m = l.m;
		id = l.id;
		address = m.getMCaddress();
		vrs = m.getVersion();
		data = m.getMCdata();
	}
	
	public byte[] receiveChunk(String fileID,int nChunk){
		return "".getBytes();
	}
}
