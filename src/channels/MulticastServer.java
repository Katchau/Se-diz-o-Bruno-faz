package channels;
import rmi.*;
import system.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
//import java.net.DatagramPacket;
//import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
//import java.util.HashMap;

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
	public static final String FSTORE = "files";
	public final int ENHANCEMENTS = 2;
	public long maxSize; // -1 equals unrestricted
	public long currSize;
	public ArrayList<String> fileB;
	public RestoreSynch rs;
	public BackupSynch bs;
	
	public MulticastServer(String args[]) throws IOException{
		rs = new RestoreSynch();
		bs = new BackupSynch();
		this.version = (int)Double.parseDouble(args[0]);
		maxSize = -1;
		currSize = 0;
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
		new Listener("MC",this).start();
		new Listener("MDR",this).start();
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
				new RMIServer(ms,peer_ap); 
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
		File folder = new File("" + id);
		fileB = new ArrayList<String>();
		if(!folder.exists()){
			folder.mkdir();
			new File(folder,FSTORE).mkdir();
			return;
		}
		for(File f : folder.listFiles()){
			if(!f.getName().equals(FSTORE)){
				fileB.add(f.getName());
				for(File ff : f.listFiles()){
					currSize += ff.length();
				}
			}
		}
		System.out.println("Space Occupied " + currSize + " bytes" );
	}
	
	public void storeNewFile(String fileID){
		fileB.add(fileID);
		new File(id + "/" + fileID).mkdir();
	}
	
	public void deleteFile(String fileID){
		fileB.remove(fileID);
	}
	
	public void deleteIDFile(String fileID){
		String folderPath = id + "/files";
		File folder = new File(folderPath);
		String value = "";
		File f = null;
		for(File file: folder.listFiles()){
			try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
				byte[] buffer = new byte[1024];
				int readValue = 0;
				while((readValue = bis.read(buffer)) > 0){
					value += new String(buffer,0,readValue);
				}
				String[] separate = value.split("\r\n"); //separar por new lines
				
				if(separate.length != 3){
					System.err.println("Error: Reading File");
					return;
				}
				
				if(separate[1].equals(fileID)){
					f = file;
					break;		
				}
				
			} catch (FileNotFoundException e) {
				System.err.println("Error, file not found: "+ e.getMessage());
			} catch (IOException e) {
				System.err.println("Error: "+ e.getMessage());
			}
		}
		
		if(f != null)f.delete();
	}
	
	public int getFolderIndex(String fileID){
		int ret = fileB.indexOf(fileID);
		if(ret == -1){
			ret = fileB.size();
			storeNewFile(fileID);
		}
		return ret;
	}
	
}
