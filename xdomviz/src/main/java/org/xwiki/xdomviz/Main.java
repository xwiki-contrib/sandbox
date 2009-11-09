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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.syntax.Syntax;

public class Main
{
    /**
     * Entry point
     */
    public static void main(String[] args) throws Exception
    {
        // Parse command line
        Options options = new Options();
        options.addOption("n", false, "Normalize the XDOM");
        options.addOption("o", true, "Output file name");
        options.addOption("h", false, "Help");

        CommandLineParser parser = new PosixParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(String.format("%s [options] [input file]", Main.class.getName()),
                "If no input file is specified, standard input is used", options, "");
            return;
        }

        boolean normalize = false;
        if (cmd.hasOption("n")) {
            normalize = true;
        }

        String outputFileName = null;
        if (cmd.hasOption("o")) {
            outputFileName = cmd.getOptionValue("o");
        }

        String inputFileName = null;
        if (cmd.getArgs().length > 0) {
            inputFileName = cmd.getArgs()[0];
        }

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        if (inputFileName != null) {
            input = new BufferedReader(new FileReader(new File(inputFileName)));
        }

        PrintWriter output = new PrintWriter(new OutputStreamWriter(System.out));
        if (outputFileName != null) {
            output = new PrintWriter(new FileWriter(new File(outputFileName)));
        }

        // Read the text to parse
        String text = readText(input);

        // Initialize Rendering components and allow getting instances
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();
        ecm.initialize(ClassLoader.getSystemClassLoader());

        // Parse XWiki 2.0 Syntax using a Parser.
        Parser xdomParser = ecm.lookup(Parser.class, Syntax.XWIKI_2_0.toIdString());
        XDOM xdom = xdomParser.parse(new StringReader(text));

        // Create the "working" tree
        Node root = createNodeTree(xdom);
        
        // Normalize it if requested
        if (normalize) {
            root = normalize(root);
        }

        // Write the output
        output.append(generateGraphViz(root));

