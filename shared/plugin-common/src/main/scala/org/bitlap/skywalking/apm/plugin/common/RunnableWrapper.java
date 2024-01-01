package org.bitlap.skywalking.apm.plugin.common;

import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.ContextSnapshot;
import org.apache.skywalking.apm.agent.core.context.tag.StringTag;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine;

public class RunnableWrapper implements Runnable {
    private final Runnable runnable;
    private final ContextSnapshot context;
    private final String className;
    private final String methodName;

    public RunnableWrapper(Runnable runnable, ContextSnapshot context, String className, String methodName) {
        this.runnable = runnable;
        this.context = context;
        this.className = className;
        this.methodName = methodName;
    }

    @Override
    public void run() {
        AbstractSpan span = ContextManager.createLocalSpan(getOperationName());
        span.setComponent(ComponentsDefine.JDK_THREADING);
        new StringTag(206, "ThreadPoolMethod").set(span, methodName);
        new StringTag(207, "ThreadPoolClass").set(span, className);
        ContextManager.continued(context);
        try {
            runnable.run();
        } finally {
            if (ContextManager.isActive()) {
                ContextManager.stopSpan();
            }
        }
    }

    private String getOperationName() {
        return "SwRunnableWrapper/" + Thread.currentThread().getName();
    }

}