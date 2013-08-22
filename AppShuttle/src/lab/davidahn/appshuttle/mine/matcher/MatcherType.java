package lab.davidahn.appshuttle.mine.matcher;

public enum MatcherType {
	FREQUENCY(0),
	TIME(1),
	LOCATION(1);
	
	private int priority;
	
	MatcherType(int priority){
		this.priority = priority;
	}

	public int getPriority() {
		return priority;
	}
}