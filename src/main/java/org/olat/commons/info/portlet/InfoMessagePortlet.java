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
package org.olat.commons.info.portlet;

import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.portal.AbstractPortlet;
import org.olat.core.gui.control.generic.portal.Portlet;
import org.olat.core.gui.control.generic.portal.PortletToolController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

/**
 * 
 * Description:<br>
 * TODO: srosse Class Description for InfoMessagePortlet
 * 
 * <P>
 * Initial Date:  27 juil. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InfoMessagePortlet extends AbstractPortlet {

	private InfoMessagePortletRunController runCtrl;

	@Override
	public String getTitle() {
		return getTranslator().translate("portlet.title");
	}

	@Override
	public String getDescription() {
		return getTranslator().translate("portlet.title");
	}

	@Override
	public Portlet createInstance(WindowControl wControl, UserRequest ureq, Map<String,String> portletConfig) {
		Translator translator = Util.createPackageTranslator(InfoMessagePortlet.class, ureq.getLocale());
		Portlet p = new InfoMessagePortlet();
		p.setName(getName());			
		p.setTranslator(translator);
		return p;
	}

	@Override
	public Component getInitialRunComponent(WindowControl wControl, UserRequest ureq) {
		if(runCtrl != null) runCtrl.dispose();
		runCtrl = new InfoMessagePortletRunController(wControl, ureq, getTranslator(), getName());
		return runCtrl.getInitialComponent();
	}
	
	@Override
	public void dispose() {
		disposeRunComponent();
	}

	@Override
	public void disposeRunComponent() {
		if (this.runCtrl != null) {
			this.runCtrl.dispose();
			this.runCtrl = null;
		}
	}

	@Override
	public String getCssClass() {
		return "o_portlet_infomessages";
	}

	@Override
	public PortletToolController<InfoSubscriptionItem> getTools(UserRequest ureq, WindowControl wControl) {
		if (runCtrl == null ) {
			runCtrl = new InfoMessagePortletRunController(wControl, ureq, getTranslator(), getName());
		}
	  return runCtrl.createSortingTool(ureq, wControl);
	}
}
