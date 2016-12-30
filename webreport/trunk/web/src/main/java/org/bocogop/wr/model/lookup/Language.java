package org.bocogop.wr.model.lookup;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.bocogop.wr.model.lookup.Language.LanguageType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

/* Legacy "Ver" version column ignored - CPB */
@Entity
@Immutable
@Table(name = "Languages", schema = "wr")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@AttributeOverrides({ @AttributeOverride(name = "name", column = @Column(name = "LanguageName") ) })
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
public class Language extends AbstractLookup<Language, LanguageType> {
	private static final long serialVersionUID = 5598414554054024076L;

	private String culture;
	private String cultureId;
	private String countryOrRegion;
	private String languageWithLocale;

	@Column(length = 10)
	@JsonProperty
	public String getCulture() {
		return culture;
	}

	public void setCulture(String culture) {
		this.culture = culture;
	}

	@Column(name = "CultureIdentifier", length = 6)
	@JsonProperty
	public String getCultureId() {
		return cultureId;
	}

	public void setCultureId(String cultureId) {
		this.cultureId = cultureId;
	}

	@Column(length = 20)
	@JsonProperty
	public String getCountryOrRegion() {
		return countryOrRegion;
	}

	public void setCountryOrRegion(String countryOrRegion) {
		this.countryOrRegion = countryOrRegion;
	}

	@Column(length = 610)
	@JsonProperty
	public String getLanguageWithLocale() {
		return languageWithLocale;
	}

	public void setLanguageWithLocale(String languageWithLocale) {
		this.languageWithLocale = languageWithLocale;
	}

	public static enum LanguageType implements LookupType {
		// FIXWR add when we have languages
		ENGLISH(1);

		private long id;

		private LanguageType(long id) {
			this.id = id;
		}

		public long getId() {
			return id;
		}

	}

}
