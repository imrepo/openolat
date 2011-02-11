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

package org.olat.admin.securitygroup.gui;

import java.util.List;

import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;

/**
 * Description:<BR>
 * Event fired when identities should be removed from a security group
 * <P>
 * Initial Date:  06.12.2006
 *
 * @author Ch.Guretzki
 */
public class IdentitiesRemoveEvent extends Event {

	private List<Identity> removedIdentities;
	
	/**
	 * @param identity the removed identity
	 */
	public IdentitiesRemoveEvent(List<Identity> identities) {
		super("identities_removed");
		this.removedIdentities = identities;
	}
	
	/**
	 * @return Identity the removed identity
	 */
	public List<Identity> getRemovedIdentities() {
		return this.removedIdentities;
	}

}
