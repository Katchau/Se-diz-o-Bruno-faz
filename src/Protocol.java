import java.util.Arrays;

public class Protocol {
	public int version;
	public int id;
	public String fileID;
	public int chunkN;
	public int state; //-1 = fail 0 received 1 sending 2 last
	public String subprotocol;
	public int repDegree;
	protected String header;
	protected byte[] body;
	
	public Protocol(byte[] message, int messageLength){
		int i;
		for(i = 3; i < message.length; i++){
			if(message[i-3]==0xD && message[i-2]==0xA && message[i-1]==0xD && message[i]==0xA)
				break;
		}
		byte[] header = Arrays.copyOfRange(message, 0, i-3);
		byte[] body = Arrays.copyOfRange(message, i+1, messageLength);
		
		if(header.equals(message)){
			state = -1;
			System.out.println("Error: Message damaged!");
			System.out.println("Message received: " + message);
			return;
		}
		
		String headerString = new String(header);
		getHeader(headerString);
		this.header = headerString;
		if(body != null){
			this.body = body;
		}
	}
	
	public Protocol(int version,int senderID, String fileID,int chunkN, int repDegree){
		this.version = version;
		this.id = senderID;
		this.fileID = fileID;
		this.chunkN = chunkN;
		this.repDegree = repDegree;
	}
	
	public Protocol(int version,int senderID, String fileID){
		this.version = version;
		this.id = senderID;
		this.fileID = fileID;
	}
	
	public void getHeader(String message){
		String[] parts = message.split(" ");
		subprotocol = parts[0];
		version = Integer.parseInt(parts[1]);
		id = Integer.parseInt(parts[2]);
		fileID = parts[3];
		if(parts.length == 5)
			chunkN = Integer.parseInt(parts[4]);
		if(parts.length == 6) repDegree = Integer.parseInt(parts[5]); //TODO ISTO
	}
	
	public String answer(String subprotocol){
		return subprotocol + " " + version + " " + id + " " + fileID + " " + MulticastServer.CRLF + MulticastServer.CRLF;
	}
	
	//TODO mudar este se for preciso
	public String request(String subprotocol){
		return subprotocol + " " + version + " " + id + " " + fileID + " " + chunkN + " " + repDegree + " " + MulticastServer.CRLF + MulticastServer.CRLF;
	}
}
