/*
 * Copyright 2020-2022 Siphalor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

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
