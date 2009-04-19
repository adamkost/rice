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
package org.kuali.rice.kew.xml.export;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.kuali.rice.kew.doctype.DocumentTypeAttribute;
import org.kuali.rice.kew.doctype.DocumentTypePolicy;
import org.kuali.rice.kew.doctype.bo.DocumentType;
import org.kuali.rice.kew.engine.node.BranchPrototype;
import org.kuali.rice.kew.engine.node.NodeType;
import org.kuali.rice.kew.engine.node.Process;
import org.kuali.rice.kew.engine.node.RouteNode;
import org.kuali.rice.kew.exception.InvalidXmlException;
import org.kuali.rice.kew.exception.ResourceUnavailableException;
import org.kuali.rice.kew.exception.WorkflowRuntimeException;
import org.kuali.rice.kew.export.ExportDataSet;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kew.util.Utilities;
import org.kuali.rice.kew.util.XmlHelper;
import org.kuali.rice.kew.xml.XmlConstants;
import org.kuali.rice.kew.xml.XmlRenderer;
import org.kuali.rice.kim.bo.Group;


/**
 * Exports {@link DocumentType}s to XML.
 *
 * @see DocumentType
 *
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 */
public class DocumentTypeXmlExporter implements XmlExporter, XmlConstants {

    protected final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(getClass());
    
    private XmlRenderer renderer = new XmlRenderer(DOCUMENT_TYPE_NAMESPACE);

    public Element export(ExportDataSet dataSet) {
        if (!dataSet.getDocumentTypes().isEmpty()) {
            Collections.sort(dataSet.getDocumentTypes(), new DocumentTypeParentComparator());
            Element rootElement = renderer.renderElement(null, DOCUMENT_TYPES);
            rootElement.setAttribute(SCHEMA_LOCATION_ATTR, DOCUMENT_TYPE_SCHEMA_LOCATION, SCHEMA_NAMESPACE);
            for (Iterator iterator = dataSet.getDocumentTypes().iterator(); iterator.hasNext();) {
                DocumentType documentType = (DocumentType) iterator.next();
                exportDocumentType(rootElement, documentType);
            }
            return rootElement;
        }
        return null;
    }

