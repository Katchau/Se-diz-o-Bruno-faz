
public class Protocol {
	public int version;
	public int id;
	public String fileID;
	public int chunkN;
	public int state; //-1 = fail 0 received 1 sending 2 last
	public String subprotocol;
	public int repDegree;
	protected String header;
	protected String body;
	
	public Protocol(String message){
		String[] headerbody = message.split(MulticastServer.CRLF + MulticastServer.CRLF);
		if(headerbody[0].equals(message)){
			state = -1;
			System.out.println("Error: Message damaged!");
			System.out.println("Message received: " + message);
			return;
		}
		System.out.println(headerbody[0]);
		getHeader(headerbody[0]);
		header = headerbody[0];
		if(headerbody.length == 2){
			body = headerbody[1];
		}
	}
	
	public Protocol(int version,int senderID, String fileID,int chunkN, int repDegree){
		this.version = version;
		this.id = senderID;
		this.fileID = fileID;
		this.chunkN = chunkN;
		this.repDegree = repDegree;
	}
	
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
