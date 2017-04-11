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
package org.olat.modules.lecture.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.model.LectureBlockAndRollCall;
import org.olat.modules.lecture.model.LectureBlockRollCallImpl;
import org.olat.modules.lecture.model.LectureStatistics;
import org.olat.modules.lecture.model.ParticipantLectureStatistics;
import org.olat.repository.RepositoryEntryRef;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 20 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LectureBlockRollCallDAO {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	
	public LectureBlockRollCall createAndPersistRollCall(LectureBlock lectureBlock, Identity identity,
			Boolean authorizedAbsence, String absenceReason, String comment, List<Integer> absences) {
		LectureBlockRollCallImpl rollCall = new LectureBlockRollCallImpl();
		rollCall.setCreationDate(new Date());
		rollCall.setLastModified(rollCall.getCreationDate());
		rollCall.setIdentity(identity);
		rollCall.setLectureBlock(lectureBlock);
		rollCall.setAbsenceAuthorized(authorizedAbsence);
		rollCall.setAbsenceReason(absenceReason);
		rollCall.setComment(comment);
		addInternalLecture(lectureBlock, rollCall, absences);
		dbInstance.getCurrentEntityManager().persist(rollCall);
		return rollCall;
	}
	
	public LectureBlockRollCall addLecture(LectureBlock lectureBlock, LectureBlockRollCall rollCall, List<Integer> absences) {
		addInternalLecture(lectureBlock, rollCall, absences);
		return dbInstance.getCurrentEntityManager().merge(rollCall);
	}
	
	private void addInternalLecture(LectureBlock lectureBlock, LectureBlockRollCall rollCall, List<Integer> absences) {
		if(absences != null) {
			LectureBlockRollCallImpl call = (LectureBlockRollCallImpl)rollCall;
			List<Integer> currentAbsentList = call.getLecturesAbsentList();
			for(int i=absences.size(); i-->0; ) {
				Integer absence = absences.get(i);
				if(absence != null && !currentAbsentList.contains(absence)) {
					currentAbsentList.add(absence);
				}
			}
			call.setLecturesAbsentList(currentAbsentList);
			call.setLecturesAbsentNumber(currentAbsentList.size());
		
			int plannedLecture = lectureBlock.getPlannedLecturesNumber();
			
			List<Integer> attendedList = new ArrayList<>();
			for(int i=0; i<plannedLecture; i++) {
				if(!currentAbsentList.contains(i)) {
					attendedList.add(i);
				}
			}
			call.setLecturesAttendedList(attendedList);
			call.setLecturesAttendedNumber(plannedLecture - currentAbsentList.size());
		}
	}
	
	public LectureBlockRollCall removeLecture(LectureBlock lectureBlock, LectureBlockRollCall rollCall, List<Integer> absences) {
		removeInternalLecture(lectureBlock, rollCall, absences);
		return dbInstance.getCurrentEntityManager().merge(rollCall);
	}
	
	private void removeInternalLecture(LectureBlock lectureBlock, LectureBlockRollCall rollCall, List<Integer> absences) {
		if(absences != null) {
			LectureBlockRollCallImpl call = (LectureBlockRollCallImpl)rollCall;
			List<Integer> currentAbsentList = call.getLecturesAbsentList();
			for(int i=absences.size(); i-->0; ) {
				currentAbsentList.remove(absences.get(i));
			}
			call.setLecturesAbsentList(currentAbsentList);
			call.setLecturesAbsentNumber(currentAbsentList.size());
		
			int plannedLecture = lectureBlock.getPlannedLecturesNumber();
			
			List<Integer> attendedList = new ArrayList<>();
			for(int i=0; i<plannedLecture; i++) {
				if(!currentAbsentList.contains(i)) {
					attendedList.add(i);
				}
			}
			call.setLecturesAttendedList(attendedList);
			call.setLecturesAttendedNumber(plannedLecture - currentAbsentList.size());
		}
	}
	
	public LectureBlockRollCall loadByKey(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rollcall from lectureblockrollcall rollcall")
		  .append(" inner join fetch rollcall.identity ident")
		  .append(" inner join fetch ident.user user")
		  .append(" inner join fetch rollcall.lectureBlock block")
		  .append(" where rollcall.key=:rollCallKey");
		
		List<LectureBlockRollCall> rollCalls = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LectureBlockRollCall.class)
				.setParameter("rollCallKey", key)
				.getResultList();
		return rollCalls == null || rollCalls.isEmpty() ? null : rollCalls.get(0);
	}
	
	public LectureBlockRollCall update(LectureBlockRollCall rollCall) {
		return dbInstance.getCurrentEntityManager().merge(rollCall);
	}
	
	public List<LectureBlockRollCall> getRollCalls(LectureBlockRef block) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rollcall from lectureblockrollcall rollcall")
		  .append(" inner join fetch rollcall.identity ident")
		  .append(" inner join fetch ident.user user")
		  .append(" inner join fetch rollcall.lectureBlock block")
		  .append(" where rollcall.lectureBlock.key=:blockKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LectureBlockRollCall.class)
				.setParameter("blockKey", block.getKey())
				.getResultList();
	}
	
	public LectureBlockRollCall getRollCall(LectureBlockRef block, IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rollcall from lectureblockrollcall rollcall")
		  .append(" where rollcall.lectureBlock.key=:blockKey and rollcall.identity.key=:identityKey");
		List<LectureBlockRollCall> rollCalls = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LectureBlockRollCall.class)
				.setParameter("blockKey", block.getKey())
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		return rollCalls != null && rollCalls.size() > 0 ? rollCalls.get(0) : null;
	}
	
	public List<LectureBlockAndRollCall> getParticipantLectureBlockAndRollCalls(RepositoryEntryRef entry, IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select block, call, re.displayname")
		  .append(" from lectureblock block")
		  .append(" inner join block.entry re")
		  .append(" inner join block.groups blockToGroup")
		  .append(" inner join blockToGroup.group bGroup")
		  .append(" inner join bGroup.members membership")
		  .append(" left join lectureblockrollcall as call on (call.identity.key=membership.identity.key and call.lectureBlock.key=block.key)")
		  .append(" where membership.identity.key=:identityKey and membership.role='").append(GroupRoles.participant.name()).append("'")
		  .append(" and block.entry.key=:repoEntryKey");
		
		List<Object[]> rawObjects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("repoEntryKey", entry.getKey())
				.getResultList();
		Map<Long,LectureBlockAndRollCall> blockToRollCallMap = new HashMap<>(); 
		for(Object[] objects:rawObjects) {
			int pos = 0;
			LectureBlock block = (LectureBlock)objects[pos++];
			LectureBlockRollCall rollCall = (LectureBlockRollCall)objects[pos++];
			String displayname = (String)objects[pos++];
			blockToRollCallMap.put(block.getKey(), new LectureBlockAndRollCall(displayname, block, rollCall));
		}
		
		// append the coaches
		StringBuilder sc = new StringBuilder();
		sc.append("select block.key, coach")
		  .append(" from lectureblock block")
		  .append(" inner join block.teacherGroup tGroup")
		  .append(" inner join tGroup.members membership")
		  .append(" inner join membership.identity coach")
		  .append(" inner join fetch coach.user usercoach")
		  .append(" where membership.role='").append("teacher").append("' and block.entry.key=:repoEntryKey");
		
		//get all, it's quick
		List<Object[]> rawCoachs = dbInstance.getCurrentEntityManager()
				.createQuery(sc.toString(), Object[].class)
				.setParameter("repoEntryKey", entry.getKey())
				.getResultList();
		for(Object[] rawCoach:rawCoachs) {
			Long blockKey = (Long)rawCoach[0];
			LectureBlockAndRollCall rollCall = blockToRollCallMap.get(blockKey);
			if(rollCall != null) {
				Identity coach = (Identity)rawCoach[1];
				String fullname = userManager.getUserDisplayName(coach);
				if(rollCall.getCoach() == null) {
					rollCall.setCoach(fullname);
				} else {
					rollCall.setCoach(rollCall.getCoach() + ", " + fullname);
				}
			}
		}
		
		return new ArrayList<>(blockToRollCallMap.values());
	}
	
	public List<LectureStatistics> getStatistics(IdentityRef identity,
			boolean authorizedAbsenceDefault, boolean countAuthorizedAbsenceAsAttendant,
			boolean calculateAttendanceRate, double requiredAttendanceRateDefault) {
		StringBuilder sb = new StringBuilder();
		sb.append("select call.key as callKey, ")
		  .append("  call.lecturesAttendedNumber as attendedLectures,")
		  .append("  call.lecturesAbsentNumber as absentLectures,")
		  .append("  call.absenceAuthorized as absenceAuthorized,")
		  .append("  block.key as blockKey,")
		  .append("  block.plannedLecturesNumber as blockPlanned,")
		  .append("  block.effectiveLecturesNumber as blockEffective,")
		  .append("  block.rollCallStatusString as rollCallStatus,")
		  .append("  block.endDate as rollCallEndDate,")
		  .append("  re.key as repoKey,")
		  .append("  re.displayname as repoDisplayName,")
		  .append("  config.calculateAttendanceRate as calculateRate,")
		  .append("  config.requiredAttendanceRate as reopConfigRate,")
		  .append("  summary.requiredAttendanceRate as summaryRate")
		  .append(" from lectureblock block")
		  .append(" inner join block.entry re")
		  .append(" inner join block.groups blockToGroup")
		  .append(" inner join blockToGroup.group bGroup")
		  .append(" inner join bGroup.members membership")
		  .append(" left join lectureentryconfig as config on (re.key=config.entry.key)")
		  .append(" left join lectureparticipantsummary as summary on (summary.identity.key=membership.identity.key and summary.entry.key=block.entry.key)")
		  .append(" left join lectureblockrollcall as call on (call.identity.key=membership.identity.key and call.lectureBlock.key=block.key)")
		  .append(" where membership.identity.key=:identityKey and membership.role='").append(GroupRoles.participant.name()).append("'");
		
		//take in account: requiredAttendanceRateDefault, from repo config requiredAttendanceRate
		//take in account: authorized absence
		//take in account: firstAddmissionDate and null
		
		Date now = new Date();
		
		List<Object[]> rawObjects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		Map<Long,LectureStatistics> stats = new HashMap<>();
		for(Object[] rawObject:rawObjects) {
			int pos = 1;//jump roll call key
			Long lecturesAttended = PersistenceHelper.extractLong(rawObject, pos++);
			Long lecturesAbsent = PersistenceHelper.extractLong(rawObject, pos++);
			Boolean absenceAuthorized = (Boolean)rawObject[pos++];
			
			pos++;//jump block key
			Long plannedLecturesNumber = PersistenceHelper.extractLong(rawObject, pos++);
			Long effectiveLecturesNumber = PersistenceHelper.extractLong(rawObject, pos++);
			String rollCallStatus = (String)rawObject[pos++];
			Date rollCallEndDate = (Date)rawObject[pos++];

			Long repoKey = PersistenceHelper.extractLong(rawObject, pos++);
			String repoDisplayname = (String)rawObject[pos++];
			
			LectureStatistics entryStatistics;
			if(stats.containsKey(repoKey)) {
				entryStatistics = stats.get(repoKey);
			} else {
				Boolean repoCalculateRate = (Boolean)rawObject[pos++];
				boolean calculateRate = calculateAttendanceRate;
				if(repoCalculateRate != null) {
					calculateRate = repoCalculateRate.booleanValue();
				}
				
				double rate = -1.0d;
				if(calculateRate) {
					Double repoRequiredRate = (Double)rawObject[pos++];
					Double persoRequiredRate = (Double)rawObject[pos++];
					if(persoRequiredRate != null && persoRequiredRate.doubleValue() >= 0.0d) {
						rate = persoRequiredRate.doubleValue();
					} else if(repoRequiredRate != null && repoRequiredRate.doubleValue() >= 0.0d) {
						rate = repoRequiredRate.doubleValue();
					} else {
						rate = requiredAttendanceRateDefault;
					}
				}

				entryStatistics = new LectureStatistics(repoKey, repoDisplayname, calculateRate, rate);
				stats.put(repoKey, entryStatistics);
			}
			
			//only count closed roll call after the end date
			if(rollCallEndDate != null && rollCallEndDate.before(now)
					&& rollCallStatus != null && (LectureRollCallStatus.closed.name().equals(rollCallStatus) || LectureRollCallStatus.autoClose.name().equals(rollCallStatus))) {
			
				if(lecturesAbsent != null) {
					if(countAuthorizedAbsenceAsAttendant && absenceAuthorized != null && absenceAuthorized.booleanValue()) {
						//authorized absence as attendant
						entryStatistics.addTotalAttendedLectures(lecturesAttended.longValue());
					} else {
						entryStatistics.addTotalAbsentLectures(lecturesAbsent.longValue());
					}
				}
				if(lecturesAttended != null) {
					entryStatistics.addTotalAttendedLectures(lecturesAttended.longValue());
				}

				if(effectiveLecturesNumber != null) {
					entryStatistics.addTotalEffectiveLectures(effectiveLecturesNumber.longValue());
				}
			}

			if(plannedLecturesNumber != null) {
				entryStatistics.addTotalPlannedLectures(plannedLecturesNumber.longValue());
			}
		}
		
		return new ArrayList<>(stats.values());
	}
	
	
	public List<ParticipantLectureStatistics> getStatistics(RepositoryEntryRef entry) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ident.key, ")
		  .append(" count(call.lecturesAttendedNumber), count(call.lecturesAbsentNumber),")
		  .append(" count(block.plannedLecturesNumber), count(block.effectiveLecturesNumber)")
		  .append(" from lectureblock block")
		  .append(" inner join block.groups blockToGroup")
		  .append(" inner join blockToGroup.group bGroup")
		  .append(" inner join bGroup.members membership")
		  .append(" inner join membership.identity ident")
		  .append(" left join lectureblockrollcall as call on (call.identity.key=membership.identity.key and call.lectureBlock.key=block.key)")
		  .append(" where block.entry.key=:entryKey and membership.role='").append(GroupRoles.participant.name()).append("'")
		  .append(" group by ident.key");
		
		List<Object[]> rawObjects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("entryKey", entry.getKey())
				.getResultList();
		Map<Long,ParticipantLectureStatistics> stats = new HashMap<>();
		for(Object[] rawObject:rawObjects) {
			int pos = 0;//jump roll call key
			Long identityKey = (Long)rawObject[pos++];
			Long attended = PersistenceHelper.extractLong(rawObject, pos++);
			Long absent = PersistenceHelper.extractLong(rawObject, pos++);
			Long plannedBlocks = PersistenceHelper.extractLong(rawObject, pos++);
			
			ParticipantLectureStatistics entryStatistics;
			if(stats.containsKey(identityKey)) {
				entryStatistics = stats.get(identityKey);
			} else {
				entryStatistics = new ParticipantLectureStatistics(identityKey);
				stats.put(identityKey, entryStatistics);
			}
			
			if(absent != null) {
				entryStatistics.addTotalAbsentLectures(absent.longValue());
			}
			if(attended != null) {
				entryStatistics.addTotalAttendedLectures(attended.longValue());
			}
			if(plannedBlocks != null) {
				entryStatistics.addTotalPlannedLectures(plannedBlocks.longValue());
			}
		}
		
		return new ArrayList<>(stats.values());
	}
}
