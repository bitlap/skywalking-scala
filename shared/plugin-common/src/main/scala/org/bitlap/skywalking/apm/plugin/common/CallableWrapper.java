package org.bitlap.skywalking.apm.plugin.common;

import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.ContextSnapshot;
import org.apache.skywalking.apm.agent.core.context.tag.StringTag;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine;

import java.util.concurrent.Callable;

public class CallableWrapper<A> implements Callable<A> {
    private final Callable<A> callable;
    private final ContextSnapshot context;
    private final String className;
    private final String methodName;

    public CallableWrapper(Callable<A> callable, ContextSnapshot context, String className, String methodName) {
        this.callable = callable;
        this.context = context;
        this.className = className;
        this.methodName = methodName;
    }

    @Override
    public A call() throws Exception {
        AbstractSpan span = ContextManager.createLocalSpan(getOperationName());
        span.setComponent(ComponentsDefine.JDK_THREADING);
        new StringTag(206, "ThreadPoolMethod").set(span, methodName);
        new StringTag(207, "ThreadPoolClass").set(span, className);
        ContextManager.continued(context);
        try {
            return callable.call();
        } finally {
            if (ContextManager.isActive()) {
                ContextManager.stopSpan();
            }
        }
    }

    private String getOperationName() {
        return "SwCallableWrapper/" + Thread.currentThread().getName();
    }

}