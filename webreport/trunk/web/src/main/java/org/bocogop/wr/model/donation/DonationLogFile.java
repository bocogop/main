package org.bocogop.wr.model.donation;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.BatchSize;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.bocogop.shared.model.core.AbstractIdentifiedPersistent;
import org.bocogop.wr.util.DateUtil;

@Entity
@Table(name = "DonationLogFile", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class DonationLogFile extends AbstractIdentifiedPersistent<DonationLogFile>
		implements Comparable<DonationLogFile> {
	private static final long serialVersionUID = 2676191929993376944L;

	// -------------------------------------- Fields

	private LocalDate fileDate;
	private String fileContents;

	private List<DonationLog> donations;
	
	// -------------------------------------- Business Methods

	// ---------------------------------------- Common Methods

	@Override
	public int compareTo(DonationLogFile oo) {
		if (equals(oo))
			return 0;

		return new CompareToBuilder().append(getFileDate(), oo.getFileDate()).toComparison() > 0 ? 1 : -1;
	}

	@Override
	protected boolean requiredEquals(DonationLogFile oo) {
		return new EqualsBuilder().append(getFileDate(), oo.getFileDate())
				.append(getFileContents(), oo.getFileContents()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getFileDate()).toHashCode();
	}

	@Override
	public String toString() {
		return "DonationLogFile for " + getFileDate().format(DateUtil.DATE_ONLY_FORMAT);
	}

	// -------------------------------------- Accessor Methods

	@Column(nullable = false)
	public LocalDate getFileDate() {
		return fileDate;
	}

	public void setFileDate(LocalDate fileDate) {
		this.fileDate = fileDate;
	}

	@Column(nullable = false)
	public String getFileContents() {
		return fileContents;
	}

	public void setFileContents(String fileContents) {
		this.fileContents = fileContents;
	}

	@OneToMany(mappedBy = "donationLogFile", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@BatchSize(size = 500)
	@JsonIgnore
	
	public List<DonationLog> getDonations() {
		if (donations == null) donations = new ArrayList<>();
		return donations;
	}

	public void setDonations(List<DonationLog> donations) {
		this.donations = donations;
	}
	
	

}
