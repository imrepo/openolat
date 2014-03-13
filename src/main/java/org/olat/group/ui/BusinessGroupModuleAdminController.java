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
package org.olat.group.ui;

import java.util.Set;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.progressbar.ProgressController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.async.ProgressDelegate;
import org.olat.group.BusinessGroupModule;
import org.olat.group.BusinessGroupService;
import org.olat.group.ui.main.DedupMembersConfirmationController;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupModuleAdminController extends FormBasicController implements ProgressDelegate {
	
	private FormLink dedupLink;
	private MultipleSelectionElement allowEl;
	private MultipleSelectionElement enrolmentEl;
	private MultipleSelectionElement membershipEl;
	private MultipleSelectionElement assignCoursesEl;
	private MultipleSelectionElement assignGroupsEl;

	private Panel mainPopPanel;
	private CloseableModalController cmc;
	private ProgressController progressCtrl;
	private DedupMembersConfirmationController dedupCtrl;
	
	private final BusinessGroupModule module;
	private final BusinessGroupService businessGroupService;
	private String[] onKeys = new String[]{"user","author"};
	private String[] enrollmentKeys = new String[]{
			"users","authors", "usermanagers", "groupmanagers", "administrators"
	};
	private String[] assignKeys = new String[]{"granted"};
	
	public BusinessGroupModuleAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "bg_admin");
		module = CoreSpringFactory.getImpl(BusinessGroupModule.class);
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer optionsContainer = FormLayoutContainer.createDefaultFormLayout("options", getTranslator());
		formLayout.add(optionsContainer);
		String[] values = new String[]{
				translate("user.allow.create"),
				translate("author.allow.create")
		};
		allowEl = uifactory.addCheckboxesVertical("module.admin.allow.create", optionsContainer, onKeys, values, null, 1);
		allowEl.select("user", module.isUserAllowedCreate());
		allowEl.select("author", module.isAuthorAllowedCreate());
		allowEl.addActionListener(this, FormEvent.ONCHANGE);

		FormLayoutContainer resourceAssignmentContainer = FormLayoutContainer.createDefaultFormLayout("resourceAssignment", getTranslator());
		formLayout.add(resourceAssignmentContainer);
		
		String[] courseValues = new String[]{ translate("module.resource.courses.grant") };
		assignCoursesEl = uifactory.addCheckboxesVertical("module.resource.courses", resourceAssignmentContainer, assignKeys, courseValues, null, 1);
		assignCoursesEl.select(assignKeys[0], module.isGroupManagersAllowedToLinkCourses());
		assignCoursesEl.addActionListener(this, FormEvent.ONCHANGE);
		
		String[] groupValues = new String[]{ translate("module.resource.groups.grant") };
		assignGroupsEl = uifactory.addCheckboxesVertical("module.resource.groups", resourceAssignmentContainer, assignKeys, groupValues, null, 1);
		assignGroupsEl.select(assignKeys[0], module.isResourceManagersAllowedToLinkGroups());
		assignGroupsEl.addActionListener(this, FormEvent.ONCHANGE);
		
		FormLayoutContainer privacyOptionsContainer = FormLayoutContainer.createDefaultFormLayout("privacy_options", getTranslator());
		formLayout.add(privacyOptionsContainer);
		String[] enrollmentValues = new String[]{
				translate("enrolment.email.users"),
				translate("enrolment.email.authors"),
				translate("enrolment.email.usermanagers"),
				translate("enrolment.email.groupmanagers"),
				translate("enrolment.email.administrators")
		};
		enrolmentEl = uifactory.addCheckboxesVertical("mandatory.enrolment", privacyOptionsContainer, enrollmentKeys, enrollmentValues, null, 1);
		enrolmentEl.select("users", "true".equals(module.getMandatoryEnrolmentEmailForUsers()));
		enrolmentEl.select("authors", "true".equals(module.getMandatoryEnrolmentEmailForAuthors()));
		enrolmentEl.select("usermanagers", "true".equals(module.getMandatoryEnrolmentEmailForUsermanagers()));
		enrolmentEl.select("groupmanagers", "true".equals(module.getMandatoryEnrolmentEmailForGroupmanagers()));
		enrolmentEl.select("administrators", "true".equals(module.getMandatoryEnrolmentEmailForAdministrators()));
		enrolmentEl.addActionListener(this, FormEvent.ONCHANGE);
		
		String[] membershipValues = new String[]{
				translate("enrolment.email.users"),
				translate("enrolment.email.authors"),
				translate("enrolment.email.usermanagers"),
				translate("enrolment.email.groupmanagers"),
				translate("enrolment.email.administrators")
		};
		membershipEl = uifactory.addCheckboxesVertical("mandatory.membership", privacyOptionsContainer, enrollmentKeys, membershipValues, null, 1);
		membershipEl.select("users", "true".equals(module.getAcceptMembershipForUsers()));
		membershipEl.select("authors", "true".equals(module.getAcceptMembershipForAuthors()));
		membershipEl.select("usermanagers", "true".equals(module.getAcceptMembershipForUsermanagers()));
		membershipEl.select("groupmanagers", "true".equals(module.getAcceptMembershipForGroupmanagers()));
		membershipEl.select("administrators", "true".equals(module.getAcceptMembershipForAdministrators()));
		membershipEl.addActionListener(this, FormEvent.ONCHANGE);

		FormLayoutContainer dedupCont = FormLayoutContainer.createDefaultFormLayout("dedup", getTranslator());
		formLayout.add(dedupCont);
		dedupLink = uifactory.addFormLink("dedup.members", dedupCont, Link.BUTTON);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == dedupCtrl) {
			boolean coaches = dedupCtrl.isDedupCoaches();
			boolean participants = dedupCtrl.isDedupParticipants();
			if(event == Event.DONE_EVENT) {
				dedupMembers(ureq, coaches, participants);
			} else {
				cmc.deactivate();
				cleanUp();
			}
		} else if(source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(dedupCtrl);
		removeAsListenerAndDispose(progressCtrl);
		removeAsListenerAndDispose(cmc);
		progressCtrl = null;
		dedupCtrl = null;
		cmc = null;
	}
	
	

	@Override
	public void setMax(float max) {
		if(progressCtrl != null) {
			progressCtrl.setMax(max);
		}
	}

	@Override
	public void setActual(float value) {
		if(progressCtrl != null) {
			progressCtrl.setActual(value);
		}
	}

	@Override
	public void setInfo(String message) {
		if(progressCtrl != null) {
			progressCtrl.setInfo(message);
		}
	}

	@Override
	public void finished() {
		cmc.deactivate();
		cleanUp();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == dedupLink) {
			doDedupMembers(ureq);
		} else if(source == allowEl) {
			module.setUserAllowedCreate(allowEl.isSelected(0));
			module.setAuthorAllowedCreate(allowEl.isSelected(1));
		} else if(source == membershipEl) {
			Set<String> membershipSelectedKeys = membershipEl.getSelectedKeys();
			module.setAcceptMembershipForUsers(membershipSelectedKeys.contains("users") ? "true" : "false");
			module.setAcceptMembershipForAuthors(membershipSelectedKeys.contains("authors") ? "true" : "false");
			module.setAcceptMembershipForUsermanagers(membershipSelectedKeys.contains("usermanagers") ? "true" : "false");
			module.setAcceptMembershipForGroupmanagers(membershipSelectedKeys.contains("groupmanagers") ? "true" : "false");
			module.setAcceptMembershipForAdministrators(membershipSelectedKeys.contains("administrators") ? "true" : "false");
		} else if(source == enrolmentEl) {
			Set<String> enrolmentSelectedKeys = enrolmentEl.getSelectedKeys();
			module.setMandatoryEnrolmentEmailForUsers(enrolmentSelectedKeys.contains("users") ? "true" : "false");
			module.setMandatoryEnrolmentEmailForAuthors(enrolmentSelectedKeys.contains("authors") ? "true" : "false");
			module.setMandatoryEnrolmentEmailForUsermanagers(enrolmentSelectedKeys.contains("usermanagers") ? "true" : "false");
			module.setMandatoryEnrolmentEmailForGroupmanagers(enrolmentSelectedKeys.contains("groupmanagers") ? "true" : "false");
			module.setMandatoryEnrolmentEmailForAdministrators(enrolmentSelectedKeys.contains("administrators") ? "true" : "false");
		} else if(assignCoursesEl == source) {
			module.setGroupManagersAllowedToLinkCourses(assignCoursesEl.isSelected(0));
		} else if(assignGroupsEl == source) {
			module.setResourceManagersAllowedToLinkGroups(assignGroupsEl.isSelected(0));
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}
	
	protected void doDedupMembers(UserRequest ureq) {
		dedupCtrl = new DedupMembersConfirmationController(ureq, getWindowControl());
		listenTo(dedupCtrl);
		
		mainPopPanel = new Panel("dedup");
		mainPopPanel.setContent(dedupCtrl.getInitialComponent());
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), mainPopPanel, true, translate("dedup.members"), false);
		cmc.activate();
		listenTo(cmc);
	}
	
	protected void dedupMembers(UserRequest ureq, final boolean coaches, final boolean participants) {
		progressCtrl = new ProgressController(ureq, getWindowControl());
		progressCtrl.setMessage(translate("dedup.running"));
		mainPopPanel.setContent(progressCtrl.getInitialComponent());
		listenTo(progressCtrl);
		
		Runnable worker = new Runnable() {
			@Override
			public void run() {
				businessGroupService.dedupMembers(getIdentity(), coaches, participants, BusinessGroupModuleAdminController.this);
			}
		};
		CoreSpringFactory.getImpl(TaskExecutorManager.class).execute(worker);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}