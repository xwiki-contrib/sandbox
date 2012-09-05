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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.securityfilter.realm.SimplePrincipal;
import org.xwiki.contrib.authentication.jdbc.internal.JDBCConfiguration;
import org.xwiki.contrib.authentication.jdbc.internal.UserSynchronizer;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.WikiReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.user.impl.xwiki.XWikiAuthServiceImpl;
import com.xpn.xwiki.web.Utils;

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

    private JDBCConfiguration configuration = Utils.getComponent(JDBCConfiguration.class);

    private UserSynchronizer userSynchronizer = Utils.getComponent(UserSynchronizer.class);

    private EntityReferenceSerializer<String> compactWikiSerializer = Utils.getComponent(
        EntityReferenceSerializer.TYPE_STRING, "compactwiki");

    @Override
    public Principal authenticate(String username, String password, XWikiContext context) throws XWikiException
    {
        if (username != null) {
            // Check auth
            try {
                Map<String, Object> properties = checkJDBCAuth(username, password, context);
                if (properties != null) {
                    DocumentReference userReference = this.userSynchronizer.updateXWikiUser(username, properties);

                    return new SimplePrincipal(compactWikiSerializer.serialize(userReference,
                        new WikiReference(context.getDatabase())));
                }
            } catch (Exception e) {
                LOG.error("Failed to authenticate on JDBC", e);
            }
        }

        // Fallback on standard XWiki authentication
        return super.authenticate(username, password, context);
    }

    // Auth

    private Map<String, Object> checkJDBCAuth(String login, String password, XWikiContext context) throws Exception
    {
        this.configuration.checkDriver();

        String selectQuery = this.configuration.getSelectQuery();

        if (selectQuery == null) {
            throw new Exception("Select query should be provided in xwiki.authentication.jdbc.mapping.select.query");
        }

        String[] selectQueryParameters = this.configuration.getSelectParameters();

        Connection connection = this.userSynchronizer.getConnection();
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(selectQuery);

            Map<String, String> fields = new HashMap<String, String>();
            fields.put("login", login);
            fields.put("password", password);
            fields.put("passwordsha1base64", Base64.encodeBase64String(DigestUtils.sha(password)));

            int index = 1;
            for (String field : selectQueryParameters) {
                statement.setString(index++, fields.get(field));
            }

            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                return null;
            }

            // Sync properties
            Map<String, Object> userProperties = new HashMap<String, Object>();

            String[] selectQueryMapping = this.configuration.getSelectMapping();

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
}
