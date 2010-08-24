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

/**
 * The constants used in the whole project
 * 
 * @version $Id$
 * @since 1.0 M
 */

public interface Constants
{

    final static String PROTOCOL_XOOMENU = "org.xwiki.xoo.xoo:";

    final static String PROTOCOL_XOOTOOLBAR = "org.xwiki.xoo.xootoolbar:";

    final static String CMD_LOGIN = "cmdLogin";

    final static String CMD_TREE = "cmdTree";

    final static String CMD_ADDPAGE = "cmdAddPage";

    final static String CMD_EDITPAGE = "cmdEditPage";

    final static String CMD_PUBLISHPAGE = "cmdPublishPage";

    final static String CMD_VIEWINBROWSER = "cmdBrowser";

    final static String CMD_DOWNATT = "cmdDownloadAtt";

    final static String CMD_UPATT = "cmdUploadAtt";

    final static String SETTINGS_DIALOG = "vnd.sun.star.script:dialogLibrary.Login?location=application";

    final static String TREE_DIALOG = "vnd.sun.star.script:dialogLibrary.XWikiTree?location=application";

    final static String CUSTOM_SETTINGS_DIALOG =
        "vnd.sun.star.script:dialogLibrary.CustomSettings?location=application";

    final static String ADD_PAGE_DIALOG = "vnd.sun.star.script:dialogLibrary.AddPage?location=application";

    final static String DOWLOAD_ATT_DIALOG = "vnd.sun.star.script:dialogLibrary.SaveFileDialog?location=application";

    final static String ADD_SPACE_DIALOG = "vnd.sun.star.script:dialogLibrary.AddSpaceDialog?location=application";

    static int TYPE_INFO = 0;

    final static int TYPE_ERROR = 1;

    final static String TITLE_ERROR = "XWiki Error";

    final static String TITLE_XWIKI = "XWiki";

    final static int ERROR_LOGINFAILED = 0;

    final static int ERROR_LOGINREQUESTED = 1;

    final static int ERROR_PAGESELECT = 2;

    final static int ERROR_PAGEPUBLISH = 3;

    final static int ERROR_FOMATNOTSUPPORTED = 4;

    final static int ERROR_ALREADYEDIT = 5;

    final static int ERROR_SPACESELECT = 6;

    final static int ERROR_SPACENAMENULL = 7;

    final static int ERROR_PAGENAMENULL = 8;

    final static int ERROR_PAGETITLENULL = 9;

    final static int ERROR_ATTSELECT = 10;

    final static int ERROR_DOWNLOAD_FAILED = 11;

    final static int ERROR_FILE_EXISTS = 12;

    final static int ERROR_UPP_FILE_NOT_EXISTS = 13;

    final static int ERROR_UPP_FILE_TOO_BIG = 14;

    final static int ERROR_UPPLOAD_FAILED = 15;

    final static int ERROR_ADDSPACE_FAILED = 16;

    final static int ERROR_PAGEEMPTY = 17;

    final static String[] errorMessages =
        {"Login failed! Please check the server URL, the username and password ",
        "You should be logged in to XWiki Server to perform this action",
        "You should select a page in the XWiki navigation panel  in order to be able to perform this action",
        "This page could not be published", "This file format is not supported (temporal aproach)",
        "You are already editing this page",
        "You should select a space in the XWiki navigation panel  in order to be able to perform this action",
        "The space name cannot be empty!", "The page name cannot be empty", "The page title cannot be empty",
        "You should select a page in the XWiki navigation panel  in order to be able to perform this action",
        "Download failed!", "File already exists. Do you want to replace it?",
        "Upload failed! Please save your document and try again.", "Upload failed! File too big!", "Upload failed!",
        "Add space failed!", "Page empty! Please save the page and try again!"};

    final static int MESS_LOGINSUCC = 0;

    final static int MESS_PUBLISHSUCC = 1;

    final static int MESS_DOWNLOADSUCC = 2;

    final static int MESS_UPLOADSUCC = 3;

    final static int MESS_ADDSPACESUCC = 4;

    final static String[] Messages =
        {"Login successfully done!", "Page publish successfully done!", "Download successfully done!",
        "Upload successfully done!", "Add space successfully done!"};

    final static String IMG_OK_BUTTON = "check-ok_26.png";
    
    final static String IMG_CANCEL_BUTTON = "button_cancel.png";
    
    final static String IMG_CONN_SETTINGS = "connection_settings_16.png";
    
    final static String IMG_ADD_SPACE = "add_21.png";
    
    final static String IMG_RELOAD = "reload_16.png";
    
    final static String IMG_FOLDER = "folder-16.png";
}
