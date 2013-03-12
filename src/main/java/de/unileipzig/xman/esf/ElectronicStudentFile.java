package de.unileipzig.xman.esf;

import java.util.Date;
import java.util.List;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Persistable;
import org.olat.core.util.resource.OresHelper;

import de.unileipzig.xman.comment.Comment;
import de.unileipzig.xman.comment.CommentEntry;
import de.unileipzig.xman.exam.Exam;
import de.unileipzig.xman.illness.IllnessReport;
import de.unileipzig.xman.illness.IllnessReportEntry;
import de.unileipzig.xman.protocol.Protocol;

/**
 * This class represents an electronic version of the students file in the exam office.
 * Every Exam including the grade, the student took place in, is listed here.
 * It is possible to add a comment and illnessreports here.
 * 
 * @author gerb
 */
public interface ElectronicStudentFile extends CreateInfo, ModifiedInfo, Persistable, OLATResourceable {
	
	// Repository types
	public static String ORES_TYPE_NAME = OresHelper.calculateTypeName(ElectronicStudentFileImpl.class);

	
	/* ------------------getter --------------------- */
	
	/**
	 * @return true if the esf has already been validated by the examoffice
	 */
	public boolean getValidated();
	
	/**
	 * @return the identity witch belongs to this esf
	 */
	public Identity getIdentity();
	
	/**
	 * @return the list of protocol of the student
	 */
	public List<Protocol> getProtocolList();
	
	/**
	 * @return the CommentEntries witch are set for this student
	 */
	public List<CommentEntry> getCommentEntries();
	
	/**
	 * @return the all IllnessReportsEntries of this particular student
	 */
	public List<IllnessReportEntry> getIllnessReportsEntries();
	
	/**
	 * @return the identity of the user who validated the esf
	 */
	public Identity getValidator();
	
	/**
	 * @return the comments of this esf
	 */
	public Comment getComments();
	
	/**
	 * @return the date, when the esf was edited the last time
	 */
	public Date getLastModified();
	
	/* ------------------setter --------------------- */
	
	/**
	 * @param sets the validated flag
	 */
	public void setValidated(boolean validated);
	
	/**
	 * @return the identity witch belongs to this esf
	 */
	public void setIdentity(Identity identity);
	
	/**
	 * @return the list of protocol of the student
	 */
	public void setProtocolList(List<Protocol> protocolList);
	
	/**
	 * @param identity - the identity of the person who validates the esf
	 */
	public void setValidator(Identity identity);
	
	/**
	 * @param the date to set
	 */
	public void setLastModified(Date lastModified);
	
	/* ------------------adder --------------------- */
	
	/**
	 * @param adds a CommentEntry to the list of those
	 */
	public void addCommentEntry(CommentEntry commentEntry);
	
	/**
	 * @param adds a IllnessReportEntry to the list of those
	 */
	public void addIllnessReportEntry(IllnessReportEntry illnessReportEntry);
	
	/**
	 * @param proto - the protocol which should be added to this esf
	 */
	public void addProtocol(Protocol proto);
	
	/* ------------------remover --------------------- */
	
	/**	 * 
	 * @param the key of the CommentEntry to delete
	 * @return the deleted CommentEntry
	 */
	public void removeCommentEntry(Long commentEntryKey);
	
	/**
	 * @param the key of the IllnessReportEntry to delete
	 * @return the deleted IllnessReportEntry
	 */
	public void removeIllnessReportEntry(Long illnessReportEntryKey);
	
	/**************************************************************/
}