        // Close everything
        output.close();
        input.close();
    }

    /**
     * <p>
     * Helper method which finds a node associated to a block in a nodelist.
     * </p>
     * 
     * @param nodes The nodelist.
     * @param block The block to be searched for in nodes.
     * @return The node corresponding to the given block.
     */
    public static Node findNode(List<Node> nodes, Block block)
    {
        for (Node node : nodes) {
            if (node.getBlock().equals(block)) {
                return node;
            }
        }

        return null;
    }

    /**
     * <p>
     * This method creates an isomorphic tree using node structures instead of blocks. This is necessary because a
     * single XDOM's block can be a child of multiple parents but the getParent() method is able to return only a single
     * parent. Using this alternative representation, full parent information is correctly stored in each node.
     * </p>
     * <p>
     * The node tree representation allows also the manipulation of the tree structure because all the attributes of a
     * node are mutable.
     * </p>
     * 
     * @param xdom
     * @return The root node of the new tree.
     */
    private static Node createNodeTree(XDOM xdom)
    {
        // The list of the nodes created from the visited XDOM's blocks.
        List<Node> nodes = new ArrayList<Node>();

        // Breadth first visit of the XDOM.
        Queue<Block> blocksQueue = new ArrayDeque<Block>();
        blocksQueue.add(xdom.getRoot());
        while (!blocksQueue.isEmpty()) {
            Block block = blocksQueue.poll();

            // If there isn't a node corresponding to this block, create it!
            Node parentNode = findNode(nodes, block);
            if (parentNode == null) {
                parentNode = new Node(block);
                nodes.add(parentNode);
            }

            for (Block child : block.getChildren()) {
                blocksQueue.add(child);

                // If there isn't a node corresponding to this child-block, create it!
                Node childNode = findNode(nodes, child);
                if (childNode == null) {
                    childNode = new Node(child);
                    nodes.add(childNode);
                }

                // Link parent and child child.
                parentNode.getChildren().add(childNode);
                childNode.getParents().add(parentNode);
            }
        }

        return findNode(nodes, xdom.getRoot());
    }

    /**
     * <p>
     * This method performs a normalization of the tree by removing all the multi-edges (due to a node having multiple
     * parents). This happens often with SpaceBlocks, which are reused all over the XDOM. The algorithm performs a
     * breadth first visit. For each visited node, it checks how many times a child occurs in its children list. If a
     * child occurs more than once, then we create new nodes, one for each occurrence.
     * </p>
     * <p>
     * The node tree corresponding to the XDOM of "this is a test" is:
     * </p>
     * <ul>
     * <li>XDOM -> P</li>
     * <li>P -> "This"</li>
     * <li>P -> S (3 edges, each one representing a space)</li>
     * <li>P -> "is"</li>
     * <li>P -> "a"</li>
     * <li>P -> "test"</li>
     * </ul>
     * <p>
     * The normalized tree will be:
     * </p>
     * <ul>
     * <li>XDOM -> P</li>
     * <li>P -> "This"</li>
     * <li>P -> S</li>
     * <li>P -> "is"</li>
     * <li>P -> S</li>
     * <li>P -> "a"</li>
     * <li>P -> S</li>
     * <li>P -> "test"</li>
     * </ul>
     * <p>
     * In a normalized tree, each node has one and only one parent.
     * </p>
     * 
     * @param root The root node of the tree.
     * @return The root node of the normalized tree.
     */
    private static Node normalize(Node root)
    {
        // Breadth first visit of the XDOM to assign simple ids to nodes.
        Queue<Node> nodesQueue = new ArrayDeque<Node>();
        nodesQueue.add(root);
        while (!nodesQueue.isEmpty()) {
            Node node = nodesQueue.poll();

            // This map contains, for the current node, where are the occurrences in the children list of each child.
            Map<Node, List<Integer>> nodeToIndexesMap = new HashMap<Node, List<Integer>>();

            int i = 0;
            // For each child calculate store the its position in the indexes list.
            for (Node child : node.getChildren()) {
                List<Integer> indexes = nodeToIndexesMap.get(child);
                if (indexes == null) {
                    indexes = new ArrayList<Integer>();
                    nodeToIndexesMap.put(child, indexes);
                }
                indexes.add(i);
                i++;
            }

            for (Node child : nodeToIndexesMap.keySet()) {
                List<Integer> indexes = nodeToIndexesMap.get(child);
                // If the indexes size is > 1 then a child occurs multiple times in a
                if (indexes.size() > 1) {
                    for (Integer index : indexes) {
                        Node newNode = new Node(child.getBlock());
                        newNode.getParents().add(node);
                        newNode.getChildren().addAll(child.getChildren());
                        node.getChildren().set(index, newNode);
                    }
                }
            }

            for (Node child : node.getChildren()) {
                nodesQueue.add(child);
            }
        }

        return root;
    }

    /**
     * <p>
     * This method produces the GraphViz source code corresponding to the node tree.
     * </p>
     * 
     * @param root The node tree root.
     * @return A string containing the GraphViz source code to display the node tree.
     */
    private static String generateGraphViz(Node root)
    {
        // The rendering buffer.
        StringBuffer sb = new StringBuffer();

        // This map contains the GraphViz Ids (integers) associated to the nodes in the tree.
        Map<Node, Integer> nodeToIdMap = new HashMap<Node, Integer>();

        sb.append("digraph XDOM {\n");

        // Breadth first visit of the XDOM to assign simple ids to nodes.
        Queue<Node> nodesQueue = new ArrayDeque<Node>();
        nodesQueue.add(root);
        // Counter used to keep track of the assigned ids. It's incremented at each time a new node is found for the
        // first time.
        int i = 0;
        while (!nodesQueue.isEmpty()) {
            Node node = nodesQueue.poll();

            if (nodeToIdMap.get(node) == null) {
                nodeToIdMap.put(node, i);
                i++;
            }

            for (Node child : node.getChildren()) {
                if (nodeToIdMap.get(child) == null) {
                    nodeToIdMap.put(child, i);
                    i++;
                }

                nodesQueue.add(child);

                // Render the edge.
                sb.append(String.format("%d -> %d;\n", nodeToIdMap.get(node), nodeToIdMap.get(child)));
            }
        }

        // Render the label assignment.
        for (Node node : nodeToIdMap.keySet()) {
            sb.append(String.format("%d [label = \"%s\"];\n", nodeToIdMap.get(node), node));
        }

        sb.append("}\n");

        return sb.toString();
    }

    /**
     * Read text data to a String
     * @param reader The reader where to read from.
     * @return The string containing the text read from the reader.
     * @throws IOException
     */
    private static String readText(BufferedReader reader) throws IOException
    {
        StringBuffer sb = new StringBuffer();

        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        return sb.toString();
    }
}
