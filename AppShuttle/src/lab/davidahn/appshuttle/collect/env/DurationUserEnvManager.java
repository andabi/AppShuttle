package lab.davidahn.appshuttle.collect.env;

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
			addNewDurationUserEnv(res, uEnv);
//			res.add(uEnv);
		} else {
			DurationUserEnv tempFirst = durationUserEnvDao.retrieveContains(beginTimeDate, envType);
			if(tempFirst != null)
				addNewDurationUserEnv(res, tempFirst);
//				res.add(tempFirst);
			
			List<DurationUserEnv> tempMiddles = durationUserEnvDao.retrieveBetween(beginTimeDate, endTimeDate, envType);
			addAllNewDurationUserEnv(res, tempMiddles);
//			res.addAll(tempMiddles);
	
			DurationUserEnv tempLast = durationUserEnvDao.retrieveContains(endTimeDate, envType);
			if(tempLast != null){
				if(res.isEmpty()) {
					addNewDurationUserEnv(res, tempLast);
//					res.add(tempLast);
				} else {
					DurationUserEnv last = res.get(res.size()-1);
					if(!last.equals(tempLast))
						addNewDurationUserEnv(res, tempLast);
//						res.add(tempLast);
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
	
	private void addAllNewDurationUserEnv(List<DurationUserEnv> list, List<DurationUserEnv> durationUserEnvs) {
		for(DurationUserEnv durationUserEnv : durationUserEnvs)
			addNewDurationUserEnv(list, durationUserEnv);
	}
	
	private void addNewDurationUserEnv(List<DurationUserEnv> list, DurationUserEnv durationUserEnv){
		if(list.isEmpty())
			list.add(durationUserEnv);
		else {
			DurationUserEnv last = list.get(list.size()-1);
			if(last.getEndTime().getTime() == durationUserEnv.getTime().getTime() 
					&& last.getUserEnv().equals(durationUserEnv.getUserEnv()))
				last.setEndTime(durationUserEnv.getEndTime());
			else
				list.add(durationUserEnv);
		}
	}
	
	public void deleteAllBefore(Date timeDate){
		durationUserEnvDao.deleteBefore(timeDate);
	}
	
	public void deleteAllBetween(Date beginTime, Date endTime){
		durationUserEnvDao.deleteBetween(beginTime, endTime);
	}
}