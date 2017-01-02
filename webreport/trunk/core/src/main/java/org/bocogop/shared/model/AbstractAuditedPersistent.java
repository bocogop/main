package org.bocogop.shared.model;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.xml.bind.annotation.XmlTransient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

@MappedSuperclass
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
public abstract class AbstractAuditedPersistent<T> extends AbstractIdentifiedPersistent<T>
		implements AuditedPersistent {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AbstractAuditedPersistent.class);
	private static final long serialVersionUID = 9081660873563592483L;

	public static String getCurrentUserIdForAudit() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String name = auth == null ? "Unknown" : auth.getName();
		return name;
	}

	// ---------------------------------------- Fields

	private String createdBy;
	private ZonedDateTime createdDate;
	private String modifiedBy;
	private ZonedDateTime modifiedDate;

	// ---------------------------------------- Business Methods

	@PreUpdate
	public void preUpdate() {
		String userId = getCurrentUserIdForAudit();
		ZonedDateTime now = null;
		// allow manual setting of this field in app code - CPB
		if (getModifiedDate() == null || getModifiedDate().getNano() != 1)
			now = ZonedDateTime.now(ZoneId.of("Z"));

		setModifiedBy(userId);
		setModifiedDate(now.withNano(0));
	}

	@PrePersist
	public void prePersist() {
		String userId = getCurrentUserIdForAudit();
		ZonedDateTime now = null;

		ZonedDateTime cd = getCreatedDate();
		ZonedDateTime md = getModifiedDate();

		if (cd == null || md == null)
			now = ZonedDateTime.now(ZoneId.of("Z"));

		if (getCreatedBy() == null)
			setCreatedBy(userId);
		if (cd == null)
			setCreatedDate(now);
		if (getModifiedBy() == null)
			setModifiedBy(userId);
		setModifiedDate(md == null ? now.withNano(0) : md.withNano(0));
	}

	// ---------------------------------------- Accessor Methods

	@Column(name = "CreatedBy", length = 30)
	@XmlTransient
	@JsonIgnore
	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	@Column(name = "CreatedDate")
	@XmlTransient
	@JsonIgnore
	public ZonedDateTime getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(ZonedDateTime createdDate) {
		this.createdDate = createdDate;
	}

	@Column(name = "ModifiedBy", length = 30)
	@XmlTransient
	@JsonIgnore
	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	@Column(name = "ModifiedDate")
	@XmlTransient
	@JsonIgnore
	public ZonedDateTime getModifiedDate() {
		return modifiedDate;
	}

	private void setModifiedDate(ZonedDateTime modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public void setModifiedDateOverride(ZonedDateTime modifiedDate) {
		setModifiedDate(modifiedDate == null ? null : modifiedDate.withNano(1));
	}
}
