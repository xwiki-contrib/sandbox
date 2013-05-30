/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 * 
 * Part of the code in this file is copied from: https://github.com/auth10/auth10-java
 * which is based on Microsoft libraries in: https://github.com/WindowsAzure/azure-sdk-for-java-samples. 
 * 
 */


package com.xwiki.authentication.sts;

public class STSException extends Exception {
	private static final long serialVersionUID = 1L;

	public STSException() {
	}

	public STSException(final String s) {
		super(s);
	}

	public STSException(final Throwable throwable) {
		super(throwable);
	}

	public STSException(final String s, final Throwable throwable) {
		super(s, throwable);
	}
}
