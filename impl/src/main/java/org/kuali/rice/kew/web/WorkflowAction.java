/*
 * Copyright 2005-2006 The Kuali Foundation.
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
package org.kuali.rice.kew.web;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;
import org.kuali.rice.kew.KEWServiceLocator;
import org.kuali.rice.kew.WorkflowServiceErrorException;
import org.kuali.rice.kew.WorkflowServiceErrorImpl;
import org.kuali.rice.kew.clientapp.WorkflowDocument;
import org.kuali.rice.kew.clientapp.WorkflowInfo;
import org.kuali.rice.kew.dto.AdHocRevokeDTO;
import org.kuali.rice.kew.dto.NetworkIdDTO;
import org.kuali.rice.kew.dto.RouteNodeInstanceDTO;
import org.kuali.rice.kew.dto.WorkgroupNameIdDTO;
import org.kuali.rice.kew.exception.KEWUserNotFoundException;
import org.kuali.rice.kew.exception.WorkflowException;
import org.kuali.rice.kew.exception.WorkflowRuntimeException;
import org.kuali.rice.kew.export.ExportDataSet;
import org.kuali.rice.kew.export.web.ExportServlet;
import org.kuali.rice.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.rice.kew.user.AuthenticationUserId;
import org.kuali.rice.kew.user.UserService;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kew.web.session.UserSession;
import org.kuali.rice.kew.workgroup.GroupNameId;
import org.kuali.rice.kew.workgroup.WorkgroupService;
import org.kuali.rice.util.JSTLConstants;


/**
 * An abstract super class for all Struts Actions in KEW.  Adds some custom
 * dispatch behavior by extending the Struts DispatchAction.
 *
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 */
public abstract class WorkflowAction extends DispatchAction {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(WorkflowAction.class);

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			request.setAttribute("Constants", new JSTLConstants(KEWConstants.class));
			request.setAttribute("UrlResolver", UrlResolver.getInstance());
			ActionMessages messages = null;
			messages = establishRequiredState(request, form);
			if (messages != null && !messages.isEmpty()) {
				// XXX: HACK: FIXME:
				// obviously this implies that we can't return both ActionErrors
				// and ActionMessages... :(
				// probably establishRequiredState should be refactored to have
				// a generic 'should-we-continue'
				// boolean return, so that control flow can be more explicitly
				// specified by the subclass
				if (messages instanceof ActionErrors) {
					saveErrors(request, (ActionErrors) messages);
				} else {
					saveMessages(request, messages);
				}
				return mapping.findForward("requiredStateError");
			}
			LOG.info(request.getQueryString());
			ActionForward returnForward = null;

			if (request.getParameterMap() != null) {
				for (Iterator iter = request.getParameterMap().entrySet().iterator(); iter.hasNext();) {
					String parameterName = (String) ((Map.Entry) iter.next()).getKey();
					if (parameterName.startsWith("methodToCall.") && parameterName.endsWith(".x")) {
						String methodToCall = parameterName.substring(parameterName.indexOf("methodToCall.") + 13, parameterName.lastIndexOf(".x"));
						if (methodToCall != null && methodToCall.length() > 0) {
							returnForward = this.dispatchMethod(mapping, form, request, response, methodToCall);
						}
					}
				}
			}
			if (returnForward == null) {
				if (request.getParameter("methodToCall") != null && !"".equals(request.getParameter("methodToCall")) && !"execute".equals(request.getParameter("methodToCall"))) {
					LOG.info("dispatch to methodToCall " + request.getParameter("methodToCall") + " called");
					returnForward = super.execute(mapping, form, request, response);
				} else {
					LOG.info("dispatch to default start methodToCall");
					returnForward = start(mapping, form, request, response);
				}
			}

