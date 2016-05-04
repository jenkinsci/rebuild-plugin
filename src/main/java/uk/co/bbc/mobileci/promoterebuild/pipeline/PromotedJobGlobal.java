package uk.co.bbc.mobileci.promoterebuild.pipeline;

import hudson.Extension;
import hudson.model.Run;
import org.jenkinsci.plugins.workflow.cps.CpsScript;
import org.jenkinsci.plugins.workflow.cps.GlobalVariable;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

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

        WorkflowRun workflowRun = null;
        if (build instanceof WorkflowRun) {
            workflowRun = (WorkflowRun) build;
        }
        BuildChangeSet buildChangeSet = new BuildChangeSet(workflowRun);
        PromotedJob promotedJob = new PromotedJob(build);
        return new MobileCISupport(build, buildChangeSet, promotedJob);
    }

}


