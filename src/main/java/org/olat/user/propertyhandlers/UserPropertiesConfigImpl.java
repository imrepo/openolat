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
package org.olat.user.propertyhandlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.olat.core.configuration.Initializable;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.LogModule;
import org.olat.user.UserPropertiesConfig;

/**
 * <h3>Description:</h3>
 * This class implements the user properties configuration 
 * <p>
 * Initial Date: 31.07.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class UserPropertiesConfigImpl implements UserPropertiesConfig, Initializable {
	private static final OLog log = Tracing.createLoggerFor(UserPropertiesConfigImpl.class);
	private static final String USER_PROPERTY_LOG_CONFIGURATION = "userPropertyLogConfiguration";
	public static final String PACKAGE = UserPropertiesConfigImpl.class.getPackage().getName(); 

	
	private Map<String, UserPropertyHandler> userPropertyNameLookupMap;
	private ConcurrentMap<String, List<UserPropertyHandler>> userPropertyUsageContextsLookupMap = new ConcurrentHashMap<String, List<UserPropertyHandler>>();
	
	private List<UserPropertyHandler> userPropertyHandlers;
	private Map<String, UserPropertyUsageContext> userPropertyUsageContexts;

	private int maxNumOfInterests;

	@Override
	public void init() {
		List<UserPropertyHandler> userPropHandlers = getUserPropertyHandlersFor(USER_PROPERTY_LOG_CONFIGURATION, false);
		Set<String> userProperties = new LinkedHashSet<String>();
		for (Iterator<UserPropertyHandler> iterator = userPropHandlers.iterator(); iterator.hasNext();) {
			userProperties.add(iterator.next().getName());
		}
		LogModule.setUserProperties(userProperties);
	}

	@Override
	public int getMaxNumOfInterests() {
		return maxNumOfInterests;
	}

	/**
	 * [used by Spring]
	 * @param maxNumOfInterests
	 */
	public void setMaxNumOfInterests(int maxNumOfInterests) {
		this.maxNumOfInterests = maxNumOfInterests;
	}

	/**
	 * Spring setter
	 * @param userPropertyUsageContexts
	 */
	public void setUserPropertyUsageContexts(Map<String,UserPropertyUsageContext> userPropertyUsageContexts) {
		this.userPropertyUsageContexts = userPropertyUsageContexts;
	}

	@Override
	public Map<String,UserPropertyUsageContext> getUserPropertyUsageContexts(){
		return userPropertyUsageContexts;
	}

	/**
	 * Spring setter
	 * @param userPropertyHandlers
	 */
	@Override
	public void setUserPropertyHandlers(List<UserPropertyHandler> userPropertyHandlers) {
		this.userPropertyHandlers = userPropertyHandlers;
		// populate name lookup map for faster lookup service
		userPropertyNameLookupMap = new HashMap<String, UserPropertyHandler>(userPropertyHandlers.size());
		for (UserPropertyHandler propertyHandler : userPropertyHandlers) {
			String name = propertyHandler.getName();
			userPropertyNameLookupMap.put(name, propertyHandler);
		}
	}

	/**
	 * 
	 * @see org.olat.user.UserPropertiesConfig#getPropertyHandler(java.lang.String)
	 */
	@Override
	public UserPropertyHandler getPropertyHandler(String handlerName) {
		UserPropertyHandler handler =  userPropertyNameLookupMap.get(handlerName);
		if (handler == null && log.isDebug()) {
			log.debug("UserPropertyHander for handlerName::" + handlerName + " not found, check your configuration.", null);
		}
		return handler;
	}

	/**
	 * @see org.olat.user.UserPropertiesConfig#getTranslator(org.olat.core.gui.translator.Translator)
	 */
	@Override
	public Translator getTranslator(Translator fallBack) {
		return new PackageTranslator(PACKAGE, fallBack.getLocale(), fallBack); 
	}

	/**
	 * @see org.olat.user.UserPropertiesConfig#getAllUserPropertyHandlers()
	 */
	@Override
	public List<UserPropertyHandler> getAllUserPropertyHandlers() {
		return userPropertyHandlers;
	}

	/**
	 * @see org.olat.user.UserPropertiesConfig#getUserPropertyHandlersFor(java.lang.String, boolean)
	 */
	@Override
	public List<UserPropertyHandler> getUserPropertyHandlersFor(String usageIdentifyer, boolean isAdministrativeUser) {
		String key = usageIdentifyer + "_" + isAdministrativeUser;
		List<UserPropertyHandler> currentUsageHandlers = userPropertyUsageContextsLookupMap.get(key);
		if (currentUsageHandlers == null) {
			List<UserPropertyHandler> newUsageHandlers = new ArrayList<>();
			UserPropertyUsageContext currentUsageConfig = getCurrentUsageConfig(usageIdentifyer);			
			// add all handlers that are accessible for this user
			for (UserPropertyHandler propertyHandler : currentUsageConfig.getPropertyHandlers()) {
				// if configured for this class and if isAdministrativeUser
				if (currentUsageConfig.isForAdministrativeUserOnly(propertyHandler) && !isAdministrativeUser) {
					// don't add this handler for this user
					continue;
				}
				newUsageHandlers.add(propertyHandler);								
			}
			
			currentUsageHandlers = userPropertyUsageContextsLookupMap.putIfAbsent(key, newUsageHandlers);
			if(currentUsageHandlers == null) {
				currentUsageHandlers = newUsageHandlers;
			}
		}
		return currentUsageHandlers;
	}

	/**
	 * @see org.olat.user.UserPropertiesConfig#isMandatoryUserProperty(java.lang.String, org.olat.user.propertyhandlers.UserPropertyHandler)
	 */
	@Override
	public boolean isMandatoryUserProperty(String usageIdentifyer, UserPropertyHandler propertyHandler) {
		UserPropertyUsageContext currentUsageConfig = getCurrentUsageConfig(usageIdentifyer);
		return currentUsageConfig.isMandatoryUserProperty(propertyHandler);
	}

	/**
	 * @see org.olat.user.UserPropertiesConfig#isUserViewReadOnly(java.lang.String, org.olat.user.propertyhandlers.UserPropertyHandler)
	 */
	@Override
	public boolean isUserViewReadOnly(String usageIdentifyer, UserPropertyHandler propertyHandler) {
		UserPropertyUsageContext currentUsageConfig = getCurrentUsageConfig(usageIdentifyer);
		return currentUsageConfig.isUserViewReadOnly(propertyHandler);
	}

	/**
	 * Internal helper to get the usage configuration for this identifyer
	 * @param usageIdentifyer
	 * @return
	 */
	private UserPropertyUsageContext getCurrentUsageConfig(String usageIdentifyer) {
		UserPropertyUsageContext currentUsageConfig = userPropertyUsageContexts.get(usageIdentifyer);
		if (currentUsageConfig == null) {
			currentUsageConfig = userPropertyUsageContexts.get("default");
			log.warn(
					"Could not find user property usage configuration for usageIdentifyer::" + usageIdentifyer
							+ ", please check yout olat_userconfig.xml file. Using default configuration instead.", null);
			if (currentUsageConfig == null) {
				throw new OLATRuntimeException("Missing default user property usage configuration in olat_userconfig.xml", null);
			}
		}
		return currentUsageConfig;
	}
}
