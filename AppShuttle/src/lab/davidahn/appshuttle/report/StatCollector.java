package lab.davidahn.appshuttle.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.AppShuttleDBHelper;
import lab.davidahn.appshuttle.collect.bhv.BaseUserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhvType;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;
import lab.davidahn.appshuttle.view.BlockedBhv;
import lab.davidahn.appshuttle.view.BlockedBhvManager;
import lab.davidahn.appshuttle.view.FavoriteBhv;
import lab.davidahn.appshuttle.view.FavoriteBhvManager;
import lab.davidahn.appshuttle.view.PredictedPresentBhv;
import lab.davidahn.appshuttle.view.PresentBhv;
import android.content.ContentValues;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

public class StatCollector {
	/* Statistics Collector
	 * 
	 */
	
	// Structure-style manifestation of a DB entry
	private class StatEntry{
		long timestamp = System.currentTimeMillis();
		UserBhvType bhvType = UserBhvType.NONE;
		String bhvName = "";
		String matchers = "";				// Matcher 이름
		boolean isPredicted = false;		// Top 6개 안에 들었는가?
		boolean isClicked = false;			// 앱셔틀을 통해 실행했는가?
		
		public String toString(){
			return timestamp + " / " + bhvType.toString() + " / " + bhvName.toString() + " / "
					+ matchers.toString() + " / " + isPredicted + " / " + isClicked; 
		}
	}
	
	private class StatResult {
		// Meta
		long from  = System.currentTimeMillis();
		long until = System.currentTimeMillis();
		
		// in percent (0~100)
		int hit_ratio = 0;		
		int click_ratio = 0;
		
		// in hit (0~N)
		int clicked_count = 0;
		int hit_count = 0;
		int total_count = 0;
		
		// You can add additional measures.
		
		public String toString(){
			return "[통계결과]\n" +
					new Date(from).toString() + " ~ " + new Date(until).toString() + "\n" +
					"uBhv count: " + total_count + "\n" +
					"Hit ratio: " + hit_ratio + " %\n" +
					"Use ratio: " + click_ratio + " %\n"; 
		}
	}
	
	private volatile static StatCollector instance;
	
	/* Internal variables */
	private SQLiteDatabase db;
	
	private UserBhv old_uBhv;		// 직전에 관측된 Bhv
	
	private long created;	// obsolete
	private StatCollector(){
		old_uBhv = null;
		
		created = System.currentTimeMillis();	// 현재 시간 저장
		
		db = AppShuttleDBHelper.getInstance().getWritableDatabase();
		
		Log.i("Stat", "New Collector created / " + created);
	}
	
	/**
	 * 지난 24시간의 (혹은 마지막 통계 전송 후의 모든) 데이터를 분석하여 통계 서버로 전송.
	 * Caller 가 그보다 더 짧은 간격으로 호출하면 동작하지 않음.
	 */
	public void sendDailyCollectedData(){
		// TODO: 이 함수는 IntentService 상속하는 애에서 처리하도록 옮김
		
		// TODO: Send statistics data to the server
		
		// TODO: If it's okay => Delete entries
		
		// TODO: 기타 고정 수 등 추가 통계 업데이트
		
		return;
	}
	
	/**
	 * 사용자 행동의 변화를 발견할 때마다 매번 보내야 하는 통계자료의 전송
	 * @param observedData
	 */
	private void sendEachEntry(StatEntry entry){
		Tracker tracker = EasyTracker.getInstance(AppShuttleApplication.getContext());
		
		String strPredicted = (entry.isPredicted == true ? "Predicted" : "Not Predicted");
		long valuePredicted = (entry.isPredicted == true ? 100 : 0);
		
		String strClicked = (entry.isClicked == true ? "Clicked" : "Not Clicked");
		long valueClicked = (entry.isClicked == true ? 100 : 0);
		
		/* FIXME:
		 * Clicked and Predicted 일 경우,
		 * 사용자가 click 하게 되면 통계가 누게가 됨.
		 * 30초 내에 bhv가 바뀌지 않으면 상관 없지만,
		 * 만약 30초 내에 또 다른 Bhv로 바뀐다면 Clicked 쪽이 통계에서 더 많이 잡힐 수 있음.
		 * 
		 * 이에 대한 bias 수정
		 */
		//Clicked / Predicted 쪽이 가중치가 더 있음. 그에 대한 대처 방안
		
		// tracker.set(Fields.customDimension(1), strPredicted + strClicked);
		// Accuracy tracking
		tracker.send(MapBuilder
				.createEvent("algorithm", strPredicted, entry.matchers, valuePredicted)
				.build()
			);
		
		// Usage tracking
		tracker.send(MapBuilder
				.createEvent("usage", strClicked, entry.bhvName, valueClicked)
				.build()
			);
		
		return;
	}
	
