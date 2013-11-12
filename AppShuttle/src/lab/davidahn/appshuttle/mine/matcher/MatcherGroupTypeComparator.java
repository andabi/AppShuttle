package lab.davidahn.appshuttle.mine.matcher;

import java.util.Comparator;

public class MatcherGroupTypeComparator implements Comparator<MatcherGroupType>
{
	public int compare(MatcherGroupType mgt1, MatcherGroupType mgt2)
	{
		if(mgt1.priority < mgt2.priority)
			return 1;
		else if(mgt1.priority == mgt2.priority)
			return 0;
		else 
			return -1;
	}
}