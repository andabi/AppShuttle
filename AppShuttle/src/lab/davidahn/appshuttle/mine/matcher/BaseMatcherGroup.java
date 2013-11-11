package lab.davidahn.appshuttle.mine.matcher;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.bhv.UserBhv;

public abstract class BaseMatcherGroup implements MatcherGroup {
	protected MatcherGroupType matcherGroupType;
	protected int priority;
	protected EnumMap<MatcherType, Matcher> matchers;
	
	public BaseMatcherGroup(MatcherGroupType _matcherType, int _priority) {
		matcherGroupType = _matcherType;
		priority = _priority;
		matchers = new EnumMap<MatcherType, Matcher>(MatcherType.class);
	}

	@Override
	public MatcherGroupType getMatcherGroupType(){
		return matcherGroupType;
	}

	@Override
	public int getPriority() {
		return priority;
	}
	
	@Override
	public MatcherGroupResult matchAndGetResult(UserBhv uBhv, SnapshotUserCxt currUCxt){

		MatcherGroupResult matcherGroupResult = new MatcherGroupResult(currUCxt.getTimeDate(), currUCxt.getTimeZone(), currUCxt.getUserEnvs());
		matcherGroupResult.setMatcherGroupType(matcherGroupType);
		matcherGroupResult.setTargetUserBhv(uBhv);

		List<MatcherResult> matcherResults = new ArrayList<MatcherResult>();
		for(Matcher matcher : matchers.values()) {
			matcherResults.add(matcher.matchAndGetResult(uBhv, currUCxt));
		}
		
		matcherGroupResult.setScore(computeScore(matcherResults));
		matcherGroupResult.setViewMsg(extractViewMsg(matcherResults));
		
		return matcherGroupResult;
	}

	public void addMatcher(Matcher matcher) {
		matchers.put(matcher.getMatcherType(), matcher);
	}

	protected abstract String extractViewMsg(List<MatcherResult>  matcherResults);
	
	protected abstract double computeScore(List<MatcherResult>  matcherResults);
}
