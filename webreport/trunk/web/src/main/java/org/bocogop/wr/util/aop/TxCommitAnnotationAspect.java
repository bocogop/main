package org.bocogop.wr.util.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.bocogop.wr.util.aop.TxCommitRunnable.TxCommitRunnableType;

@Aspect
@Component
public class TxCommitAnnotationAspect {
	private static final Logger log = LoggerFactory.getLogger(TxCommitAnnotationAspect.class);

	private final TxCommitExecutor commitExecutor;

	@Autowired
	public TxCommitAnnotationAspect(TxCommitExecutor commitExecutor) {
		this.commitExecutor = commitExecutor;
	}

	@Around(value = "@annotation(org.bocogop.wr.util.aop.BeforeCommit)", argNames = "pjp")
	public Object beforeAroundAdvice(final ProceedingJoinPoint pjp) {
		commitExecutor.execute(new PjpCommitRunnable(pjp, TxCommitRunnableType.BEFORE_COMMIT));
		return null;
	}

	@Around(value = "@annotation(org.bocogop.wr.util.aop.AfterCommit)", argNames = "pjp")
	public Object afterAroundAdvice(final ProceedingJoinPoint pjp) {
		commitExecutor.execute(new PjpCommitRunnable(pjp, TxCommitRunnableType.AFTER_COMMIT));
		return null;
	}

	static final class PjpCommitRunnable implements TxCommitRunnable {

		private ProceedingJoinPoint pjp;
		private TxCommitRunnableType type;

		public PjpCommitRunnable(ProceedingJoinPoint pjp, TxCommitRunnableType type) {
			this.pjp = pjp;
			this.type = type;
		}

		@Override
		public TxCommitRunnableType getWhen() {
			return type;
		}

		@Override
		public Object run(Boolean readOnly) {
			try {
				Object result = pjp.proceed();
				return result;
			} catch (Throwable e) {
				log.error("Exception while invoking pjp.proceed()", e);
				throw new RuntimeException(e);
			}
		}

		@Override
		public String toString() {
			String typeName = pjp.getTarget().getClass().getSimpleName();
			String methodName = pjp.getSignature().getName();
			return getClass().getSimpleName() + "[type=" + typeName + ", method=" + methodName + "]";
		}
	}

}
