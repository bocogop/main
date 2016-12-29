package org.bocogop.wr.model.lookup;

public enum CommonTemplateType {
	MEAL_TICKET_TEXT("mealTicket"), //
	;
	
	private String name;

	private CommonTemplateType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
