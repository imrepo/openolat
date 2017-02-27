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
package org.olat.course.highscore.model;

import org.olat.course.highscore.ui.HighScoreTableEntry;

/**
 * 27.02.2017
 * 
 * @author fkiefer, fabian.kiefer@frentix.com
 */
public class HighScoreDataModel {
	
	private double[] scores;
	private double[] modifiedScores;
	private double min;
	private long classwidth;
	private HighScoreTableEntry ownTableEntry;

	public HighScoreDataModel(double[] modifiedScores, long classwidth, double min) {
		super();
		this.modifiedScores = modifiedScores;
		this.classwidth = classwidth;
		this.min = min;
	}

	public HighScoreDataModel(double[] scores, HighScoreTableEntry ownTableEntry) {
		super();
		this.scores = scores;
		this.ownTableEntry = ownTableEntry;
	}

	public double[] getScores() {
		return scores;
	}

	public double[] getModifiedScores() {
		return modifiedScores;
	}

	public long getClasswidth() {
		return classwidth;
	}
	
	public double getMin() {
		return min;
	}

	public HighScoreTableEntry getOwnTableEntry() {
		return ownTableEntry;
	}
	
	

	

}