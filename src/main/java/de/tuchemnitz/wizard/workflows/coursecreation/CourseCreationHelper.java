/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
* Initial code contributed and copyrighted by<br>
* Technische Universitaet Chemnitz Lehrstuhl Technische Informatik<br>
* <br>
* Author Marcel Karras (toka@freebits.de)<br>
* Author Norbert Englisch (norbert.englisch@informatik.tu-chemnitz.de)<br>
* Author Sebastian Fritzsche (seb.fritzsche@googlemail.com)
*/

package de.tuchemnitz.wizard.workflows.coursecreation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.condition.Condition;
import org.olat.course.editor.PublishProcess;
import org.olat.course.editor.StatusDescription;
import org.olat.course.editor.PublishSetInformations;
import org.olat.course.nodes.AbstractAccessableCourseNode;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.COCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.ENCourseNode;
import org.olat.course.nodes.FOCourseNode;
import org.olat.course.nodes.SPCourseNode;
import org.olat.course.nodes.co.COEditController;
import org.olat.course.nodes.sp.SPEditController;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.repository.CatalogEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.manager.CatalogManager;

import de.tuchemnitz.wizard.helper.course.CourseExtensionHelper;
import de.tuchemnitz.wizard.helper.course.HTMLDocumentHelper;
import de.tuchemnitz.wizard.workflows.coursecreation.model.CourseCreationConfiguration;

/**
 * 
 * Description:<br>
 * This helper class is responsible for finalizing the course creation wizard.
 * It reads the prepared configuration and creates the course with the necessary
 * course nodes and contents.
 * 
 * <P>
 * @author Marcel Karras (toka@freebits.de)
 * @author Norbert Englisch (norbert.englisch@informatik.tu-chemnitz.de)
 * @author Sebastian Fritzsche (seb.fritzsche@googlemail.com)
 */
public class CourseCreationHelper {
	
	private static final OLog log = Tracing.createLoggerFor(CourseCreationHelper.class);

	private CourseCreationConfiguration courseConfig;
	private final Translator translator;
	private ICourse course;
	private RepositoryEntry addedEntry;

	/**
	 * Beendet den CourseCreationWizard
	 * 
	 * @param ureq
	 * @param control
	 * @param repoEntry
	 * @param courseConfig
	 * @param course
	 * @author Norbert Englisch (norbert.englisch@informatik.tu-chemnitz.de)
	 * @author Sebastian Fritzsche (seb.fritzsche@googlemail.com)
	 */
	public CourseCreationHelper(Locale locale, final RepositoryEntry repoEntry,
			CourseCreationConfiguration courseConfig, ICourse course) {
		this.translator = Util.createPackageTranslator(CourseCreationHelper.class, locale);
		this.addedEntry = repoEntry;
		this.course = course;
		this.courseConfig = courseConfig;
	}

	/**
	 * @return the created course
	 */
	public final RepositoryEntry getUserObject() {
		return addedEntry;
	}

	/**
	 * @return the currently course configuration
	 */
	public final CourseCreationConfiguration getConfiguration() {
		return courseConfig;
	}

