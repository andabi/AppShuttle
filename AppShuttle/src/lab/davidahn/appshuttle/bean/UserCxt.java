package lab.davidahn.appshuttle.bean;

import java.util.ArrayList;
import java.util.List;

public class UserCxt {
	private UserEnv userEnv;
	private List<UserBhv> userBhvs = new ArrayList<UserBhv>();
	
	public UserCxt(UserEnv userEnv){
		this.userEnv = userEnv;
	}
	public UserEnv getUserEnv() {
		return userEnv;
	}
	public List<UserBhv> getUserBhvs() {
		return userBhvs;
	}
	public void addUserBhv(UserBhv userBhv) {
		userBhvs.add(userBhv);
	}
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("userEnv: ").append(userEnv.toString());
		msg.append("userBhvs: ").append(userBhvs.toString());
		return msg.toString();
	}
}
