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
package org.olat.course.site;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.navigation.SiteDefinitions;
import org.olat.core.gui.control.navigation.SiteSecurityCallback;
import org.olat.course.site.model.CourseSiteConfiguration;
import org.olat.course.site.model.LanguageConfiguration;

/**
 * 
 * <h3>Description:</h3>
 * <p>Hack to give a second course site an other classname for the resume function
 * <p>
 * Initial Date:  14 jan. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class CourseSiteDef2 extends CourseSiteDef {

	@Override
	protected CourseSiteConfiguration getCourseSiteconfiguration() {
		SiteDefinitions siteModule = CoreSpringFactory.getImpl(SiteDefinitions.class);
		CourseSiteConfiguration config = siteModule.getConfigurationCourseSite2();
		return config;
	}

	@Override
	protected CourseSite createCourseSiteInstance(UserRequest ureq, LanguageConfiguration langConfig,
			boolean showToolController, SiteSecurityCallback siteSecCallback, String icon) {
		return new CourseSite2(this, ureq.getLocale(), langConfig.getRepoSoftKey(), showToolController,
				siteSecCallback, langConfig.getTitle(), icon);
	}
}
