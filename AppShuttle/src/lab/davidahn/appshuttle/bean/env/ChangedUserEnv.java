package lab.davidahn.appshuttle.bean.env;

import java.util.Date;

public class ChangedUserEnv<T> {
	private Date time;
	private UserEnv userEnv;
	private T fromVal;
	private T toVal;
}
