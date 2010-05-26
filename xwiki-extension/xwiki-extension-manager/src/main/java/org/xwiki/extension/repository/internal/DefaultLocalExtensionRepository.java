package org.xwiki.extension.repository.internal;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryId;
import org.xwiki.extension.repository.ExtensionRepositoryManager;
import org.xwiki.extension.repository.LocalExtensionRepository;

/**
 * TODO: decide local repository format (probably maven-like)
 */
@Component
public class DefaultLocalExtensionRepository implements LocalExtensionRepository, Initializable
{
    private ExtensionRepositoryId repositoryId;

    private File rootFolder;

    public void initialize() throws InitializationException
    {
        // TODO: resolve local repository path/uri

        this.repositoryId = new ExtensionRepositoryId("local", "xwiki", uri);
        this.rootFolder = new File(uri);
    }

    // Repository

    public Extension resolve(ExtensionId extensionId)
    {
        return getLocalExtension(extensionId);
    }

    public ExtensionRepositoryId getId()
    {
        return this.repositoryId;
    }

    // LocalRepository

    public List<LocalExtension> getLocalExtensions(int nb, int offset)
    {
        // TODO
        return Collections.emptyList();
    }

    public LocalExtension getLocalExtension(ExtensionId extensionId)
    {

    }

    public List<Extension> getExtensions(int nb, int offset)
    {
        return (List) getLocalExtensions(nb, offset);
    }

    public File getFile(Extension extension)
    {
        return new File(this.rootFolder, extension.getName() + "-" + extension.getVersion() + "."
            + extension.getType().getFileExtension());
    }

    public void installExtension(Extension extension)
    {
        File artifactFile = getFile(extension);

        if (!artifactFile.exists()) {
            extension.download(artifactFile);

            // TODO: write descriptor, need to decide the descriptor format
        }
    }

    public void uninstallExtension(Extension extension)
    {
        // TODO: delete artifact file and descriptor
    }
}
