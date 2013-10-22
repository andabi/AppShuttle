package lab.davidahn.appshuttle.mine.matcher;

public enum MatcherType {
	FREQUENCY(0, false),
	WEAK_TIME(1, true),
	PLACE(1, false),
	STRICT_TIME(2, false),
	LOCATION(2, false);
//	WEEKLY_TIME
//	UNFAMILIER_PLACE
	
	private int _priority;
	private boolean _enabled;
	
	MatcherType(int priority, boolean enabled){
		_priority = priority;
		_enabled = enabled;
	}

	public int getPriority() {
		return _priority;
	}
	
	public boolean enabled() {
		return _enabled;
	}
}