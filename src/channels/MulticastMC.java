package channels;
import protocols.*;
import system.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Random;

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
		DatagramPacket packet = new DatagramPacket(buffer,buffer.length,address,data.getLocalPort());
		if(protocol.equals(DeleteProtocol.msgDelete)){
				if(vrs != m.ENHANCEMENTS){
						System.out.println("Sending Delete");
						data.send(packet);
					}
				else{
					sendDeleteSpammer(packet,fileID);
				}
			}
				
		if(protocol.equals(ReclaimProtocol.msgRemoved))
			deleteFiles();
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
				 if(p.id == id || p.version != vrs) return;
				 int indice = m.fileB.indexOf(p.fileID);
				 switch(p.subprotocol){
				 	case BackupProtocol.msgTypeStored:
				 		int curRepDeg = m.bs.addChunk2(p.fileID, p.chunkN, 1);
				 		if(vrs != m.ENHANCEMENTS)m.storeStored(p.fileID, p.chunkN, curRepDeg);
				 		else {
				 			BackupProtocol  bp= new BackupProtocol(packet.getData(), packet.getLength());
				 			if(bp.repeatedChunk(id + "/" + p.fileID))
				 				m.storeStored(p.fileID, p.chunkN, curRepDeg);
				 		} 			
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
				 		if(indice != -1){
				 			RestoreProtocol rp = new RestoreProtocol(packet.getData(),packet.getLength());
				 			reclaimFileRestore(rp);
				 		}
				 		break;
				 	default:
				 		System.err.println("Error: Unrecognized Message received @MC " + p.subprotocol );
				 		return;
				 }
		     }
		}).start();
	}
	
	public void reclaimFileRestore(RestoreProtocol rp){
		int curRepDeg = m.bs.checkDesiredRepDegree(rp.fileID, rp.chunkN) - 1;//since 1 was deleted
		int desired = m.bs.checkRepDegree(rp.fileID, rp.chunkN);
		rp.repDegree = desired;
		rp.id = id;
		rp.readChunk(id + "/" + rp.fileID);
		byte[] buff = rp.request2();
		if(curRepDeg < desired){
			Random r = new Random();
			try {
				sleep(r.nextInt(BackupProtocol.MAXDELAY));
				if(!m.rs.receivedChunk(rp.fileID, rp.chunkN)){
					System.out.println("Sending PUTCHUNK");
					new MulticastMDB(m,1).sendChunk(rp.fileID, rp.chunkN, buff, buff.length);
				}
			} catch (InterruptedException e) {
				System.err.println("Error: interrupted delay at Reclaim function");
			} catch (IOException e) {
				System.err.println("Error: Sending PUTCHUNK");
			}
		}
		
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

	public void sendDeleteSpammer(DatagramPacket packet,String fileID){
		new Thread(new Runnable() {
		     public void run() {
		    	 long endTime = System.currentTimeMillis() + 3600000;//3 600 000 = 2h in milliseconds
		    	 do{
		    		 try {
		    			 System.out.println("Sending Delete");
						data.send(packet);
						sleep(60000); //send delete every minute
						if(m.bs.checkFileID(fileID))return;
		    		 } catch (IOException | InterruptedException e) {
		    			 System.out.println("Error: Sending Delete spammer");
		    		 }
		    		 
		    	 }while(endTime > System.currentTimeMillis());
		     }
		}).start();
	}
	
	public void deleteFiles() throws IOException{
		while(m.maxSize < m.currSize){
			System.out.println("Current size "  + m.currSize);
			String fileID =  m.fileB.get(0);
			ReclaimProtocol rp = new ReclaimProtocol(vrs,id,fileID);
			if(rp.removeChunk(id + "/" +fileID)){
				m.deleteFile(fileID);
				m.deleteIDFile(fileID);
			}
			byte[] buffer = rp.request();
			m.storeStored(fileID, rp.chunkN, m.bs.checkDesiredRepDegree(fileID, rp.chunkN)-1);
			m.bs.decreaseChunk(fileID, rp.chunkN);
			m.currSize-=rp.removedSize;
			System.out.println("Sending Remove");
			data.send(new DatagramPacket(buffer,buffer.length,address,data.getLocalPort()));
		}
		
	}
	
	public void deleteFileServerInfo(String fileID){
		DeleteProtocol dp = new DeleteProtocol(packet.getData(),packet.getLength(),id + "/" + fileID);
		m.currSize -= dp.sizeDeleted;
		if(m.currSize < 0)m.currSize = 0; 
		m.deleteFile(fileID);
		m.bs.deleteAllChunks(fileID);
		m.deleteStored(fileID);
		System.out.println(fileID + " was deleted successfully!");
	}
	
	public void run(){
//		readFile();
	}
}
