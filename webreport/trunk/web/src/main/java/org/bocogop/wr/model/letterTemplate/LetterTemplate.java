package org.bocogop.wr.model.letterTemplate;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SortNatural;
import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.wr.model.ObjectScopedToStationNumbers;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.persistence.conversion.LetterTypeConverter;

@Entity
@Table(name = "LetterTemplates", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class LetterTemplate extends AbstractAuditedVersionedPersistent<LetterTemplate>
		implements Comparable<LetterTemplate>, ObjectScopedToStationNumbers {
	private static final long serialVersionUID = 6904844123870655771L;

	/**
	 * See @JsonView documentation - CPB
	 */
	public static class LetterTemplateView {
		public interface Basic {
		}

		public interface Extended extends Basic {
		}
	}

	// -------------------------------------- Fields

	private Facility facility;
	private boolean showHeader;
	private boolean showFooter;

	@NotNull
	private LetterType type;

	@Length(max = 4000)
	private String body;

	private SortedSet<LetterTemplatePlaceholder> placeholders;

	// -------------------------------------- Constructors

	public LetterTemplate() {
	}

	public LetterTemplate(Facility facility, LetterType type) {
		this.facility = facility;
		this.type = type;
	}

	// -------------------------------------- Business Methods

	@Transient
	public boolean isDefault() {
		return getFacility().isCentralOffice();
	}

	@Transient
	@JsonIgnore
	public Collection<String> getScopedToStationNumbers() {
		Facility institution = getFacility();
		if (institution == null)
			return new ArrayList<>();
		return Arrays.asList(institution.getStationNumber());
	}

	@Transient
	@JsonView(LetterTemplateView.Basic.class)
	public String getDisplayName() {
		return getType().getName();
	}

	// -------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(LetterTemplate oo) {
		return new EqualsBuilder().append(getType(), oo.getType())
				.append(nullSafeGetId(getFacility()), nullSafeGetId(oo.getFacility())).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getType()).append(nullSafeGetId(getFacility())).toHashCode();
	}

	@Override
	public int compareTo(LetterTemplate o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder().append(getType(), o.getType()).toComparison() > 0 ? 1 : -1;
	}

	public String toString() {
		return getDisplayName();
	}

	// -------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "FacilityFK")
	@JsonView(LetterTemplateView.Extended.class)
	public Facility getFacility() {
		return facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	@OneToMany(mappedBy = "letterTemplate", fetch = FetchType.LAZY)
	@BatchSize(size = 50)
	@JsonView(LetterTemplateView.Extended.class)
	@SortNatural
	public SortedSet<LetterTemplatePlaceholder> getPlaceholders() {
		if (placeholders == null)
			placeholders = new TreeSet<>();
		return placeholders;
	}

	public void setPlaceholders(SortedSet<LetterTemplatePlaceholder> placeholders) {
		this.placeholders = placeholders;
	}

	@Column(name = "ShowHeader", nullable = false)
	@JsonView(LetterTemplateView.Basic.class)
	public boolean isShowHeader() {
		return showHeader;
	}

	public void setShowHeader(boolean showHeader) {
		this.showHeader = showHeader;
	}

	@Column(name = "ShowFooter", nullable = false)
	@JsonView(LetterTemplateView.Basic.class)
	public boolean isShowFooter() {
		return showFooter;
	}

	public void setShowFooter(boolean showFooter) {
		this.showFooter = showFooter;
	}

	@Column(length = 4000)
	@JsonView(LetterTemplateView.Extended.class)
	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	@Column(name = "LetterType", length = 20, nullable = false)
	@Convert(converter = LetterTypeConverter.class)
	@JsonView(LetterTemplateView.Basic.class)
	public LetterType getType() {
		return type;
	}

	public void setType(LetterType type) {
		this.type = type;
	}

}
