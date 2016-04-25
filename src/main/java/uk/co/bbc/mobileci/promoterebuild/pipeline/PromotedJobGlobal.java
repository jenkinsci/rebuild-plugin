package uk.co.bbc.mobileci.promoterebuild.pipeline;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.jenkinsci.plugins.workflow.cps.CpsScript;
import org.jenkinsci.plugins.workflow.cps.GlobalVariable;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import uk.co.bbc.mobileci.promoterebuild.PromoteRebuildCauseAction;

import java.io.IOException;

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
        private Run<?, ?> build;

        public PromotedJob(Run<?, ?> build) {
            this.build = build;

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

        @Whitelisted
        public void store(String key, String value) throws IOException {
            Job<?, ?> parent = build.getParent();
            WorkflowJob wParent = (WorkflowJob) parent;
            KVProperty property = wParent.getProperty(KVProperty.class);
            if(property==null) {
                property = new KVProperty();
            } else {
               wParent.removeProperty(KVProperty.class);
            }
            property.store(key, value);
            wParent.addProperty(property);
        }

        @Whitelisted
        public String retrieve(String key) {
            Job<?, ?> parent = build.getParent();
            WorkflowJob wParent = (WorkflowJob) parent;
            KVProperty property = wParent.getProperty(KVProperty.class);
            String result = null;
            if(property!=null) {
                result = property.retrieve(key);
            }
            return result;
        }
    }

}


