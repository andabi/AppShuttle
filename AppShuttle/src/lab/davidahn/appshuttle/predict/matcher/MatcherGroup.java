package lab.davidahn.appshuttle.predict.matcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;

public abstract class MatcherGroup extends MatcherElem {
	protected EnumMap<MatcherType, MatcherElem> matchers;
	
	public MatcherGroup() {
		matchers = new EnumMap<MatcherType, MatcherElem>(MatcherType.class);
	}

	@Override
	public MatcherResultElem matchAndGetResult(UserBhv uBhv, SnapshotUserCxt currUCxt){
		
		if(matchers.isEmpty())
			return null;

		List<MatcherResultElem> matcherResults = new ArrayList<MatcherResultElem>();
		for(MatcherElem matcher : matchers.values()) {
			MatcherResultElem matcherResult = matcher.matchAndGetResult(uBhv, currUCxt);
			if(matcherResult != null)
				matcherResults.add(matcherResult);
		}
		
		if(matcherResults.isEmpty())
			return null;
		
		MatcherGroupResult matcherGroupResult = new MatcherGroupResult(currUCxt.getTime(), currUCxt.getTimeZone(), currUCxt.getUserEnvs());
		matcherGroupResult.setMatcherType(getType());
		matcherGroupResult.setUserBhv(uBhv);
		for(MatcherResultElem matcherResult : matcherResults)
			matcherGroupResult.addMatcherResult(matcherResult);
		matcherGroupResult.setScore(computeScore(matcherResults));
		matcherGroupResult.setViewMsg(extractViewMsg(matcherResults));
		
		return matcherGroupResult;
	}

	public void registerMatcher(MatcherElem matcher) {
		matchers.put(matcher.getType(), matcher);
	}

	protected String extractViewMsg(List<MatcherResultElem> matcherResults) {
		assert(!matcherResults.isEmpty());

		MatcherResultElem maxPriority = Collections.max(matcherResults);
		return maxPriority.getMatcherType().viewMsg;
	}

	protected double computeScore(List<MatcherResultElem> matcherResults) {
		assert(!matcherResults.isEmpty());
		
		MatcherResultElem maxPriority = Collections.max(matcherResults);
		return maxPriority.getScore();
	}
}
