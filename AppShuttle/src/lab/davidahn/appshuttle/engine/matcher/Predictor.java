package lab.davidahn.appshuttle.engine.matcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lab.davidahn.appshuttle.GlobalState;
import lab.davidahn.appshuttle.bean.MatchedCxt;
import lab.davidahn.appshuttle.collector.ContextManager;
import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;

public class Predictor {
	private ContextManager contextManager;
	private Context cxt;
	private SharedPreferences settings;
	
	public Predictor(Context cxt){
		this.cxt = cxt;
		contextManager = ContextManager.getInstance(cxt);
		settings = cxt.getSharedPreferences("AppShuttle", Context.MODE_PRIVATE);
	}
	
	public List<MatchedCxt> predict(int topN){
		List<MatchedCxt> res = new ArrayList<MatchedCxt>();

		ContextMatcher timeCxtMatcher = new TimeContextMatcher(cxt
				, settings.getFloat("matcher.time.min_likelihood", 0.1f)
				, settings.getInt("matcher.time.min_num_cxt", 3)
				, AlarmManager.INTERVAL_DAY
				, settings.getLong("matcher.time.tolerance", AlarmManager.INTERVAL_HOUR / 6));
		List<MatchedCxt> timeMatchedCxtList = timeCxtMatcher.matchAndGetResult(GlobalState.currentUEnv);
		
		ContextMatcher locCxtMatcher = new LocContextMatcher(cxt
				, settings.getFloat("matcher.loc.min_likelihood", 0.1f)
				, settings.getInt("matcher.loc.min_num_cxt", 3)
				, settings.getInt("matcher.loc.min_distance", 2000));
		List<MatchedCxt> locMatchedCxtList = locCxtMatcher.matchAndGetResult(GlobalState.currentUEnv);

//		List<MatchedCxt> locMatchhedCxtList;
//		if(GlobalState.recentLocMatchedCxtList == null) 
//			GlobalState.recentLocMatchedCxtList = new ArrayList<MatchedCxt>();
//		if(GlobalState.moved == true){
//			ContextMatcher locCxtMatcher = new LocContextMatcher(cxt, 0, settings.getInt("location.min_distance", 2000));
//			locMatchedCxtList = locCxtMatcher.matchAndGetResult(GlobalState.currentUEnv);
//			GlobalState.recentLocMatchedCxtList = locMatchedCxtList;
//			Log.i("location", "moved");
//		} else {
//			locMatchedCxtList = GlobalState.recentLocMatchedCxtList;
//		}
		
		ContextMatcher FreqCxtMatcher = new FreqContextMatcher(cxt
				, settings.getFloat("matcher.freq.min_likelihood", 0.1f)
				, settings.getInt("matcher.freq.min_num_cxt", 3));
		List<MatchedCxt> FreqCatchedCxtList = FreqCxtMatcher.matchAndGetResult(GlobalState.currentUEnv);

		res.addAll(timeMatchedCxtList);
		res.addAll(locMatchedCxtList);
		res.addAll(FreqCatchedCxtList);
		
		Collections.sort(res);
		res = res.subList(0, Math.min(res.size(), topN));
		return res;
	}

	public void storePrediction(MatchedCxt matchedCxt){
		contextManager.storeMatchedCxt(matchedCxt);
	}
}