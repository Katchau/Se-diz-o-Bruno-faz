import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.security.MessageDigest;

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
		
		new Thread(new Runnable() {
		     public void run() {
		    	 String request = new String(packet.getData(), 0, packet.getLength());
				 BackupProtocol bp = new BackupProtocol(request);
				 if(bp.state == 0 /*&& bp.id != id*/){//TODO remover isto quando ñ estiver em fase de testes
					bp.storeChunk("" + m.getFolderIndex(bp.fileID));
					bp.state = 1;
					System.out.println(bp.storeAnswer());
					//new MulticastMC(m).start();
				}
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

	
	private void readFile(){
		int partCounter = 0;
		byte[] buffer = new byte[BackupFile.maxSize];
		File file = new File(path);
		if(!file.exists()){
			System.out.println("Oh meu ganda burro, " + path + " não existe!");
			return;
		}
		String hashname = "Testezinho";
		try {
   		 	 MessageDigest md = MessageDigest.getInstance("SHA-256");
	    	 md.update(path.getBytes("UTF-16"));
	    	 md.update(Integer.toString((int)file.length()).getBytes());
	    	 md.update(Integer.toString((int)file.lastModified()).getBytes());
	    	 hashname = new String(md.digest());
		} catch (Exception e1) {
			System.err.println("this shouldn't happen");
		}
		
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
			int readValue = 0;
			while ((readValue = bis.read(buffer)) > 0) {
				for(int i = 0; i < repDegree; i++)
					sendChunk(hashname,++partCounter, buffer, readValue); //TODO falta por o timer
			}
			if(file.length()%BackupFile.maxSize == 0){
				for(int i = 0; i < repDegree; i++)
					sendChunk(hashname,++partCounter, "".getBytes(),0);
			}
		}catch(IOException e){
			System.err.println("Ups i did it again");
		}
	}
	
	public void sendChunk(String fileID, int n, byte[] buffer, int size){
		new Thread(new Runnable() { //ñ sei se vale a pena ter isto como thread
		     public void run() {
		    	 BackupProtocol bp = new BackupProtocol(vrs,id,fileID,n,repDegree,buffer,size);
		    	 byte[] buff = bp.request().getBytes();
		    	 DatagramPacket packet = new DatagramPacket(buff,buff.length, address, data.getLocalPort());
		    	 try {
					data.send(packet);
					System.out.println("Sent!");
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
