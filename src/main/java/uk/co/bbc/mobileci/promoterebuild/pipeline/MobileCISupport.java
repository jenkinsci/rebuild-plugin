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

    private  String hash;
    @Whitelisted
    private boolean promotion;
    private String fromBuildNumber;
    private Run<?, ?> build;
    private BuildChangeSet buildChangeSet;
    private PromotedJob promotedJob;

    public MobileCISupport(Run<?, ?> build, BuildChangeSet buildChangeSet, PromotedJob promotedJob) {
        this.build = build;
        this.buildChangeSet = buildChangeSet;
        this.promotedJob = promotedJob;

        PromoteRebuildCauseAction action = build.getAction(PromoteRebuildCauseAction.class);
        if(action!=null) {
            promotion = true;
            PromoteRebuildCauseAction.PromoteRebuildCause promoteRebuildCause = action.getPromoteRebuildCause();
            this.hash = promoteRebuildCause.getBuildHash();
            this.fromBuildNumber = String.valueOf(promoteRebuildCause.getUpstreamBuild());
        }
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
        Job<?, ?> parent = build.getParent();
        WorkflowJob wParent = (WorkflowJob) parent;
        KVStore property = wParent.getProperty(KVStore.class);
        if(property==null) {
            property = new KVStore();
        } else {
           wParent.removeProperty(KVStore.class);
        }
        property.store(key, value);
        wParent.addProperty(property);
    }

    @Whitelisted
    public String retrieve(String key) {
        Job<?, ?> parent = build.getParent();
        WorkflowJob wParent = (WorkflowJob) parent;
        KVStore property = wParent.getProperty(KVStore.class);
        String result = null;
        if(property!=null) {
            result = property.retrieve(key);
        }
        return result;
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
