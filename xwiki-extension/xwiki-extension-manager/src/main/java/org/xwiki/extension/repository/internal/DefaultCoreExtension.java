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
package org.xwiki.extension.repository.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xwiki.extension.CoreExtension;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionException;
import org.xwiki.extension.repository.ExtensionRepository;

public class DefaultCoreExtension implements CoreExtension
{
    private URL url;

    private String name;

    private String version;

    private String type;

    private String description;

    private String author;

    private String website;

    private List<ExtensionDependency> dependencies = new ArrayList<ExtensionDependency>();

    private DefaultCoreExtensionRepository repository;

    public DefaultCoreExtension(String name, String version)
    {
        this.name = name;
        this.version = version;
    }

    public DefaultCoreExtension(DefaultCoreExtensionRepository repository, URL url, InputStream descriptorStream)
        throws ParserConfigurationException, SAXException, IOException
    {
        this.repository = repository;

        this.url = url;

        parseDescriptor(descriptorStream);
    }

    private void parseDescriptor(InputStream descriptorStream) throws ParserConfigurationException, SAXException,
        IOException
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(descriptorStream);

        Node projectNode = extractElement(doc, "project");

        Node groupIdNode = extractElement(projectNode, "groupId");
        Node artifactIdNode = extractElement(projectNode, "artifactId");
        Node versionNode = extractElement(projectNode, "version");

        if (versionNode == null || groupIdNode == null) {
            Node parentNode = extractElement(projectNode, "parent");

            if (groupIdNode == null) {
                groupIdNode = extractElement(parentNode, "groupId");
            }

            if (versionNode == null) {
                versionNode = extractElement(parentNode, "version");
            }
        }

        this.name = groupIdNode.getTextContent() + ":" + artifactIdNode.getTextContent();
        this.version = versionNode.getTextContent();
    }

    private Node extractElement(Node node, String elementName)
    {
        NodeList nodeChildren = node.getChildNodes();
        for (int i = 0; i < nodeChildren.getLength(); ++i) {
            Node childNode = nodeChildren.item(i);

            if (childNode.getNodeName().equals(elementName)) {
                return childNode;
            }
        }

        return null;
    }

    // Extension

    public void download(File file) throws ExtensionException
    {
        // TODO
    }

    public String getName()
    {
        return this.name;
    }

    public String getVersion()
    {
        return this.version;
    }

    public String getType()
    {
        return this.type;
    }

    public String getDescription()
    {
        return this.description;
    }

    public String getAuthor()
    {
        return this.author;
    }

    public String getWebSite()
    {
        return this.website;
    }

    public List<ExtensionDependency> getDependencies()
    {
        return Collections.unmodifiableList(this.dependencies);
    }

    public ExtensionRepository getRepository()
    {
        return this.repository;
    }

    // CoreExtension

    public URL getURL()
    {
        return this.url;
    }
}
