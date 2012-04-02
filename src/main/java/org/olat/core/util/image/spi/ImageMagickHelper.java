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
package org.olat.core.util.image.spi;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.olat.core.commons.services.thumbnail.FinalSize;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.image.ImageHelperSPI;
import org.olat.core.util.image.Size;
import org.olat.core.util.vfs.LocalImpl;
import org.olat.core.util.vfs.NamedLeaf;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * This is an implementation which call the ImageMagick command line tool to make the
 * scaling and thumbnailing og images and pdfs. For PDFs, ImageMagick use GhostScript
 * internally with the command "gs". Set the magickPath the path where the command line
 * tools are.
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ImageMagickHelper extends BasicManager implements ImageHelperSPI {
	
	public ImageMagickHelper() {
		//
	}

	@Override
	public Size thumbnailPDF(VFSLeaf pdfFile, VFSLeaf thumbnailFile, int maxWidth, int maxHeight) {
		File baseFile = extractIOFile(pdfFile);
		File thumbnailBaseFile = extractIOFile(thumbnailFile);
		FinalSize finalSize = generateThumbnail(baseFile, thumbnailBaseFile, true, maxWidth, maxHeight);
		if(finalSize != null) {
			return new Size(finalSize.getWidth(), finalSize.getHeight(), true);
		}
		return null;
	}
	
	@Override
	public Size scaleImage(File image, String imgExt, VFSLeaf scaledImage, int maxWidth, int maxHeight) {
		File scaledBaseFile = extractIOFile(scaledImage);
		FinalSize finalSize = generateThumbnail(image, scaledBaseFile, false, maxWidth, maxHeight);
		if(finalSize != null) {
			return new Size(finalSize.getWidth(), finalSize.getHeight(), true);
		}
		return null;
	}
	
	@Override
	public Size scaleImage(VFSLeaf image, VFSLeaf scaledImage, int maxWidth, int maxHeight) {
		FinalSize finalSize = generateThumbnail(image, scaledImage, maxWidth, maxHeight);
		if(finalSize != null) {
			return new Size(finalSize.getWidth(), finalSize.getHeight(), true);
		}
		return null;
	}
	
	@Override
	public Size scaleImage(File image, String extension, File scaledImage, int maxWidth, int maxHeight) {
		return scaleImage(image, scaledImage, maxWidth, maxHeight);
	}

	@Override
	public Size scaleImage(File image, File scaledImage, int maxWidth, int maxHeight) {
		FinalSize finalSize = generateThumbnail(image, scaledImage, false, maxWidth, maxHeight);
		if(finalSize != null) {
			return new Size(finalSize.getWidth(), finalSize.getHeight(), true);
		}
		return null;
	}

	private final FinalSize generateThumbnail(VFSLeaf file, VFSLeaf thumbnailFile, int maxWidth, int maxHeight) {
		File baseFile = extractIOFile(file);
		File thumbnailBaseFile = extractIOFile(thumbnailFile);
		return generateThumbnail(baseFile, thumbnailBaseFile, false, maxWidth, maxHeight);
	}
	
	private final File extractIOFile(VFSLeaf leaf) {
		if(leaf instanceof NamedLeaf) {
			leaf = ((NamedLeaf)leaf).getDelegate();
		}
		
		File file = null;
		if(leaf instanceof LocalImpl) {
			file = ((LocalImpl)leaf).getBasefile();
		}
		return file;
	}
		
	private final FinalSize generateThumbnail(File file, File thumbnailFile, boolean firstOnly, int maxWidth, int maxHeight) {
		if(file == null || thumbnailFile == null) {
			logError("Input file or output file for thumbnailing?" + file + " -> " + thumbnailFile, null);
			return null;
		}
		
		if(!thumbnailFile.getParentFile().exists()) {
			thumbnailFile.getParentFile().mkdirs();
		}

		List<String> cmds = new ArrayList<String>();
		cmds.add("convert");
		cmds.add("-verbose");
		cmds.add("-thumbnail");
		cmds.add(maxWidth + "x" + maxHeight);
		if(firstOnly) {
			cmds.add(file.getAbsolutePath() + "[0]");
		} else {
			cmds.add(file.getAbsolutePath());
		}
		cmds.add(thumbnailFile.getAbsolutePath());

		CountDownLatch doneSignal = new CountDownLatch(1);

		ProcessWorker worker = new ProcessWorker(thumbnailFile, cmds, doneSignal);
		worker.start();

		try {
			doneSignal.await(3000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			logError("", e);
		}
		
		if(worker.isProcessing()) {
			worker.destroyProcess();
		}
		return worker.size;
	}
	
	private final FinalSize executeProcess(File thumbnailFile, Process proc) {
		try {
			InputStream stderr = proc.getErrorStream();
			InputStreamReader iserr = new InputStreamReader(stderr);
      BufferedReader berr = new BufferedReader(iserr);
      String l = null;
      StringBuilder errors = new StringBuilder();
      while ( (l = berr.readLine()) != null) {
      	errors.append(l);
      }
			
			InputStream stdout = proc.getInputStream();
			InputStreamReader isr = new InputStreamReader(stdout);
      BufferedReader br = new BufferedReader(isr);
      String line = null;
      StringBuilder output = new StringBuilder();
      while ( (line = br.readLine()) != null) {
      	output.append(line);
      }
      
      if(isLogDebugEnabled()) {
				logDebug("Error: " + errors.toString());
				logDebug("Output: " + output.toString());
			}

			int exitValue = proc.waitFor();
			if(exitValue == 0) {
				return extractSizeFromOutput(thumbnailFile, output);
			}
			return null;
		} catch (Exception e) {
			logError("", e);
			return null;
		}
	}
		
	private final FinalSize extractSizeFromOutput(File thumbnailBaseFile, StringBuilder output) {
		try {
			String verbose = output.toString();
			int lastIndex = verbose.lastIndexOf(thumbnailBaseFile.getName());
			if(lastIndex > 0) {
				int sizeIndex = verbose.indexOf("=>", lastIndex);
				if(sizeIndex > 0) {
					int stopIndex = verbose.indexOf(' ', sizeIndex);
					if(stopIndex > sizeIndex) {
						String sizeStr = verbose.substring(sizeIndex + 2, stopIndex);
						String[] sizes = sizeStr.split("x");
						if(sizes != null && sizes.length == 2) {
							int width = Integer.parseInt(sizes[0]);
							int height = Integer.parseInt(sizes[1]);
							return new FinalSize(width, height);
						}
					}
				}
			}
		} catch (NumberFormatException e) {
			logError("Error parsing output: " + output, null);
		}
		return null;
	}
	
	private class ProcessWorker extends Thread {
		
		private volatile boolean processing;
		private volatile Process process;
		private volatile FinalSize size;


		private final List<String> cmd;
		private final File thumbnailFile;
		private final CountDownLatch doneSignal;
		
		public ProcessWorker(File thumbnailFile, List<String> cmd, CountDownLatch doneSignal) {
			this.cmd = cmd;
			this.thumbnailFile = thumbnailFile;
			this.doneSignal = doneSignal;
		}
		
		public boolean isProcessing() {
			return processing;
		}
		
		public void destroyProcess() {
			process.destroy();
		}

		@Override
		public void run() {
			processing = true;
			
			try {
				if(isLogDebugEnabled()) {
					logDebug(cmd.toString());
				}
				
				ProcessBuilder builder = new ProcessBuilder(cmd);
				process = builder.start();
				size = executeProcess(thumbnailFile, process);
				doneSignal.countDown();
			} catch (IOException e) {
				logError("", e);
				if(process != null) {
					process.destroy();
				}
			}
			
			processing = false;
		}
	}
}
