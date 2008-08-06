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
package org.kuali.rice.kew.routetemplate;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.kew.engine.RouteContext;
import org.kuali.rice.kew.exception.KEWUserNotFoundException;
import org.kuali.rice.kew.exception.WorkflowException;
import org.kuali.rice.util.ClassLoaderUtils;


/**
 * {@link Rule} implementation 
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 */
class RuleImpl implements Rule {
    /**
     * The default type of rule expression implementation to use if none is explicitly
     * specified for the node.
     */
    public static final String DEFAULT_RULE_EXPRESSION = "WorkflowAttribute";
    /**
     * Package in which rule expression implementations live
     */
    private static final String RULE_EXPRESSION_PACKAGE = "org.kuali.rice.kew.routetemplate";
    /**
     * The class name suffix all rule expressions should have; e.g. FooRuleExpression
     */
    private static final String RULE_EXPRESSION_SUFFIX= "RuleExpression";

    /**
     * The BO of the rule definition in the system
     */
    private final RuleBaseValues ruleDefinition;

    RuleImpl(RuleBaseValues ruleDefinition) {
        this.ruleDefinition = ruleDefinition;
    }

    public RuleBaseValues getDefinition() {
        return ruleDefinition;
    }

    // loads a RuleExpression implementation
    protected RuleExpression loadRuleExpression(String type) throws WorkflowException {
        if (type == null) {
            type = DEFAULT_RULE_EXPRESSION;
        }
        // type is of the format 'category:qualifier'
        // we just want the category
        int colon = type.indexOf(':');
        if (colon == -1) colon = type.length();
        type = type.substring(0, colon);
        type = StringUtils.capitalize(type);

        // load up the rule expression implementation
        String className = RULE_EXPRESSION_PACKAGE + "." + type + RULE_EXPRESSION_SUFFIX;
        Class<?> ruleExpressionClass;
        try {
            ruleExpressionClass = ClassLoaderUtils.getDefaultClassLoader().loadClass(className);
        } catch (ClassNotFoundException cnfe) {
            throw new WorkflowException("Rule expression implementation '" + className + "' not found", cnfe);
        }
        if (!RuleExpression.class.isAssignableFrom(ruleExpressionClass)) {
            throw new WorkflowException("Specified class '" + ruleExpressionClass + "' does not implement RuleExpression interface");
        }
        RuleExpression ruleExpression;
        try {
            ruleExpression = ((Class<RuleExpression>) ruleExpressionClass).newInstance();
        } catch (Exception e) {
            throw new WorkflowException("Error instantiating rule expression implementation '" + ruleExpressionClass + "'", e);
        }

        return ruleExpression;
    }

    public RuleExpressionResult evaluate(Rule rule, RouteContext context) throws KEWUserNotFoundException, WorkflowException {
        RuleBaseValues ruleDefinition = rule.getDefinition();
        RuleExpressionDef ruleExprDef = ruleDefinition.getRuleExpressionDef();
        String type = DEFAULT_RULE_EXPRESSION;
        if (ruleExprDef != null) {
            type = ruleExprDef.getType();
        }
        RuleExpression ruleExpression = loadRuleExpression(type);        
        return ruleExpression.evaluate(rule, context);
    }
}