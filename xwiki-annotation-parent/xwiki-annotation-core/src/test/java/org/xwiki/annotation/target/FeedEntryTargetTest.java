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

package org.xwiki.annotation.target;

import java.util.Collections;

import junit.framework.Assert;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;
import org.xwiki.annotation.AnnotationTarget;
import org.xwiki.annotation.IOService;
import org.xwiki.annotation.IOTargetService;
import org.xwiki.annotation.internal.annotation.Annotation;
import org.xwiki.annotation.internal.context.Source;
import org.xwiki.annotation.internal.exception.AnnotationServiceException;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.context.ExecutionContext;
import org.xwiki.context.ExecutionContextManager;
import org.xwiki.test.AbstractComponentTestCase;

import com.xpn.xwiki.XWikiContext;

/**
 * @version $Id$
 */
public class FeedEntryTargetTest extends AbstractComponentTestCase
{
    private final Mockery mockery = new Mockery();

    private static IOTargetService ioTargetService;

    private static IOService ioService;

    private static ExecutionContextManager executionContextManager;

    private static AnnotationTarget feedEntryAnnotationTarget;

    private static final CharSequence emptyDocument = "Empty.Document";

    private static final Source source = new Source()
    {
        public CharSequence getSource()
        {
            return "<item>"
                + "<title>Onlinepetition zum Erhalt von Astrastube, Fundbureau und Waagenbau</title>"
                + "<link>http://www.byte.fm/blog/2009/09/04/onlinepetition-zum-erhalt-von-astrastube-fundbureau-und-"
                + "waagenbau/</link>"
                + "<comments>http://www.byte.fm/blog/2009/09/04/onlinepetition-zum-erhalt-von-astrastube-fundbureau-und"
                + "-waagenbau/#comments</comments>"
                + "<pubDate>Fri, 04 Sep 2009 09:14:38 +0000</pubDate>"
                + "<dc:creator>ByteFM Redaktion</dc:creator>"
                + "<category><![CDATA[Blog]]></category>"
                + "<guid isPermaLink='false'>http://www.byte.fm/blog/?p=3912</guid>"
                + "<description>"
                + "   <![CDATA[Als Jan Delay das Cover zu „Wir Kinder vom Bahnhof Soul“ aussuchte, dachte er bestimmt "
                + "nicht daran, dass die abgebildete   Sternbrücke in dieser Form bald Geschichte sein könnte. Zum "
                + "31.12.09 hat die Bahn eine Kündigung für einen wichtigen subkulturellen   Standort Hamburgs "
                + "eingereicht. Aststrastube, Fundbureau und Waagenbau sollen geschlossen werden, damit die Sternbrücke"
                + " umfangreich   saniert werden kann. Um [...]]]>"
                + "</description>"
                + "<content:encoded>"
                + "   <![CDATA[<p><img style='margin:' title='BRD' "
                + "src='http://byte.fm/live/Blogbilder/sternbruecke.jpg' alt='CIAlex Martyn' hspace='20'   vspace='20' "
                + "width='200' align='left' />Als Jan Delay das Cover zu „Wir Kinder vom Bahnhof Soul“ aussuchte, "
                + "dachte er bestimmt nicht   daran, dass die abgebildete Sternbrücke in dieser Form bald Geschichte "
                + "sein könnte.</p><p>Zum 31.12.09 hat die Bahn eine Kündigung   für einen wichtigen subkulturellen "
                + "Standort Hamburgs eingereicht. Aststrastube, Fundbureau und Waagenbau sollen geschlossen werden,   "
                + "damit die Sternbrücke umfangreich saniert werden kann.</p><p>Um die drei Klubs am Leben zu halten, "
                + "sei es unter der Sternbrücke oder   anderswo, suchen die Besitzer das Gespräch mit Politik, Medien "
                + "und der Bahn.<br />In diesem Rahmen habt ihr die Möglichkeit über   <strong><a "
                + "href='http://www.petitiononline.com/31122009/petition.html' target='blank'>eine Onlinepetition </a>"
                + "</strong>die Klubs,   und damit ein kleines Stück Hamburger Musik- und Partykultur, zu unterstützen."
                + "</p>]]>" + "</content:encoded>"
                + "<wfw:commentRss>http://www.byte.fm/blog/2009/09/04/onlinepetition-zum-erhalt-von-astrastube-"
                + "fundbureau-und-waagenbau/feed/</wfw:commentRss></item>";
        }
    };

