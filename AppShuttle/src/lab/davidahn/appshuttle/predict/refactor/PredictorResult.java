package lab.davidahn.appshuttle.predict.refactor;

import java.util.Map;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;

public class PredictorResult implements Comparable<PredictorResult> {
	// Main properties
	public final UserBhv bhv;
	public double probability;
	
	// Aux. properties (can be eliminated)
	public Map<String, ?> metas;
	/*
	protected TimeZone timeZone;
	protected long time;
	protected long validUntil;
	protected MatcherType matcherType;
	*/
	// TODO: if needed, Predictor 궤적 (ex: Assoc -> Group -> ...) 
	
	
	// Main functions
	public PredictorResult(UserBhv bhv){
		this.bhv = bhv;
		this.probability = 0;
	}
	
	public PredictorResult(UserBhv bhv, double prob){
		this.bhv = bhv;
		this.probability = prob;
	}

	public UserBhv getBhv(){
		return bhv;
	}
	
	@Override
	public int compareTo(PredictorResult another) {
		if (this.probability < another.probability)
			return -1;
		else if (this.probability > another.probability)
			return +1;
		else 
			return 0;
	}
	
	/* TODO list
	 * PreductorResult + PredictorResult (bhv가 같을 때만)
	 * PredictorResult * double
	 */
	
	
	// Aux. functions
	public String viewMsg() {
		return "";
	}
	
	
	
	//protected Map<EnvType, UserEnv> userEnvs;
}
