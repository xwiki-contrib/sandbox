package org.xwiki.extension.internal.script;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionManager;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.UninstallException;
import org.xwiki.script.service.ScriptService;

@Component("extension")
public class ExtensionManagerScriptService implements ScriptService
{
    @Requirement
    private ExtensionManager extensionManager;

    public LocalExtension install(String id, String version) throws InstallException
    {
        // TODO: check rights

        return this.extensionManager.installExtension(new ExtensionId(id, version));
    }

    public Extension resolve(String id, String version) throws ResolveException
    {
        return this.extensionManager.resolveExtension(new ExtensionId(id, version));
    }

    public void uninstall(String id) throws UninstallException
    {
        // TODO: check rights

        this.extensionManager.uninstallExtension(id);
    }
}
