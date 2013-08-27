package lab.davidahn.appshuttle.mine.pattern;

import android.content.Context;

public class PatternManager {
	private static PatternManager patternManager;
	private PatternMiner patternMiner;

	public PatternMiner getPatternMiner() {
		return patternMiner;
	}
	
	private PatternManager(Context cxt) {
		patternMiner = new PatternMiner();
	}

	public static PatternManager getInstance(Context cxt) {
		if (patternManager == null)
			patternManager = new PatternManager(cxt);
		return patternManager;
	}
}