	/**
	 * Finalizes the course creation workflow via wizard.
	 * @param ureq
	 */
	public void finalizeWorkflow(final UserRequest ureq) {
		// --------------------------
		// 1. insert the course nodes
		// --------------------------
		// single page node
		CourseNode singlePageNode = null;
		if (courseConfig.isCreateSinglePage()) {
			singlePageNode = CourseExtensionHelper.createSinglePageNode(course, translator.translate("cce.informationpage"), translator
					.translate("cce.informationpage.descr"));
			if (singlePageNode instanceof SPCourseNode) {
				final VFSLeaf htmlLeaf = HTMLDocumentHelper.createHtmlDocument(course, "start.html", courseConfig.getSinglePageText(translator));
				((SPCourseNode) singlePageNode).getModuleConfiguration().set(SPEditController.CONFIG_KEY_FILE, "/" + htmlLeaf.getName());
			}
		}
		// enrollment node
		CourseNode enCourseNode = null;
		if (courseConfig.isCreateEnrollment()) {
			enCourseNode = CourseExtensionHelper.createEnrollmentNode(course, translator.translate("cce.enrollment"), translator
					.translate("cce.enrollment.descr"));
		}
		// download folder node
		CourseNode downloadFolderNode = null;
		if (courseConfig.isCreateDownloadFolder()) {
			downloadFolderNode = CourseExtensionHelper.createDownloadFolderNode(course, translator.translate("cce.downloadfolder"),
					translator.translate("cce.downloadfolder.descr"));
		}
		// forum node
		CourseNode forumNode = null;
		if (courseConfig.isCreateForum()) {
			forumNode = CourseExtensionHelper.createForumNode(course, translator.translate("cce.forum"), translator
					.translate("cce.forum.descr"));
		}
		// contact form node
		CourseNode contactNode = null;
		if (courseConfig.isCreateContactForm()) {
			contactNode = CourseExtensionHelper.createContactFormNode(course, translator.translate("cce.contactform"), translator
					.translate("cce.contactform.descr"));
			if (contactNode instanceof COCourseNode) {

				final List<String> emails = new ArrayList<String>();
				String subject = translator.translate("cce.contactform.subject") + " " + courseConfig.getCourseTitle();

				emails.add(ureq.getIdentity().getUser().getProperty(UserConstants.EMAIL, ureq.getLocale()));

				COCourseNode cocn = (COCourseNode) contactNode;

				cocn.getModuleConfiguration().set(COEditController.CONFIG_KEY_EMAILTOADRESSES, emails);
				cocn.getModuleConfiguration().set(COEditController.CONFIG_KEY_MSUBJECT_DEFAULT, subject);
			}
		}

		// enrollment node
		if (courseConfig.isCreateEnrollment()) {
			// --------------------------
			// 2. setup enrollment
			// --------------------------
			final String groupBaseName = createGroupBaseName();
			final BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);

			// get default context for learning groups
			
			// create n learning groups with m allowed members
			String comma = "";
			String tmpGroupList = "";
			String groupNamesList = "";
			for (int i = 0; i < courseConfig.getGroupCount(); i++) {
				// create group
				BusinessGroup learningGroup = bgs.createBusinessGroup( null, groupBaseName + " "
						+ (i + 1), null, 0, courseConfig.getSubscriberCount(), courseConfig.getEnableWaitlist(), courseConfig.getEnableFollowup(),
						addedEntry);
				// enable the contact collaboration tool
				CollaborationTools ct = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(learningGroup);
				ct.setToolEnabled(CollaborationTools.TOOL_CONTACT, true);
				// append to current learning group list
				groupNamesList = tmpGroupList + comma + learningGroup.getName();
				enCourseNode.getModuleConfiguration().set(ENCourseNode.CONFIG_GROUPNAME, groupNamesList);
				if (i == 0) {
					comma = ",";
				}
				tmpGroupList = (String) enCourseNode.getModuleConfiguration().get(ENCourseNode.CONFIG_GROUPNAME);
			}

			// set signout property
			enCourseNode.getModuleConfiguration().set(ENCourseNode.CONF_CANCEL_ENROLL_ENABLED, courseConfig.getEnableSignout());

			// access limits on chosen course elements
			if (courseConfig.getEnableAccessLimit()) {
				if (courseConfig.isEnableAclContactForm()) {
					if (contactNode instanceof COCourseNode) {
						Condition c = ((COCourseNode) contactNode).getPreConditionVisibility();
						c.setEasyModeGroupAccess(groupNamesList);
						((COCourseNode) contactNode).setNoAccessExplanation(translator.translate("noaccessexplain"));
						// calculate expression from easy mode form
						String condString = c.getConditionFromEasyModeConfiguration();
						c.setConditionExpression(condString);
						c.setExpertMode(false);
					}
				}
				if (courseConfig.isEnableAclSinglePage()) {
					if (singlePageNode instanceof SPCourseNode) {
						Condition c = ((SPCourseNode) singlePageNode).getPreConditionVisibility();
						c.setEasyModeGroupAccess(groupNamesList);
						((SPCourseNode) singlePageNode).setNoAccessExplanation(translator.translate("noaccessexplain"));
						// calculate expression from easy mode form
						String condString = c.getConditionFromEasyModeConfiguration();
						c.setConditionExpression(condString);
						c.setExpertMode(false);
					}
				}
				if (courseConfig.isEnableAclForum()) {
					if (forumNode instanceof FOCourseNode) {
						Condition c = ((FOCourseNode) forumNode).getPreConditionVisibility();
						c.setEasyModeGroupAccess(groupNamesList);
						((FOCourseNode) forumNode).setNoAccessExplanation(translator.translate("noaccessexplain"));
						// calculate expression from easy mode form
						String condString = c.getConditionFromEasyModeConfiguration();
						c.setConditionExpression(condString);
						c.setExpertMode(false);
					}
				}
				if (courseConfig.isEnableAclDownloadFolder()) {
					if (downloadFolderNode instanceof BCCourseNode) {
						Condition c = ((BCCourseNode) downloadFolderNode).getPreConditionVisibility();
						c.setEasyModeGroupAccess(groupNamesList);
						((BCCourseNode) downloadFolderNode).setNoAccessExplanation(translator.translate("noaccessexplain"));
						// calculate expression from easy mode form
						String condString = c.getConditionFromEasyModeConfiguration();
						c.setConditionExpression(condString);
						c.setExpertMode(false);
					}
				}
			}
		}

		// --------------------------
		// 3. persist complete course
		// --------------------------

