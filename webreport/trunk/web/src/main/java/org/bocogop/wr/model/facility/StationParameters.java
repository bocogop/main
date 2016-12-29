package org.bocogop.wr.model.facility;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.wr.model.lookup.Language;

@Entity
@Table(name = "VoluntaryStationParameters", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class StationParameters extends AbstractAuditedVersionedPersistent<StationParameters> {
	private static final long serialVersionUID = 7608953991510510186L;

	// -------------------------------------- Fields

	@NotNull
	private Facility facility;
	private String introductoryText;
	private String alternateLanguageIntroText;
	private Boolean requiresAlternateLanguage;
	private Language language;
	private Language alternateLanguage;
	private Integer numberOfMeals;
	private String mealAuthorization; // may be a boolean
	private Boolean saturdayMeal;
	private Boolean sundayMeal;
	private Boolean holidayMeal;
	private BigDecimal mealPrice;
	private BigDecimal meal1Duration;
	private String meal1CutoffTime;
	private BigDecimal meal2Duration;
	private String meal2CutoffTime;
	private BigDecimal meal3Duration;
	private String meal3CutoffTime;
	private String voluntarySignatureLine;
	private String directorSignatureLine;
	private BigDecimal valueAmount;
	private Boolean vaLetterHead;
	private String startServiceCode;
	private String endServiceCode;

	// -------------------------------------- Constructors

	public StationParameters() {
	}

	/**
	 * Constructor for new StationParameters - populates parent reference(s) and
	 * creates new empty @NotNull children
	 * 
	 * @param facility
	 */
	public StationParameters(Facility facility) {
		this.facility = facility;
	}

	// -------------------------------------- Business Methods

	// ---------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(StationParameters oo) {
		return new EqualsBuilder().append(nullSafeGetId(getFacility()), nullSafeGetId(oo.getFacility())).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(getFacility())).toHashCode();
	}

	// -------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "FacilityFK", nullable = false, unique = true)
	@JsonIgnore
	public Facility getFacility() {
		return facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	@Column(length = 240)
	public String getIntroductoryText() {
		return introductoryText;
	}

	public void setIntroductoryText(String introductoryText) {
		this.introductoryText = introductoryText;
	}

	@Column(length = 240)
	public String getAlternateLanguageIntroText() {
		return alternateLanguageIntroText;
	}

	public void setAlternateLanguageIntroText(String alternateLanguageIntroText) {
		this.alternateLanguageIntroText = alternateLanguageIntroText;
	}

	@Column(name = "IsRequireAlternateLanguage")
	public Boolean getRequiresAlternateLanguage() {
		return requiresAlternateLanguage;
	}

	public void setRequiresAlternateLanguage(Boolean requiresAlternateLanguage) {
		this.requiresAlternateLanguage = requiresAlternateLanguage;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WrDefaultLanguageFK")
	public Language getLanguage() {
		return language;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WrAlternateLanguageFK")
	public Language getAlternateLanguage() {
		return alternateLanguage;
	}

	public void setAlternateLanguage(Language alternateLanguage) {
		this.alternateLanguage = alternateLanguage;
	}

	@Column(name = "NumberOfMeals")
	public Integer getNumberOfMeals() {
		return numberOfMeals;
	}

	public void setNumberOfMeals(Integer numberOfMeals) {
		this.numberOfMeals = numberOfMeals;
	}

	@Column(name = "MealAuthorization", length = 1)
	public String getMealAuthorization() {
		return mealAuthorization;
	}

	public void setMealAuthorization(String mealAuthorization) {
		this.mealAuthorization = mealAuthorization;
	}

	@Column(name = "IsSaturdayMeal")
	public Boolean getSaturdayMeal() {
		return saturdayMeal;
	}

	public void setSaturdayMeal(Boolean saturdayMeal) {
		this.saturdayMeal = saturdayMeal;
	}

	@Column(name = "IsSundayMeal")
	public Boolean getSundayMeal() {
		return sundayMeal;
	}

	public void setSundayMeal(Boolean sundayMeal) {
		this.sundayMeal = sundayMeal;
	}

	@Column(name = "IsHolidayMeal")
	public Boolean getHolidayMeal() {
		return holidayMeal;
	}

	public void setHolidayMeal(Boolean holidayMeal) {
		this.holidayMeal = holidayMeal;
	}

	@Column(name = "MealPrice")
	public BigDecimal getMealPrice() {
		return mealPrice;
	}

	public void setMealPrice(BigDecimal mealPrice) {
		this.mealPrice = mealPrice;
	}

	@Column(name = "DurationForMeal1")
	public BigDecimal getMeal1Duration() {
		return meal1Duration;
	}

	public void setMeal1Duration(BigDecimal meal1Duration) {
		this.meal1Duration = meal1Duration;
	}

	@Column(name = "CutOffTimeForMeal1", length = 4)
	public String getMeal1CutoffTime() {
		return meal1CutoffTime;
	}

	public void setMeal1CutoffTime(String meal1CutoffTime) {
		this.meal1CutoffTime = meal1CutoffTime;
	}

	@Column(name = "DurationForMeal2")
	public BigDecimal getMeal2Duration() {
		return meal2Duration;
	}

	public void setMeal2Duration(BigDecimal meal2Duration) {
		this.meal2Duration = meal2Duration;
	}

	@Column(name = "CutOffTimeForMeal2", length = 4)
	public String getMeal2CutoffTime() {
		return meal2CutoffTime;
	}

	public void setMeal2CutoffTime(String meal2CutoffTime) {
		this.meal2CutoffTime = meal2CutoffTime;
	}

	@Column(name = "DurationForMeal3")
	public BigDecimal getMeal3Duration() {
		return meal3Duration;
	}

	public void setMeal3Duration(BigDecimal meal3Duration) {
		this.meal3Duration = meal3Duration;
	}

	@Column(name = "CutOffTimeForMeal3", length = 4)
	public String getMeal3CutoffTime() {
		return meal3CutoffTime;
	}

	public void setMeal3CutoffTime(String meal3CutoffTime) {
		this.meal3CutoffTime = meal3CutoffTime;
	}

	@Column(length = 50)
	public String getVoluntarySignatureLine() {
		return voluntarySignatureLine;
	}

	public void setVoluntarySignatureLine(String voluntarySignatureLine) {
		this.voluntarySignatureLine = voluntarySignatureLine;
	}

	@Column(length = 50)
	public String getDirectorSignatureLine() {
		return directorSignatureLine;
	}

	public void setDirectorSignatureLine(String directorySignatureLine) {
		this.directorSignatureLine = directorySignatureLine;
	}

	public BigDecimal getValueAmount() {
		return valueAmount;
	}

	public void setValueAmount(BigDecimal valueAmount) {
		this.valueAmount = valueAmount;
	}

	@Column(name = "IsVALetterHead")
	public Boolean getVaLetterHead() {
		return vaLetterHead;
	}

	public void setVaLetterHead(Boolean vaLetterHead) {
		this.vaLetterHead = vaLetterHead;
	}

	@Column(name = "StartSvcCode", length = 3)
	public String getStartServiceCode() {
		return startServiceCode;
	}

	public void setStartServiceCode(String startServiceCode) {
		this.startServiceCode = startServiceCode;
	}

	@Column(name = "EndSvcCode", length = 3)
	public String getEndServiceCode() {
		return endServiceCode;
	}

	public void setEndServiceCode(String endServiceCode) {
		this.endServiceCode = endServiceCode;
	}

}
