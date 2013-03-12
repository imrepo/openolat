package de.unileipzig.xman.exam;

import java.util.Date;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Persistable;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.references.ReferenceImpl;

import de.unileipzig.xman.module.Module;

/**
 * This is the interface of the exam.
 *
 * @author iggy
 * 
 */
public interface Exam extends ModifiedInfo, CreateInfo, Persistable, OLATResourceable {
	
	// Repository types
	public static String ORES_TYPE_NAME = OresHelper.calculateTypeName(Exam.class);
	
	public static String EXAM_TYPE_WRITTEN = "written";
	public static String EXAM_TYPE_ORAL = "oral";
	
	public RepositoryEntry getCourseRepoEntry();
	
	public ReferenceImpl getCourseReference();
	
	
	/*------------------------- getter -------------------------*/
	
	
	/**
	 * @return a String representing the name of the exam
	 */
	public String getName();
	
	/**
	 * @return a Date representing the start of the registration period
	 */
	public Date getRegStartDate();
	
	/**
	 * @return a Date representing the end of the registration period
	 */
	public Date getRegEndDate();
	
	/**
	 * @return a Date representing the time until a student can unsubscribe
	 */
	public Date getSignOffDate();
	
	/**
	 * @return
	 */
	public boolean getEarmarkedEnabled();
	
	/**
	 * @return
	 */
	public String getComments();
	
	/**
	 * @return true, if the exam is oral
	 */
	public boolean getIsOral();
	
	/**
	 * @return the module of the exam
	 */
	public Module getModule();
	
	/**
	 * @return the calculated semester of the exam
	 */
	public Identity getIdentity();
	
	
	/*------------------------- setter -------------------------*/
	
	
	/**
	 * @param oral
	 */
	public void setIsOral(boolean oral);
	
	/**
	 * @param String - sets the given String as name of the exam
	 */
	public void setName(String name);
	
	/**
	 * @param startDate - sets the beginning of the registration period
	 */
	public void setRegStartDate(Date startDate);
	
	/**
	 * @param endDate - sets the end of the registration period
	 */
	public void setRegEndDate(Date endDate);
	
	/**
	 * @param signOffDate - sets the date until students can unsubscribe
	 */
	public void setSignOffDate(Date signOffDate);
	
	/**
	 * @param enable
	 */
	public void setEarmarkedEnabled(boolean enable);
	
	/**
	 * @param comments
	 */
	public void setComments(String comments);
	
	/**
	 * @param module - sets the module of the exam
	 */
	public void setModule(Module module);
	
	/**
	 * @param identity - the identity of the person which is responsible for this exam
	 */
	public void setIdentity(Identity identity);
}
