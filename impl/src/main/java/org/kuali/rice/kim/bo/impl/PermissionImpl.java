/*
 * Copyright 2007 The Kuali Foundation
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
package org.kuali.rice.kim.bo.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kim.bo.role.impl.KimPermissionImpl;
import org.kuali.rice.kim.util.KimConstants;
import org.kuali.rice.kns.util.TypedArrayList;

/**
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 */
public class PermissionImpl extends KimPermissionImpl {

	private static final long serialVersionUID = 1L;
	
	List<RoleImpl> assignedToRoles = new TypedArrayList(RoleImpl.class);
	
	protected String assignedToRoleNamespaceForLookup;
	protected String assignedToRoleNameForLookup;
	protected RoleImpl assignedToRole;
	protected String assignedToPrincipalNameForLookup;
	protected Person assignedToPrincipal;
	protected String assignedToGroupNamespaceForLookup;
	protected String assignedToGroupNameForLookup;
	protected GroupImpl assignedToGroup;
	protected String attributeName;
	protected String attributeValue;
	protected String detailCriteria;
	
	/**
	 * @return the assignedToRoles
	 */
	public String getAssignedToRolesToDisplay() {
		StringBuffer assignedToRolesToDisplay = new StringBuffer();
		for(RoleImpl roleImpl: assignedToRoles){
			assignedToRolesToDisplay.append(getRoleDetailsToDisplay(roleImpl));
		}
		return StringUtils.chomp( assignedToRolesToDisplay.toString(), KimConstants.KimUIConstants.COMMA_SEPARATOR);
	}

	public String getRoleDetailsToDisplay(RoleImpl roleImpl){
		return roleImpl.getNamespaceCode()+" "+roleImpl.getRoleName()+KimConstants.KimUIConstants.COMMA_SEPARATOR;
	}
	
	/**
	 * @return the assignedToGroupNameForLookup
	 */
	public String getAssignedToGroupNameForLookup() {
		return this.assignedToGroupNameForLookup;
	}

	/**
	 * @param assignedToGroupNameForLookup the assignedToGroupNameForLookup to set
	 */
	public void setAssignedToGroupNameForLookup(String assignedToGroupNameForLookup) {
		this.assignedToGroupNameForLookup = assignedToGroupNameForLookup;
	}

	/**
	 * @return the assignedToGroupNamespaceForLookup
	 */
	public String getAssignedToGroupNamespaceForLookup() {
		return this.assignedToGroupNamespaceForLookup;
	}

	/**
	 * @param assignedToGroupNamespaceForLookup the assignedToGroupNamespaceForLookup to set
	 */
	public void setAssignedToGroupNamespaceForLookup(
			String assignedToGroupNamespaceForLookup) {
		this.assignedToGroupNamespaceForLookup = assignedToGroupNamespaceForLookup;
	}

	/**
	 * @return the assignedToPrincipalNameForLookup
	 */
	public String getAssignedToPrincipalNameForLookup() {
		return this.assignedToPrincipalNameForLookup;
	}

	/**
	 * @param assignedToPrincipalNameForLookup the assignedToPrincipalNameForLookup to set
	 */
	public void setAssignedToPrincipalNameForLookup(
			String assignedToPrincipalNameForLookup) {
		this.assignedToPrincipalNameForLookup = assignedToPrincipalNameForLookup;
	}

	/**
	 * @return the assignedToRoleNameForLookup
	 */
	public String getAssignedToRoleNameForLookup() {
		return this.assignedToRoleNameForLookup;
	}

	/**
	 * @param assignedToRoleNameForLookup the assignedToRoleNameForLookup to set
	 */
	public void setAssignedToRoleNameForLookup(String assignedToRoleNameForLookup) {
		this.assignedToRoleNameForLookup = assignedToRoleNameForLookup;
	}

	/**
	 * @return the assignedToRoleNamespaceForLookup
	 */
	public String getAssignedToRoleNamespaceForLookup() {
		return this.assignedToRoleNamespaceForLookup;
	}

	/**
	 * @param assignedToRoleNamespaceForLookup the assignedToRoleNamespaceForLookup to set
	 */
	public void setAssignedToRoleNamespaceForLookup(
			String assignedToRoleNamespaceForLookup) {
		this.assignedToRoleNamespaceForLookup = assignedToRoleNamespaceForLookup;
	}

	/**
	 * @return the attributeValue
	 */
	public String getAttributeValue() {
		return this.attributeValue;
	}

	/**
	 * @param attributeValue the attributeValue to set
	 */
	public void setAttributeValue(String attributeValue) {
		this.attributeValue = attributeValue;
	}

	/**
	 * @return the assignedToRoles
	 */
	public List<RoleImpl> getAssignedToRoles() {
		return this.assignedToRoles;
	}

	/**
	 * @param assignedToRoles the assignedToRoles to set
	 */
	public void setAssignedToRoles(List<RoleImpl> assignedToRoles) {
		this.assignedToRoles = assignedToRoles;
	}

	/**
	 * @return the assignedToGroup
	 */
	public GroupImpl getAssignedToGroup() {
		return this.assignedToGroup;
	}

	/**
	 * @param assignedToGroup the assignedToGroup to set
	 */
	public void setAssignedToGroup(GroupImpl assignedToGroup) {
		this.assignedToGroup = assignedToGroup;
	}

	/**
	 * @return the assignedToPrincipal
	 */
	public Person getAssignedToPrincipal() {
		return this.assignedToPrincipal;
	}

	/**
	 * @param assignedToPrincipal the assignedToPrincipal to set
	 */
	public void setAssignedToPrincipal(Person assignedToPrincipal) {
		this.assignedToPrincipal = assignedToPrincipal;
	}

	/**
	 * @return the assignedToRole
	 */
	public RoleImpl getAssignedToRole() {
		return this.assignedToRole;
	}

	/**
	 * @param assignedToRole the assignedToRole to set
	 */
	public void setAssignedToRole(RoleImpl assignedToRole) {
		this.assignedToRole = assignedToRole;
	}

	public String getDetailCriteria() {
		return this.detailCriteria;
	}

	public void setDetailCriteria(String detailCriteria) {
		this.detailCriteria = detailCriteria;
	}

	/**
	 * @return the attributeName
	 */
	public String getAttributeName() {
		return this.attributeName;
	}

	/**
	 * @param attributeName the attributeName to set
	 */
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

}