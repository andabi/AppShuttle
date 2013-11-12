package lab.davidahn.appshuttle.predict.matcher;


public class TimeDailyMatcher extends TimeMatcher {

	public TimeDailyMatcher(long duration, double minLikelihood, double minInverseEntropy, int minNumCxt, long period, long tolerance, long acceptanceDelay) {
		super(duration, minLikelihood, minInverseEntropy, minNumCxt, period, tolerance, acceptanceDelay);
	}
	
	@Override
	public MatcherType getMatcherType(){
		return MatcherType.TIME_DAILY;
	}
}
