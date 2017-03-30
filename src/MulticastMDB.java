import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.util.Arrays;

public class MulticastMDB extends Thread{
	private MulticastSocket data;
	private InetAddress address;
	private int id;
	private int vrs; //version xpto
	private Listener l;
	private MulticastServer m;
	private String path = null;
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
						bp.storeChunk("" + m.getFolderIndex(bp.fileID));
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
	
	public MulticastMDB(Listener l, String path, int repDegree) throws IOException{
		this.l = l;
		this.m = l.m;
		id = l.id;
		address = l.address;
		vrs = m.getVersion();
		data = l.data;
		
		this.path = path;
		this.repDegree = repDegree;
	}

	private String createHash(File file){
		try {
  		 	 MessageDigest md = MessageDigest.getInstance("SHA-256");
	    	 md.update(path.getBytes("UTF-16"));
	    	 md.update(Integer.toString((int)file.length()).getBytes());
	    	 md.update(Integer.toString((int)file.lastModified()).getBytes());
	    	 return new String(md.digest());
		} catch (Exception e1) {
			System.err.println("this shouldn't happen");
			return "";
		}
	}
	
	private void readFile(){
		int partCounter = 0;
		byte[] buffer = new byte[BackupFile.maxSize];
		File file = new File(path);
		if(!file.exists()){
			System.out.println("Oh meu ganda burro, " + path + " não existe!");
			return;
		}
		String hashname = createHash(file);
		if(hashname.equals(""))return;
		
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
			int readValue = 0;
			while ((readValue = bis.read(buffer)) > 0) {
				byte[] message = Arrays.copyOfRange(buffer, 0, readValue);
				for(int i = 0; i < repDegree; i++)
					sendChunk(hashname,++partCounter, message, readValue); //TODO falta por o timer
			}
			if(file.length()%BackupFile.maxSize == 0){
				for(int i = 0; i < repDegree; i++)
					sendChunk(hashname,++partCounter, "".getBytes(),0);
			}
		}catch(IOException e){
			System.err.println("Ups i did it again");
		}
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
					//TODO invocar um novo mc ride pra ouvir a resposta <(*.*<)
				} catch (IOException e) {
					System.err.println("Error: Fail sending chunk");
				}
		     }
		}).start();
	}
	
	public void run(){
		readFile();
	}
}
