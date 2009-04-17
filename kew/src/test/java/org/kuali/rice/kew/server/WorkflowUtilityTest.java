/*
 * Copyright 2005-2007 The Kuali Foundation.
 *
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.kew.server;


import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.kuali.rice.kew.actionrequest.ActionRequestValue;
import org.kuali.rice.kew.docsearch.DocSearchUtils;
import org.kuali.rice.kew.docsearch.TestXMLSearchableAttributeDateTime;
import org.kuali.rice.kew.docsearch.TestXMLSearchableAttributeFloat;
import org.kuali.rice.kew.docsearch.TestXMLSearchableAttributeLong;
import org.kuali.rice.kew.docsearch.TestXMLSearchableAttributeString;
import org.kuali.rice.kew.dto.ActionItemDTO;
import org.kuali.rice.kew.dto.ActionRequestDTO;
import org.kuali.rice.kew.dto.DocumentDetailDTO;
import org.kuali.rice.kew.dto.DocumentSearchCriteriaDTO;
import org.kuali.rice.kew.dto.DocumentSearchResultDTO;
import org.kuali.rice.kew.dto.DocumentSearchResultRowDTO;
import org.kuali.rice.kew.dto.KeyValueDTO;
import org.kuali.rice.kew.dto.ReportActionToTakeDTO;
import org.kuali.rice.kew.dto.ReportCriteriaDTO;
import org.kuali.rice.kew.dto.RuleDTO;
import org.kuali.rice.kew.dto.RuleExtensionDTO;
import org.kuali.rice.kew.dto.RuleReportCriteriaDTO;
import org.kuali.rice.kew.dto.RuleResponsibilityDTO;
import org.kuali.rice.kew.engine.RouteContext;
import org.kuali.rice.kew.engine.RouteHelper;
import org.kuali.rice.kew.engine.node.SimpleSplitNode;
import org.kuali.rice.kew.engine.node.SplitResult;
import org.kuali.rice.kew.exception.WorkflowException;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kew.service.WorkflowDocument;
import org.kuali.rice.kew.service.WorkflowInfo;
import org.kuali.rice.kew.service.WorkflowUtility;
import org.kuali.rice.kew.test.KEWTestCase;
import org.kuali.rice.kew.test.TestUtilities;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kew.util.KEWPropertyConstants;
import org.kuali.rice.kew.web.session.UserSession;
import org.kuali.rice.kim.bo.entity.KimPrincipal;
import org.kuali.rice.kim.bo.group.KimGroup;
import org.kuali.rice.kim.service.KIMServiceLocator;
import org.kuali.rice.kim.util.KimConstants;
import org.kuali.rice.kns.bo.Parameter;
import org.kuali.rice.kns.service.KNSServiceLocator;
import org.kuali.rice.kns.util.KNSConstants;


public class WorkflowUtilityTest extends KEWTestCase {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(WorkflowUtilityTest.class);

    private WorkflowUtility utility;

    protected void loadTestData() throws Exception {
        loadXmlFile("WorkflowUtilityConfig.xml");
    }

    protected void setUpAfterDataLoad() throws Exception {
        super.setUpAfterDataLoad();
        utility = KEWServiceLocator.getWorkflowUtilityService();
    }

    @Test public void testIsUserInRouteLog() throws Exception {
        WorkflowDocument document = new WorkflowDocument(getPrincipalIdForName("ewestfal"), SeqSetup.DOCUMENT_TYPE_NAME);
        document.routeDocument("");
        assertTrue(document.stateIsEnroute());
        assertTrue("User should be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("ewestfal"), false));
        assertTrue("User should be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("bmcgough"), false));
        assertTrue("User should be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("rkirkend"), false));
        assertFalse("User should not be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("pmckown"), false));
        assertFalse("User should not be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("temay"), false));
        assertFalse("User should not be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("jhopf"), false));
        assertTrue("User should be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("pmckown"), true));
        assertTrue("User should be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("temay"), true));
        assertTrue("User should be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("jhopf"), true));

        // test that we can run isUserInRouteLog on a SAVED document
        document = new WorkflowDocument(getPrincipalIdForName("ewestfal"), SeqSetup.DOCUMENT_TYPE_NAME);
        document.saveDocument("");
        assertTrue(document.stateIsSaved());
        assertTrue("User should be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("ewestfal"), false));
        assertFalse("User should not be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("bmcgough"), false));
        assertFalse("User should not be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("rkirkend"), false));
        assertFalse("User should not be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("pmckown"), false));
        assertFalse("User should not be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("temay"), false));
        assertFalse("User should not be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("jhopf"), false));

        // now look all up in the future of this saved document
        assertTrue("User should be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("bmcgough"), true));
        assertTrue("User should be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("rkirkend"), true));
        assertTrue("User should be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("pmckown"), true));
        assertTrue("User should be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("temay"), true));
        assertTrue("User should be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("jhopf"), true));
    }

    @Test public void testIsUserInRouteLogAfterReturnToPrevious() throws Exception {
	WorkflowDocument document = new WorkflowDocument(getPrincipalIdForName("ewestfal"), SeqSetup.DOCUMENT_TYPE_NAME);
        document.routeDocument("");
        assertTrue(document.stateIsEnroute());

        document = new WorkflowDocument(getPrincipalIdForName("bmcgough"), document.getRouteHeaderId());
        assertTrue(document.isApprovalRequested());
        document = new WorkflowDocument(getPrincipalIdForName("rkirkend"), document.getRouteHeaderId());
        assertTrue(document.isApprovalRequested());

        // bmcgough and rkirkend should be in route log
        assertTrue("User should be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("bmcgough"), false));
        assertTrue("User should be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("rkirkend"), false));
        assertTrue("User should be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("bmcgough"), true));
        assertTrue("User should be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("rkirkend"), true));
        assertFalse("User should NOT be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("pmckown"), false));
        // Phil of the future
        assertTrue("User should be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("pmckown"), true));
        TestUtilities.assertAtNode(document, "WorkflowDocument");

        document.returnToPreviousNode("", "AdHoc");
        TestUtilities.assertAtNode(document, "AdHoc");
        document = new WorkflowDocument(getPrincipalIdForName("ewestfal"), document.getRouteHeaderId());
        assertTrue(document.isApprovalRequested());

        document.approve("");

        // we should be back where we were
        document = new WorkflowDocument(getPrincipalIdForName("bmcgough"), document.getRouteHeaderId());
        assertTrue(document.isApprovalRequested());
        document = new WorkflowDocument(getPrincipalIdForName("rkirkend"), document.getRouteHeaderId());
        assertTrue(document.isApprovalRequested());
        TestUtilities.assertAtNode(document, "WorkflowDocument");

        // now verify that is route log authenticated still works
        assertTrue("User should be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("bmcgough"), false));
        assertTrue("User should be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("rkirkend"), false));
        assertTrue("User should be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("bmcgough"), true));
        assertTrue("User should be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("rkirkend"), true));
        assertFalse("User should NOT be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("pmckown"), false));
        assertTrue("User should be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("pmckown"), true));

        // let's look at the revoked node instances

        List revokedNodeInstances = KEWServiceLocator.getRouteNodeService().getRevokedNodeInstances(KEWServiceLocator.getRouteHeaderService().getRouteHeader(document.getRouteHeaderId()));
        assertNotNull(revokedNodeInstances);
        assertEquals(2, revokedNodeInstances.size());

        // let's approve past this node and another
        document = new WorkflowDocument(getPrincipalIdForName("bmcgough"), document.getRouteHeaderId());
        document.approve("");
        document = new WorkflowDocument(getPrincipalIdForName("rkirkend"), document.getRouteHeaderId());
        document.approve("");

        // should be at WorkflowDocument2
        document = new WorkflowDocument(getPrincipalIdForName("pmckown"), document.getRouteHeaderId());
        TestUtilities.assertAtNode(document, "WorkflowDocument2");
        assertTrue(document.isApprovalRequested());
        assertTrue("User should be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("bmcgough"), false));
        assertTrue("User should be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("rkirkend"), false));
        assertTrue("User should be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("bmcgough"), true));
        assertTrue("User should be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("rkirkend"), true));
        assertTrue("User should be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("pmckown"), false));
        assertTrue("User should be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("pmckown"), true));

        // now return back to WorkflowDocument
        document.returnToPreviousNode("", "WorkflowDocument");
        document = new WorkflowDocument(getPrincipalIdForName("bmcgough"), document.getRouteHeaderId());
        assertTrue(document.isApprovalRequested());
        // Phil should no longer be non-future route log authenticated
        assertTrue("User should be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("bmcgough"), false));
        assertTrue("User should be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("rkirkend"), false));
        assertTrue("User should be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("bmcgough"), true));
        assertTrue("User should be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("rkirkend"), true));
        assertFalse("User should NOT be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("pmckown"), false));
        assertTrue("User should be authenticated.", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("pmckown"), true));
    }
    
    @Test
    public void testIsUserInRouteLogWithSplits() throws Exception {
    	loadXmlFile("WorkflowUtilitySplitConfig.xml");
    	
    	// initialize the split node to both branches
    	TestSplitNode.setLeftBranch(true);
    	TestSplitNode.setRightBranch(true);
    	
    	WorkflowDocument document = new WorkflowDocument(getPrincipalIdForName("admin"), "UserInRouteLog_Split");
        document.routeDocument("");
        
        // document should be in ewestfal action list
        document = TestUtilities.switchByPrincipalName("ewestfal", document);
        assertTrue("should have approve", document.isApprovalRequested());
        TestUtilities.assertAtNode(document, "BeforeSplit");
        
        // now let's run some simulations
        assertTrue("should be in route log", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("ewestfal"), true));
        assertTrue("should be in route log", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("rkirkend"), true));
        assertTrue("should be in route log", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("bmcgough"), true));
        assertTrue("should be in route log", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("jhopf"), true));
        assertTrue("should be in route log", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("natjohns"), true));
        assertFalse("should NOT be in route log", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("user1"), true));
        
        // now let's activate only the left branch and make sure the split is properly executed
        TestSplitNode.setRightBranch(false);
        assertTrue("should be in route log", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("rkirkend"), true));
        assertTrue("should be in route log", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("bmcgough"), true));
        assertFalse("should NOT be in route log because right branch is not active", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("jhopf"), true));
        assertTrue("should be in route log", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("natjohns"), true));
        
        // now let's do a flattened evaluation, it should hit both branches
        assertTrue("should be in route log", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("rkirkend"), true, true));
        assertTrue("should be in route log", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("bmcgough"), true, true));
        assertTrue("should be in route log because we've flattened nodes", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("jhopf"), true, true));
        assertTrue("should be in route log", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("natjohns"), true, true));
        
        // now let's switch to the right branch
        TestSplitNode.setRightBranch(true);
        TestSplitNode.setLeftBranch(false);
        
        assertFalse("should NOT be in route log", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("rkirkend"), true));
        assertFalse("should NOT be in route log", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("bmcgough"), true));
        assertTrue("should be in route log", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("jhopf"), true));
        assertTrue("should be in route log", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("natjohns"), true));

        // now let's switch back to the left branch and approve it
        TestSplitNode.setLeftBranch(true);
        TestSplitNode.setRightBranch(false);
        
        // now let's approve it so that we're inside the right branch of the split
        document.approve("");
        // shoudl be at SplitLeft1 node
        TestUtilities.assertAtNode(document, "SplitLeft1");
        
        document = TestUtilities.switchByPrincipalName("rkirkend", document);
        assertTrue("should have an approve request", document.isApprovalRequested());
        
        // now let's run the simulation so we can test running from inside a split branch
        assertTrue("should be in route log", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("rkirkend"), true));
        assertTrue("should be in route log", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("bmcgough"), true));
        assertFalse("should NOT be in route log because right branch is not active", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("jhopf"), true));
        assertTrue("should be in route log", utility.isUserInRouteLog(document.getRouteHeaderId(), getPrincipalIdForName("natjohns"), true));
    }
    
    public static class TestSplitNode extends SimpleSplitNode {

    	private static boolean leftBranch = true;
    	private static boolean rightBranch = true;
    	
		@Override
		public SplitResult process(RouteContext routeContext,
				RouteHelper routeHelper) throws Exception {
			return new SplitResult(getBranchNames());
		}
		
		public List<String> getBranchNames() {
			List<String> branchNames = new ArrayList<String>();
			if (isLeftBranch()) {
				branchNames.add("Left");
			}
			if (isRightBranch()) {
				branchNames.add("Right");
			}
			return branchNames;
		}
		public static void setLeftBranch(boolean leftBranch) {
			TestSplitNode.leftBranch = leftBranch;
		}
		public static boolean isLeftBranch() {
			return TestSplitNode.leftBranch;
		}
		public static void setRightBranch(boolean rightBranch) {
			TestSplitNode.rightBranch = rightBranch;
		}		
		public static boolean isRightBranch() {
			return TestSplitNode.rightBranch;
		}  	
    }

    public abstract interface ReportCriteriaGenerator { public abstract ReportCriteriaDTO buildCriteria(WorkflowDocument workflowDoc) throws Exception; public boolean isCriteriaRouteHeaderBased();}

    private class ReportCriteriaGeneratorUsingXML implements ReportCriteriaGenerator {
        public ReportCriteriaDTO buildCriteria(WorkflowDocument workflowDoc) throws Exception {
            ReportCriteriaDTO criteria = new ReportCriteriaDTO(workflowDoc.getDocumentType());
            criteria.setXmlContent(workflowDoc.getDocumentContent().getApplicationContent());
            return criteria;
        }
        public boolean isCriteriaRouteHeaderBased() {
            return false;
        }
    }

    private class ReportCriteriaGeneratorUsingRouteHeaderId implements ReportCriteriaGenerator {
        public ReportCriteriaDTO buildCriteria(WorkflowDocument workflowDoc) throws Exception {
            ReportCriteriaDTO criteria = new ReportCriteriaDTO(workflowDoc.getRouteHeaderId());
            return criteria;
        }
        public boolean isCriteriaRouteHeaderBased() {
            return true;
        }
    }

    @Test public void testDocumentWillHaveApproveOrCompleteRequestAtNode_RouteHeaderId() throws Exception {
        runDocumentWillHaveApproveOrCompleteRequestAtNode(SeqSetup.DOCUMENT_TYPE_NAME,new ReportCriteriaGeneratorUsingRouteHeaderId());
    }

    @Test public void testDocumentWillHaveApproveOrCompleteRequestAtNode_XmlContent() throws Exception {
        runDocumentWillHaveApproveOrCompleteRequestAtNode(SeqSetup.DOCUMENT_TYPE_NAME,new ReportCriteriaGeneratorUsingXML());
    }

    @Test public void testDocumentWillHaveApproveOrCompleteRequestAtNode_ForceAction_RouteHeaderId() throws Exception {
        runDocumentWillHaveApproveOrCompleteRequestAtNode_ForceAction("SimulationTestDocumenType_ForceAction",new ReportCriteriaGeneratorUsingRouteHeaderId());
    }

    @Test public void testDocumentWillHaveApproveOrCompleteRequestAtNode_ForceAction_XmlContent() throws Exception {
        runDocumentWillHaveApproveOrCompleteRequestAtNode_ForceAction("SimulationTestDocumenType_ForceAction",new ReportCriteriaGeneratorUsingXML());
    }

    private void runDocumentWillHaveApproveOrCompleteRequestAtNode_ForceAction(String documentType, ReportCriteriaGenerator generator) throws Exception {
      /*
        name="WorkflowDocument"
          -  rkirkend - Approve - false
        name="WorkflowDocument2"
          -  rkirkend - Approve - false
        name="WorkflowDocument3"
          -  rkirkend - Approve - true
        name="WorkflowDocument4"
          -  rkirkend - Approve - false
          -  jitrue   - Approve - true
      */
        ReportCriteriaDTO reportCriteriaDTO = generator.buildCriteria(new WorkflowDocument(getPrincipalIdForName("ewestfal"), documentType));
        reportCriteriaDTO.setTargetNodeName("WorkflowDocument2");
        reportCriteriaDTO.setRoutingPrincipalId(getPrincipalIdForName("bmcgough"));
        assertTrue("Document should have at least one unfulfilled approve/complete request",utility.documentWillHaveAtLeastOneActionRequest(reportCriteriaDTO, new String[]{KEWConstants.ACTION_REQUEST_APPROVE_REQ,KEWConstants.ACTION_REQUEST_COMPLETE_REQ}, false));
        reportCriteriaDTO.setTargetPrincipalIds(new String[]{getPrincipalIdForName("bmcgough")});
        assertFalse("Document should not have any unfulfilled approve/complete requests",utility.documentWillHaveAtLeastOneActionRequest(reportCriteriaDTO, new String[]{KEWConstants.ACTION_REQUEST_APPROVE_REQ,KEWConstants.ACTION_REQUEST_COMPLETE_REQ}, false));

        reportCriteriaDTO = generator.buildCriteria(new WorkflowDocument(getPrincipalIdForName("ewestfal"), documentType));
        reportCriteriaDTO.setTargetNodeName("WorkflowDocument4");
        reportCriteriaDTO.setRoutingPrincipalId(getPrincipalIdForName("bmcgough"));
        ReportActionToTakeDTO[] actionsToTake = new ReportActionToTakeDTO[2];
        actionsToTake[0] = new ReportActionToTakeDTO(KEWConstants.ACTION_TAKEN_APPROVED_CD,getPrincipalIdForName("rkirkend"),"WorkflowDocument3");
        actionsToTake[1] = new ReportActionToTakeDTO(KEWConstants.ACTION_TAKEN_APPROVED_CD,getPrincipalIdForName("jitrue"),"WorkflowDocument4");
        reportCriteriaDTO.setActionsToTake(actionsToTake);
        assertFalse("Document should not have any unfulfilled approve/complete requests",utility.documentWillHaveAtLeastOneActionRequest(reportCriteriaDTO, new String[]{KEWConstants.ACTION_REQUEST_APPROVE_REQ,KEWConstants.ACTION_REQUEST_COMPLETE_REQ}, false));

        reportCriteriaDTO = generator.buildCriteria(new WorkflowDocument(getPrincipalIdForName("ewestfal"), documentType));
        reportCriteriaDTO.setRoutingPrincipalId(getPrincipalIdForName("pmckown"));
        reportCriteriaDTO.setTargetNodeName("WorkflowDocument4");
        actionsToTake = new ReportActionToTakeDTO[2];
        actionsToTake[0] = new ReportActionToTakeDTO(KEWConstants.ACTION_TAKEN_APPROVED_CD,getPrincipalIdForName("rkirkend"),"WorkflowDocument3");
        actionsToTake[1] = new ReportActionToTakeDTO(KEWConstants.ACTION_TAKEN_APPROVED_CD,getPrincipalIdForName("jitrue"),"WorkflowDocument4");
        reportCriteriaDTO.setActionsToTake(actionsToTake);
        assertFalse("Document should not have any unfulfilled approve/complete requests",utility.documentWillHaveAtLeastOneActionRequest(reportCriteriaDTO, new String[]{KEWConstants.ACTION_REQUEST_APPROVE_REQ,KEWConstants.ACTION_REQUEST_COMPLETE_REQ}, false));

        WorkflowDocument document = new WorkflowDocument(getPrincipalIdForName("rkirkend"), documentType);
        reportCriteriaDTO = generator.buildCriteria(document);
        reportCriteriaDTO.setRoutingPrincipalId(getPrincipalIdForName("rkirkend"));
        reportCriteriaDTO.setTargetNodeName("WorkflowDocument");
        assertFalse("Document should not have any approve/complete requests",utility.documentWillHaveAtLeastOneActionRequest(reportCriteriaDTO, new String[]{KEWConstants.ACTION_REQUEST_APPROVE_REQ,KEWConstants.ACTION_REQUEST_COMPLETE_REQ}, false));

        reportCriteriaDTO = generator.buildCriteria(document);
        reportCriteriaDTO.setRoutingPrincipalId(getPrincipalIdForName("rkirkend"));
        reportCriteriaDTO.setTargetNodeName("WorkflowDocument2");
        assertFalse("Document should not have any approve/complete requests",utility.documentWillHaveAtLeastOneActionRequest(reportCriteriaDTO, new String[]{KEWConstants.ACTION_REQUEST_APPROVE_REQ,KEWConstants.ACTION_REQUEST_COMPLETE_REQ}, false));

        reportCriteriaDTO = generator.buildCriteria(document);
        reportCriteriaDTO.setRoutingPrincipalId(getPrincipalIdForName("rkirkend"));
        reportCriteriaDTO.setTargetPrincipalIds(new String[]{getPrincipalIdForName("rkirkend")});
        assertFalse("Document should not have any approve/complete requests for user rkirkend",utility.documentWillHaveAtLeastOneActionRequest(reportCriteriaDTO, new String[]{KEWConstants.ACTION_REQUEST_APPROVE_REQ,KEWConstants.ACTION_REQUEST_COMPLETE_REQ}, false));

        document.routeDocument("");
        assertEquals("Document should be enroute", KEWConstants.ROUTE_HEADER_ENROUTE_CD, document.getRouteHeader().getDocRouteStatus());
        assertEquals("Document route node is incorrect", "WorkflowDocument3", document.getNodeNames()[0]);
        reportCriteriaDTO = generator.buildCriteria(document);
        reportCriteriaDTO.setTargetNodeName("WorkflowDocument4");
        assertTrue("At least one unfulfilled approve/complete request should have been generated",utility.documentWillHaveAtLeastOneActionRequest(reportCriteriaDTO, new String[]{KEWConstants.ACTION_REQUEST_APPROVE_REQ,KEWConstants.ACTION_REQUEST_COMPLETE_REQ}, false));

        reportCriteriaDTO = generator.buildCriteria(document);
        reportCriteriaDTO.setTargetPrincipalIds(new String[]{getPrincipalIdForName("rkirkend")});
        assertTrue("At least one unfulfilled approve/complete request should have been generated for rkirkend",utility.documentWillHaveAtLeastOneActionRequest(reportCriteriaDTO, new String[]{KEWConstants.ACTION_REQUEST_APPROVE_REQ,KEWConstants.ACTION_REQUEST_COMPLETE_REQ}, false));

        reportCriteriaDTO = generator.buildCriteria(document);
        reportCriteriaDTO.setTargetNodeName("WorkflowDocument4");
        assertTrue("At least one unfulfilled approve/complete request should have been generated",utility.documentWillHaveAtLeastOneActionRequest(reportCriteriaDTO, new String[]{KEWConstants.ACTION_REQUEST_APPROVE_REQ,KEWConstants.ACTION_REQUEST_COMPLETE_REQ}, false));

        // if rkirkend approvers the document here it will move to last route node and no more simulations need to be run
        document = new WorkflowDocument(getPrincipalIdForName("rkirkend"), document.getRouteHeaderId());
        document.approve("");
        assertEquals("Document should be enroute", KEWConstants.ROUTE_HEADER_ENROUTE_CD, document.getRouteHeader().getDocRouteStatus());
        assertEquals("Document route node is incorrect", "WorkflowDocument4", document.getNodeNames()[0]);
    }

    private void runDocumentWillHaveApproveOrCompleteRequestAtNode(String documentType,ReportCriteriaGenerator generator) throws Exception {
      /*
        name="WorkflowDocument"
          -  bmcgough - Approve - false
          -  rkirkend - Approve - false
        name="WorkflowDocument2"
          -  pmckown - Approve - false
        name="Acknowledge1"
          -  temay - Ack - false
        name="Acknowledge2"
          -  jhopf - Ack - false
      */
        WorkflowDocument document = new WorkflowDocument(getPrincipalIdForName("ewestfal"), documentType);

        ReportCriteriaDTO reportCriteriaDTO = generator.buildCriteria(document);
//        ReportCriteriaDTO reportCriteriaDTO = new ReportCriteriaDTO(document.getRouteHeaderId());
        reportCriteriaDTO.setTargetNodeName("WorkflowDocument2");
        reportCriteriaDTO.setRoutingPrincipalId(getPrincipalIdForName("bmcgough"));
        assertTrue("Document should have one unfulfilled approve/complete request",utility.documentWillHaveAtLeastOneActionRequest(reportCriteriaDTO, new String[]{KEWConstants.ACTION_REQUEST_APPROVE_REQ,KEWConstants.ACTION_REQUEST_COMPLETE_REQ}, false));
        reportCriteriaDTO.setTargetPrincipalIds(new String[]{getPrincipalIdForName("bmcgough")});
        assertFalse("Document should not have any unfulfilled approve/complete requests",utility.documentWillHaveAtLeastOneActionRequest(reportCriteriaDTO, new String[]{KEWConstants.ACTION_REQUEST_APPROVE_REQ,KEWConstants.ACTION_REQUEST_COMPLETE_REQ}, false));

        reportCriteriaDTO = generator.buildCriteria(document);
        reportCriteriaDTO.setTargetNodeName("WorkflowDocument2");
        reportCriteriaDTO.setRoutingPrincipalId(getPrincipalIdForName("bmcgough"));
        ReportActionToTakeDTO[] actionsToTake = new ReportActionToTakeDTO[1];
//        actionsToTake[0] = new ReportActionToTakeDTO(KEWConstants.ACTION_TAKEN_APPROVED_CD,getPrincipalIdForName("rkirkend"),"WorkflowDocument");
        actionsToTake[0] = new ReportActionToTakeDTO(KEWConstants.ACTION_TAKEN_APPROVED_CD,getPrincipalIdForName("pmckown"),"WorkflowDocument2");
        reportCriteriaDTO.setActionsToTake(actionsToTake);
        assertFalse("Document should not have any unfulfilled approve/complete requests",utility.documentWillHaveAtLeastOneActionRequest(reportCriteriaDTO, new String[]{KEWConstants.ACTION_REQUEST_APPROVE_REQ,KEWConstants.ACTION_REQUEST_COMPLETE_REQ}, false));

        reportCriteriaDTO = generator.buildCriteria(document);
        reportCriteriaDTO.setTargetNodeName("WorkflowDocument2");
        actionsToTake = new ReportActionToTakeDTO[2];
        actionsToTake[0] = new ReportActionToTakeDTO(KEWConstants.ACTION_TAKEN_APPROVED_CD,getPrincipalIdForName("bmcgough"),"WorkflowDocument");
        actionsToTake[1] = new ReportActionToTakeDTO(KEWConstants.ACTION_TAKEN_APPROVED_CD,getPrincipalIdForName("rkirkend"),"WorkflowDocument");
        reportCriteriaDTO.setActionsToTake(actionsToTake);
        reportCriteriaDTO.setRoutingPrincipalId(getPrincipalIdForName("pmckown"));
        assertFalse("Document should not have any unfulfilled approve/complete requests",utility.documentWillHaveAtLeastOneActionRequest(reportCriteriaDTO, new String[]{KEWConstants.ACTION_REQUEST_APPROVE_REQ,KEWConstants.ACTION_REQUEST_COMPLETE_REQ}, false));

        document = new WorkflowDocument(getPrincipalIdForName("ewestfal"), documentType);
        document.routeDocument("");
        assertTrue(document.stateIsEnroute());

        document = new WorkflowDocument(getPrincipalIdForName("bmcgough"), document.getRouteHeaderId());
        document.approve("");

        document = new WorkflowDocument(getPrincipalIdForName("rkirkend"), document.getRouteHeaderId());
        document.approve("");

        reportCriteriaDTO = generator.buildCriteria(document);
        reportCriteriaDTO.setTargetNodeName("WorkflowDocument2");
        assertTrue("Document should have one unfulfilled approve/complete request",utility.documentWillHaveAtLeastOneActionRequest(reportCriteriaDTO, new String[]{KEWConstants.ACTION_REQUEST_APPROVE_REQ,KEWConstants.ACTION_REQUEST_COMPLETE_REQ}, false));

        document = new WorkflowDocument(getPrincipalIdForName("pmckown"), document.getRouteHeaderId());
        document.approve("");
        assertTrue(document.stateIsProcessed());

        reportCriteriaDTO = generator.buildCriteria(document);
        reportCriteriaDTO.setTargetNodeName("Acknowledge1");
        assertFalse("Document should not have any unfulfilled approve/complete requests when in processed status",utility.documentWillHaveAtLeastOneActionRequest(reportCriteriaDTO, new String[]{KEWConstants.ACTION_REQUEST_APPROVE_REQ,KEWConstants.ACTION_REQUEST_COMPLETE_REQ}, false));

        reportCriteriaDTO = generator.buildCriteria(document);
        reportCriteriaDTO.setTargetNodeName("Acknowledge1");
        assertTrue("Document should have one unfulfilled Ack request when in final status",utility.documentWillHaveAtLeastOneActionRequest(reportCriteriaDTO, new String[]{KEWConstants.ACTION_REQUEST_ACKNOWLEDGE_REQ}, false));
        if (generator.isCriteriaRouteHeaderBased()) {
            assertFalse("Document should have no unfulfilled Ack request generated when in final status",utility.documentWillHaveAtLeastOneActionRequest(reportCriteriaDTO, new String[]{KEWConstants.ACTION_REQUEST_ACKNOWLEDGE_REQ}, true));
        }

        // if temay acknowledges the document here it will move to processed and no more simulations would need to be tested
        document = new WorkflowDocument(getPrincipalIdForName("temay"), document.getRouteHeaderId());
        document.acknowledge("");
        assertTrue(document.stateIsProcessed());
    }

    @Test public void testIsLastApprover() throws Exception {
        // test the is last approver in route level against our sequential document type
        WorkflowDocument document = new WorkflowDocument(getPrincipalIdForName("ewestfal"), SeqSetup.DOCUMENT_TYPE_NAME);
        document.saveRoutingData();

        // the initial "route level" should have no requests initially so it should return false
        assertFalse("Should not be last approver.", utility.isLastApproverInRouteLevel(document.getRouteHeaderId(), getPrincipalIdForName("ewestfal"), new Integer(0)));
        assertFalse("Should not be last approver.", utility.isLastApproverAtNode(document.getRouteHeaderId(), getPrincipalIdForName("ewestfal"), SeqSetup.ADHOC_NODE));

        // app specific route a request to a workgroup at the initial node (TestWorkgroup)
		String groupId = getGroupIdForName(KimConstants.KIM_GROUP_WORKFLOW_NAMESPACE_CODE, "TestWorkgroup");
        document.adHocRouteDocumentToGroup(KEWConstants.ACTION_REQUEST_APPROVE_REQ, "AdHoc", "", groupId, "", false);
        assertTrue("Should be last approver.", utility.isLastApproverInRouteLevel(document.getRouteHeaderId(), getPrincipalIdForName("ewestfal"), new Integer(0)));
        assertTrue("Should be last approver.", utility.isLastApproverAtNode(document.getRouteHeaderId(), getPrincipalIdForName("ewestfal"), SeqSetup.ADHOC_NODE));

        // app specific route a request to a member of the workgroup (jitrue)
        document.adHocRouteDocumentToPrincipal(KEWConstants.ACTION_REQUEST_APPROVE_REQ, "AdHoc", "", getPrincipalIdForName("jitrue"), "", false);
        // member of the workgroup with the user request should be last approver
        assertTrue("Should be last approver.", utility.isLastApproverInRouteLevel(document.getRouteHeaderId(), getPrincipalIdForName("jitrue"), new Integer(0)));
        assertTrue("Should be last approver.", utility.isLastApproverAtNode(document.getRouteHeaderId(), getPrincipalIdForName("jitrue"), SeqSetup.ADHOC_NODE));
        // other members of the workgroup will not be last approvers because they don't satisfy the individuals request (ewestfal)
        assertFalse("Should not be last approver.", utility.isLastApproverAtNode(document.getRouteHeaderId(), getPrincipalIdForName("ewestfal"), SeqSetup.ADHOC_NODE));

        // route the document, should stay at the adhoc node until those requests have been completed
        document.routeDocument("");
        document = new WorkflowDocument(getPrincipalIdForName("jitrue"), document.getRouteHeaderId());
        assertEquals("Document should be at adhoc node.", SeqSetup.ADHOC_NODE, document.getNodeNames()[0]);
        assertTrue("Approve should be requested.", document.isApprovalRequested());
        document.approve("");

        // document should now be at the WorkflowDocument node with a request to bmcgough and rkirkend
        document = new WorkflowDocument(getPrincipalIdForName("bmcgough"), document.getRouteHeaderId());
        assertTrue("Approve should be requested.", document.isApprovalRequested());
        document = new WorkflowDocument(getPrincipalIdForName("rkirkend"), document.getRouteHeaderId());
        assertTrue("Approve should be requested.", document.isApprovalRequested());
        // since there are two requests, neither should be last approver
        assertFalse("Should not be last approver.", utility.isLastApproverAtNode(document.getRouteHeaderId(), getPrincipalIdForName("bmcgough"), SeqSetup.WORKFLOW_DOCUMENT_NODE));
        assertFalse("Should not be last approver.", utility.isLastApproverAtNode(document.getRouteHeaderId(), getPrincipalIdForName("rkirkend"), SeqSetup.WORKFLOW_DOCUMENT_NODE));
        document.approve("");

        // request to rirkend has been satisfied, now request to bmcgough is only request remaining at level so he should be last approver
        document = new WorkflowDocument(getPrincipalIdForName("bmcgough"), document.getRouteHeaderId());
        assertTrue("Approve should be requested.", document.isApprovalRequested());
        assertTrue("Should be last approver.", utility.isLastApproverAtNode(document.getRouteHeaderId(), getPrincipalIdForName("bmcgough"), SeqSetup.WORKFLOW_DOCUMENT_NODE));
        document.approve("");

    }

    /**
     * This method tests how the isLastApproverAtNode method deals with force action requests, there is an app constant
     * with the value specified in KEWConstants.IS_LAST_APPROVER_ACTIVATE_FIRST which dictates whether or not to simulate
     * activation of initialized requests before running the method.
     *
     * Tests the fix to issue http://fms.dfa.cornell.edu:8080/browse/KULWF-366
     */
    @Test public void testIsLastApproverActivation() throws Exception {
        // first test without the constant set
        //ApplicationConstant appConstant = KEWServiceLocator.getApplicationConstantsService().findByName(KEWConstants.IS_LAST_APPROVER_ACTIVATE_FIRST);
        Parameter lastApproverActivateParameter = KNSServiceLocator.getKualiConfigurationService().getParameterWithoutExceptions(KEWConstants.KEW_NAMESPACE, KNSConstants.DetailTypes.FEATURE_DETAIL_TYPE, KEWConstants.IS_LAST_APPROVER_ACTIVATE_FIRST_IND);
        assertNotNull("last approver parameter should exist.", lastApproverActivateParameter);
        assertTrue("initial parameter value should be null or empty.", StringUtils.isBlank(lastApproverActivateParameter.getParameterValue()));
        String originalParameterValue = lastApproverActivateParameter.getParameterValue();
        
        WorkflowDocument document = new WorkflowDocument(getPrincipalIdForName("ewestfal"), SeqSetup.LAST_APPROVER_DOCUMENT_TYPE_NAME);
        document.routeDocument("");

        // at the first node (WorkflowDocument) we should have a request to rkirkend, bmcgough and to ewestfal with forceAction=true,
        assertEquals("We should be at the WorkflowDocument node.", SeqSetup.WORKFLOW_DOCUMENT_NODE, document.getNodeNames()[0]);
        assertFalse("ewestfal should have not have approve because it's initiated", document.isApprovalRequested());
        document = new WorkflowDocument(getPrincipalIdForName("rkirkend"), document.getRouteHeaderId());
        assertFalse("rkirkend should not have approve because it's initiated", document.isApprovalRequested());
        document = new WorkflowDocument(getPrincipalIdForName("bmcgough"), document.getRouteHeaderId());
        assertTrue("bmcgough should have approve", document.isApprovalRequested());
        List actionRequests = KEWServiceLocator.getActionRequestService().findPendingByDoc(document.getRouteHeaderId());
        assertEquals("Should be 3 pending requests.", 3, actionRequests.size());
        // the requests to bmcgough should be activated, the request to rkirkend should be initialized,
        // and the request to ewestfal should be initialized and forceAction=true
        boolean foundBmcgoughRequest = false;
        boolean foundRkirkendRequest = false;
        boolean foundEwestfalRequest = false;
        for (Iterator iterator = actionRequests.iterator(); iterator.hasNext();) {
            ActionRequestValue actionRequest = (ActionRequestValue) iterator.next();
            String netId = getPrincipalNameForId(actionRequest.getPrincipalId());
            if ("bmcgough".equals(netId)) {
                assertTrue("Request to bmcgough should be activated.", actionRequest.isActive());
                foundBmcgoughRequest = true;
            } else if ("rkirkend".equals(netId)) {
                assertTrue("Request to rkirkend should be initialized.", actionRequest.isInitialized());
                foundRkirkendRequest = true;
            } else if ("ewestfal".equals(netId)) {
                assertTrue("Request to ewestfal should be initialized.", actionRequest.isInitialized());
                assertTrue("Request to ewestfal should be forceAction.", actionRequest.getForceAction().booleanValue());
                foundEwestfalRequest = true;
            }
        }
        assertTrue("Did not find request to bmcgough.", foundBmcgoughRequest);
        assertTrue("Did not find request to rkirkend.", foundRkirkendRequest);
        assertTrue("Did not find request to ewestfal.", foundEwestfalRequest);

        // at this point, neither bmcgough, rkirkend nor ewestfal should be the last approver
        assertFalse("Bmcgough should not be the final approver.", utility.isLastApproverAtNode(document.getRouteHeaderId(), getPrincipalIdForName("bmcgough"), SeqSetup.WORKFLOW_DOCUMENT_NODE));
        assertFalse("Rkirkend should not be the final approver.", utility.isLastApproverAtNode(document.getRouteHeaderId(), getPrincipalIdForName("rkirkend"), SeqSetup.WORKFLOW_DOCUMENT_NODE));
        assertFalse("Ewestfal should not be the final approver.", utility.isLastApproverAtNode(document.getRouteHeaderId(), getPrincipalIdForName("ewestfal"), SeqSetup.WORKFLOW_DOCUMENT_NODE));

        // approve as bmcgough
        document = new WorkflowDocument(getPrincipalIdForName("bmcgough"), document.getRouteHeaderId());
        document.approve("");

        // still, neither rkirkend nor ewestfal should be "final approver"
        // at this point, neither bmcgough, rkirkend nor ewestfal should be the last approver
        assertFalse("Rkirkend should not be the final approver.", utility.isLastApproverAtNode(document.getRouteHeaderId(), getPrincipalIdForName("rkirkend"), SeqSetup.WORKFLOW_DOCUMENT_NODE));
        assertFalse("Ewestfal should not be the final approver.", utility.isLastApproverAtNode(document.getRouteHeaderId(), getPrincipalIdForName("ewestfal"), SeqSetup.WORKFLOW_DOCUMENT_NODE));

        // approve as rkirkend
        document = new WorkflowDocument(getPrincipalIdForName("rkirkend"), document.getRouteHeaderId());
        document.approve("");

        // should be one pending activated to ewestfal now
        actionRequests = KEWServiceLocator.getActionRequestService().findPendingByDoc(document.getRouteHeaderId());
        assertEquals("Should be 1 pending requests.", 1, actionRequests.size());
        ActionRequestValue actionRequest = (ActionRequestValue)actionRequests.get(0);
        assertTrue("Should be activated.", actionRequest.isActive());

        // ewestfal should now be the final approver
        assertTrue("Ewestfal should be the final approver.", utility.isLastApproverAtNode(document.getRouteHeaderId(), getPrincipalIdForName("ewestfal"), SeqSetup.WORKFLOW_DOCUMENT_NODE));

        // approve as ewestfal to send to next node
        document = new WorkflowDocument(getPrincipalIdForName("ewestfal"), document.getRouteHeaderId());
        assertTrue("ewestfal should have approve request", document.isApprovalRequested());
        document.approve("");

        // should be at the workflow document 2 node
        assertEquals("Should be at the WorkflowDocument2 Node.", SeqSetup.WORKFLOW_DOCUMENT_2_NODE, document.getNodeNames()[0]);
        // at this node there should be two requests, one to ewestfal with forceAction=false and one to pmckown,
        // since we haven't set the application constant, the non-force action request won't be activated first so pmckown
        // will not be the final approver
        assertFalse("Pmckown should not be the final approver.", utility.isLastApproverAtNode(document.getRouteHeaderId(), getPrincipalIdForName("pmckown"), SeqSetup.WORKFLOW_DOCUMENT_2_NODE));
        assertFalse("Ewestfal should not be the final approver.", utility.isLastApproverAtNode(document.getRouteHeaderId(), getPrincipalIdForName("ewestfal"), SeqSetup.WORKFLOW_DOCUMENT_2_NODE));
        actionRequests = KEWServiceLocator.getActionRequestService().findPendingByDoc(document.getRouteHeaderId());
        assertEquals("Should be 2 action requests.", 2, actionRequests.size());

        // Now set up the app constant that checks force action properly and try a new document
        String parameterValue = "Y";
        lastApproverActivateParameter.setParameterValue(parameterValue);
        KNSServiceLocator.getBusinessObjectService().save(lastApproverActivateParameter);

        lastApproverActivateParameter = KNSServiceLocator.getKualiConfigurationService().getParameterWithoutExceptions(KEWConstants.KEW_NAMESPACE, KNSConstants.DetailTypes.FEATURE_DETAIL_TYPE, KEWConstants.IS_LAST_APPROVER_ACTIVATE_FIRST_IND);
        assertNotNull("Parameter should not be null.", lastApproverActivateParameter);
        assertEquals("Parameter should be Y.", parameterValue, lastApproverActivateParameter.getParameterValue());


        document = new WorkflowDocument(getPrincipalIdForName("ewestfal"), SeqSetup.LAST_APPROVER_DOCUMENT_TYPE_NAME);
        document.routeDocument("");

        // at this point, neither bmcgough, rkirkend nor ewestfal should be the last approver
        assertFalse("Bmcgough should not be the final approver.", utility.isLastApproverAtNode(document.getRouteHeaderId(), getPrincipalIdForName("bmcgough"), SeqSetup.WORKFLOW_DOCUMENT_NODE));
        assertFalse("Rkirkend should not be the final approver.", utility.isLastApproverAtNode(document.getRouteHeaderId(), getPrincipalIdForName("rkirkend"), SeqSetup.WORKFLOW_DOCUMENT_NODE));
        assertFalse("Ewestfal should not be the final approver.", utility.isLastApproverAtNode(document.getRouteHeaderId(), getPrincipalIdForName("ewestfal"), SeqSetup.WORKFLOW_DOCUMENT_NODE));

        // approve as bmcgough
        document = new WorkflowDocument(getPrincipalIdForName("bmcgough"), document.getRouteHeaderId());
        document.approve("");

        // now there is just a request to rkirkend and ewestfal, since ewestfal is force action true, neither should be final approver
        assertFalse("Rkirkend should not be the final approver.", utility.isLastApproverAtNode(document.getRouteHeaderId(), getPrincipalIdForName("rkirkend"), SeqSetup.WORKFLOW_DOCUMENT_NODE));
        assertFalse("Ewestfal should not be the final approver.", utility.isLastApproverAtNode(document.getRouteHeaderId(), getPrincipalIdForName("ewestfal"), SeqSetup.WORKFLOW_DOCUMENT_NODE));

        // approve as ewestfal
        document = new WorkflowDocument(getPrincipalIdForName("ewestfal"), document.getRouteHeaderId());
        document.approve("");

        // rkirkend should now be the final approver
        assertTrue("Rkirkend should now be the final approver.", utility.isLastApproverAtNode(document.getRouteHeaderId(), getPrincipalIdForName("rkirkend"), SeqSetup.WORKFLOW_DOCUMENT_NODE));

        // approve as rkirkend to send it to the next node
        document = new WorkflowDocument(getPrincipalIdForName("rkirkend"), document.getRouteHeaderId());
        document.approve("");

        TestUtilities.assertAtNode(document, SeqSetup.WORKFLOW_DOCUMENT_2_NODE);
        List<ActionRequestValue> requests = KEWServiceLocator.getActionRequestService().findPendingRootRequestsByDocId(document.getRouteHeaderId());
        assertEquals("We should have 2 requests here.", 2, requests.size());
        
        // now, there are requests to pmckown and ewestfal here, the request to ewestfal is forceAction=false and since ewestfal
        // routed the document, this request should be auto-approved.  However, it's priority is 2 so it is activated after the
        // request to pmckown which is the situation we are testing
        assertTrue("Pmckown should be the last approver at this node.", utility.isLastApproverAtNode(document.getRouteHeaderId(), getPrincipalIdForName("pmckown"), SeqSetup.WORKFLOW_DOCUMENT_2_NODE));
        assertFalse("Ewestfal should not be the final approver.", utility.isLastApproverAtNode(document.getRouteHeaderId(), getPrincipalIdForName("ewestfal"), SeqSetup.WORKFLOW_DOCUMENT_2_NODE));

        // if we approve as pmckown, the document should go into acknowledgement and become processed
        document = new WorkflowDocument(getPrincipalIdForName("pmckown"), document.getRouteHeaderId());
        document.approve("");
        assertTrue("Document should be processed.", document.stateIsProcessed());

        // set parameter value back to it's original value
        
        lastApproverActivateParameter.setParameterValue("N");
        KNSServiceLocator.getBusinessObjectService().save(lastApproverActivateParameter);
    }

    @Test public void testIsFinalApprover() throws Exception {
        // for this document, pmckown should be the final approver
        WorkflowDocument document = new WorkflowDocument(getPrincipalIdForName("ewestfal"), SeqSetup.DOCUMENT_TYPE_NAME);
        assertFinalApprover(document);
    }

    @Test public void testIsFinalApproverChild() throws Exception {
        // 12-13-2005: HR ran into a bug where this method was not correctly locating the final approver node when using a document type whic
        // inherits the route from a parent, so we will incorporate this into the unit test to prevent regression
        WorkflowDocument childDocument = new WorkflowDocument(getPrincipalIdForName("ewestfal"), SeqSetup.CHILD_DOCUMENT_TYPE_NAME);
        assertFinalApprover(childDocument);
    }

    /**
     * Factored out so as not to duplicate a bunch of code between testIsFinalApprover and testIsFinalApproverChild.
     */
    private void assertFinalApprover(WorkflowDocument document) throws Exception {
        document.routeDocument("");

        document = new WorkflowDocument(getPrincipalIdForName("bmcgough"), document.getRouteHeaderId());
        assertTrue("Document should be enroute.", document.stateIsEnroute());
        assertTrue("Should have approve request.", document.isApprovalRequested());

        // bmcgough is not the final approver
        assertFalse("Should not be final approver.", utility.isFinalApprover(document.getRouteHeaderId(), getPrincipalIdForName("bmcgough")));
        // approve as bmcgough
        document.approve("");

        // should be to Ryan now, who is also not the final approver on the document
        document = new WorkflowDocument(getPrincipalIdForName("rkirkend"), document.getRouteHeaderId());
        assertTrue("Document should be enroute.", document.stateIsEnroute());
        assertTrue("Should have approve request.", document.isApprovalRequested());
        assertFalse("Should not be final approver.", utility.isFinalApprover(document.getRouteHeaderId(), getPrincipalIdForName("rkirkend")));
        document.approve("");

        // should be to Phil now, who *IS* the final approver on the document
        document = new WorkflowDocument(getPrincipalIdForName("pmckown"), document.getRouteHeaderId());
        assertTrue("Document should be enroute.", document.stateIsEnroute());
        assertTrue("Should have approve request.", document.isApprovalRequested());
        assertTrue("Should be final approver.", utility.isFinalApprover(document.getRouteHeaderId(), getPrincipalIdForName("pmckown")));

        // now adhoc an approve to temay, phil should no longer be the final approver
        document.adHocRouteDocumentToPrincipal(KEWConstants.ACTION_REQUEST_APPROVE_REQ, SeqSetup.WORKFLOW_DOCUMENT_2_NODE,
                "", getPrincipalIdForName("temay"), "", true);
        assertFalse("Should not be final approver.", utility.isFinalApprover(document.getRouteHeaderId(), getPrincipalIdForName("pmckown")));
        assertFalse("Should not be final approver.", utility.isFinalApprover(document.getRouteHeaderId(), getPrincipalIdForName("temay")));

        // now approve as temay and then adhoc an ack to jeremy
        document = new WorkflowDocument(getPrincipalIdForName("temay"), document.getRouteHeaderId());
        assertTrue("SHould have approve request.", document.isApprovalRequested());
        document.approve("");

        // phil should be final approver again
        assertTrue("Should be final approver.", utility.isFinalApprover(document.getRouteHeaderId(), getPrincipalIdForName("pmckown")));
        document.adHocRouteDocumentToPrincipal(KEWConstants.ACTION_REQUEST_ACKNOWLEDGE_REQ, SeqSetup.WORKFLOW_DOCUMENT_2_NODE,
                "", getPrincipalIdForName("jhopf"), "", true);
        document = new WorkflowDocument(getPrincipalIdForName("jhopf"), document.getRouteHeaderId());
        assertTrue("Should have acknowledge request.", document.isAcknowledgeRequested());

        // now there should be an approve to phil and an ack to jeremy, so phil should be the final approver and jeremy should not
        assertTrue("Should be final approver.", utility.isFinalApprover(document.getRouteHeaderId(), getPrincipalIdForName("pmckown")));
        assertFalse("Should not be final approver.", utility.isFinalApprover(document.getRouteHeaderId(), getPrincipalIdForName("jhopf")));

        // after approving as phil, the document should go processed
        document = new WorkflowDocument(getPrincipalIdForName("pmckown"), document.getRouteHeaderId());
        document.approve("");
        assertTrue("Document should be processed.", document.stateIsProcessed());
    }

    @Test public void testRoutingReportOnDocumentType() throws Exception {
    	WorkflowInfo info = new WorkflowInfo();
    	ReportCriteriaDTO criteria = new ReportCriteriaDTO("SeqDocType");
    	criteria.setRuleTemplateNames(new String[] { "WorkflowDocumentTemplate" });
    	DocumentDetailDTO documentDetail = info.routingReport(criteria);
    	assertNotNull(documentDetail);
    	assertEquals("Should have been 2 requests generated.", 2, documentDetail.getActionRequests().length);

    	// let's try doing both WorkflowDocumentTemplate and WorkflowDocumentTemplate2 together
    	criteria.setRuleTemplateNames(new String[] { "WorkflowDocumentTemplate", "WorkflowDocument2Template" });
    	documentDetail = info.routingReport(criteria);
    	assertEquals("Should have been 3 requests generated.", 3, documentDetail.getActionRequests().length);

    	boolean foundRkirkend = false;
    	boolean foundBmcgough = false;
    	boolean foundPmckown = false;
    	for (int index = 0; index < documentDetail.getActionRequests().length; index++) {
			ActionRequestDTO actionRequest = documentDetail.getActionRequests()[index];
			String netId = getPrincipalNameForId(actionRequest.getPrincipalId());
			if (netId.equals("rkirkend")) {
				foundRkirkend = true;
				assertEquals(SeqSetup.WORKFLOW_DOCUMENT_NODE, actionRequest.getNodeName());
			} else if (netId.equals("bmcgough")) {
				foundBmcgough = true;
				assertEquals(SeqSetup.WORKFLOW_DOCUMENT_NODE, actionRequest.getNodeName());
			} else if (netId.equals("pmckown")) {
				foundPmckown = true;
				assertEquals(SeqSetup.WORKFLOW_DOCUMENT_2_NODE, actionRequest.getNodeName());
			}
		}
    	assertTrue("Did not find request for rkirkend", foundRkirkend);
    	assertTrue("Did not find request for bmcgough", foundBmcgough);
    	assertTrue("Did not find request for pmckown", foundPmckown);

    }

    @Test public void testRoutingReportOnRouteHeaderId() throws Exception {
        WorkflowDocument doc = new WorkflowDocument(getPrincipalIdForName("user1"), "SeqDocType");
        WorkflowInfo info = new WorkflowInfo();

        ReportCriteriaDTO criteria = new ReportCriteriaDTO(doc.getRouteHeaderId());
        criteria.setRuleTemplateNames(new String[] { "WorkflowDocumentTemplate" });
        DocumentDetailDTO documentDetail = info.routingReport(criteria);
        assertNotNull(documentDetail);
        assertEquals("Route header id returned should be the same as the one passed in", doc.getRouteHeaderId(), documentDetail.getRouteHeaderId());
        assertEquals("Wrong number of action requests generated", 2, documentDetail.getActionRequests().length);

        // let's try doing both WorkflowDocumentTemplate and WorkflowDocumentTemplate2 together
        criteria.setRuleTemplateNames(new String[] { "WorkflowDocumentTemplate", "WorkflowDocument2Template" });
        documentDetail = info.routingReport(criteria);
        assertEquals("Should have been 3 requests generated.", 3, documentDetail.getActionRequests().length);

        boolean foundRkirkend = false;
        boolean foundBmcgough = false;
        boolean foundPmckown = false;
        for (int index = 0; index < documentDetail.getActionRequests().length; index++) {
            ActionRequestDTO actionRequest = documentDetail.getActionRequests()[index];
            String netId = getPrincipalNameForId(actionRequest.getPrincipalId());
            if (netId.equals("rkirkend")) {
                foundRkirkend = true;
                assertEquals(SeqSetup.WORKFLOW_DOCUMENT_NODE, actionRequest.getNodeName());
            } else if (netId.equals("bmcgough")) {
                foundBmcgough = true;
                assertEquals(SeqSetup.WORKFLOW_DOCUMENT_NODE, actionRequest.getNodeName());
            } else if (netId.equals("pmckown")) {
                foundPmckown = true;
                assertEquals(SeqSetup.WORKFLOW_DOCUMENT_2_NODE, actionRequest.getNodeName());
            }
        }
        assertTrue("Did not find request for rkirkend", foundRkirkend);
        assertTrue("Did not find request for bmcgough", foundBmcgough);
        assertTrue("Did not find request for pmckown", foundPmckown);

    }

    @Test public void testRuleReportGeneralFunction() throws Exception {
        WorkflowInfo info = new WorkflowInfo();

        RuleReportCriteriaDTO ruleReportCriteria = null;
        this.ruleExceptionTest(info, ruleReportCriteria, "Sending in null RuleReportCriteriaDTO should throw Exception");

        ruleReportCriteria = new RuleReportCriteriaDTO();
        this.ruleExceptionTest(info, ruleReportCriteria, "Sending in empty RuleReportCriteriaDTO should throw Exception");

        ruleReportCriteria = new RuleReportCriteriaDTO();
        ruleReportCriteria.setResponsiblePrincipalId("hobo_man");
        this.ruleExceptionTest(info, ruleReportCriteria, "Sending in an invalid principle ID should throw Exception");

        ruleReportCriteria = new RuleReportCriteriaDTO();
        ruleReportCriteria.setResponsibleGroupId("-1234567");
        this.ruleExceptionTest(info, ruleReportCriteria, "Sending in an invalid Workgroup ID should throw Exception");

        ruleReportCriteria = new RuleReportCriteriaDTO();
        RuleExtensionDTO ruleExtensionVO = new RuleExtensionDTO("key","value");
        ruleReportCriteria.setRuleExtensionVOs(new RuleExtensionDTO[]{ruleExtensionVO});
        this.ruleExceptionTest(info, ruleReportCriteria, "Sending in one or more RuleExtentionVO objects with no Rule Template Name should throw Exception");

        RuleDTO[] rules = null;
        ruleReportCriteria = new RuleReportCriteriaDTO();
        ruleReportCriteria.setConsiderWorkgroupMembership(Boolean.FALSE);
        ruleReportCriteria.setDocumentTypeName(RuleTestGeneralSetup.DOCUMENT_TYPE_NAME);
        ruleReportCriteria.setIncludeDelegations(Boolean.FALSE);
        rules = info.ruleReport(ruleReportCriteria);
        assertEquals("Number of Rules Returned Should be 3",3,rules.length);

        rules = null;
        ruleReportCriteria = new RuleReportCriteriaDTO();
        ruleReportCriteria.setActionRequestCodes(new String[]{KEWConstants.ACTION_REQUEST_ACKNOWLEDGE_REQ});
        ruleReportCriteria.setConsiderWorkgroupMembership(Boolean.FALSE);
        ruleReportCriteria.setDocumentTypeName(RuleTestGeneralSetup.DOCUMENT_TYPE_NAME);
        ruleReportCriteria.setIncludeDelegations(Boolean.FALSE);
        ruleReportCriteria.setResponsiblePrincipalId(getPrincipalIdForName("temay"));
        rules = info.ruleReport(ruleReportCriteria);
        assertEquals("Number of Rules Returned Should be 0",0,rules.length);

        rules = null;
        ruleReportCriteria = new RuleReportCriteriaDTO();
        ruleReportCriteria.setActionRequestCodes(new String[]{KEWConstants.ACTION_REQUEST_ACKNOWLEDGE_REQ});
        ruleReportCriteria.setConsiderWorkgroupMembership(Boolean.FALSE);
        ruleReportCriteria.setDocumentTypeName(RuleTestGeneralSetup.DOCUMENT_TYPE_NAME);
        ruleReportCriteria.setIncludeDelegations(Boolean.FALSE);
        rules = info.ruleReport(ruleReportCriteria);
        assertEquals("Number of Rules Returned Should be 1",1,rules.length);
        // check the rule returned
        RuleDTO ruleVO = rules[0];
        assertEquals("Rule Document Type is not " + RuleTestGeneralSetup.DOCUMENT_TYPE_NAME,RuleTestGeneralSetup.DOCUMENT_TYPE_NAME,ruleVO.getDocTypeName());
        assertEquals("Rule Template Named returned is not " + RuleTestGeneralSetup.RULE_TEST_TEMPLATE_2,RuleTestGeneralSetup.RULE_TEST_TEMPLATE_2,ruleVO.getRuleTemplateName());
        assertEquals("Rule did not have force action set to false",Boolean.FALSE,ruleVO.getForceAction());
        assertEquals("Number of Rule Responsibilities returned is incorrect",2,ruleVO.getRuleResponsibilities().length);
        RuleResponsibilityDTO responsibilityVO = null;
        for (int i = 0; i < ruleVO.getRuleResponsibilities().length; i++) {
            responsibilityVO = ruleVO.getRuleResponsibilities()[i];
            String responsibilityPrincipalName = getPrincipalNameForId(responsibilityVO.getPrincipalId());
            if ("temay".equals(responsibilityPrincipalName)) {
                assertEquals("Rule user is not correct","temay",responsibilityPrincipalName);
                assertEquals("Rule priority is incorrect",Integer.valueOf(1),responsibilityVO.getPriority());
                assertEquals("Rule should be Ack Request",KEWConstants.ACTION_REQUEST_APPROVE_REQ,responsibilityVO.getActionRequestedCd());
            } else if ("ewestfal".equals(responsibilityPrincipalName)) {
                assertEquals("Rule user is not correct","ewestfal",responsibilityPrincipalName);
                assertEquals("Rule priority is incorrect",Integer.valueOf(2),responsibilityVO.getPriority());
                assertEquals("Rule should be Ack Request",KEWConstants.ACTION_REQUEST_ACKNOWLEDGE_REQ,responsibilityVO.getActionRequestedCd());
            } else {
                fail("Network ID of user for this responsibility is neither temay or ewestfal");
            }
        }

        rules = null;
        ruleVO = null;
        responsibilityVO = null;
        ruleReportCriteria = new RuleReportCriteriaDTO();
        ruleReportCriteria.setConsiderWorkgroupMembership(Boolean.FALSE);
        ruleReportCriteria.setDocumentTypeName(RuleTestGeneralSetup.DOCUMENT_TYPE_NAME);
        ruleReportCriteria.setIncludeDelegations(Boolean.FALSE);
        ruleReportCriteria.setResponsiblePrincipalId(getPrincipalIdForName("temay"));
        rules = info.ruleReport(ruleReportCriteria);
        assertEquals("Number of Rules returned is not correct",2,rules.length);
        for (int i = 0; i < rules.length; i++) {
            ruleVO = rules[i];
            if (RuleTestGeneralSetup.RULE_TEST_TEMPLATE_1.equals(ruleVO.getRuleTemplateName())) {
                assertEquals("Rule Document Type is not " + RuleTestGeneralSetup.DOCUMENT_TYPE_NAME,RuleTestGeneralSetup.DOCUMENT_TYPE_NAME,ruleVO.getDocTypeName());
                assertEquals("Rule Template Named returned is not " + RuleTestGeneralSetup.RULE_TEST_TEMPLATE_1,RuleTestGeneralSetup.RULE_TEST_TEMPLATE_1,ruleVO.getRuleTemplateName());
                assertEquals("Rule did not have force action set to true",Boolean.TRUE,ruleVO.getForceAction());
                assertEquals("Number of Rule Responsibilities Returned Should be 1",1,ruleVO.getRuleResponsibilities().length);
                responsibilityVO = ruleVO.getRuleResponsibilities()[0];
                assertEquals("Rule user is incorrect","temay",getPrincipalNameForId(responsibilityVO.getPrincipalId()));
                assertEquals("Rule priority is incorrect",Integer.valueOf(3),responsibilityVO.getPriority());
                assertEquals("Rule action request is incorrect",KEWConstants.ACTION_REQUEST_APPROVE_REQ,responsibilityVO.getActionRequestedCd());
            } else if (RuleTestGeneralSetup.RULE_TEST_TEMPLATE_2.equals(ruleVO.getRuleTemplateName())) {
                assertEquals("Rule Document Type is not " + RuleTestGeneralSetup.DOCUMENT_TYPE_NAME,RuleTestGeneralSetup.DOCUMENT_TYPE_NAME,ruleVO.getDocTypeName());
                assertEquals("Rule Template Named returned is not " + RuleTestGeneralSetup.RULE_TEST_TEMPLATE_2,RuleTestGeneralSetup.RULE_TEST_TEMPLATE_2,ruleVO.getRuleTemplateName());
                assertEquals("Rule did not have force action set to false",Boolean.FALSE,ruleVO.getForceAction());
                assertEquals("Number of Rule Responsibilities returned is incorrect",2,ruleVO.getRuleResponsibilities().length);
                responsibilityVO = null;
                for (int l = 0; l < ruleVO.getRuleResponsibilities().length; l++) {
                    responsibilityVO = ruleVO.getRuleResponsibilities()[l];
                    String responsibilityPrincipalName = getPrincipalNameForId(responsibilityVO.getPrincipalId());
                    if ("temay".equals(responsibilityPrincipalName)) {
                        assertEquals("Rule user is not correct","temay",responsibilityPrincipalName);
                        assertEquals("Rule priority is incorrect",Integer.valueOf(1),responsibilityVO.getPriority());
                        assertEquals("Rule should be Ack Request",KEWConstants.ACTION_REQUEST_APPROVE_REQ,responsibilityVO.getActionRequestedCd());
                    } else if ("ewestfal".equals(responsibilityPrincipalName)) {
                        assertEquals("Rule user is not correct","ewestfal",responsibilityPrincipalName);
                        assertEquals("Rule priority is incorrect",Integer.valueOf(2),responsibilityVO.getPriority());
                        assertEquals("Rule should be Ack Request",KEWConstants.ACTION_REQUEST_ACKNOWLEDGE_REQ,responsibilityVO.getActionRequestedCd());
                    } else {
                        fail("Network ID of user for this responsibility is neither temay or ewestfal");
                    }
                }
            } else {
                fail("Rule Template of returned rule is not of type " + RuleTestGeneralSetup.RULE_TEST_TEMPLATE_1 + " nor " + RuleTestGeneralSetup.RULE_TEST_TEMPLATE_2);
            }
        }

        rules = null;
        ruleVO = null;
        responsibilityVO = null;
        ruleReportCriteria = new RuleReportCriteriaDTO();
        ruleReportCriteria.setDocumentTypeName(RuleTestGeneralSetup.DOCUMENT_TYPE_NAME);
        ruleReportCriteria.setIncludeDelegations(Boolean.FALSE);
        ruleReportCriteria.setResponsibleGroupId(RuleTestGeneralSetup.RULE_TEST_GROUP_ID);
        rules = info.ruleReport(ruleReportCriteria);
        assertEquals("Number of Rules Returned Should be 1",1,rules.length);
        ruleVO = rules[0];
        assertEquals("Rule Document Type is not " + RuleTestGeneralSetup.DOCUMENT_TYPE_NAME,RuleTestGeneralSetup.DOCUMENT_TYPE_NAME,ruleVO.getDocTypeName());
        assertEquals("Rule Template Named returned is not " + RuleTestGeneralSetup.RULE_TEST_TEMPLATE_3,RuleTestGeneralSetup.RULE_TEST_TEMPLATE_3,ruleVO.getRuleTemplateName());
        assertEquals("Rule did not have force action set to true",Boolean.TRUE,ruleVO.getForceAction());
        assertEquals("Number of Rule Responsibilities Returned Should be 1",1,ruleVO.getRuleResponsibilities().length);
        responsibilityVO = ruleVO.getRuleResponsibilities()[0];
        KimGroup ruleTestGroup = KIMServiceLocator.getIdentityManagementService().getGroup(responsibilityVO.getGroupId());
        assertEquals("Rule workgroup id is incorrect",RuleTestGeneralSetup.RULE_TEST_GROUP_ID, ruleTestGroup.getGroupId());
        assertEquals("Rule priority is incorrect",Integer.valueOf(1),responsibilityVO.getPriority());
        assertEquals("Rule action request is incorrect",KEWConstants.ACTION_REQUEST_FYI_REQ,responsibilityVO.getActionRequestedCd());

        rules = null;
        ruleVO = null;
        responsibilityVO = null;
        ruleReportCriteria = new RuleReportCriteriaDTO();
        ruleReportCriteria.setDocumentTypeName(RuleTestGeneralSetup.DOCUMENT_TYPE_NAME);
        ruleReportCriteria.setIncludeDelegations(Boolean.FALSE);
        ruleReportCriteria.setResponsiblePrincipalId(getPrincipalIdForName("user1"));
        rules = info.ruleReport(ruleReportCriteria);
        assertEquals("Number of Rules Returned Should be 1",1,rules.length);
        ruleVO = rules[0];
        assertEquals("Rule Document Type is not " + RuleTestGeneralSetup.DOCUMENT_TYPE_NAME,RuleTestGeneralSetup.DOCUMENT_TYPE_NAME,ruleVO.getDocTypeName());
        assertEquals("Rule Template Named returned is not " + RuleTestGeneralSetup.RULE_TEST_TEMPLATE_3,RuleTestGeneralSetup.RULE_TEST_TEMPLATE_3,ruleVO.getRuleTemplateName());
        assertEquals("Rule did not have force action set to true",Boolean.TRUE,ruleVO.getForceAction());
        assertEquals("Number of Rule Responsibilities Returned Should be 1",1,ruleVO.getRuleResponsibilities().length);
        responsibilityVO = ruleVO.getRuleResponsibilities()[0];
        assertEquals("Rule workgroup id is incorrect",RuleTestGeneralSetup.RULE_TEST_GROUP_ID, ruleTestGroup.getGroupId());
        assertEquals("Rule priority is incorrect",Integer.valueOf(1),responsibilityVO.getPriority());
        assertEquals("Rule action request is incorrect",KEWConstants.ACTION_REQUEST_FYI_REQ,responsibilityVO.getActionRequestedCd());
    }

    /**
     * Tests specific rule scenario relating to standard org review routing
     *
     * @throws Exception
     */
    @Test public void testRuleReportOrgReviewTest() throws Exception {
        loadXmlFile("WorkflowUtilityRuleReportConfig.xml");
        WorkflowInfo info = new WorkflowInfo();
        RuleReportCriteriaDTO ruleReportCriteria = new RuleReportCriteriaDTO();
        ruleReportCriteria.setDocumentTypeName(RuleTestOrgReviewSetup.DOCUMENT_TYPE_NAME);
        ruleReportCriteria.setRuleTemplateName(RuleTestOrgReviewSetup.RULE_TEST_TEMPLATE);
        ruleReportCriteria.setResponsiblePrincipalId(getPrincipalIdForName("user1"));
        ruleReportCriteria.setIncludeDelegations(Boolean.FALSE);
        RuleDTO[] rules = info.ruleReport(ruleReportCriteria);
        assertEquals("Number of rules returned is incorrect",2,rules.length);

        ruleReportCriteria = null;
        rules = null;
        ruleReportCriteria = new RuleReportCriteriaDTO();
        ruleReportCriteria.setDocumentTypeName(RuleTestOrgReviewSetup.DOCUMENT_TYPE_NAME);
        ruleReportCriteria.setRuleTemplateName(RuleTestOrgReviewSetup.RULE_TEST_TEMPLATE);
        ruleReportCriteria.setResponsiblePrincipalId(getPrincipalIdForName("user1"));
        ruleReportCriteria.setConsiderWorkgroupMembership(Boolean.FALSE);
        ruleReportCriteria.setIncludeDelegations(Boolean.FALSE);
        rules = info.ruleReport(ruleReportCriteria);
        assertEquals("Number of rules returned is incorrect",1,rules.length);

        ruleReportCriteria = null;
        rules = null;
        ruleReportCriteria = new RuleReportCriteriaDTO();
        ruleReportCriteria.setDocumentTypeName(RuleTestOrgReviewSetup.DOCUMENT_TYPE_NAME);
        ruleReportCriteria.setRuleTemplateName(RuleTestOrgReviewSetup.RULE_TEST_TEMPLATE);
        RuleExtensionDTO ruleExtensionVO = new RuleExtensionDTO(RuleTestOrgReviewSetup.RULE_TEST_CHART_CODE_NAME,"BA");
        RuleExtensionDTO ruleExtensionVO2 = new RuleExtensionDTO(RuleTestOrgReviewSetup.RULE_TEST_ORG_CODE_NAME,"FMOP");
        RuleExtensionDTO[] ruleExtensionVOs = new RuleExtensionDTO[] {ruleExtensionVO,ruleExtensionVO2};
        ruleReportCriteria.setRuleExtensionVOs(ruleExtensionVOs);
        ruleReportCriteria.setIncludeDelegations(Boolean.FALSE);
        rules = info.ruleReport(ruleReportCriteria);
        assertEquals("Number of rules returned is incorrect",2,rules.length);

        ruleReportCriteria = null;
        rules = null;
        ruleExtensionVO = null;
        ruleExtensionVO2 = null;
        ruleExtensionVOs = null;
        ruleReportCriteria = new RuleReportCriteriaDTO();
        ruleReportCriteria.setDocumentTypeName(RuleTestOrgReviewSetup.DOCUMENT_TYPE_NAME);
        ruleReportCriteria.setRuleTemplateName(RuleTestOrgReviewSetup.RULE_TEST_TEMPLATE);
        ruleReportCriteria.setResponsiblePrincipalId(getPrincipalIdForName("ewestfal"));
        ruleReportCriteria.setIncludeDelegations(Boolean.FALSE);
        rules = info.ruleReport(ruleReportCriteria);
        assertEquals("Number of rules returned is incorrect",1,rules.length);
        RuleDTO ruleVO = rules[0];
        assertEquals("Rule Document Type is not " + RuleTestOrgReviewSetup.DOCUMENT_TYPE_NAME,RuleTestOrgReviewSetup.DOCUMENT_TYPE_NAME,ruleVO.getDocTypeName());
        assertEquals("Rule Template Named returned is not " + RuleTestOrgReviewSetup.RULE_TEST_TEMPLATE,RuleTestOrgReviewSetup.RULE_TEST_TEMPLATE,ruleVO.getRuleTemplateName());
        assertEquals("Rule did not have force action set to true",Boolean.TRUE,ruleVO.getForceAction());
        assertEquals("Number of Rule Responsibilities Returned Should be 1",1,ruleVO.getRuleResponsibilities().length);
        RuleResponsibilityDTO responsibilityVO = ruleVO.getRuleResponsibilities()[0];
        KimGroup ruleTestGroup2 = KIMServiceLocator.getIdentityManagementService().getGroup(responsibilityVO.getGroupId());
        assertEquals("Rule workgroup name is incorrect",RuleTestOrgReviewSetup.RULE_TEST_WORKGROUP2,ruleTestGroup2.getGroupName());
        assertEquals("Rule priority is incorrect",Integer.valueOf(4),responsibilityVO.getPriority());
        assertEquals("Rule action request is incorrect",KEWConstants.ACTION_REQUEST_FYI_REQ,responsibilityVO.getActionRequestedCd());
        ruleExtensionVOs = ruleVO.getRuleExtensions();
        assertEquals("Number of Rule Extensions Returned Should be 2",2,ruleExtensionVOs.length);
        for (int i = 0; i < ruleExtensionVOs.length; i++) {
            RuleExtensionDTO extensionVO = ruleExtensionVOs[i];
            // if rule key is chartCode.... should equal UA
            // else if rule key is orgCode.... should equal VPIT
            // otherwise error
            if (RuleTestOrgReviewSetup.RULE_TEST_CHART_CODE_NAME.equals(extensionVO.getKey())) {
                assertEquals("Rule Extension for key '" + RuleTestOrgReviewSetup.RULE_TEST_CHART_CODE_NAME + "' is incorrect","UA",extensionVO.getValue());
            } else if (RuleTestOrgReviewSetup.RULE_TEST_ORG_CODE_NAME.equals(extensionVO.getKey())) {
                assertEquals("Rule Extension for key '" + RuleTestOrgReviewSetup.RULE_TEST_ORG_CODE_NAME + "' is incorrect","VPIT",extensionVO.getValue());
            } else {
                fail("Rule Extension has attribute key that is neither '" + RuleTestOrgReviewSetup.RULE_TEST_CHART_CODE_NAME +
                        "' nor '" + RuleTestOrgReviewSetup.RULE_TEST_ORG_CODE_NAME + "'");
            }
        }

        ruleReportCriteria = null;
        rules = null;
        ruleVO = null;
        responsibilityVO = null;
        ruleReportCriteria = new RuleReportCriteriaDTO();
        ruleReportCriteria.setDocumentTypeName(RuleTestOrgReviewSetup.DOCUMENT_TYPE_NAME);
        ruleReportCriteria.setRuleTemplateName(RuleTestOrgReviewSetup.RULE_TEST_TEMPLATE);
        ruleReportCriteria.setResponsiblePrincipalId(getPrincipalIdForName("user1"));
        ruleReportCriteria.setIncludeDelegations(Boolean.FALSE);
        rules = info.ruleReport(ruleReportCriteria);
        assertEquals("Number of rules returned is incorrect",2,rules.length);

        ruleReportCriteria = null;
        rules = null;
        ruleVO = null;
        responsibilityVO = null;
        ruleExtensionVO = null;
        ruleExtensionVO2 = null;
        ruleExtensionVOs = null;
        ruleReportCriteria = new RuleReportCriteriaDTO();
        ruleReportCriteria.setDocumentTypeName(RuleTestOrgReviewSetup.DOCUMENT_TYPE_NAME);
        ruleReportCriteria.setRuleTemplateName(RuleTestOrgReviewSetup.RULE_TEST_TEMPLATE);
        ruleExtensionVO = new RuleExtensionDTO(RuleTestOrgReviewSetup.RULE_TEST_CHART_CODE_NAME,"UA");
        ruleExtensionVO2 = new RuleExtensionDTO(RuleTestOrgReviewSetup.RULE_TEST_ORG_CODE_NAME,"FMOP");
        ruleExtensionVOs = new RuleExtensionDTO[] {ruleExtensionVO,ruleExtensionVO2};
        ruleReportCriteria.setRuleExtensionVOs(ruleExtensionVOs);
        ruleReportCriteria.setIncludeDelegations(Boolean.FALSE);
        rules = info.ruleReport(ruleReportCriteria);
        assertEquals("Number of rules returned is incorrect",1,rules.length);
        ruleVO = rules[0];
        assertEquals("Rule Document Type is not " + RuleTestOrgReviewSetup.DOCUMENT_TYPE_NAME,RuleTestOrgReviewSetup.DOCUMENT_TYPE_NAME,ruleVO.getDocTypeName());
        assertEquals("Rule Template Named returned is not " + RuleTestOrgReviewSetup.RULE_TEST_TEMPLATE,RuleTestOrgReviewSetup.RULE_TEST_TEMPLATE,ruleVO.getRuleTemplateName());
        assertEquals("Rule did not have force action set to true",Boolean.TRUE,ruleVO.getForceAction());
        assertEquals("Number of Rule Responsibilities Returned Should be 1",1,ruleVO.getRuleResponsibilities().length);
        responsibilityVO = ruleVO.getRuleResponsibilities()[0];
        ruleTestGroup2 = KIMServiceLocator.getIdentityManagementService().getGroup(responsibilityVO.getGroupId());
        assertEquals("Rule workgroup name is incorrect",RuleTestOrgReviewSetup.RULE_TEST_WORKGROUP, ruleTestGroup2.getGroupName());
        assertEquals("Rule priority is incorrect",Integer.valueOf(1),responsibilityVO.getPriority());
        assertEquals("Rule action request is incorrect",KEWConstants.ACTION_REQUEST_APPROVE_REQ,responsibilityVO.getActionRequestedCd());
    }

    @Test public void testGetUserActionItemCount() throws Exception {
        WorkflowInfo info = new WorkflowInfo();
        String principalId = getPrincipalIdForName("ewestfal");
        WorkflowDocument document = new WorkflowDocument(principalId, SeqSetup.DOCUMENT_TYPE_NAME);
        document.routeDocument("");
        assertTrue(document.stateIsEnroute());

        assertEquals("Count is incorrect for user " + principalId, Integer.valueOf(0), info.getUserActionItemCount(principalId));
        principalId = getPrincipalIdForName("bmcgough");
        document = new WorkflowDocument(principalId, document.getRouteHeaderId());
        assertTrue(document.isApprovalRequested());
        assertEquals("Count is incorrect for user " + principalId, Integer.valueOf(1), info.getUserActionItemCount(principalId));
        principalId = getPrincipalIdForName("rkirkend");
        document = new WorkflowDocument(principalId, document.getRouteHeaderId());
        assertTrue(document.isApprovalRequested());
        assertEquals("Count is incorrect for user " + principalId, Integer.valueOf(1), info.getUserActionItemCount(principalId));

        TestUtilities.assertAtNode(document, "WorkflowDocument");
        document.returnToPreviousNode("", "AdHoc");
        TestUtilities.assertAtNode(document, "AdHoc");
        // verify count after return to previous
        principalId = getPrincipalIdForName("ewestfal");
        document = new WorkflowDocument(principalId, document.getRouteHeaderId());
        assertTrue(document.isApprovalRequested());
        // expect one action item for approval request
        assertEquals("Count is incorrect for user " + principalId, Integer.valueOf(1), info.getUserActionItemCount(principalId));
        principalId = getPrincipalIdForName("bmcgough");
        document = new WorkflowDocument(principalId, document.getRouteHeaderId());
        assertFalse(document.isApprovalRequested());
        assertTrue(document.isFYIRequested());
        // expect one action item for fyi action request
        assertEquals("Count is incorrect for user " + principalId, Integer.valueOf(1), info.getUserActionItemCount(principalId));
        principalId = getPrincipalIdForName("rkirkend");
        document = new WorkflowDocument(principalId, document.getRouteHeaderId());
        assertFalse(document.isApprovalRequested());
        // expect no action items
        assertEquals("Count is incorrect for user " + principalId, Integer.valueOf(0), info.getUserActionItemCount(principalId));

        principalId = getPrincipalIdForName("ewestfal");
        document = new WorkflowDocument(principalId, document.getRouteHeaderId());
        document.approve("");
        TestUtilities.assertAtNode(document, "WorkflowDocument");

        // we should be back where we were
        principalId = getPrincipalIdForName("ewestfal");
        document = new WorkflowDocument(principalId, document.getRouteHeaderId());
        assertFalse(document.isApprovalRequested());
        assertEquals("Count is incorrect for user " + principalId, Integer.valueOf(0), info.getUserActionItemCount(principalId));
        principalId = getPrincipalIdForName("bmcgough");
        document = new WorkflowDocument(principalId, document.getRouteHeaderId());
        assertTrue(document.isApprovalRequested());
        assertEquals("Count is incorrect for user " + principalId, Integer.valueOf(1), info.getUserActionItemCount(principalId));
        principalId = getPrincipalIdForName("rkirkend");
        document = new WorkflowDocument(principalId, document.getRouteHeaderId());
        assertTrue(document.isApprovalRequested());
        assertEquals("Count is incorrect for user " + principalId, Integer.valueOf(1), info.getUserActionItemCount(principalId));
    }

    @Test public void testGetActionItems() throws Exception {
        String initiatorNetworkId = "ewestfal";
        String user1NetworkId = "bmcgough";
        String user2NetworkId ="rkirkend";
        String initiatorPrincipalId = getPrincipalIdForName(initiatorNetworkId);
        String user1PrincipalId = getPrincipalIdForName(user1NetworkId);
        String user2PrincipalId = getPrincipalIdForName(user2NetworkId);
        WorkflowInfo info = new WorkflowInfo();
        String principalId = getPrincipalIdForName(initiatorNetworkId);
        String docTitle = "this is the doc title";
        WorkflowDocument document = new WorkflowDocument(principalId, SeqSetup.DOCUMENT_TYPE_NAME);
        document.setTitle(docTitle);
        document.routeDocument("");
        assertTrue(document.stateIsEnroute());

        ActionItemDTO[] actionItems = info.getActionItems(document.getRouteHeaderId());
        assertEquals("Incorrect number of action items returned",2,actionItems.length);
        for (ActionItemDTO actionItem : actionItems) {
            assertEquals("Action Item should be Approve request", KEWConstants.ACTION_REQUEST_APPROVE_REQ, actionItem.getActionRequestCd());
            assertEquals("Action Item has incorrect doc title", docTitle, actionItem.getDocTitle());
            assertTrue("User should be one of '" + user1NetworkId + "' or '" + user2NetworkId + "'", user1PrincipalId.equals(actionItem.getPrincipalId()) || user2PrincipalId.equals(actionItem.getPrincipalId()));
        }

        principalId = getPrincipalIdForName(user2NetworkId);
        document = new WorkflowDocument(principalId, document.getRouteHeaderId());
        assertTrue(document.isApprovalRequested());
        TestUtilities.assertAtNode(document, "WorkflowDocument");
        document.returnToPreviousNode("", "AdHoc");
        TestUtilities.assertAtNode(document, "AdHoc");
        // verify count after return to previous
        actionItems = info.getActionItems(document.getRouteHeaderId());
        assertEquals("Incorrect number of action items returned",2,actionItems.length);
        for (ActionItemDTO actionItem : actionItems) {
            assertEquals("Action Item has incorrect doc title", docTitle, actionItem.getDocTitle());
            assertTrue("Action Items should be Approve or FYI requests only", KEWConstants.ACTION_REQUEST_APPROVE_REQ.equals(actionItem.getActionRequestCd()) || KEWConstants.ACTION_REQUEST_FYI_REQ.equals(actionItem.getActionRequestCd()));
            if (KEWConstants.ACTION_REQUEST_APPROVE_REQ.equals(actionItem.getActionRequestCd())) {
                assertTrue("User should be '" + initiatorNetworkId + "'", initiatorPrincipalId.equals(actionItem.getPrincipalId()));
            } else if (KEWConstants.ACTION_REQUEST_FYI_REQ.equals(actionItem.getActionRequestCd())) {
                assertTrue("User should be  '" + user1NetworkId + "'", user1PrincipalId.equals(actionItem.getPrincipalId()));
            }
        }

        principalId = getPrincipalIdForName(initiatorNetworkId);
        document = new WorkflowDocument(principalId, document.getRouteHeaderId());
        assertTrue(document.isApprovalRequested());
        document.approve("");
        TestUtilities.assertAtNode(document, "WorkflowDocument");

        // we should be back where we were
        actionItems = info.getActionItems(document.getRouteHeaderId());
        assertEquals("Incorrect number of action items returned",2,actionItems.length);
        for (ActionItemDTO actionItem : actionItems) {
            assertEquals("Action Item should be Approve request", KEWConstants.ACTION_REQUEST_APPROVE_REQ, actionItem.getActionRequestCd());
            assertEquals("Action Item has incorrect doc title", docTitle, actionItem.getDocTitle());
            assertTrue("User should be one of '" + user1NetworkId + "' or '" + user2NetworkId + "'", user1PrincipalId.equals(actionItem.getPrincipalId()) || user2PrincipalId.equals(actionItem.getPrincipalId()));
        }
    }

    @Test public void testGetActionItems_ActionRequestCodes() throws Exception {
        String initiatorNetworkId = "ewestfal";
        String user1NetworkId = "bmcgough";
        String user2NetworkId ="rkirkend";
        String initiatorPrincipalId = getPrincipalIdForName(initiatorNetworkId);
        String user1PrincipalId = getPrincipalIdForName(user1NetworkId);
        String user2PrincipalId = getPrincipalIdForName(user2NetworkId);
        WorkflowInfo info = new WorkflowInfo();
        String principalId = getPrincipalIdForName(initiatorNetworkId);
        String docTitle = "this is the doc title";
        WorkflowDocument document = new WorkflowDocument(principalId, SeqSetup.DOCUMENT_TYPE_NAME);
        document.setTitle(docTitle);
        document.routeDocument("");
        assertTrue(document.stateIsEnroute());

        ActionItemDTO[] actionItems = info.getActionItems(document.getRouteHeaderId(), new String[]{KEWConstants.ACTION_REQUEST_COMPLETE_REQ});
        assertEquals("Incorrect number of action items returned",0,actionItems.length);
        actionItems = info.getActionItems(document.getRouteHeaderId(), new String[]{KEWConstants.ACTION_REQUEST_APPROVE_REQ});
        assertEquals("Incorrect number of action items returned",2,actionItems.length);
        for (ActionItemDTO actionItem : actionItems) {
            assertEquals("Action Item should be Approve request", KEWConstants.ACTION_REQUEST_APPROVE_REQ, actionItem.getActionRequestCd());
            assertEquals("Action Item has incorrect doc title", docTitle, actionItem.getDocTitle());
            assertTrue("User should be one of '" + user1NetworkId + "' or '" + user2NetworkId + "'", user1PrincipalId.equals(actionItem.getPrincipalId()) || user2PrincipalId.equals(actionItem.getPrincipalId()));
        }

        principalId = getPrincipalIdForName(user2NetworkId);
        document = new WorkflowDocument(principalId, document.getRouteHeaderId());
        assertTrue(document.isApprovalRequested());
        TestUtilities.assertAtNode(document, "WorkflowDocument");
        document.returnToPreviousNode("", "AdHoc");
        TestUtilities.assertAtNode(document, "AdHoc");
        // verify count after return to previous
        actionItems = info.getActionItems(document.getRouteHeaderId(), new String[]{KEWConstants.ACTION_REQUEST_COMPLETE_REQ});
        assertEquals("Incorrect number of action items returned",0,actionItems.length);
        actionItems = info.getActionItems(document.getRouteHeaderId(), new String[]{KEWConstants.ACTION_REQUEST_APPROVE_REQ});
        assertEquals("Incorrect number of action items returned",1,actionItems.length);
        actionItems = info.getActionItems(document.getRouteHeaderId(), new String[]{KEWConstants.ACTION_REQUEST_FYI_REQ});
        assertEquals("Incorrect number of action items returned",1,actionItems.length);
        actionItems = info.getActionItems(document.getRouteHeaderId(), new String[]{KEWConstants.ACTION_REQUEST_FYI_REQ, KEWConstants.ACTION_REQUEST_APPROVE_REQ});
        assertEquals("Incorrect number of action items returned",2,actionItems.length);
        for (ActionItemDTO actionItem : actionItems) {
            assertEquals("Action Item has incorrect doc title", docTitle, actionItem.getDocTitle());
            assertTrue("Action Items should be Approve or FYI requests only", KEWConstants.ACTION_REQUEST_APPROVE_REQ.equals(actionItem.getActionRequestCd()) || KEWConstants.ACTION_REQUEST_FYI_REQ.equals(actionItem.getActionRequestCd()));
            if (KEWConstants.ACTION_REQUEST_APPROVE_REQ.equals(actionItem.getActionRequestCd())) {
                assertTrue("User should be '" + initiatorNetworkId + "'", initiatorPrincipalId.equals(actionItem.getPrincipalId()));
            } else if (KEWConstants.ACTION_REQUEST_FYI_REQ.equals(actionItem.getActionRequestCd())) {
                assertTrue("User should be  '" + user1NetworkId + "'", user1PrincipalId.equals(actionItem.getPrincipalId()));
            } else {
                fail("Should not have found action request with requested action '" + KEWConstants.ACTION_REQUEST_CD.get(actionItem.getActionRequestCd()) + "'");
            }
        }

        principalId = getPrincipalIdForName(initiatorNetworkId);
        document = new WorkflowDocument(principalId, document.getRouteHeaderId());
        assertTrue(document.isApprovalRequested());
        document.approve("");
        TestUtilities.assertAtNode(document, "WorkflowDocument");

        // we should be back where we were
        actionItems = info.getActionItems(document.getRouteHeaderId(), new String[]{KEWConstants.ACTION_REQUEST_COMPLETE_REQ});
        assertEquals("Incorrect number of action items returned",0,actionItems.length);
        actionItems = info.getActionItems(document.getRouteHeaderId(), new String[]{KEWConstants.ACTION_REQUEST_APPROVE_REQ});
        assertEquals("Incorrect number of action items returned",2,actionItems.length);
        for (ActionItemDTO actionItem : actionItems) {
            assertEquals("Action Item should be Approve request", KEWConstants.ACTION_REQUEST_APPROVE_REQ, actionItem.getActionRequestCd());
            assertEquals("Action Item has incorrect doc title", docTitle, actionItem.getDocTitle());
            assertTrue("User should be one of '" + user1NetworkId + "' or '" + user2NetworkId + "'", user1PrincipalId.equals(actionItem.getPrincipalId()) || user2PrincipalId.equals(actionItem.getPrincipalId()));
        }
    }

    /**
     * This method routes two test documents of the type specified.  One has the given title and another has a dummy title.
     */
    private void setupPerformDocumentSearchTests(String documentTypeName, String expectedRouteNodeName, String docTitle) throws WorkflowException {
        String userNetworkId = "ewestfal";
        WorkflowDocument workflowDocument = new WorkflowDocument(getPrincipalIdForName(userNetworkId), documentTypeName);
        int size = docTitle.length();
        workflowDocument.setTitle("Respect my Authoritah");
        workflowDocument.routeDocument("routing this document.");
        if (StringUtils.isNotBlank(expectedRouteNodeName)) {
        	assertEquals("Document is not at expected routeNodeName", expectedRouteNodeName, workflowDocument.getNodeNames()[0]);
        }

        userNetworkId = "rkirkend";
        workflowDocument = new WorkflowDocument(getPrincipalIdForName(userNetworkId), documentTypeName);
        workflowDocument.setTitle(docTitle);
        workflowDocument.routeDocument("routing this document.");
        if (StringUtils.isNotBlank(expectedRouteNodeName)) {
        	assertEquals("Document is not at expected routeNodeName", expectedRouteNodeName, workflowDocument.getNodeNames()[0]);
        }
    }

    @Test public void testPerformDocumentSearch_WithUser_CustomThreshold() throws Exception {
    	KimPrincipal user = KIMServiceLocator.getIdentityService().getPrincipalByPrincipalName("user1");
        UserSession.setAuthenticatedUser(new UserSession(user));
        runTestPerformDocumentSearch_CustomThreshold(getPrincipalIdForName("user2"));
    }

    @Test public void testPerformDocumentSearch_NoUser_CustomThreshold() throws Exception {
    	runTestPerformDocumentSearch_CustomThreshold(null);
    }

    private void runTestPerformDocumentSearch_CustomThreshold(String principalId) throws Exception {
        String documentTypeName = SeqSetup.DOCUMENT_TYPE_NAME;
        String docTitle = "Routing Style";
        setupPerformDocumentSearchTests(documentTypeName, null, docTitle);

        DocumentSearchCriteriaDTO criteria = new DocumentSearchCriteriaDTO();
        criteria.setDocTypeFullName(documentTypeName);
        DocumentSearchResultDTO result = utility.performDocumentSearch(principalId, criteria);
        List<DocumentSearchResultRowDTO> searchResults = result.getSearchResults();
        assertEquals("Search results should have two documents.", 2, searchResults.size());

        int threshold = 1;
        criteria = new DocumentSearchCriteriaDTO();
        criteria.setDocTypeFullName(documentTypeName);
        criteria.setThreshold(Integer.valueOf(threshold));
        result = utility.performDocumentSearch(principalId, criteria);
        assertTrue("Search results should signify search went over the given threshold: " + threshold, result.isOverThreshold());
        searchResults = result.getSearchResults();
        assertEquals("Search results should have one document.", threshold, searchResults.size());
    }

    @Test public void testPerformDocumentSearch_WithUser_BasicCriteria() throws Exception {
    	KimPrincipal user = KIMServiceLocator.getIdentityService().getPrincipalByPrincipalName("user1");
        UserSession.setAuthenticatedUser(new UserSession(user));
        runTestPerformDocumentSearch_BasicCriteria(getPrincipalIdForName("user2"));
    }

    @Test public void testPerformDocumentSearch_NoUser_BasicCriteria() throws Exception {
    	runTestPerformDocumentSearch_BasicCriteria(null);
    }

    private void runTestPerformDocumentSearch_BasicCriteria(String principalId) throws Exception {
    	KimPrincipal user = KIMServiceLocator.getIdentityService().getPrincipalByPrincipalName("user1");
        UserSession.setAuthenticatedUser(new UserSession(user));
        String documentTypeName = SeqSetup.DOCUMENT_TYPE_NAME;
        String docTitle = "Routing Style";
        setupPerformDocumentSearchTests(documentTypeName, null, docTitle);
        String userNetworkId = "delyea";
        WorkflowDocument workflowDocument = new WorkflowDocument(getPrincipalIdForName(userNetworkId), documentTypeName);
        int size = docTitle.length();
        workflowDocument.setTitle("Get Outta Dodge");
        workflowDocument.routeDocument("routing this document.");

        DocumentSearchCriteriaDTO criteria = new DocumentSearchCriteriaDTO();
        criteria.setDocTypeFullName(documentTypeName);
        criteria.setDocTitle(docTitle);
        DocumentSearchResultDTO result = utility.performDocumentSearch(principalId, criteria);
        List<DocumentSearchResultRowDTO> searchResults = result.getSearchResults();
        assertEquals("Search results should have one document.", 1, searchResults.size());

        criteria = new DocumentSearchCriteriaDTO();
        criteria.setInitiator("rkirkend");
        result = utility.performDocumentSearch(principalId, criteria);
        searchResults = result.getSearchResults();
        assertEquals("Search results should have one document.", 1, searchResults.size());

        criteria = new DocumentSearchCriteriaDTO();
        criteria.setInitiator("user1");
        result = utility.performDocumentSearch(principalId, criteria);
        searchResults = result.getSearchResults();
        assertEquals("Search results should be empty.", 0, searchResults.size());

        criteria = new DocumentSearchCriteriaDTO();
        criteria.setDocTypeFullName(documentTypeName);
        result = utility.performDocumentSearch(principalId, criteria);
        searchResults = result.getSearchResults();
        assertEquals("Search results should have two documents.", 3, searchResults.size());
        // now verify that the search returned the proper document id
        boolean foundValidDocId = false;
        for (DocumentSearchResultRowDTO documentSearchResultRowVO : searchResults) {
			for (KeyValueDTO keyValueVO : documentSearchResultRowVO.getFieldValues()) {
				if ( (KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_ROUTE_HEADER_ID.equals(keyValueVO.getKey())) &&
					 (StringUtils.equals(workflowDocument.getRouteHeaderId().toString(), keyValueVO.getUserDisplayValue())) ) {
					foundValidDocId = true;
					break;
				}
			}
		}
        assertTrue("Should have found document search result with specified document id",foundValidDocId);
    }

    @Test public void testPerformDocumentSearch_WithUser_RouteNodeSearch() throws Exception {
    	KimPrincipal user = KIMServiceLocator.getIdentityService().getPrincipalByPrincipalName("user1");
        UserSession.setAuthenticatedUser(new UserSession(user));
        runTestPerformDocumentSearch_RouteNodeSearch(getPrincipalIdForName("user2"));
    }

    @Test public void testPerformDocumentSearch_NoUser_RouteNodeSearch() throws Exception {
    	runTestPerformDocumentSearch_RouteNodeSearch(null);
    }

    private void runTestPerformDocumentSearch_RouteNodeSearch(String principalId) throws Exception {
        String documentTypeName = SeqSetup.DOCUMENT_TYPE_NAME;
        setupPerformDocumentSearchTests(documentTypeName, SeqSetup.WORKFLOW_DOCUMENT_NODE, "Doc Title");

        // test exception thrown when route node specified and no doc type specified
        DocumentSearchCriteriaDTO criteria = new DocumentSearchCriteriaDTO();
        criteria.setDocRouteNodeName(SeqSetup.ADHOC_NODE);
        try {
            utility.performDocumentSearch(principalId, criteria);
            fail("Exception should have been thrown when specifying a route node name but no document type name");
        } catch (Exception e) {}

        // test exception thrown when route node specified does not exist on document type
        criteria = new DocumentSearchCriteriaDTO();
        criteria.setDocTypeFullName(documentTypeName);
        criteria.setDocRouteNodeName("Yo homes, smell ya later!");
        try {
            utility.performDocumentSearch(principalId, criteria);
            fail("Exception should have been thrown when specifying a route node name that does not exist on the specified document type name");
        } catch (Exception e) {}

        runPerformDocumentSearch_RouteNodeSearch(principalId, SeqSetup.ADHOC_NODE, documentTypeName, 0, 0, 2);
        runPerformDocumentSearch_RouteNodeSearch(principalId, SeqSetup.WORKFLOW_DOCUMENT_NODE, documentTypeName, 0, 2, 0);
        runPerformDocumentSearch_RouteNodeSearch(principalId, SeqSetup.WORKFLOW_DOCUMENT_2_NODE, documentTypeName, 2, 0, 0);
    }

    @Test public void testPerformDocumentSearch_RouteNodeSpecial() throws RemoteException, WorkflowException {
        String documentTypeName = "DocumentWithSpecialRouteNodes";
        setupPerformDocumentSearchTests(documentTypeName, "Level1", "Doc Title");
        runPerformDocumentSearch_RouteNodeSearch(null, "Level5", documentTypeName, 0, 0, 2);
        runPerformDocumentSearch_RouteNodeSearch(null, "Level1", documentTypeName, 0, 2, 0);
        runPerformDocumentSearch_RouteNodeSearch(null, "Level3", documentTypeName, 2, 0, 0);

    }

    private void runPerformDocumentSearch_RouteNodeSearch(String principalId, String routeNodeName, String documentTypeName, int countBeforeNode, int countAtNode, int countAfterNode) throws RemoteException, WorkflowException {
        DocumentSearchCriteriaDTO criteria = new DocumentSearchCriteriaDTO();
        criteria.setDocTypeFullName(documentTypeName);
        criteria.setDocRouteNodeName(routeNodeName);
        criteria.findDocsAtExactSpecifiedRouteNode();
        DocumentSearchResultDTO result = utility.performDocumentSearch(principalId, criteria);
        List<DocumentSearchResultRowDTO> searchResults = result.getSearchResults();
        assertEquals("Wrong number of search results when checking default node qualifier.", countAtNode, searchResults.size());

        criteria = new DocumentSearchCriteriaDTO();
        criteria.setDocTypeFullName(documentTypeName);
        criteria.setDocRouteNodeName(routeNodeName);
        result = utility.performDocumentSearch(principalId, criteria);
        searchResults = result.getSearchResults();
        assertEquals("Wrong number of search results when checking docs at exact node.", countAtNode, searchResults.size());

        criteria = new DocumentSearchCriteriaDTO();
        criteria.setDocTypeFullName(documentTypeName);
        criteria.setDocRouteNodeName(routeNodeName);
        criteria.findDocsBeforeSpecifiedRouteNode();
        result = utility.performDocumentSearch(principalId, criteria);
        searchResults = result.getSearchResults();
        assertEquals("Wrong number of search results when checking docs before node.", countBeforeNode, searchResults.size());

        criteria = new DocumentSearchCriteriaDTO();
        criteria.setDocTypeFullName(documentTypeName);
        criteria.setDocRouteNodeName(routeNodeName);
        criteria.findDocsAfterSpecifiedRouteNode();
        result = utility.performDocumentSearch(principalId, criteria);
        searchResults = result.getSearchResults();
        assertEquals("Wrong number of search results when checking docs after node.", countAfterNode, searchResults.size());
    }

    @Test public void testPerformDocumentSearch_WithUser_SearchAttributes() throws Exception {
    	KimPrincipal user = KIMServiceLocator.getIdentityService().getPrincipalByPrincipalName("user1");
        UserSession.setAuthenticatedUser(new UserSession(user));
    	runTestPerformDocumentSearch_SearchAttributes(getPrincipalIdForName("user2"));
    }

    @Test public void testPerformDocumentSearch_NoUser_SearchAttributes() throws Exception {
    	runTestPerformDocumentSearch_SearchAttributes(null);
    }

    private void runTestPerformDocumentSearch_SearchAttributes(String principalId) throws Exception {
        String documentTypeName = SeqSetup.DOCUMENT_TYPE_NAME;
        String docTitle = "Routing Style";
        setupPerformDocumentSearchTests(documentTypeName, null, docTitle);

        DocumentSearchCriteriaDTO criteria = new DocumentSearchCriteriaDTO();
        criteria.setDocTypeFullName(documentTypeName);
        criteria.setSearchAttributeValues(Arrays.asList(new KeyValueDTO[]{new KeyValueDTO(TestXMLSearchableAttributeString.SEARCH_STORAGE_KEY,TestXMLSearchableAttributeString.SEARCH_STORAGE_VALUE)}));
        DocumentSearchResultDTO result = utility.performDocumentSearch(principalId, criteria);
        List<DocumentSearchResultRowDTO> searchResults = result.getSearchResults();
        assertEquals("Search results should have two documents.", 2, searchResults.size());

        criteria = new DocumentSearchCriteriaDTO();
        criteria.setDocTypeFullName(documentTypeName);
        criteria.setSearchAttributeValues(Arrays.asList(new KeyValueDTO[]{new KeyValueDTO(TestXMLSearchableAttributeString.SEARCH_STORAGE_KEY,"fred")}));
        result = utility.performDocumentSearch(principalId, criteria);
        searchResults = result.getSearchResults();
        assertEquals("Search results should be empty.", 0, searchResults.size());

        criteria = new DocumentSearchCriteriaDTO();
        criteria.setDocTypeFullName(documentTypeName);
        criteria.setSearchAttributeValues(Arrays.asList(new KeyValueDTO[]{new KeyValueDTO("fakeproperty", "doesntexist")}));
        try {
            result = utility.performDocumentSearch(principalId, criteria);
            fail("Search results should be throwing a validation exception for use of non-existant searchable attribute");
        } catch (Exception e) {}

        criteria = new DocumentSearchCriteriaDTO();
        criteria.setDocTypeFullName(documentTypeName);
        criteria.setSearchAttributeValues(Arrays.asList(new KeyValueDTO[]{new KeyValueDTO(TestXMLSearchableAttributeLong.SEARCH_STORAGE_KEY, TestXMLSearchableAttributeLong.SEARCH_STORAGE_VALUE.toString())}));
        result = utility.performDocumentSearch(principalId, criteria);
        searchResults = result.getSearchResults();
        assertEquals("Search results should have two documents.", 2, searchResults.size());

        criteria = new DocumentSearchCriteriaDTO();
        criteria.setDocTypeFullName(documentTypeName);
        criteria.setSearchAttributeValues(Arrays.asList(new KeyValueDTO[]{new KeyValueDTO(TestXMLSearchableAttributeLong.SEARCH_STORAGE_KEY, "1111111")}));
        result = utility.performDocumentSearch(principalId, criteria);
        searchResults = result.getSearchResults();
        assertEquals("Search results should be empty.", 0, searchResults.size());

        criteria = new DocumentSearchCriteriaDTO();
        criteria.setDocTypeFullName(documentTypeName);
        criteria.setSearchAttributeValues(Arrays.asList(new KeyValueDTO[]{new KeyValueDTO("fakeymcfakefake", "99999999")}));
        try {
            result = utility.performDocumentSearch(principalId, criteria);
            fail("Search results should be throwing a validation exception for use of non-existant searchable attribute");
        } catch (Exception e) {}

        criteria = new DocumentSearchCriteriaDTO();
        criteria.setDocTypeFullName(documentTypeName);
        criteria.setSearchAttributeValues(Arrays.asList(new KeyValueDTO[]{new KeyValueDTO(TestXMLSearchableAttributeFloat.SEARCH_STORAGE_KEY, TestXMLSearchableAttributeFloat.SEARCH_STORAGE_VALUE.toString())}));
        result = utility.performDocumentSearch(principalId, criteria);
        searchResults = result.getSearchResults();
        assertEquals("Search results should have two documents.", 2, searchResults.size());

        criteria = new DocumentSearchCriteriaDTO();
        criteria.setDocTypeFullName(documentTypeName);
        criteria.setSearchAttributeValues(Arrays.asList(new KeyValueDTO[]{new KeyValueDTO(TestXMLSearchableAttributeFloat.SEARCH_STORAGE_KEY, "215.3548")}));
        result = utility.performDocumentSearch(principalId, criteria);
        searchResults = result.getSearchResults();
        assertEquals("Search results should be empty.", 0, searchResults.size());

        criteria = new DocumentSearchCriteriaDTO();
        criteria.setDocTypeFullName(documentTypeName);
        criteria.setSearchAttributeValues(Arrays.asList(new KeyValueDTO[]{new KeyValueDTO("fakeylostington", "9999.9999")}));
        try {
            result = utility.performDocumentSearch(principalId, criteria);
            fail("Search results should be throwing a validation exception for use of non-existant searchable attribute");
        } catch (Exception e) {}

        criteria = new DocumentSearchCriteriaDTO();
        criteria.setDocTypeFullName(documentTypeName);
        criteria.setSearchAttributeValues(Arrays.asList(new KeyValueDTO[]{new KeyValueDTO(TestXMLSearchableAttributeDateTime.SEARCH_STORAGE_KEY, DocSearchUtils.getDisplayValueWithDateOnly(new Timestamp(TestXMLSearchableAttributeDateTime.SEARCH_STORAGE_VALUE_IN_MILLS)))}));
        result = utility.performDocumentSearch(principalId, criteria);
        searchResults = result.getSearchResults();
        assertEquals("Search results should have two documents.", 2, searchResults.size());

        criteria = new DocumentSearchCriteriaDTO();
        criteria.setDocTypeFullName(documentTypeName);
        criteria.setSearchAttributeValues(Arrays.asList(new KeyValueDTO[]{new KeyValueDTO(TestXMLSearchableAttributeDateTime.SEARCH_STORAGE_KEY, "07/06/1979")}));
        result = utility.performDocumentSearch(principalId, criteria);
        searchResults = result.getSearchResults();
        assertEquals("Search results should be empty.", 0, searchResults.size());

        criteria = new DocumentSearchCriteriaDTO();
        criteria.setDocTypeFullName(documentTypeName);
        criteria.setSearchAttributeValues(Arrays.asList(new KeyValueDTO[]{new KeyValueDTO("lastingsfakerson","07/06/2007")}));
        try {
            result = utility.performDocumentSearch(principalId, criteria);
            fail("Search results should be throwing a validation exception for use of non-existant searchable attribute");
        } catch (Exception e) {}
    }

    private void ruleExceptionTest(WorkflowInfo info, RuleReportCriteriaDTO ruleReportCriteria, String message) {
        try {
            info.ruleReport(ruleReportCriteria);
            fail(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class RuleTestGeneralSetup {
        public static final String DOCUMENT_TYPE_NAME = "RuleTestDocType";
        public static final String RULE_TEST_TEMPLATE_1 = "WorkflowDocumentTemplate";
        public static final String RULE_TEST_TEMPLATE_2 = "WorkflowDocument2Template";
        public static final String RULE_TEST_TEMPLATE_3 = "WorkflowDocument3Template";
        public static final String RULE_TEST_GROUP_ID = "3003"; // the NonSIT group
    }

    private class RuleTestOrgReviewSetup {
        public static final String DOCUMENT_TYPE_NAME = "OrgReviewTestDocType";
        public static final String RULE_TEST_TEMPLATE = "OrgReviewTemplate";
        public static final String RULE_TEST_WORKGROUP = "Org_Review_Group";
        public static final String RULE_TEST_WORKGROUP2 = "Org_Review_Group_2";
        public static final String RULE_TEST_CHART_CODE_NAME = "chartCode";
        public static final String RULE_TEST_ORG_CODE_NAME = "orgCode";
    }

    private class SeqSetup {
        public static final String DOCUMENT_TYPE_NAME = "SeqDocType";
        public static final String LAST_APPROVER_DOCUMENT_TYPE_NAME = "SeqLastApproverDocType";
        public static final String CHILD_DOCUMENT_TYPE_NAME = "SeqChildDocType";
        public static final String ADHOC_NODE = "AdHoc";
        public static final String WORKFLOW_DOCUMENT_NODE = "WorkflowDocument";
        public static final String WORKFLOW_DOCUMENT_2_NODE = "WorkflowDocument2";
        public static final String ACKNOWLEDGE_1_NODE = "Acknowledge1";
        public static final String ACKNOWLEDGE_2_NODE = "Acknowledge2";
    }
}
