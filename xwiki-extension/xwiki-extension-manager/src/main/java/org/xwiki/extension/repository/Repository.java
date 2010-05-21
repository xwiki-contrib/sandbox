package org.xwiki.extension.repository;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface Repository
{
    List<Artifact> getArtefacts(int offset, int nb);
}
