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
package org.olat.modules.assessment.manager;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.model.AssessmentEntryImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 20.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AssessmentEntryDAO {
	
	@Autowired
	private DB dbInstance;
	
	public AssessmentEntry createCourseNodeAssessment(Identity assessedIdentity,
			RepositoryEntry entry, String subIdent, RepositoryEntry referenceEntry) {
		
		AssessmentEntryImpl data = new AssessmentEntryImpl();
		data.setCreationDate(new Date());
		data.setLastModified(data.getCreationDate());
		data.setIdentity(assessedIdentity);
		data.setRepositoryEntry(entry);
		data.setSubIdent(subIdent);
		data.setReferenceEntry(referenceEntry);
		dbInstance.getCurrentEntityManager().persist(data);
		return data;
	}
	
	public AssessmentEntry loadAssessmentEntryById(Long id) {
		List<AssessmentEntry> nodeAssessment = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadAssessmentEntryById", AssessmentEntry.class)
				.setParameter("key", id)
				.getResultList();
		return nodeAssessment.isEmpty() ? null : nodeAssessment.get(0);
	}

	public AssessmentEntry loadAssessmentEntry(IdentityRef assessedIdentity, RepositoryEntryRef entry,
			String subIdent) {
		
		List<AssessmentEntry> nodeAssessment = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadAssessmentEntryByRepositoryEntryAndUser", AssessmentEntry.class)
				.setParameter("repositoryEntryKey", entry.getKey())
				.setParameter("identityKey", assessedIdentity.getKey())
				.setParameter("subIdent", subIdent)
				.getResultList();
		return nodeAssessment.isEmpty() ? null : nodeAssessment.get(0);
	}
	
	public AssessmentEntry updateCourseNodeAssessment(AssessmentEntry nodeAssessment) {
		((AssessmentEntryImpl)nodeAssessment).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(nodeAssessment);
	}
	
	public List<AssessmentEntry> loadAssessmentEntryBySubIdent(RepositoryEntryRef entry, String subIdent) {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadAssessmentEntryByRepositoryEntryAndSubIdent", AssessmentEntry.class)
				.setParameter("repositoryEntryKey", entry.getKey())
				.setParameter("subIdent", subIdent)
				.getResultList();
	}
	
	public List<Identity> getAllIdentitiesWithAssessmentData(RepositoryEntryRef courseEntry) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct data.identity from assessmententry data where data.repositoryEntry.key=:repositoryEntryKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("repositoryEntryKey", courseEntry.getKey())
				.getResultList();
	}
	

	
	

}