    private static final XWikiContext deprecatedContext = null;

    private static final String user = "XWiki.Scribo";

    private static final int offset = 0;

    private static final CharSequence metadata = "Byte FM is a great web radio.";

    private static final CharSequence selection =
        "dass die abgebildete Sternbrücke in dieser Form bald Geschichte sein "
            + "könnte.Zum 31.12.09 hat die Bahn eine Kündigung für einen wichtigen";

    private static final CharSequence context = selection;

    {
        // IOTargetService mockup
        ioTargetService = mockery.mock(IOTargetService.class);

        ioService = mockery.mock(IOService.class);

        // ExecutionContextManager mockup
        executionContextManager = mockery.mock(ExecutionContextManager.class);
    }

    @Override
    public void setUp() throws Exception
    {
        // Setting up IOTargetService
        DefaultComponentDescriptor<IOTargetService> iotsDesc = new DefaultComponentDescriptor<IOTargetService>();
        iotsDesc.setRole(IOTargetService.class);
        iotsDesc.setRoleHint("FEEDENTRY");
        getComponentManager().registerComponent(iotsDesc, ioTargetService);

        // Setting up io service
        DefaultComponentDescriptor<IOService> ioDesc = new DefaultComponentDescriptor<IOService>();
        ioDesc.setRole(IOService.class);
        getComponentManager().registerComponent(ioDesc, ioService);

        // Setting up ExecutionContextManager
        DefaultComponentDescriptor<ExecutionContextManager> ecmDesc =
            new DefaultComponentDescriptor<ExecutionContextManager>();
        ecmDesc.setRole(ExecutionContextManager.class);
        getComponentManager().registerComponent(ecmDesc, executionContextManager);

        mockery.checking(new Expectations()
        {
            {
                // IOTargetService configuration
                oneOf(ioTargetService).getRenderedContent(with(emptyDocument), with(source), with(deprecatedContext));
                will(returnValue(source.getSource()));
                oneOf(ioTargetService).getSource(with(emptyDocument), with(deprecatedContext));
                will(returnValue(source));

                // IOservice configuration
                oneOf(ioService).addAnnotation(with(emptyDocument), with(any(Annotation.class)),
                    with(deprecatedContext));
                oneOf(ioService).getSafeAnnotations(with(emptyDocument), with(deprecatedContext));
                will(returnValue(Collections.<Annotation> emptySet()));

                // ExecutionContextManager configuration
                oneOf(executionContextManager).initialize(with(any(ExecutionContext.class)));
            }
        });
        feedEntryAnnotationTarget = getComponentManager().lookup(AnnotationTarget.class, "feedEntry");
        super.setUp();
    }

    @Test
    public void getAnnotatedHTML()
    {
        try {
            CharSequence html = feedEntryAnnotationTarget.getAnnotatedHTML(emptyDocument, null);
            Assert.assertEquals(source.getSource(), html);
        } catch (AnnotationServiceException e) {
            Assert.fail(getExceptionFailureMessage(e));
        }
    }

    @Test
    public void addAnnotation()
    {
        try {
            feedEntryAnnotationTarget.addAnnotation(metadata, selection, context, offset, emptyDocument, user,
                deprecatedContext);
        } catch (AnnotationServiceException e) {
            Assert.fail(getExceptionFailureMessage(e));
        }
    }

    /**
     * @param e the exception to provide failure message for
     * @return a failure message computed for the passed exception
     */
    private String getExceptionFailureMessage(Throwable e)
    {
        return "An exception was thrown: " + e.getMessage();
    }
}
