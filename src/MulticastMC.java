import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;

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
	
	public MulticastMC(MulticastServer m, String protocol, String fileID) throws IOException{
		this.m = m;
		id = m.getId();
		address = m.getMCaddress();
		vrs = m.getVersion();
		data = m.getMCdata();
		byte []buffer = new Protocol(vrs,id,fileID).answer(protocol).getBytes();
		if(protocol.equals(DeleteProtocol.msgDelete))
			data.send(new DatagramPacket(buffer,buffer.length,address,data.getLocalPort()));
		if(protocol.equals(ReclaimProtocol.msgRemoved))
			deleteFiles();
//		if(protocol.equals(RestoreProtocol.msgRestore))
//			restoreFile(fileID);
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
//				 if(p.id == id && p.version != vrs) return; TODO Remover comentario
				 int indice = m.fileB.indexOf(p.fileID);
				 switch(p.subprotocol){
				 	case BackupProtocol.msgTypeStored:
				 		new BackupProtocol(packet.getData(),packet.getLength());
				 		break;
				 	case DeleteProtocol.msgDelete:
				 		if(indice != -1)
				 			deleteFileServerInfo(p.fileID);
				 		break;
				 	case RestoreProtocol.msgRestore:
				 		if(indice != -1){
				 			RestoreProtocol rp = new RestoreProtocol(packet.getData(),packet.getLength());
				 			rp.id = id;
				 			new MulticastMDR(l).sendChunk(rp);
				 		}
				 		break;
				 	case ReclaimProtocol.msgRemoved:
				 		//TODO fazer isto dp :)
				 		System.out.println("Não me apetece fazer isto xp");
				 		//TODO isto usa a pseudo melhoria do restore
				 		break;
				 	default:
				 		System.err.println("Error: Unrecognized Message received @MC " + p.subprotocol );
				 		return;
				 }
		     }
		}).start();
	}
	
	public ArrayList<byte[]> restoreFile(String fileID){

		        boolean moreChunks = true;
		    	int nChunk = 1;
		 		ArrayList<byte[]> chunks = new ArrayList<byte[]>();
		 		do{
		 			RestoreProtocol rp = new RestoreProtocol(vrs,id,fileID,nChunk);
		 			byte[] buffer = rp.request();
		 			DatagramPacket packet = new DatagramPacket(buffer,buffer.length,address,data.getLocalPort());
		 			try {
		 				data.send(packet);
		 				System.out.println("Sending restore to MC");
		 				m.rs.waitChunk(fileID, nChunk);
		 				System.out.println("Restore: Reiceved chunk" + nChunk);
		 				byte[] buff = m.rs.getData(fileID, nChunk);
		 				chunks.add(buff);
		 				m.rs.deleteChunk(fileID, nChunk);
		 				if(buff.length != BackupFile.maxSize) moreChunks = false;
		 			} catch (IOException e) {
		 				System.err.println("Error: Sendind Restore in MC");
		 			} catch (InterruptedException e){
		 				System.err.println("Error: Interrupted Expception");
		 			}
		 			nChunk++;
		 		}while (moreChunks);
		 		return chunks;
	}
	
	public void deleteFiles() throws IOException{
		//TODO apagar aqueles ficheiros no files
		while(m.maxSize < m.currSize){
			System.out.println("Current size "  + m.currSize);
			String fileID =  m.fileB.get(0);
			ReclaimProtocol rp = new ReclaimProtocol(vrs,id,fileID);
			byte[] buffer = rp.request();
			data.send(new DatagramPacket(buffer,buffer.length,address,data.getLocalPort()));
			if(rp.removeChunk(id + "/" +fileID)){
				m.deleteFile(fileID);
			}
			m.currSize-=rp.removedSize;
		}
		
	}
	
	public void deleteFileServerInfo(String fileID){
		DeleteProtocol dp = new DeleteProtocol(packet.getData(),packet.getLength(),id + "/" + fileID);
		m.currSize -= dp.sizeDeleted;
		if(m.currSize < 0)m.currSize = 0; 
		m.deleteFile(fileID);
	}
	
	public void run(){
//		readFile();
	}
}
