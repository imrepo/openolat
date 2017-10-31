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
package org.olat.modules.taxonomy.manager;

import java.io.File;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.model.TaxonomyImpl;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 18 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class TaxonomyDAO implements InitializingBean{

	private File rootDirectory, taxonomyDirectory;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	
	@Override
	public void afterPropertiesSet() {
		File bcrootDirectory = new File(FolderConfig.getCanonicalRoot());
		rootDirectory = new File(bcrootDirectory, "taxonomy");
		taxonomyDirectory = new File(rootDirectory, "taxonomy");
		if(!taxonomyDirectory.exists()) {
			taxonomyDirectory.mkdirs();
		}
	}
	
	public Taxonomy createTaxonomy(String identifier, String displayName, String description, String externalId) {
		TaxonomyImpl taxonomy = new TaxonomyImpl();
		taxonomy.setCreationDate(new Date());
		taxonomy.setLastModified(taxonomy.getCreationDate());
		if(StringHelper.containsNonWhitespace(identifier)) {
			taxonomy.setIdentifier(identifier);
		} else {
			taxonomy.setIdentifier(UUID.randomUUID().toString());
		}
		taxonomy.setDisplayName(displayName);
		taxonomy.setDescription(description);
		taxonomy.setExternalId(externalId);
		taxonomy.setDocumentsLibraryEnabled(true);
		Group group = groupDao.createGroup();
		taxonomy.setGroup(group);
		dbInstance.getCurrentEntityManager().persist(taxonomy);
		String storage = createStorage(taxonomy, "directory");
		taxonomy.setDirectoryPath(storage);
		String infoStorage = createStorage(taxonomy, "infopage");
		taxonomy.setDirectoryInfoPagePath(infoStorage);
		taxonomy = dbInstance.getCurrentEntityManager().merge(taxonomy);
		taxonomy.getGroup();
		return taxonomy;
	}
	
	public Taxonomy loadByKey(Long key) {
		List<Taxonomy> taxonomies = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadTaxonomyByKey", Taxonomy.class)
				.setParameter("taxonomyKey", key)
				.getResultList();
		return taxonomies == null || taxonomies.isEmpty() ? null : taxonomies.get(0);
	}
	
	public Taxonomy updateTaxonomy(Taxonomy taxonomy) {
		((TaxonomyImpl)taxonomy).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(taxonomy);
	}
	
	public List<Taxonomy> getTaxonomyList() {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadAllTaxonomy", Taxonomy.class)
				.getResultList();
	}
	
	public String createStorage(Taxonomy taxonomy, String type) {
		File storage = new File(taxonomyDirectory, taxonomy.getKey().toString());
		File directory = new File(storage, type);
		Path relativePath = rootDirectory.toPath().relativize(directory.toPath());
		String relativePathString = relativePath.toString();
		return relativePathString;
	}
	
	public VFSContainer getDocumentsLibrary(Taxonomy taxonomy) {
		String path = ((TaxonomyImpl)taxonomy).getDirectoryPath();
		return new OlatRootFolderImpl(path, null);
	}
	
	public VFSContainer getTaxonomyInfoPageContainer(Taxonomy taxonomy) {
		String path = ((TaxonomyImpl)taxonomy).getDirectoryInfoPagePath();
		return new OlatRootFolderImpl(path, null);
	}
}