			messages = establishFinalState(request, form);
			if (messages != null && !messages.isEmpty()) {
				saveMessages(request, messages);
				return mapping.findForward("finalStateError");
			}
			return returnForward;
		} catch (Exception e) {
		    try {
			establishExceptionFinalState(request, form);
		    } catch (Exception ne) {
			LOG.error("Error establishing final exception state", ne);
		    }
		    LOG.error("Error processing action " + mapping.getPath(), e);
		    throw new WorkflowRuntimeException(e);
		}
	}

	public abstract ActionForward start(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception;

	public ActionForward refresh(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		return start(mapping, form, request, response);
	}

	public abstract ActionMessages establishRequiredState(HttpServletRequest request, ActionForm form) throws Exception;

	public ActionMessages establishFinalState(HttpServletRequest request, ActionForm form) throws Exception {
		return null;
	}

	public void establishExceptionFinalState(HttpServletRequest request, ActionForm form) throws Exception {
	}

	protected ActionForward exportDataSet(HttpServletRequest request, ExportDataSet dataSet) {
		request.getSession().setAttribute(ExportServlet.EXPORT_DATA_SET_KEY, dataSet);
		return new ActionForward(ExportServlet.generateExportPath(request, dataSet), true);
	}

	public static UserSession getUserSession(HttpServletRequest request) {
		return UserLoginFilter.getUserSession(request);
	}

	public boolean isEmpty(String propertyValue) {
		if (propertyValue == null || propertyValue.trim().equals("")) {
			return true;
		}
		return false;
	}

	public ActionForward performLookup(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + mapping.getModuleConfig().getPrefix();
		String lookupUrl = basePath + "/Lookup.do?methodToCall=start&docFormKey=" + getUserSession(request).addObject(form) + "&lookupableImplServiceName=" + request.getParameter("lookupableImplServiceName") + "&returnLocation=" + basePath + mapping.getPath() + ".do";
		return new ActionForward(lookupUrl, true);
	}

	public ActionForward routeToAppSpecificRecipient(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		WorkflowRoutingForm routingForm = (WorkflowRoutingForm) form;
		AppSpecificRouteRecipient recipient = routingForm.getAppSpecificRouteRecipient();
		recipient.setActionRequested(routingForm.getAppSpecificRouteActionRequestCd());
		recipient.setType(routingForm.getAppSpecificRouteRecipientType());
		validateAppSpecificRoute(recipient);

		try {
			String routeNodeName = getAdHocRouteNodeName(routingForm.getWorkflowDocument().getRouteHeaderId());
			if (KEWConstants.PERSON.equals(recipient.getType())) {
				routingForm.getWorkflowDocument().appSpecificRouteDocumentToUser(recipient.getActionRequested(), routeNodeName, routingForm.getAnnotation(), new NetworkIdDTO(recipient.getId()), "", true);
			} else {
				routingForm.getWorkflowDocument().appSpecificRouteDocumentToWorkgroup(recipient.getActionRequested(), routeNodeName, routingForm.getAnnotation(), new WorkgroupNameIdDTO(recipient.getId()), "", true);
			}
			routingForm.getAppSpecificRouteList().add(recipient);
			routingForm.resetAppSpecificRoute();
		} catch (Exception e) {
			LOG.error("Error generating app specific route request", e);
			throw new WorkflowServiceErrorException("AppSpecific Route Error", new WorkflowServiceErrorImpl("AppSpecific Route Error", "appspecificroute.systemerror"));
		}
		return mapping.getInputForward();
	}

	public ActionForward noOp(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		return mapping.findForward("basic");
	}

	protected String getAdHocRouteNodeName(Long routeHeaderId) throws WorkflowException {
		WorkflowInfo info = new WorkflowInfo();
		RouteNodeInstanceDTO[] nodeInstances = info.getActiveNodeInstances(routeHeaderId);
		if (nodeInstances == null || nodeInstances.length == 0) {
			nodeInstances = info.getTerminalNodeInstances(routeHeaderId);
		}
		if (nodeInstances == null || nodeInstances.length == 0) {
			throw new WorkflowException("Could not locate a node on the document to send the ad hoc request to.");
		}
		return nodeInstances[0].getName();
	}

	protected void validateAppSpecificRoute(AppSpecificRouteRecipient recipient) {
		List messages = new ArrayList();
		if (recipient.getId() == null || recipient.getId().trim().equals("")) {
			messages.add(new WorkflowServiceErrorImpl("AppSpecific Recipient empty", "appspecificroute.recipient.required"));
		}

		if (KEWConstants.PERSON.equals(recipient.getType()) && recipient.getId() != null && !recipient.getId().trim().equals("")) {
			try {
				getUserService().getWorkflowUser(new AuthenticationUserId(recipient.getId()));
			} catch (KEWUserNotFoundException e) {
				LOG.error("App Specific user recipient not found", e);
				messages.add(new WorkflowServiceErrorImpl("AppSpecific Recipient invalid", "appspecificroute.user.invalid"));
			}
		}

		if (KEWConstants.WORKGROUP.equals(recipient.getType()) && recipient.getId() != null && !recipient.getId().trim().equals("")) {
			if (getWorkgroupService().getWorkgroup(new GroupNameId(recipient.getId())) == null) {
				messages.add(new WorkflowServiceErrorImpl("AppSpecific Recipient workgroup invalid", "appspecificroute.workgroup.invalid"));
			}
		}

		if (!messages.isEmpty()) {
			throw new WorkflowServiceErrorException("AppSpecific Route validation Error", messages);
		}

	}

	private UserService getUserService() {
		return (UserService) KEWServiceLocator.getService(KEWServiceLocator.USER_SERVICE);
	}

	private WorkgroupService getWorkgroupService() {
		return (WorkgroupService) KEWServiceLocator.getService(KEWServiceLocator.WORKGROUP_SRV);
	}

	public ActionForward cancelDocument(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		WorkflowRoutingForm routingForm = (WorkflowRoutingForm) form;
		DocumentRouteHeaderValue routeHeader = KEWServiceLocator.getRouteHeaderService().getRouteHeader(routingForm.getDocId());
		KEWServiceLocator.getWorkflowDocumentService().cancelDocument(getUserSession(request).getWorkflowUser(), routeHeader, routingForm.getAnnotation());
		request.setAttribute("routeHeaderId", routingForm.getDocId());
		return mapping.findForward("DeleteRouteHeaderConfirmation");
	}

	public ActionForward removeAppSpecificRecipient(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		WorkflowRoutingForm routingForm = (WorkflowRoutingForm) form;
		int removedIndex = new Integer(routingForm.getRemovedAppSpecificRecipient()).intValue();
		AppSpecificRouteRecipient recipient = (AppSpecificRouteRecipient) routingForm.getAppSpecificRouteList().get(removedIndex);
		WorkflowDocument document = routingForm.getWorkflowDocument();
		List revocations = new ArrayList();
		String[] nodeNames = document.getNodeNames();
		for (int index = 0; index < nodeNames.length; index++) {
			String nodeName = nodeNames[index];
			AdHocRevokeDTO revoke = new AdHocRevokeDTO(nodeName);
			if (recipient.getType().equals("person")) {
				revoke.setUserId(new NetworkIdDTO(recipient.getId()));
			} else if (recipient.getType().equals("workgroup")) {
				revoke.setWorkgroupId(new WorkgroupNameIdDTO(recipient.getId()));
			}
			revocations.add(revoke);
		}
		for (Iterator iterator = revocations.iterator(); iterator.hasNext();) {
			AdHocRevokeDTO revoke = (AdHocRevokeDTO) iterator.next();
			// TODO print better message here
			document.revokeAdHocRequests(revoke, "Removed ad hoc recipient from node '" + revoke.getNodeName() + "'.");
		}
		routingForm.getAppSpecificRouteList().remove(removedIndex);
		routingForm.setRemovedAppSpecificRecipient(null);
		return mapping.getInputForward();
	}

}