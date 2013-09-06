package lab.davidahn.appshuttle;

import java.util.Set;

import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import android.app.Application;

public class AppShuttleApplication extends Application {
	private SnapshotUserCxt currUserCxt;
	private Set<UserBhv> recentMatchedBhvSet;
	
	public SnapshotUserCxt getCurrUserCxt() {
		return currUserCxt;
	}
	public void setCurrUserCxt(SnapshotUserCxt currUserCxt) {
		this.currUserCxt = currUserCxt;
	}
	public Set<UserBhv> getRecentMatchedBhvSet() {
		return recentMatchedBhvSet;
	}
	public void setRecentMatchedBhvSet(Set<UserBhv> recentMatchedBhvSet) {
		this.recentMatchedBhvSet = recentMatchedBhvSet;
	}
}
