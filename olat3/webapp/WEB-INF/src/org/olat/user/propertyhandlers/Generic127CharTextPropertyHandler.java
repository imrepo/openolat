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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 2007 frentix GmbH, Switzerland<br>
 * <p>
 */
package org.olat.user.propertyhandlers;

import java.util.Locale;
import java.util.Map;

import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.formelements.FormElement;
import org.olat.core.gui.formelements.TextElement;
import org.olat.core.id.User;
import org.olat.user.AbstractUserPropertyHandler;
import org.olat.user.UserManager;

/**
 * <h3>Description:</h3>
 * This generic text property provides a userfield that has a maximum of 127
 * characters length. It can contain any string, the only validity check that is
 * performed is the not empty check if the property is declared as mandatory.
 * <p>
 * Create subclass of this class if you allow only a certain range of entered
 * data. See EmailProperty as an example.
 * <p>
 * Initial Date: 27.07.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class Generic127CharTextPropertyHandler extends AbstractUserPropertyHandler {
	
	/**
	 * @see org.olat.user.propertyhandlers.UserPropertyHandler#updateUserFromFormElement(org.olat.core.id.User, org.olat.core.gui.formelements.FormElement)
	 */
	public void updateUserFromFormElement(User user, FormElement ui) {
		String internalValue = getStringValue(ui);
		setInternalValue(user, internalValue);
	}

	/**
	 * @see org.olat.user.propertyhandlers.UserPropertyHandler#updateUserFromFormItem(org.olat.core.id.User, org.olat.core.gui.components.form.flexible.FormItem)
	 */
	public void updateUserFromFormItem(User user, FormItem formItem) {
		String internalValue = getStringValue(formItem);
		setInternalValue(user, internalValue);
	}


	/**
	 * @see org.olat.user.propertyhandlers.UserPropertyHandler#getStringValue(org.olat.core.gui.formelements.FormElement)
	 */	
	public String getStringValue(FormElement ui) {
		return ((TextElement) ui).getValue();
	}

	/**
	 * @see org.olat.user.propertyhandlers.UserPropertyHandler#getStringValue(org.olat.core.gui.formelements.FormElement)
	 */
	public String getStringValue(FormItem formItem) {
		return ((org.olat.core.gui.components.form.flexible.elements.TextElement) formItem).getValue();
	}

	/**
	 * @see org.olat.user.propertyhandlers.UserPropertyHandler#getStringValue(java.lang.String, java.util.Locale)
	 */
	public String getStringValue(String displayValue, Locale locale) {
		// default implementation is to use same as displayValue
		return displayValue;
	}
	
	/**
	 * @see org.olat.user.propertyhandlers.UserPropertyHandler#getFormElement(java.util.Locale, org.olat.core.id.User, java.lang.String, boolean)
	 */
	public FormElement getFormElement(Locale locale, User user, String usageIdentifyer, boolean isAdministrativeUser) {
		// default is no element
		TextElement ui = null;
		UserManager um = UserManager.getInstance();
		ui = new TextElement(i18nFormElementLabelKey(), getInternalValue(user), 127);
		if ( um.isUserViewReadOnly(usageIdentifyer, this) && ! isAdministrativeUser) {
			ui.setReadOnly(true);
		}
		if (um.isMandatoryUserProperty(usageIdentifyer, this)) {
			ui.setMandatory(true);
		}
		return ui;
	}
	
	/**
	 *  
	 * @see org.olat.user.propertyhandlers.UserPropertyHandler#addFormItem(java.util.Locale, org.olat.core.id.User, java.lang.String, boolean, org.olat.core.gui.components.form.flexible.FormItemContainer)
	 */
	public FormItem addFormItem(Locale locale, User user, String usageIdentifyer, boolean isAdministrativeUser,	FormItemContainer formItemContainer) {
		org.olat.core.gui.components.form.flexible.elements.TextElement tElem = null;
		tElem = FormUIFactory.getInstance().addTextElement(getName(), i18nFormElementLabelKey(), 127, getInternalValue(user), formItemContainer);
		tElem.setItemValidatorProvider(this);
		tElem.setLabel(i18nFormElementLabelKey(), null);
		UserManager um = UserManager.getInstance();
		if ( um.isUserViewReadOnly(usageIdentifyer, this) && ! isAdministrativeUser) {
			tElem.setEnabled(false);
		}
		if (um.isMandatoryUserProperty(usageIdentifyer, this)) {
			tElem.setMandatory(true);
		}
		return tElem;
	}
	

	/**
	 * @see org.olat.user.propertyhandlers.UserPropertyHandler#updateFormElementFromUser(org.olat.core.gui.formelements.FormElement, org.olat.core.id.User)
	 */
	public void updateFormElementFromUser(FormElement ui, User user) {
		((TextElement) ui).setValue(getInternalValue(user));
	}

	/**
	 * @see org.olat.user.propertyhandlers.UserPropertyHandler#isValid(org.olat.core.gui.formelements.FormElement)
	 */
	public boolean isValid(FormElement ui, Map formContext) {
		TextElement uiText = (TextElement) ui;
		if (ui.isMandatory()) {
			return uiText.notEmpty(i18nFormElementLabelKey()+ ".error.empty");			
		}
		return true;
	}
	
	/**
	 * @see org.olat.user.propertyhandlers.UserPropertyHandler#isValid(org.olat.core.gui.components.form.flexible.FormItem, java.util.Map)
	 */
	public boolean isValid(FormItem formItem, Map formContext) {
		org.olat.core.gui.components.form.flexible.elements.TextElement textElemItem = (org.olat.core.gui.components.form.flexible.elements.TextElement) formItem;
		if (textElemItem.isMandatory()) {
			return ! textElemItem.isEmpty("new.form.mandatory");
		} 
		return true;
	}

	
	/**
	 * @see org.olat.user.propertyhandlers.UserPropertyHandler#isValidValue(java.lang.String, org.olat.core.gui.components.form.ValidationError, java.util.Locale)
	 */
	public boolean isValidValue(String value, ValidationError validationError, Locale locale) {
		if (value != null && value.length() > 127) {
			validationError.setErrorKey("general.error.max.127");
			return false;
		}
		return true;
	}

	
}
