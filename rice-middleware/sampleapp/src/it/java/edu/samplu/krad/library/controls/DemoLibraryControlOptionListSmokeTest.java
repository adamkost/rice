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
package edu.samplu.krad.library.controls;

import org.junit.Test;

import edu.samplu.common.Failable;
import edu.samplu.common.ITUtil;
import edu.samplu.common.SmokeTestBase;
import edu.samplu.common.WebDriverLegacyITBase;

/**
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class DemoLibraryControlOptionListSmokeTest extends SmokeTestBase {

    /**
     * /kr-krad/kradsampleapp?viewId=Demo-OptionList-View
     */
    public static final String BOOKMARK_URL = "/kr-krad/kradsampleapp?viewId=Demo-OptionList-View";

    @Override
    protected String getBookmarkUrl() {
        return BOOKMARK_URL;
    }

    @Override
    protected void navigate() throws Exception {
        waitAndClickById("Demo-LibraryLink", "");
        waitAndClickByLinkText("Controls");
        waitAndClickByLinkText("Option List");
    }

    protected void testLibraryControlOptionListDefault() throws Exception {
        assertElementPresentByXpath("//div[@data-parent='Demo-OptionList-Example1']/ul[@class='uif-optionList']/li");
    }
    
    protected void testLibraryControlOptionListShowOnlySelectedFlag() throws Exception {
        waitAndClickByLinkText("showOnlySelected flag");
        assertElementPresentByXpath("//div[@data-parent='Demo-OptionList-Example2']/ul[@class='uif-optionList']/li/span[@data-key='2']");
        assertElementPresentByXpath("//div[@data-parent='Demo-OptionList-Example2']/ul[@class='uif-optionList']/li/span[@data-key='4']");
    }
    
    protected void testLibraryControlOptionListNavigation() throws Exception {
        waitAndClickByXpath("//li[@data-tabfor='Demo-OptionList-Example3']/a");
        assertElementPresentByXpath("//div[@data-parent='Demo-OptionList-Example3']/ul/li/a[@href='http://www.kuali.org']");
    }
    
    @Test
    public void testControlOptionListBookmark() throws Exception {
        testLibraryControlOptionListDefault();
        testLibraryControlOptionListShowOnlySelectedFlag();
        testLibraryControlOptionListNavigation();
        passed();
    }

    @Test
    public void testControlOptionListNav() throws Exception {
        testLibraryControlOptionListDefault();
        testLibraryControlOptionListShowOnlySelectedFlag();
        testLibraryControlOptionListNavigation();
        passed();
    }  
}