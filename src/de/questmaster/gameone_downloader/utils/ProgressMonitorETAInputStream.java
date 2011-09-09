/*
 * Copyright (C) 2011 Daniel Jacobi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * @(#)ProgressMonitorInputStream.java	1.20 05/11/17
 *
 */


package de.questmaster.gameone_downloader.utils;


import javax.swing.*;


import java.io.*;
import java.util.Calendar;
import java.awt.Component;



/**
 * Monitors the progress of reading from some InputStream. This ProgressMonitor
 * is normally invoked in roughly this form:
 * <pre>
 * InputStream in = new BufferedInputStream(
 *                          new ProgressMonitorInputStream(
 *                                  parentComponent,
 *                                  "Reading " + fileName,
 *                                  new FileInputStream(fileName)));
 * </pre><p>
 * This creates a progress monitor to monitor the progress of reading
 * the input stream.  If it's taking a while, a ProgressDialog will
 * be popped up to inform the user.  If the user hits the Cancel button
 * an InterruptedIOException will be thrown on the next read.
 * All the right cleanup is done when the stream is closed.
 *
 *
 * <p>
 *
 * For further documentation and examples see
 * <a href="http://java.sun.com/docs/books/tutorial/uiswing/components/progress.html">How to Monitor Progress</a>,
 * a section in <em>The Java Tutorial.</em>
 *
 * @see ProgressMonitor
 * @see JOptionPane
 * @author James Gosling (original ProgressMonitorInputStream)
 * @author Daniel Jacobi
 * @version 1.00 01/05/09
 */
public class ProgressMonitorETAInputStream extends FilterInputStream
{
    private ProgressMonitor monitor;
    private int             nread = 0;
    private int             size = 0;
    protected long			old_timestamp = 0;
    protected long			old_nread = 0;
    protected String		old_eta = null;


    /**
     * Constructs an object to monitor the progress of an input stream.
     *
     * @param message Descriptive text to be placed in the dialog box
     *                if one is popped up.
     * @param parentComponent The component triggering the operation
     *                        being monitored.
     * @param in The input stream to be monitored.
     */
    public ProgressMonitorETAInputStream(Component parentComponent,
                                      Object message,
                                      InputStream in) {
        super(in);
        try {
            size = in.available();
        }
        catch(IOException ioe) {
            size = 0;
        }
        monitor = new ProgressMonitor(parentComponent, message, " ", 0, size);
    }

    /**
     * Set the size of the stream manually. Handy when no automatic detection possible.
     *
     * @param size Size of transfer
     */
    public void setSize(int size) {
    	this.size = size;
    	monitor.setMaximum(size);
    }

	protected String processETA() {
		long timestamp = System.currentTimeMillis();
		long bps = 0;
		Calendar eta = null;
		String sTime = null;
		long throughput = nread - old_nread;

		// set timestamp for the first time
		if (old_timestamp == 0) {
			old_eta = " ";
			old_nread = nread;
			old_timestamp = timestamp;
		}

		if (old_timestamp > 0 && old_timestamp != timestamp
				&& ((timestamp - old_timestamp) / 1000) > 0) {
			// compute throughput
			bps = throughput / ((timestamp - old_timestamp) / 1000);
			// compute eta-time
			eta = Calendar.getInstance();
			eta.setTimeInMillis(((size - nread + 1) / bps) * 1000);

			sTime = eta.get(Calendar.HOUR_OF_DAY) < 10 ? "0"
					+ (eta.get(Calendar.HOUR_OF_DAY) - 1) : ""
					+ (eta.get(Calendar.HOUR_OF_DAY) - 1);
			sTime += ((eta.get(Calendar.MINUTE) < 10) ? ":0"
					+ eta.get(Calendar.MINUTE) : ":" + eta.get(Calendar.MINUTE));
			sTime += ((eta.get(Calendar.SECOND) < 10) ? ":0"
					+ eta.get(Calendar.SECOND) : ":" + eta.get(Calendar.SECOND));

			old_eta = "" + sTime + " ("	+ JHelper.formatByteSize(bps, 1) + "/s)";
			old_nread = nread;
			old_timestamp = timestamp;

		}
		return old_eta;
	}


    /**
     * Get the ProgressMonitor object being used by this stream. Normally
     * this isn't needed unless you want to do something like change the
     * descriptive text partway through reading the file.
     * @return the ProgressMonitor object used by this object
     */
    public ProgressMonitor getProgressMonitor() {
        return monitor;
    }


    /**
     * Overrides <code>FilterInputStream.read</code>
     * to update the progress monitor after the read.
     */
    public int read() throws IOException {
        int c = in.read();
        if (c >= 0) {
        	monitor.setProgress(++nread);
        	monitor.setNote(processETA());
        }
        if (monitor.isCanceled()) {
            InterruptedIOException exc =
                                    new InterruptedIOException("progress");
            exc.bytesTransferred = nread;
            throw exc;
        }
        return c;
    }


    /**
     * Overrides <code>FilterInputStream.read</code>
     * to update the progress monitor after the read.
     */
    public int read(byte b[]) throws IOException {
        int nr = in.read(b);
        if (nr > 0) {
        	monitor.setProgress(nread += nr);
        	monitor.setNote(processETA());
        }
        if (monitor.isCanceled()) {
            InterruptedIOException exc =
                                    new InterruptedIOException("progress");
            exc.bytesTransferred = nread;
            throw exc;
        }
        return nr;
    }


    /**
     * Overrides <code>FilterInputStream.read</code>
     * to update the progress monitor after the read.
     */
    public int read(byte b[],
                    int off,
                    int len) throws IOException {
        int nr = in.read(b, off, len);
        if (nr > 0) {
        	monitor.setProgress(nread += nr);
        	monitor.setNote(processETA());
        }
        if (monitor.isCanceled()) {
            InterruptedIOException exc =
                                    new InterruptedIOException("progress");
            exc.bytesTransferred = nread;
            throw exc;
        }
        return nr;
    }


    /**
     * Overrides <code>FilterInputStream.skip</code>
     * to update the progress monitor after the skip.
     */
    public long skip(long n) throws IOException {
        long nr = in.skip(n);
        if (nr > 0) {
        	monitor.setProgress(nread += nr);
        	monitor.setNote(processETA());
        }
        return nr;
    }


    /**
     * Overrides <code>FilterInputStream.close</code>
     * to close the progress monitor as well as the stream.
     */
    public void close() throws IOException {
        in.close();
        monitor.close();
    }


    /**
     * Overrides <code>FilterInputStream.reset</code>
     * to reset the progress monitor as well as the stream.
     */
    public synchronized void reset() throws IOException {
        in.reset();
        nread = size - in.available();
        monitor.setProgress(nread);
        monitor.setNote("Reset");
    }
}
