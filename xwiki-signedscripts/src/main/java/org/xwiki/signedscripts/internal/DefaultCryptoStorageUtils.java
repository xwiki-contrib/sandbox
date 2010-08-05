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
 */
package org.xwiki.signedscripts.internal;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;


/**
 * Default implementation of {@link CryptoStorageUtils}. This class uses objects to store 
 * 
 * @version $Id$
 * @since 2.5
 */
@Component
public class DefaultCryptoStorageUtils implements CryptoStorageUtils
{
    /** The name of the XClass which represents a user's certificate. */
    private final String certClassName = "XWiki.X509CertificateClass";

    /** The name of the property in the certificate XClass which represents the entire certificate in PEM format. */
    private final String certFingerprintPropertyName = "fingerprint";

    /** DocumentAccessBridge for getting the current user's document and URL. */
    @Requirement
    private DocumentAccessBridge bridge;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.signedscripts.internal.CryptoStorageUtils#getCertificateFingerprintsForUser(java.lang.String)
     */
    public List<String> getCertificateFingerprintsForUser(final String userName)
    {
        List<String> out = new ArrayList<String>();
        String certFingerprint = (String) this.bridge.getProperty(userName,
                                                                  this.certClassName,
                                                                  0,
                                                                  this.certFingerprintPropertyName);
        for (int counter = 0; certFingerprint != null; counter++) {
            out.add(certFingerprint);
            certFingerprint = (String) this.bridge.getProperty(userName,
                                                               this.certClassName, 
                                                               counter,
                                                               this.certFingerprintPropertyName);
            if (counter > 500) {
                throw new InfiniteLoopException("Either the document " + userName + " has over 500 "
                                                + this.certClassName
                                                + " objects or something went wrong. Chickening out...");
            }
        }
        return out;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.signedscripts.internal.CryptoStorageUtils#addCertificateFingerprint(java.lang.String, java.lang.String)
     */
    public void addCertificateFingerprint(String userName, String fingerprint) throws Exception
    {
        // FIXME this method changes the 0-th object, need to add a new object
        this.bridge.setProperty(userName, this.certClassName, this.certFingerprintPropertyName, fingerprint);
    }

    /**
     * Thrown when a loop has looped over an unreasonable number of cycles and is probably looping infinitely.
     * 
     * @version $Id$
     * @since 2.5
     */
    public static class InfiniteLoopException extends RuntimeException
    {
        /** Version ID. */
        private static final long serialVersionUID = -7135937602338126967L;

        /**
         * The Constructor.
         *
         * @param message the message to give in the Exception
         */
        public InfiniteLoopException(String message)
        {
            super(message);
        }
    }
}

