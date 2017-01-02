package org.bocogop.shared.util.aop;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.bocogop.shared.util.aop.TxCommitRunnable.TxCommitRunnableType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class TxCommitExecutorImpl extends TransactionSynchronizationAdapter implements TxCommitExecutor {
	private static final Logger log = LoggerFactory.getLogger(TxCommitExecutorImpl.class);

	private static final ThreadLocal<Map<TxCommitRunnableType, List<TxCommitRunnable>>> COMMIT_RUNNABLES = new ThreadLocal<>();

	@Override
	public void execute(TxCommitRunnable runnable) {
		log.debug("Submitting new runnable {} to run", runnable);
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			log.warn("Transaction synchronization is not active; executing runnable {} immediately", runnable);
			runnable.run(null);
			return;
		}
		Map<TxCommitRunnableType, List<TxCommitRunnable>> threadRunnables = COMMIT_RUNNABLES.get();
		if (threadRunnables == null) {
			threadRunnables = new EnumMap<TxCommitRunnableType, List<TxCommitRunnable>>(TxCommitRunnableType.class);
			COMMIT_RUNNABLES.set(threadRunnables);
			TransactionSynchronizationManager.registerSynchronization(this);
		}
		List<TxCommitRunnable> list = threadRunnables.get(runnable.getWhen());
		if (list == null) {
			list = new ArrayList<>();
			threadRunnables.put(runnable.getWhen(), list);
		}
		list.add(runnable);
	}

	@Override
	public void beforeCommit(boolean readOnly) {
		run(TxCommitRunnableType.BEFORE_COMMIT, readOnly);
	}

	@Override
	public void afterCommit() {
		run(TxCommitRunnableType.AFTER_COMMIT, null);
	}

	private void run(TxCommitRunnableType type, Boolean readOnly) {
		Map<TxCommitRunnableType, List<TxCommitRunnable>> map = COMMIT_RUNNABLES.get();
		if (map == null)
			return;

		List<TxCommitRunnable> threadRunnables = map.get(type);
		if (threadRunnables == null)
			return;

		log.debug("Transaction successfully committed, executing {} {} runnables", threadRunnables.size(), type);
		for (TxCommitRunnable runnable : threadRunnables) {
			log.debug("Executing runnable {}", runnable);
			try {
				runnable.run(readOnly);
			} catch (RuntimeException e) {
				log.error("Failed to execute runnable " + runnable, e);
			}
		}
	}

	@Override
	public void afterCompletion(int status) {
		log.debug("Transaction completed with status {}", status == STATUS_COMMITTED ? "COMMITTED" : "ROLLED_BACK");
		COMMIT_RUNNABLES.remove();
	}

}
