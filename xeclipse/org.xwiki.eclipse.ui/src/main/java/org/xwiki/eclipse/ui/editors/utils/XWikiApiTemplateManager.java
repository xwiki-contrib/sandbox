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
package org.xwiki.eclipse.ui.editors.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.templates.Template;
import org.xwiki.eclipse.ui.UIPlugin;
import org.xwiki.eclipse.ui.editors.Constants;
import org.xwiki.eclipse.ui.editors.XWikiApiType;

public class XWikiApiTemplateManager
{
    private static XWikiApiTemplateManager sharedInstance;

    private Map<XWikiApiType, Template[]> apiTypeToTemplatesMapping;

    /**
     * Regex for splitting a line containing a method declaration into its components. Basically there are 4 regex
     * groups:
     * <ul>
     * <li>The first ([^\\s]+) captures the return type</li>
     * <li>The second ([^\\s\\(]+) captures the method name</li>
     * <li>The third ([\\(]*) captures the parameters list (everything between '(' and ')')</li>
     * <li>The fourth (.*) captures everything else at the end, i.e., exception declarations</li>
     * </ul>
     */
    private static Pattern methodPattern = Pattern.compile("([^\\s]+) +([^\\s\\(]+) *\\(([^\\)]*)\\) *(.*)");

    private XWikiApiTemplateManager()
    {
        apiTypeToTemplatesMapping = new HashMap<XWikiApiType, Template[]>();
    }

    public static XWikiApiTemplateManager getDefault()
    {
        if (sharedInstance == null) {
            sharedInstance = new XWikiApiTemplateManager();
        }

        return sharedInstance;
    }

    /**
     * creates the template list for the given xwiki variable content type
     * 
     * @param apiType
     * @return
     */
    public Template[] getXWikiCompletionTemplates(XWikiApiType apiType)
    {
        Template[] result = apiTypeToTemplatesMapping.get(apiType);
        if (result == null) {
            String entryPath = Constants.API_DATA_DIRECTORY + "/" + apiType.toString().toLowerCase() + ".api";
            result = readTemplatesFromBundleEntry(entryPath);
            apiTypeToTemplatesMapping.put(apiType, result);
        }

        return result;
    }

    /**
     * Read the resource entry containing all the method declarations and returns the corresponding templates.
     * 
     * @param entryPath The resource entry containing all the method declarations.
     * @return A template array with the templates corresponding to method declarations contained in it.
     */
    private Template[] readTemplatesFromBundleEntry(String entryPath)
    {
        List<Template> apiList = new ArrayList<Template>();
        try {
            BufferedReader reader =
                new BufferedReader(new InputStreamReader(UIPlugin.getDefault().getBundle().getEntry(entryPath)
                    .openStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                Template template = getCompletionTemplateForMethodDeclaration(line);

                /* Add only the templates that are well formed. */
                if (template != null) {
                    apiList.add(template);
                }
            }

            reader.close();
        } catch (FileNotFoundException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }

        Template[] result = apiList.toArray(new Template[apiList.size()]);
        Arrays.sort(result, new Comparator<Template>()
        {
            public int compare(Template o1, Template o2)
            {
                return o1.getName().compareTo(o2.getName());
            }
        });

        return result;
    }

    /**
     * Parse a line containing a method declaration and create the corresponding completion template
     * 
     * @param methodDeclaration The string where the template should be built from.
     * @return The corresponding template.
     */
    private Template getCompletionTemplateForMethodDeclaration(String methodDeclaration)
    {
        Template template = null;
        String contextTypeId = "org.xwiki.eclipse.ui.editors.velocity.xwikiapi";

        Matcher matcher = methodPattern.matcher(methodDeclaration);
        if (matcher.matches()) {
            /* Get the matched regex components */
            String returnType = matcher.group(1);
            String methodName = matcher.group(2);
            String parameters = matcher.group(3).trim();
            String[] parametersArray = parameters.split(",");
            String exception = matcher.group(4);

            /* Build the string that will be displayed in the completion box */
            StringBuffer templateNameString = new StringBuffer();
            templateNameString.append(methodName);
            templateNameString.append("(");
            templateNameString.append(parameters);
            templateNameString.append(") : ");
            templateNameString.append(returnType);

            /* Build the template pattern that will be inserted in the text in the form methodName(${paramName}...) */
            StringBuffer templatePatternString = new StringBuffer();
            templatePatternString.append(methodName);
            templatePatternString.append("(");
            for (int i = 0; i < parametersArray.length; i++) {

                String currentParameter = parametersArray[i].trim();

                if (!currentParameter.equals("")) {
                    /* Split the parameter that is in the form of "Type name" into its components */
                    String[] components = currentParameter.split(" ");

                    /* If parameter type is String then put "" around the placeholder */
                    if (components[0].equals("String")) {
                        templatePatternString.append("\"");
                    }

                    templatePatternString.append("${");
                    templatePatternString.append(components[1]);
                    templatePatternString.append("}");

                    /* If parameter type is String then put "" around the placeholder */
                    if (components[0].equals("String")) {
                        templatePatternString.append("\"");
                    }

                    /* If there are more parameters, put a , */
                    if (i != (parametersArray.length - 1)) {
                        templatePatternString.append(", ");
                    }
                }
            }
            templatePatternString.append(")");

            /* Use the exception string in the proposal box to give more information to the user */
            if (exception != null && !exception.equals("")) {
                template =
                    new Template(templateNameString.toString(), exception, contextTypeId, templatePatternString
                        .toString(), false);
            } else {
                template =
                    new Template(templateNameString.toString(), "", contextTypeId, templatePatternString.toString(),
                        false);
            }
        }

        return template;
    }
}
