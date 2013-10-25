package lab.davidahn.appshuttle.mine.matcher;

import java.util.Comparator;

public class MatcherTypeComparator implements Comparator<MatcherType>
{
	public int compare(MatcherType mt1, MatcherType mt2)
	{
		if(mt1.priority < mt2.priority)
			return 1;
		else if(mt1.priority == mt2.priority)
			return 0;
		else 
			return -1;
	}
}