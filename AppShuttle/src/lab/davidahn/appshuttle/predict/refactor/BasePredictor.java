package lab.davidahn.appshuttle.predict.refactor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.predict.datasource.DataSource;

public abstract class BasePredictor {
	private String name;
	private Map<String, Object> conf;
	private DataSource<?> dataSource;		// get DAO
	private String resKey = "UserBhv";			// default key for results
	
	public BasePredictor(String name) {
		this.name = name;
	}
	
	public BasePredictor(String name, DataSource dataSource, Map<String, Object> conf) {
		this.name = name;
		this.dataSource = dataSource;
		this.conf = conf;
	}
	
	// Type이 필요한가?
	//public abstract MatcherType getType();
	
	public String toString() {
		return name;
	}
	
	public void doTraining(DataSource dataSource){
		this.dataSource = dataSource;
		return;
	}
	
	
	/**
	 * calcProbability of [0, 1]
	 * @param uBhv
	 * @param currUCxt
	 * @return
	 */
	protected abstract List<PredictorResult> calcUnnormalizedProbability(List<UserBhv> list, Map<String, ?> currUCxt);
	
	public PredictorResult calcUnnormalizedProbability(UserBhv uBhv, Map<String, ?> currUCxt) {
		List<UserBhv> listSingle = new ArrayList<UserBhv>();
		
		listSingle.add(uBhv);
		
		return calcUnnormalizedProbability(listSingle, currUCxt).get(0);
	}
	
	public List<PredictorResult> calcNormalizedProbability(List<UserBhv> list, Map<String, ?> currUCxt) {
		List <PredictorResult> resList = calcUnnormalizedProbability(list, currUCxt);
		normalize(resList);
		
		return resList; 
	}
	
	/**
	 * sum(prob.) => 1
	 * @param list containing PredictorResult
	 */
	protected void normalize(List<PredictorResult> list) {
		// TODO: Sum and divide
	}
}
