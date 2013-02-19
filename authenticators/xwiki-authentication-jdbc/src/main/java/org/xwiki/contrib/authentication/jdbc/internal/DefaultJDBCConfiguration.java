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
package org.xwiki.contrib.authentication.jdbc.internal;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.configuration.ConfigurationSource;

@Component
@Singleton
public class DefaultJDBCConfiguration implements JDBCConfiguration, Initializable
{
    private static final String CONFIGURATION_PREFIX = "authentication.jdbc.";

    private static final String CONNECTIONCONFIGURATION_PREFIX = CONFIGURATION_PREFIX + "connection.";

    @Inject
    @Named("xwikiproperties")
    private ConfigurationSource configurationSource;

    private boolean driverInitialized = false;

    @Override
    public void initialize() throws InitializationException
    {
    }

    @Override
    public Properties getConnectionProperties()
    {
        return this.configurationSource.getProperty(CONNECTIONCONFIGURATION_PREFIX + "properties", Properties.class);
    }

    @Override
    public String getConnectionURL()
    {
        return this.configurationSource.getProperty(CONNECTIONCONFIGURATION_PREFIX + "url", String.class);
    }

    @Override
    public synchronized void checkDriver() throws InstantiationException, IllegalAccessException,
        ClassNotFoundException
    {
        if (!this.driverInitialized) {
            String driverClassName =
                this.configurationSource.getProperty(CONNECTIONCONFIGURATION_PREFIX + "driver_class", String.class);

            Class.forName(driverClassName).newInstance();

            this.driverInitialized = true;
        }
    }

    @Override
    public String getPasswordColumn()
    {
        return this.configurationSource.getProperty(CONFIGURATION_PREFIX + "select.password_column", String.class);
    }

    @Override
    public String getQuery(String type)
    {
        return this.configurationSource.getProperty(CONFIGURATION_PREFIX + type + ".query", String.class);
    }

    @Override
    public String[] getQueryParameters(String type)
    {
        return this.configurationSource.getProperty(CONFIGURATION_PREFIX + type + ".parameters", String[].class);
    }

    // SELECT

    @Override
    public String getSelectQuery()
    {
        return getQuery("select");
    }

    @Override
    public String[] getSelectParameters()
    {
        return getQueryParameters("select");
    }

    @Override
    public String[] getSelectMapping()
    {
        return this.configurationSource.getProperty(CONFIGURATION_PREFIX + "select.mapping", String[].class);
    }

    // UPDATE

    @Override
    public String getUpdateQuery()
    {
        return getQuery("update");
    }

    @Override
    public String[] getUpdateParameters()
    {
        return getQueryParameters("update");
    }

    // INSERT

    @Override
    public String getInsertQuery()
    {
        return getQuery("insert");
    }

    @Override
    public String[] getInsertParameters()
    {
        return getQueryParameters("insert");
    }

    // DELETE

    @Override
    public String getDeleteQuery()
    {
        return getQuery("delete");
    }

    @Override
    public String[] getDeleteParameters()
    {
        return getQueryParameters("delete");
    }

    public String getPasswordHasher()
    {
        return configurationSource.getProperty(CONFIGURATION_PREFIX + "password_hasher", String.class);
    }
}
