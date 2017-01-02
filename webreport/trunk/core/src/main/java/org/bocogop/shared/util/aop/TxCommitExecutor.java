package org.bocogop.shared.util.aop;

public interface TxCommitExecutor {

	void execute(TxCommitRunnable runnable);

}