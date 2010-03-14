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
package org.xwiki.wikiimporter.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.xwiki.rendering.block.AbstractBlock;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.listener.Listener;

/**
 * Abstarct implementation of WikiImporterListener. This listener generates XDOM of the page content based on event call
 * backs from parser.
 * 
 * @version $Id$
 */
public abstract class AbstractWikiImporterListenerXDOM implements WikiImporterListener
{

    protected Stack<Block> stack = new Stack<Block>();

    protected final MarkerBlock marker = new MarkerBlock();

    protected static class MarkerBlock extends AbstractBlock
    {
        /**
         * {@inheritDoc}
         * 
         * @see AbstractBlock#traverse(Listener)
         */
        public void traverse(Listener listener)
        {
            // Nothing to do since this block is only used as a marker.
        }

    }

    /**
     * @return content of the page in XDOM.
     */
    public XDOM getXDOM()
    {
        return new XDOM(generateListFromStack());
    }

    protected List<Block> generateListFromStack()
    {
        List<Block> blocks = new ArrayList<Block>();
        while (!this.stack.empty()) {
            if (this.stack.peek() != this.marker) {
                blocks.add(this.stack.pop());
            } else {
                this.stack.pop();
                break;
            }
        }
        Collections.reverse(blocks);
        return blocks;
    }

}
