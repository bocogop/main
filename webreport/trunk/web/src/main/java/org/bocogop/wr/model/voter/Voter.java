package org.bocogop.wr.model.voter;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.bocogop.wr.model.lookup.Language;
import org.bocogop.wr.model.precinct.Precinct;
import org.bocogop.wr.util.DateUtil;
import org.hibernate.annotations.BatchSize;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

/**
 * @author Connor
 *
 */
@Entity
@Table(name = "Voters", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class Voter extends AbstractVoter<Voter> {
	private static final long serialVersionUID = 6904844123870655771L;

	public static class VoterView {
		public interface Basic {
		}

		public interface Search extends Basic {
		}

		public interface Extended extends Search {
		}

		public interface Demographics extends Basic {
		}
	}

	// -------------------------------------- Fields

	private Language preferredLanguage;
	private String remarks;
	@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY)
	private LocalDate entryDate;

	private boolean vaEmployee;
	private String vaEmployeeUsername;

	private Integer lastAwardHours;
	@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY)
	private LocalDate lastAwardDate;

	@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY)
	private LocalDate statusDate;

	private Integer mealsEligible;
	private String mealRemarks;

	private Precinct originallyCreatedAt;
	private Precinct primaryPrecinct;

	private String pivBadgeID;
	private LocalDate pivExpiration;

	// -------------------------------------- Constructors

	public Voter() {
	}

	/**
	 * Convenience constructor for when we just want to create a dummy object
	 * for the UI (e.g. Notifications)
	 */
	public Voter(long id, String lastName, String firstName, String middleName, String nameSuffix) {
		super(id, lastName, firstName, middleName, nameSuffix);
	}

	// -------------------------------------- Business Methods

	// -------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PreferredLanguageForLoginFK")
	@JsonView(VoterView.Extended.class)
	public Language getPreferredLanguage() {
		return preferredLanguage;
	}

	public void setPreferredLanguage(Language preferredLanguage) {
		this.preferredLanguage = preferredLanguage;
	}

	@JsonView(VoterView.Extended.class)
	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	@Column(name = "VAEmployee", nullable = false)
	@JsonView(VoterView.Extended.class)
	public boolean isVaEmployee() {
		return vaEmployee;
	}

	public void setVaEmployee(boolean vaEmployee) {
		this.vaEmployee = vaEmployee;
	}

	@JsonView(VoterView.Extended.class)
	public String getVaEmployeeUsername() {
		return vaEmployeeUsername;
	}

	public void setVaEmployeeUsername(String vaEmployeeUsername) {
		this.vaEmployeeUsername = vaEmployeeUsername;
	}

	@Column(name = "HoursLastAward")
	@JsonView(VoterView.Extended.class)
	public Integer getLastAwardHours() {
		return lastAwardHours;
	}

	public void setLastAwardHours(Integer lastAwardHours) {
		this.lastAwardHours = lastAwardHours;
	}

	@JsonView(VoterView.Search.class)
	@Column(insertable = false, updatable = false)
	public LocalDate getStatusDate() {
		return statusDate;
	}

	public void setStatusDate(LocalDate statusDate) {
		this.statusDate = statusDate;
	}

	@Column(name = "EligibleForNumMeals")
	@JsonView(VoterView.Extended.class)
	public Integer getMealsEligible() {
		return mealsEligible;
	}

	public void setMealsEligible(Integer mealsEligible) {
		this.mealsEligible = mealsEligible;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PrimaryPrecinctFK", insertable = false, updatable = false)
	@BatchSize(size = 500)
	@JsonView({ VoterView.Basic.class, //
	})
	public Precinct getPrimaryPrecinct() {
		return primaryPrecinct;
	}

	/* Managed by a native method in VoterDAOImpl */
	@SuppressWarnings("unused")
	private void setPrimaryPrecinct(Precinct primaryPrecinct) {
		this.primaryPrecinct = primaryPrecinct;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "OriginalPrecinctCreatedFK", nullable = false)
	@BatchSize(size = 500)
	@JsonView(VoterView.Extended.class)
	public Precinct getOriginallyCreatedAt() {
		return originallyCreatedAt;
	}

	public void setOriginallyCreatedAt(Precinct originallyCreatedAt) {
		this.originallyCreatedAt = originallyCreatedAt;
	}

	@JsonView(VoterView.Extended.class)
	public LocalDate getEntryDate() {
		return entryDate;
	}

	public void setEntryDate(LocalDate entryDate) {
		this.entryDate = entryDate;
	}

	@Column(name = "PIVBadgeID", length = 100)
	@JsonView(VoterView.Extended.class)
	public String getPivBadgeID() {
		return pivBadgeID;
	}

	public void setPivBadgeID(String pivBadgeID) {
		this.pivBadgeID = pivBadgeID;
	}

	@Column(name = "PIVExpiration")
	@JsonView(VoterView.Extended.class)
	public LocalDate getPivExpiration() {
		return pivExpiration;
	}

	public void setPivExpiration(LocalDate pivExpiration) {
		this.pivExpiration = pivExpiration;
	}

	@JsonIgnore
	public String getMealRemarks() {
		return mealRemarks;
	}

	public void setMealRemarks(String mealRemarks) {
		this.mealRemarks = mealRemarks;
	}

}
