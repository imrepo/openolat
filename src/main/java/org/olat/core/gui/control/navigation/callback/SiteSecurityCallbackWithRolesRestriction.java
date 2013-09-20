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
package org.olat.core.gui.control.navigation.callback;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.navigation.SiteSecurityCallback;
import org.olat.core.id.Roles;

/**
 * 
 * Initial date: 18.09.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SiteSecurityCallbackWithRolesRestriction implements SiteSecurityCallback {
	private String limitToRole;
	
	@Override
	public boolean isAllowedToViewSite(UserRequest ureq) {
		return isAllowed(ureq);
	}

	@Override
	public boolean isAllowedToLaunchSite(UserRequest ureq) {
		return isAllowed(ureq);
	}

	/**
	 * @see org.olat.core.gui.control.navigation.SiteDefinition#createSite(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl)
	 */
	private boolean isAllowed(UserRequest ureq) {
		if (limitToRole != null) {
			String theRole = limitToRole.toLowerCase();
			Roles roles = ureq.getUserSession().getRoles();
			if(roles == null || roles.isInvitee() || roles.isGuestOnly()) {
				return false;
			} else if (theRole.equals("administrator") && !roles.isOLATAdmin()) {
				return false;
			} else if (theRole.equals("groupmanager") && !roles.isGroupManager()) {
				return false;
			} else if (theRole.equals("usermanager") && !roles.isUserManager()) {
				return false;
			} else if (theRole.equals("author") && !roles.isAuthor()) {
				return false;
			} else if (theRole.equals("pooladmin") && !roles.isPoolAdmin()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * spring method to limit the visibility of the site tab
	 * @param limitToRoleConfig
	 */
	public void setLimitToRole(String limitToRoleConfig) {
		limitToRole = limitToRoleConfig;
	}
}
