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
                if(bp.state == 0 && bp.version == vrs && ( m.maxSize == -1 || m.maxSize >= bp.chunk.length + m.currSize)/*&& bp.id != id*/){//TODO remover isto quando ñ estiver em fase de testes
                	saveChunk(bp);
                }
				else{
					if(bp.state != 0)System.err.println("Error: Unrecognized Message received @MDB " + bp.subprotocol );
					if(m.maxSize < bp.chunk.length + m.currSize) System.err.println("Error: Max Sized reached!");
				}
		     }
		}).start();
	}
	
	private void saveChunk(BackupProtocol bp){
		try {
			sleep((long)bp.delay);
			if(bp.repeatedChunk(id + "/" + bp.fileID)){
				System.err.println("This peer already has this chunk!");
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
	
	private Thread timeOut(DatagramPacket packet, String fileID, int n){
		return new Thread(new Runnable() {
		     public void run() {
		    	int time_out = 1000;
		 		int nTries = 1;
		 		do{
		 			try {
		 				data.send(packet);
		 				System.out.println("Chunk Sent! try number " + nTries);
		 				sleep(time_out);
		 				if(m.bs.receivedChunk(fileID, n))return;
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
		for(int i = 0; i < repDegree;i++){
			try {
		    	Thread t = timeOut(packet,fileID,n);
		    	t.start();
				m.bs.waitChunk(fileID, n);
				if(m.bs.invalidChunk(fileID, n) || m.bs.checkDesiredRepDegree(fileID,n) != i+1)
					System.out.println("Chunk " + n + " not received!");
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
