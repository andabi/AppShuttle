package lab.davidahn.appshuttle.predict.refactor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.predict.matcher.MatcherElem;
import lab.davidahn.appshuttle.predict.matcher.MatcherGroupResult;
import lab.davidahn.appshuttle.predict.matcher.MatcherResultElem;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;

/**
 * 기존의 MatcherGroup
 * Meta-predictor containing multiple predictors with individual priority, num_limit
 * 
 * 아직 구현 안됨
 * @author carrot
 *
 */
public class OrderedPredictorGroup extends BasePredictor {
	List<PredictorEntry> predictorList;
	int numPredictors = 0;
	
	public OrderedPredictorGroup(String name) {
		super(name);
		
		predictorList = new ArrayList<PredictorEntry>();
	}
	
	/** 
	 * Isn't thread-safe
	 * @param p
	 * @param numMaxRes
	 */
	public void addPredictor(BasePredictor p, int numMaxRes) {
		// TODO: 기존에 있었는지 검사
		
		predictorList.add(new PredictorEntry(p, numPredictors, numMaxRes));
		numPredictors++;
	}

	@Override
	protected List<PredictorResult> calcUnnormalizedProbability(List<UserBhv> list,
			Map<String, ?> currUCxt) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private class PredictorEntry implements Comparable<PredictorEntry>{
		public BasePredictor p;
		public int order;
		public int numMaxRes;
		
		PredictorEntry(BasePredictor p, int order, int numMaxRes) {
			this.p = p;
			this.order = order;
			this.numMaxRes = numMaxRes;
		}

		@Override
		public int compareTo(PredictorEntry another) {
			// TODO Auto-generated method stub
			return 0;
		}
		
	}
}
