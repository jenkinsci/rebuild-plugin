package uk.co.bbc.mobileci.promoterebuild.pipeline;

import hudson.Extension;
import hudson.model.Run;
import hudson.scm.ChangeLogSet;
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

        List<PromoteRebuildCauseAction> result = build.getActions(PromoteRebuildCauseAction.class);
        return new PromotedJob(result);
    }

    public static final class PromotedJob {

        private boolean promotion;

        public PromotedJob(List<PromoteRebuildCauseAction> result) {
            promotion = result.size()>0;
        }

        public boolean isPromotion() {

            return promotion;
        }
    }

}


