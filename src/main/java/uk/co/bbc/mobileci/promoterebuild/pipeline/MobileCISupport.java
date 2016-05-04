package uk.co.bbc.mobileci.promoterebuild.pipeline;

import hudson.model.Job;
import hudson.model.Run;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import uk.co.bbc.mobileci.promoterebuild.PromoteRebuildCauseAction;

import java.io.IOException;
import java.util.Collection;

/**
* Created by beazlr02 on 04/05/16.
*/
public final class MobileCISupport {

    private BuildChangeSet buildChangeSet;
    private PromotedJob promotedJob;
    private KVStoreProxy kvStoreProxy;

    public MobileCISupport(BuildChangeSet buildChangeSet, PromotedJob promotedJob, KVStoreProxy kvStoreProxy) {
        this.buildChangeSet = buildChangeSet;
        this.promotedJob = promotedJob;
        this.kvStoreProxy = kvStoreProxy;
    }

    @Whitelisted
    public boolean getPromotion() {
        return promotedJob.isPromotion();
    }

    @Whitelisted
    public boolean isPromotion() {
        return promotedJob.isPromotion();
    }

    @Whitelisted
    public String getHash() {
        return promotedJob.getHash();
    }

    @Whitelisted
    public String getFromBuildNumber() {
        return promotedJob.getFromBuildNumber();
    }

    public String toString() {
        return "PromotedJob: from: " +getFromBuildNumber() + " for:"+getHash();
    }

    @Whitelisted
    public void store(String key, String value) throws IOException {
        kvStoreProxy.store(key,value);
    }

    @Whitelisted
    public String retrieve(String key) {
        return kvStoreProxy.retrieve(key);
    }

    @Whitelisted
    public String getChangeSet() {
        return buildChangeSet.getChangeSet();
    }

    @Whitelisted
    public String getBranchName() {
        return buildChangeSet.getBranchName();
    }

    @Whitelisted
    public Collection<String> getBranchNames() {
        return buildChangeSet.getBranchNames();
    }
}
