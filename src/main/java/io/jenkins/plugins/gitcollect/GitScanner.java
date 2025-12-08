package io.jenkins.plugins.gitcollect;

import jenkins.MasterToSlaveFileCallable;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.transport.URIish;
import org.jenkinsci.plugins.gitclient.GitClient;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import hudson.plugins.git.Branch;
import hudson.plugins.git.GitException;
import hudson.plugins.git.Revision;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressFBWarnings(value = "SE", justification = "GitClient implementation is Serializable")
public class GitScanner extends MasterToSlaveFileCallable<LocalGitInfo> {
    private static final long serialVersionUID = 1L;
    private final GitClient git;
    private final String markedCommit;

    public static final Logger LOGGER = Logger.getLogger(GitScanner.class.getName());

    public GitScanner(GitClient git, String markedCommit) {
        this.git = git;
        this.markedCommit = markedCommit;
    }

    @Override
    public LocalGitInfo invoke(File workspace, VirtualChannel channel) throws IOException, InterruptedException {

        String url = git.getRemoteUrl("origin");

        String referenceHead = (markedCommit != null && !markedCommit.trim().isEmpty())
                           ? markedCommit
                           : "HEAD";

        ObjectId resolvedObjectId;
        try {
            resolvedObjectId = git.revParse(referenceHead);
        } catch (GitException e) {
            throw new IOException("[GitCollect] Could not resolve revision '" + referenceHead + "'", e);
        }

        ObjectId topOnObjectId;
        try {
            topOnObjectId = git.revParse("HEAD");
        } catch (GitException e) {
            throw new IOException("[GitCollect] Could not resolve revision '" + referenceHead + "'", e);
        }

        LOGGER.log(Level.FINE, "referenceHead: " + referenceHead);

        try {
            Revision builtRevision = new Revision(topOnObjectId);
            Revision markedRevision = new Revision(resolvedObjectId);

            Collection<Branch> branches = new ArrayList<>();
            branches.add(new Branch(referenceHead, resolvedObjectId));
            builtRevision.setBranches(branches);

            if (markedCommit != null && !ObjectId.isId(markedCommit)) {
                branches = new ArrayList<>();
                branches.add(new Branch(markedCommit, resolvedObjectId));
                markedRevision.setBranches(branches);
            }

            URIish uri = new URIish(url);

            return new LocalGitInfo(uri.getHumanishName(), url, builtRevision, markedRevision);
        } catch (URISyntaxException e) {
            throw new IOException("[GitCollect] Unable to find an uri name", e);
        }
    }
}
