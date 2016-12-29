package org.bocogop.wr.persistence.dao;

import java.time.LocalDate;

public class TimeSummary {
	LocalDate mostRecentWorkEntryDate;
	double priorYearHours;
	double currentYearHours;
	double totalHours;
	double adjustedHours;

	public TimeSummary(LocalDate mostRecentWorkEntryDate, double priorYearHours, double currentYearHours,
			double totalHours, double adjustedHours) {
		this.mostRecentWorkEntryDate = mostRecentWorkEntryDate;
		this.priorYearHours = priorYearHours;
		this.currentYearHours = currentYearHours;
		this.totalHours = totalHours;
		this.adjustedHours = adjustedHours;
	}

	public double getPriorYearHours() {
		return priorYearHours;
	}

	public double getCurrentYearHours() {
		return currentYearHours;
	}

	public double getTotalHours() {
		return totalHours;
	}

	public double getAdjustedHours() {
		return adjustedHours;
	}

	public LocalDate getMostRecentWorkEntryDate() {
		return mostRecentWorkEntryDate;
	}

}