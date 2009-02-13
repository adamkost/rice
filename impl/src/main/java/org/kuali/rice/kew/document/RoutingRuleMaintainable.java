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
package org.kuali.rice.kew.document;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.kuali.rice.core.exception.RiceRuntimeException;
import org.kuali.rice.kew.rule.GroupRuleResponsibility;
import org.kuali.rice.kew.rule.PersonRuleResponsibility;
import org.kuali.rice.kew.rule.RuleBaseValues;
import org.kuali.rice.kew.rule.RuleResponsibility;
import org.kuali.rice.kew.rule.web.WebRuleUtils;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.document.MaintenanceLock;
import org.kuali.rice.kns.maintenance.KualiMaintainableImpl;
import org.kuali.rice.kns.maintenance.Maintainable;
import org.kuali.rice.kns.web.ui.Section;

/**
 * This class is the maintainable implementation for Routing Rules
 * in KEW (represented by the {@link RuleBaseValues} business object).
 *
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 *
 */
public class RoutingRuleMaintainable extends KualiMaintainableImpl {

    private static final long serialVersionUID = -5920808902137192662L;

	private static final String RULE_ATTRIBUTES_SECTION_ID = "RuleAttributes";
	private static final String ID_SEPARATOR = ":";

	/**
	 * Override the getSections method on this maintainable so that the Section Containing the various Rule Attributes
	 * can be dynamically generated based on the RuleTemplate which is selected.
	 */
	@Override
	public List getSections(MaintenanceDocument document, Maintainable oldMaintainable) {
		List<Section> sections = super.getSections(document, oldMaintainable);
		return WebRuleUtils.customizeSections(getThisRule(), sections);

	}

	/**
	 * On creation of a new rule document, we must validate that a rule template and document type are set.
	 */
	@Override
	public void processAfterNew(MaintenanceDocument document,
			Map<String, String[]> parameters) {
		WebRuleUtils.validateRuleTemplateAndDocumentType(getOldRule(document), getNewRule(document), parameters);
		WebRuleUtils.establishDefaultRuleValues(getNewRule(document));
		getNewRule(document).setRouteHeaderId(new Long(document.getDocumentHeader().getDocumentNumber()));
	}

	/**
	 * This is a hack to get around the fact that when a document is first created, this value is
 	 * true which causes issues if you want to be able to initialize fields on  the document using
 	 * request parameters.  See SectionBridge.toSection for the "if" block where it populates
 	 * Field.propertyValue to see why this causes problems
	 */
	@Override
	public boolean isGenerateDefaultValues() {
		return false;
	}

    /**
     * A complete override of the implementation for saving a Rule
     */
    @Override
    public void saveBusinessObject() {
    	WebRuleUtils.clearKeysForSave(getThisRule());
    	WebRuleUtils.translateResponsibilitiesForSave(getThisRule());
    	WebRuleUtils.translateFieldValuesForSave(getThisRule());
    	KEWServiceLocator.getRuleService().makeCurrent(getThisRule());
    }

    @Override
    public void processAfterCopy(MaintenanceDocument document, Map<String, String[]> parameters) {
    	WebRuleUtils.processRuleForCopy(document.getDocumentNumber(), getOldRule(document), getNewRule(document));
        super.processAfterCopy(document, parameters);
    }

	@Override
	public void processAfterEdit(MaintenanceDocument document,
			Map<String, String[]> parameters) {
		if (!getOldRule(document).getCurrentInd()) {
			throw new RiceRuntimeException("Cannot edit a non-current version of a rule.");
		}
		WebRuleUtils.populateForCopyOrEdit(getOldRule(document), getNewRule(document));
		getNewRule(document).setRouteHeaderId(new Long(document.getDocumentHeader().getDocumentNumber()));
		super.processAfterEdit(document, parameters);
	}

	/**
	 * Returns the new RuleBaseValues business object.
	 */
	protected RuleBaseValues getNewRule(MaintenanceDocument document) {
		return (RuleBaseValues)document.getNewMaintainableObject().getBusinessObject();
	}

	/**
	 * Returns the old RuleBaseValues business object.
	 */
	protected RuleBaseValues getOldRule(MaintenanceDocument document) {
		return (RuleBaseValues)document.getOldMaintainableObject().getBusinessObject();
	}

	/**
	 * Returns the RuleBaseValues business object associated with this Maintainable.
	 */
	protected RuleBaseValues getThisRule() {
		return (RuleBaseValues)getBusinessObject();
	}

	/**
	 * Overridden implementation of maintenance locks.  The default locking for Routing Rules
	 * is based on previous version (can't route more than one rule based off the same
	 * previous verison).  However, for the first version of a rule, the previous version id
	 * will be null.
	 *
	 * So for a new Route Rule maintenance document we don't want any locks generated.
	 *
	 * TODO can we just let the locking key be the primary key? (ruleBaseValuesId)
	 */
	@Override
	public List<MaintenanceLock> generateMaintenanceLocks() {
		if (getThisRule().getPreviousVersionId() == null) {
			return Collections.emptyList();
		}
		return super.generateMaintenanceLocks();
	}

	@Override
	public String getDocumentTitle(MaintenanceDocument document) {
		StringBuffer title = new StringBuffer();
        RuleBaseValues rule = getThisRule();
        if (rule.getPreviousVersionId() != null) {
            title.append("Editing Rule '").append(rule.getDescription()).append("'");
        } else {
            title.append("Adding Rule '").append(rule.getDescription()).append("'");
        }
        return title.toString();
	}

	/**
	 * This overridden method ...
	 *
	 * @see org.kuali.rice.kns.maintenance.KualiMaintainableImpl#prepareForSave()
	 */
	@Override
	public void prepareForSave() {
		super.prepareForSave();
		WebRuleUtils.translateResponsibilitiesForSave(getThisRule());
	}

}
