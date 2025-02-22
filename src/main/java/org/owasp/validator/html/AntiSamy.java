/*
 * Copyright (c) 2007-2019, Arshan Dabirsiaghi, Jason Li
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of OWASP nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.owasp.validator.html;

import org.owasp.validator.html.scan.AntiSamyDOMScanner;
import org.owasp.validator.html.scan.AntiSamySAXScanner;

import java.io.File;
import java.io.Reader;
import java.io.Writer;

/**
 * 
 * This is the only class from which the outside world should be calling. The
 * <code>scan()</code> method holds the meat and potatoes of AntiSamy. The file
 * contains a number of ways for <code>scan()</code>'ing depending on the
 * accessibility of the policy file.
 * 
 * @author Arshan Dabirsiaghi
 */

public class AntiSamy {

	public static final int DOM = 0;
	public static final int SAX = 1;

	private Policy policy = null;

	public AntiSamy() {
	}

	public AntiSamy(Policy policy) {
		this.policy = policy;
	}

	/**
	 * The meat and potatoes. The <code>scan()</code> family of methods are the
	 * only methods the outside world should be calling to invoke AntiSamy.
	 * 
	 * @param taintedHTML Untrusted HTML which may contain malicious code.
	 * @return A <code>CleanResults</code> object which contains information
	 *         about the scan (including the results).
	 * @throws ScanException When there is a problem encountered
	 *         while scanning the HTML.
	 * @throws PolicyException When there is a problem reading the policy file.
	 */
	public CleanResults scan(String taintedHTML) throws ScanException, PolicyException {
		return this.scan(taintedHTML, this.policy, SAX);
	}

	/**
	 * This method sets <code>scan()</code> to use the specified scan type.
	 * 
	 * @param taintedHTML Untrusted HTML which may contain malicious code.
	 * @param scanType The type of scan (DOM or SAX).
	 * @return A <code>CleanResults</code> object which contains information
	 *         about the scan (including the results).
	 * @throws ScanException When there is a problem encountered
	 *         while scanning the HTML.
	 * @throws PolicyException When there is a problem reading the policy file.
	 */
	public CleanResults scan(String taintedHTML, int scanType) throws ScanException, PolicyException {

		return this.scan(taintedHTML, this.policy, scanType);
	}

	/**
	 * This method wraps <code>scan()</code> using the Policy object passed in.
	 * 
	 * @param taintedHTML Untrusted HTML which may contain malicious code.
	 * @param policy The custom policy to enforce.
	 * @return A <code>CleanResults</code> object which contains information
	 *         about the scan (including the results).
	 * @throws ScanException When there is a problem encountered
	 *         while scanning the HTML.
	 * @throws PolicyException When there is a problem reading the policy file.
	 */
	public CleanResults scan(String taintedHTML, Policy policy) throws ScanException, PolicyException {
		return this.scan(taintedHTML, policy, DOM);
	}

	/**
	 * This method wraps <code>scan()</code> using the Policy object passed in and the specified scan type.
	 * 
	 * @param taintedHTML Untrusted HTML which may contain malicious code.
	 * @param policy The custom policy to enforce.
	 * @param scanType The type of scan (DOM or SAX).
	 * @return A <code>CleanResults</code> object which contains information
	 *         about the scan (including the results).
	 * @throws ScanException When there is a problem encountered
	 *         while scanning the HTML.
	 * @throws PolicyException When there is a problem reading the policy file.
	 */
	public CleanResults scan(String taintedHTML, Policy policy, int scanType) throws ScanException, PolicyException {
		if (policy == null) {
			throw new PolicyException("No policy loaded");
		}

		if (scanType == DOM) {
			return new AntiSamyDOMScanner(policy).scan(taintedHTML);
		} else {
			return new AntiSamySAXScanner(policy).scan(taintedHTML);
		}
	}
	
	/**
	 * Use this method if caller has Streams rather than Strings for I/O
	 * Useful for servlets where the response is very large and we don't validate, 
	 * simply encode as bytes are consumed from the stream.
	 * @param reader Reader that produces the input, possibly a little at a time
	 * @param writer Writer that receives the cleaned output, possibly a little at a time
	 * @param policy Policy that directs the scan
	 * @return CleanResults where the cleanHtml is null. If caller wants the clean HTML, it
	 *         must capture the writer's contents. When using Streams, caller generally
	 *         doesn't want to create a single string containing clean HTML.
	 * @throws ScanException When there is a problem encountered
	 *         while scanning the HTML.
	 */
	public CleanResults scan(Reader reader, Writer writer, Policy policy) throws ScanException {
	    return (new AntiSamySAXScanner(policy)).scan(reader, writer);
	}

	/**
	 * This method wraps <code>scan()</code> using the Policy in the specified file.
	 * 
	 * @param taintedHTML Untrusted HTML which may contain malicious code.
	 * @param filename The file name of the custom policy to enforce.
	 * @return A <code>CleanResults</code> object which contains information
	 *         about the scan (including the results).
	 * @throws ScanException When there is a problem encountered
	 *         while scanning the HTML.
	 * @throws PolicyException When there is a problem reading the policy file.
	 */
	public CleanResults scan(String taintedHTML, String filename) throws ScanException, PolicyException {

        Policy policy = Policy.getInstance(filename);

        return this.scan(taintedHTML, policy);
	}

	/**
	 * This method wraps <code>scan()</code> using the policy File object passed in.
	 * 
	 * @param taintedHTML Untrusted HTML which may contain malicious code.
	 * @param policyFile The File object of the custom policy to enforce.
	 * @return A <code>CleanResults</code> object which contains information
	 *         about the scan (including the results).
	 * @throws ScanException When there is a problem encountered
	 *         while scanning the HTML.
	 * @throws PolicyException When there is a problem reading the policy file.
	 */
	public CleanResults scan(String taintedHTML, File policyFile) throws ScanException, PolicyException {

        Policy policy = Policy.getInstance(policyFile);

        return this.scan(taintedHTML, policy);
	}

}
