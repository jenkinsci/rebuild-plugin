package uk.co.bbc.mobileci.promoterebuild.pipeline;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
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
public class KVStoreTest {

    @ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();

    @Rule public RestartableJenkinsRule story = new RestartableJenkinsRule();

    @Rule public GitSampleRepoRule sampleRepo = new GitSampleRepoRule();

    @Test
    public void propertySurvivesJenkinsRestarts() throws Exception {
        story.addStep(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                WorkflowJob p = story.j.jenkins.createProject(WorkflowJob.class, "p");
                p.setDefinition(new CpsFlowDefinition(
                        "node {\n" +
                                "  promotedJob.store('key','value')\n" +
                                "}", true));
                story.j.assertBuildStatusSuccess(p.scheduleBuild2(0));

                KVProperty property = p.getProperty(KVProperty.class);
                assertEquals("KV Store value ", "value", property.retrieve("key"));
            }
        });


    story.addStep(new Statement() {
        @Override
        public void evaluate() throws Throwable {
            WorkflowJob p = story.j.jenkins.getItemByFullName("p", WorkflowJob.class);

            KVProperty property = p.getProperty(KVProperty.class);
            assertEquals("KV Store value ", "value", property.retrieve("key"));

        }
    });

    story.addStep(new Statement() {
        @Override
        public void evaluate() throws Throwable {
            WorkflowJob p = story.j.jenkins.getItemByFullName("p", WorkflowJob.class);
            p.setDefinition(new CpsFlowDefinition(
                    "node {\n" +
                            "  promotedJob.store('key','value2')\n" +
                            "}", true));

            story.j.assertBuildStatusSuccess(p.scheduleBuild2(0));

            KVProperty property = p.getProperty(KVProperty.class);
            assertEquals("KV Store value ", "value2", property.retrieve("key"));

        }
    });



    }


}
