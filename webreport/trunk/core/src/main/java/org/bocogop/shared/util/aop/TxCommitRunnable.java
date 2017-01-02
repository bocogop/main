package org.bocogop.shared.util.aop;

public interface TxCommitRunnable {

	/*
	 * Run the transaction; param readOnly is true/false if known (as in
	 * BEFORE_COMMIT) or null if unknown - CPB
	 */
	Object run(Boolean readOnly);

	TxCommitRunnableType getWhen();

	public static enum TxCommitRunnableType {
		SUSPEND, RESUME, FLUSH, BEFORE_COMMIT, BEFORE_COMPLETION, AFTER_COMMIT, AFTER_COMPLETION;
	}

}
