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

package org.xwiki.xoo;

import org.codehaus.swizzle.confluence.Attachment;
import org.codehaus.swizzle.confluence.PageSummary;
import org.xwiki.xmlrpc.XWikiXmlRpcClient;
import org.xwiki.xoo.xwiki.PageNode;
import org.xwiki.xoo.xwiki.SpaceNode;
import org.xwiki.xoo.xwiki.XWikiStructure;

import com.sun.star.awt.MouseEvent;
import com.sun.star.awt.PosSize;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.XButton;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XMouseListener;
import com.sun.star.awt.XWindow;
import com.sun.star.awt.tree.XMutableTreeDataModel;
import com.sun.star.awt.tree.XMutableTreeNode;
import com.sun.star.awt.tree.XTreeControl;
import com.sun.star.awt.tree.XTreeNode;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * The navigation panel used to display the structure of the XWiki server.
 * 
 * @version $Id$
 * @since 1.0 M
 */

public class NavigationDialog extends XWikiModelessDialog implements XMouseListener
{

    /* Singleton */
    static private NavigationDialog _instance = null;

    private XWikiExtension xWikiExtension;

    private XWikiExtensionActions xWikiActions;

    private XTreeControl xTree = null;

    private XControl xControlTree = null;

    private final String sRefreshMethod = "btnRefresh_Clicked";

    private final String sAddSpaceMethod = "btnAddSpace_Clicked";

    private final short SPACE_LEVEL = 1;

    private final short PAGE_LEVEL = 2;

    private final short ATT_LEVEL = 3;

    /**
     * @return an instance of this class
     */
    public static synchronized NavigationDialog getInstance()
    {
        XWikiExtension xwikiExtension = XWikiExtension.getInstance();
        if (null == _instance) {
            _instance = new NavigationDialog(XWikiExtension.getInstance().getComponentContext());
            xwikiExtension.getExtensionStatus().setNavBarStatus(ExtensionStatus.NEW_NAVBAR);
        }
        else
        {
            xwikiExtension.getExtensionStatus().setNavBarStatus(ExtensionStatus.OLD_NAVBAR);
        }
        return _instance;
    }

    public static synchronized boolean isNull()
    {
        return (_instance == null);
    }

    /**
     * Constructor.
     * 
     * @param c the OpenOffice component context
     */
    private NavigationDialog(XComponentContext c)
    {

        super(c, Constants.TREE_DIALOG);

        XControl dialogControl = (XControl) UnoRuntime.queryInterface(XControl.class, m_xDialog);
        XWindow dialogWin = (XWindow) UnoRuntime.queryInterface(XWindow.class, dialogControl);

        try {
            Object desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", m_xContext);
            XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, desktop);
            XFrame m_xFrame = xDesktop.getCurrentFrame();
            XWindow defaultWin = m_xFrame.getComponentWindow();

            dialogWin.setPosSize(defaultWin.getPosSize().X + defaultWin.getPosSize().Width
                - dialogWin.getPosSize().Width - 10, 0, 0, 0, PosSize.X);
        } catch (Exception e) {
            e.printStackTrace();
        }

        xWikiExtension = XWikiExtension.getInstance();
        xWikiActions = new XWikiExtensionActions(xWikiExtension);

        refreshXWikiStructure();

        xControlTree = getControl("treeXWiki");
        xTree = (XTreeControl) UnoRuntime.queryInterface(XTreeControl.class, xControlTree);
        // xTree.addSelectionChangeListener(this);
        XWindow xWindow = (XWindow) UnoRuntime.queryInterface(XWindow.class, xControlTree);
        xWindow.addMouseListener(this);

        updateTreeControl();

        XControl xRefreshButtonControl = getControl("btnRefresh");
        XButton xrefreshButton = (XButton) UnoRuntime.queryInterface(XButton.class, xRefreshButtonControl);
        xrefreshButton.addActionListener(new XActionListener()
        {
            @Override
            public void actionPerformed(com.sun.star.awt.ActionEvent arg0)
            {
                btnRefresh_Clicked();
            }

            @Override
            public void disposing(EventObject arg0)
            {
            }
        });

        XControl xAddSpaceButtonControl = getControl("btnAddSpace");
        XButton xAddSpaceButton = (XButton) UnoRuntime.queryInterface(XButton.class, xAddSpaceButtonControl);
        xAddSpaceButton.addActionListener(new XActionListener()
        {
            @Override
            public void actionPerformed(com.sun.star.awt.ActionEvent arg0)
            {
                btnAddSpace_Clicked();
            }

            @Override
            public void disposing(EventObject arg0)
            {
            }
        });

        XWikiExtension xWikiExtension = XWikiExtension.getInstance();
        String urlRefreshButton = xWikiExtension.getImagesDirUrl() + "/" + Constants.IMG_RELOAD;
        String urlAddSpaceButton = xWikiExtension.getImagesDirUrl() + "/" + Constants.IMG_ADD_SPACE;