    private void exportDocumentType(Element parent, DocumentType documentType) {
        Element docTypeElement = renderer.renderElement(parent, DOCUMENT_TYPE);
        List flattenedNodes = KEWServiceLocator.getRouteNodeService().getFlattenedNodes(documentType, false);
        // derive a default exception workgroup by looking at how the nodes are configured
        boolean hasDefaultExceptionWorkgroup = hasDefaultExceptionWorkgroup(flattenedNodes);
        renderer.renderTextElement(docTypeElement, NAME, documentType.getName());
        if (documentType.getParentDocType() != null) {
            renderer.renderTextElement(docTypeElement, PARENT, documentType.getParentDocType().getName());
        }
        renderer.renderTextElement(docTypeElement, DESCRIPTION, documentType.getDescription());
        renderer.renderTextElement(docTypeElement, LABEL, documentType.getLabel());
        if (!StringUtils.isBlank(documentType.getActualServiceNamespace())) {
            renderer.renderTextElement(docTypeElement, SERVICE_NAMESPACE, documentType.getActualServiceNamespace());
        }
        renderer.renderTextElement(docTypeElement, POST_PROCESSOR_NAME, documentType.getPostProcessorName());
        Group superUserWorkgroup = documentType.getSuperUserWorkgroupNoInheritence();
        if (superUserWorkgroup != null) {
            renderer.renderTextElement(docTypeElement, SUPER_USER_WORKGROUP_NAME, superUserWorkgroup.getNamespaceCode().trim() + KEWConstants.KIM_GROUP_NAMESPACE_NAME_DELIMITER_CHARACTER + superUserWorkgroup.getGroupName().trim());
        }
        Group blanketWorkgroup = documentType.getBlanketApproveWorkgroup();
        if (blanketWorkgroup != null){
        	renderer.renderTextElement(docTypeElement, BLANKET_APPROVE_WORKGROUP_NAME, blanketWorkgroup.getNamespaceCode().trim() + KEWConstants.KIM_GROUP_NAMESPACE_NAME_DELIMITER_CHARACTER + blanketWorkgroup.getGroupName().trim());
        }
        if (documentType.getBlanketApprovePolicy() != null){
        	renderer.renderTextElement(docTypeElement, BLANKET_APPROVE_POLICY, documentType.getBlanketApprovePolicy());
        }
        Group reportingWorkgroup = documentType.getReportingWorkgroup();
        if (reportingWorkgroup != null) {
            renderer.renderTextElement(docTypeElement, REPORTING_WORKGROUP_NAME, reportingWorkgroup.getNamespaceCode().trim() + KEWConstants.KIM_GROUP_NAMESPACE_NAME_DELIMITER_CHARACTER + reportingWorkgroup.getGroupName().trim());
        }
        if (!flattenedNodes.isEmpty() && hasDefaultExceptionWorkgroup) {
        	Group exceptionWorkgroup = ((RouteNode)flattenedNodes.get(0)).getExceptionWorkgroup();
        	if (exceptionWorkgroup != null) {
        		renderer.renderTextElement(docTypeElement, DEFAULT_EXCEPTION_WORKGROUP_NAME, exceptionWorkgroup.getNamespaceCode().trim() + KEWConstants.KIM_GROUP_NAMESPACE_NAME_DELIMITER_CHARACTER + exceptionWorkgroup.getGroupName().trim());
        	}
        }
        if (StringUtils.isNotBlank(documentType.getUnresolvedDocHandlerUrl())) {
            renderer.renderTextElement(docTypeElement, DOC_HANDLER, documentType.getUnresolvedDocHandlerUrl());
        }
        if (!StringUtils.isBlank(documentType.getUnresolvedHelpDefinitionUrl())) {
            renderer.renderTextElement(docTypeElement, HELP_DEFINITION_URL, documentType.getUnresolvedHelpDefinitionUrl());
        }
        if (!StringUtils.isBlank(documentType.getActualNotificationFromAddress())) {
        	renderer.renderTextElement(docTypeElement, NOTIFICATION_FROM_ADDRESS, documentType.getActualNotificationFromAddress());
        }
        renderer.renderBooleanElement(docTypeElement, ACTIVE, documentType.getActive(), true);
        exportPolicies(docTypeElement, documentType.getPolicies());
      exportAttributes(docTypeElement, documentType.getDocumentTypeAttributes());
      exportSecurity(docTypeElement, documentType.getDocumentTypeSecurityXml());
      	if (!StringUtils.isBlank(documentType.getRoutingVersion())) {
      		renderer.renderTextElement(docTypeElement, ROUTING_VERSION, documentType.getRoutingVersion());
      	}
        exportRouteData(docTypeElement, documentType, flattenedNodes, hasDefaultExceptionWorkgroup);
    }

    private void exportPolicies(Element parent, Collection policies) {
        if (!policies.isEmpty()) {
            Element policiesElement = renderer.renderElement(parent, POLICIES);
            for (Iterator iterator = policies.iterator(); iterator.hasNext();) {
                DocumentTypePolicy policy = (DocumentTypePolicy) iterator.next();
                Element policyElement = renderer.renderElement(policiesElement, POLICY);
                renderer.renderTextElement(policyElement, NAME, policy.getPolicyName());
                renderer.renderBooleanElement(policyElement, VALUE, policy.getPolicyValue(), false);
            }
        }
    }

    private void exportAttributes(Element parent, List attributes) {
        if (!attributes.isEmpty()) {
            Element attributesElement = renderer.renderElement(parent, ATTRIBUTES);
            for (Iterator iterator = attributes.iterator(); iterator.hasNext();) {
                DocumentTypeAttribute attribute = (DocumentTypeAttribute) iterator.next();
                Element attributeElement = renderer.renderElement(attributesElement, ATTRIBUTE);
                renderer.renderTextElement(attributeElement, NAME, attribute.getRuleAttribute().getName());
            }
        }
    }

