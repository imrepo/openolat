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
package org.olat.modules.taxonomy.ui;

import org.olat.core.commons.modules.singlepage.SinglePageController;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.manager.TaxonomyDocumentsLibraryNotificationsHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyRootController extends BasicController {
	
	private VelocityContainer mainVC;
	
	private SinglePageController indexCtrl;
	
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private TaxonomyDocumentsLibraryNotificationsHandler notificationsHandler;
	
	public TaxonomyRootController(UserRequest ureq, WindowControl wControl, Taxonomy taxonomy) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("taxonomy_root");
		mainVC.contextPut("displayName", StringHelper.escapeHtml(taxonomy.getDisplayName()));
		mainVC.contextPut("identifier", StringHelper.escapeHtml(taxonomy.getIdentifier()));
		
		//add subscription
		SubscriptionContext subsContext = notificationsHandler.getTaxonomyDocumentsLibrarySubscriptionContext(taxonomy);
		PublisherData data = notificationsHandler.getTaxonomyDocumentsLibraryPublisherData(taxonomy);
		ContextualSubscriptionController csController = new ContextualSubscriptionController(ureq, getWindowControl(), subsContext, data);
		listenTo(csController);
		mainVC.put("subscription", csController.getInitialComponent());
		
		VFSContainer container = taxonomyService.getTaxonomyInfoPageContainer(taxonomy);
		indexCtrl = new SinglePageController(ureq, this.getWindowControl(), container, "index.html", false);
		listenTo(indexCtrl);
		mainVC.put("index", indexCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
