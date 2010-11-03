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
 */

package com.xpn.xwiki.doc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * The content of an attachment. This implementation is based on a BinaryObject.
 *
 * @version $Id$
 */
public class XWikiBinaryObjectAttachmentContent extends XWikiAttachmentContent
{
    private final BinaryObjectProvider provider;

    /** The storage. Not final because it may be changed if an attachment is cloned then written to. */
    private BinaryObject storage;

    /** If true then the storage is shared and must be copied if it is to be written to. */
    private boolean isShared;

    private XWikiBinaryObjectAttachmentContent(final BinaryObjectProvider provider,
                                               final BinaryObject storage)
    {
        this.provider = provider;
        this.storage = storage;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.doc.XWikiAttachmentContent#clone()
     */
    @Override
    public XWikiBinaryObjectAttachmentContent clone()
    {
        final XWikiBinaryObjectAttachmentContent clone =
            new XWikiBinaryObjectAttachmentContent(this.provider, this.storage);
        clone.isShared = true;
        this.isShared = true;
        return clone;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.doc.XWikiAttachmentContent#getContent()
     */
    @Deprecated
    public byte[] getContent()
    {
        return this.file.get();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.doc.XWikiAttachmentContent#getContentInputStream()
     */
    public InputStream getContentInputStream()
    {
        try {
            return this.storage.getContent();
        } catch (IOException e) {
            throw new RuntimeException("Failed to get InputStream", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.doc.XWikiAttachmentContent#setContent(InputStream is)
     */
    public void setContent(final InputStream is) throws IOException
    {
        if (this.isShared) {
            /*
             * If this object shares storage with a clone then it must create new storage.
             * If it was possible to append content then we would have top copy over the content from the
             * BinaryObject but it's not so we don't have to.
             */
            this.storage = this.provider.get();
            this.isShared = false;
        }
        this.storage.addContent(is);
        this.setContentDirty(true);
        this.attachment.setFilesize(this.getSize());
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.doc.XWikiAttachmentContent#getSize()
     */
    public int getSize()
    {
        long size = this.storage.size();
        // The most important thing is that it doesn't roll over into the negative space.
        if (size > ((long) Integer.MAX_VALUE)) {
            return Integer.MAX_VALUE;
        }
        return (int) size;
    }

    /**
     * Persist the content which has been set using setContent(), content will not be available to getContent()
     * until this is called.
     */
    public void save()
    {
        this.storage.save();
        this.setContentDirty(false);
    }
}
