package org.bocogop.wr.model.letterTemplate;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;

@Entity
@Table(name = "LetterTemplatePlaceholders", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class LetterTemplatePlaceholder extends AbstractAuditedVersionedPersistent<LetterTemplatePlaceholder>
		implements Comparable<LetterTemplatePlaceholder> {
	private static final long serialVersionUID = 6904844123870655771L;

	// -------------------------------------- Fields

	private LetterTemplate letterTemplate;
	private String name;
	private String description;

	// -------------------------------------- Business Methods

	// -------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(LetterTemplatePlaceholder oo) {
		return new EqualsBuilder().append(nullSafeGetId(getLetterTemplate()), nullSafeGetId(oo.getLetterTemplate()))
				.append(getName(), oo.getName()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(getLetterTemplate())).append(getName()).toHashCode();
	}

	@Override
	public int compareTo(LetterTemplatePlaceholder o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder().append(getName(), o.getName()).toComparison() > 0 ? 1 : -1;
	}

	public String toString() {
		return getName();
	}

	// -------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WrLetterTemplatesFK")
	public LetterTemplate getLetterTemplate() {
		return letterTemplate;
	}

	public void setLetterTemplate(LetterTemplate letterTemplate) {
		this.letterTemplate = letterTemplate;
	}

	@Column(name = "StringName", length = 30, nullable = false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "StringDescription", length = 255, nullable = false)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
