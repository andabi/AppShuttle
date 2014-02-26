package lab.davidahn.appshuttle.predict.matchergroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import lab.davidahn.appshuttle.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.predict.matcher.BaseMatcherElem;
import lab.davidahn.appshuttle.predict.matcher.MatcherElem;
import lab.davidahn.appshuttle.predict.matcher.MatcherResultElem;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;

public abstract class BaseMatcherGroup extends BaseMatcherElem {
	protected EnumMap<MatcherType, MatcherElem> matchers;
	
	public BaseMatcherGroup() {
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
		
		MatcherGroupResult matcherGroupResult = new MatcherGroupResult(currUCxt.getTimeDate(), currUCxt.getTimeZone(), currUCxt.getUserEnvs());
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
