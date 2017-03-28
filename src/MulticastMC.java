import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastMC extends Thread{
//	private MulticastSocket data;
//	private InetAddress address;
//	private int id;
//	private int vrs; //version xpto
//	
//	public MulticastMC(MulticastServer m) throws IOException{
//		id = m.getId();
//		address = m.getAddress();
//		vrs = m.getVersion();
//		data = m.getData();
//	}
//	
//	public void run(){
//		try{
//				String cenas = "lmao";
//				byte[] buff = new byte[100000];
//				buff = cenas.getBytes();
//				
//			    data.joinGroup(address);
//			    DatagramPacket packet = new DatagramPacket(buff,buff.length);
//			    
//				System.out.println("receiving");
//				data.receive(packet);
//				System.out.println("oi");
//				System.out.println(new String(packet.getData()));
//				buff = new byte[100000];
//				packet = new DatagramPacket(buff,buff.length);
//					
//		}catch (IOException e){
//			System.err.println("oi");
//		}
////		data.close();
//	}
}
