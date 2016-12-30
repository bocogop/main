package org.bocogop.wr.model.voter;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class VoterDemographics extends AbstractSimpleVoter<VoterDemographics> {
	private static final long serialVersionUID = -556040665741732416L;

	// -------------------------------------------- Fields

	private String state;
	private Long stateId;
	private String gender;
	private String status;
	private LocalDate statusDate;
	private Long primaryPrecinctId;
	private String primaryPrecinctName;
	private LocalDate entryDate;
	private List<String> combinedAssignments = new ArrayList<>();
	private List<String> supervisors = new ArrayList<>();
	private List<String[]> combinedParkingStickers = new ArrayList<>();
	private List<String[]> combinedUniforms = new ArrayList<>();
	private LocalDate lastVoteredDate;
	private double currentYearHours;
	private double priorHours;
	private double adjustedHours;
	private double totalHours;
	private BigDecimal totalDonations;
	private Double hoursLastAward;
	private LocalDate dateLastAward;
	private String primaryOrganization;

	public VoterDemographics(long id, String identifyingCode, String lastName, String firstName, String middleName,
			String nameSuffix, LocalDate dateOfBirth, int age, boolean youth, String nickname, String gender,
			String status, LocalDate statusDate, String addressLine1, String addressLine2, String city, String state,
			Long stateId, String zip, String combinedParkingStickers, String combinedUniforms, String phone,
			String phoneAlt, String phoneAlt2, String email, String emergencyContactName,
			String emergencyContactRelationship, String emergencyContactPhone, String emergencyContactPhoneAlt,
			Long primaryPrecinctId, String primaryPrecinctName, LocalDate entryDate, String combinedAssignments,
			LocalDate lastVoteredDate, double currentYearHours, double priorHours, double adjustedHours,
			double totalHours, BigDecimal totalDonations, Double hoursLastAward, LocalDate dateLastAward,
			String primaryOrganization) {
		super(id, identifyingCode, lastName, firstName, middleName, nameSuffix, dateOfBirth, age, youth, nickname,
				addressLine1, addressLine2, city, zip, phone, phoneAlt, phoneAlt2, email, emergencyContactName,
				emergencyContactRelationship, emergencyContactPhone, emergencyContactPhoneAlt);
		this.state = state;
		this.stateId = stateId;
		this.gender = gender;
		this.status = status;
		this.statusDate = statusDate;
		this.primaryPrecinctId = primaryPrecinctId;
		this.primaryPrecinctName = primaryPrecinctName;
		this.entryDate = entryDate;
		this.lastVoteredDate = lastVoteredDate;
		this.currentYearHours = currentYearHours;
		this.priorHours = priorHours;
		this.adjustedHours = adjustedHours;
		this.totalHours = totalHours;
		this.totalDonations = totalDonations;
		this.hoursLastAward = hoursLastAward;
		this.dateLastAward = dateLastAward;
		this.primaryOrganization = primaryOrganization;

		if (combinedAssignments != null) {
			String[] assignmentsAndSupervisors = combinedAssignments.split(";;");
			for (String assignmentAndSupervisor : assignmentsAndSupervisors) {
				this.combinedAssignments.add(assignmentAndSupervisor.split("\\|")[0]);
				this.supervisors.add(assignmentAndSupervisor);
			}
		}

		if (combinedParkingStickers != null) {
			String[] parkingStickers = combinedParkingStickers.split(";");
			for (String parkingSticker : parkingStickers) {
				this.combinedParkingStickers.add(parkingSticker.split("\\|", -1));
			}
		}

		if (combinedUniforms != null) {
			String[] uniforms = combinedUniforms.split(";");
			for (String uniform : uniforms) {
				this.combinedUniforms.add(uniform.split("\\|", -1));
			}
		}
	}

	// -------------------------------------------- Business Methods

	@Override
	protected String getStateString() {
		return state;
	}

	@Override
	protected Long getStateId() {
		return stateId;
	}

	// -------------------------------------------- Accessor Methods

	public String getState() {
		return state;
	}

	public String getGender() {
		return gender;
	}

	public String getStatus() {
		return status;
	}

	public LocalDate getStatusDate() {
		return statusDate;
	}

	public Long getPrimaryPrecinctId() {
		return primaryPrecinctId;
	}

	public String getPrimaryPrecinctName() {
		return primaryPrecinctName;
	}

	public LocalDate getEntryDate() {
		return entryDate;
	}

	public List<String> getCombinedAssignments() {
		return combinedAssignments;
	}

	public List<String> getSupervisors() {
		return supervisors;
	}

	public LocalDate getLastVoteredDate() {
		return lastVoteredDate;
	}

	public double getCurrentYearHours() {
		return currentYearHours;
	}

	public double getPriorHours() {
		return priorHours;
	}

	public double getAdjustedHours() {
		return adjustedHours;
	}

	public double getTotalHours() {
		return totalHours;
	}

	public Double getHoursLastAward() {
		return hoursLastAward;
	}

	public LocalDate getDateLastAward() {
		return dateLastAward;
	}

	public String getPrimaryOrganization() {
		return primaryOrganization;
	}

	public List<String[]> getCombinedParkingStickers() {
		return combinedParkingStickers;
	}

	public List<String[]> getCombinedUniforms() {
		return combinedUniforms;
	}

	public BigDecimal getTotalDonations() {
		return totalDonations;
	}

}
