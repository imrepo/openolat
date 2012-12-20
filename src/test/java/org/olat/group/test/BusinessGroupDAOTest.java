/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.group.test;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupOrder;
import org.olat.group.BusinessGroupShort;
import org.olat.group.BusinessGroupView;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.group.manager.BusinessGroupPropertyDAO;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.group.model.BusinessGroupMembershipViewImpl;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.properties.Property;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.model.Offer;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupDAOTest extends OlatTestCase {
	
	private OLog log = Tracing.createLoggerFor(BusinessGroupDAOTest.class);
	
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BusinessGroupPropertyDAO businessGroupPropertyManager;
	@Autowired
	private ACService acService;
	@Autowired
	private MarkManager markManager;
	
	@After
	public void tearDown() throws Exception {
		try {
			DBFactory.getInstance().commitAndCloseSession();
		} catch (Exception e) {
			log.error("Exception in tearDown(): " + e);
			throw e;
		}
	}
	
	@Test
	public void should_service_present() {
		Assert.assertNotNull(businessGroupDao);
	}
	
	@Test
	public void createBusinessGroup() {
		BusinessGroup group = businessGroupDao.createAndPersist(null, "gdao", "gdao-desc", -1, -1, false, false, false, false, false);
		dbInstance.commit();

		Assert.assertNotNull(group);
		Assert.assertNull(group.getMinParticipants());
		Assert.assertNull(group.getMaxParticipants());
		Assert.assertNotNull(group.getLastUsage());
		Assert.assertNotNull(group.getCreationDate());
		Assert.assertNotNull(group.getLastModified());
		Assert.assertNotNull(group.getOwnerGroup());
		Assert.assertNotNull(group.getPartipiciantGroup());
		Assert.assertNotNull(group.getWaitingGroup());
		Assert.assertNotNull(group.getResource());
		Assert.assertEquals("gdao", group.getName());
		Assert.assertEquals("gdao-desc", group.getDescription());
		Assert.assertFalse(group.getWaitingListEnabled());
		Assert.assertFalse(group.getAutoCloseRanksEnabled());
	}
	
	@Test
	public void loadBusinessGroupStandard() {
		BusinessGroup group = businessGroupDao.createAndPersist(null, "gdbo", "gdbo-desc", -1, -1, false, false, false, false, false);
		dbInstance.commitAndCloseSession();
		
		BusinessGroup reloadedGroup = businessGroupDao.load(group.getKey());
		
		Assert.assertNotNull(reloadedGroup);
		Assert.assertNull(reloadedGroup.getMinParticipants());
		Assert.assertNull(reloadedGroup.getMaxParticipants());
		Assert.assertNotNull(reloadedGroup.getLastUsage());
		Assert.assertNotNull(reloadedGroup.getCreationDate());
		Assert.assertNotNull(reloadedGroup.getLastModified());
		Assert.assertNotNull(reloadedGroup.getOwnerGroup());
		Assert.assertNotNull(reloadedGroup.getPartipiciantGroup());
		Assert.assertNotNull(reloadedGroup.getWaitingGroup());
		Assert.assertNotNull(group.getResource());
		Assert.assertEquals("gdbo", reloadedGroup.getName());
		Assert.assertEquals("gdbo-desc", reloadedGroup.getDescription());
		Assert.assertFalse(reloadedGroup.getWaitingListEnabled());
		Assert.assertFalse(reloadedGroup.getAutoCloseRanksEnabled());
	}
	
	@Test
	public void loadBusinessGroup() {
		//create business group
		BusinessGroup group = businessGroupDao.createAndPersist(null, "gdco", "gdco-desc", 0, 10, true, true, false, false, false);
		dbInstance.commitAndCloseSession();
		
		BusinessGroup reloadedGroup = businessGroupDao.load(group.getKey());
		//check the saved values
		Assert.assertNotNull(reloadedGroup);
		Assert.assertNotNull(reloadedGroup.getMinParticipants());
		Assert.assertNotNull(reloadedGroup.getMaxParticipants());
		Assert.assertEquals(0, reloadedGroup.getMinParticipants().intValue());
		Assert.assertEquals(10, reloadedGroup.getMaxParticipants().intValue());
		Assert.assertNotNull(reloadedGroup.getLastUsage());
		Assert.assertNotNull(reloadedGroup.getCreationDate());
		Assert.assertNotNull(reloadedGroup.getLastModified());
		Assert.assertNotNull(reloadedGroup.getOwnerGroup());
		Assert.assertNotNull(reloadedGroup.getPartipiciantGroup());
		Assert.assertNotNull(reloadedGroup.getWaitingGroup());
		Assert.assertEquals("gdco", reloadedGroup.getName());
		Assert.assertEquals("gdco-desc", reloadedGroup.getDescription());
		Assert.assertTrue(reloadedGroup.getWaitingListEnabled());
		Assert.assertTrue(reloadedGroup.getAutoCloseRanksEnabled());
	}
	
	@Test
	public void loadBusinessGroupWithOwner() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("bdao-1-" + UUID.randomUUID().toString());
		dbInstance.commitAndCloseSession();
		
		BusinessGroup group = businessGroupDao.createAndPersist(owner, "gddo", "gddo-desc", 0, 10, true, true, false, false, false);
		dbInstance.commitAndCloseSession();
		
		BusinessGroup reloadedGroup = businessGroupDao.load(group.getKey());
		//check if the owner is in the owner security group
		Assert.assertNotNull(reloadedGroup);
		Assert.assertNotNull(reloadedGroup.getOwnerGroup());
		boolean isOwner = securityManager.isIdentityInSecurityGroup(owner, reloadedGroup.getOwnerGroup());
		Assert.assertTrue(isOwner);
	}
	
	@Test
	public void loadBusinessGroupsByIds() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("bdao-2-" + UUID.randomUUID().toString());
		BusinessGroup group1 = businessGroupDao.createAndPersist(owner, "gdeo", "gdeo-desc", 0, 10, true, true, false, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(owner, "gdfo", "gdfo-desc", 0, 10, true, true, false, false, false);
		dbInstance.commitAndCloseSession();
		
		//check if the method is robust against empty list fo keys
		List<BusinessGroup> groups1 = businessGroupDao.load(Collections.<Long>emptyList());
		Assert.assertNotNull(groups1);
		Assert.assertEquals(0, groups1.size());
		
		//check load 1 group
		List<BusinessGroup> groups2 = businessGroupDao.load(Collections.singletonList(group1.getKey()));
		Assert.assertNotNull(groups2);
		Assert.assertEquals(1, groups2.size());
		Assert.assertEquals(group1, groups2.get(0));
		
		//check load 2 groups
		List<Long> groupKeys = new ArrayList<Long>(2);
		groupKeys.add(group1.getKey());
		groupKeys.add(group2.getKey());
		List<BusinessGroup> groups3 = businessGroupDao.load(groupKeys);
		Assert.assertNotNull(groups3);
		Assert.assertEquals(2, groups3.size());
		Assert.assertTrue(groups3.contains(group1));
		Assert.assertTrue(groups3.contains(group2));
	}
	
	@Test
	public void loadShortBusinessGroupsByKeys() {
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "shorty-1", "shorty-1-desc", 0, 10, true, true, false, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "shorty-2", "shorty-2-desc", 0, 10, true, true, false, false, false);
		dbInstance.commitAndCloseSession();
		
		//check if the method is robust against empty list fo keys
		List<BusinessGroupShort> groups1 = businessGroupDao.loadShort(Collections.<Long>emptyList());
		Assert.assertNotNull(groups1);
		Assert.assertEquals(0, groups1.size());
		
		//check load 1 group
		List<BusinessGroupShort> groups2 = businessGroupDao.loadShort(Collections.singletonList(group1.getKey()));
		Assert.assertNotNull(groups2);
		Assert.assertEquals(1, groups2.size());
		Assert.assertEquals(group1.getKey(), groups2.get(0).getKey());
		Assert.assertEquals(group1.getName(), groups2.get(0).getName());
		
		//check load 2 groups
		List<Long> groupKeys = new ArrayList<Long>(2);
		groupKeys.add(group1.getKey());
		groupKeys.add(group2.getKey());
		List<BusinessGroupShort> groups3 = businessGroupDao.loadShort(groupKeys);
		Assert.assertNotNull(groups3);
		Assert.assertEquals(2, groups3.size());
		List<Long> groupShortKeys3 = new ArrayList<Long>(3);
		for(BusinessGroupShort group:groups3) {
			groupShortKeys3.add(group.getKey());
		}
		Assert.assertTrue(groupShortKeys3.contains(group1.getKey()));
		Assert.assertTrue(groupShortKeys3.contains(group2.getKey()));
	}
	
	@Test
	public void loadAllBusinessGroups() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("bdao-3-" + UUID.randomUUID().toString());
		BusinessGroup group1 = businessGroupDao.createAndPersist(owner, "gdgo", "gdgo-desc", 0, 10, true, true, false, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(owner, "gdho", "gdho-desc", 0, 10, true, true, false, false, false);
		dbInstance.commitAndCloseSession();
		
		//load all business groups
		List<BusinessGroup> allGroups = businessGroupDao.loadAll();
		Assert.assertNotNull(allGroups);
		Assert.assertTrue(allGroups.size() >= 2);
		Assert.assertTrue(allGroups.contains(group1));
		Assert.assertTrue(allGroups.contains(group2));
	}

	
	@Test
	public void mergeBusinessGroup() {
		//create a business group
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("bdao-3-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupDao.createAndPersist(owner, "gdho", "gdho-desc", 0, 10, true, true, false, false, false);
		dbInstance.commitAndCloseSession();
		
		//delete a business group
		group.setAutoCloseRanksEnabled(false);
		group.setName("gdho-2");

		//merge business group
		BusinessGroup mergedGroup = businessGroupDao.merge(group);
		Assert.assertNotNull(mergedGroup);
		Assert.assertEquals(group, mergedGroup);
		Assert.assertEquals("gdho-2", mergedGroup.getName());
		Assert.assertEquals(Boolean.FALSE, mergedGroup.getAutoCloseRanksEnabled());
		
		dbInstance.commitAndCloseSession();
		
		//reload the merged group and check values
		BusinessGroup reloadedGroup = businessGroupDao.load(group.getKey());
		Assert.assertNotNull(reloadedGroup);
		Assert.assertEquals(group, reloadedGroup);
		Assert.assertEquals("gdho-2", reloadedGroup.getName());
		Assert.assertEquals(Boolean.FALSE, reloadedGroup.getAutoCloseRanksEnabled());
	}
	
	@Test
	public void updateBusinessGroup() {
		//create a business group
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("bdao-4-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupDao.createAndPersist(owner, "gdio", "gdio-desc", 1, 10, true, true, false, false, false);
		dbInstance.commitAndCloseSession();
		
		//delete a business group
		group.setWaitingListEnabled(false);
		group.setDescription("gdio-2-desc");

		//update business group (semantic of Hibernate before JPA)
		BusinessGroup updatedGroup = businessGroupDao.update(group);
		Assert.assertNotNull(updatedGroup);
		Assert.assertEquals(group, updatedGroup);
		Assert.assertEquals("gdio-2-desc", updatedGroup.getDescription());
		Assert.assertEquals(Boolean.FALSE, updatedGroup.getWaitingListEnabled());
		Assert.assertTrue(updatedGroup == group);
		
		dbInstance.commitAndCloseSession();
		
		//reload the merged group and check values
		BusinessGroup reloadedGroup = businessGroupDao.load(group.getKey());
		Assert.assertNotNull(reloadedGroup);
		Assert.assertEquals(group, reloadedGroup);
		Assert.assertEquals("gdio-2-desc", reloadedGroup.getDescription());
		Assert.assertEquals(Boolean.FALSE, reloadedGroup.getWaitingListEnabled());
	}
	
	@Test
	public void findBusinessGroupBySecurityGroup() {
		//create 2 groups
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "gdjo", "gdjo-desc", -1, -1, false, false, false, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "gdko", "gdko-desc", -1, -1, false, false, false, false, false);
		dbInstance.commitAndCloseSession();

		//check find by owner group
		BusinessGroup byOwnerGroup = businessGroupDao.findBusinessGroup(group1.getOwnerGroup());
		Assert.assertNotNull(byOwnerGroup);
		Assert.assertEquals(group1, byOwnerGroup);
		Assert.assertNotSame(group2, byOwnerGroup);

		//check find by participant group
		BusinessGroup byParticipantGroup = businessGroupDao.findBusinessGroup(group1.getPartipiciantGroup());
		Assert.assertNotNull(byParticipantGroup);
		Assert.assertEquals(group1, byParticipantGroup);
		
		//check find by waiting group
		BusinessGroup byWaitingGroup = businessGroupDao.findBusinessGroup(group2.getWaitingGroup());
		Assert.assertNotNull(byWaitingGroup);
		Assert.assertEquals(group2, byWaitingGroup);
		Assert.assertNotSame(group1, byWaitingGroup);
	}
	
	@Test
	public void findBusinessGroupsWithWaitingListAttendedBy() {
		//3 identities
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("bdao-5-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("bdao-6-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("bdao-7-" + UUID.randomUUID().toString());

		//create 3 groups
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "gdlo", "gdlo-desc", 0, 5, true, false, false, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "gdmo", "gdmo-desc", 0, 5, true, false, false, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "gdno", "gdno-desc", 0, 5, true, false, false, false, false);
		dbInstance.commitAndCloseSession();
		
		//id1 -> group 1 and 2
		securityManager.addIdentityToSecurityGroup(id1, group1.getWaitingGroup());
		securityManager.addIdentityToSecurityGroup(id1, group2.getWaitingGroup());
		//id2 -> group 1 and 3
		securityManager.addIdentityToSecurityGroup(id2, group1.getWaitingGroup());
		securityManager.addIdentityToSecurityGroup(id2, group3.getWaitingGroup());

		//check:
		//id1: group 1 and 2
		List<BusinessGroup> groupOfId1 = businessGroupDao.findBusinessGroupsWithWaitingListAttendedBy(id1,  null);
		Assert.assertNotNull(groupOfId1);
		Assert.assertTrue(groupOfId1.contains(group1));
		Assert.assertTrue(groupOfId1.contains(group2));
		//id2 -> group 1 and 3
		List<BusinessGroup> groupOfId2 = businessGroupDao.findBusinessGroupsWithWaitingListAttendedBy(id2,  null);
		Assert.assertNotNull(groupOfId2);
		Assert.assertTrue(groupOfId2.contains(group1));
		Assert.assertTrue(groupOfId2.contains(group3));

		List<BusinessGroup> groupOfId3 = businessGroupDao.findBusinessGroupsWithWaitingListAttendedBy(id3,  null);
		Assert.assertNotNull(groupOfId3);
		Assert.assertTrue(groupOfId3.isEmpty());
	}
	
	@Test
	public void findBusinessGroupWithAuthorConnection() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsUser("bdao-5-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		securityManager.addIdentityToSecurityGroup(author, re.getOwnerGroup());
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "gdlo", "gdlo-desc", 0, 5, true, false, false, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(author, "gdmo", "gdmo-desc", 0, 5, true, false, false, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(author, "gdmo", "gdmo-desc", 0, 5, true, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group1, re.getOlatResource());
		businessGroupRelationDao.addRelationToResource(group3, re.getOlatResource());
		dbInstance.commitAndCloseSession();
		
		//check 
		List<BusinessGroupView> groups = businessGroupDao.findBusinessGroupWithAuthorConnection(author);
		Assert.assertNotNull(groups);
		Assert.assertEquals(2, groups.size());
		
		Set<Long> retrievedGroupkey = new HashSet<Long>();
		for(BusinessGroupView view:groups) {
			retrievedGroupkey.add(view.getKey());
		}
		Assert.assertTrue(retrievedGroupkey.contains(group1.getKey()));
		Assert.assertTrue(retrievedGroupkey.contains(group3.getKey()));
		Assert.assertFalse(retrievedGroupkey.contains(group2.getKey()));
	}
	
	@Test
	public void testVisibilityOfSecurityGroups() {
		//create 3 groups
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "gdro", "gdro-desc", 0, 5, true, false, true, true, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "gdso", "gdso-desc", 0, 5, true, false, false, true, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "gdto", "gdto-desc", 0, 5, true, false, false, false, true);
		dbInstance.commitAndCloseSession();
		
		//check the value
		Property prop1 = businessGroupPropertyManager.findProperty(group1);
		Assert.assertTrue(businessGroupPropertyManager.showOwners(prop1));
		Assert.assertTrue(businessGroupPropertyManager.showPartips(prop1));
		Assert.assertFalse(businessGroupPropertyManager.showWaitingList(prop1));
		
		Property prop2 = businessGroupPropertyManager.findProperty(group2);
		Assert.assertFalse(businessGroupPropertyManager.showOwners(prop2));
		Assert.assertTrue(businessGroupPropertyManager.showPartips(prop2));
		Assert.assertFalse(businessGroupPropertyManager.showWaitingList(prop2));
		
		Property prop3 = businessGroupPropertyManager.findProperty(group3);
		Assert.assertFalse(businessGroupPropertyManager.showOwners(prop3));
		Assert.assertFalse(businessGroupPropertyManager.showPartips(prop3));
		Assert.assertTrue(businessGroupPropertyManager.showWaitingList(prop3));
	}
	
	@Test
	public void findBusinessGroups() {
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "gduo", "gduo-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "gdvo", "gdvo-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		SearchBusinessGroupParams params = new SearchBusinessGroupParams(); 
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(params, null, 0, -1);
		Assert.assertNotNull(groups);
		Assert.assertTrue(groups.size() >= 2);
		Assert.assertTrue(groups.contains(group1));
		Assert.assertTrue(groups.contains(group2));

		List<BusinessGroupView> groupViews = businessGroupDao.findBusinessGroupViews(params, null, 0, -1);
		Assert.assertNotNull(groupViews);
		Assert.assertTrue(groupViews.size() >= 2);
		Assert.assertTrue(contains(groupViews, group1));
		Assert.assertTrue(contains(groupViews, group2));

		dbInstance.commit();

		List<BusinessGroup> groupLimit = businessGroupDao.findBusinessGroups(params, null, 0, 1);
		Assert.assertNotNull(groupLimit);
		Assert.assertEquals(1, groupLimit.size());
		
		List<BusinessGroupView> groupViewLimit = businessGroupDao.findBusinessGroupViews(params, null, 0, 1);
		Assert.assertNotNull(groupViewLimit);
		Assert.assertEquals(1, groupViewLimit.size());
	}
	
	@Test
	public void findBusinessGroupsByExactName() {
		String exactName = UUID.randomUUID().toString();
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, exactName, "gdwo-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, exactName + "x", "gdxo-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "y" +exactName, "gdyo-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setExactName(exactName);
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(params, null, 0, -1);
		Assert.assertNotNull(groups);
		Assert.assertEquals(1, groups.size());
		Assert.assertTrue(groups.contains(group1));
		Assert.assertFalse(groups.contains(group2));
		Assert.assertFalse(groups.contains(group3));
		
		//check find with views
		List<BusinessGroupView> groupViews = businessGroupDao.findBusinessGroupViews(params, null, 0, -1);
		Assert.assertNotNull(groupViews);
		Assert.assertEquals(1, groupViews.size());
		Assert.assertTrue(contains(groupViews, group1));
		Assert.assertFalse(contains(groupViews, group2));
		Assert.assertFalse(contains(groupViews, group3));
	}
	
	@Test
	public void findBusinessGroupsByName() {
		String name = UUID.randomUUID().toString();
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, name.toUpperCase(), "fingbg-1-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, name + "xxx", "fingbg-2-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "yyy" + name.toUpperCase(), "fingbg-3-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setName(name);
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(params, null, 0, -1);
		Assert.assertNotNull(groups);
		Assert.assertEquals(2, groups.size() );
		Assert.assertTrue(groups.contains(group1));
		Assert.assertTrue(groups.contains(group2));
		Assert.assertFalse(groups.contains(group3));
		
		//check the same with the views
		List<BusinessGroupView> groupViews = businessGroupDao.findBusinessGroupViews(params, null, 0, -1);
		Assert.assertNotNull(groupViews);
		Assert.assertEquals(2, groupViews.size() );
		Assert.assertTrue(contains(groupViews, group1));
		Assert.assertTrue(contains(groupViews, group2));
		Assert.assertFalse(contains(groupViews, group3));
	}
	
	@Test
	public void findBusinessGroupsByNameFuzzy() {
		String name = UUID.randomUUID().toString();
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, name.toUpperCase(), "fingbg-1-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, name + "xxx", "fingbg-2-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "yyy" + name.toUpperCase(), "fingbg-3-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setName("*" + name + "*");
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(params, null, 0, -1);
		Assert.assertNotNull(groups);
		Assert.assertEquals(3, groups.size() );
		Assert.assertTrue(groups.contains(group1));
		Assert.assertTrue(groups.contains(group2));
		Assert.assertTrue(groups.contains(group3));
		
		//check the same with the views
		List<BusinessGroupView> groupViews = businessGroupDao.findBusinessGroupViews(params, null, 0, -1);
		Assert.assertNotNull(groupViews);
		Assert.assertEquals(3, groupViews.size() );
		Assert.assertTrue(contains(groupViews, group1));
		Assert.assertTrue(contains(groupViews, group2));
		Assert.assertTrue(contains(groupViews, group3));
	}
	
	@Test
	public void findBusinessGroupsByDescription() {
		String name = UUID.randomUUID().toString();
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "fingbg-1", name.toUpperCase() + "-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "fingbg-2", "desc-" + name, 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "fingbg-3", "desc-" + name + "-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		//check find business group
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setDescription(name);
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(params, null, 0, -1);
		Assert.assertNotNull(groups);
		Assert.assertEquals(1, groups.size() );
		Assert.assertTrue(groups.contains(group1));
		Assert.assertFalse(groups.contains(group2));
		Assert.assertFalse(groups.contains(group3));
		
		//check find business group
		List<BusinessGroupView> groupViews = businessGroupDao.findBusinessGroupViews(params, null, 0, -1);
		Assert.assertNotNull(groupViews);
		Assert.assertEquals(1, groupViews.size() );
		Assert.assertTrue(contains(groupViews, group1));
		Assert.assertFalse(contains(groupViews, group2));
		Assert.assertFalse(contains(groupViews, group3));
	}
	
	@Test
	public void findBusinessGroupsByDescriptionFuzzy() {
		String name = UUID.randomUUID().toString();
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "fingbg-1", name + "-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "fingbg-2", "desc-" + name.toUpperCase(), 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "fingbg-3", "desc-" + name + "-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setDescription("*" + name + "*");
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(params, null, 0, -1);
		Assert.assertNotNull(groups);
		Assert.assertEquals(3, groups.size() );
		Assert.assertTrue(groups.contains(group1));
		Assert.assertTrue(groups.contains(group2));
		Assert.assertTrue(groups.contains(group3));
		
		//check same search with the views
		List<BusinessGroupView> groupViews = businessGroupDao.findBusinessGroupViews(params, null, 0, -1);
		Assert.assertNotNull(groupViews);
		Assert.assertEquals(3, groupViews.size() );
		Assert.assertTrue(contains(groupViews, group1));
		Assert.assertTrue(contains(groupViews, group2));
		Assert.assertTrue(contains(groupViews, group3));
	}
	
	@Test
	public void findBusinessGroupsByNameOrDesc() {
		String name = UUID.randomUUID().toString();
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "fingbg-1", name.toUpperCase() + "-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "fingbg-2", "fingbg-2-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, name.toUpperCase() + "-xxx", "desc-fingb-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setNameOrDesc(name);
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(params, null, 0, -1);
		Assert.assertNotNull(groups);
		Assert.assertEquals(2, groups.size() );
		Assert.assertTrue(groups.contains(group1));
		Assert.assertFalse(groups.contains(group2));
		Assert.assertTrue(groups.contains(group3));
		
		//check the same search with the views
		List<BusinessGroupView> groupViews = businessGroupDao.findBusinessGroupViews(params, null, 0, -1);
		Assert.assertNotNull(groupViews);
		Assert.assertEquals(2, groupViews.size() );
		Assert.assertTrue(contains(groupViews, group1));
		Assert.assertFalse(contains(groupViews, group2));
		Assert.assertTrue(contains(groupViews, group3));
	}
	
	@Test
	public void findBusinessGroupsByNameOrDescFuzzy() {
		String name = UUID.randomUUID().toString();
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "fingbg-1", name + "-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "fingbg-2", "desc-" + name.toUpperCase(), 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "fingbg-3", "desc-" + name + "-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setNameOrDesc("*" + name + "*");
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(params, null, 0, -1);
		Assert.assertNotNull(groups);
		Assert.assertEquals(3, groups.size() );
		Assert.assertTrue(groups.contains(group1));
		Assert.assertTrue(groups.contains(group2));
		Assert.assertTrue(groups.contains(group3));
		
		//check the same search with the views
		List<BusinessGroupView> groupViews = businessGroupDao.findBusinessGroupViews(params, null, 0, -1);
		Assert.assertNotNull(groupViews);
		Assert.assertEquals(3, groupViews.size() );
		Assert.assertTrue(contains(groupViews, group1));
		Assert.assertTrue(contains(groupViews, group2));
		Assert.assertTrue(contains(groupViews, group3));
	}
	
	@Test
	public void findBusinessGroupsByOwner() {
		//5 identities
		String name = UUID.randomUUID().toString();
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser(name);
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("ddao-2-" + name);
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser(name + "-ddao-3");

		BusinessGroup group1 = businessGroupDao.createAndPersist(id1, "fingbgown-1", "fingbgown-1-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(id2, "fingbgown-2", "fingbgown-2-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(id3, "fingbgown-3", "fingbgown-3-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setOwnerName(name);
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(params, null, 0, -1);
		Assert.assertNotNull(groups);
		Assert.assertEquals(2, groups.size() );
		Assert.assertTrue(groups.contains(group1));
		Assert.assertFalse(groups.contains(group2));
		Assert.assertTrue(groups.contains(group3));
		
		//check the same with the views
		List<BusinessGroupView> groupViews = businessGroupDao.findBusinessGroupViews(params, null, 0, -1);
		Assert.assertNotNull(groupViews);
		Assert.assertEquals(2, groupViews.size() );
		Assert.assertTrue(contains(groupViews, group1));
		Assert.assertFalse(contains(groupViews, group2));
		Assert.assertTrue(contains(groupViews, group3));
	}
	
	@Test
	public void findBusinessGroupsByOwnerFuzzy() {
		String name = UUID.randomUUID().toString();
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser(name);
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("ddao-2-" + name.toUpperCase());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser(name + "-ddao-3-");
		
		BusinessGroup group1 = businessGroupDao.createAndPersist(id1, "fingbg-own-1-1", "fingbg-own-1-1-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(id2, "fingbg-own-1-2", "fingbg-own-1-2-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(id3, "fingbg-own-1-3", "fingbg-own-1-3-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setOwnerName("*" + name + "*");
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(params, null, 0, -1);
		Assert.assertNotNull(groups);
		Assert.assertEquals(3, groups.size() );
		Assert.assertTrue(groups.contains(group1));
		Assert.assertTrue(groups.contains(group2));
		Assert.assertTrue(groups.contains(group3));
		
		//check the same with the views
		List<BusinessGroupView> groupViews = businessGroupDao.findBusinessGroupViews(params, null, 0, -1);
		Assert.assertNotNull(groupViews);
		Assert.assertEquals(3, groupViews.size() );
		Assert.assertTrue(contains(groupViews, group1));
		Assert.assertTrue(contains(groupViews, group2));
		Assert.assertTrue(contains(groupViews, group3));
	}
	
	@Test
	public void findBusinessGroupsByIdentity() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("is-in-grp-" + UUID.randomUUID().toString());
		BusinessGroup group1 = businessGroupDao.createAndPersist(id, "is-in-grp-1", "is-in-grp-1-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "is-in-grp-2", "is-in-grp-2-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "is-in-grp-3", "is-in-grp-3-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		securityManager.addIdentityToSecurityGroup(id, group2.getPartipiciantGroup());
		securityManager.addIdentityToSecurityGroup(id, group3.getWaitingGroup());
		dbInstance.commitAndCloseSession();

		//check owner
		SearchBusinessGroupParams paramsOwner = new SearchBusinessGroupParams();
		paramsOwner.setIdentity(id);
		paramsOwner.setOwner(true);
		List<BusinessGroup> ownedGroups = businessGroupDao.findBusinessGroups(paramsOwner, null, 0, 0);
		Assert.assertNotNull(ownedGroups);
		Assert.assertEquals(1, ownedGroups.size());
		Assert.assertTrue(ownedGroups.contains(group1));
		
		//check attendee
		SearchBusinessGroupParams paramsAttendee = new SearchBusinessGroupParams();
		paramsAttendee.setIdentity(id);
		paramsAttendee.setAttendee(true);
		List<BusinessGroup> attendeeGroups = businessGroupDao.findBusinessGroups(paramsAttendee, null, 0, 0);
		Assert.assertNotNull(attendeeGroups);
		Assert.assertEquals(1, attendeeGroups.size());
		Assert.assertTrue(attendeeGroups.contains(group2));

		//check waiting
		SearchBusinessGroupParams paramsWaiting = new SearchBusinessGroupParams();
		paramsWaiting.setIdentity(id);
		paramsWaiting.setWaiting(true);
		List<BusinessGroup> waitingGroups = businessGroupDao.findBusinessGroups(paramsWaiting, null, 0, 0);
		Assert.assertNotNull(waitingGroups);
		Assert.assertEquals(1, waitingGroups.size());
		Assert.assertTrue(waitingGroups.contains(group3));
		
		//check all
		SearchBusinessGroupParams paramsAll = new SearchBusinessGroupParams();
		paramsAll.setIdentity(id);
		paramsAll.setOwner(true);
		paramsAll.setAttendee(true);
		paramsAll.setWaiting(true);
		List<BusinessGroup> allGroups = businessGroupDao.findBusinessGroups(paramsAll, null, 0, 0);
		Assert.assertNotNull(allGroups);
		Assert.assertEquals(3, allGroups.size());
		Assert.assertTrue(allGroups.contains(group1));
		Assert.assertTrue(allGroups.contains(group2));
		Assert.assertTrue(allGroups.contains(group3));
		
		//The same tests with the views
		//check owner on views
		List<BusinessGroupView> ownedGroupViews = businessGroupDao.findBusinessGroupViews(paramsOwner, null, 0, 0);
		Assert.assertNotNull(ownedGroupViews);
		Assert.assertEquals(1, ownedGroupViews.size());
		Assert.assertTrue(contains(ownedGroupViews, group1));
		
		//check attendee on views
		List<BusinessGroupView> attendeeGroupViews = businessGroupDao.findBusinessGroupViews(paramsAttendee, null, 0, 0);
		Assert.assertNotNull(attendeeGroupViews);
		Assert.assertEquals(1, attendeeGroupViews.size());
		Assert.assertTrue(contains(attendeeGroupViews, group2));

		//check waiting on views
		List<BusinessGroupView> waitingGroupViews = businessGroupDao.findBusinessGroupViews(paramsWaiting, null, 0, 0);
		Assert.assertNotNull(waitingGroupViews);
		Assert.assertEquals(1, waitingGroupViews.size());
		Assert.assertTrue(contains(waitingGroupViews, group3));
		
		//check all on views
		List<BusinessGroupView> allGroupViews = businessGroupDao.findBusinessGroupViews(paramsAll, null, 0, 0);
		Assert.assertNotNull(allGroupViews);
		Assert.assertEquals(3, allGroupViews.size());
		Assert.assertTrue(contains(allGroupViews, group1));
		Assert.assertTrue(contains(allGroupViews, group2));
		Assert.assertTrue(contains(allGroupViews, group3));
	}
	
	@Test
	public void findPublicGroups() {
		//create a group with an access control
		BusinessGroup group = businessGroupDao.createAndPersist(null, "access-grp-1", "access-grp-1-desc", 0, 5, true, false, true, false, false);
		//create and save an offer
		Offer offer = acService.createOffer(group.getResource(), "TestBGWorkflow");
		assertNotNull(offer);
		acService.save(offer);
			
		dbInstance.commitAndCloseSession();
			
		//retrieve the offer
		SearchBusinessGroupParams paramsAll = new SearchBusinessGroupParams();
		paramsAll.setPublicGroups(Boolean.TRUE);
		List<BusinessGroup> accessGroups = businessGroupDao.findBusinessGroups(paramsAll, null, 0, 0);
		Assert.assertNotNull(accessGroups);
		Assert.assertTrue(accessGroups.size() >= 1);
		Assert.assertTrue(accessGroups.contains(group));
		
		for(BusinessGroup accessGroup:accessGroups) {
			List<Offer> offers = acService.findOfferByResource(accessGroup.getResource(), true, new Date());
			Assert.assertNotNull(offers);
			Assert.assertFalse(offers.isEmpty());
		}
		
		//check the search with the views
		List<BusinessGroupView> accessGroupViews = businessGroupDao.findBusinessGroupViews(paramsAll, null, 0, 0);
		Assert.assertNotNull(accessGroupViews);
		Assert.assertTrue(accessGroupViews.size() >= 1);
		Assert.assertTrue(contains(accessGroupViews, group));
		
		for(BusinessGroupView accessGroup:accessGroupViews) {
			List<Offer> offers = acService.findOfferByResource(accessGroup.getResource(), true, new Date());
			Assert.assertNotNull(offers);
			Assert.assertFalse(offers.isEmpty());
		}
	}
	
	@Test
	public void findPublicGroupsLimitedDate() {
		//create a group with an access control limited by a valid date
		BusinessGroup groupVisible = businessGroupDao.createAndPersist(null, "access-grp-2", "access-grp-2-desc", 0, 5, true, false, true, false, false);
		//create and save an offer
		Offer offer = acService.createOffer(groupVisible.getResource(), "TestBGWorkflow");

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, -1);
		offer.setValidFrom(cal.getTime());
		cal.add(Calendar.HOUR_OF_DAY, 2);
		offer.setValidTo(cal.getTime());
		assertNotNull(offer);
		acService.save(offer);

		//create a group with an access control limited by dates in the past
		BusinessGroup oldGroup = businessGroupDao.createAndPersist(null, "access-grp-3", "access-grp-3-desc", 0, 5, true, false, true, false, false);
		//create and save an offer
		Offer oldOffer = acService.createOffer(oldGroup.getResource(), "TestBGWorkflow");
		cal.add(Calendar.HOUR_OF_DAY, -5);
		oldOffer.setValidFrom(cal.getTime());
		cal.add(Calendar.HOUR_OF_DAY, -5);
		oldOffer.setValidTo(cal.getTime());
		assertNotNull(oldOffer);
		acService.save(oldOffer);

		dbInstance.commitAndCloseSession();
			
		//retrieve the offer
		SearchBusinessGroupParams paramsAll = new SearchBusinessGroupParams();
		paramsAll.setPublicGroups(Boolean.TRUE);
		List<BusinessGroup> accessGroups = businessGroupDao.findBusinessGroups(paramsAll, null, 0, 0);
		Assert.assertNotNull(accessGroups);
		Assert.assertTrue(accessGroups.size() >= 1);
		Assert.assertTrue(accessGroups.contains(groupVisible));
		Assert.assertFalse(accessGroups.contains(oldGroup));
	}	
	
	@Test
	public void findBusinessGroupsWithResources() {
		//create a group attach to a resource
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("marker-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group1 = businessGroupDao.createAndPersist(owner, "rsrc-grp-1", "rsrc-grp-1-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(owner, "rsrc-grp-2", "rsrc-grp-2-desc", 0, 5, true, false, true, false, false);
		businessGroupRelationDao.addRelationToResource(group1, re.getOlatResource());
		dbInstance.commitAndCloseSession();
		
		//check the search function with resources
		SearchBusinessGroupParams paramsWith = new SearchBusinessGroupParams();
		paramsWith.setResources(Boolean.TRUE);
		List<BusinessGroup> groupWith = businessGroupDao.findBusinessGroups(paramsWith, null, 0, 0);
		Assert.assertNotNull(groupWith);
		Assert.assertFalse(groupWith.isEmpty());
		Assert.assertTrue(groupWith.contains(group1));

		//check the search function without resources
		SearchBusinessGroupParams paramsWithout = new SearchBusinessGroupParams();
		paramsWithout.setResources(Boolean.FALSE);
		List<BusinessGroup> groupWithout = businessGroupDao.findBusinessGroups(paramsWithout, null, 0, 0);
		Assert.assertNotNull(groupWithout);
		Assert.assertFalse(groupWithout.isEmpty());
		Assert.assertTrue(groupWithout.contains(group2));

		//check the same with the views
		//check the search function with resources
		List<BusinessGroupView> groupViewWith = businessGroupDao.findBusinessGroupViews(paramsWith, null, 0, 0);
		Assert.assertNotNull(groupViewWith);
		Assert.assertFalse(groupViewWith.isEmpty());
		Assert.assertTrue(contains(groupViewWith, group1));

		//check the search function without resources
		List<BusinessGroupView> groupViewWithout = businessGroupDao.findBusinessGroupViews(paramsWithout, null, 0, 0);
		Assert.assertNotNull(groupViewWithout);
		Assert.assertFalse(groupViewWithout.isEmpty());
		Assert.assertTrue(contains(groupViewWithout, group2));
	}
	
	@Test
	public void findMarkedBusinessGroup() {
		Identity marker = JunitTestHelper.createAndPersistIdentityAsUser("marker-" + UUID.randomUUID().toString());
		//create a group with a mark and an other without as control
		BusinessGroup group1 = businessGroupDao.createAndPersist(marker, "marked-grp-1", "marked-grp-1-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(marker, "marked-grp-2", "marked-grp-2-desc", 0, 5, true, false, true, false, false);
		markManager.setMark(group1.getResource(), marker, null, "[BusinessGroup:" + group1.getKey() + "]");
		dbInstance.commitAndCloseSession();
		
		//check marked
		SearchBusinessGroupParams paramsAll = new SearchBusinessGroupParams();
		paramsAll.setIdentity(marker);
		paramsAll.setMarked(Boolean.TRUE);
		List<BusinessGroup> markedGroups = businessGroupDao.findBusinessGroups(paramsAll, null, 0, 0);
		Assert.assertNotNull(markedGroups);
		Assert.assertEquals(1, markedGroups.size());
		Assert.assertTrue(markedGroups.contains(group1));
		
		//check not marked
		SearchBusinessGroupParams paramsNotMarked = new SearchBusinessGroupParams();
		paramsNotMarked.setIdentity(marker);
		paramsNotMarked.setOwner(true);
		paramsNotMarked.setMarked(Boolean.FALSE);
		List<BusinessGroup> notMarkedGroups = businessGroupDao.findBusinessGroups(paramsNotMarked, null, 0, 0);
		Assert.assertNotNull(notMarkedGroups);
		Assert.assertEquals(1, notMarkedGroups.size());
		Assert.assertTrue(notMarkedGroups.contains(group2));
		
		//check the search with the views
		//check marked
		List<BusinessGroupView> markedGroupViews = businessGroupDao.findBusinessGroupViews(paramsAll, null, 0, 0);
		Assert.assertNotNull(markedGroupViews);
		Assert.assertEquals(1, markedGroupViews.size());
		Assert.assertTrue(contains(markedGroupViews, group1));
		
		//check not marked
		List<BusinessGroupView> notMarkedGroupViews = businessGroupDao.findBusinessGroupViews(paramsNotMarked, null, 0, 0);
		Assert.assertNotNull(notMarkedGroupViews);
		Assert.assertEquals(1, notMarkedGroupViews.size());
		Assert.assertTrue(contains(notMarkedGroupViews, group2));
	}
	
	@Test
	public void findMarkedBusinessGroupCrossContamination() {
		Identity marker1 = JunitTestHelper.createAndPersistIdentityAsUser("marker-1-" + UUID.randomUUID().toString());
		Identity marker2 = JunitTestHelper.createAndPersistIdentityAsUser("marker-2-" + UUID.randomUUID().toString());
		//create a group with a mark and an other without as control
		BusinessGroup group1 = businessGroupDao.createAndPersist(marker1, "marked-grp-3", "marked-grp-1-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(marker1, "marked-grp-4", "marked-grp-2-desc", 0, 5, true, false, true, false, false);
		markManager.setMark(group1.getResource(), marker1, null, "[BusinessGroup:" + group1.getKey() + "]");
		markManager.setMark(group2.getResource(), marker2, null, "[BusinessGroup:" + group2.getKey() + "]");
		dbInstance.commitAndCloseSession();
		
		//check marked
		SearchBusinessGroupParams paramsMarker1 = new SearchBusinessGroupParams();
		paramsMarker1.setIdentity(marker1);
		paramsMarker1.setMarked(Boolean.TRUE);
		List<BusinessGroup> markedGroups = businessGroupDao.findBusinessGroups(paramsMarker1, null, 0, 0);
		Assert.assertNotNull(markedGroups);
		Assert.assertEquals(1, markedGroups.size());
		Assert.assertTrue(markedGroups.contains(group1));
		
		//check not marked
		SearchBusinessGroupParams paramsMarker2 = new SearchBusinessGroupParams();
		paramsMarker2.setIdentity(marker2);
		paramsMarker2.setMarked(Boolean.TRUE);
		List<BusinessGroup> markedGroups2 = businessGroupDao.findBusinessGroups(paramsMarker2, null, 0, 0);
		Assert.assertNotNull(markedGroups2);
		Assert.assertEquals(1, markedGroups2.size());
		Assert.assertTrue(markedGroups2.contains(group2));
		
		//check the search with views
		//check marked
		List<BusinessGroupView> markedGroupViews = businessGroupDao.findBusinessGroupViews(paramsMarker1, null, 0, 0);
		Assert.assertNotNull(markedGroupViews);
		Assert.assertEquals(1, markedGroupViews.size());
		Assert.assertTrue(contains(markedGroupViews, group1));
		
		//check not marked
		List<BusinessGroupView> markedGroupsView2 = businessGroupDao.findBusinessGroupViews(paramsMarker2, null, 0, 0);
		Assert.assertNotNull(markedGroupsView2);
		Assert.assertEquals(1, markedGroupsView2.size());
		Assert.assertTrue(contains(markedGroupsView2, group2));
	}
	
	@Test
	public void findBusinessGroupsHeadless() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("head-1-" + UUID.randomUUID().toString());
		BusinessGroup headlessGroup = businessGroupDao.createAndPersist(null, "headless-grp", "headless-grp-desc", 0, 5, true, false, true, false, false);
		BusinessGroup headedGroup = businessGroupDao.createAndPersist(owner, "headed-grp", "headed-grp-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();
		
		//check marked
		SearchBusinessGroupParams headlessParams = new SearchBusinessGroupParams();
		headlessParams.setHeadless(true);
		List<BusinessGroupView> groups = businessGroupDao.findBusinessGroupViews(headlessParams, null, 0, 0);
		Assert.assertNotNull(groups);
		Assert.assertFalse(groups.isEmpty());
		Assert.assertTrue(contains(groups, headlessGroup));
		Assert.assertFalse(contains(groups, headedGroup));
	}
	
	@Test
	public void findBusinessGroupsNumOfMembers() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("head-1-" + UUID.randomUUID().toString());
		Identity part1 = JunitTestHelper.createAndPersistIdentityAsUser("head-1-" + UUID.randomUUID().toString());
		Identity part2 = JunitTestHelper.createAndPersistIdentityAsUser("head-1-" + UUID.randomUUID().toString());
		BusinessGroup groupWith1 = businessGroupDao.createAndPersist(owner, "headless-grp", "headless-grp-desc", 0, 5, true, false, true, false, false);
		BusinessGroup groupWith3 = businessGroupDao.createAndPersist(owner, "headed-grp", "headed-grp-desc", 0, 5, true, false, true, false, false);
		securityManager.addIdentityToSecurityGroup(part1, groupWith3.getPartipiciantGroup());
		securityManager.addIdentityToSecurityGroup(part2, groupWith3.getPartipiciantGroup());
		dbInstance.commitAndCloseSession();
		
		//check groups with more than 2 members
		SearchBusinessGroupParams paramsWithMoreThan2 = new SearchBusinessGroupParams();
		paramsWithMoreThan2.setNumOfMembers(2);
		paramsWithMoreThan2.setNumOfMembersBigger(true);
		List<BusinessGroupView> groupsWithMoreThan2 = businessGroupDao.findBusinessGroupViews(paramsWithMoreThan2, null, 0, 0);
		Assert.assertNotNull(groupsWithMoreThan2);
		Assert.assertFalse(groupsWithMoreThan2.isEmpty());
		Assert.assertTrue(contains(groupsWithMoreThan2, groupWith3));
		Assert.assertFalse(contains(groupsWithMoreThan2, groupWith1));
		
		//check groups with more than 2 members
		SearchBusinessGroupParams paramsWithLessThan2 = new SearchBusinessGroupParams();
		paramsWithLessThan2.setNumOfMembers(2);
		paramsWithLessThan2.setNumOfMembersBigger(false);
		List<BusinessGroupView> groupsWithLessThan2 = businessGroupDao.findBusinessGroupViews(paramsWithLessThan2, null, 0, 0);
		Assert.assertNotNull(groupsWithLessThan2);
		Assert.assertFalse(groupsWithLessThan2.isEmpty());
		Assert.assertTrue(contains(groupsWithLessThan2, groupWith1));
		Assert.assertFalse(contains(groupsWithLessThan2, groupWith3));
	}
	
	
	@Test
	public void findBusinessGroupOrdered() {
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "a_ordered-grp-3", "marked-grp-1-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "z_ordered-grp-4", "marked-grp-2-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();
		
		//check the query order by name
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		List<BusinessGroup> orderedByName = businessGroupDao.findBusinessGroups(params, null, 0, 0, BusinessGroupOrder.nameAsc);
		Assert.assertNotNull(orderedByName);
		Assert.assertFalse(orderedByName.isEmpty());
		int index1 = orderedByName.indexOf(group1);
		int index2 = orderedByName.indexOf(group2);
		Assert.assertTrue(index1 < index2);

		//check the query order by creation date
		List<BusinessGroup> orderedByCreationDate = businessGroupDao.findBusinessGroups(params, null, 0, 0, BusinessGroupOrder.creationDateAsc);
		Assert.assertNotNull(orderedByCreationDate);
		Assert.assertFalse(orderedByCreationDate.isEmpty());
		int index3 = orderedByCreationDate.indexOf(group1);
		int index4 = orderedByCreationDate.indexOf(group2);
		Assert.assertTrue(index3 < index4);
		
		//check the query order by creation date
		List<BusinessGroup> orderedBy = businessGroupDao.findBusinessGroups(params, null, 0, 0, BusinessGroupOrder.nameAsc, BusinessGroupOrder.creationDateDesc);
		Assert.assertNotNull(orderedBy);
		Assert.assertFalse(orderedBy.isEmpty());
		int index5 = orderedBy.indexOf(group1);
		int index6 = orderedBy.indexOf(group2);
		Assert.assertTrue(index5 < index6);
		
		//The find views must return exactly the same results
		//views: check the query order by name
		List<BusinessGroupView> orderedViewByName = businessGroupDao.findBusinessGroupViews(params, null, 0, 0, BusinessGroupOrder.nameAsc);
		Assert.assertNotNull(orderedViewByName);
		Assert.assertFalse(orderedViewByName.isEmpty());
		int indexView1 = indexOf(orderedViewByName, group1);
		int indexView2 = indexOf(orderedViewByName, group2);
		Assert.assertTrue(indexView1 < indexView2);

		//check the query order by creation date
		List<BusinessGroupView> orderedViewByCreationDate = businessGroupDao.findBusinessGroupViews(params, null, 0, 0, BusinessGroupOrder.creationDateAsc);
		Assert.assertNotNull(orderedViewByCreationDate);
		Assert.assertFalse(orderedViewByCreationDate.isEmpty());
		int indexView3 = indexOf(orderedViewByCreationDate, group1);
		int indexView4 = indexOf(orderedViewByCreationDate, group2);
		Assert.assertTrue(indexView3 < indexView4);
		
		//check the query order by creation date
		List<BusinessGroupView> orderedViewBy = businessGroupDao.findBusinessGroupViews(params, null, 0, 0, BusinessGroupOrder.nameAsc, BusinessGroupOrder.creationDateDesc);
		Assert.assertNotNull(orderedViewBy);
		Assert.assertFalse(orderedViewBy.isEmpty());
		int indexView5 = indexOf(orderedViewBy, group1);
		int indexView6 = indexOf(orderedViewBy, group2);
		Assert.assertTrue(indexView5 < indexView6);
	}
	
	@Test
	public void isIdentityInBusinessGroups() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("is-in-grp-" + UUID.randomUUID().toString());
		BusinessGroup group1 = businessGroupDao.createAndPersist(id, "is-in-grp-1", "is-in-grp-1-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "is-in-grp-2", "is-in-grp-2-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "is-in-grp-3", "is-in-grp-3-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		securityManager.addIdentityToSecurityGroup(id, group2.getPartipiciantGroup());
		securityManager.addIdentityToSecurityGroup(id, group3.getWaitingGroup());
		dbInstance.commitAndCloseSession();
		
		List<BusinessGroup> groups = new ArrayList<BusinessGroup>();
		groups.add(group1);
		groups.add(group2);
		groups.add(group3);

		//check owner + attendee
		List<Long> groupKeysA = businessGroupDao.isIdentityInBusinessGroups(id, true, true, false, groups);
		Assert.assertNotNull(groupKeysA);
		Assert.assertEquals(2, groupKeysA.size());
		Assert.assertTrue(groupKeysA.contains(group1.getKey()));
		Assert.assertTrue(groupKeysA.contains(group2.getKey()));
		
		//check owner 
		List<Long> groupKeysB = businessGroupDao.isIdentityInBusinessGroups(id, true, false, false, groups);
		Assert.assertNotNull(groupKeysB);
		Assert.assertEquals(1, groupKeysB.size());
		Assert.assertTrue(groupKeysB.contains(group1.getKey()));

		//check attendee 
		List<Long> groupKeysC = businessGroupDao.isIdentityInBusinessGroups(id, false, true, false, groups);
		Assert.assertNotNull(groupKeysC);
		Assert.assertEquals(1, groupKeysC.size());
		Assert.assertTrue(groupKeysC.contains(group2.getKey()));
	}
	
	@Test
	public void getMembershipInfoInBusinessGroups() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("is-in-grp-" + UUID.randomUUID().toString());
		BusinessGroup group1 = businessGroupDao.createAndPersist(id, "is-in-grp-1", "is-in-grp-1-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "is-in-grp-2", "is-in-grp-2-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "is-in-grp-3", "is-in-grp-3-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		securityManager.addIdentityToSecurityGroup(id, group2.getPartipiciantGroup());
		securityManager.addIdentityToSecurityGroup(id, group3.getWaitingGroup());
		dbInstance.commitAndCloseSession();
		
		List<Long> groupKeys = new ArrayList<Long>();
		groupKeys.add(group1.getKey());
		groupKeys.add(group2.getKey());
		groupKeys.add(group3.getKey());

		//check owner + attendee
		int countMembershipA = businessGroupDao.countMembershipInfoInBusinessGroups(id, groupKeys);
		Assert.assertEquals(3, countMembershipA);
		List<BusinessGroupMembershipViewImpl> memberships = businessGroupDao.getMembershipInfoInBusinessGroups(groupKeys, id);
		Assert.assertNotNull(memberships);
		Assert.assertEquals(3, memberships.size());
		
		int found = 0;
		for(BusinessGroupMembershipViewImpl membership:memberships) {
			Assert.assertNotNull(membership.getIdentityKey());
			Assert.assertNotNull(membership.getCreationDate());
			Assert.assertNotNull(membership.getLastModified());
			if(membership.getOwnerGroupKey() != null && group1.getKey().equals(membership.getOwnerGroupKey())) {
				found++;
			}
			if(membership.getParticipantGroupKey() != null && group2.getKey().equals(membership.getParticipantGroupKey())) {
				found++;
			}
			if(membership.getWaitingGroupKey() != null && group3.getKey().equals(membership.getWaitingGroupKey())) {
				found++;
			}
		}
		Assert.assertEquals(3, found);
	}
	
	@Test
	public void getMembershipInfoInBusinessGroupsWithoutIdentityParam() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("is-in-grp-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("is-in-grp-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("is-in-grp-" + UUID.randomUUID().toString());
		
		BusinessGroup group1 = businessGroupDao.createAndPersist(id1, "is-in-grp-1", "is-in-grp-1-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(id2, "is-in-grp-2", "is-in-grp-2-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "is-in-grp-3", "is-in-grp-3-desc", 0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		securityManager.addIdentityToSecurityGroup(id1, group1.getPartipiciantGroup());
		securityManager.addIdentityToSecurityGroup(id1, group3.getWaitingGroup());
		securityManager.addIdentityToSecurityGroup(id2, group3.getOwnerGroup());
		securityManager.addIdentityToSecurityGroup(id3, group2.getWaitingGroup());
		securityManager.addIdentityToSecurityGroup(id3, group3.getPartipiciantGroup());
		dbInstance.commitAndCloseSession();
		
		List<Long> groupKeys = new ArrayList<Long>();
		groupKeys.add(group1.getKey());
		groupKeys.add(group2.getKey());
		groupKeys.add(group3.getKey());

		//check owner + attendee + waiting
		List<BusinessGroupMembershipViewImpl> memberships = businessGroupDao.getMembershipInfoInBusinessGroups(groupKeys);
		Assert.assertNotNull(memberships);
		Assert.assertEquals(7, memberships.size());
		for(BusinessGroupMembershipViewImpl membership:memberships) {
			Assert.assertNotNull(membership.getIdentityKey());
			Assert.assertNotNull(membership.getCreationDate());
			Assert.assertNotNull(membership.getLastModified());
		}
	}
	
	private boolean contains(List<BusinessGroupView> views, BusinessGroup group) {
		if(views != null && !views.isEmpty()) {
			for(BusinessGroupView view:views) {
				if(view.getKey().equals(group.getKey())) {
					return true; 
				}
			}
		}
		return false;
	}
	
	private int indexOf(List<BusinessGroupView> views, BusinessGroup group) {
		int index = -1;
		if(views != null && !views.isEmpty()) {
			for(BusinessGroupView view:views) {
				index++;
				if(view.getKey().equals(group.getKey())) {
					break; 
				}
			}
		}
		return index;
	}
}
