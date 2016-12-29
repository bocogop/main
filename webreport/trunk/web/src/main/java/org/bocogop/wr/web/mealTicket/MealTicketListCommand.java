package org.bocogop.wr.web.mealTicket;

import java.util.ArrayList;
import java.util.List;

import org.bocogop.wr.model.mealTicket.MealTicket;

public class MealTicketListCommand {

	// ----------------------------------- Fields

	private List<MealTicket> mealTickets;

	// ----------------------------------- Constructors

	public MealTicketListCommand() {
	}

	public MealTicketListCommand(List<MealTicket> mealTickets) {
		this.mealTickets = mealTickets;
	}

	// ----------------------------------- Accessor Methods

	public List<MealTicket> getMealTickets() {
		if (mealTickets == null)
			mealTickets = new ArrayList<>();
		return mealTickets;
	}

	public void setMealTickets(List<MealTicket> mealTickets) {
		this.mealTickets = mealTickets;
	}
}
