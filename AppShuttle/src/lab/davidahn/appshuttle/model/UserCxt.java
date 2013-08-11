package lab.davidahn.appshuttle.model;

public class UserCxt {
	private UserEnv userEnv;
	private UserBhv userBhv;
	
	public UserCxt(UserEnv userEnv, UserBhv userBhv){
		this.userEnv = userEnv;
		this.userBhv = userBhv;
	}
	
	public UserEnv getUserEnv() {
		return userEnv;
	}
	public UserBhv getUserBhv() {
		return userBhv;
	}
	public String toString(){
		return userEnv.toString()+", "+userBhv.toString();
	}
}
