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
package org.kuali.rice.kew.docsearch;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.kuali.rice.kew.exception.KEWUserNotFoundException;
import org.kuali.rice.kew.exception.WorkflowServiceError;


/**
 * TODO delyea - documentation
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 */
public interface DocumentSearchGenerator {
	public static final int DEFAULT_SEARCH_RESULT_CAP = 500;

	public void setSearchableAttributes(List searchableAttributes);
	public void setSearchingUser(String principalId);
    public List<WorkflowServiceError> performPreSearchConditions(String principalId, DocSearchCriteriaDTO searchCriteria);
    public List<WorkflowServiceError> validateSearchableAttributes(DocSearchCriteriaDTO searchCriteria);
    public String generateSearchSql(DocSearchCriteriaDTO searchCriteria) throws KEWUserNotFoundException;
    /**
     * @deprecated Removed as of version 0.9.3.  Use {@link #processResultSet(Statement, ResultSet, DocSearchCriteriaDTO, WorkflowUser)} instead.
     */
    public List<DocSearchDTO> processResultSet(Statement searchAttributeStatement, ResultSet resultSet,DocSearchCriteriaDTO searchCriteria) throws KEWUserNotFoundException, SQLException;


    /**
     * This method processes search results in the given <code>resultSet</code> into {@link DocSearchDTO} objects
     *
     * @param searchAttributeStatement - statement to use when fetching search attributes
     * @param resultSet - resultSet containing data from document search main tables
     * @param searchCriteria - criteria used to perform the search
     * @param principalId - user who performed the search
     * @return a list of DocSearchDTO objects (one for each route header id)
     * @throws KEWUserNotFoundException
     * @throws SQLException
     */
    public List<DocSearchDTO> processResultSet(Statement searchAttributeStatement, ResultSet resultSet,DocSearchCriteriaDTO searchCriteria, String principalId) throws KEWUserNotFoundException, SQLException;
    public DocSearchCriteriaDTO clearSearch(DocSearchCriteriaDTO searchCriteria);

    public int getDocumentSearchResultSetLimit();
}
