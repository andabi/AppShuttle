package lab.davidahn.appshuttle.mine.matcher;

import java.util.Comparator;

public class MatcherGroupTypeComparator implements Comparator<MatcherGroupType>
{
	public int compare(MatcherGroupType mt1, MatcherGroupType mt2)
	{
		if(mt1.priority < mt2.priority)
			return 1;
		else if(mt1.priority == mt2.priority)
			return 0;
		else 
			return -1;
	}
}