package lab.davidahn.appshuttle.mine.pattern;

import java.util.ArrayList;
import java.util.List;

import lab.davidahn.appshuttle.context.RfdUserCxt;

public class PatternMiner {
	
	public PatternMiner() {
	}

	public List<Pattern> minePattern(RfdUserCxt rfdUCxt, String tableName){
		List<Pattern> res = new ArrayList<Pattern>();
//		Area area = new Area(rfdUCxt.getLocFreqMap().get(0).getLatitude(), rfdUCxt.getLocFreqMap().get(0).getLatitude(), 0);
//		Pattern pat = new Pattern(rfdUCxt.getStartTime(), rfdUCxt.getEndTime(), rfdUCxt.getTimeZone(), area, rfdUCxt.getBhv());
//		res.add(pat);
		return res;
	}
	
	public boolean storePattern(List<Pattern> patList){
		//TODO store into db
		return true;
	}
}
