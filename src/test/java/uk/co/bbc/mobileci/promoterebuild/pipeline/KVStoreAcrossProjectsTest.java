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

/**
 * Created by beazlr02 on 23/04/16.
 */
public class KVStoreAcrossProjectsTest {

    @ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();

    @Rule public RestartableJenkinsRule story = new RestartableJenkinsRule();

    @Rule public GitSampleRepoRule sampleRepo = new GitSampleRepoRule();

    @Test
    public void keysAreNameSpaced() throws Exception {
        story.addStep(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                WorkflowJob p = story.j.jenkins.createProject(WorkflowJob.class, "p");
                p.setDefinition(new CpsFlowDefinition(
                        "node {\n" +
                                "  mobileCiSupport.store('key','value')\n" +
                                "}", true));
                story.j.assertBuildStatusSuccess(p.scheduleBuild2(0));
            }
        });


        story.addStep(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                WorkflowJob p = story.j.jenkins.createProject(WorkflowJob.class, "p2");
                p.setDefinition(new CpsFlowDefinition(
                        "node {\n" +
                                "  echo 'LOADED:' + mobileCiSupport.retrieve('key')\n" +
                                "}", true));

                QueueTaskFuture<WorkflowRun> r = p.scheduleBuild2(0);
                story.j.assertBuildStatusSuccess(r);
                story.j.assertLogNotContains("LOADED:value", r.get());
            }
        });
    }


}
