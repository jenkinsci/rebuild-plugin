package uk.co.bbc.mobileci.promoterebuild.pipeline;

import hudson.Extension;
import hudson.model.Run;
import hudson.scm.ChangeLogSet;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.jenkinsci.plugins.workflow.cps.CpsScript;
import org.jenkinsci.plugins.workflow.cps.GlobalVariable;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

/**
 * Created by beazlr02 on 23/04/16.
 */
@Extension
public class BuildChangesetGlobal extends GlobalVariable {


    @Override
    public String getName() {
        return "pipelineBuildChangeset";
    }

    @Override
    public Object getValue(CpsScript script) throws Exception {
        Run<?, ?> build = script.$build();
        if (build == null) {
            throw new IllegalStateException("cannot find associated build");
        }

        WorkflowRun workflowRun = null;
        if (build instanceof WorkflowRun) {
            workflowRun = (WorkflowRun) build;
        }

        return new BuildChangeSet(workflowRun);
    }

    public static final class BuildChangeSet {

        private WorkflowRun workflowRun;

        public BuildChangeSet(WorkflowRun result) {
            this.workflowRun = result;
        }

        @Whitelisted
        public String getChangeSet() {
            StringBuilder changeSet = new StringBuilder();

            if (workflowRun != null) {
                for (ChangeLogSet<? extends ChangeLogSet.Entry> entries : workflowRun.getChangeSets()) {

                    for (ChangeLogSet.Entry entry : entries) {

                        changeSet.append(entry.getCommitId())
                                .append(" ")
                                .append(entry.getMsg())
                                .append("\n");
                    }
                }
            }

            return changeSet.toString();
        }
    }

}


