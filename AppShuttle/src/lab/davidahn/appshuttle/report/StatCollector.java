package lab.davidahn.appshuttle.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.AppShuttleDBHelper;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhvManager;
import lab.davidahn.appshuttle.collect.bhv.UserBhvType;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;
import lab.davidahn.appshuttle.view.BlockedBhv;
import lab.davidahn.appshuttle.view.BlockedBhvManager;
import lab.davidahn.appshuttle.view.FavoriteBhv;
import lab.davidahn.appshuttle.view.FavoriteBhvManager;
import lab.davidahn.appshuttle.view.PredictedPresentBhv;
import lab.davidahn.appshuttle.view.PresentBhv;
import lab.davidahn.appshuttle.view.PresentBhvType;
import lab.davidahn.appshuttle.view.ui.NotiBarNotifier;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class StatCollector {

	private static final String tableName = "stat_bhv_transition";
	private static final String columnTime = "time";
	private static final String columnBhvType = "bhv_type";
	private static final String columnBhvName = "bhv_name";
	private static final String columnMatchers = "matchers";
	private static final String columnPredicted = "predicted";
	private static final String columnClicked = "clicked";

	private static StatCollector statCollector = new StatCollector();
	public static StatCollector getInstance(){
		return statCollector;
	}
	
	private SQLiteDatabase db;
	private Gson gson;
	private UserBhv prevBhv;
	
	private StatCollector(){
		db = AppShuttleDBHelper.getInstance().getWritableDatabase();
		gson = new Gson();
//		Log.d("Stat", "New Collector created / " + created);
	}
	
	/**
	 * 사용자 행동의 변화를 발견할 때마다 매번 보내야 하는 통계자료의 전송
	 */
	private void sendEvent(StatEntry entry){
		Tracker tracker = EasyTracker.getInstance(AppShuttleApplication.getContext());

		String strPredicted = (entry.isPredicted) ? "Predicted" : "Not Predicted";
		long valuePredicted = (entry.isPredicted) ? 100 : 0; //percent

		switch(entry.presentBhvType){
		case PREDICTED:
			valuePredicted /= entry.matchers.size();
			for(MatcherType matcherType : entry.matchers) {
				String label = matcherType.name();
				tracker.send(MapBuilder
						.createEvent("algorithm", strPredicted,	label, valuePredicted).build());

//				List<MatcherType> withMatchers = new ArrayList<MatcherType>(entry.matchers);
//				withMatchers.remove(matcherType);
//				if(!withMatchers.isEmpty()) {
//					Collections.sort(withMatchers);
//					String label = matcherType.name() 
//							+ " (with " + withMatchers.toString().replace("[", "").replace("]", "") + ")";
//					tracker.send(MapBuilder
//							.createEvent("algorithm", strPredicted, label, valuePredicted).build());
//				}
			}
			break;
		default:
			tracker.send(MapBuilder.createEvent("algorithm", strPredicted, 
					entry.presentBhvType.name() , valuePredicted).build());
		}
//		case FAVORITE:
//			tracker.send(MapBuilder.createEvent("algorithm", strPredicted, 
//							entry.presentBhvType.name() , valuePredicted).build());
//			break;
//		case HISTORY:
//			tracker.send(MapBuilder.createEvent("algorithm", strPredicted, 
//					entry.presentBhvType.name() , valuePredicted).build());
//			break;
//		case SELECTED:
//			tracker.send(MapBuilder.createEvent("algorithm", strPredicted, 
//					entry.presentBhvType.name() , valuePredicted).build());
//			break;
		
		// tracker.set(Fields.customDimension(1), strPredicted + strClicked);
		
//		String strClicked = (entry.isClicked) ? "Clicked" : "Not Clicked";
//		long valueClicked = (entry.isClicked) ? 100 : 0;
//		tracker.send(MapBuilder
//				.createEvent("usage", strClicked, entry.bhvName, valueClicked)
//				.build()
//			);
		
		/* FIXME:
		 * Clicked and Predicted 일 경우,
		 * 사용자가 click 하게 되면 통계가 누게가 됨.
		 * 30초 내에 bhv가 바뀌지 않으면 상관 없지만,
		 * 만약 30초 내에 또 다른 Bhv로 바뀐다면 Clicked 쪽이 통계에서 더 많이 잡힐 수 있음.
		 * 
		 * 이에 대한 bias 수정
		 */
		//Clicked / Predicted 쪽이 가중치가 더 있음. 그에 대한 대처 방안
	}
	
	/**
	 * 해당 기간의 통계 자료를 추출 
	 * @param fromTime
	 * 			통계에 포함되는 시작 시각. null 을 주면 until을 기준으로 24시간 전의 시점부터 통계 
	 * @param toTime
	 * 			통계에 포함되는 끝 시각. null 을 주면 현재를 기준으로 통계
	 */
	protected StatResult getStatistics(long fromTime, long toTime){
		if (toTime == 0)
			toTime = System.currentTimeMillis();
		if (fromTime == 0)
			fromTime = toTime - (24 * 60 * 60 * 1000);
		
		StatResult statResult = new StatResult();
		statResult.fromTime = fromTime;
		statResult.toTime = toTime;
		statResult.totalCount = (int)DatabaseUtils.queryNumEntries(db, tableName,
				columnTime + " > " + fromTime + " AND " + columnTime + " <= " + toTime);
		statResult.clickedCount = (int)DatabaseUtils.queryNumEntries(db, tableName,
				columnTime + " > " + fromTime + " AND " + columnTime + " <= " + toTime + " AND " + columnClicked + " > 0");
		statResult.hitCount = (int)DatabaseUtils.queryNumEntries(db, tableName,
				columnTime + " > " + fromTime + " AND " + columnTime + " <= " + toTime + " AND " + columnPredicted + " > 0");
		statResult.clickRatio = statResult.clickedCount * 100.0f / statResult.totalCount;
		statResult.hitRatio = statResult.hitCount * 100.0f / statResult.totalCount;
		
		return statResult;
	}
	
	public StatResult getStatistics(){
		return this.getStatistics(0, 0);
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
	// TODO: App 말고 다른 타입에 대한 고려
	// TODO: 24시간이 지나면, 통계사이트로 전송하고 객체 초기화하는 코드 추가
	public void notifyBhvTransition(UserBhv userBhv, boolean isClicked){
		//Log.d("Stat", "Input: " + userBhv.toString());
		if (userBhv.getBhvType() != UserBhvType.APP)
			return;
		
		// Same UserBhv (Screen ON -> OFF -> ON) 무시
		//Log.d("Stat", "old Ubhv: " + old_uBhv.toString());
		if (prevBhv != null && prevBhv.equals(userBhv))
			return;

		prevBhv = userBhv;
		
		// 런쳐 / 앱셔틀 등 통계낼 필요 없는 앱 무시
		if (!(UserBhvManager.getInstance().getRegisteredUserBhv(userBhv)).isValid()) {
//			Log.d("Stat", "Tracking is not needed.");
			return;
		}
			
//		Log.d("Stat", "Bhv transition caught");
		StatEntry newEntry = new StatEntry();
		newEntry.bhvType = userBhv.getBhvType();
		newEntry.bhvName = userBhv.getBhvName();
		
		// 노티바에 보이는 6개의 앱 얻기 (현재 앱 제외 안함)
		/* 차단된 앱에 속해 있으면 무시 */
		Set<BlockedBhv> setBlockedBhvs = BlockedBhvManager.getInstance().getBlockedBhvSet();
		if (setBlockedBhvs.contains(userBhv)){
//			Log.d("Stat", "Blocked bhv: " + userBhv.getBhvName().toString());
			return;
		}
		
		/* 고정 앱 처리 */
		Set<FavoriteBhv> setFavoriteBhvs = FavoriteBhvManager.getInstance().getFavoriteBhvSet();
		if (setFavoriteBhvs.contains(userBhv)){
			newEntry.isPredicted = true;
			newEntry.presentBhvType = PresentBhvType.FAVORITE;
		}
		
		/* 현재 앱 처리 */
		int numPrediction = NotiBarNotifier.getInstance().getNumPresentElem();
		//현재 쓰는 앱은 알림창에 표시가 안되어 사용자가 한칸을 더 볼 수 있으므로 1을 더한다.
		List<PresentBhv> listPredictedBhvs = PresentBhv.getPresentBhvListFilteredSorted(numPrediction + 1, false);
		for (int i=0; i<listPredictedBhvs.size(); i++){
			PresentBhv uBhvPredicted = listPredictedBhvs.get(i); 
			if (!uBhvPredicted.getBhvName().equals(newEntry.bhvName))
				continue;
//			Log.d("Stat", "Bhv transition hit.");
			newEntry.isPredicted = true;
			
			switch (uBhvPredicted.getType()){
				case HISTORY:
					newEntry.presentBhvType = PresentBhvType.HISTORY;
					break;
				case PREDICTED:
					newEntry.presentBhvType = PresentBhvType.PREDICTED;
					newEntry.matchers = ((PredictedPresentBhv)uBhvPredicted).getFinalMatchers();
					break;
				case SELECTED:
					newEntry.presentBhvType = PresentBhvType.SELECTED;
					break;
				default:
			}
		}
		
		newEntry.isClicked = isClicked;
		
//		// View 차이 때문에 생길 수 있음. (7번째 예상 앱인데, 현재 사용중인 앱을 제외하다보니 보이게 된 경우)
//		if (newEntry.isClicked == true && newEntry.isPredicted == false) {
//			newEntry.isPredicted = true;
//			newEntry.matchers = "Unknown Matcher";
//		}
		store(newEntry);
		sendEvent(newEntry);
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
	
	public void createTable() {
		db.execSQL("CREATE TABLE IF NOT EXISTS " + tableName + " ("
				+ columnTime + " INTEGER, "
				+ columnBhvType + " TEXT, "
				+ columnBhvName + " TEXT, "
				+ columnMatchers + " TEXT, "
				+ columnPredicted + " INTEGER, "
				+ columnClicked + " INTEGER, "
				+ "PRIMARY KEY (" + columnTime + ") "
				+ ");");
	}

	public void store(StatEntry entry){
		ContentValues row = new ContentValues();
		row.put(columnTime, entry.timestamp);
		row.put(columnBhvType, entry.bhvType.toString());
		row.put(columnBhvName, entry.bhvName);
		row.put(columnMatchers, gson.toJson(entry.matchers));
		row.put(columnPredicted, (entry.isPredicted ? 1 : 0));
		row.put(columnClicked, (entry.isClicked ? 1 : 0));
		db.insertWithOnConflict(tableName, null, row, SQLiteDatabase.CONFLICT_REPLACE);
//		Log.d("Stat", row.toString());
	}
	
	public List<StatEntry> retrieve(long fromTime, long toTime){
		Cursor cur = db.rawQuery(
				"SELECT *" +
				" FROM stat_bhv_transition" +
				" WHERE time > " + fromTime +
					" AND time <= " + toTime + ";", null);
		
		List <StatEntry> statEntries = new ArrayList<StatEntry>();
		while (cur.moveToNext()) {
			StatEntry entry = new StatEntry();
			entry.timestamp = cur.getLong(0);
			entry.bhvType = UserBhvType.valueOf(cur.getString(1));
			entry.bhvName = cur.getString(2);
			entry.matchers = gson.fromJson(cur.getString(3), new TypeToken<List<MatcherType>>(){}.getType());
			entry.isPredicted = (cur.getInt(4) > 0) ? true : false;
			entry.isClicked = (cur.getInt(5) > 0) ? true : false;
			statEntries.add(entry);
		}
		cur.close();
		return statEntries;
	}

	public void deleteAllBefore(long time){
		db.execSQL("DELETE FROM " + tableName +
				" WHERE " + columnTime + " < " + time + ";");
	}
	
	@Override
	public String toString(){
		return getStatistics().toString();
	}
	
	/**
	 *  Structure-style manifestation of a DB entry
	 */
	private class StatEntry{
		long timestamp = System.currentTimeMillis();
		UserBhvType bhvType = UserBhvType.NONE;
		String bhvName = "";
		List<MatcherType> matchers = new ArrayList<MatcherType>();
		PresentBhvType presentBhvType = PresentBhvType.NONE;
		boolean isPredicted = false;		// Top 6개 안에 들었는가?
		boolean isClicked = false;			// 앱셔틀을 통해 실행했는가?
		
		public String toString(){
			return timestamp + " / " + bhvType.toString() + " / " + bhvName.toString() + " / "
					+ matchers.toString() + " / " + isPredicted + " / " + isClicked; 
		}
	}

	private class StatResult {
		// Meta
		long fromTime  = System.currentTimeMillis();
		long toTime = System.currentTimeMillis();
		
		// in percent (0~100)
		float hitRatio = 0;		
		float clickRatio = 0;
		
		// in hit (0~N)
		int clickedCount = 0;
		int hitCount = 0;
		int totalCount = 0;
		
		public String toString(){
			return new Date(fromTime).toString() + " ~ " + new Date(toTime).toString() + "\n" +
					"uBhv count: " + totalCount + "\n" +
					"Hit ratio: " + hitRatio + " %\n" +
					"Use ratio: " + clickRatio + " %\n"; 
		}
	}
}
