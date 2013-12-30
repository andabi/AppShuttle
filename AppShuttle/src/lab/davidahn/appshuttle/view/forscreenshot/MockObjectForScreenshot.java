package lab.davidahn.appshuttle.view.forscreenshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lab.davidahn.appshuttle.collect.bhv.BaseUserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhvType;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;
import lab.davidahn.appshuttle.view.BlockedBhv;
import lab.davidahn.appshuttle.view.ViewableUserBhv;
import android.app.AlarmManager;
import android.app.Fragment;

public class MockObjectForScreenshot {
	private static UserBhv[] uBhvs = {
		BaseUserBhv.create(UserBhvType.APP, "com.facebook.katana"),
		BaseUserBhv.create(UserBhvType.CALL, "자기♥"),
		BaseUserBhv.create(UserBhvType.APP, "com.astroframe.seoulbus"),
		BaseUserBhv.create(UserBhvType.APP, "com.iloen.melon"),
		BaseUserBhv.create(UserBhvType.APP, "com.kakao.talk"),
		BaseUserBhv.create(UserBhvType.APP, "com.android.chrome"),
		BaseUserBhv.create(UserBhvType.APP, "vStudio.Android.Camera360"),
		BaseUserBhv.create(UserBhvType.APP, "com.google.android.gm"),
		BaseUserBhv.create(UserBhvType.APP, "com.android.settings"),
		BaseUserBhv.create(UserBhvType.APP, "com.android.contacts"),
		//
		BaseUserBhv.create(UserBhvType.APP, "com.facebook.orca"),
		BaseUserBhv.create(UserBhvType.APP, "flipboard.app"),
		BaseUserBhv.create(UserBhvType.APP, "com.facebook.katana"),
		BaseUserBhv.create(UserBhvType.APP, "com.evernote"),
		BaseUserBhv.create(UserBhvType.APP, "com.google.android.apps.docs"),
	};
	
	private static String[] viewMsgs = {
		MatcherType.FREQUENTLY_RECENT.viewMsg,
		MatcherType.TIME_DAILY.viewMsg,
		MatcherType.TIME_DAILY.viewMsg + ", " + MatcherType.PLACE.viewMsg,
		MatcherType.MOVE.viewMsg + ", " + MatcherType.INSTANTALY_RECENT.viewMsg,
		MatcherType.INSTANTALY_RECENT.viewMsg,
		MatcherType.PLACE.viewMsg + ", " + MatcherType.FREQUENTLY_RECENT.viewMsg,
		MatcherType.PLACE.viewMsg + ", " + MatcherType.INSTANTALY_RECENT.viewMsg,
	};
	
	private static ViewableUserBhv[] present = {
		new MockPresentUserBhv(uBhvs[0], viewMsgs[0]),
		new MockPresentUserBhv(uBhvs[1], viewMsgs[1]),
		new MockPresentUserBhv(uBhvs[2], viewMsgs[2]),
		new MockPresentUserBhv(uBhvs[3], viewMsgs[3]),
//		new MockPresentUserBhv(uBhvs[4], viewMsgs[4]),
//		new MockPresentUserBhv(uBhvs[5], viewMsgs[5]),
	};
	
	private static ViewableUserBhv[] favorite = {
		new MockFavoriteBhv(uBhvs[4], viewMsgs[4], true),
		new MockFavoriteBhv(uBhvs[5], viewMsgs[5], true),
		new MockFavoriteBhv(uBhvs[6], viewMsgs[6], false),
	};

	private static ViewableUserBhv[] blocked = {
		new BlockedBhv(uBhvs[7], System.currentTimeMillis() - AlarmManager.INTERVAL_DAY),
		new BlockedBhv(uBhvs[8], System.currentTimeMillis() - 5 * AlarmManager.INTERVAL_DAY),
		new BlockedBhv(uBhvs[9], System.currentTimeMillis() - 7 * AlarmManager.INTERVAL_DAY),
	};
	
	private static Class<? extends Fragment> fragmentSelection = PresentBhvFragmentForScreenshot.class;
//	private static Class<? extends Fragment> fragmentSelection = FavoriteBhvFragmentForScreenshot.class;
//	private static Class<? extends Fragment> fragmentSelection = BlockedBhvFragmentForScreenshot.class;

	private static ViewableUserBhv[] viewSelection = present;
//	private static ViewableUserBhv[] viewSelection = favorite;
//	private static ViewableUserBhv[] viewSelection = blocked;
	
	public static List<ViewableUserBhv> getFakeViewableUserBhvList(Class<? extends Fragment> clazz) {
		if(!fragmentSelection.getSimpleName().equals(clazz.getSimpleName()))
			return Collections.emptyList();
		
		return new ArrayList<ViewableUserBhv>(Arrays.asList(viewSelection));
	}
}

//public static void doFackPredict(List<UserBhv> uBhvs){
//Map<UserBhv, PredictionInfo> predictionInfos = new HashMap<UserBhv, PredictionInfo>();
//for(UserBhv uBhv : uBhvs){
//	EnumMap<MatcherGroupType, MatcherGroupResult> matcherGroupMap = new EnumMap<MatcherGroupType, MatcherGroupResult>(MatcherGroupType.class);
//
//	List<MatcherResult> matcherResults = new ArrayList<MatcherResult>();
//	matcherResults.add(getMockMatcherResult(uBhv, MatcherType.TIME_DAILY));
//
//	matcherGroupMap.put(MatcherGroupType.TIME, getMockMatcherGroupResult(uBhv, MatcherGroupType.TIME, matcherResults));
//	predictionInfos.put(uBhv, getMockPredictionInfo(uBhv, matcherGroupMap));
//}
//
//PresentBhv.extractPresentBhvs(predictionInfos);
//}
//
//public static PredictionInfo getMockPredictionInfo(UserBhv uBhv, EnumMap<MatcherGroupType, MatcherGroupResult> matcherGroupMap ){
//PredictionInfo predictionInfo = new PredictionInfo(null, null, null, uBhv, matcherGroupMap, 0);
//return predictionInfo;
//}
//
//public static MatcherGroupResult getMockMatcherGroupResult(UserBhv uBhv, MatcherGroupType matcherGroupType, List<MatcherResult> matcherResults){
//MatcherGroupResult matcherGroupResult = new MatcherGroupResult(null, null, null);
//matcherGroupResult.setMatcherGroupType(matcherGroupType);
//matcherGroupResult.setTargetUserBhv(uBhv);
//for(MatcherResult matcherResult : matcherResults)
//	matcherGroupResult.addMatcherResult(matcherResult);
//matcherGroupResult.setScore(0);
//matcherGroupResult.setViewMsg(Collections.max(matcherResults).getMatcherType().viewMsg);
//
//return matcherGroupResult;
//}
//
//public static MatcherResult getMockMatcherResult(UserBhv uBhv, MatcherType matcherType){
//MatcherResult matcherResult = new MatcherResult(null, null, null);
//matcherResult.setUserBhv(uBhv);
//matcherResult.setMatcherType(matcherType);
//matcherResult.setNumTotalHistory(0);
//matcherResult.setNumRelatedHistory(0);
//matcherResult.setRelatedHistory(null);
//matcherResult.setLikelihood(0);
//matcherResult.setInverseEntropy(0);
//matcherResult.setScore(0);
//return matcherResult;
//}
