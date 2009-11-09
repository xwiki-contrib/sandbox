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
package org.xwoot.contentprovider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @version $Id$
 */
public class XWootContentProviderConfiguration
{
    final Log logger = LogFactory.getLog(XWootContentProviderConfiguration.class);

    private static final String CONFIGURATION_FILE = "/xwoot-content-provider.properties";

    private static final String IGNORE_PROPERTY = "ignore";

    private static final String ACCEPT_PROPERTY = "accept";

    private static final String CUMULATIVE_CLASSES_PROPERTY = "cumulative_classes";

    private static final String WOOTABLE_PROPERTIES_SUFFIX = ".wootable_properties";

    private URL configurationFileUrl;

    private Set<String> cumulativeClasses;

    private Map<String, Set<String>> wootablePropertiesMap;

    private ArrayList<Pattern> ignorePatterns;

    private ArrayList<Pattern> acceptPatterns;

    public XWootContentProviderConfiguration()
    {
        this(null);
    }

    public XWootContentProviderConfiguration(Properties properties)
    {
        if (properties == null) {
            properties = new Properties();
            configurationFileUrl = XWootContentProviderConfiguration.class.getResource(CONFIGURATION_FILE);
            InputStream is = XWootContentProviderConfiguration.class.getResourceAsStream(CONFIGURATION_FILE);

            if (is != null) {
                try {
                    properties.load(is);
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /* Read ignore list */
        ignorePatterns = new ArrayList<Pattern>();
        String ignoreListValue = properties.getProperty(IGNORE_PROPERTY);
        if (ignoreListValue != null) {
            String[] values = ignoreListValue.split(",");
            for (String value : values) {
                ignorePatterns.add(Pattern.compile(value));
            }
        }

        /* Read accept list */
        acceptPatterns = new ArrayList<Pattern>();
        String acceptListValue = properties.getProperty(ACCEPT_PROPERTY);
        if (acceptListValue != null) {
            String[] values = acceptListValue.split(",");
            for (String value : values) {
                acceptPatterns.add(Pattern.compile(value));
            }
        }

        /* Read cumulative classes */
        cumulativeClasses = new HashSet<String>();

        String cumulativeClassesValue = properties.getProperty(CUMULATIVE_CLASSES_PROPERTY);
        if (cumulativeClassesValue != null) {
            String[] values = cumulativeClassesValue.split(",");
            for (String value : values) {
                cumulativeClasses.add(value.trim());
            }
        }

        /* Read wootable fields */
        wootablePropertiesMap = new HashMap<String, Set<String>>();

        for (Object object : properties.keySet()) {
            String key = (String) object;
            if (key.endsWith(WOOTABLE_PROPERTIES_SUFFIX)) {
                String className = key.substring(0, key.indexOf(WOOTABLE_PROPERTIES_SUFFIX));

                Set<String> wootableProperties = wootablePropertiesMap.get(className);
                if (wootableProperties == null) {
                    wootableProperties = new HashSet<String>();
                    wootablePropertiesMap.put(className, wootableProperties);
                }

                String[] values = properties.getProperty(key).split(",");
                for (String value : values) {
                    wootableProperties.add(value.trim());
                }
            }
        }
    }

    public boolean isCumulative(String className)
    {
        return cumulativeClasses.contains(className);
    }

    public boolean isWootable(String className, String property)
    {
        Set<String> properties = wootablePropertiesMap.get(className);
        if (properties != null) {
            return properties.contains(property);
        }

        return false;
    }

    public boolean isIgnored(String pageName)
    {
        boolean isIgnored = false;

        for (Pattern pattern : ignorePatterns) {
            Matcher matcher = pattern.matcher(pageName);
            if (matcher.matches()) {
                isIgnored = true;
                break;
            }
        }

        for (Pattern pattern : acceptPatterns) {
            Matcher matcher = pattern.matcher(pageName);
            if (matcher.matches()) {
                isIgnored = false;
                break;
            }
        }

        return isIgnored;
    }

    public Set<String> getCumulativeClasses()
    {
        return cumulativeClasses;
    }

    public Map<String, Set<String>> getWootablePropertiesMap()
    {
        return wootablePropertiesMap;
    }

    public URL getConfigurationFileUrl()
    {
        return configurationFileUrl;
    }

    public ArrayList<Pattern> getAcceptPatterns()
    {
        return acceptPatterns;
    }

    public ArrayList<Pattern> getIgnorePatterns()
    {
        return ignorePatterns;
    }

    public void addIgnorePattern(String pattern)
    {
        ignorePatterns.add(Pattern.compile(pattern));
    }

    public void addAcceptPattern(String pattern)
    {
        acceptPatterns.add(Pattern.compile(pattern));
    }

    public void removeIgnorePattern(String pattern)
    {
        for (Pattern p : ignorePatterns) {
            if (p.toString().equals(pattern)) {
                ignorePatterns.remove(p);
                break;
            }
        }
    }

    public void removeAcceptPattern(String pattern)
    {
        for (Pattern p : acceptPatterns) {
            if (p.toString().equals(pattern)) {
                acceptPatterns.remove(p);
                break;
            }
        }
    }
}
