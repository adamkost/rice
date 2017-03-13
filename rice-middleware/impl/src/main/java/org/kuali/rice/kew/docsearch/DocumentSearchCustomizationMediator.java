/**
 * Copyright 2005-2017 The Kuali Foundation
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
package org.kuali.rice.kew.docsearch;

import org.kuali.rice.core.api.uif.RemotableAttributeError;
import org.kuali.rice.kew.api.document.search.DocumentSearchCriteria;
import org.kuali.rice.kew.api.document.search.DocumentSearchResults;
import org.kuali.rice.kew.framework.document.search.DocumentSearchCriteriaConfiguration;
import org.kuali.rice.kew.framework.document.search.DocumentSearchResultSetConfiguration;
import org.kuali.rice.kew.framework.document.search.DocumentSearchResultValues;
import org.kuali.rice.kew.doctype.bo.DocumentType;

import java.util.List;

/**
 * Handles communication between {@link org.kuali.rice.kew.framework.document.search.DocumentSearchCustomizationHandlerService}
 * endpoints in order to invoke document search customizations which might be hosted from various applications.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public interface DocumentSearchCustomizationMediator {

    /**
     * Retrieves the document search criteria configuration for the given document type.  This should include attribute
     * fields that should be included in the document search user interface when displaying criteria by which a user
     * can search for documents.  If this method returns null then no additional criteria should be presented on the
     * search screen.
     *
     * @param documentType the document type for which to find document search criteria configuration, must not be null
     *
     * @return configuration information containing additional criteria (beyond the default set) which should be
     * displayed to the user when performing a search against documents of the given type, if null is returned this
     * indicates that the default document search criteria configuration should be used
     */
    DocumentSearchCriteriaConfiguration getDocumentSearchCriteriaConfiguration(DocumentType documentType);

    /**
     * Performs optional validation of document search criteria prior to execution of the search.
     *
     * @param documentType the document type against which the lookup is being performed
     * @param documentSearchCriteria the criteria representing the submission of the document search
     *
     * @return a list of error messages generated by the validation, may an empty list in which case the calling code
     * may safely ignore the response and assume that the given criteria validated successfully
     */
    List<RemotableAttributeError> validateLookupFieldParameters(DocumentType documentType,
            DocumentSearchCriteria documentSearchCriteria);

    /**
     * Optionally performs customization of the given document search criteria in the cases where the document type
     * implements criteria customization.  If this method returns a non-null value, then the calling code should use
     * the returned criteria in order to execute the search.  If this method returns a null value, it means the criteria
     * that was given did not require any customization.  In this case, the calling code should proceed with search
     * execution using the originally provided criteria.
     *
     * @param documentType the document type against which to perform the criteria customization, should never be null
     * @param documentSearchCriteria the criteria to use as the starting point for customization
     * 
     * @return a customized version of the given criteria, or null if the criteria was not customized
     */
    DocumentSearchCriteria customizeCriteria(DocumentType documentType, DocumentSearchCriteria documentSearchCriteria);

    /**
     * Optionally performs a custom clearing of the given document search criteria if the given document type
     * implements a customized clear algorithm.  If this method returns a non-null value, then the value returned
     * should be instated by the calling code as the "cleared" version of the criteria.  If null is returned, then the
     * default implementation of criteria clearing should be used.
     *
     * @param documentType the document type against which to check for a custom implementation of criteria clearing
     * @param documentSearchCriteria the current criteria of the document search prior to being cleared
     *
     * @return the result of clearing the criteria, if this returns null it means the given document type does not
     * implement custom clearing and the default behavior should be used
     */
    DocumentSearchCriteria customizeClearCriteria(DocumentType documentType,
            DocumentSearchCriteria documentSearchCriteria);

    /**
     * Optionally performs customization on the given set of document search results.  This could include changing
     * existing document values or synthesizing new ones.  The results of this method include a list of
     * {@link org.kuali.rice.kew.framework.document.search.DocumentSearchResultValue} objects, each of which are mapped to a
     * specific document id from the results and include additional key-value pairs for customized or synthesized
     * values for that document.  This method can return a null value if no customization was performed.
     *
     * @param documentType the document type to use when determining what customization logic (if any) should be invoked
     * @param documentSearchCriteria the criteria of the document search which produced the supplied results
     * @param results the results of the document search which are being considered for customization
     *
     * @return the customized result values, or null if not result customization was performed
     */
    DocumentSearchResultValues customizeResults(DocumentType documentType,
            DocumentSearchCriteria documentSearchCriteria,
            DocumentSearchResults results);

    /**
     * Optionally provides configuration information that allows for document search result set customization to occur.
     * The resulting {@code DocumentSearchResultSetConfiguration} can be used by the calling code to determine how best
     * to render the lookup results.
     *
     * @param documentType the document type for which to customize result set configuration
     * @param documentSearchCriteria the criteria that was used to perform the lookup
     *
     * @return the customized document search result set configuration, or null if no result set customization was
     * performed
     */
    DocumentSearchResultSetConfiguration customizeResultSetConfiguration(DocumentType documentType,
            DocumentSearchCriteria documentSearchCriteria);

}


