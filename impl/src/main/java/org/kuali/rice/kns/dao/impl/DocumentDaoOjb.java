/*
 * Copyright 2005-2007 The Kuali Foundation.
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
package org.kuali.rice.kns.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.rice.kim.bo.Group;
import org.kuali.rice.kim.service.KIMServiceLocator;
import org.kuali.rice.kns.bo.AdHocRoutePerson;
import org.kuali.rice.kns.bo.AdHocRouteWorkgroup;
import org.kuali.rice.kns.bo.PersistableBusinessObject;
import org.kuali.rice.kns.dao.BusinessObjectDao;
import org.kuali.rice.kns.dao.DocumentDao;
import org.kuali.rice.kns.document.Document;
import org.kuali.rice.kns.service.KNSServiceLocator;
import org.kuali.rice.kns.util.KNSPropertyConstants;
import org.kuali.rice.kns.util.OjbCollectionAware;
import org.springframework.dao.DataAccessException;

/**
 * This class is the OJB implementation of the DocumentDao interface.
 */
public class DocumentDaoOjb extends PlatformAwareDaoBaseOjb implements DocumentDao, OjbCollectionAware{
    private static Logger LOG = Logger.getLogger(DocumentDaoOjb.class);
    private BusinessObjectDao businessObjectDao;


    public DocumentDaoOjb() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kuali.dao.DocumentDao#save(null)
     */
    public void save(Document document) throws DataAccessException {
        Document retrievedDocument = findByDocumentHeaderId(document.getClass(),document.getDocumentNumber());
        KNSServiceLocator.getOjbCollectionHelper().processCollections(this, document, retrievedDocument);
        this.getPersistenceBrokerTemplate().store(document);
    }

    /**
     * Retrieve a Document of a specific type with a given document header ID.
     *
     * @param clazz
     * @param id
     * @return Document with given id
     */
    public Document findByDocumentHeaderId(Class clazz, String id) throws DataAccessException {
        List idList = new ArrayList();
        idList.add(id);

        List documentList = findByDocumentHeaderIds(clazz, idList);

        Document document = null;
        if ((null != documentList) && (documentList.size() > 0)) {
            document = (Document) documentList.get(0);
        }

        return document;
    }

    /**
     * Retrieve a List of Document instances with the given ids
     *
     * @param clazz
     * @param idList
     * @return List
     */
    public List findByDocumentHeaderIds(Class clazz, List idList) throws DataAccessException {
        Criteria criteria = new Criteria();
        criteria.addIn(KNSPropertyConstants.DOCUMENT_NUMBER, idList);

        QueryByCriteria query = QueryFactory.newQuery(clazz, criteria);
        ArrayList <Document> tempList = new ArrayList(this.getPersistenceBrokerTemplate().getCollectionByQuery(query));
        for (Document doc : tempList) addAdHocs(doc);
        return tempList;
    }

    /**
     *
     * Deprecated method. Should use BusinessObjectService.linkAndSave() instead.
     *
     */
    @Deprecated
    public void saveMaintainableBusinessObject(PersistableBusinessObject businessObject) {
        /*
         * this call is to assure all the object fk values are in sync and the fk fields is set in the main object
         */
        KNSServiceLocator.getPersistenceService().linkObjects(businessObject);
        this.getPersistenceBrokerTemplate().store(businessObject);
    }


    public Document addAdHocs(Document document){
        /* Instead of reading the doc header to see if doc is in saved status
         * its probably easier and faster to just do this all the time and
         * store a null when appropriate.
         */
        List<AdHocRoutePerson> adHocRoutePersons;
        List<AdHocRouteWorkgroup> adHocRouteWorkgroups;
        HashMap criteriaPerson = new HashMap();
        HashMap criteriaWorkgroup = new HashMap();

        criteriaPerson.put("documentNumber", document.getDocumentNumber());
        criteriaPerson.put("type", AdHocRoutePerson.PERSON_TYPE);
        adHocRoutePersons = (List) businessObjectDao.findMatching(AdHocRoutePerson.class, criteriaPerson);
        criteriaWorkgroup.put("documentNumber", document.getDocumentNumber());
        criteriaWorkgroup.put("type", AdHocRouteWorkgroup.WORKGROUP_TYPE);
        adHocRouteWorkgroups = (List) businessObjectDao.findMatching(AdHocRouteWorkgroup.class, criteriaWorkgroup);

        //populate group namespace and names on adHocRoutWorkgroups
        for (AdHocRouteWorkgroup adHocRouteWorkgroup : adHocRouteWorkgroups) {
            Group group = KIMServiceLocator.getIdentityManagementService().getGroup(adHocRouteWorkgroup.getId());
            adHocRouteWorkgroup.setRecipientName(group.getGroupName());
            adHocRouteWorkgroup.setRecipientNamespaceCode(group.getNamespaceCode());
        }
        document.setAdHocRoutePersons(adHocRoutePersons);
        document.setAdHocRouteWorkgroups(adHocRouteWorkgroups);

        return document;
    }

    /**
     * Returns the {@link BusinessObjectDao}, lazily initializing if necessary
     * @see org.kuali.rice.kns.dao.DocumentDao#getBusinessObjectDao()
     * @return the {@link BusinessObjectDao}
     */
    public synchronized BusinessObjectDao getBusinessObjectDao() {
        if (this.businessObjectDao == null) {
            this.businessObjectDao = KNSServiceLocator.getBusinessObjectDao();
        }
        return businessObjectDao;
    }

    /**
     * Sets the {@link BusinessObjectDao}
     * @param businessObjectDao ths {@link BusinessObjectDao}
     */
    public synchronized void setBusinessObjectDao(BusinessObjectDao businessObjectDao) {
        this.businessObjectDao = businessObjectDao;
    }

}