	/**
	 * 해당 기간의 통계 자료를 추출 
	 * @param from
	 * 			통계에 포함되는 시작 시각. null 을 주면 until을 기준으로 24시간 전의 시점부터 통계 
	 * @param until
	 * 			통계에 포함되는 끝 시각. null 을 주면 현재를 기준으로 통계
	 */
	protected StatResult getStatistics(long from, long until){
		if (until == 0)
			until = System.currentTimeMillis(); // 현재 시각
		if (from == 0)
			from = until - (24 * 60 * 60 * 1000);
		
		StatResult statReturn = new StatResult();
		statReturn.from = from;
		statReturn.until = until;
		
		/*
		 *  Utilize this if needed.
		// Retrieve entries from DB
		Cursor cur = db.rawQuery("SELECT * " +
				"FROM stat_bhv_transition" +
				"WHERE time > " + from.getTime() + " " +
					"AND time <= " + until.getTime() + ";", null);
		
		List <StatEntry> statEntries = new ArrayList<StatEntry>();
		while (cur.moveToNext()) {
			StatEntry entry = new StatEntry();
			
			entry.timestamp = new Date(cur.getLong(0));
			entry.bhvType = UserBhvType.valueOf(cur.getString(1));
			entry.bhvName = cur.getString(2);
			entry.matchers = cur.getString(3);
			entry.isPredicted = (cur.getInt(4) > 0 ? true : false);
			entry.isClicked = (cur.getInt(5) > 0 ? true : false);
			
			statEntries.add(entry);
			
			
		}
		cur.close();
		*/
		
		
		// 로그 데이터에서 통계 값 계산
		statReturn.total_count = (int)DatabaseUtils.queryNumEntries(db, "stat_bhv_transition",
						"time > " + from + " AND time <= " + until);
		
		statReturn.clicked_count = (int)DatabaseUtils.queryNumEntries(db, "stat_bhv_transition",
				"time > " + from + " AND time <= " + until + " AND clicked > 0");
		
		statReturn.hit_count = (int)DatabaseUtils.queryNumEntries(db, "stat_bhv_transition",
				"time > " + from + " AND time <= " + until + " AND predicted > 0");
		
		statReturn.click_ratio = (int)(statReturn.clicked_count * 100.0f / statReturn.total_count);
		statReturn.hit_ratio = (int)(statReturn.hit_count * 100.0f / statReturn.total_count);
		
		// You can add additional measures.
		
		return statReturn;
	}
	
	public StatResult getStatistics(){
		return this.getStatistics(0, 0);
	}	
	
	public static StatCollector getInstance(){
		if (instance == null) {
			synchronized(StatCollector.class) {
				if (instance == null)
					instance = new StatCollector();
			}
		}
		
		return instance;
	}
	
//	public void setPredictedBhvList(List <ViewableUserBhv> l){
//		// FIXME: Matcher 종류 알 수 있는 데이터 구조로 전달 받기
//		
//		this.listPredictedBhvs = l;
//		
//		if (this.listPredictedBhvs.size() > 6)
//			this.listPredictedBhvs = this.listPredictedBhvs.subList(0, 6);
//	}
	