    private void exportSecurity(Element parent, String securityXML) {
      if (!Utilities.isEmpty(securityXML)) {
        try {
          org.jdom.Document securityDoc = new SAXBuilder().build(new StringReader(securityXML));
          XmlHelper.propogateNamespace(securityDoc.getRootElement(), DOCUMENT_TYPE_NAMESPACE);
          parent.addContent(securityDoc.getRootElement().detach());
        } catch (IOException e) {
          throw new WorkflowRuntimeException("Error parsing doctype security XML.");
        } catch (JDOMException e) {
          throw new WorkflowRuntimeException("Error parsing doctype security XML.");
        }
      }
    }

    private void exportRouteData(Element parent, DocumentType documentType, List flattenedNodes, boolean hasDefaultExceptionWorkgroup) {
        if (!flattenedNodes.isEmpty()) {
            Element routePathsElement = renderer.renderElement(parent, ROUTE_PATHS);
            for (Iterator iterator = documentType.getProcesses().iterator(); iterator.hasNext();) {
                Process process = (Process) iterator.next();
                Element routePathElement = renderer.renderElement(routePathsElement, ROUTE_PATH);
                if (!process.isInitial()) {
                    renderer.renderAttribute(routePathElement, INITIAL_NODE, process.getInitialRouteNode().getRouteNodeName());
                    renderer.renderAttribute(routePathElement, PROCESS_NAME, process.getName());
                }
                exportProcess(routePathElement, process);
            }

            Element routeNodesElement = renderer.renderElement(parent, ROUTE_NODES);
            for (Iterator iterator = flattenedNodes.iterator(); iterator.hasNext();) {
                RouteNode node = (RouteNode) iterator.next();
                exportRouteNode(routeNodesElement, node, hasDefaultExceptionWorkgroup);
            }
        }
    }

    /* default exception workgroup is not stored independently in db, so derive
     * one from the definition itself: if all nodes have the *same* exception workgroup name
     * defined, then this is tantamount to a *default* exception workgroup and can be
     * used as such.
     */
    private boolean hasDefaultExceptionWorkgroup(List flattenedNodes) {
        boolean hasDefaultExceptionWorkgroup = true;
        String exceptionWorkgroupName = null;
        for (Iterator iterator = flattenedNodes.iterator(); iterator.hasNext();) {
            RouteNode node = (RouteNode) iterator.next();
            if (exceptionWorkgroupName == null) {
                exceptionWorkgroupName = node.getExceptionWorkgroupName();
            }
            if (exceptionWorkgroupName == null || !exceptionWorkgroupName.equals(node.getExceptionWorkgroupName())) {
                hasDefaultExceptionWorkgroup = false;
                break;
            }
        }
        return hasDefaultExceptionWorkgroup;
    }

    private void exportProcess(Element parent, Process process) {
        exportNodeGraph(parent, process.getInitialRouteNode(), null);
    }

    private void exportNodeGraph(Element parent, RouteNode node, SplitJoinContext splitJoinContext) {
        NodeType nodeType = getNodeTypeForNode(node);
        if (nodeType.isAssignableFrom(NodeType.SPLIT)) {
            exportSplitNode(parent, node, nodeType, splitJoinContext);
        } else if (nodeType.isAssignableFrom(NodeType.JOIN)) {
            exportJoinNode(parent, node, nodeType, splitJoinContext);
        } else {
            exportSimpleNode(parent, node, nodeType, splitJoinContext);
        }
    }

    private void exportSimpleNode(Element parent, RouteNode node, NodeType nodeType, SplitJoinContext splitJoinContext) {
        Element simpleElement = renderNodeElement(parent, node, nodeType);
        if (node.getNextNodes().size() > 1) {
            throw new WorkflowRuntimeException("Simple node cannot have more than one next node: " + node.getRouteNodeName());
        }
        if (node.getNextNodes().size() == 1) {
            RouteNode nextNode = (RouteNode)node.getNextNodes().get(0);
            renderer.renderAttribute(simpleElement, NEXT_NODE, nextNode.getRouteNodeName());
            exportNodeGraph(parent, nextNode, splitJoinContext);
        }
    }

