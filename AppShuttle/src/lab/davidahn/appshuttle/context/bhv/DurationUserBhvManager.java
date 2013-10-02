package lab.davidahn.appshuttle.context.bhv;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import lab.davidahn.appshuttle.AppShuttleApplication;
import android.content.SharedPreferences;

import com.google.common.collect.EvictingQueue;

//TODO
public class DurationUserBhvManager {
	private DurationUserBhvDao _durationUserBhvDao;
	private EvictingQueue<DurationUserBhv> _cachedBhvQueue;
	private int _cacheSize;
	private Date _eldestCachedBhvDate;

	private SharedPreferences _preferenceSettings;

	private static DurationUserBhvManager durationUserBhvManager = new DurationUserBhvManager();
	private DurationUserBhvManager() {
//		preferenceSettings = cxt.getSharedPreferences(cxt.getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
		_preferenceSettings = AppShuttleApplication.getContext().getPreferenceSettings();

		_durationUserBhvDao = DurationUserBhvDao.getInstance();
		_cacheSize = _preferenceSettings.getInt("context.bhv.duration_user_bhv.cache_size", 100);
		_cachedBhvQueue = EvictingQueue.create(_cacheSize);
	}
	public static DurationUserBhvManager getInstance() {
		return durationUserBhvManager;
	}
	
	public synchronized void storeDurationBhv(DurationUserBhv uBhv) {
		_durationUserBhvDao.store(uBhv);
		_cachedBhvQueue.add(uBhv);
		_eldestCachedBhvDate = uBhv.getTimeDate();
	}

	public synchronized List<DurationUserBhv> retrieveDurationBhv(long fromTime, long toTime) {
		List<DurationUserBhv> res = new ArrayList<DurationUserBhv>();
		Iterator<DurationUserBhv> it = _cachedBhvQueue.iterator();
		while(it.hasNext()) {
			DurationUserBhv bhv = it.next();
		}
		
		return res;
	}
}