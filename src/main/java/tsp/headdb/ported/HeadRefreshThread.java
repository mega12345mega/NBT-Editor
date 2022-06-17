package tsp.headdb.ported;

public class HeadRefreshThread extends Thread {
	
	public HeadRefreshThread() {
		super("Auto Head Refresh");
		setDaemon(true);
	}
	
	@Override
	public void run() {
		while (true) {
			if (HeadAPI.getDatabase().isLastUpdateOld())
				HeadAPI.updateDatabase();
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				return;
			}
		}
	}
	
}
