package lab.davidahn.appshuttle.mine.matcher;

public enum MatcherType {
	FREQUENCY(0),
	WEAK_TIME(1),
	PLACE(1),
	STRICT_TIME(2),
	LOCATION(2);
//	WEEKLY_TIME
//	UNFAMILIER_PLACE
	private int priority;
	
	MatcherType(int priority){
		this.priority = priority;
	}

	public int getPriority() {
		return priority;
	}
}