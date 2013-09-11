package lab.davidahn.appshuttle;

import java.util.Set;

import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import android.app.Application;

public class AppShuttleApplication extends Application {
	private SnapshotUserCxt currUserCxt;
	private Set<UserBhv> recentPredictedBhvSet;
	private Set<UserBhv> recentPredictedBhvSetForView;
	
	public SnapshotUserCxt getCurrUserCxt() {
		return currUserCxt;
	}
	public void setCurrUserCxt(SnapshotUserCxt currUserCxt) {
		this.currUserCxt = currUserCxt;
	}
	public Set<UserBhv> getRecentPredictedBhvSet() {
		return recentPredictedBhvSet;
	}
	public void setRecentPredictedBhvSet(Set<UserBhv> recentPredictedBhvSet) {
		this.recentPredictedBhvSet = recentPredictedBhvSet;
	}
	public Set<UserBhv> getRecentPredictedBhvSetForView() {
		return recentPredictedBhvSetForView;
	}
	public void setRecentPredictedBhvSetForView(
			Set<UserBhv> recentPredictedBhvSetForView) {
		this.recentPredictedBhvSetForView = recentPredictedBhvSetForView;
	}
}