        try {

            getPropSet("btnRefresh").setPropertyValue("ImageURL", urlRefreshButton);
            getPropSet("btnAddSpace").setPropertyValue("ImageURL", urlAddSpaceButton);

        } catch (UnknownPropertyException e) {
            e.printStackTrace();
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (WrappedTargetException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the tree data model with the structure of the XWiki server.
     */
    public void updateTreeControl()
    {

        XControlModel xTreeModel = xControlTree.getModel();
        Object xTreeData;
        try {
            xTreeData = xMCF.createInstanceWithContext("com.sun.star.awt.tree.MutableTreeDataModel", m_xContext);
            XMutableTreeDataModel mxTreeDataModel =
                (XMutableTreeDataModel) UnoRuntime.queryInterface(XMutableTreeDataModel.class, xTreeData);

            XMutableTreeNode xNode = mxTreeDataModel.createNode("root", false);
            XWikiStructure xWikiStructure = xWikiExtension.getXWikiStructure();

            buildTree(xNode, xWikiStructure, mxTreeDataModel);
            mxTreeDataModel.setRoot(xNode);

            XPropertySet xTreeModelProperty = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xTreeModel);
            xTreeModelProperty.setPropertyValue("DataModel", mxTreeDataModel);

        } catch (UnknownPropertyException e) {
            e.printStackTrace();
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (WrappedTargetException e) {
            e.printStackTrace();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Creates a tree from a XWikiStructure object.
     * 
     * @param root the root of the tree
     * @param xWikiStructure the structure of the XWiki server
     * @param mxTreeDataModel the DataModel interface
     */
    private void buildTree(XMutableTreeNode root, XWikiStructure xWikiStructure, XMutableTreeDataModel mxTreeDataModel)
    {
        if (xWikiStructure != null) {
            try {
                for (SpaceNode space : xWikiStructure.spaces) {
                    XMutableTreeNode spaceTreeNode = mxTreeDataModel.createNode(space.spaceSummary.getKey(), false);
                    for (PageNode page : space.pages) {
                        XMutableTreeNode pageTreeNode = mxTreeDataModel.createNode(page.pageSummary.getTitle(), false);
                        for (Attachment att : page.attachments) {
                            XMutableTreeNode attTreeNode = mxTreeDataModel.createNode(att.getFileName(), false);
                            pageTreeNode.appendChild(attTreeNode);
                        }
                        spaceTreeNode.appendChild(pageTreeNode);
                    }
                    root.appendChild(spaceTreeNode);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Updates the global XWikiStructure object.
     */
    public void refreshXWikiStructure()
    {
        XWikiXmlRpcClient client = xWikiExtension.getClient();
        if (client != null) {
            XWikiStructure xWikiStructure = XWikiStructureActions.getXWikiStructure(client);
            xWikiExtension.setXWikiStructure(xWikiStructure);
        }
    }

    /**
     * Updates or adds a space and its children in the XWikiStructure.
     * 
     * @param spaceId the space name
     */
    public void refreshSpaceInXwikiStructure(String spaceId)
    {
        XWikiXmlRpcClient client = xWikiExtension.getClient();
        if (client != null) {
            XWikiStructure xWikiStructure = xWikiExtension.getXWikiStructure();
            XWikiStructureActions.refreshSpaceInXWikiStructure(client, xWikiStructure, spaceId);
            xWikiExtension.setXWikiStructure(xWikiStructure);
        }
    }

    /**
     * Updates or adds a space and its children in the tree control.
     * 
     * @param spaceId
     */
    public void refreshSpace(String spaceId)
    {
        refreshSpaceInXwikiStructure(spaceId);
        updateTreeControl();

    }

    public void refreshPageAttInXwikiStructure(PageSummary page)
    {

        XWikiXmlRpcClient client = xWikiExtension.getClient();
        if (client != null) {
            XWikiStructure xWikiStructure = xWikiExtension.getXWikiStructure();
            XWikiStructureActions.refreshPageAttInXWikiStructure(client, xWikiStructure, page);
            xWikiExtension.setXWikiStructure(xWikiStructure);
        }
    }

    public void refreshPageAtt(PageSummary page)
    {
        refreshPageAttInXwikiStructure(page);
        updateTreeControl();
    }

    /**
     * Gets the level of a node.
     * 
     * @param node the input tree node
     * @return SPACE_LEVEL or PAGE_LEVEL or ATT_LEVEL
     */
    private short getLevel(XTreeNode node)
    {
        short level = 0;
        while (node.getParent() != null) {
            ++level;
            node = node.getParent();
        }
        return level;
    }

    /**
     * Gets the spaceNode from the XWiki structure object which corresponds with a tree node from the SPACE_LEVEL.
     * 
     * @param node the input tree node
     * @return the corresponding spaceNode
     */
    private SpaceNode getSpaceNode(XTreeNode node)
    {
        XTreeNode root = node.getParent();
        int spaceindex = root.getIndex(node);

        XWikiStructure xwikiStructure = this.xWikiExtension.getXWikiStructure();
        return xwikiStructure.spaces.get(spaceindex);
    }

    /**
     * Gets the pageNode from the XWiki structure object which corresponds with a tree node from the PAGE_LEVEL.
     * 
     * @param node the input tree node
     * @return the corresponding pageNode
     */
    private PageNode getPageNode(XTreeNode node)
    {
        XTreeNode space = node.getParent();
        int pageindex = space.getIndex(node);

        XTreeNode root = space.getParent();
        int spaceindex = root.getIndex(space);

        XWikiStructure xwikiStructure = this.xWikiExtension.getXWikiStructure();
        return xwikiStructure.spaces.get(spaceindex).pages.get(pageindex);
    }

    /**
     * Gets the attachment from the XWiki structure object which corresponds with a tree node from the ATT_LEVEL.
     * 
     * @param node the input tree node
     * @return the corresponding attachment
     */
    private Attachment getAttNode(XTreeNode node)
    {
        XTreeNode page = node.getParent();
        int attindex = page.getIndex(node);

        XTreeNode space = page.getParent();
        int pageindex = space.getIndex(page);

        XTreeNode root = space.getParent();
        int spaceindex = root.getIndex(space);

        XWikiStructure xwikiStructure = this.xWikiExtension.getXWikiStructure();
        return xwikiStructure.spaces.get(spaceindex).pages.get(pageindex).attachments.get(attindex);
    }

    /**
     * @return the spaceNode from the XWiki structure object which corresponds with the selected tree node
     */
    public SpaceNode getSelectedSpaceNode()
    {

        Object oObject = xTree.getSelection();

        XMutableTreeNode node = (XMutableTreeNode) UnoRuntime.queryInterface(XMutableTreeNode.class, oObject);

        if (node != null && getLevel(node) == SPACE_LEVEL)
            return getSpaceNode(node);

        return null;
    }

    /**
     * @return the pageNode from the XWiki structure object which corresponds with the selected tree node
     */
    public PageNode getSelectedPageNode()
    {

        Object oObject = xTree.getSelection();

        XMutableTreeNode node = (XMutableTreeNode) UnoRuntime.queryInterface(XMutableTreeNode.class, oObject);

        if (node != null && getLevel(node) == PAGE_LEVEL)
            return getPageNode(node);

        return null;
    }

    /**
     * @return the attachment from the XWiki structure object which corresponds with the selected tree node
     */
    public Attachment getSelectedAttNode()
    {

        Object oObject = xTree.getSelection();

        XMutableTreeNode node = (XMutableTreeNode) UnoRuntime.queryInterface(XMutableTreeNode.class, oObject);

        if (node != null && getLevel(node) == ATT_LEVEL)
            return getAttNode(node);

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void mousePressed(MouseEvent ev)
    {
        /*
         * if (MouseButton.RIGHT == ev.Buttons) { XTreeNode node = xTree.getNodeForLocation(ev.X, ev.Y); if (node !=
         * null && getLevel(node) == PAGE_LEVEL) { PageNode pageNode = getPageNode(node);
         * xWikiActions.editPage(pageNode.pageSummary); } }
         */
    }

    /**
     * {@inheritDoc}
     */
    public void mouseReleased(MouseEvent arg0)
    {
    }

    /**
     * {@inheritDoc}
     */
    public void mouseEntered(MouseEvent arg0)
    {
    }

    /**
     * {@inheritDoc}
     */
    public void mouseExited(MouseEvent arg0)
    {
    }

    public void disposing(EventObject arg0)
    {
        _instance = null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean callHandlerMethod(XDialog xDialog, Object EventObject, String MethodName)
    {

        if (MethodName.equals(sRefreshMethod)) {
            btnRefresh_Clicked();
        } else if (MethodName.equals(sAddSpaceMethod)) {
            btnAddSpace_Clicked();
        }

        return true;
    }

    /**
     * The handler for the clicked event of the Refresh button.
     */

    private void btnRefresh_Clicked()
    {
        refreshXWikiStructure();
        updateTreeControl();
    }

    /**
     * The handler for the clicked event of the Add Space button.
     */
    private void btnAddSpace_Clicked()
    {
        XControl dialogControl = (XControl) UnoRuntime.queryInterface(XControl.class, m_xDialog);
        XWindow xWindow = (XWindow) UnoRuntime.queryInterface(XWindow.class, dialogControl);

        XWikiExtensionActions xWikiExtensionActions = new XWikiExtensionActions(xWikiExtension);
        xWikiExtensionActions.cmdAddSpace(xWindow.getPosSize().X);
    }
}
