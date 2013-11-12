package lab.davidahn.appshuttle.predict.matchergroup;

public enum MatcherGroupType {
	FREQUENCY(0),
	POSITION(1),
	TIME(2);
	
	public int priority;
	
	MatcherGroupType(int _priority){
		priority = _priority;
	}
}