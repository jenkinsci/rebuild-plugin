package uk.co.bbc.mobileci.promoterebuild.pipeline;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * Created by beazlr02 on 23/04/16.
 */
public class MobileCIGlobalSerialisesAcrossStages {
    @ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    @Ignore(/* Ignored- why is this ignored? */)
    public void promotedJobGlobalCreated() throws Exception {
        WorkflowJob p = j.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(
                "def support = mobileCiSupport\n" +
                        "\n" +
                        "stage 'one'\n" +
                        "node {\n" +
                        "\tsupport.isPromotion()\n" +
                        "}\n" +
                        "\n" +
                        "stage 'two'\n" +
                        "node {\n" +
                        "\tsupport.isPromotion()\n" +
                        "}", true));
        WorkflowRun run = j.assertBuildStatusSuccess(p.scheduleBuild2(0));
    }



}
