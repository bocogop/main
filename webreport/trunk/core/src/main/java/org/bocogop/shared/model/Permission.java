package org.bocogop.shared.model;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.model.lookup.AbstractLookup;
import org.bocogop.shared.model.lookup.LookupType;

@Entity
@Table(name = "PERMISSION", schema = "CORE")
@AttributeOverrides({ @AttributeOverride(name = "id", column = @Column(name = "PERMISSION_ID")) })
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
@Immutable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class Permission extends AbstractLookup<Permission, PermissionType> implements GrantedAuthority {
	private static final long serialVersionUID = -3175355751589521985L;

	// --------------------------------------------- Static Fields

	public static final String AWARD_CODE_CREATE = "Create Award Code";
	public static final String BENEFITING_SERVICE_CREATE = "Create Benefiting Services";
	public static final String BENEFITING_SERVICE_TEMPLATE_MANAGE = "Create Benefiting Service Templates";
	public static final String DONATION_CREATE = "Create Donation";
	public static final String DONATION_READ = "Read Donation";
	public static final String DONATION_RECEIPT_PRINT = "Print Donation Receipt";
	public static final String EDONATION_MANAGE = "Manage Edonation Notification";
	public static final String EXCLUDED_ENTITY_VIEW_LOCAL = "View Local LEIE Matches";
	public static final String EXCLUDED_ENTITY_VIEW_ALL = "View All LEIE Matches";
	public static final String EXPENDITURE_CREATE = "Create Expenditure";
	public static final String EXPENDITURE_DELETE = "Delete Expenditure";
	public static final String FACILITY_CREATE = "Create Facility";
	public static final String FACILITY_EDIT_CURRENT = "Edit Current Facility";
	public static final String FACILITY_EDIT_ALL = "Edit All Facilities";
	public static final String LOCAL_BRANCH_CREATE = "Create Local Branch";
	public static final String LOGIN_APPLICATION = "Login Staff Application";
	public static final String LOGIN_KIOSK = "Login Kiosk Application";
	public static final String MEALTICKET_CREATE = "Create Meal Ticket";
	public static final String MEALTICKET_READ = "Read Meal Ticket";
	public static final String MERGE_DONOR = "Merge Donor";
	public static final String NPDB_EXECUTE = "Execute NPDB Check";
	public static final String NPDB_REQUEST = "Request NPDB Check";
	public static final String ORG_CODE_NATIONAL_CREATE = "Create National Organization Codes";
	public static final String ORG_CODE_NATIONAL_READ = "Read National Organization Codes";
	public static final String ORG_CODE_LOCAL_CREATE = "Create Local Organization Codes";
	public static final String ORG_CODE_LOCAL_READ = "Read Local Organization Codes";
	public static final String PERM_CODE_SERVICE_CREATE = "Create Voluntary Services";
	public static final String PERM_CODE_SERVICE_READ = "Read Voluntary Services";
	public static final String REQUIREMENTS_LOCAL_MANAGE = "Create Local Requirement";
	public static final String REQUIREMENTS_GLOBAL_MANAGE = "Create National Requirement";

	public static final String RUN_BENEFITING_SERVIVE_LISTING = "Run Benefiting Service Listing";
	public static final String RUN_COMMITTEE_ATTENDANCE_LISTING = "Run Committee Attendance Listing";
	public static final String RUN_DONATION_MEMORANDUM = "Run Donation Memorandum";
	public static final String RUN_DONATION_RECEIPT = "Run Donation Receipt";
	public static final String RUN_DONATION_THANK_YOU_LETTER = "Run Donation Thank You Letter";
	public static final String RUN_GRAND_TOTAL_DONATIONS = "Run Grand Total Donations";
	public static final String RUN_MEAL_TICKET_REPORT = "Run Meal Ticket Report";
	public static final String RUN_NATIONAL_OFFICIAL_ADDRESS_LABELS = "Run National Official Address Labels";
	public static final String RUN_NATIONAL_OFFICIAL_LISTING = "Run National Officials Listing";
	public static final String RUN_NEW_VOLUNTEERS = "Run New Volunteers";
	public static final String RUN_ORGANIZATION_LISTING = "Run Organization Listing";
	public static final String RUN_VOL_ORGS_RS_OCC_HOURS = "Run Vol Orgs RS and Occ Hours";
	public static final String RUN_SCHEDULED_OCCAS_HOURS = "Run Regular Scheduled and Occasional Hours";
	public static final String RUN_VOLUNTEER_ADDRESS_LABELS = "Run Volunteer Address Labels";
	public static final String RUN_VOLUNTEER_ALPHA = "Run Volunteer Alphabetical";
	public static final String RUN_VOLUNTEERS_BY_ORG = "Run Volunteers By Organization";
	public static final String RUN_VOLUNTEERS_BY_SERVICE = "Run Volunteers By Service";
	public static final String RUN_VOLUNTARY_SERVICE_ADDRESS_LABELS = "Run Voluntary Service Address Labels";
	public static final String RUN_VOLUNTARY_SERVICE_DIRECTORY = "Run Voluntary Service Directory";
	public static final String STAFF_TITLE_CREATE = "Create Staff Title";
	public static final String TIME_READ = "Read Time";
	public static final String TIME_CREATE = "Create Time";
	public static final String UPDATE_VOL_PROFILE_FACILITY_MGMT = "Update Vol Profile Facility Mgmt";
	public static final String USER_MANAGER = "Manage Users";
	public static final String VOLUNTEER_AWARD_CREATE = "Create Volunteer Award";
	public static final String VOLUNTEER_AWARD_READ = "Read Volunteer Award";
	public static final String VOLUNTEER_CREATE = "Create Volunteer";
	public static final String VOLUNTEER_READ = "Read Volunteer";
	public static final String VOLUNTEER_SELF_SERVICE_NOTIFICATION_VIEW = "View Volunteer Self Service Notification";
	public static final String VOL_SVC_STAFF_DELETE = "Delete Voluntary Services Staff";
	public static final String VOLUNTEER_UNTERMINATE_BY_CAUSE = "Unterminate Volunteer By Cause";

	// --------------------------------------------- Fields

	private Set<RolePermission> roles;

	// --------------------------------------------- Business Methods

	public void initializeAll() {
		initialize(getRoles());
	}

	// --------------------------------------------- Accessor Methods

	@Transient
	@Override
	public String getAuthority() {
		return getName();
	}

	@OneToMany(mappedBy = "permission", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@BatchSize(size = 500)
	public Set<RolePermission> getRoles() {
		if (roles == null)
			roles = new HashSet<>();
		return roles;
	}

	public void setRoles(Set<RolePermission> roles) {
		this.roles = roles;
	}

	public static enum PermissionType implements LookupType {

		LOGIN_APPLICATION(Permission.LOGIN_APPLICATION, 10), //
		ORG_CODE_NATIONAL_CREATE(Permission.ORG_CODE_NATIONAL_CREATE, 20), //
		ORG_CODE_NATIONAL_READ(Permission.ORG_CODE_NATIONAL_READ, 30), //
		ORG_CODE_LOCAL_CREATE(Permission.ORG_CODE_LOCAL_CREATE, 40), //
		ORG_CODE_LOCAL_READ(Permission.ORG_CODE_LOCAL_READ, 50), //
		PERM_CODE_SERVICE_CREATE(Permission.PERM_CODE_SERVICE_CREATE, 60), //
		PERM_CODE_SERVICE_READ(Permission.PERM_CODE_SERVICE_READ, 70), //
		USER_MANAGER(Permission.USER_MANAGER, 80), //
		VOLUNTEER_CREATE(Permission.VOLUNTEER_CREATE, 90), //
		VOLUNTEER_READ(Permission.VOLUNTEER_READ, 100), //
		VOL_SVC_STAFF_DELETE(Permission.VOL_SVC_STAFF_DELETE, 110), //
		DONATION_CREATE(Permission.DONATION_CREATE, 120), //
		DONATION_READ(Permission.DONATION_READ, 130), //
		VOLUNTEER_UNTERMINATE_BY_CAUSE(Permission.VOLUNTEER_UNTERMINATE_BY_CAUSE, 140), //
		FACILITY_CREATE(Permission.FACILITY_CREATE, 150), //
		FACILITY_EDIT_CURRENT(Permission.FACILITY_EDIT_CURRENT, 160), //
		FACILITY_EDIT_ALL(Permission.FACILITY_EDIT_ALL, 170), //
		EXCLUDED_ENTITY_VIEW_LOCAL(Permission.EXCLUDED_ENTITY_VIEW_LOCAL, 180), //
		EXCLUDED_ENTITY_VIEW_ALL(Permission.EXCLUDED_ENTITY_VIEW_ALL, 190), //
		LOCAL_BRANCH_CREATE(Permission.LOCAL_BRANCH_CREATE, 200), //
		BENEFITING_SERVICE_TEMPLATE_MANAGE(Permission.BENEFITING_SERVICE_TEMPLATE_MANAGE, 220), //
		MEALTICKET_CREATE(Permission.MEALTICKET_CREATE, 230), //
		MEALTICKET_READ(Permission.MEALTICKET_READ, 240), //
		TIME_CREATE(Permission.TIME_CREATE, 250), //
		TIME_READ(Permission.TIME_READ, 260), //
		LOGIN_KIOSK(Permission.LOGIN_KIOSK, 270), //
		BENEFITING_SERVICE_CREATE(Permission.BENEFITING_SERVICE_CREATE, 280), //
		REQUIREMENTS_LOCAL_MANAGE(Permission.REQUIREMENTS_LOCAL_MANAGE, 290), //
		REQUIREMENTS_GLOBAL_MANAGE(Permission.REQUIREMENTS_GLOBAL_MANAGE, 300), //
		MERGE_DONOR(Permission.MERGE_DONOR, 310), //
		UPDATE_VOL_PROFILE_FACILITY_MGMT(Permission.UPDATE_VOL_PROFILE_FACILITY_MGMT, 320), //
		RUN_GRAND_TOTAL_DONATIONS(Permission.RUN_GRAND_TOTAL_DONATIONS, 330), //
		RUN_DONATION_RECEIPT(Permission.RUN_DONATION_RECEIPT, 340), //
		RUN_DONATION_MEMORANDUM(Permission.RUN_DONATION_MEMORANDUM, 350), //
		RUN_NEW_VOLUNTEERS(Permission.RUN_NEW_VOLUNTEERS, 360), //
		RUN_MEAL_TICKET_REPORT(Permission.RUN_MEAL_TICKET_REPORT, 370), //
		RUN_VOLUNTEER_ADDRESS_LABELS(Permission.RUN_VOLUNTEER_ADDRESS_LABELS, 380), //
		RUN_VOLUNTARY_SERVICE_ADDRESS_LABELS(Permission.RUN_VOLUNTARY_SERVICE_ADDRESS_LABELS, 390), //
		RUN_NATIONAL_OFFICIAL_ADDRESS_LABELS(Permission.RUN_NATIONAL_OFFICIAL_ADDRESS_LABELS, 400), //
		RUN_VOLUNTARY_SERVICE_DIRECTORY(Permission.RUN_VOLUNTARY_SERVICE_DIRECTORY, 410), //
		RUN_DONATION_THANK_YOU_LETTER(Permission.RUN_DONATION_THANK_YOU_LETTER, 420), //
		// skipped a couple IDs in the DB 430, 440
		STAFF_TITLE_CREATE(Permission.STAFF_TITLE_CREATE, 450), //
		AWARD_CODE_CREATE(Permission.AWARD_CODE_CREATE, 460), //
		VOLUNTEER_AWARD_CREATE(Permission.VOLUNTEER_AWARD_CREATE, 470), //
		VOLUNTEER_AWARD_READ(Permission.VOLUNTEER_AWARD_READ, 480), //
		RUN_VOLUNTEERS_BY_ORG(Permission.RUN_VOLUNTEERS_BY_ORG, 490), //
		// unused since NPDB was descoped - CPB
		// NPDB_EXECUTE(Permission.NPDB_EXECUTE, 500), //
		// NPDB_REQUEST(Permission.NPDB_REQUEST, 510), //
		RUN_NATIONAL_OFFICIAL_LISTING(Permission.RUN_NATIONAL_OFFICIAL_LISTING, 520), //
		RUN_SCHEDULED_OCCAS_HOURS(Permission.RUN_SCHEDULED_OCCAS_HOURS, 530), //
		RUN_VOLUNTEER_ALPHA(Permission.RUN_VOLUNTEER_ALPHA, 540), //
		VOLUNTEER_SELF_SERVICE_NOTIFICATION_VIEW(Permission.VOLUNTEER_SELF_SERVICE_NOTIFICATION_VIEW, 550), //
		RUN_COMMITTEE_ATTENDANCE_LISTING(Permission.RUN_COMMITTEE_ATTENDANCE_LISTING, 570), //
		EDONATION_MANAGE(Permission.EDONATION_MANAGE, 580), //
		RUN_ORGANIZATION_LISTING(Permission.RUN_ORGANIZATION_LISTING, 590), //
		RUN_VOL_ORGS_RS_OCC_HOURS(Permission.RUN_VOL_ORGS_RS_OCC_HOURS, 600), //
		EXPENDITURE_CREATE(Permission.EXPENDITURE_CREATE, 610), //
		EXPENDITURE_DELETE(Permission.EXPENDITURE_DELETE, 620), //
		RUN_BENEFITING_SERVIVE_LISTING(Permission.RUN_BENEFITING_SERVIVE_LISTING, 630),//
		;

		private String name;
		private long id;

		private PermissionType(String name, long id) {
			this.name = name;
			this.id = id;
		}

		public String getName() {
			return name;
		}

		@Override
		public long getId() {
			return id;
		}

		public static Collection<GrantedAuthority> getAllAsGrantedAuthorities() {
			return Arrays.asList(PermissionType.values()).stream().map(p -> new SimpleGrantedAuthority(p.getName()))
					.collect(Collectors.toList());
		}
	}

}
