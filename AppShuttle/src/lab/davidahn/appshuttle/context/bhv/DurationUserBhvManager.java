package lab.davidahn.appshuttle.context.bhv;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.content.Context;

public class DurationUserBhvManager {
	private volatile List<UserBhv> bhvList;
	private UserBhvDao _userBhvDao;

	private Context _cxt;

	private static DurationUserBhvManager durationUserBhvManager;
	
	private DurationUserBhvManager(Context cxt) {
		_cxt = cxt;
		_userBhvDao = UserBhvDao.getInstance(cxt);
		bhvList = new CopyOnWriteArrayList<UserBhv>();
	}

	public synchronized static DurationUserBhvManager getInstance(Context cxt) {
		if (durationUserBhvManager == null) {
			durationUserBhvManager = new DurationUserBhvManager(cxt);
			durationUserBhvManager.syncBhvListFromPermanentStorage();
		}
		return durationUserBhvManager;
	}
	
	public List<UserBhv> getBhvList(){
		return bhvList;
	}
	
	public void registerBhv(UserBhv uBhv){
		if(bhvList.contains(uBhv))
			return ;

		UserBhvDao userBhvDao = UserBhvDao.getInstance(_cxt);
		userBhvDao.storeUserBhv(uBhv);

		bhvList.add(uBhv);
	}
	
	public void unregisterBhv(UserBhv uBhv){	
		UserBhvDao userBhvDao = UserBhvDao.getInstance(_cxt);
		userBhvDao.deleteUserBhv(uBhv);
		bhvList.remove(uBhv);
	}

	private void syncBhvListFromPermanentStorage() {
		UserBhvDao userBhvDao = UserBhvDao.getInstance(_cxt);
		bhvList = new CopyOnWriteArrayList<UserBhv>(userBhvDao.retrieveUserBhv());
	}
}