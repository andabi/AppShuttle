package lab.davidahn.appshuttle.context.bhv;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;

public class UserBhvManager {
	private Set<UserBhv> bhvList = new HashSet<UserBhv>();
	private Context cxt;

	private static UserBhvManager userBhvManager;
	
	private UserBhvManager(Context cxt) {
		this.cxt = cxt;
	}

	public static UserBhvManager getInstance(Context cxt) {
		if (userBhvManager == null) {
			userBhvManager = new UserBhvManager(cxt);
			userBhvManager.syncBhvListFromPermanentStorage();
		}
		return userBhvManager;
	}
	
	public Set<UserBhv> getBhvList(){
		return bhvList;
	}
	
	public void registerBhv(UserBhv uBhv){
		if(bhvList.contains(uBhv))
			return ;

		UserBhvDao userBhvDao = UserBhvDao.getInstance(cxt);
		if(uBhv.getBhvType() == BhvType.NONE) {
			;
		} else {
			if(uBhv.getBhvType() == BhvType.APP) {
				if(((AppUserBhv)uBhv).isValid(cxt))
						userBhvDao.storeUserBhv(uBhv);
			} else {
				userBhvDao.storeUserBhv(uBhv);
			}
		}

		bhvList.add(uBhv);
	}
	
	public void unregisterBhv(UserBhv uBhv){	
		UserBhvDao userBhvDao = UserBhvDao.getInstance(cxt);
		userBhvDao.deleteUserBhv(uBhv);
		
		bhvList.remove(uBhv);
	}

	private void syncBhvListFromPermanentStorage() {
		UserBhvDao userBhvDao = UserBhvDao.getInstance(cxt);
		bhvList = userBhvDao.retrieveUserBhv();
	}
}