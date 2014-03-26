package lab.davidahn.appshuttle.predict.datasource;

import java.util.Date;
import java.util.List;

import android.app.AlarmManager;

/**
 * This makes an abstracted data table for training
 * @author carrot
 *
 */
public abstract class DataSource<T> {
	public long retrievedTime;
	
	/**
	 * This should be called after refreshing Data
	 */
	protected void updateRetrivedTime(Date t){
		if (t == null) 
			retrievedTime = System.currentTimeMillis();
		else
			retrievedTime = t.getTime();
	}
	
	public void refreshData() {
		// Do nothing
	}
	
	//public abstract int getNumberOfHistory();
	/* XXX: Map? Tuple?
	public abstract Tuple<?, UserBhv> getNext();
	public abstract Tuple<?, UserBhv> getAll();
	*/
	
	public List<T> getList() {
		return getList(AlarmManager.INTERVAL_DAY * 14);
	}
	public List<T> getList(long duration) {
		long curTime = System.currentTimeMillis();
		return getList(curTime - duration, curTime);
	}
	public abstract List<T> getList(long fromTime, long untilTime);
	
	/*
	public abstract Map<String, ?> getNext();
	public abstract List<Map<String, ?>> getList();
	*/
}
