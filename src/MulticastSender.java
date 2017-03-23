import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class MulticastSender extends Thread{
//	public void run(){
//		try{
//			while(true){
//				byte[] buff = new byte[100000];
//				
//				InetAddress group = InetAddress.getByName(s_ip);
//				DatagramPacket packet = new DatagramPacket(buff,buff.length, group, s_port);
//				//data.send(packet);
//				
//				while(true){
//					data.receive(packet);
//				}
//				
////				try{
////					sleep((long)1000);
////				}catch(InterruptedException e){
////					System.err.println("???");
////				}
//				
//			}
//		}catch (IOException e){
//			System.err.println("oi");
//		}
////		data.close();
//	}
}
