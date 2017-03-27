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
	private MulticastSocket data;
	private int version;
	private int id;
	private InetAddress address;
	public static final String CRLF = "\r\n";
	public ArrayList<String> fileB;
	
	public MulticastSocket getData() {
		return data;
	}

	public int getVersion() {
		return version;
	}

	public int getId() {
		return id;
	}

	public InetAddress getAddress() {
		return address;
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
			out.write(buffer, 0, buffer.length);//tmp is chunk size
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
	
	public MulticastServer(String args[]) throws IOException{
		this.version = (int)Double.parseDouble(args[0]);
		id = Integer.parseInt(args[1]);
		data = new MulticastSocket(Integer.parseInt(args[4]));
		address = InetAddress.getByName(args[3]);
		loadFileStorage();
		new MulticastMDB(this).start();
	}
	
	public void clientTest() throws IOException{
		new MulticastMDB(this,"cebas.txt",1).start();
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
