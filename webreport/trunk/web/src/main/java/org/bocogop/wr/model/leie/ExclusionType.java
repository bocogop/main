package org.bocogop.wr.model.leie;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.core.AbstractIdentifiedPersistent;
import org.bocogop.shared.model.lookup.LookupType;
import org.bocogop.wr.model.volunteer.Volunteer.VolunteerView;

@Entity
@Immutable
@Table(name = "ExclusionTypes", schema = "wr")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class ExclusionType extends AbstractIdentifiedPersistent<ExclusionType> {
	private static final long serialVersionUID = 5598414554054024076L;

	public static class ExclusionTypeView {
		public interface Basic {
		}

		public interface Extended extends Basic {
		}
	}

	public static enum ExcludedEntityTypeValue implements LookupType {
		PROGRAM_RELATED_CRIMES(1);

		private long id;

		private ExcludedEntityTypeValue(long id) {
			this.id = id;
		}

		public long getId() {
			return id;
		}

	}

	// ---------------------------------------------- Fields

	private String ssa;
	private String code42Usc;
	private String description;
	private String type;
	private String desc1;
	private String desc2;
	private String desc3;
	private String desc4;
	private String desc5;
	private String desc6;
	private String desc7;
	private String desc8;
	private String desc9;
	private String desc10;

	// ---------------------------------------------- Business Methods

	@Transient
	@JsonView({ ExclusionTypeView.Extended.class, //
			VolunteerView.Search.class })
	public String getDisplayName() {
		return ssa + " - " + description;
	}

	// ---------------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(ExclusionType oo) {
		return new EqualsBuilder().append(getSsa(), oo.getSsa()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getSsa()).toHashCode();
	}

	// ---------------------------------------------- Accessor Methods

	@Column(length = 9)
	@JsonView(ExclusionTypeView.Basic.class)
	public String getSsa() {
		return ssa;
	}

	public void setSsa(String ssa) {
		this.ssa = ssa;
	}

	@Column(name = "`42USC`", length = 25)
	@JsonView(ExclusionTypeView.Basic.class)
	public String getCode42Usc() {
		return code42Usc;
	}

	public void setCode42Usc(String code42Usc) {
		this.code42Usc = code42Usc;
	}

	@Column(length = 1000)
	@JsonView(ExclusionTypeView.Basic.class)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(length = 1)
	@JsonView(ExclusionTypeView.Extended.class)
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Column(length = 30)
	@JsonView(ExclusionTypeView.Extended.class)
	public String getDesc1() {
		return desc1;
	}

	public void setDesc1(String desc1) {
		this.desc1 = desc1;
	}

	@Column(length = 30)
	@JsonView(ExclusionTypeView.Extended.class)
	public String getDesc2() {
		return desc2;
	}

	public void setDesc2(String desc2) {
		this.desc2 = desc2;
	}

	@Column(length = 30)
	@JsonView(ExclusionTypeView.Extended.class)
	public String getDesc3() {
		return desc3;
	}

	public void setDesc3(String desc3) {
		this.desc3 = desc3;
	}

	@Column(length = 30)
	@JsonView(ExclusionTypeView.Extended.class)
	public String getDesc4() {
		return desc4;
	}

	public void setDesc4(String desc4) {
		this.desc4 = desc4;
	}

	@Column(length = 30)
	@JsonView(ExclusionTypeView.Extended.class)
	public String getDesc5() {
		return desc5;
	}

	public void setDesc5(String desc5) {
		this.desc5 = desc5;
	}

	@Column(length = 30)
	@JsonView(ExclusionTypeView.Extended.class)
	public String getDesc6() {
		return desc6;
	}

	public void setDesc6(String desc6) {
		this.desc6 = desc6;
	}

	@Column(length = 30)
	@JsonView(ExclusionTypeView.Extended.class)
	public String getDesc7() {
		return desc7;
	}

	public void setDesc7(String desc7) {
		this.desc7 = desc7;
	}

	@Column(length = 30)
	@JsonView(ExclusionTypeView.Extended.class)
	public String getDesc8() {
		return desc8;
	}

	public void setDesc8(String desc8) {
		this.desc8 = desc8;
	}

	@Column(length = 30)
	@JsonView(ExclusionTypeView.Extended.class)
	public String getDesc9() {
		return desc9;
	}

	public void setDesc9(String desc9) {
		this.desc9 = desc9;
	}

	@Column(length = 30)
	@JsonView(ExclusionTypeView.Extended.class)
	public String getDesc10() {
		return desc10;
	}

	public void setDesc10(String desc10) {
		this.desc10 = desc10;
	}

}
