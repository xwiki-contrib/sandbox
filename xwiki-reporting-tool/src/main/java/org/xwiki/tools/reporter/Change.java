package org.xwiki.tools.reporter;

import java.util.List;

/** A revision in the source tree which might have broken a test.  */
public interface Change
{
    String getDate();

    int getRevision();

    String getCommitLog();

    String getCommitter();
}
