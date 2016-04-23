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

        StringBuilder changeSet = new StringBuilder();

        if(build instanceof WorkflowRun) {
            WorkflowRun workflowRun = (WorkflowRun) build;
            List<ChangeLogSet<? extends ChangeLogSet.Entry>> changeSets = workflowRun.getChangeSets();
            for (ChangeLogSet<? extends ChangeLogSet.Entry> entries : changeSets) {

                for (ChangeLogSet.Entry entry : entries) {

                    //echo "${entry.commitId} by ${entry.author} on ${new Date(entry.timestamp)}: ${entry.msg}"
//                    String line = entry.getCommitId()
                    changeSet.append(entry.getCommitId())
                            .append(" ")
                            .append(entry.getMsg())
                            .append("\n");
                }

            }

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


