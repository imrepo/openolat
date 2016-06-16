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
package org.olat.modules.portfolio.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.model.GroupImpl;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.Section;

/**
 * 
 * Initial date: 07.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="pfsection")
@Table(name="o_pf_section")
public class SectionImpl implements Persistable, ModifiedInfo, CreateInfo, Section {

	private static final long serialVersionUID = -8520480114785588566L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	/** Only used for order by */
	@GeneratedValue
	@Column(name="pos")
	private long pos;

	@Column(name="p_title", nullable=true, insertable=true, updatable=true)
	private String title;
	@Column(name="p_description", nullable=true, insertable=true, updatable=true)
	private String description;
	@Column(name="p_status", nullable=true, insertable=true, updatable=true)
	private String status;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="p_begin", nullable=true, insertable=true, updatable=true)
	private Date beginDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="p_end", nullable=true, insertable=true, updatable=true)
	private Date endDate;

	@ManyToOne(targetEntity=GroupImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_group_id", nullable=false, insertable=true, updatable=false)
	private Group baseGroup;
	
	@ManyToOne(targetEntity=BinderImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_binder_id", nullable=false, insertable=true, updatable=false)
	private Binder binder;
	
	@OneToMany(targetEntity=PageImpl.class, mappedBy="section", fetch=FetchType.LAZY,
			orphanRemoval=true, cascade={CascadeType.REMOVE})
	@OrderColumn(name="pos")
	private List<Page> pages;
	

	@ManyToOne(targetEntity=SectionImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_template_reference_id", nullable=true, insertable=true, updatable=false)
	private Section templateReference;
	
	@Override
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public long getPos() {
		return pos;
	}
	
	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date date) {
		lastModified = date;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Group getBaseGroup() {
		return baseGroup;
	}

	public void setBaseGroup(Group baseGroup) {
		this.baseGroup = baseGroup;
	}

	public Binder getBinder() {
		return binder;
	}

	public void setBinder(Binder binder) {
		this.binder = binder;
	}

	@Override
	public List<Page> getPages() {
		if(pages == null) {
			pages = new ArrayList<>();
		}
		return pages;
	}

	public void setPages(List<Page> pages) {
		this.pages = pages;
	}

	@Override
	public int hashCode() {
		return key == null ? -7659236 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof SectionImpl) {
			SectionImpl section = (SectionImpl)obj;
			return key != null && key.equals(section.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}