package org.bocogop.wr.persistence.dao.benefitingService;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;

public class BenefitingServiceRoleQuickSearchResult implements Comparable<BenefitingServiceRoleQuickSearchResult> {

	private long id;
	private String name;
	private String serviceName;
	private String serviceSubdivision;
	private String locationName;
	private boolean active;

	public BenefitingServiceRoleQuickSearchResult(long id, String name, String serviceName, String serviceSubdivision,
			String locationName, boolean active) {
		this.id = id;
		this.name = name;
		this.serviceName = serviceName;
		this.serviceSubdivision = serviceSubdivision;
		this.locationName = locationName;
		this.active = active;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BenefitingServiceRoleQuickSearchResult other = (BenefitingServiceRoleQuickSearchResult) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public int compareTo(BenefitingServiceRoleQuickSearchResult o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder()
				.append(StringUtils.lowerCase(getServiceName()), StringUtils.lowerCase(o.getServiceName()))
				.append(StringUtils.lowerCase(getServiceSubdivision()),
						StringUtils.lowerCase(o.getServiceSubdivision()))
				.append(StringUtils.lowerCase(getName()), StringUtils.lowerCase(o.getName())).append(id, o.id)
				.toComparison() > 0 ? 1 : -1;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getServiceSubdivision() {
		return serviceSubdivision;
	}

	public boolean isActive() {
		return active;
	}

	public String getLocationName() {
		return locationName;
	}

}