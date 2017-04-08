
public class ChunkInfo {
	public String fileID;
	public int n;
	public byte[] data;
	
	public ChunkInfo(String f, int n){
		fileID = f;
		this.n = n;
		data = null;
	}
	
	public ChunkInfo(String f, int n, byte[] data){
		fileID = f;
		this.n = n;
		this.data = data;
	}
	
	public boolean equals(ChunkInfo c){
		return (c.fileID.equals(fileID) && n == c.n);
	}
}
