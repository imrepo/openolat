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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.commons.info.model;

import java.util.Date;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;

public class InfoMessageImpl extends PersistentObject implements InfoMessage {

	private Date modificationDate;
	
	private String title;
	private String message;
	
	private Long resId;
	private String resName;
	private String subPath;
	private String businessPath;
	
	private Identity author;
	private Identity modifier;
	
	public InfoMessageImpl() {
		//
	}

	public Date getModificationDate() {
		return modificationDate;
	}

	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Long getResId() {
		return resId;
	}

	public void setResId(Long resId) {
		this.resId = resId;
	}

	public String getResName() {
		return resName;
	}

	public void setResName(String resName) {
		this.resName = resName;
	}

	public String getResSubPath() {
		return subPath;
	}

	public void setResSubPath(String subPath) {
		this.subPath = subPath;
	}

	public String getBusinessPath() {
		return businessPath;
	}

	public void setBusinessPath(String businessPath) {
		this.businessPath = businessPath;
	}

	public Identity getAuthor() {
		return author;
	}

	public void setAuthor(Identity author) {
		this.author = author;
	}

	public Identity getModifier() {
		return modifier;
	}

	public void setModifier(Identity modifier) {
		this.modifier = modifier;
	}

	public OLATResourceable getOLATResourceable() {
		final String name = resName;
		final Long id = resId;
		return new OLATResourceable() {
			@Override
			public String getResourceableTypeName() {
				return name;
			}
			@Override
			public Long getResourceableId() {
				return id;
			}
		};
	}
}
