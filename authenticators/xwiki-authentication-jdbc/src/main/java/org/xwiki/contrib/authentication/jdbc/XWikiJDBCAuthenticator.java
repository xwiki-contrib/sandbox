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
package org.xwiki.contrib.authentication.jdbc;

import java.security.Principal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.securityfilter.realm.SimplePrincipal;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.user.impl.xwiki.XWikiAuthServiceImpl;

/**
 * Authentication based on HTTP auth on a configured URL.
 * 
 * @version $Id$
 */
public class XWikiJDBCAuthenticator extends XWikiAuthServiceImpl
{
    /**
     * Logging tool.
     */
    private static final Log LOG = LogFactory.getLog(XWikiJDBCAuthenticator.class);

    @Override
    public Principal authenticate(String username, String password, XWikiContext context) throws XWikiException
    {
        if (username != null) {
            // Check auth
            try {
                Map<String, Object> properties = checkJDBCAuth(username, password, context);
                if (properties != null) {
                    return syncUser(username, properties, context);
                }
            } catch (Exception e) {
                LOG.error("Failed to authenticate on JDBC", e);
            }
        }

        // Fallback on standard XWiki authentication
        return super.authenticate(username, password, context);
    }

    // Auth

    private Properties getJDBCConnectionProperties(XWikiContext context)
    {
        Properties properties = new Properties();

        for (Map.Entry<Object, Object> entry : context.getWiki().getConfig().entrySet()) {
            String key = (String) entry.getKey();

            if (key.startsWith("xwiki.authentication.jdbc.connection.")) {
                String value = (String) entry.getValue();

                properties.setProperty(key.substring("xwiki.authentication.jdbc.connection.".length()), value);
            }
        }

        return properties;
    }

    private Map<String, Object> checkJDBCAuth(String login, String password, XWikiContext context) throws Exception
    {
        Properties connectionProperties = getJDBCConnectionProperties(context);

        String driverClassName = (String) connectionProperties.remove("driver_class");
        String url = (String) connectionProperties.remove("url");

        String selectQuery = context.getWiki().Param("xwiki.authentication.jdbc.mapping.select.query");

        if (selectQuery == null) {
            throw new Exception("Select query should be provided in xwiki.authentication.jdbc.mapping.select.query");
        }

        String[] insertQueryFields =
            StringUtils.split(context.getWiki().Param("xwiki.authentication.jdbc.mapping.select.fields"), ',');

        Class.forName(driverClassName).newInstance();

        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DriverManager.getConnection(url, connectionProperties);
            statement = connection.prepareStatement(selectQuery);

            Map<String, String> fields = new HashMap<String, String>();
            fields.put("login", login);
            fields.put("password", password);
            fields.put("passwordsha1base64", Base64.encodeBase64String(DigestUtils.sha(password)));

            int index = 1;
            for (String field : insertQueryFields) {
                if (!fields.containsKey(field)) {
                    throw new Exception("Field [" + fields + "] does not exist in avaibale properties");
                }

                statement.setString(index++, fields.get(field));
            }

            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                return null;
            }

            // Sync properties
            Map<String, Object> userProperties = new HashMap<String, Object>();

            String[] selectQueryMapping =
                StringUtils.split(context.getWiki().Param("xwiki.authentication.jdbc.mapping.select.mapping"), ',');

            int columnCount = resultSet.getMetaData().getColumnCount();
            for (int i = 0; i < columnCount && i < selectQueryMapping.length; ++i) {
                String mappingField = selectQueryMapping[i];
                if (StringUtils.isNotEmpty(mappingField)) {
                    String value = resultSet.getString(i + 1);

                    userProperties.put(mappingField, value);
                }
            }

            return userProperties;
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }

    // Sync

    public String getValidUserName(String userName)
    {
        return userName.replace('.', '_').replace('@', '_').replace(' ', '_');
    }

    private Principal syncUser(String username, Map<String, Object> properties, XWikiContext context)
        throws XWikiException
    {
        // Synch user
        String validUserName = getValidUserName(username);
        String validUserFullName = "XWiki." + validUserName;

        String database = context.getDatabase();
        try {
            // Switch to main wiki to force users to be global users
            context.setDatabase(context.getMainXWiki());

            // test if user already exists
            if (!context.getWiki().exists(validUserFullName, context)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Need to create user " + validUserName);
                }

                // create user
                if (properties.containsKey("active")) {
                    properties.put("active", "1");
                }

                // Convert some properties
                Object nameProperty = properties.get("name");
                if (nameProperty != null && nameProperty instanceof String) {
                    String name = (String) nameProperty;

                    name = name.trim();
                    int index = name.indexOf(' ');
                    if (index != -1) {
                        properties.put("first_name", name.substring(0, index));
                        properties.put("last_name", name.substring(index + 1));
                    } else {
                        properties.put("first_name", name);
                    }
                }

                context.getWiki().createUser(validUserName, properties, context);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("User " + validUserName + " has been successfully created");
                }
            }
        } finally {
            context.setDatabase(database);
        }

        if (context.isMainWiki()) {
            return new SimplePrincipal(validUserFullName);
        } else {
            return new SimplePrincipal(context.getMainXWiki() + ":" + validUserFullName);
        }
    }
}
