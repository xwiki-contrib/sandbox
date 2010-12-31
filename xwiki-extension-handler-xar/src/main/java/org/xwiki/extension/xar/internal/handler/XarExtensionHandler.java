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
package org.xwiki.extension.xar.internal.handler;

import java.io.IOException;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.UninstallException;
import org.xwiki.extension.handler.internal.AbstractExtensionHandler;
import org.xwiki.extension.xar.internal.handler.packager.Packager;

@Component("xar")
public class XarExtensionHandler extends AbstractExtensionHandler implements Initializable
{
    @Requirement
    private ComponentManager componentManager;

    @Requirement
    private Packager packager;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.phase.Initializable#initialize()
     */
    public void initialize() throws InitializationException
    {

    }

    // TODO: support question/answer with the UI to resolve conflicts
    public void install(LocalExtension localExtension, String namespace) throws InstallException
    {
        // import xar into wiki (add new version when the page already exists)
        try {
            this.packager.importXAR(localExtension.getFile(), namespace);
        } catch (IOException e) {
            throw new InstallException("Failed to import xar for extension [" + localExtension + "]");
        }
    }

    // TODO: support question/answer with the UI to resolve conflicts
    @Override
    public void upgrade(LocalExtension previousLocalExtension, LocalExtension newLocalExtension, String namespace)
        throws InstallException
    {
        // 1) find all modified pages between old and new version
        // 2) compare old version and wiki (to find pages modified by user)
        // 3) delete pages removed in new version (even if modified ?)
        // 4) merge xar
        // 4.1) merge modified pages in wiki with diff between old/new version
        // 4.2) update unmodified pages different between old and new version
    }

    public void uninstall(LocalExtension localExtension, String namespace) throws UninstallException
    {
        // delete pages from the wiki which belong only to this extension (several extension could have some common
        // pages which is not very nice but still could happen technically)
    }

    @Override
    public boolean isDisableSupported()
    {
        return false;
    }
}
