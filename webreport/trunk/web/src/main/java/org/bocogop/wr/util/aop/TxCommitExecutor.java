package org.bocogop.wr.util.aop;

public interface TxCommitExecutor {

	void execute(TxCommitRunnable runnable);

}