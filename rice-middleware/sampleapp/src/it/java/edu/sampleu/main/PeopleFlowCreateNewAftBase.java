/**
 * Copyright 2005-2013 The Kuali Foundation
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
package edu.sampleu.main;

import org.kuali.rice.testtools.common.Failable;
import org.kuali.rice.testtools.selenium.AutomatedFunctionalTestUtils;
import org.kuali.rice.testtools.selenium.WebDriverUtil;

/**
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class PeopleFlowCreateNewAftBase extends MainTmplMthdSTNavBase{

    /**
     * ITUtil.PORTAL + "?channelTitle=People%20Flow&channelUrl="
     *  + WebDriverUtil.getBaseUrlString() + ITUtil.KRAD_LOOKUP_METHOD
     *  + "org.kuali.rice.kew.impl.peopleflow.PeopleFlowBo"
     *  + "&returnLocation=" + ITUtil.PORTAL_URL + ITUtil.SHOW_MAINTENANCE_LINKS;
     */
    public static final String BOOKMARK_URL = AutomatedFunctionalTestUtils.PORTAL + "?channelTitle=People%20Flow&channelUrl="
            + WebDriverUtil.getBaseUrlString() + AutomatedFunctionalTestUtils.KRAD_LOOKUP_METHOD
            + "org.kuali.rice.kew.impl.peopleflow.PeopleFlowBo"
            + "&returnLocation=" + AutomatedFunctionalTestUtils.PORTAL_URL + AutomatedFunctionalTestUtils.SHOW_MAINTENANCE_LINKS;
    /**
     * {@inheritDoc}
     * People Flow
     * @return
     */
    @Override
    protected String getLinkLocator() {
        return "People Flow";
    }

    public void testPeopleFlowCreateNewBookmark(Failable failable) throws Exception {
        testPeopleFlow();
        passed();
    }
    public void testPeopleFlowCreateNewNav(Failable failable) throws Exception {
        testPeopleFlow();
        passed();
    }
}