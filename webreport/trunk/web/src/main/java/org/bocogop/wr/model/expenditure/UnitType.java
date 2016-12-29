package org.bocogop.wr.model.expenditure;

import com.fasterxml.jackson.annotation.JsonFormat;

import org.bocogop.shared.persistence.usertype.CodedEnum;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum UnitType implements CodedEnum {
	ASSORTMENT("AS", "Assortment"), //
	BAG("BG", "Bag"), //
	BOX("BX", "Box"), //
	CASE("CS", "Case"), //
	CARTON("CT", "Carton"), //
	DOZEN("D", "Dozen"), //
	EACH("EA", "Each"), //
	JOB("JB", "Job"), //
	LOT("LT", "Lot"), //
	MEAL("ME", "Meal"), //
	MONTH("MO", "Month"), //
	PACKAGE("PG", "Package"), //
	UNIT("UN", "Unit"), //
	YEAR("YR", "Year") //
	;

	private String code;
	private String name;

	private UnitType(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getCode() {
		return code;
	}

}
