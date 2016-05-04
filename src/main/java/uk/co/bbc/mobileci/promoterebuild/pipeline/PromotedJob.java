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
public final class PromotedJob {

    private  String hash;
    @Whitelisted
    private boolean promotion;
    private String fromBuildNumber;

    public PromotedJob(Run<?, ?> build) {
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
        return promotion;
    }

    @Whitelisted
    public String getHash() {
        return hash;
    }

    @Whitelisted
    public String getFromBuildNumber() {
        return fromBuildNumber;
    }

    public String toString() {
        return "PromotedJob: from: " +getFromBuildNumber() + " for:"+getHash();
    }

}
