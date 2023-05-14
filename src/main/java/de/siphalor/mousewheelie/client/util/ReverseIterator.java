package de.siphalor.mousewheelie.client.util;

import java.util.Iterator;
import java.util.List;

public class ReverseIterator<T> implements Iterator<T> {
	private final List<T> backingList;
	private int index;

	private ReverseIterator(List<T> backingList) {
		this.backingList = backingList;
		this.index = backingList.size();
	}

	public static <T> ReverseIterator<T> of(List<T> backingList) {
		return new ReverseIterator<>(backingList);
	}

	@Override
	public boolean hasNext() {
		return index > 0;
	}

	@Override
	public T next() {
		return backingList.get(--index);
	}
}
