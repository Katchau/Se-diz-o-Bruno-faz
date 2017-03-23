import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
//import java.net.MulticastSocket;
//import java.io.IOException;
//import java.net.DatagramSocket;
//import java.net.MulticastSocket;

public class MulticastReceiver extends Thread{
	private MulticastSocket data;
	private String s_ip;
	
	public MulticastReceiver(MulticastSocket data, String ip) throws IOException{
		s_ip = ip;
		this.data = data;
	}
	
	public void run(){
		try{
			while(true){
				String cenas = "lmao";
				byte[] buff = new byte[100000];
				buff = cenas.getBytes();
				
			    InetAddress address = InetAddress.getByName(s_ip);
			    data.joinGroup(address);
			    DatagramPacket packet = new DatagramPacket(buff,buff.length);
			    
				while(true){
					System.out.println("receiving");
					data.receive(packet);
					System.out.println(new String(packet.getData()));
					buff = new byte[100000];
					packet = new DatagramPacket(buff,buff.length);
				}
				
			}
		}catch (IOException e){
			System.err.println("oi");
		}
//		data.close();
	}

}
