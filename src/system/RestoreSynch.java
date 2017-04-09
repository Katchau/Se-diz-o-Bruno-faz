package system;

import java.util.ArrayList;

public class RestoreSynch {
	public  ArrayList<ChunkInfo> restoredChunks;
	
	public RestoreSynch(){
		restoredChunks = new ArrayList<ChunkInfo>();
	}
	
	public synchronized boolean receivedChunk(String fileID, int n){
		ChunkInfo c = new ChunkInfo(fileID,n);
		for(ChunkInfo ci : restoredChunks){
			if(ci.equals(c)){
				notify();
				return true;
			}
		}
		notify();
		return false;
	}
	
	public synchronized void waitChunk(String fileID, int nChunk) throws InterruptedException{
		while(!receivedChunk(fileID, nChunk)){
			wait();
		}
		notify();
	}
	
	public synchronized void addChunk(String fileID, int n, byte[] data){
		restoredChunks.add(0,new ChunkInfo(fileID,n,data));
		notify();
	}
	
	public synchronized void deleteChunk(String fileID, int n){
		restoredChunks.remove(new ChunkInfo(fileID,n));
		notify();
	}
	
	public synchronized byte[] getData(String fileID, int n){
		ChunkInfo c = new ChunkInfo(fileID,n);
		for(ChunkInfo ci : restoredChunks){
			if(ci.equals(c)){
				notify();
				return ci.data;
			}
		}
		notify();
		return null;//should never happen
	}
}
