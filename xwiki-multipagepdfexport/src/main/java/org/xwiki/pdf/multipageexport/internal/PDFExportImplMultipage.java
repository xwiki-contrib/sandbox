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
package org.xwiki.pdf.multipageexport.internal;

import java.io.InputStream;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.pdf.impl.PdfExportImpl;

/**
 * Extends PDFExportImpl to overwrite the xhml2fo.xsl retrieval to direct it to xhtml2fo-multipage.xsl before falling
 * back on default. <br />
 * TODO: this is a bit of a hackish implementation, since PdfExportImpl was not made for this, it doesn't expose exactly
 * the right functions and variables so we overwrite what we can.
 * 
 * @version $Id$
 */
class PDFExportImplMultipage extends PdfExportImpl
{
    /** The name of the default XHTML2FOP transformation file for the multipage case. */
    private static final String DEFAULT_XHTML2FOP_XSLT_MULTIPAGE = "xhtml2fo-multipage.xsl";

    /**
     * So, so unfortunately that the function {@link PdfExportImpl#getXhtml2FopXslt(XWikiContext context)} is private so
     * we cannot overwrite that one. However, that function calls this one with the "xhtmlxsl" parameter on the first
     * position, so we'll be able to intercept that call and pass it through xhtml2fo-multipage.xsl before falling back
     * to default. <br /> {@inheritDoc}
     * 
     * @see com.xpn.xwiki.pdf.impl.PdfExportImpl#getXslt(java.lang.String, java.lang.String, com.xpn.xwiki.XWikiContext)
     */
    @Override
    protected InputStream getXslt(String propertyName, String fallbackFile, XWikiContext context)
    {
        if (propertyName.equals("xhtmlxsl")) {
            // we need xhtml2fo.xsl, let's work some magic: Try first to get the customized one, falling back on the
            // default multipage file from the resource. If it returns null, it means that there is no customization
            // in the pdf template and the file doesn't exist so we need to fallback on the default one
            InputStream multipagexsl = super.getXslt(propertyName, DEFAULT_XHTML2FOP_XSLT_MULTIPAGE, context);
            if (multipagexsl == null) {
                multipagexsl = super.getXslt(propertyName, fallbackFile, context);
            }
            return multipagexsl;
        } else {
            return super.getXslt(propertyName, fallbackFile, context);
        }
    }
}
