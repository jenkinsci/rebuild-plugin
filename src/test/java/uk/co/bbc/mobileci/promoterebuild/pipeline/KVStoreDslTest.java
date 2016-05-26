package uk.co.bbc.mobileci.promoterebuild.pipeline;

import hudson.model.queue.QueueTaskFuture;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.scm.GitSampleRepoRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.model.Statement;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.RestartableJenkinsRule;

import static org.junit.Assert.assertEquals;

/**
 * Created by beazlr02 on 23/04/16.
 */
public class KVStoreDslTest {

    @ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();

    @Rule
    public RestartableJenkinsRule story = new RestartableJenkinsRule();

    @Rule
    public GitSampleRepoRule sampleRepo = new GitSampleRepoRule();

    @Test
    public void savesAndLoadsAValue() throws Exception {
        story.addStep(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                WorkflowJob p = story.j.jenkins.createProject(WorkflowJob.class, "p");
                p.setDefinition(new CpsFlowDefinition(
                        "node {\n" +
                                "  mobileCiSupport.store('key','value')\n" +
                                "}", true));
                doAnotherBuild(p);
            }
        });

        story.addStep(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                WorkflowJob p = story.j.jenkins.getItemByFullName("p", WorkflowJob.class);
                p.setDefinition(new CpsFlowDefinition(
                        "node {\n" +
                                "  echo 'LOADED:' + mobileCiSupport.retrieve('key')\n" +
                                "}", true));

                WorkflowRun workflowRun = doAnotherBuild(p);
                story.j.assertLogContains("LOADED:value", workflowRun);
            }
        });
    }

    @Test
    public void updatesAValue() throws Exception {

        savesAndLoadsAValue();

        story.addStep(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                WorkflowJob p = story.j.jenkins.getItemByFullName("p", WorkflowJob.class);
                p.setDefinition(new CpsFlowDefinition(
                        "node {\n" +
                                "  mobileCiSupport.store('key','totallyDifferent')\n" +
                                "}", true));

                doAnotherBuild(p);
            }
        });

        story.addStep(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                WorkflowJob p = story.j.jenkins.getItemByFullName("p", WorkflowJob.class);
                p.setDefinition(new CpsFlowDefinition(
                        "node {\n" +
                                "  echo 'LOADED:' + mobileCiSupport.retrieve('key')\n" +
                                "}", true));

                WorkflowRun workflowRun = doAnotherBuild(p);
                story.j.assertLogContains("LOADED:totallyDifferent", workflowRun);
            }
        });


    }

    private WorkflowRun doAnotherBuild(WorkflowJob p) throws Exception {
        WorkflowRun workflowRun = p.scheduleBuild2(0).get();
        story.j.waitForCompletion(workflowRun);
        story.j.assertBuildStatusSuccess(workflowRun);
        return workflowRun;
    }


}
