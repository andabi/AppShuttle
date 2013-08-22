package lab.davidahn.appshuttle.mine.pattern;

import lab.davidahn.appshuttle.DBHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class PatternManager {
	private static PatternManager patternManager;
	private SQLiteDatabase db;
	private PatternMiner patternMiner;

	public PatternMiner getPatternMiner() {
		return patternMiner;
	}
	
	private PatternManager(Context cxt) {
		db = DBHelper.getInstance(cxt).getWritableDatabase();
		patternMiner = new PatternMiner(db);
	}

	public static PatternManager getInstance(Context cxt) {
		if (patternManager == null)
			patternManager = new PatternManager(cxt);
		return patternManager;
	}
}
