package channels;
import protocols.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastMDB extends Thread{
	private MulticastSocket data;
	private InetAddress address;
	private int id;
	private int vrs; //version xpto
	private Listener l;
	private MulticastServer m;
	private int repDegree;
	private int curRepDegree = 0;
	private DatagramPacket packet;
	
	public MulticastMDB(Listener l, DatagramPacket packet) throws IOException{
		this.l = l;
		this.m = l.m;
		id = l.id;
		address = l.address;
		vrs = m.getVersion();
		data = l.data;
		this.packet = packet;
		createResponse();
	}
	
	private void createResponse(){
		new Thread(new Runnable() {
		     public void run() {
				BackupProtocol bp = new BackupProtocol(packet.getData(), packet.getLength());
                if(bp.state == 0 && bp.version == vrs && ( m.maxSize == -1 || m.maxSize >= bp.chunk.length + m.currSize)&& bp.id != id){
                	m.rs.addChunk(bp.fileID, bp.chunkN, null);
                	m.bs.addChunk(bp.fileID, bp.chunkN, bp.repDegree);
                	saveChunk(bp);
                	m.rs.deleteChunk(bp.fileID, bp.chunkN);
                }
				else{
					if(bp.state != 0)System.err.println("Error: Unrecognized Message received @MDB " + bp.subprotocol );
					if(m.maxSize < bp.chunk.length + m.currSize) System.err.println("Error: Max Sized reached!");
					m.rs.deleteChunk(bp.fileID, bp.chunkN);
				}
		     }
		}).start();
	}
	
	private void saveChunk(BackupProtocol bp){
		try {
			sleep((long)bp.delay);
			if(bp.repeatedChunk(id + "/" + bp.fileID)){
				System.err.println("This peer already has this chunk!");
				m.rs.deleteChunk(bp.fileID, bp.chunkN);
				return;
			}
			if(vrs == m.ENHANCEMENTS){
				System.out.println("Checking for other store messages ... ");
				int curRepDeg = m.bs.checkDesiredRepDegree(bp.fileID, bp.chunkN);
				if(curRepDeg > repDegree) {
					System.err.println("Replication degree already achieved.");
					return;
				}
				else{
					m.storeStored(bp.fileID, bp.chunkN, curRepDeg);
				}
			}
			bp.state = 1;
			bp.id = id;
			m.currSize+= bp.chunk.length;
			new MulticastMC(l,bp.storeAnswer());
			m.getFolderIndex(bp.fileID);
			if(!bp.storeChunk(id + "/" + bp.fileID)) {
				System.err.println("Error: Writting chunk @MDB");
				return;
			}
		} catch (IOException e) {
			System.err.println("Error: Sending Store chunk");
		} catch (InterruptedException e) {
			System.err.println("Error: Sleep was interrupted 4some reason");
		}
	}
	
	public MulticastMDB(MulticastServer m, int repDegree) throws IOException{
		this.m = m;
		id = m.getId();
		address = m.getMDBaddress();
		vrs = m.getVersion();
		data = m.getMDBdata();
		this.repDegree = repDegree;
	}
	
	private Thread timeOut(DatagramPacket packet, String fileID, int n, int repDegree){
		return new Thread(new Runnable() {
		     public void run() {
		    	int time_out = 1000;
		 		int nTries = 1;
		 		do{
		 			try {
		 				data.send(packet);
		 				System.out.println("Chunk Sent! try number " + nTries);
		 				sleep(time_out);
		 				if(curRepDegree >= repDegree)return;
		 				System.out.println("Attempt nº " + nTries);
		 				nTries++;
		 				time_out*=2;
		 			}catch (IOException e) {
		 				System.err.println("Error: Fail sending chunk");
		 			} catch (InterruptedException e) {
		 				//nothing at all
		 			}
		 		}while(nTries < 6);
		 		m.bs.addChunk(fileID, n, -1);
		     }
		});
	}
	
	public void sendChunk(String fileID, int n, byte[] buffer, int size){	
		BackupProtocol bp = new BackupProtocol(vrs,id,fileID,n,repDegree,buffer,size);
		byte[] buff = bp.request();
		DatagramPacket packet = new DatagramPacket(buff,buff.length, address, data.getLocalPort());
    	Thread t = timeOut(packet,fileID,n,repDegree);
    	t.start();
		for(int i = 0; i < repDegree;i++){
			try {
				m.bs.waitChunk(fileID, n,i+1);
				curRepDegree++;
				if(m.bs.invalidChunk(fileID, n)){
						System.err.println("Chunk " + n + " not received!");
						return;
					}
				else{
					System.out.println("Chunk " + n + " received for " + fileID);
				}
			} catch (InterruptedException e) {
				System.err.println("Error: Receiving STORE message");
			}
		}
	}
	
	public void run(){
	}
}
