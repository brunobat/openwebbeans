/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.apache.webbeans.test.unittests.newcomp;

import java.util.List;

import junit.framework.Assert;

import org.apache.webbeans.component.AbstractBean;
import org.apache.webbeans.context.ContextFactory;
import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.component.CheckWithCheckPayment;
import org.apache.webbeans.test.component.IPayment;
import org.apache.webbeans.test.component.dependent.DependentComponent;
import org.apache.webbeans.test.component.dependent.DependentOwnerComponent;
import org.apache.webbeans.test.component.newcomp.NewComponent;
import org.apache.webbeans.test.component.newcomp.ProducerNewComponent;
import org.junit.Before;
import org.junit.Test;

public class NewComponentTest extends TestContext
{
    public NewComponentTest()
    {
        super(NewComponentTest.class.getName());
    }

    @Before
    public void init()
    {
        super.init();

    }

    @Test
    public void testDependent()
    {
        clear();

        defineManagedBean(DependentComponent.class);
        defineManagedBean(DependentOwnerComponent.class);
        defineManagedBean(NewComponent.class);

        ContextFactory.initRequestContext(null);

        List<AbstractBean<?>> comps = getComponents();

        Assert.assertEquals(3, comps.size());

        NewComponent comp = (NewComponent) getManager().getInstance(comps.get(2));

        DependentOwnerComponent own = comp.owner();

        Assert.assertNotNull(own);

        ContextFactory.destroyRequestContext(null);
    }

    @Test
    public void testDepedent2()
    {
        clear();
        defineManagedBean(CheckWithCheckPayment.class);
        defineManagedBean(ProducerNewComponent.class);

        ContextFactory.initRequestContext(null);
        Assert.assertEquals(5, getDeployedComponents());

        IPayment payment = (IPayment) getManager().getInstanceByName("paymentProducer");
        Assert.assertNotNull(payment);

        IPayment payment2 = (IPayment) getManager().getInstanceByName("paymentProducer");

        Assert.assertNotSame(payment, payment2);
    }
}
