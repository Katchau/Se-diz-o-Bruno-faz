
public class Protocol {
	public int version;
	public int id;
	public String fileID;
	public int chunkN;
	public String subprotocol;
	public int repDegree;
	
	public void getHeader(String message){
		String[] parts = message.split(" ");
		
		subprotocol = parts[0];
		version = Integer.parseInt(parts[1]);
		id = Integer.parseInt(parts[2]);
		fileID = parts[3];
		chunkN = Integer.parseInt(parts[4]);
		if(parts.length == 6) repDegree = Integer.parseInt(parts[5]);
	}
	
	public String answer(String subprotocol){
		return subprotocol + " " + version + " " + id + " " + fileID + " " + chunkN + " " + MulticastServer.CRLF + MulticastServer.CRLF;
	}
	
	public String request(String subprotocol){
		return subprotocol + " " + version + " " + id + " " + fileID + " " + chunkN + " " + repDegree + " " + MulticastServer.CRLF + MulticastServer.CRLF;
	}
}
