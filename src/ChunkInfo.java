
public class ChunkInfo {
	public String fileID;
	public int n;
	public byte[] data;
	public int repDeg;
	public int curRepDeg;
	
	public ChunkInfo(String f, int n){
		fileID = f;
		this.n = n;
		data = null;
		repDeg = 1;
		curRepDeg = 1;
	}
	
	public ChunkInfo(String f, int n, byte[] data){
		fileID = f;
		this.n = n;
		this.data = data;
		repDeg = 1;
		curRepDeg = 1;
	}
	
	public ChunkInfo(String f, int n, int repDeg){
		fileID = f;
		this.n = n;
		this.repDeg = repDeg;
		curRepDeg = 1;
	}
	
	public boolean equals(ChunkInfo c){
		return (c.fileID.equals(fileID) && n == c.n);
	}
}
