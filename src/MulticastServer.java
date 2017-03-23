import java.io.IOException;
import java.net.DatagramPacket;
//import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastServer{
	private MulticastSocket data;
	private int s_port;
	private String s_ip;
	
	public MulticastServer(int port1, String ip) throws IOException{
		s_ip = ip;
		s_port = port1;
		data = new MulticastSocket(port1);
		new MulticastReceiver(data, ip).start();
	}
	
	public void clientTest() throws IOException{
	      MulticastSocket umo = new MulticastSocket(s_port);
	      
	      byte[] buff = new byte[256];
	      String antes = "lel";
	      buff = antes.getBytes();
	      
		  InetAddress group = InetAddress.getByName(s_ip);
		  DatagramPacket pacote = new DatagramPacket(buff,buff.length, group, umo.getLocalPort());
	      
	      System.out.println("sending...");
	      umo.send(pacote);
	      
	      System.out.println("kek");
	      
	      
	}
	
}
