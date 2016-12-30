package org.bocogop.wr.web.precinct;

import org.bocogop.wr.model.precinct.Precinct;

public class PrecinctCommand {

	private Precinct precinct;

	public PrecinctCommand() {
	}

	public PrecinctCommand(Precinct precinct) {
		this.precinct = precinct;
	}

	public void refreshPrecinct(Precinct f) {
		setPrecinct(f);
	}

	public Precinct getPrecinct() {
		return precinct;
	}

	public void setPrecinct(Precinct precinct) {
		this.precinct = precinct;
	}

}
