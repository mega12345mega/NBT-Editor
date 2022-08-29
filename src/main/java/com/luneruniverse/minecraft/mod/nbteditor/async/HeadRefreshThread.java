package com.luneruniverse.minecraft.mod.nbteditor.async;

import tsp.headdb.ported.HeadAPI;

public class HeadRefreshThread extends Thread {
	
	public HeadRefreshThread() {
		super("NBTEditor/Async/HeadRefresh/Auto");
		setDaemon(true);
	}
	
	@Override
	public void run() {
		while (true) {
			HeadAPI.updateDatabase();
			
			try {
				Thread.sleep(HeadAPI.getDatabase().getTimeUntilLastUpdateOld() * 1000);
			} catch (InterruptedException e) {
				return;
			}
		}
	}
	
}
