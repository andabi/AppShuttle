package lab.davidahn.appshuttle.predict.refactor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.predict.datasource.DataSource;

/**
 * Meta-predictor containing Multiple Predictors with individual weights
 * @author carrot
 *
 */
public class PredictorGroup extends BasePredictor {
	Map<BasePredictor, Double> weightedPredictorMap = null;
	
	public PredictorGroup(String name) {
		super(name);
		// TODO Auto-generated constructor stub
		
		weightedPredictorMap = new HashMap<BasePredictor, Double>();
	}
	
	public void addPredictor(BasePredictor p, Double weight) {
		weightedPredictorMap.put(p, weight);
	}

	@Override
	protected List<PredictorResult> calcUnnormalizedProbability(List<UserBhv> list,
			Map<String, ?> currUCxt) {
		
		if (weightedPredictorMap.isEmpty())
			return null;
		
		/* TODO
		 * - predict
		 * - weighted sum
		 * - generate result list
		 */
		
		return null;
	}
}
