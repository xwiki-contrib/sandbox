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
package org.xwiki.opensocial.social.mock;

import org.apache.shindig.social.core.config.SocialApiGuiceModule;
import org.apache.shindig.social.opensocial.model.Person;
import org.apache.shindig.social.opensocial.oauth.OAuthDataStore;
import org.apache.shindig.social.opensocial.spi.ActivityService;
import org.apache.shindig.social.opensocial.spi.AppDataService;
import org.apache.shindig.social.opensocial.spi.PersonService;
import org.xwiki.opensocial.social.mock.model.PersonXW;
import org.xwiki.opensocial.social.mock.oauth.MockOAuthDataStore;
import org.xwiki.opensocial.social.mock.spi.internal.MockActivityService;
import org.xwiki.opensocial.social.mock.spi.internal.MockAppDataService;
import org.xwiki.opensocial.social.mock.spi.internal.MockPersonService;

import com.google.inject.name.Names;

public class MockSocialModule extends SocialApiGuiceModule
{

    /** {@inheritDoc} */
    @Override
    protected void configure()
    {
        // bind standard Shindig Social API implementations
        super.configure();

        bind(String.class).annotatedWith(Names.named("shindig.canonical.json.db")).toInstance(
            "sampledata/canonicaldb.json");

        // bind XWiki service implementations
        bind(Person.class).to(PersonXW.class);

        bind(PersonService.class).to(MockPersonService.class);
        bind(AppDataService.class).to(MockAppDataService.class);
        bind(ActivityService.class).to(MockActivityService.class);

        bind(OAuthDataStore.class).to(MockOAuthDataStore.class);
    }

}
