/*
 * Copyright 2006-2011 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kuali.test;

import org.kuali.rice.core.lifecycle.BaseLifecycle;
import org.kuali.rice.core.lifecycle.Lifecycle;
import org.kuali.rice.kns.service.KNSServiceLocatorInternal;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.MessageMap;
import org.kuali.rice.test.RiceInternalSuiteDataTestCase;
import org.kuali.rice.test.TransactionalLifecycle;
import org.kuali.rice.test.lifecycles.KEWXmlDataLoaderLifecycle;
import org.kuali.rice.test.lifecycles.SQLDataLoaderLifecycle;
import org.springframework.context.support.GenericXmlApplicationContext;

import java.util.List;


/**
 * Default test base for a full KNS enabled unit test.
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public abstract class KNSTestCase extends RiceInternalSuiteDataTestCase {

	private static final String SQL_FILE = "classpath:org/kuali/rice/kns/test/DefaultSuiteTestData.sql";
	private static final String XML_FILE = "classpath:org/kuali/rice/kns/test/DefaultSuiteTestData.xml";
	
	private TransactionalLifecycle transactionalLifecycle;
	
	@Override
	protected void loadSuiteTestData() throws Exception {
		super.loadSuiteTestData();
		new SQLDataLoaderLifecycle(SQL_FILE, ";").start();
	}

	@Override
	protected List<Lifecycle> getSuiteLifecycles() {
		List<Lifecycle> suiteLifecycles = super.getSuiteLifecycles();
		suiteLifecycles.add(new KEWXmlDataLoaderLifecycle(XML_FILE));
		return suiteLifecycles;
	}

	@Override
	protected Lifecycle getLoadApplicationLifecycle() {
		return new BaseLifecycle() {
			GenericXmlApplicationContext context;
            public void start() throws Exception {
                context = new GenericXmlApplicationContext("classpath:KnsTestSpringBeans.xml");
                context.start();
				super.start();
			}

            public void stop() throws Exception {
                context.stop();
            }
		};	
	}

	public void setUp() throws Exception {
        super.setUp();
		final boolean needsSpring = getClass().isAnnotationPresent(NeedsTransactionSupport.class);
		GlobalVariables.setMessageMap(new MessageMap());
		if (needsSpring) {
			transactionalLifecycle = new TransactionalLifecycle();
			transactionalLifecycle.setTransactionManager(KNSServiceLocatorInternal.getTransactionManager());
			transactionalLifecycle.start();
		}
	}
	
	public void tearDown() throws Exception {
		final boolean needsSpring = getClass().isAnnotationPresent(NeedsTransactionSupport.class);
		if (needsSpring) {
		    if ( (transactionalLifecycle != null) && (transactionalLifecycle.isStarted()) ) {
		        transactionalLifecycle.stop();
		    }
		}
		GlobalVariables.setMessageMap(new MessageMap());
		super.tearDown();
	}


	@Override
	protected String getModuleName() {
		return "kns";
	}
}
