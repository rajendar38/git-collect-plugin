package io.jenkins.plugins.gitcollect;

import java.io.Serializable;
import java.util.Collection;

import hudson.plugins.git.Branch;
import hudson.plugins.git.Revision;

public class LocalGitInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String scmName;
    private final String remoteUrl;
    private final Revision builtRevision;
    private final Revision markedRevision;

    public LocalGitInfo(String scmName, String remoteUrl, Revision builRevision,
                        Revision markedRevision) {
        this.scmName = scmName;
        this.remoteUrl = remoteUrl;
        this.builtRevision = builRevision;
        this.markedRevision = markedRevision;
    }

    public String getBranch() {
        Collection<Branch> branches = builtRevision.getBranches();
        return branches.iterator().next().getName();
    }

    public String getShaRevision() {
        return builtRevision.getSha1String();
    }

    public String getScmName() {
        return scmName;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public Revision getMarkedRevision() {
        return markedRevision;
    }

    public Revision getBuiltRevision() {
        return builtRevision;
    }
}