	/**
	 * Make a new statistics entry.
	 * 사용자의 새로운 행동이 관측될 때마다 호출해야함.
	 * 런쳐/ScreenOFF 등의 무시는 Caller 가 고려하지 않아도 됨.
	 * @param uCxt
	 * @param isClicked
	 */
	public void notifyBhvTransition(UserBhv userBhv, boolean isClicked){
		// TODO: App 말고 다른 타입에 대한 고려
		// TODO: 24시간이 지나면, 통계사이트로 전송하고 객체 초기화하는 코드 추가
		
		//Log.d("Stat", "Input: " + userBhv.toString());
		
		// 화면 OFF 등 앱이 아닌 경우, 무시
		if (userBhv.getBhvType() != UserBhvType.APP)
			return;
		
		// Same UserBhv (Screen ON -> OFF -> ON) 무시
		//if (old_uBhv != null)
		//	Log.d("Stat", "old Ubhv: " + old_uBhv.toString());
		if (old_uBhv != null && old_uBhv.equals(userBhv))
			return;
		this.old_uBhv = userBhv; // FIXME: Is this okay? Reference
		
		// 런쳐 / 앱셔틀 등 통계낼 필요 없는 앱 무시
		if (userBhv instanceof BaseUserBhv) {
			BaseUserBhv b = (BaseUserBhv) userBhv;
			if (!b.isValid()) {
				Log.i("Stat", "Tracking is not needed.");
				return;
			}
		}
		
		Log.i("Stat", "Bhv transition caught");
		StatEntry newEntry = new StatEntry();
		newEntry.bhvType = userBhv.getBhvType();
		newEntry.bhvName = userBhv.getBhvName();
		
		// 노티바에 보이는 6개의 앱 얻기 (현재 앱 제외 안함)
		/* 차단된 앱에 속해 있으면 무시 */
		Set<BlockedBhv> setBlockedBhvs = BlockedBhvManager.getInstance().getBlockedBhvSet();
		if (setBlockedBhvs.contains(userBhv)){
			Log.i("Stat", "Blocked bhv: " + userBhv.getBhvName().toString());
			return;
		}
		
		/* 고정 앱 처리 */
		Set<FavoriteBhv> setFavoriteBhvs = FavoriteBhvManager.getInstance().getFavoriteBhvSet();
		if (setFavoriteBhvs.contains(userBhv)){
			newEntry.isPredicted = true;
			newEntry.matchers = "Favorite";
		}
		
		/* 현재 앱 처리 */
		// 고정앱 포함해서 최대 6개까지만 비교하도록
		int numPrediction = 6 - setFavoriteBhvs.size();
		numPrediction = (numPrediction < 0 ? 0 : numPrediction);
		
		List<PresentBhv> listPredictedBhvs = PresentBhv.getPresentBhvListFilteredSorted(numPrediction, false);

		for (int l=0; l<listPredictedBhvs.size(); l++){
			PresentBhv uBhvPredicted = listPredictedBhvs.get(l); 
			if (uBhvPredicted.getBhvName().equals(newEntry.bhvName)) {
				Log.i("Stat", "Bhv transition hit.");
				
				newEntry.isPredicted = true;
				
				switch (uBhvPredicted.getType()){
					case HISTORY:
						newEntry.matchers = "History";
						break;
					case PREDICTED:
						PredictedPresentBhv p = (PredictedPresentBhv)uBhvPredicted;
						List<MatcherType> matcherList = p.getMatcherList();
						List<String> matcherStrList = new ArrayList<String>();
						
						for (MatcherType m: matcherList)
							matcherStrList.add(m.toString());
						
						/* FIXME: 여러개의 matcher에 대한 고려 (현재는 그냥 ARRAY를 출력)
						if (matcherStrList.size() > 0)
							newEntry.matchers = matcherStrList.get(0);
						*/
						
						newEntry.matchers = matcherStrList.toString().replace("[", "").replace("]", "");
						break;
					case SELECTED:
						newEntry.matchers = "Dummy";
						break;
					default:
						
				}				
			}
		}
		
		newEntry.isClicked = isClicked;
		
		if (newEntry.isClicked == true && newEntry.isPredicted == false) {
			// View 차이 때문에 생길 수 있음. (7번째 예상 앱인데, 현재 사용중인 앱을 제외하다보니 보이게 된 경우)
			newEntry.isPredicted = true;
			newEntry.matchers = "Unknown Matcher";
		}
		
	
		/* Store into DB */
		ContentValues row = new ContentValues();
		row.put("time", newEntry.timestamp);
		row.put("bhv_type", newEntry.bhvType.toString());
		row.put("bhv_name", newEntry.bhvName);
		row.put("matchers", newEntry.matchers);
		row.put("predicted", (newEntry.isPredicted ? 1 : 0));
		row.put("clicked",  (newEntry.isClicked ? 1 : 0));
		
		db.insertWithOnConflict("stat_bhv_transition", null, row, SQLiteDatabase.CONFLICT_REPLACE);
		Log.i("Stat", row.toString());
		
		this.sendEachEntry(newEntry);
	}
	
	public void deleteAllBefore(long time){
		db.execSQL("DELETE " +
				"FROM stat_bhv_transition " +
				"WHERE time < " + time +";");
	}
	
		
	@Override
	public String toString(){
		return this.getStatistics().toString();
		/*
		return "Since " + created.toString() +
				"\nTotalClicked = " + this.numTotalClicked + 
				"\nBhvTransition: " + this.numBhvTransitionHit + " / " + this.numBhvTransitionTotal;
				*/
	}
}
