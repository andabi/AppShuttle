package lab.davidahn.appshuttle.predict.matcher.time;

import lab.davidahn.appshuttle.predict.matcher.MatcherGroup;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;


/* 
 * TODO: 불필요한 클래스
 * - 타입만 다른 클래스가 있는 것보다
 * 생성자에 타입(혹은 이름)을 넣는 편히 코드 관리가 편함.
 */
public class TimeMatcherGroup extends MatcherGroup {

	@Override
	public MatcherType getType() {
		return MatcherType.TIME;
	}
}
