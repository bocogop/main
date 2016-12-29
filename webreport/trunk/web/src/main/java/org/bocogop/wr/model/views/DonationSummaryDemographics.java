package org.bocogop.wr.model.views;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Immutable;

import org.bocogop.shared.model.core.AbstractDerivedPersistent;
import org.bocogop.shared.model.lookup.sds.State;
import org.bocogop.wr.model.donation.DonationSummary;
import org.bocogop.wr.model.donation.DonorType;
import org.bocogop.wr.persistence.conversion.DonationSummaryLetterTypeConverter;

@Entity
@Immutable
@Table(name = "FinalDonationSummaryDemographics", schema = "dbo")
public class DonationSummaryDemographics extends AbstractDerivedPersistent<DonationSummaryDemographics> {
	private static final long serialVersionUID = 3254887002195956807L;

	// --------------------------------------------- Fields

	private DonationSummary donationSummary;
	private DonorType donorType;
	private String consolidatedName;
	private String donorOrganization;
	private String addressLine1;
	private String addressLine2;
	private String city;
	private State state;
	private String zip;
	private DonationSummaryLetterType letterType;

	// ---------------------------------------- Business Methods

	// ---------------------------------------- Common Methods

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getDonationSummary().getId()).toHashCode();
	}

	@Override
	protected boolean requiredEquals(DonationSummaryDemographics o) {
		return new EqualsBuilder().append(getDonationSummary().getId(), o.getDonationSummary().getId()).isEquals();
	}

	// --------------------------------------------- Accessor Methods

	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DonationSummaryId", unique = true)
	@BatchSize(size = 500)
	public DonationSummary getDonationSummary() {
		return donationSummary;
	}

	public void setDonationSummary(DonationSummary donationSummary) {
		this.donationSummary = donationSummary;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DonorTypeFK")
	@BatchSize(size = 500)
	public DonorType getDonorType() {
		return donorType;
	}

	public void setDonorType(DonorType donorType) {
		this.donorType = donorType;
	}

	public String getConsolidatedName() {
		return consolidatedName;
	}

	public void setConsolidatedName(String consolidatedName) {
		this.consolidatedName = consolidatedName;
	}

	public String getDonorOrganization() {
		return donorOrganization;
	}

	public void setDonorOrganization(String donorOrganization) {
		this.donorOrganization = donorOrganization;
	}

	@Column(name = "Address1")
	public String getAddressLine1() {
		return addressLine1;
	}

	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	@Column(name = "Address2")
	public String getAddressLine2() {
		return addressLine2;
	}

	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "StateId")
	@BatchSize(size = 500)
	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	@Column(name = "DonationTypeForLetter")
	@Convert(converter = DonationSummaryLetterTypeConverter.class)
	public DonationSummaryLetterType getLetterType() {
		return letterType;
	}

	public void setLetterType(DonationSummaryLetterType letterType) {
		this.letterType = letterType;
	}

}
