package lab.davidahn.appshuttle.mine.matcher;

public enum MatcherGroupType {
	FREQUENCY(0),
	TIME(1),
	POSITION(1);

	public int priority;
//	public boolean enabled;
	
	MatcherGroupType(int _priority/*, boolean _enabled*/){
		priority = _priority;
//		enabled = _enabled;
	}
}