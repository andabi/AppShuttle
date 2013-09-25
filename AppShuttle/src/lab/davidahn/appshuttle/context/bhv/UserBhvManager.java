package lab.davidahn.appshuttle.context.bhv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.content.Context;

/**
 * 
 * @author andabi
 * @thread safe
 */

public class UserBhvManager {
	private List<UserBhv> bhvList;
	private Context cxt;

	private static UserBhvManager userBhvManager;
	
	private UserBhvManager(Context cxt) {
		this.cxt = cxt;
		bhvList = new ArrayList<UserBhv>();
	}

	public synchronized static UserBhvManager getInstance(Context cxt) {
		if (userBhvManager == null) {
			userBhvManager = new UserBhvManager(cxt);
			userBhvManager.syncBhvListFromPermanentStorage();
		}
		return userBhvManager;
	}
	
	public List<UserBhv> getBhvList(){
		return Collections.unmodifiableList(bhvList);
	}
	
	public synchronized void registerBhv(UserBhv uBhv){
		if(bhvList.contains(uBhv))
			return ;

		UserBhvDao userBhvDao = UserBhvDao.getInstance(cxt);
		userBhvDao.storeUserBhv(uBhv);

		bhvList.add(uBhv);
	}
	
	public synchronized void unregisterBhv(UserBhv uBhv){	
		UserBhvDao userBhvDao = UserBhvDao.getInstance(cxt);
		userBhvDao.deleteUserBhv(uBhv);
		bhvList.remove(uBhv);
	}

	private synchronized void syncBhvListFromPermanentStorage() {
		UserBhvDao userBhvDao = UserBhvDao.getInstance(cxt);
		bhvList = new CopyOnWriteArrayList<UserBhv>(userBhvDao.retrieveUserBhv());
	}
}