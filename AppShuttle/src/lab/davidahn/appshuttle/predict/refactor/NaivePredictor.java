package lab.davidahn.appshuttle.predict.refactor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.collect.bhv.UserBhv;

/** 
 * 학습 없이 확률을 리턴하는 predictor
 * @author carrot
 *
 */
public class NaivePredictor extends BasePredictor {

	public NaivePredictor(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected List<PredictorResult> calcUnnormalizedProbability(List<UserBhv> list,
			Map<String, ?> currUCxt) {
		
		int numBhvs = list.size();
		
		List<PredictorResult> retList = new ArrayList<PredictorResult>();
		
		// 현재는 모두 같은 확률을 리턴하도록 되어 있음
		for (UserBhv u: list) {
			retList.add(new PredictorResult(u, 1.0 / (double)numBhvs));
		}
		
		return null;
	}

}
