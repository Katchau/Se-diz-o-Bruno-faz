import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
//import java.net.DatagramPacket;
//import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
//import java.util.HashMap;
import java.util.Arrays;

public class MulticastServer{
	private MulticastSocket MCdata;
	private InetAddress MCaddress;
	private MulticastSocket MDBdata;
	private InetAddress MDBaddress;
	private MulticastSocket MDRdata;
	private InetAddress MDRaddress;
	private int version;
	private int id;
	private int peer_ap;
	public static final String CRLF = "\r\n";
	public ArrayList<String> fileB;
	
	public MulticastServer(String args[]) throws IOException{
		this.version = (int)Double.parseDouble(args[0]);
		id = Integer.parseInt(args[1]);
		peer_ap = Integer.parseInt(args[2]);
		MCdata = new MulticastSocket(Integer.parseInt(args[4]));
		MCaddress = InetAddress.getByName(args[3]);
		MDBdata = new MulticastSocket(Integer.parseInt(args[6]));
		MDBaddress = InetAddress.getByName(args[5]);
		MDRdata = new MulticastSocket(Integer.parseInt(args[8]));
		MDRaddress = InetAddress.getByName(args[7]);
		loadFileStorage();
		RMIServer(this);
		new Listener("MDB",this).start();
	}
	
	public void RMIServer(MulticastServer ms){
		new Thread(new Runnable(){
			public void run(){
				try {
				    java.rmi.registry.LocateRegistry.createRegistry(peer_ap);
				    System.out.println("RMI registry ready.");
				} catch (Exception e) {
				    System.out.println("Exception starting RMI registry:");
				    e.printStackTrace();
				}
				new RMIServer(ms); 
			}
		}).start();
	}
	
	public int getVersion() {
		return version;
	}

	public int getId() {
		return id;
	}

	
	public MulticastSocket getMCdata() {
		return MCdata;
	}

	public InetAddress getMCaddress() {
		return MCaddress;
	}

	public MulticastSocket getMDBdata() {
		return MDBdata;
	}

	public InetAddress getMDBaddress() {
		return MDBaddress;
	}

	public MulticastSocket getMDRdata() {
		return MDRdata;
	}

	public InetAddress getMDRaddress() {
		return MDRaddress;
	}

	public void loadFileStorage(){
		File f = new File("backup.txt");
		String files = "";
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f))) {
			byte[] buffer = new byte[1024];
			int readValue = 0;
			while((readValue = bis.read(buffer)) > 0){
				files += new String(buffer,0,readValue);
			}
			String[] separate = files.split("\r\n"); //separar por new lines
			fileB = new ArrayList<String>(Arrays.asList(separate));
		}
		catch(IOException e){
			System.err.println("Error: No backup file!");
			fileB = new ArrayList<String>();
			//TODO criar ficheiro backup??
		}
	}
	
	public void storeNewFile(String fileID){
		String fName = Integer.toString(fileB.size());
		fileB.add(fileID);
		new File(fName).mkdir();
		File f = new File("backup.txt");
		try (FileOutputStream out = new FileOutputStream(f,true)) {
			byte[] buffer = ("\r\n" + fileID).getBytes();
			out.write(buffer, 0, buffer.length);
		}
		catch(IOException e){
			System.err.println("Error: Saving metadata");
		}
	}
	
	public int getFolderIndex(String fileID){
		int ret = fileB.indexOf(fileID);
		if(ret == -1){
			ret = fileB.size();
			storeNewFile(fileID);
		}
		return ret;
	}
	
//	public void clientTest() throws IOException{
//	      MulticastSocket umo = new MulticastSocket(data.getLocalPort());
//	      
//	      byte[] buff = new byte[256];
//	      String antes = "lel";
//	      buff = antes.getBytes();
//	      
//		  DatagramPacket pacote = new DatagramPacket(buff,buff.length, address, umo.getLocalPort());
//	      
//	      System.out.println("sending...");
//	      umo.send(pacote);
//	      
//	      System.out.println("kek");
//	      
//	      
//	}
	
}
