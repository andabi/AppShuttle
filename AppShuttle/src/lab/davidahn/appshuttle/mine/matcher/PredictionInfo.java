package lab.davidahn.appshuttle.mine.matcher;

import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.TimeZone;

import lab.davidahn.appshuttle.context.bhv.BhvType;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.context.env.EnvType;
import lab.davidahn.appshuttle.context.env.UserEnv;

public class PredictionInfo implements UserBhv, Comparable<PredictionInfo> {
	private final UserBhv _uBhv;
	private final Date _timeDate;
	private final TimeZone _timeZone;
	private final Map<EnvType, UserEnv> _uEnvs;
	private final EnumMap<MatcherType, MatchedResult> _matchedResults;
	private final double _score;
	
//	protected Drawable _icon;
//	protected String _bhvNameText;
//	protected String _viewMsg;
//	protected Intent _launchIntent;
	
	public PredictionInfo(Date time, TimeZone timeZone, Map<EnvType, UserEnv> userEnvs, UserBhv uBhv, EnumMap<MatcherType, MatchedResult> matchedResults, double score){
		_timeDate = time;
		_timeZone = timeZone;
		_uEnvs = userEnvs;
		_uBhv = uBhv;
		_matchedResults = matchedResults;
		_score = score;
	}
	
	@Override
	public BhvType getBhvType() {
		return _uBhv.getBhvType();
	}
	@Override
	public void setBhvType(BhvType bhvType) {
		_uBhv.setBhvType(bhvType);
	}
	@Override
	public String getBhvName() {
		return _uBhv.getBhvName();
	}
	@Override
	public void setBhvName(String bhvName) {
		_uBhv.setBhvName(bhvName);
	}
	@Override
	public Object getMeta(String key) {
		return _uBhv.getMeta(key);
	}
	@Override
	public void setMeta(String key, Object val){
		_uBhv.setMeta(key, val);
	}
	
	public Date getTime() {
		return _timeDate;
	}

	public TimeZone getTimeZone() {
		return _timeZone;
	}

	public Map<EnvType, UserEnv> getUserEnvMap() {
		return _uEnvs;
	}

	public UserEnv getUserEnv(EnvType envType) {
		return _uEnvs.get(envType);
	}
	
	public UserBhv getUserBhv() {
		return _uBhv;
	}

	public Map<MatcherType, MatchedResult> getMatchedResultMap() {
		return _matchedResults;
	}
	
	public MatchedResult getMatchedResult(MatcherType matcherType) {
		return _matchedResults.get(matcherType);
	}

	public double getScore() {
		return _score;
	}
	
	@Override
	public boolean equals(Object o) {
		if ((o instanceof UserBhv)
				&& _uBhv.getBhvName().equals(
						((UserBhv) o).getBhvName())
				&& _uBhv.getBhvType() == ((UserBhv) o).getBhvType())
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode(){
		return _uBhv.hashCode();
	}
	
	public int compareTo(PredictionInfo predictedBhv){
		if(_score < predictedBhv._score) return 1;
		else if(_score == predictedBhv._score) return 0;
		else return -1;
	}
	
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("matched results: ").append(_matchedResults.toString()).append(", ");
		msg.append("predicted bhv: ").append(_uBhv.toString()).append(", ");
		msg.append("score: ").append(_score);
		return msg.toString();
	}

//	public Drawable getIcon() {
//		if(_icon == null) {
//			_icon = AppShuttleApplication.getContext().getResources().getDrawable(R.drawable.ic_launcher);
//			
//			BhvType bhvType = getBhvType();
//			switch(bhvType){
//				case APP:
//					PackageManager packageManager = AppShuttleApplication.getContext().getPackageManager();
//					String packageName = getBhvName();
//					try {
//						_icon = (BitmapDrawable) packageManager.getApplicationIcon(packageName);
//					} catch (NameNotFoundException e) {}
//					break;
//				case CALL:
//					_icon = AppShuttleApplication.getContext().getResources().getDrawable(android.R.drawable.sym_action_call);
//				case NONE:
//					;
//				default:
//					;
//			}
//		}
//		
//		return _icon;
//	}
//
//	public String getBhvNameText() {
//		if(_bhvNameText == null) {
//			_bhvNameText = "(no name)";
//			
//			BhvType bhvType = getBhvType();
//			switch(bhvType){
//				case APP:
//					PackageManager packageManager = AppShuttleApplication.getContext().getPackageManager();
//					String packageName = getBhvName();
//					try {
//						ApplicationInfo ai = packageManager.getApplicationInfo(packageName, 0);
//						_bhvNameText = (String) (ai != null ? packageManager.getApplicationLabel(ai) : "no name");
//					} catch (NameNotFoundException e) {}
//					break;
//				case CALL:
//					_bhvNameText = (String) getMeta("cachedName");
//					break;
//				case NONE:
//					break;
//				default:
//					;
//				}
//		}
//		
//		return _bhvNameText;
//	}
//
//	public String getViewMsg() {
//		if(_viewMsg == null) {
//			List<MatcherType> matcherTypeList = new ArrayList<MatcherType>(getMatchedResultMap().keySet());
//			Collections.sort(matcherTypeList, new MatcherTypeComparator());
//			
//			StringBuffer msg = new StringBuffer();
//			for (MatcherType matcherType : matcherTypeList) {
//				msg.append(matcherType.viewMsg).append(", ");
//			}
//			msg.delete(msg.length() - 2, msg.length());
//			_viewMsg = msg.toString();
//		}
//		
//		return _viewMsg;
//	}
//	
//	public Intent getLaunchIntent() {
//		if(_launchIntent == null) {
//			_launchIntent = new Intent();
//			
//			BhvType bhvType = getBhvType();
//			String bhvName = getBhvName();
//			switch(bhvType){
//				case APP:
//					PackageManager packageManager = AppShuttleApplication.getContext().getPackageManager();
//					_launchIntent = packageManager.getLaunchIntentForPackage(bhvName);
//					break;
//				case CALL:
//					_launchIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel: "+ bhvName));
//					break;
//				case NONE:
//					break;
//				default:
//					;
//			}
//		}
//		
//		return _launchIntent;
//	}
}
