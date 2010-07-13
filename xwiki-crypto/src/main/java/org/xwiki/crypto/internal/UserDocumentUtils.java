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
package org.xwiki.crypto.internal;

import java.util.List;
import java.util.ArrayList;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.DocumentReference;

/**
 * Means by which to get a string representation of the user page for the current user for inclusion in a certificate.
 * Also allows to get a user's certificates based on the username.
 * This component has no cryptographic code.
 * 
 * @version $Id$
 * @since 2.5
 */
@Component
@ComponentRole
public class UserDocumentUtils
{
    /** The name of the XClass which represents a user's certificate. */
    private final String certClassName = "XWiki.X509CertificateClass";

    /** The name of the property in the certificate XClass which represents the entire certificate in PEM format. */
    private final String certFingerprintPropertyName = "fingerprint";

    /** DocumentAccessBridge for getting the current user's document and URL. */
    @Requirement
    DocumentAccessBridge bridge;

    /** Resolver which can make a DocumentReference out of a String. */
    @Requirement(role = String.class)
    DocumentReferenceResolver<String> resolver;

    /** Serializer to turn a document reference into a String which can be put in a certificate. */
    @Requirement(role = String.class)
    EntityReferenceSerializer<String> serializer;

    /** @return The fully qualified name of the current user's document eg: xwiki:XWiki.JohnSmith. */
    public String getCurrentUser()
    {
        String localName = this.bridge.getCurrentUser();
        DocumentReference dr = this.resolver.resolve(localName);
        return this.serializer.serialize(dr);
    }

    /**
     * Get the external URL pointing to the given user document.
     *
     * @param userName the string representation of the document reference for the user document.
     * @return A string representation of the external URL for the user doc.
     */
    public String getUserDocURL(String userDocName)
    {
        DocumentReference dr = this.resolver.resolve(userDocName);
        return this.bridge.getDocumentURL(dr, "view", "", "");
    }

    /**
     * Get the X509Certificate fingerprints for the named user.
     *
     * @param userName the string representation of the document reference for the user document.
     * @return A list of all of this user's authorized certificate fingerprints.
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
     * Thrown when a loop has looped over an unreasonable number of cycles and is probably looping infinitely.
     * 
     * @version $Id$
     * @since 2.5
     */
    public static class InfiniteLoopException extends RuntimeException
    {
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
