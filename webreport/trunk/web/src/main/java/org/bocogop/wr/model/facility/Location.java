package org.bocogop.wr.model.facility;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.util.PersistenceUtil;
import org.bocogop.wr.model.validation.constraints.ExtendedEmailValidator;
import org.bocogop.wr.util.ValidationUtil;

@Entity
@DiscriminatorValue("L")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class Location extends AbstractUpdateableLocation<Location> {
	private static final long serialVersionUID = -8678395783438462990L;

	public static class LocationView {
		public interface Basic {
		}

		public interface Extended extends Basic {
		}

		public interface ShowHierarchy extends Basic, BasicLocationView.ShowHierarchy {
		}
	}

	// ---------------------------------------- Fields

	private String contactName;
	private String contactRole;
	@ExtendedEmailValidator(message = "Please enter a valid email in the format 'user@domain.tld'.")
	private String contactEmail;
	@Pattern(regexp = ValidationUtil.PHONE_REGEX, message = "Please enter a valid phone number.")
	private String contactPhone;

	// ---------------------------------------- Business Methods

	@Override
	@Transient
	@JsonView(LocationView.Extended.class)
	public String getScale() {
		return "Location";
	}

	/**
	 * Walk up the tree and get the nearest Facility ancestor - CPB
	 */
	@Transient
	@JsonIgnore
	public Facility getFacility() {
		AbstractLocation l = this;
		Set<AbstractLocation> previousParents = new HashSet<>();
		while (!l.getScale().equals("Facility")) {
			previousParents.add(l);
			AbstractLocation lNew = l.getParent();
			if (lNew == null || previousParents.contains(lNew))
				return null;
			l = lNew;
		}
		return (Facility) PersistenceUtil.initializeAndUnproxy(l);
	}

	// ---------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(AbstractLocation oo) {
		/* Safe cast due to AbstractBusinessKeyOwner logic - CPB */
		// Location l = (Location) oo;
		return new EqualsBuilder().append(nullSafeGetId(getParent()), nullSafeGetId(oo.getParent()))
				.append(getName(), oo.getName()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(getParent())).append(getName()).toHashCode();
	}

	// ---------------------------------------- Accessor Methods

	@Column(length = 50)
	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	@Column(length = 50)
	public String getContactRole() {
		return contactRole;
	}

	public void setContactRole(String contactRole) {
		this.contactRole = contactRole;
	}

	@Column(length = 250)
	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	@Column(length = 30)
	public String getContactPhone() {
		return contactPhone;
	}

	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}

}
