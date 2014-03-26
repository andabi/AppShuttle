package lab.davidahn.appshuttle.predict.datasource;

import java.util.List;
import java.util.Map;

public class EnvDataSource extends DataSource<Map<String,?>> {

	@Override
	public void refreshData() {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Map<String, ?>> getList(long fromTime, long untilTime) {
		/*
		 * DAO에 억세스하여,
		 * Map에 환경변수들과("headset", "weekend", "location" 등)
		 * 그 시간의 "userBhv" 를 저장.
		 * 
		 * 그 Map들의 list를 리턴
		 */
		
		return null;
	}

}
