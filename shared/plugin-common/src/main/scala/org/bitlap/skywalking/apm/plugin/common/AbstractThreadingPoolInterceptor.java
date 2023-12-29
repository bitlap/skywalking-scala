package org.bitlap.skywalking.apm.plugin.common;

import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.ContextSnapshot;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

import java.lang.reflect.Method;

public abstract class AbstractThreadingPoolInterceptor implements InstanceMethodsAroundInterceptor {
    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {
        if (!ContextManager.isActive()) {
            return;
        }

        if (allArguments == null || allArguments.length < 1) {
            return;
        }


        Object argument = allArguments[0];

        // Avoid duplicate enhancement, such as the case where it has already been enhanced by RunnableWrapper or CallableWrapper with toolkit.
        if (argument instanceof EnhancedInstance && ((EnhancedInstance) argument).getSkyWalkingDynamicField() instanceof ContextSnapshot) {
            return;
        }


        Object wrappedObject = wrap(argument);
        if (wrappedObject != null) {
            allArguments[0] = wrappedObject;
        }
    }

    /**
     * wrap the Callable or Runnable object if needed
     *
     * @param param Callable or Runnable object
     * @return Wrapped object or null if not needed to wrap
     */
    public abstract Object wrap(Object param);

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
        AgentUtils$.MODULE$.logError(t);
    }
}
