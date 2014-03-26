package lab.davidahn.appshuttle.predict.datasource;

import java.util.List;
import java.util.Map;

public class TransitionDataSource extends DataSource <Map<String, ?>>{

	@Override
	public List<Map<String, ?>> getList(long fromTime, long untilTime) {
		
		// TODO: DAO 를 읽어서 Env-Bhv 쌍을 만든다.
		
		// TODO: Env-Bhv 쌍을 Map 에 저장한다.
		
		/* Out 예시
		 * "time" : long
		 * "timeFrom" : long
		 * "timeUntil" : long
		 * "day" : String or Enum (요일)
		 * "weekend" : true or false / 1 or 0
		 * "headset" : true or false / 1 or 0
		 * ...
		 * "userBhv" : UserBhv
		 */
		
		return null;
	}
}