    private void exportSplitNode(Element parent, RouteNode node, NodeType nodeType, SplitJoinContext splitJoinContext) {
        Element splitElement = renderNodeElement(parent, node, nodeType);
        SplitJoinContext newSplitJoinContext = new SplitJoinContext(node);
        for (Iterator iterator = node.getNextNodes().iterator(); iterator.hasNext();) {
            RouteNode nextNode = (RouteNode) iterator.next();
            BranchPrototype branch = nextNode.getBranch();
            if (branch == null) {
                throw new WorkflowRuntimeException("Found a split next node with no associated branch prototype: " + nextNode.getRouteNodeName());
            }
            exportBranch(splitElement, nextNode, branch, newSplitJoinContext);
        }
        RouteNode joinNode = newSplitJoinContext.joinNode;
        if (joinNode == null) {
            if (node.getNextNodes().size() > 0) {
                throw new WorkflowRuntimeException("Could not locate the join node for the given split node " + node.getRouteNodeName());
            }
        } else {
            renderNodeElement(splitElement, joinNode, newSplitJoinContext.joinNodeType);
            if (joinNode.getNextNodes().size() > 1) {
                throw new WorkflowRuntimeException("Join node cannot have more than one next node: " + joinNode.getRouteNodeName());
            }
            if (joinNode.getNextNodes().size() == 1) {
                RouteNode nextNode = (RouteNode)joinNode.getNextNodes().get(0);
                renderer.renderAttribute(splitElement, NEXT_NODE, nextNode.getRouteNodeName());
                exportNodeGraph(parent, nextNode, splitJoinContext);
            }
        }
    }

    private void exportBranch(Element parent, RouteNode node, BranchPrototype branch, SplitJoinContext splitJoinContext) {
        Element branchElement = renderer.renderElement(parent, BRANCH);
        renderer.renderAttribute(branchElement, NAME, branch.getName());
        exportNodeGraph(branchElement, node, splitJoinContext);
    }

    private void exportJoinNode(Element parent, RouteNode node, NodeType nodeType, SplitJoinContext splitJoinContext) {
        if (splitJoinContext == null) {
            // this is the case where a join node is defined as part of a sub process to be used by a dynamic node, in this case it is
            // not associated with a proper split node.
            if (!node.getNextNodes().isEmpty()) {
                throw new WorkflowRuntimeException("Could not export join node with next nodes that is not contained within a split.");
            }
            renderNodeElement(parent, node, nodeType);
        } else if (splitJoinContext.joinNode == null) {
            // this is the case where we are "inside" the split node in the XML, by setting up this context, the calling code knows that
            // when it renders all of the branches of the split node, it can then use this context info to render the join node before
            // closing the split
            splitJoinContext.joinNode = node;
            splitJoinContext.joinNodeType = nodeType;
        }
    }

    private Element renderNodeElement(Element parent, RouteNode node, NodeType nodeType) {
	String nodeName = nodeType.getName();
	// if it's a request activation node, be sure to export it as a simple node
	if (nodeType.equals(NodeType.REQUEST_ACTIVATION)) {
	    nodeName = NodeType.SIMPLE.getName();
	}
        Element nodeElement = renderer.renderElement(parent, nodeName);
        renderer.renderAttribute(nodeElement, NAME, node.getRouteNodeName());
        return nodeElement;
    }

