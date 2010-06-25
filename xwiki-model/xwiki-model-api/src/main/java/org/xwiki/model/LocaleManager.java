package org.xwiki.model;

import org.xwiki.model.reference.EntityReference;

import java.util.Locale;

public interface LocaleManager
{
    Entity getEntity(EntityReference reference, Locale locale);
}
