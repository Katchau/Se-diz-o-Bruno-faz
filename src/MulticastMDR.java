import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Random;

public class MulticastMDR extends Thread{
	private MulticastSocket data;
	private InetAddress address;
	private int id;
	private int vrs; //version xpto
	private MulticastServer m;
	private DatagramPacket packet;
	
	public MulticastMDR(Listener l, DatagramPacket packet) throws IOException{
		this.m = l.m;
		id = l.id;
		address = m.getMDRaddress();
		vrs = m.getVersion();
		data = m.getMDRdata();
		this.packet = packet;
		createResponse();
	}
//	if(rp.readChunk(id + "/" + rp.fileID)){
//			
//		}
	public MulticastMDR(Listener l){
		this.m = l.m;
		id = l.id;
		address = m.getMDRaddress();
		vrs = m.getVersion();
		data = m.getMDRdata();
	}
	
	public void sendChunk(RestoreProtocol rp){
		if(rp.readChunk(id + "/" + rp.fileID)){
			byte []buff = rp.answer();
			DatagramPacket packet = new DatagramPacket(buff,buff.length,address,data.getLocalPort());
			try {
				Random r = new Random();
				sleep(r.nextInt(BackupProtocol.MAXDELAY));
				if(!m.rs.receivedChunk(rp.fileID,rp.chunkN))data.send(packet);
				else m.rs.deleteChunk(rp.fileID, rp.chunkN);
			} catch (IOException | InterruptedException e) {
				System.err.println("Error: Sending Packet @MDR");
			}
		}
	}
	
	private void createResponse(){
		new Thread(new Runnable() {
			public void run() {
				 RestoreProtocol p = new RestoreProtocol(packet.getData(), packet.getLength());
//				 if(p.id == id && p.version != vrs) return; TODO Remover comentario
				 int indice = m.fileB.indexOf(p.fileID);
				 switch(p.subprotocol){
				 	case RestoreProtocol.msgRR:
				 		if(indice != -1){
				 			m.rs.addChunk(p.fileID,p.chunkN,p.chunk);
				 		}
				 		break;
				 	default:
				 		System.err.println("Error: Unrecognized Message received @MDR" + p.subprotocol);
				 		return;
				 }
		     }
		}).start();
	}
	
}
