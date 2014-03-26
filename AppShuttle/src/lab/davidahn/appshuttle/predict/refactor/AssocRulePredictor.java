package lab.davidahn.appshuttle.predict.refactor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.predict.datasource.DataSource;
import lab.davidahn.appshuttle.predict.datasource.Tuple;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;

/**
 * Association rule based predictor.
 * DataSource should return (Map for cond., UserBhv for res) tuple
 * 
 *  Currently, cond can contain only one var. 
 * @author carrot
 *
 */
// TODO: Confidence말고 더 발전된 확률 metric 사용
// TODO: Prior 확률 적용
public class AssocRulePredictor extends BasePredictor {
	private String condKey = null;	// 여러 변수를 고려할 수 있게 하려면, list 등으로 변경해야 함.
	private String resKey = "userBhv";
	private DataSource<Map<String, ?>> dataSource;
	private Comparator comparator = null;
	
	public AssocRulePredictor(String name, DataSource dataSource, String condKey, Comparator comp) {
		//super(name, dataSource, conf);
		super(name);
		
		this.dataSource = dataSource;
		this.condKey = condKey;
		this.comparator = comp;
		
		if (comp == null) {
			this.comparator = new Comparator() {
				@Override
				public int compare(Object lhs, Object rhs) {
					// TODO: Default comp.
					return 0;
				}
			};
		}
	}

	/**
	 * 
	 * @param currCond
	 * @param u
	 * @param cntUnits (함수 호출 후 변경됨)
	 * @return
	 */
	protected double countAndCalcProb(Object currCond, UserBhv u, List<Map<String, ?>> cntUnits) {
		// TODO: 확률 계산 방식 개선
		
		int numberOfRelated = 0;
		int numberOfHit = 0;
		
		/* Why iterator?
		 * UserBhv 리스트를 돌면서, 리스트의 길이를 줄여나가기 위해서
		 * (계산 속도 향상) 
		 */
		Iterator<Map<String, ?>> it = cntUnits.iterator();
		while (it.hasNext()) {
			Map<String, ?> t = it.next();
			Object cond = t.get(condKey);
			UserBhv ubhv = (UserBhv)t.get(resKey);
			
			if (comparator.compare(cond, currCond) != 0) {
				it.remove();	// Remove unrelated data
				continue;
			}
			
			numberOfRelated++;
			if (u.equals(ubhv)) {
				numberOfHit++;
				it.remove();	// 
			}    			
		}
		
		double prob = (double)numberOfHit / (double)numberOfRelated;
		
		return prob;
	}

	@Override
	protected List<PredictorResult> calcUnnormalizedProbability(List<UserBhv> list,
			Map<String, ?> currUCxt) {
		
		Object currCond = currUCxt.get("condKey"); // retreive from currUCxt
		
		// TODO: Retrieve data from datasource
		//List<Tuple<?, UserBhv>> cntUnits = dataSource.getList();
		// List를 수정할 거기 때문에 일단 clone
		List<Map<String, ?>> cntUnits = new ArrayList<Map<String, ?>>(dataSource.getList());
		
		// Count and calc prob.
		List<PredictorResult> resList = new ArrayList<PredictorResult>();
		for (UserBhv u: list) {
			double prob = countAndCalcProb(currCond, u, cntUnits);
			resList.add(new PredictorResult(u, prob));
    	}
		
		// TODO: Normalize
		
		
		return null;
	}
}
