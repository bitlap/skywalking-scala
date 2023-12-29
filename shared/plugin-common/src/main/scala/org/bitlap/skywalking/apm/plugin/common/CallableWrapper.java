package org.bitlap.skywalking.apm.plugin.common;

import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.ContextSnapshot;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine;

import java.util.concurrent.Callable;

public class CallableWrapper<A> implements Callable<A> {
    private final Callable<A> callable;
    private final ContextSnapshot context;

    public CallableWrapper(Callable<A> callable, ContextSnapshot context) {
        this.callable = callable;
        this.context = context;
    }

    @Override
    public A call() throws Exception {
        AbstractSpan span = ContextManager.createLocalSpan(getOperationName());
        span.setComponent(ComponentsDefine.JDK_THREADING);
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