/*
 * Created on 26.04.2005
 *
 * Copyright 2005-2006 Daniel Jacobi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package de.questmaster.gameone_downloader.utils;

import java.io.*;
import javax.swing.tree.*;

/**
 * This class contains generic/reusable methods for many purposes.
 * 
 * @author Daniel Jacobi, <a href="daniel@questmaster.de">daniel@questmaster.de</a>
 * @version $Id: JHelper.java 296 2008-02-05 23:02:45Z daniel $
 */
public class JHelper {

	/**
	 * This methode returns, if array contains obj. If the array and object are
	 * from incompatible types you also get a false, cause obj can not be found
	 * in the array.
	 * 
	 * @param array
	 *            Array to be searched
	 * @param obj
	 *            Object to be found in the array
	 * @return <code>true</code> - obj found, <code>false</code> - nothing
	 *         found
	 */
	public static boolean ArrayContains(Object[] array, Object obj) {
		for (int i = 0; i < array.length; i++) {
			if (array[i].equals(obj)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * This methode formats the bytes in human readable form. With the
	 * lastDigits you can specify the number of digits after the '.'. This is
	 * turned off, if the value is less equal zero.
	 * 
	 * @param bytes
	 *            number of bytes
	 * @param lastDigits
	 *            number of digits after the `.` point
	 * @return String representation of bytes
	 */
	public static String formatByteSize(long bytes, int lastDigits) {
		String formated = null;
		double work = bytes;
		long count = 0;

		// digits
		while (work >= 1024) {
			work /= 1024;
			count++;
		}
		formated = String.valueOf(work);

		// format last digits
		if (lastDigits > 0) {
			int pos = formated.lastIndexOf('.');
			if (formated.length() - pos - 1 < lastDigits) {
				for (int i = formated.length() - pos - 1; i < lastDigits; i++)
					formated += "0";
			} else {
				formated = formated.substring(0, pos + lastDigits + 1);
			}
		}

		// unit
		if (count == 0) {
			formated += " B";
		} else if (count == 1) {
			formated += " kB";
		} else if (count == 2) {
			formated += " MB";
		} else if (count == 3) {
			formated += " GB";
		} else if (count == 4) {
			formated += " TB";
		} else if (count == 5) {
			formated += " PB";
		}

		return formated;
	}

	/**
	 * Copy an input stream to an output stream. Copied from jEdit Project.
	 * 
	 * @param bufferSize
	 *            the size of the buffer
	 * @param progress
	 *            the progress observer it could be null
	 * @param in
	 *            the input stream
	 * @param out
	 *            the output stream
	 * @param canStop
	 *            if true, the copy can be stopped by interrupting the thread
	 * @return <code>true</code> if the copy was done, <code>false</code> if
	 *         it was interrupted
	 * @throws IOException
	 *             IOException If an I/O error occurs
	 */
	public static boolean copyStream(int bufferSize, /*ProgressObserver progress,*/ InputStream in, OutputStream out, boolean canStop) throws IOException {
		byte[] buffer = new byte[bufferSize];
		int n;
		long copied = 0L;
		while (-1 != (n = in.read(buffer))) {
			out.write(buffer, 0, n);
			copied += n;
//			if (progress != null)
//				progress.setValue(copied);
			if (canStop && Thread.interrupted())
				return false;
		}
		return true;
	}

	/**
	 * Copy an input stream to an output stream with a buffer of 4096 bytes.
	 * 
	 * @param progress
	 *            the progress observer it could be null
	 * @param in
	 *            the input stream
	 * @param out
	 *            the output stream
	 * @param canStop
	 *            if true, the copy can be stopped by interrupting the thread
	 * @return <code>true</code> if the copy was done, <code>false</code> if
	 *         it was interrupted
	 * @throws IOException
	 *             IOException If an I/O error occurs
	 */
	public static boolean copyStream(/*ProgressObserver progress,*/ InputStream in, OutputStream out, boolean canStop) throws IOException {
		return copyStream(4096, /*progress,*/ in, out, canStop);
	}
	
	/**
	 * Makes a deep copy of a gives DMTreeNode. The new TreeNode is constructed 
	 * below the new root <i>out</i>.
	 * 
	 * @param out DeepCopy of TreeNode
	 * @param in Input TreeNode
	 */
	public static void copyDMTreeNode(DefaultMutableTreeNode out, DefaultMutableTreeNode in) {
		int curDepth = 0;
		DefaultMutableTreeNode cur = null;
		DefaultMutableTreeNode new_node = null;
		DefaultMutableTreeNode curDepthNode = null;
		

		// skip old root
		cur = in;
		curDepth = cur.getLevel();
		curDepthNode = out;

		// reconstruct tree
		cur = (DefaultMutableTreeNode) cur.getNextNode();
		while (cur != null) {
			new_node = (DefaultMutableTreeNode) cur.clone();
			
			if (cur.getLevel() > curDepth) { // new child
				curDepthNode.add(new_node);
			} else if (cur.getLevel() < curDepth) { // new Parent
				((DefaultMutableTreeNode) curDepthNode.getParent().getParent()).add(new_node);
			} else { // new 'brother'
				((DefaultMutableTreeNode) curDepthNode.getParent()).add(new_node);
			}

			// save last node info
			curDepthNode = new_node;
			curDepth = cur.getLevel();
			
			// next node
			cur = (DefaultMutableTreeNode) cur.getNextNode();
		}
	}
}
