package lab.davidahn.appshuttle.commons;

import java.util.Collection;
import java.util.LinkedList;

public class NoDuplicatesList<E> extends LinkedList<E> {
	private static final long serialVersionUID = 1L;

	@Override
    public boolean add(E e) {
        if (this.contains(e)) {
            return false;
        }
        else {
            return super.add(e);
        }
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        Collection<E> copy = new LinkedList<E>(collection);
        copy.removeAll(this);
        return super.addAll(copy);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> collection) {
        Collection<E> copy = new LinkedList<E>(collection);
        copy.removeAll(this);
        return super.addAll(index, copy);
    }

    @Override
    public void add(int index, E element) {
        if (this.contains(element)) {
            return;
        }
        else {
            super.add(index, element);
        }
    }
}   