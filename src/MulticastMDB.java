import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;

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
				 if(bp.state == 0 && bp.version == vrs/*&& bp.id != id*/){//TODO remover isto quando ñ estiver em fase de testes
					try {
						sleep((long)bp.delay);
						if(!bp.storeChunk(id + "/" + m.getFolderIndex(bp.fileID))) return;
						bp.state = 1;
						bp.id = id;
						new MulticastMC(l,bp.storeAnswer());
					} catch (IOException e) {
						System.err.println("Error: Sending Store chunk");
					} catch (InterruptedException e) {
						System.err.println("Error: Sleep was interrupted 4some reason");
					}
					
				}
				//TODO para ter outras versões colocar aqi
		     }
		}).start();
	}
	
	public MulticastMDB(MulticastServer m, int repDegree) throws IOException{
		this.m = m;
		id = m.getId();
		address = m.getMDBaddress();
		vrs = m.getVersion();
		data = m.getMDBdata();
		
		this.repDegree = repDegree;
	}

	private boolean gotSaveChunk(int n) throws IOException{
		int time_out = 1000; //hardcoded mudar dp >:D
		MulticastSocket dataMC = new MulticastSocket(m.getMCdata().getLocalPort());
		dataMC.joinGroup(m.getMCaddress());
		boolean received = false;
		int nTries = 1;
		do{
			byte[] buffer = new byte[BackupFile.maxSize + 1000];
			DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
			dataMC.setSoTimeout(time_out);
			try {
				dataMC.receive(packet);
				received = true;
				System.out.println("Packet: " + new String(packet.getData()));
			} catch (SocketTimeoutException e) {
				System.out.println("Attempt nº " + nTries);
				nTries++;
				time_out*=2;
			}
			catch (IOException e) {
				System.err.println("Error: During reception of savechunk");
			}
		}while(!received && nTries < 6);
		dataMC.close();
		return received;
	}
	
	public void sendChunk(String fileID, int n, byte[] buffer, int size){
		new Thread(new Runnable() { //ñ sei se vale a pena ter isto como thread
		     public void run() {
		    	 BackupProtocol bp = new BackupProtocol(vrs,id,fileID,n,repDegree,buffer,size);
		    	 byte[] buff = bp.request();
		    	 DatagramPacket packet = new DatagramPacket(buff,buff.length, address, data.getLocalPort());
		    	 try {
					data.send(packet);
					System.out.println("Sent!");
					gotSaveChunk(n);
				} catch (IOException e) {
					System.err.println("Error: Fail sending chunk");
				}
		     }
		}).start();
	}
	
	public void run(){
	}
}
