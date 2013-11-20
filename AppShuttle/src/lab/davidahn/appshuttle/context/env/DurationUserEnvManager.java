package lab.davidahn.appshuttle.context.env;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

//TODO cache 구현
public class DurationUserEnvManager {
	private DurationUserEnvDao _durationUserEnvDao;

//	private SharedPreferences _preferenceSettings;

	private static DurationUserEnvManager durationUserBhvManager = new DurationUserEnvManager();
	private DurationUserEnvManager() {
//		_preferenceSettings = AppShuttleApplication.getContext().getPreferenceSettings();

		_durationUserEnvDao = DurationUserEnvDao.getInstance();
	}
	public static DurationUserEnvManager getInstance() {
		return durationUserBhvManager;
	}
	
//	public synchronized void store(DurationUserEnv uEnv) {
//		_durationUserEnvDao.store(uEnv);
//	}
//
//	public synchronized List<DurationUserBhv> retrieve(long beginTime, long endTime) {
//		List<DurationUserBhv> res = new ArrayList<DurationUserBhv>();
//		
//		return res;
//	}
	
	public void store(DurationUserEnv durationUserEnv) {
		_durationUserEnvDao.store(durationUserEnv);
	}
	
	public List<DurationUserEnv> retrieve(Date beginTimeDate, Date endTimeDate, EnvType envType){
		return _durationUserEnvDao.retrieveBetweenByEnv(beginTimeDate, endTimeDate, envType);
	}
	
	public List<DurationUserEnv> retrieveExactly(Date beginTimeDate, Date endTimeDate, EnvType envType){
		List<DurationUserEnv> res = new ArrayList<DurationUserEnv>();
		
		DurationUserEnv tempFirst = _durationUserEnvDao.retrieveContainsByEnv(beginTimeDate, envType);
		if(tempFirst != null)
			res.add(tempFirst);
		
		List<DurationUserEnv> tempMiddles = _durationUserEnvDao.retrieveBetweenByEnv(beginTimeDate, endTimeDate, envType);
		res.addAll(tempMiddles);

		DurationUserEnv tempLast = _durationUserEnvDao.retrieveContainsByEnv(endTimeDate, envType);
		if(tempLast != null){
			if(res.isEmpty()) {
				res.add(tempLast);
			} else {
				DurationUserEnv last = res.get(res.size()-1);
				if(!last.equals(tempLast))
					res.add(tempLast);
			}
		}
		
		if(res.isEmpty())
			return Collections.emptyList();
		
		DurationUserEnv first = res.get(0);
		first.setTime(beginTimeDate);
		
		DurationUserEnv last = res.get(res.size()-1);
		last.setEndTime(endTimeDate);
		
		return res;
	}
	
	public void deleteAllBefore(Date timeDate){
		_durationUserEnvDao.deleteBefore(timeDate);
	}
	
	public void deleteAllBetween(Date beginTime, Date endTime){
		_durationUserEnvDao.deleteBetween(beginTime, endTime);
	}
}