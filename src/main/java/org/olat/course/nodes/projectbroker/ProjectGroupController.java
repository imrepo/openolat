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
* <p>
*/ 

package org.olat.course.nodes.projectbroker;

import java.util.ArrayList;
import java.util.List;

import org.olat.admin.securitygroup.gui.GroupController;
import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.admin.securitygroup.gui.IdentitiesMoveEvent;
import org.olat.admin.securitygroup.gui.IdentitiesRemoveEvent;
import org.olat.admin.securitygroup.gui.WaitingGroupController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.logging.activity.ActionType;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.MailerWithTemplate;
import org.olat.course.nodes.projectbroker.datamodel.Project;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerManagerFactory;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerModuleConfiguration;
import org.olat.group.BusinessGroupAddResponse;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.group.ui.BGConfigFlags;

/**
 * 
 * @author guretzki
 * 
 */
public class ProjectGroupController extends BasicController {

	private GroupController projectLeaderController;
	private GroupController projectMemberController;
	private WaitingGroupController projectCandidatesController;

	private Project project;

	private ProjectBrokerModuleConfiguration projectBrokerModuleConfiguration;

	/**
	 * @param ureq
	 * @param wControl
	 * @param hpc
	 */
	public ProjectGroupController(UserRequest ureq, WindowControl wControl, Project project, ProjectBrokerModuleConfiguration projectBrokerModuleConfiguration) {
		super(ureq, wControl);
		getUserActivityLogger().setStickyActionType(ActionType.admin);
		this.project = project;
		this.projectBrokerModuleConfiguration = projectBrokerModuleConfiguration;
		
		VelocityContainer myContent = createVelocityContainer("projectgroup_management");

		// Project Leader Management
		projectLeaderController = new GroupController(ureq, getWindowControl(), true, true, true, project.getProjectLeaderGroup());
		listenTo(projectLeaderController);
		myContent.put("projectLeaderController", projectLeaderController.getInitialComponent());

		// Project Member Management
		projectMemberController = new GroupController(ureq, getWindowControl(), true, false, true, project.getProjectParticipantGroup());
		listenTo(projectMemberController);
		myContent.put("projectMemberController", projectMemberController.getInitialComponent());
		// add mail templates used when adding and removing users
		MailTemplate partAddUserMailTempl = ProjectBrokerManagerFactory.getProjectBrokerEmailer().createAddParticipantMailTemplate(project, ureq.getIdentity(), this.getTranslator());
		projectMemberController.setAddUserMailTempl(partAddUserMailTempl,false);
		MailTemplate partRemoveUserMailTempl = ProjectBrokerManagerFactory.getProjectBrokerEmailer().createRemoveParticipantMailTemplate(project, ureq.getIdentity(), this.getTranslator());
		projectMemberController.setRemoveUserMailTempl(partRemoveUserMailTempl,false);

		// Project Candidates Management
		if (projectBrokerModuleConfiguration.isAcceptSelectionManually()) {
			projectCandidatesController = new WaitingGroupController(ureq, getWindowControl(), true, false, true, project.getCandidateGroup());
			listenTo(projectCandidatesController);
			myContent.contextPut("isProjectCandidatesListEmpty", ProjectBrokerManagerFactory.getProjectGroupManager().isCandidateListEmpty(project.getCandidateGroup()) );
			myContent.put("projectCandidatesController", projectCandidatesController.getInitialComponent());
			// add mail templates used when adding and removing users
			MailTemplate waitAddUserMailTempl = ProjectBrokerManagerFactory.getProjectBrokerEmailer().createAddCandidateMailTemplate(project, ureq.getIdentity(), this.getTranslator());
			projectCandidatesController.setAddUserMailTempl(waitAddUserMailTempl,false);
			MailTemplate waitRemoveUserMailTempl = ProjectBrokerManagerFactory.getProjectBrokerEmailer().createRemoveAsCandiadateMailTemplate(project, ureq.getIdentity(), this.getTranslator());
			projectCandidatesController.setRemoveUserMailTempl(waitRemoveUserMailTempl,false);
			MailTemplate waitTransferUserMailTempl = ProjectBrokerManagerFactory.getProjectBrokerEmailer().createAcceptCandiadateMailTemplate(project, ureq.getIdentity(), this.getTranslator());
			projectCandidatesController.setTransferUserMailTempl(waitTransferUserMailTempl);
		}
		
		putInitialPanel(myContent);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
	}
	
	public void event(UserRequest urequest, Controller source, Event event) {
		if ( ProjectBrokerManagerFactory.getProjectBrokerManager().existsProject( project.getKey() ) ) {
			if (source == projectLeaderController) {
				handleProjectLeaderGroupEvent(urequest, event);
			} else if (source == projectMemberController) {
				handleProjectMemberGroupEvent(urequest, event);
			} else if (source == projectCandidatesController) {
				handleCandidateGroupEvent(urequest, event);
			}
		} else {
			this.showInfo("info.project.nolonger.exist", project.getTitle());
		}
	}

