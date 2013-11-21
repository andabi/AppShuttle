package lab.davidahn.appshuttle.context.env;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DurationUserEnvManager {
	private DurationUserEnvDao durationUserEnvDao;

	private static DurationUserEnvManager durationUserBhvManager = new DurationUserEnvManager();
	private DurationUserEnvManager() {
		durationUserEnvDao = DurationUserEnvDao.getInstance();
	}
	public static DurationUserEnvManager getInstance() {
		return durationUserBhvManager;
	}
	
	public void store(DurationUserEnv durationUserEnv) {
		durationUserEnvDao.store(durationUserEnv);
	}
	
//	public List<DurationUserEnv> retrieve(Date beginTimeDate, Date endTimeDate, EnvType envType){
//		return durationUserEnvDao.retrieveBetweenByEnv(beginTimeDate, endTimeDate, envType);
//	}
	
	public List<DurationUserEnv> retrieve(Date beginTimeDate, Date endTimeDate, EnvType envType){
		List<DurationUserEnv> res = new ArrayList<DurationUserEnv>();
		
		DurationUserEnv uEnv = durationUserEnvDao.retrieveContains(beginTimeDate, endTimeDate, envType);
		if(uEnv != null){
			res.add(uEnv);
		} else {
			DurationUserEnv tempFirst = durationUserEnvDao.retrieveContains(beginTimeDate, envType);
			if(tempFirst != null)
				res.add(tempFirst);
			
			List<DurationUserEnv> tempMiddles = durationUserEnvDao.retrieveBetween(beginTimeDate, endTimeDate, envType);
			res.addAll(tempMiddles);
	
			DurationUserEnv tempLast = durationUserEnvDao.retrieveContains(endTimeDate, envType);
			if(tempLast != null){
				if(res.isEmpty()) {
					res.add(tempLast);
				} else {
					DurationUserEnv last = res.get(res.size()-1);
					if(!last.equals(tempLast))
						res.add(tempLast);
				}
			}
			
			if(res.isEmpty()){
				return Collections.emptyList();
			}
		}
			
		DurationUserEnv first = res.get(0);
		first.setTime(beginTimeDate);
		
		DurationUserEnv last = res.get(res.size()-1);
		last.setEndTime(endTimeDate);
		
		return res;
	}
	
	public void deleteAllBefore(Date timeDate){
		durationUserEnvDao.deleteBefore(timeDate);
	}
	
	public void deleteAllBetween(Date beginTime, Date endTime){
		durationUserEnvDao.deleteBetween(beginTime, endTime);
	}
}