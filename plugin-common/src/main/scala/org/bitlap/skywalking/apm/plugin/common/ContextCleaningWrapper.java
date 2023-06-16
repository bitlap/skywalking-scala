package org.bitlap.skywalking.apm.plugin.common;

import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.ContextSnapshot;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine;

public class ContextCleaningWrapper implements Runnable {
    private final Runnable runnable;
    private final ContextSnapshot context;

    public ContextCleaningWrapper(Runnable runnable, ContextSnapshot context) {
        this.runnable = runnable;
        this.context = context;
    }

    @Override
    public void run() {
        AbstractSpan span = ContextManager.createLocalSpan(getOperationName());
        span.setComponent(ComponentsDefine.JDK_THREADING);
        ContextManager.continued(context);
        try {
            runnable.run();
        } finally {
            ContextManager.stopSpan();
        }
    }

    private String getOperationName() {
        return "RunnableWrapper/" + Thread.currentThread().getName();
    }

}