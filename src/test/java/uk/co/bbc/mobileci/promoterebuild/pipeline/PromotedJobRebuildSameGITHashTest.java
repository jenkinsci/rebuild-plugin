package uk.co.bbc.mobileci.promoterebuild.pipeline;

import hudson.plugins.git.util.BuildData;
import org.apache.commons.lang.RandomStringUtils;
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.scm.GitSampleRepoRule;
import org.jenkinsci.plugins.workflow.steps.scm.GitStep;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.junit.Assert.fail;

/**
 * Created by beazlr02 on 23/04/16.
 */
public class PromotedJobRebuildSameGITHashTest {

    @ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();

    @Rule public JenkinsRule story = new JenkinsRule();

    @Rule public GitSampleRepoRule sampleRepo = new GitSampleRepoRule();



    @Test
    public void promotingCommitNotFromHead() throws Exception {

        String script =
                "node " +
                        "{\n" +
                        "  checkout scm\n" +
                        "  echo readFile('file')\n" +
                        "}";

        commitJenkinsPipelineScript(script, sampleRepo);

        sampleRepo.write("file", "initial content");
        sampleRepo.git("add", "file");
        sampleRepo.git("commit", "--message=init");

        WorkflowJob p = setupPipelineInJenkins(story, sampleRepo);
        story.waitForCompletion(p.scheduleBuild2(0).get());

        sampleRepo.write("file", "subsequent content");
        sampleRepo.git("add", "file");
        sampleRepo.git("commit", "--message=update");

        story.waitForCompletion(p.scheduleBuild2(0).get());

        //PROMOTE build 1
        WorkflowRun b = promoteBuildNumber(p, 1);
        story.assertBuildStatusSuccess(b);

        story.assertLogContains("initial content", b);
    }

    @Test
    public void promotingCommitNotFromHeadUsesPipelineScriptNotFromHead() throws Exception {

        String script =
                "node " +
                        "{\n" +
                        "  checkout scm\n" +
                        "  echo 'I am the first pipeline script'\n" +
                        "}";

        sampleRepo.init();
        sampleRepo.write("Jenkinsfile", script);
        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--message=Add Jenkinsfile");

        WorkflowJob p = story.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsScmFlowDefinition(new GitStep(sampleRepo.toString()).createSCM(), "Jenkinsfile"));

        WorkflowRun b = story.waitForCompletion(p.scheduleBuild2(0).get());
        story.assertLogContains("I am the first pipeline script", b);

        script =
                "node " +
                        "{\n" +
                        "  checkout scm\n" +
                        "  echo 'I am the second pipeline script'\n" +
                        "}";

        sampleRepo.write("Jenkinsfile", script);
        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--message=Update Jenkinsfile");

        b = story.waitForCompletion(p.scheduleBuild2(0).get());
        story.assertLogContains("I am the second pipeline script", b);

        b = promoteBuildNumber(p, 1);
        story.assertLogContains("I am the first pipeline script", b);
    }

    private WorkflowJob setupPipelineInJenkins(JenkinsRule story, GitSampleRepoRule sampleRepo) throws IOException {
        WorkflowJob p = story.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsScmFlowDefinition(new GitStep(sampleRepo.toString()).createSCM(), "Jenkinsfile"));
        return p;
    }

    private void commitJenkinsPipelineScript(String scriptThatLogsAssertableStrings, GitSampleRepoRule sampleRepo) throws Exception {
        sampleRepo.init();
        sampleRepo.write("Jenkinsfile", scriptThatLogsAssertableStrings);
        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--message=files");
    }

    private WorkflowRun promoteBuildNumber(WorkflowJob p, int buildNumber) throws IOException, SAXException, InterruptedException {
        WorkflowRun b;
        b = p.getBuildByNumber(buildNumber);
        story.createWebClient().getPage(b, "promoterebuild");
        b = p.getLastBuild();
        story.waitForCompletion(b);
        return b;
    }
}