	private void handleCandidateGroupEvent(UserRequest urequest, Event event) {
		BGConfigFlags flags = BGConfigFlags.createRightGroupDefaultFlags();
		if (event instanceof IdentitiesAddEvent) {
			IdentitiesAddEvent identitiesAddEvent = (IdentitiesAddEvent)event;
			List<Identity> addedIdentities = ProjectBrokerManagerFactory.getProjectGroupManager().addCandidates(identitiesAddEvent.getAddIdentities(), project);
			identitiesAddEvent.setIdentitiesAddedEvent(addedIdentities);
			fireEvent(urequest, Event.CHANGED_EVENT );			
		} else if (event instanceof IdentitiesRemoveEvent) {
			ProjectBrokerManagerFactory.getProjectGroupManager().removeCandidates(((IdentitiesRemoveEvent)event).getRemovedIdentities(), project);
			fireEvent(urequest, Event.CHANGED_EVENT );
		} else if (event instanceof IdentitiesMoveEvent) {
			final IdentitiesMoveEvent identitiesMoveEvent = (IdentitiesMoveEvent) event;
			//OLAT-6342: check identity not in group first!
			List<Identity> moveIdents = identitiesMoveEvent.getChosenIdentities();
			BusinessGroupAddResponse response = ProjectBrokerManagerFactory.getProjectGroupManager().acceptCandidates(moveIdents, project, urequest.getIdentity(),
					projectBrokerModuleConfiguration.isAutoSignOut(), projectBrokerModuleConfiguration.isAcceptSelectionManually());
			identitiesMoveEvent.setMovedIdentities(response.getAddedIdentities());
			identitiesMoveEvent.setNotMovedIdentities(response.getIdentitiesAlreadyInGroup());
			// send mail for all of them
			MailerWithTemplate mailer = MailerWithTemplate.getInstance();
			MailTemplate mailTemplate = identitiesMoveEvent.getMailTemplate();
			if (mailTemplate != null) {
				List<Identity> ccIdentities = new ArrayList<Identity>();
				if(mailTemplate.getCpfrom()) {
					// add sender as CC 
					ccIdentities.add(urequest.getIdentity()); 
				} else {
					ccIdentities = null;	
				}
				//fxdiff VCRP-16: intern mail system
				MailContext context = new MailContextImpl(getWindowControl().getBusinessControl().getAsString());
				MailerResult mailerResult = mailer.sendMailAsSeparateMails(context, identitiesMoveEvent.getMovedIdentities(), ccIdentities, null, mailTemplate, null);
				MailHelper.printErrorsAndWarnings(mailerResult, getWindowControl(), urequest.getLocale());
			}
			fireEvent(urequest, Event.CHANGED_EVENT );		
			// Participant and waiting-list were changed => reload both
			projectMemberController.reloadData();
		  projectCandidatesController.reloadData(); // Do only reload data in case of IdentitiesMoveEvent (IdentitiesAddEvent and reload data resulting in doublicate values)
		}
	}

	private void handleProjectMemberGroupEvent(UserRequest urequest, Event event) {
		BGConfigFlags flags = BGConfigFlags.createRightGroupDefaultFlags();
		if (event instanceof IdentitiesAddEvent) {
			IdentitiesAddEvent identitiesAddedEvent = (IdentitiesAddEvent)event;
			BusinessGroupAddResponse response = BusinessGroupManagerImpl.getInstance().addParticipantsAndFireEvent(urequest.getIdentity(), identitiesAddedEvent.getAddIdentities(), project.getProjectGroup(), flags);
			identitiesAddedEvent.setIdentitiesAddedEvent(response.getAddedIdentities());
			identitiesAddedEvent.setIdentitiesWithoutPermission(response.getIdentitiesWithoutPermission());
			identitiesAddedEvent.setIdentitiesAlreadyInGroup(response.getIdentitiesAlreadyInGroup());
			getLogger().info("Add users as project-members");
			fireEvent(urequest, Event.CHANGED_EVENT );			
		} else if (event instanceof IdentitiesRemoveEvent) {
			BusinessGroupManagerImpl.getInstance().removeParticipantsAndFireEvent(urequest.getIdentity(), ((IdentitiesRemoveEvent) event).getRemovedIdentities(), project.getProjectGroup(), flags);
			getLogger().info("Remove users as account-managers");
			fireEvent(urequest, Event.CHANGED_EVENT );
		}
	}

	private void handleProjectLeaderGroupEvent(UserRequest urequest, Event event) {
		BGConfigFlags flags = BGConfigFlags.createRightGroupDefaultFlags();
		if (event instanceof IdentitiesAddEvent) {
			IdentitiesAddEvent identitiesAddedEvent = (IdentitiesAddEvent)event;
			BusinessGroupAddResponse response = BusinessGroupManagerImpl.getInstance().addOwnersAndFireEvent(urequest.getIdentity(), identitiesAddedEvent.getAddIdentities(), project.getProjectGroup(), flags);
			identitiesAddedEvent.setIdentitiesAddedEvent(response.getAddedIdentities());
			identitiesAddedEvent.setIdentitiesWithoutPermission(response.getIdentitiesWithoutPermission());
			identitiesAddedEvent.setIdentitiesAlreadyInGroup(response.getIdentitiesAlreadyInGroup());
			getLogger().info("Add users as project-leader");
			fireEvent(urequest, Event.CHANGED_EVENT );			
		} else if (event instanceof IdentitiesRemoveEvent) {
			BusinessGroupManagerImpl.getInstance().removeOwnersAndFireEvent(urequest.getIdentity(), ((IdentitiesRemoveEvent) event).getRemovedIdentities(), project.getProjectGroup(), flags);
			getLogger().info("Remove users as account-managers");
			fireEvent(urequest, Event.CHANGED_EVENT );
		}
	}

	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// child controller disposed by basic controller
	}
	
}
