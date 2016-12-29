package org.bocogop.wr.model.award;

import java.time.LocalDate;

public class AwardResult {

	// ----------------------------------- Fields
	
	private Long volunteerId;
	private Long deservedAwardId;
	private String volLastName;
	private String VolFirstName;
	private LocalDate dateLastAward;
	private String currentAwardName;
	private Long currentAwardHours;
	private Long hoursLastAward;
	private String deservedAwardName;
	private Long deservedAwardHours;
	private Long actualHours;
	private Long yearsWorked;
	private String volStatus;
	private String ageStatus;
	private Long aveHours;
	private boolean grandAwardChecked;
	private LocalDate dateLastVolunteered;
	private String awardType;
			
	// ----------------------------------- Constructors

	public AwardResult() {
	}

	// ----------------------------------- Accessor Methods
	
	public Long getVolunteerId() {
		return volunteerId;
	}

	public void setVolunteerId(Long volunteerId) {
		this.volunteerId = volunteerId;
	}

	public String getVolLastName() {
		return volLastName;
	}

	public void setVolLastName(String volLastName) {
		this.volLastName = volLastName;
	}

	public String getVolFirstName() {
		return VolFirstName;
	}

	public void setVolFirstName(String volFirstName) {
		VolFirstName = volFirstName;
	}

	public LocalDate getDateLastAward() {
		return dateLastAward;
	}

	public void setDateLastAward(LocalDate dateLastAward) {
		this.dateLastAward = dateLastAward;
	}

	public Long getHoursLastAward() {
		return hoursLastAward;
	}

	public void setHoursLastAward(Long hoursLastAward) {
		this.hoursLastAward = hoursLastAward;
	}

	public String getDeservedAwardName() {
		return deservedAwardName;
	}

	public void setDeservedAwardName(String deservedAwardName) {
		this.deservedAwardName = deservedAwardName;
	}

	public Long getCurrentAwardHours() {
		return currentAwardHours;
	}

	public void setCurrentAwardHours(Long currentAwardHours) {
		this.currentAwardHours = currentAwardHours;
	}

	public Long getActualHours() {
		return actualHours;
	}

	public void setActualHours(Long actualHours) {
		this.actualHours = actualHours;
	}

	public String getVolStatus() {
		return volStatus;
	}

	public void setVolStatus(String volStatus) {
		this.volStatus = volStatus;
	}

	public Long getDeservesAwardHours() {
		return deservedAwardHours;
	}

	public void setDeservesAwardHours(Long deservesAwardHours) {
		this.deservedAwardHours = deservesAwardHours;
	}

	public Long getYearsWorked() {
		return yearsWorked;
	}

	public void setYearsWorked(Long yearsWorked) {
		this.yearsWorked = yearsWorked;
	}

	public boolean isGrandAwardChecked() {
		return grandAwardChecked;
	}

	public void setGrandAwardChecked(boolean grandAwardChecked) {
		this.grandAwardChecked = grandAwardChecked;
	}

	public Long getDeservedAwardId() {
		return deservedAwardId;
	}

	public void setDeservedAwardId(Long deservedAwardId) {
		this.deservedAwardId = deservedAwardId;
	}

	
	public String getCurrentAwardName() {
		return currentAwardName;
	}

	public void setCurrentAwardName(String currentAwardName) {
		this.currentAwardName = currentAwardName;
	}

	public Long getDeservedAwardHours() {
		return deservedAwardHours;
	}

	public void setDeservedAwardHours(Long deservedAwardHours) {
		this.deservedAwardHours = deservedAwardHours;
	}

	public String getAgeStatus() {
		return ageStatus;
	}

	public void setAgeStatus(String ageStatus) {
		this.ageStatus = ageStatus;
	}

	public Long getAveHours() {
		 if(this.yearsWorked != 0)
			 return Math.round(this.actualHours.doubleValue()/(this.yearsWorked.doubleValue()*12));
	     return null;
	}

	public void setAveHours(Long aveHours) {
		this.aveHours = aveHours;
	}

	public LocalDate getDateLastVolunteered() {
		return dateLastVolunteered;
	}

	public void setDateLastVolunteered(LocalDate dateLastVolunteered) {
		this.dateLastVolunteered = dateLastVolunteered;
	}

	public String getAwardType() {
		return awardType;
	}

	public void setAwardType(String awardType) {
		this.awardType = awardType;
	}

			
}
