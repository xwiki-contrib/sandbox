package org.xwiki.tools.reporter.internal;

import java.util.List;

import org.xwiki.tools.reporter.Change;

public class DefaultChange implements Change
{
    private final String date;

    private final String commitLog;

    private final int revision;

    private final String committer;

    DefaultChange(final String date,
                  final String commitLog,
                  final int revision,
                  final String committer)
    {
        this.date = date;
        this.revision = revision;
        this.commitLog = commitLog;
        this.committer = committer;
    }

    public String getDate()
    {
        return this.date;
    }

    public int getRevision()
    {
        return this.revision;
    }

    public String getCommitLog()
    {
        return this.commitLog;
    }

    public String getCommitter()
    {
        return this.committer;
    }
}