    /**
     * Exists for backward compatability for nodes which don't have a content fragment.
     */
    private void exportRouteNodeOld(Element parent, RouteNode node, boolean hasDefaultExceptionWorkgroup) {
        NodeType nodeType = getNodeTypeForNode(node);
        Element nodeElement = renderer.renderElement(parent, nodeType.getName());
        renderer.renderAttribute(nodeElement, NAME, node.getRouteNodeName());
        if (!hasDefaultExceptionWorkgroup) {
        	if (!StringUtils.isBlank(node.getExceptionWorkgroupName())) {
        		renderer.renderTextElement(nodeElement, EXCEPTION_WORKGROUP_NAME, node.getExceptionWorkgroupName());
        	}
        }
        if (supportsActivationType(nodeType) && !StringUtils.isBlank(node.getActivationType())) {
            renderer.renderTextElement(nodeElement, ACTIVATION_TYPE, node.getActivationType());
        }
        if (supportsRouteMethod(nodeType)) {
            exportRouteMethod(nodeElement, node);
            renderer.renderBooleanElement(nodeElement, MANDATORY_ROUTE, node.getMandatoryRouteInd(), false);
            renderer.renderBooleanElement(nodeElement, FINAL_APPROVAL, node.getFinalApprovalInd(), false);
        }
        if (nodeType.isCustomNode(node.getNodeType())) {
            renderer.renderTextElement(nodeElement, TYPE, node.getNodeType());
        }
    }

    private void exportRouteNode(Element parent, RouteNode node, boolean hasDefaultExceptionWorkgroup) {
	String contentFragment = node.getContentFragment();
	if (StringUtils.isBlank(contentFragment)) {
	    exportRouteNodeOld(parent, node, hasDefaultExceptionWorkgroup);
	} else {
	    try {
		Document document = XmlHelper.buildJDocument(new StringReader(contentFragment));
		Element rootElement = document.detachRootElement();
		XmlHelper.propogateNamespace(rootElement, parent.getNamespace());
		parent.addContent(rootElement);
	    } catch (InvalidXmlException e) {
		throw new WorkflowRuntimeException("Failed to load the content fragment.", e);
	    }
	}
    }


    private NodeType getNodeTypeForNode(RouteNode node) {
        NodeType nodeType = null;
        String errorMessage = "Could not determine proper XML element for the given node type: " + node.getNodeType();
        try {
            nodeType = NodeType.fromClassName(node.getNodeType());
        } catch (ResourceUnavailableException e) {
            throw new WorkflowRuntimeException(errorMessage, e);
        }
        if (nodeType == null) {
            throw new WorkflowRuntimeException(errorMessage);
        }
        return nodeType;
    }

    /**
     * Any node can support activation type, this use to not be the case but now it is.
     */
    private boolean supportsActivationType(NodeType nodeType) {
        return true;
    }

    /**
     * Any node can support route methods, this use to not be the case but now it is.
     */
    private boolean supportsRouteMethod(NodeType nodeType) {
        return true;
    }

    private void exportRouteMethod(Element parent, RouteNode node) {
        if (!StringUtils.isBlank(node.getRouteMethodName())) {
            String routeMethodCode = node.getRouteMethodCode();
            String elementName = null;
            if (KEWConstants.ROUTE_LEVEL_FLEX_RM.equals(routeMethodCode)) {
                elementName = RULE_TEMPLATE;
            } else if (KEWConstants.ROUTE_LEVEL_ROUTE_MODULE.equals(routeMethodCode)) {
                elementName = ROUTE_MODULE;
            } else {
                throw new WorkflowRuntimeException("Invalid route method code '"+routeMethodCode+"' for node " + node.getRouteNodeName());
            }
            renderer.renderTextElement(parent, elementName, node.getRouteMethodName());
        }
    }

    private class DocumentTypeParentComparator implements Comparator {

        public int compare(Object object1, Object object2) {
            DocumentType docType1 = (DocumentType)object1;
            DocumentType docType2 = (DocumentType)object2;
            Integer depth1 = getDepth(docType1);
            Integer depth2 = getDepth(docType2);
            return depth1.compareTo(depth2);
        }

        private Integer getDepth(DocumentType docType) {
        	int depth = 0;
        	while ((docType = docType.getParentDocType()) != null) {
        		depth++;
        	}
        	return new Integer(depth);
        }

    }

    private class SplitJoinContext {
        public RouteNode splitNode;
        public RouteNode joinNode;
        public NodeType joinNodeType;
        public SplitJoinContext(RouteNode splitNode) {
            this.splitNode = splitNode;
        }
    }


}
