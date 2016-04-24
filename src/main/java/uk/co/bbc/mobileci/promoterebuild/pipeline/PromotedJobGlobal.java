package uk.co.bbc.mobileci.promoterebuild.pipeline;

import hudson.Extension;
import hudson.model.Run;
import hudson.plugins.git.util.BuildData;
import hudson.scm.ChangeLogSet;
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

        private final String hash;
        @Whitelisted private boolean promotion;

        public PromotedJob(Run<?, ?> build) {

            promotion = build.getActions(PromoteRebuildCauseAction.class).size()>0;

            this.hash = build.getPreviousBuild().getAction(BuildData.class).getLastBuiltRevision().getSha1().getName();
        }

        @Whitelisted
        public boolean isPromotion() {

            return promotion;
        }

        @Whitelisted
        public String getHash() {
            return hash;
        }
    }

}


