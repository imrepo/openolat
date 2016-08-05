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
package org.olat.selenium.page.portfolio;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 14.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PortfolioV2Page {
	
	public static final By tocBy = By.cssSelector("div.o_portfolio_toc");

	private final WebDriver browser;

	public PortfolioV2Page(WebDriver browser) {
		this.browser = browser;
	}
	
	/*
	  .openEditor()
	 .selectMapInEditor()
			.selectFirstPageInEditor()
			.setPage(pageTitle, "With a little description")
			.createStructureElement(structureElementTitle, "Structure description")
			.closeEditor()
			.assertStructure(structureElementTitle);
	 */

}
