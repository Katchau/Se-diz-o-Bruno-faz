package system;


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
				if(ci.curRepDeg == 0){
					notify();
					return false;
				}
				notify();
				return true;
			}
		}
		notify();
		return false;
	}
	
	public synchronized boolean checkFileID(String fileID){
		for(ChunkInfo ci : backupChunks){
			if(ci.fileID.equals(fileID)){
				notify();
				return true;
			}
		}
		notify();
		return false;
	}
	
	public synchronized boolean receivedChunk(String fileID, int n,int repDegree){
		ChunkInfo c = new ChunkInfo(fileID,n);
		for(ChunkInfo ci : backupChunks){
			if(ci.equals(c)){
				if(ci.curRepDeg != repDegree){
					notify();
					return false;
				}
				notify();
				return true;
			}
		}
		notify();
		return false;
	}
	
	public synchronized boolean invalidChunk(String fileID, int n){
		ChunkInfo c = new ChunkInfo(fileID,n);
		for(ChunkInfo ci : backupChunks){
			if(ci.equals(c)){
				notify();
				return ci.repDeg == -1;
			}
		}
		notify();
		return true;
	}
	
	public synchronized int checkRepDegree(String fileID, int n){
		ChunkInfo c = new ChunkInfo(fileID,n);
		for(ChunkInfo ci : backupChunks){
			if(ci.equals(c)){
				notify();
				return ci.repDeg;
			}
		}
		notify();
		return 0;
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
	
	public synchronized void waitChunk(String fileID, int nChunk, int rep) throws InterruptedException{
		while(!receivedChunk(fileID, nChunk, rep)){
			wait();
		}
		notify();
	}
	
	public synchronized void addChunk(String fileID, int n,int repDegree){
		if(receivedChunk(fileID,n)){
			notify();
			return;
		}
		backupChunks.add(0,new ChunkInfo(fileID,n,repDegree,0));
		notify();
	}
	
	public synchronized int addChunk2(String fileID, int n,int repDegree){
		ChunkInfo cf = new ChunkInfo(fileID,n,repDegree);
		for(ChunkInfo ci : backupChunks){
			if(ci.equals(cf)){
				ci.curRepDeg++;
				notify();
				return ci.curRepDeg;
			}
		}
		backupChunks.add(0,cf);
		notify();
		return 1;
	}
	
	public synchronized void deleteAllChunks(String fileID){
		for(ChunkInfo ci : backupChunks){
			if(ci.fileID.equals(fileID))
				backupChunks.remove(ci);
		}
		notify();
	}
	
	public synchronized void decreaseChunk(String fileID, int n){
		ChunkInfo cf = new ChunkInfo(fileID,n);
		for(ChunkInfo ci : backupChunks){
			if(ci.equals(cf)){
				ci.curRepDeg--;
//				if(ci.curRepDeg == 0) backupChunks.remove(ci);
				notify();
				return;
			}
		}
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

