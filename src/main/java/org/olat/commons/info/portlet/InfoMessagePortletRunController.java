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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.NewControllerFactory;
import org.olat.commons.info.manager.InfoMessageFrontendManager;
import org.olat.commons.info.model.InfoMessage;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.BaseTableDataModelWithoutFilter;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.portal.AbstractPortletRunController;
import org.olat.core.gui.control.generic.portal.PortletDefaultTableDataModel;
import org.olat.core.gui.control.generic.portal.PortletEntry;
import org.olat.core.gui.control.generic.portal.PortletToolSortingControllerImpl;
import org.olat.core.gui.control.generic.portal.SortingCriteria;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.notifications.NotificationsManager;
import org.olat.core.util.notifications.SubscriptionInfo;
import org.olat.core.util.notifications.items.SubscriptionListItem;

/**
 * 
 * Description:<br>
 * Show the last five infos 
 * 
 * <P>
 * Initial Date:  27 juil. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InfoMessagePortletRunController extends AbstractPortletRunController<InfoSubscriptionItem> implements GenericEventListener {
	
	private Link showAllLink;
	private TableController tableController;
	private VelocityContainer portletVC;
	
	public InfoMessagePortletRunController(WindowControl wControl, UserRequest ureq, Translator trans, String portletName) {
		super(wControl, ureq, trans, portletName);
		
		portletVC =  createVelocityContainer("infosPortlet");
		showAllLink = LinkFactory.createLink("portlet.showall", portletVC, this);

		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("portlet.no_messages"));
		tableConfig.setDisplayTableHeader(false);
		tableConfig.setCustomCssClass("b_portlet_table");
		tableConfig.setDisplayRowCount(false);
		tableConfig.setPageingEnabled(false);
		tableConfig.setDownloadOffered(false);
		tableConfig.setSortingEnabled(false);
		
		removeAsListenerAndDispose(tableController);
		tableController = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		tableController.addColumnDescriptor(new CustomRenderColumnDescriptor("peekview.title", 0,
				null, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, new InfoNodeRenderer()));
		
		listenTo(tableController);
		
		sortingTermsList.add(SortingCriteria.DATE_SORTING);
		sortingCriteria = getPersistentSortingConfiguration(ureq);
		sortingCriteria.setSortingTerm(SortingCriteria.DATE_SORTING);
		reloadModel(sortingCriteria);
		
		portletVC.put("table", tableController.getInitialComponent());
		
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, getIdentity(), InfoMessageFrontendManager.oresFrontend);

		putInitialPanel(portletVC);
	}

	@Override
	protected SortingCriteria createDefaultSortingCriteria() {
		SortingCriteria sortingCriteria = new SortingCriteria(this.sortingTermsList);
		sortingCriteria.setAscending(false);
		return sortingCriteria;
	}

	@Override
	public synchronized void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, InfoMessageFrontendManager.oresFrontend);
		super.doDispose();
	}

	@Override
	public void event(Event event) {
		if("new_info_message".equals(event.getCommand())) {
			reloadModel(sortingCriteria);
		}
	}

	@Override
	protected Comparator<InfoSubscriptionItem> getComparator(SortingCriteria criteria) {
		return new InfoPortletEntryComparator(criteria);
	}
	
	/**
	 * 
	 * @param items
	 * @return
	 */
	private List<PortletEntry<InfoSubscriptionItem>> convertToPortletEntryList(List<InfoSubscriptionItem> infos) {
		List<PortletEntry<InfoSubscriptionItem>> convertedList = new ArrayList<PortletEntry<InfoSubscriptionItem>>();
		long i = 0;
		for(InfoSubscriptionItem info:infos) {
			convertedList.add(new InfoPortletEntry(i++, info));
		}
		return convertedList;
	}

	@Override
	protected void reloadModel(SortingCriteria criteria) {
		List<SubscriptionInfo> infos = NotificationsManager.getInstance().getSubscriptionInfos(getIdentity(), "InfoMessage");
		List<InfoSubscriptionItem> items = new ArrayList<InfoSubscriptionItem>();
		for(SubscriptionInfo info:infos) {
			for(SubscriptionListItem item:info.getSubscriptionListItems()) {
				items.add(new InfoSubscriptionItem(info, item));
			}
		}
		items = getSortedList(items, criteria);
		List<PortletEntry<InfoSubscriptionItem>> entries = convertToPortletEntryList(items);
		InfosTableModel model = new InfosTableModel(entries);
		tableController.setTableDataModel(model);
	}

	@Override
	protected void reloadModel(List<PortletEntry<InfoSubscriptionItem>> sortedItems) {
		InfosTableModel model = new InfosTableModel(sortedItems);
		tableController.setTableDataModel(model);
	}
	
	protected PortletToolSortingControllerImpl<InfoSubscriptionItem> createSortingTool(UserRequest ureq, WindowControl wControl) {
		if(portletToolsController==null) {
			final List<PortletEntry<InfoSubscriptionItem>> empty = Collections.<PortletEntry<InfoSubscriptionItem>>emptyList();
			final PortletDefaultTableDataModel<InfoSubscriptionItem> defaultModel = new PortletDefaultTableDataModel<InfoSubscriptionItem>(empty, 2) {
				@Override
				public Object getValueAt(int row, int col) {
					return null;
				}
			};
			portletToolsController = new PortletToolSortingControllerImpl<InfoSubscriptionItem>(ureq, wControl, getTranslator(), sortingCriteria, defaultModel, empty);
			portletToolsController.setConfigManualSorting(false);
			portletToolsController.setConfigAutoSorting(true);
			portletToolsController.addControllerListener(this);
		}		
		return portletToolsController;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == showAllLink) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.MONTH, -1);
			// fxdiff activate homes tab in top navigation and activate the correct
			// menu item
			String resourceUrl = "[HomeSite:" + ureq.getIdentity().getKey() + "][notifications:0][type=" + InfoMessage.class.getSimpleName()
					+ ":0]" + BusinessControlFactory.getInstance().getContextEntryStringForDate(cal.getTime());
			BusinessControl bc = BusinessControlFactory.getInstance().createFromString(resourceUrl);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
			NewControllerFactory.getInstance().launch(ureq, bwControl);
		}
	}
	
	public class InfosTableModel extends BaseTableDataModelWithoutFilter<PortletEntry<InfoSubscriptionItem>> {
		private final List<PortletEntry<InfoSubscriptionItem>> infos;
		
		public InfosTableModel(List<PortletEntry<InfoSubscriptionItem>> infos) {
			this.infos = infos;
		}

		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public int getRowCount() {
			return infos.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			InfoPortletEntry entry = (InfoPortletEntry)infos.get(row);
			switch(col) {
				case 0: return entry.getValue();
				default: return entry;
			}
		}
	}
	
	public class InfoNodeRenderer implements CustomCellRenderer {
		
		@Override
		public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
			if(val instanceof InfoSubscriptionItem) {
				InfoSubscriptionItem isi = (InfoSubscriptionItem)val;
				SubscriptionListItem item = isi.getItem();
				SubscriptionInfo info = isi.getInfo();
				//title
				String title = info.getTitle(SubscriptionInfo.MIME_PLAIN);
				
				String tip = null;
				boolean tooltip = StringHelper.containsNonWhitespace(item.getDescriptionTooltip());
				if(tooltip) {
					StringBuilder tipSb = new StringBuilder();
					tipSb.append("<b>").append(title).append(":</b>")
						.append("<br/>")
						.append(Formatter.escWithBR(Formatter.truncate(item.getDescriptionTooltip(), 256)));
					tip = StringEscapeUtils.escapeHtml(tipSb.toString());
					sb.append("<span ext:qtip=\"").append(tip).append("\">");
				} else {
					sb.append("<span>");
				}
				sb.append(Formatter.truncate(title, 30)).append("</span>&nbsp;");
				//link
				String infoTitle = Formatter.truncate(item.getDescription(), 30);
				sb.append("<a href=\"").append(item.getLink()).append("\" class=\"o_portlet_infomessage_link\"");
				if(tooltip) {
					sb.append("ext:qtip=\"").append(tip).append("\"");
				}
				sb.append(">")
					.append(infoTitle)
					.append("</a>");
			} else {
				sb.append("-");
			}
		}
	}
}
