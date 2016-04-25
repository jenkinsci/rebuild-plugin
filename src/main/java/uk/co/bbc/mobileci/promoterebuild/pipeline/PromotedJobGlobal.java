package uk.co.bbc.mobileci.promoterebuild.pipeline;

import hudson.Extension;
import hudson.model.Run;
import hudson.plugins.git.Revision;
import hudson.plugins.git.util.BuildData;
import hudson.scm.ChangeLogSet;
import org.eclipse.jgit.lib.ObjectId;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.jenkinsci.plugins.workflow.cps.CpsScript;
import org.jenkinsci.plugins.workflow.cps.GlobalVariable;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import uk.co.bbc.mobileci.promoterebuild.PromoteRebuildCauseAction;

import java.util.List;

/**
 * Created by beazlr02 on 23/04/16.
 */
@Extension
public class PromotedJobGlobal extends GlobalVariable {


    @Override
    public String getName() {
        return "promotedJob";
    }

    @Override
    public Object getValue(CpsScript script) throws Exception {
        Run<?, ?> build = script.$build();
        if (build == null) {
            throw new IllegalStateException("cannot find associated build");
        }

        return new PromotedJob(build);
    }

    public static final class PromotedJob {

        private  String hash;
        @Whitelisted private boolean promotion;
        private String fromBuildNumber;

        public PromotedJob(Run<?, ?> build) {

            PromoteRebuildCauseAction action = build.getAction(PromoteRebuildCauseAction.class);
            promotion = action!=null;

            Run<?, ?> previousBuild = build.getPreviousBuild();
            if(isPromotion() && previousBuild !=null) {


                // need to get data from cause, including commit hash
                // write a test for commit hash
                PromoteRebuildCauseAction.PromoteRebuildCause promoteRebuildCause = action.getPromoteRebuildCause();
                this.hash = promoteRebuildCause.getBuildHash();
                this.fromBuildNumber = String.valueOf(promoteRebuildCause.getUpstreamBuild());
            }
        }

        private String  parseBuildHash(Run<?, ?> previousBuild) {
            String hash = null;
            BuildData action = previousBuild.getAction(BuildData.class);
            if(action!=null) {
                Revision lastBuiltRevision = action.getLastBuiltRevision();
                if(lastBuiltRevision!=null) {
                    ObjectId sha1 = lastBuiltRevision.getSha1();
                    if (sha1!=null) {
                        hash = sha1.getName();
                    }
                }
            }
            return hash;
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

}