		// --------------------------
		// 3.1. setup rights
		// --------------------------
		if (courseConfig.getPublish()) {
			
			int access = RepositoryEntry.ACC_OWNERS;
			if (courseConfig.getAclType().equals(CourseCreationConfiguration.ACL_GUEST)) {
				// set "BARG" as rule
				access = RepositoryEntry.ACC_USERS_GUESTS;
			} else if (courseConfig.getAclType().equals(CourseCreationConfiguration.ACL_OLAT)) {
				// set "BAR" as rule
				access = RepositoryEntry.ACC_USERS;
			} else if (courseConfig.getAclType().equals(CourseCreationConfiguration.ACL_UNI)) {
				// set "BAR" rule + expert rule on university
				// hasAttribute("institution","[Hochschule]")
				access = RepositoryEntry.ACC_USERS;
				final CourseNode cnRoot = course.getEditorTreeModel().getCourseEditorNodeById(course.getEditorTreeModel().getRootNode().getIdent())
				.getCourseNode();
				String shibInstitution = ureq.getIdentity().getUser().getProperty(UserConstants.INSTITUTIONALNAME, ureq.getLocale());
				if (shibInstitution == null) {
					shibInstitution = ureq.getUserSession().getSessionInfo().getAuthProvider();
				}
				cnRoot.setNoAccessExplanation(translator.translate("noaccessroot", new String[] {shibInstitution}));
				if (cnRoot instanceof AbstractAccessableCourseNode) {
					((AbstractAccessableCourseNode) cnRoot).setPreConditionAccess(new Condition("hasAttribute(\"institution\",\"" + shibInstitution
							+ "\")"));
				}
			} else {
				log.error("No valid ACL Rule: " + courseConfig.getAclType());
			}
			addedEntry = RepositoryManager.getInstance().setAccess(addedEntry, access, false);
		}

		course = CourseFactory.openCourseEditSession(course.getResourceableId());
		course.getRunStructure().getRootNode().setShortTitle(addedEntry.getDisplayname());
		course.getRunStructure().getRootNode().setLongTitle(addedEntry.getDisplayname());
		CourseFactory.saveCourse(course.getResourceableId());
		final CourseEditorTreeModel cetm = course.getEditorTreeModel();
		final CourseNode rootNode = cetm.getCourseNode(course.getRunStructure().getRootNode().getIdent());
		rootNode.setShortTitle(addedEntry.getDisplayname());
		rootNode.setLongTitle(addedEntry.getDisplayname());
		course.getEditorTreeModel().nodeConfigChanged(course.getRunStructure().getRootNode());
		CourseFactory.saveCourseEditorTreeModel(course.getResourceableId());

		// --------------------------
		// 3.2 publish the course
		// --------------------------
		// fetch publish process
		final PublishProcess pp = PublishProcess.getInstance(course, cetm, ureq.getLocale());
		final StatusDescription[] sds;
		// create publish node list
		List<String> nodeIds = new ArrayList<String>();
		nodeIds.add(cetm.getRootNode().getIdent());
		for (int i = 0; i < cetm.getRootNode().getChildCount(); i++) {
			nodeIds.add(cetm.getRootNode().getChildAt(i).getIdent());
		}
		pp.createPublishSetFor(nodeIds);
		PublishSetInformations set = pp.testPublishSet(ureq.getLocale());
		sds = set.getWarnings();
		boolean isValid = sds.length == 0;
		if (!isValid) {
			// no error and no warnings -> return immediate
			log.error("Course Publishing failed", new AssertionError());
		}
		pp.applyPublishSet(ureq.getIdentity(), ureq.getLocale());
		CourseFactory.closeCourseEditSession(course.getResourceableId(), true);

		// save catalog entry
		if (getConfiguration().getSelectedCatalogEntry() != null) {
			CatalogManager cm = CoreSpringFactory.getImpl(CatalogManager.class);
			CatalogEntry newEntry = cm.createCatalogEntry();
			newEntry.setRepositoryEntry(addedEntry);
			newEntry.setName(addedEntry.getDisplayname());
			newEntry.setDescription(addedEntry.getDescription());
			newEntry.setType(CatalogEntry.TYPE_LEAF);
			newEntry.setOwnerGroup(BaseSecurityManager.getInstance().createAndPersistSecurityGroup());
			// save entry
			cm.addCatalogEntry(getConfiguration().getSelectedCatalogEntry(), newEntry);
		}
	}

	/**
	 * Internal helper method to create the group names
	 * @return group name
	 */
	private String createGroupBaseName() {
		String groupBaseName = course.getCourseTitle() + " " + translator.translate("group");
		groupBaseName = groupBaseName.replace("\"", "'");
		groupBaseName = groupBaseName.replace(",", " ");
		return groupBaseName;
	}
}
