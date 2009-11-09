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
package org.xwiki.xdomviz;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.FormatBlock;
import org.xwiki.rendering.block.SpaceBlock;
import org.xwiki.rendering.block.SpecialSymbolBlock;
import org.xwiki.rendering.block.WordBlock;

/**
 * <p>
 * This class represent a node in a tree
 * </p>
 */
class Node
{
    /**
     * The XDOM's block associated to this node.
     */
    private Block block;

    /**
     * The node's children.
     */
    private List<Node> children;

    /**
     * The node's parents.
     */
    private List<Node> parents;

    public Node(Block block)
    {
        this.block = block;
        children = new ArrayList<Node>();
        parents = new ArrayList<Node>();
    }

    public Block getBlock()
    {
        return block;
    }

    public List<Node> getChildren()
    {
        return children;
    }

    public List<Node> getParents()
    {
        return parents;
    }

    @Override
    public String toString()
    {
        String label = block.getClass().getSimpleName();

        if (block instanceof FormatBlock) {
            FormatBlock formatBlock = (FormatBlock) block;
            label = String.format("Format: %s", formatBlock.getFormat().toString());
        } else if (block instanceof WordBlock) {
            WordBlock wordBlock = (WordBlock) block;
            label = String.format("%s", wordBlock.getWord());
        } else if (block instanceof SpecialSymbolBlock) {
            SpecialSymbolBlock specialSymbolBlock = (SpecialSymbolBlock) block;
            label = String.format("%c", specialSymbolBlock.getSymbol());
        } else if (block instanceof SpaceBlock) {
            label = " ";
        }

        return String.format("%s", label);
    }

}
