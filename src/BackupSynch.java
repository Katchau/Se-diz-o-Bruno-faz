
import java.util.ArrayList;

public class BackupSynch {
	public  ArrayList<ChunkInfo> backupChunks;
	
	public BackupSynch(){
		backupChunks = new ArrayList<ChunkInfo>();
	}
	
	public synchronized boolean receivedChunk(String fileID, int n){
		ChunkInfo c = new ChunkInfo(fileID,n);
		for(ChunkInfo ci : backupChunks){
			if(ci.equals(c)){
				notify();
				return true;
			}
		}
		notify();
		return false;
	}
	
	public synchronized int checkDesiredRepDegree(String fileID, int n){
		ChunkInfo c = new ChunkInfo(fileID,n);
		for(ChunkInfo ci : backupChunks){
			if(ci.equals(c)){
				notify();
				return ci.curRepDeg;
			}
		}
		notify();
		return 0;
	}
	
	public synchronized boolean waitChunk2(String fileID, int nChunk, int timeout) throws InterruptedException{
		while(!receivedChunk(fileID, nChunk)){
			wait(timeout);
		}
		notify();
		return receivedChunk(fileID,nChunk);
	}
	
	public synchronized void waitChunk(String fileID, int nChunk) throws InterruptedException{
		while(!receivedChunk(fileID, nChunk)){
			wait();
		}
		notify();
	}
	
	public synchronized void addChunk(String fileID, int n,int repDegree){
		backupChunks.add(0,new ChunkInfo(fileID,n,repDegree));
		notify();
	}
	
	public synchronized void addChunk2(String fileID, int n,int repDegree){
		ChunkInfo cf = new ChunkInfo(fileID,n,repDegree);
		for(ChunkInfo ci : backupChunks){
			if(ci.equals(cf)){
				ci.curRepDeg++;
				notify();
				return;
			}
		}
		backupChunks.add(0,cf);
		notify();
	}
	
	public synchronized void deleteChunk(String fileID, int n){
		backupChunks.remove(new ChunkInfo(fileID,n));
		notify();
	}
	
	public synchronized byte[] getData(String fileID, int n){
		ChunkInfo c = new ChunkInfo(fileID,n);
		for(ChunkInfo ci : backupChunks){
			if(ci.equals(c)){
				notify();
				return ci.data;
			}
		}
		notify();
		return null;//should never happen
	}
}

