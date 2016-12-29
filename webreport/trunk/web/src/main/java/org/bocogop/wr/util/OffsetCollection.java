package org.bocogop.wr.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OffsetCollection<T> implements Serializable {
	private static final long serialVersionUID = 3248982264873196487L;

	private List<T> items;
	private int startIndex;
	private boolean lastPage;

	public OffsetCollection(List<T> items, int startIndex, boolean lastPage) {
		this.items = items;
		this.startIndex = startIndex;
		this.lastPage = lastPage;
	}

	public List<T> getPage(int fromIndex, int length) {
		if (length == 0)
			return new ArrayList<T>();

		int finalFrom = fromIndex - startIndex;
		int finalTo = fromIndex + length - startIndex;
		if (lastPage)
			finalTo = Math.min(items.size(), finalTo);

		if (finalFrom < 0 || finalFrom >= items.size() || finalTo < 0 || finalTo > items.size())
			return null;
		return items.subList(finalFrom, finalTo);
	}

}
