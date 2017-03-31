import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Listener extends Thread {
	public String protocol;
	public MulticastServer m;
	public MulticastSocket data;
	public InetAddress address;
	public int id;
	public int vrs; //version xpto
	public String path = null;
	public int repDegree = 0;
	
	public Listener(String protocol, MulticastServer m) throws IOException{
		this.m = m;
		id = m.getId();
		vrs = m.getVersion();
		this.protocol = protocol;		
		getIPandPort();
		data.joinGroup(address);
	}
	
	public Listener(String protocol, MulticastServer m, String path, int repDegree) throws IOException{
		this.m = m;
		id = m.getId();
		vrs = m.getVersion();
		this.protocol = protocol;		
		getIPandPort();
		this.path = path;
		this.repDegree = repDegree;
		//TODO adicionar depois opções para o client
		testClient();
	}
	
	public void testClient(){
		try {
			new MulticastMDB(this,path,repDegree).start();
		} catch (IOException e) {
			System.err.println("Error: Starting client mdb");
		}
	}
	
	public void getIPandPort(){
		switch(protocol){
		case "MC":
			address = m.getMCaddress();
			data = m.getMCdata();
			break;
		case "MDB":
			address = m.getMDBaddress();
			data = m.getMDBdata();
			break;
		case "MDR":
			address = m.getMDRaddress();
			data = m.getMDRdata();
			break;
		}
	}
	
	public void run(){
		try{
	    	while(true){
	    		byte[] buff = new byte[BackupFile.maxSize + 1000]; //1kbytes pro header idk
	    		DatagramPacket packet = new DatagramPacket(buff,buff.length);
				System.out.println("receiving");
				data.receive(packet);
				switch(protocol){
					case "MC":
						new MulticastMC(this, packet);
						break;
					case "MDB":
						new MulticastMDB(this, packet);
						break;
					case "MDR":
//						new MulticastMDR();
						break;
				}
	    	}
		}catch (IOException e){
			System.err.println("Error: At listener " + protocol);
		}
	}
}
