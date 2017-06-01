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
*/

package org.olat.course.nodes.ms;

import java.io.File;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.CourseModule;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.highscore.ui.HighScoreRunController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.ObjectivesHelper;
import org.olat.course.nodes.PersistentAssessableCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date:  Jun 16, 2004
 * @author gnaegi
 */
public class MSCourseNodeRunController extends BasicController {

	private final VelocityContainer myContent;
	private final boolean showLog;
	private boolean hasScore, hasPassed, hasComment;
	private final boolean overrideUserResultsVisiblity;
	
	@Autowired
	private CourseModule courseModule;

	/**
	 * Constructor for a manual scoring course run controller
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param userCourseEnv The user course environment
	 * @param msCourseNode An assessable course node
	 * @param displayNodeInfo If true, the node title and learning objectives will be displayed
	 * @param showLog If true, the change log will be displayed
	 */
	public MSCourseNodeRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			PersistentAssessableCourseNode courseNode, boolean displayNodeInfo, boolean showLog) {
		this(ureq, wControl, userCourseEnv, courseNode, displayNodeInfo, showLog, false);
	}
	
	/**
	 * Constructor for a manual scoring course run controller
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param userCourseEnv The user course environment
	 * @param msCourseNode An assessable course element
	 * @param displayNodeInfo If true, the node title and learning objectives will be displayed
	 * @param showLog If true, the change log will be displayed
	 * @param overrideUserResultsVisiblity If the controller can override the user visiblity of the score evaluation
	 */
	public MSCourseNodeRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, PersistentAssessableCourseNode msCourseNode,
			boolean displayNodeInfo, boolean showLog, boolean overrideUserResultsVisiblity) {
		super(ureq, wControl, Util.createPackageTranslator(CourseNode.class, ureq.getLocale()));
		
		this.showLog = showLog;
		this.overrideUserResultsVisiblity = overrideUserResultsVisiblity;
		myContent = createVelocityContainer("run");

		if (msCourseNode.getModuleConfiguration().getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD,false)){
			HighScoreRunController highScoreCtr = new HighScoreRunController(ureq, wControl, userCourseEnv, msCourseNode);
			if (highScoreCtr.isViewHighscore()) {
				Component highScoreComponent = highScoreCtr.getInitialComponent();
				myContent.put("highScore", highScoreComponent);							
			}
		}
				
		ModuleConfiguration config = msCourseNode.getModuleConfiguration();
		myContent.contextPut("displayNodeInfo", Boolean.valueOf(displayNodeInfo));
		if (displayNodeInfo) {
			// push title and learning objectives, only visible on intro page
			myContent.contextPut("menuTitle", msCourseNode.getShortTitle());
			myContent.contextPut("displayTitle", msCourseNode.getLongTitle());
			
			// Adding learning objectives
			String learningObj = msCourseNode.getLearningObjectives();
			if (learningObj != null) {
				Component learningObjectives = ObjectivesHelper.createLearningObjectivesComponent(learningObj, ureq); 
				myContent.put("learningObjectives", learningObjectives);
				myContent.contextPut("hasObjectives", learningObj); // dummy value, just an exists operator					
			}
		} 
		
		//admin setting whether to show change log or not
		myContent.contextPut("changelogconfig", courseModule.isDisplayChangeLog());

		// Push variables to velcity page
		exposeConfigToVC(config);		
		exposeUserDataToVC(ureq, userCourseEnv, msCourseNode);
		putInitialPanel(myContent);
	}
	
	/**
	 * @return true if the assessed user has a score
	 */
	public boolean hasScore() {
		return hasScore;
	}
	
	/**
	 * @return true if the assessed user has passed or failed.
	 */
	public boolean hasPassed() {
		return hasPassed;
	}
	
	/**
	 * @return true if the assessed user has a comment
	 */
	public boolean hasComment() {
		return hasComment;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	private void exposeConfigToVC(ModuleConfiguration config) {
	    myContent.contextPut(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD, config.get(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD));
	    myContent.contextPut(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD, config.get(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD));
	    myContent.contextPut(MSCourseNode.CONFIG_KEY_HAS_COMMENT_FIELD, config.get(MSCourseNode.CONFIG_KEY_HAS_COMMENT_FIELD));
	    String infoTextUser = (String) config.get(MSCourseNode.CONFIG_KEY_INFOTEXT_USER);
	    if(StringHelper.containsNonWhitespace(infoTextUser)) {
	    	myContent.contextPut(MSCourseNode.CONFIG_KEY_INFOTEXT_USER, infoTextUser);
	    }
	    myContent.contextPut(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE, AssessmentHelper.getRoundedScore((Float)config.get(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE)));
	    myContent.contextPut(MSCourseNode.CONFIG_KEY_SCORE_MIN, AssessmentHelper.getRoundedScore((Float)config.get(MSCourseNode.CONFIG_KEY_SCORE_MIN)));
	    myContent.contextPut(MSCourseNode.CONFIG_KEY_SCORE_MAX, AssessmentHelper.getRoundedScore((Float)config.get(MSCourseNode.CONFIG_KEY_SCORE_MAX)));
	}
	
	private void exposeUserDataToVC(UserRequest ureq, UserCourseEnvironment userCourseEnv, PersistentAssessableCourseNode courseNode) {
		AssessmentEntry assessmentEntry = courseNode.getUserAssessmentEntry(userCourseEnv);
		if(assessmentEntry == null) {
			myContent.contextPut("hasPassedValue", Boolean.FALSE);
			myContent.contextPut("passed", Boolean.FALSE);
			hasPassed = hasScore = hasComment = false;
		} else {
			String rawComment = assessmentEntry.getComment();
			hasPassed = assessmentEntry.getPassed() != null;
			hasScore = assessmentEntry.getScore() != null;
			hasComment = courseNode.hasCommentConfigured() && StringHelper.containsNonWhitespace(rawComment);
		
			boolean resultsVisible = overrideUserResultsVisiblity
					|| assessmentEntry.getUserVisibility() == null
					|| assessmentEntry.getUserVisibility().booleanValue();
			myContent.contextPut("resultsVisible", resultsVisible);
			myContent.contextPut("score", AssessmentHelper.getRoundedScore(assessmentEntry.getScore()));
			myContent.contextPut("hasPassedValue", (assessmentEntry.getPassed() == null ? Boolean.FALSE : Boolean.TRUE));
			myContent.contextPut("passed", assessmentEntry.getPassed());
			
			if(resultsVisible) {
				if(hasComment) {
					StringBuilder comment = Formatter.stripTabsAndReturns(rawComment);
					myContent.contextPut("comment", StringHelper.xssScan(comment));
				}
				
				if(courseNode.hasIndividualAsssessmentDocuments()) {
					List<File> docs = courseNode.getIndividualAssessmentDocuments(userCourseEnv);
					String mapperUri = registerCacheableMapper(ureq, null, new DocumentsMapper(docs));
					myContent.contextPut("docsMapperUri", mapperUri);
					myContent.contextPut("docs", docs);
				}
			}
		}

		if(showLog) {
			UserNodeAuditManager am = userCourseEnv.getCourseEnvironment().getAuditManager();
			myContent.contextPut("log", am.getUserNodeLog(courseNode, userCourseEnv.getIdentityEnvironment().getIdentity()));
		}
	}
	
	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
		// do nothing here yet
	}
}
