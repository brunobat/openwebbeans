/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.newtests.interceptors.factory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.webbeans.exception.WebBeansException;
import org.apache.webbeans.newtests.AbstractUnitTest;
import org.apache.webbeans.newtests.interceptors.factory.beans.ClassInterceptedClass;
import org.apache.webbeans.proxy.InterceptorDecoratorProxyFactory;

import org.apache.webbeans.proxy.InterceptorHandler;
import org.apache.webbeans.proxy.OwbInterceptorProxy;
import org.apache.webbeans.util.ClassUtil;
import org.junit.Assert;
import org.junit.Test;


/**
 * Test the {@link org.apache.webbeans.proxy.InterceptorDecoratorProxyFactory}
 */
public class InterceptorDecoratorProxyFactoryTest extends AbstractUnitTest
{

    @Test
    public void textSimpleProxyCreation() throws Exception
    {
        InterceptorDecoratorProxyFactory pf = new InterceptorDecoratorProxyFactory();

        // we take a fresh URLClassLoader to not blur the test classpath with synthetic classes.
        ClassLoader classLoader = new URLClassLoader(new URL[0]);

        List<Method> methods = ClassUtil.getNonPrivateMethods(ClassInterceptedClass.class);

        Method[] interceptedMethods = methods.toArray(new Method[methods.size()]);
        Method[] nonInterceptedMethods = null;


        Class<ClassInterceptedClass> proxyClass = pf.createProxyClass(classLoader, ClassInterceptedClass.class, interceptedMethods, nonInterceptedMethods);
        Assert.assertNotNull(proxyClass);

        ClassInterceptedClass internalInstance = new ClassInterceptedClass();
        internalInstance.init();

        TestInterceptorHandler testInvocationHandler = new TestInterceptorHandler(internalInstance);

        ClassInterceptedClass proxy = pf.createProxyInstance(proxyClass, internalInstance, testInvocationHandler);
        Assert.assertNotNull(proxy);

        // we need to get the field from the proxy via reflection
        // otherwise we will end up seeing the proxied method on the internal state
        Field field = proxy.getClass().getSuperclass().getDeclaredField("defaultCtInvoked");
        Assert.assertNotNull(field);
        field.setAccessible(true);
        Boolean isDefaultCtInvoked = (Boolean) field.get(proxy);
        Assert.assertTrue(isDefaultCtInvoked);

        Assert.assertTrue(proxy instanceof OwbInterceptorProxy);

        proxy.setMeaningOfLife(42);

        Assert.assertEquals(42, proxy.getMeaningOfLife());
        Assert.assertEquals(internalInstance.getFloat(), proxy.getFloat(), 0f);
        Assert.assertEquals('c', proxy.getChar());
        Assert.assertEquals(internalInstance, proxy.getSelf());

        Assert.assertEquals(5, testInvocationHandler.invokedMethodNames.size());
    }

    public static class TestInterceptorHandler implements InterceptorHandler
    {
        public List<String> invokedMethodNames = new ArrayList<String>();

        private Object instance;

        public TestInterceptorHandler(Object instance)
        {
            this.instance = instance;
        }

        @Override
        public Object invoke(Method method, Object[] args)
        {
            if (!method.getName().equals("toString"))
            {
                invokedMethodNames.add(method.getName());
            }

            System.out.println("TestInvocationHandler got properly invoked for method " + method.getName());

            try
            {
                return method.invoke(instance, args);
            }
            catch (IllegalAccessException e)
            {
                throw new WebBeansException(e);
            }
            catch (InvocationTargetException e)
            {
                throw new WebBeansException(e);
            }
        }
    }
}
