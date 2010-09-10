package org.xwiki.extension.internal.script;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionManager;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.ResolveException;
import org.xwiki.script.service.ScriptService;

@Component("extension")
public class ExtensionManagerScriptService implements ScriptService
{
    @Requirement
    private ExtensionManager extensionManager;

    public LocalExtension install(String name, String version) throws InstallException
    {
        // TODO: check rights

        return this.extensionManager.installExtension(new ExtensionId(name, version));
    }

    public Extension resolve(String name, String version) throws ResolveException
    {
        return this.extensionManager.resolveExtension(new ExtensionId(name, version));
    }
}
