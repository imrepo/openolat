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
package org.olat.user;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.persistence.TypedQuery;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.IdentityShort;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.Preferences;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.AssertException;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.mail.MailHelper;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <h3>Description:</h3>
 * This implementation of the user manager manipulates user objects based on a
 * hibernate implementation
 * <p>
 * Initial Date: 31.07.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class UserManagerImpl extends UserManager {
  // used to save user data in the properties table 
  private static final String CHARSET = "charset";
  private UserDisplayNameCreator userDisplayNameCreator;
  
  @Autowired
  private DB dbInstance;
  @Autowired
  private BaseSecurity securityManager;
  @Autowired
  private CoordinatorManager coordinatorManager;

	private CacheWrapper<Serializable,String> usernameCache;
  
	/**
	 * Use UserManager.getInstance(), this is a spring factory method to load the
	 * correct user manager
	 */
	private UserManagerImpl() {
		INSTANCE = this;
	}
	
	@PostConstruct
	public void init() {
		usernameCache = coordinatorManager.getCoordinator().getCacher()
				.getCache(UserManager.class.getSimpleName(), "username");
	}

	/**
	 * @see org.olat.user.UserManager#createUser(java.lang.String, java.lang.String, java.lang.String)
	 */
	public User createUser(String firstName, String lastName, String eMail) {
		User newUser = new UserImpl(firstName, lastName, eMail);
		Preferences prefs = newUser.getPreferences();
		
		Locale loc;
		// for junit test case: use German Locale
		if (Settings.isJUnitTest()) { 
			loc = Locale.GERMAN;
		} else {
			loc = I18nModule.getDefaultLocale();
		}
		//Locale loc
		prefs.setLanguage(loc.toString());
		prefs.setFontsize("normal");
		prefs.setPresenceMessagesPublic(false);
		prefs.setInformSessionTimeout(false);
		return newUser;
	}

	/**
	 * @see org.olat.user.UserManager#createAndPersistUser(java.lang.String, java.lang.String, java.lang.String)
	 */
	public User createAndPersistUser(String firstName, String lastName, String email) {
		User user = new UserImpl(firstName, lastName, email);
		dbInstance.getCurrentEntityManager().persist(user);
		return user;
	}
	
	// fxdiff: check also for emails in change-workflow
	public boolean isEmailInUse(String email) {
		DB db = DBFactory.getInstance();
		String[] emailProperties = {UserConstants.EMAIL, UserConstants.INSTITUTIONALEMAIL};
		for(String emailProperty:emailProperties) {
			StringBuilder sb = new StringBuilder();
			sb.append("select count(user) from org.olat.core.id.User user where ")
				.append("user.properties['")
				.append(emailProperty)
				.append("']=:email_value");
			
			String query = sb.toString();
			DBQuery dbq = db.createQuery(query);
			dbq.setString("email_value", email);
			Number countEmail = (Number)dbq.uniqueResult();
			if(countEmail.intValue() > 0) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public List<Long> findUserKeyWithProperty(String propName, String propValue) {
		StringBuilder sb = new StringBuilder("select user.key from ").append(UserImpl.class.getName()).append(" user ")
			.append(" where user.properties['").append(propName).append("'] =:propValue");

		List<Long> userKeys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("propValue", propValue).getResultList();
		return userKeys;
	}
	
	@Override
	public Identity findIdentityKeyWithProperty(String propName, String propValue) {
		StringBuilder sb = new StringBuilder("select identity from ").append(IdentityImpl.class.getName()).append(" identity ")
			.append(" inner join identity.user user ")
			.append(" where user.properties['").append(propName).append("'] =:propValue");

		List<Identity> userKeys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("propValue", propValue).getResultList();
		if(userKeys.isEmpty()) {
			return null;
		}
		return userKeys.get(0);
	}

	/**
	 * @see org.olat.user.UserManager#findIdentityByEmail(java.lang.String)
	 */
	public Identity findIdentityByEmail(String email) {
		if (!MailHelper.isValidEmailAddress(email)) {
			throw new AssertException("Identity cannot be searched by email, if email is not valid. Used address: " + email);
		}

		StringBuilder sb = new StringBuilder("select identity from ").append(IdentityImpl.class.getName()).append(" identity ")
			.append(" inner join identity.user user ")
			.append(" where ");
		
		boolean mysql = "mysql".equals(dbInstance.getDbVendor());
		//search email
		StringBuilder emailSb = new StringBuilder(sb);
		if(mysql) {
			emailSb.append(" user.properties['").append(UserConstants.EMAIL).append("'] =:email");
		} else {
			emailSb.append(" lower(user.properties['").append(UserConstants.EMAIL).append("']) = lower(:email)");
		}

		List<Identity> identities = dbInstance.getCurrentEntityManager()
				.createQuery(emailSb.toString(), Identity.class)
				.setParameter("email", email).getResultList();
		if (identities.size() > 1) {
			throw new AssertException("more than one identity found with email::" + email);
		}

		//search institutional email
		StringBuilder institutionalSb = new StringBuilder(sb);
		if(mysql) {
			institutionalSb.append(" user.properties['").append(UserConstants.INSTITUTIONALEMAIL).append("'] =:email");
		} else {
			institutionalSb.append(" lower(user.properties['").append(UserConstants.INSTITUTIONALEMAIL).append("']) = lower(:email)");
		}
		List<Identity> instIdentities = dbInstance.getCurrentEntityManager()
				.createQuery(institutionalSb.toString(), Identity.class)
				.setParameter("email", email).getResultList();
		if (instIdentities.size() > 1) {
			throw new AssertException("more than one identity found with institutional-email::" + email);
		}

		// check if email found in both fields && identity is not the same
		if ( (identities.size() > 0) && (instIdentities.size() > 0) && 
				 ( identities.get(0) != instIdentities.get(0) ) ) {
			throw new AssertException("found two identites with same email::" + email + " identity1=" + identities.get(0) + " identity2=" + instIdentities.get(0));
		}
		if (identities.size() == 1) {
			return identities.get(0);
		}
		if (instIdentities.size() == 1) {
			return instIdentities.get(0);
		}
		return null;
	}
	
	@Override
	public List<Identity> findIdentitiesByEmail(List<String> emailList) {
		List<String> emails = new ArrayList<String>(emailList);
		for(Iterator<String> emailIt=emails.iterator(); emailIt.hasNext(); ) {
			String email = emailIt.next();
			if (!MailHelper.isValidEmailAddress(email)) {
				emailIt.remove();
				logWarn("Invalid email address: " + email, null);
			}
		}
		
		if(emails.isEmpty()) {
			return Collections.emptyList();
		}
		StringBuilder sb = new StringBuilder("select identity from ").append(IdentityImpl.class.getName()).append(" identity ")
			.append(" inner join identity.user user ")
			.append(" where ");
		
		boolean mysql = "mysql".equals(dbInstance.getDbVendor());
		//search email
		StringBuilder emailSb = new StringBuilder(sb);
		if(mysql) {
			emailSb.append(" user.properties['").append(UserConstants.EMAIL).append("']  in (:emails) ");
		} else {
			emailSb.append(" lower(user.properties['").append(UserConstants.EMAIL).append("']) = lower(:emails)");
		}

		List<Identity> identities = dbInstance.getCurrentEntityManager()
				.createQuery(emailSb.toString(), Identity.class)
				.setParameter("emails", emails).getResultList();

		//search institutional email
		StringBuilder institutionalSb = new StringBuilder(sb);
		if(mysql) {
			institutionalSb.append(" user.properties['").append(UserConstants.INSTITUTIONALEMAIL).append("'] in (:emails) ");
		} else {
			institutionalSb.append(" lower(user.properties['").append(UserConstants.INSTITUTIONALEMAIL).append("']) = lower(:emails)");
		}
		if(!identities.isEmpty()) {
			institutionalSb.append(" and identity not in (:identities) ");
		}
		TypedQuery<Identity> institutionalQuery = dbInstance.getCurrentEntityManager()
				.createQuery(institutionalSb.toString(), Identity.class)
				.setParameter("emails", emails);
		if(!identities.isEmpty()) {
			institutionalQuery.setParameter("identities", identities);
		}
		List<Identity> instIdentities = institutionalQuery.getResultList();
		identities.addAll(instIdentities);
		return identities;
	}

	/**
	 * @see org.olat.user.UserManager#findUserByEmail(java.lang.String)
	 */
	public User findUserByEmail(String email) {
		if (isLogDebugEnabled()){
			logDebug("Trying to find user with email '" + email + "'");
		}
		
		Identity ident = findIdentityByEmail(email);
		// if no user found return null
		if (ident == null) {
			if (isLogDebugEnabled()){
				logDebug("Could not find user '" + email + "'");
			}
			return null;
		} 
		return ident.getUser();
	}
	
	public boolean userExist(String email) {
		StringBuilder sb = new StringBuilder("select distinct count(user) from ").append(UserImpl.class.getName()).append(" user where ");
		boolean mysql = "mysql".equals(dbInstance.getDbVendor());
		//search email
		StringBuilder emailSb = new StringBuilder(sb);
		if(mysql) {
			emailSb.append(" user.properties['").append(UserConstants.EMAIL).append("'] =:email");
		} else {
			emailSb.append(" lower(user.properties['").append(UserConstants.EMAIL).append("']) = lower(:email)");
		}
		
		Number count = dbInstance.getCurrentEntityManager()
				.createQuery(emailSb.toString(), Number.class)
				.setParameter("email", email)
				.getSingleResult();
		if(count.intValue() > 0) {
			return true;
		}
		
		//search institutional email
		StringBuilder institutionalSb = new StringBuilder(sb);
		if(mysql) {
			institutionalSb.append(" user.properties['").append(UserConstants.INSTITUTIONALEMAIL).append("'] =:email");
		} else {
			institutionalSb.append(" lower(user.properties['").append(UserConstants.INSTITUTIONALEMAIL).append("']) = lower(:email)");
		}
		count = dbInstance.getCurrentEntityManager()
				.createQuery(institutionalSb.toString(), Number.class)
				.setParameter("email", email)
				.getSingleResult();
		return count.intValue() > 0;
	}

	/**
	 * @see org.olat.user.UserManager#loadUserByKey(java.lang.Long)
	 */
	public User loadUserByKey(Long key) {
		return DBFactory.getInstance().loadObject(UserImpl.class, key);
		// User not loaded yet (lazy initialization). Need to access
		// a field first to really load user from database.
	}

	/**
	 * @see org.olat.user.UserManager#updateUser(org.olat.core.id.User)
	 */
	@Override
	public User updateUser(User usr) {
		if (usr == null) throw new AssertException("User object is null!");
		return dbInstance.getCurrentEntityManager().merge(usr);
	}

	/**
	 * @see org.olat.user.UserManager#updateUserFromIdentity(org.olat.core.id.Identity)
	 */
	@Override
	public boolean updateUserFromIdentity(Identity identity) {
		try {
			String fullName = getUserDisplayName(identity);
			updateUsernameCache(identity.getKey(), identity.getName(), fullName);
		} catch (Exception e) {
			logWarn("Error update usernames cache", e);
		}
		updateUser(identity.getUser());
		return true;
	}

	/**
	 * @see org.olat.user.UserManager#setUserCharset(org.olat.core.id.Identity, java.lang.String)
	 */
	public void setUserCharset(Identity identity, String charset){
	    PropertyManager pm = PropertyManager.getInstance();
	    Property p = pm.findProperty(identity, null, null, null, CHARSET);
	    
	    if(p != null){
	        p.setStringValue(charset);
	        pm.updateProperty(p);
		} else {
	        Property newP = pm.createUserPropertyInstance(identity, null, CHARSET, null, null, charset, null);
	        pm.saveProperty(newP);
	    }
	}

	/**
	 * @see org.olat.user.UserManager#getUserCharset(org.olat.core.id.Identity)
	 */
	public String getUserCharset(Identity identity){
	   String charset;
	   charset = WebappHelper.getDefaultCharset();
	   PropertyManager pm = PropertyManager.getInstance();
	   Property p = pm.findProperty(identity, null, null, null, CHARSET);
	   if(p != null){
	       charset = p.getStringValue();
			// if after migration the system does not support the charset choosen by a
			// user
	       // (a rather rare case)
	       if(!Charset.isSupported(charset)){
	           charset = WebappHelper.getDefaultCharset();
	       }
		} else {
	       charset = WebappHelper.getDefaultCharset();
	   }
	   return charset;
	}

	/**
	 * Delete all user-properties which are deletable.
	 * @param user
	 */
	public void deleteUserProperties(User user) {
		// prevent stale objects, reload first
		user = loadUserByKey(user.getKey());
		// loop over user fields and remove them form the database if they are
		// deletable
		List<UserPropertyHandler> propertyHandlers = userPropertiesConfig.getAllUserPropertyHandlers();
		for (UserPropertyHandler propertyHandler : propertyHandlers) {
			String fieldName = propertyHandler.getName();
			if (propertyHandler.isDeletable()) {
				user.setProperty(fieldName, null);
			}		
		}
		// persist changes
		updateUser(user);
		if(isLogDebugEnabled()) logDebug("Delete all user-attributtes for user=" + user);
	}
	
	

	@Override
	public String getUserDisplayName(String username) {
		String fullName = usernameCache.get(username);
		if(fullName == null) {
			List<IdentityShort> identities = securityManager.findShortIdentitiesByName(Collections.singletonList(username));
			for(IdentityShort identity:identities) {
				fullName = getUserDisplayName(identity);
			}
		}
		return fullName;
	}

	@Override
	public String getUserDisplayName(Long identityKey) {
		if(identityKey == null || identityKey.longValue() <= 0) {
			return "";
		}
		
		String fullName = usernameCache.get(identityKey);
		if(fullName == null) {
			IdentityShort identity = securityManager.loadIdentityShortByKey(identityKey);
			fullName = getUserDisplayName(identity);
		}
		return fullName;
	}

	@Override
	public Map<String, String> getUserDisplayNamesByUserName(Collection<String> usernames) {
		if(usernames == null | usernames.isEmpty()) {
			return Collections.emptyMap();
		}
		
		Map<String, String> fullNames = new HashMap<String,String>();
		List<String> newUsernames = new ArrayList<String>();
		for(String username:usernames) {
			String fullName = usernameCache.get(username);
			if(fullName != null) {
				fullNames.put(username, fullName);
			} else {
				newUsernames.add(username);
			}
		}

		List<IdentityShort> identities = securityManager.findShortIdentitiesByName(newUsernames);
		for(IdentityShort identity:identities) {
			String fullName = getUserDisplayName(identity);
			fullNames.put(identity.getName(), fullName);
			newUsernames.remove(identity.getName());
		}
		//not found
		for(String notFound:newUsernames) {
			usernameCache.put(notFound, notFound);
		}
		return fullNames;
	}

	@Override
	public String getUserDisplayName(Identity identity) {
		if (userDisplayNameCreator == null || identity == null) return "";
		String fullName = getUserDisplayName(identity.getUser());
		updateUsernameCache(identity.getKey(), identity.getName(), fullName);
		return fullName;
	}

	/**
	 * @see org.olat.user.UserManager#getUserDisplayName(org.olat.core.id.User)
	 */
	@Override
	public String getUserDisplayName(User user) {
		if (userDisplayNameCreator == null || user == null) return "";
		return userDisplayNameCreator.getUserDisplayName(user);
	}
	
	/**
	 * @see org.olat.user.UserManager#getUserDisplayName(org.olat.core.id.IdentityShort)
	 */
	@Override
	public String getUserDisplayName(IdentityShort identity) {
		if (userDisplayNameCreator == null || identity == null) return "";
		String fullName = userDisplayNameCreator.getUserDisplayName(identity);
		updateUsernameCache(identity.getKey(), identity.getName(), fullName);
		return fullName;
	}

	@Override
	public Map<Long, String> getUserDisplayNamesByKey(Collection<Long> identityKeys) {
		
		if(identityKeys == null | identityKeys.isEmpty()) {
			return Collections.emptyMap();
		}
		
		Map<Long, String> fullNames = new HashMap<Long,String>();
		List<Long> newIdentityKeys = new ArrayList<Long>();
		for(Long identityKey:identityKeys) {
			String fullName = usernameCache.get(identityKey);
			if(fullName != null) {
				fullNames.put(identityKey, fullName);
			} else {
				newIdentityKeys.add(identityKey);
			}
		}

		List<IdentityShort> identities = securityManager.loadIdentityShortByKeys(identityKeys);
		for(IdentityShort identity:identities) {
			String fullName = getUserDisplayName(identity);
			updateUsernameCache(identity.getKey(), identity.getName(), fullName);
			fullNames.put(identity.getKey(), fullName);
		}

		return fullNames;
	}
	
	private void updateUsernameCache(Long identityKey, String username, String fullName) {
		if(fullName == null) return;
		
		if(identityKey != null) {
			usernameCache.put(identityKey, fullName);
		}
		if(username != null) {
			usernameCache.put(username, fullName);
		}
	}

	/**
	 * Sping setter method
	 * @param userDisplayNameCreator the userDisplayNameCreator to set
	 */
	public void setUserDisplayNameCreator(UserDisplayNameCreator userDisplayNameCreator) {
		this.userDisplayNameCreator = userDisplayNameCreator;
	}

}
