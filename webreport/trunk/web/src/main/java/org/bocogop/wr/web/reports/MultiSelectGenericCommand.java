package org.bocogop.wr.web.reports;

import org.apache.commons.lang3.builder.CompareToBuilder;

public class MultiSelectGenericCommand<T,M> implements Comparable<MultiSelectGenericCommand<T,M>> {

	private T id;

	private M displayName;
	
	public MultiSelectGenericCommand(){
		
	}
	
	public MultiSelectGenericCommand(T id, M name) {
		this.id = id;
		this.displayName = name;
	}

	public T getId() {
		return id;
	}

	public M getDisplayName() {
		return displayName;
	}

	public void setId(T id) {
		this.id = id;
	}

	public void setDisplayName(M displayName) {
		this.displayName = displayName;
	}

	@Override
	public int compareTo(MultiSelectGenericCommand<T,M> u) {
		if (equals(u))
			return 0;

		return new CompareToBuilder().append(getId(), u.getId()).toComparison() > 0 ? 1 : -1;
	}

}