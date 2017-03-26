import java.io.IOException;
//import java.net.DatagramPacket;
//import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
//import java.util.HashMap;

public class MulticastServer{
	private MulticastSocket data;
	private int version;
	private int id;
	private InetAddress address;
	public static final String CRLF = "\r\n";
	
	public MulticastSocket getData() {
		return data;
	}

	public int getVersion() {
		return version;
	}

	public int getId() {
		return id;
	}

	public InetAddress getAddress() {
		return address;
	}
	
	public MulticastServer(String args[]) throws IOException{
		this.version = (int)Double.parseDouble(args[0]);
		id = Integer.parseInt(args[1]);
		data = new MulticastSocket(Integer.parseInt(args[4]));
		address = InetAddress.getByName(args[3]);
		new MulticastMDB(this).start();
	}
	
	public void clientTest() throws IOException{
		new MulticastMDB(this,"cebas.txt",1).start();
	}
	
//	public void clientTest() throws IOException{
//	      MulticastSocket umo = new MulticastSocket(data.getLocalPort());
//	      
//	      byte[] buff = new byte[256];
//	      String antes = "lel";
//	      buff = antes.getBytes();
//	      
//		  DatagramPacket pacote = new DatagramPacket(buff,buff.length, address, umo.getLocalPort());
//	      
//	      System.out.println("sending...");
//	      umo.send(pacote);
//	      
//	      System.out.println("kek");
//	      
//	      
//	}
	
}
