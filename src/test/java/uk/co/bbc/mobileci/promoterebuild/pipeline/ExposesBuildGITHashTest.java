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

/**
 * Created by beazlr02 on 23/04/16.
 */
public class ExposesBuildGITHashTest {

    @ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();

    @Rule public JenkinsRule story = new JenkinsRule();

    @Rule public GitSampleRepoRule sampleRepo = new GitSampleRepoRule();



    @Test
    public void twoCommitsOneHash() throws Exception {

        String scriptThatLogsAssertableStrings =
                "node " +
                        "{\n" +
                        "    echo 'BUILDNUMBER:' + mobileCiSupport.getBuildTriggerHash()\n" +
                        "    \n" +
                        "}";

        commitJenkinsPipelineScript(scriptThatLogsAssertableStrings, sampleRepo);
        WorkflowJob p = setupPipelineInJenkins(story, sampleRepo);
        story.waitForCompletion(p.scheduleBuild2(0).get());

        commitSomething();
        commitSomething();

        WorkflowRun b = story.waitForCompletion(p.scheduleBuild2(0).get());

        String masterHeadHash = workOutCommitHashOfBuild(b);

        story.assertLogContains("BUILDNUMBER:"+masterHeadHash, b);
    }






    private WorkflowJob setupPipelineInJenkins(JenkinsRule story, GitSampleRepoRule sampleRepo) throws IOException {
        WorkflowJob p = story.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsScmFlowDefinition(new GitStep(sampleRepo.toString()).createSCM(), "Jenkinsfile"));
        return p;
    }

    private String workOutCommitHashOfBuild(WorkflowRun b) {
        return b.getAction(BuildData.class).getLastBuiltRevision().getSha1().getName();
    }

    private void commitSomething() throws Exception {
        String randomFileName = RandomStringUtils.randomAlphabetic(10);
        sampleRepo.write(randomFileName, RandomStringUtils.randomAlphabetic(10));
        sampleRepo.git("add", randomFileName);
        sampleRepo.git("commit", "--message=commitForTest"+randomFileName);
    }

    private void commitJenkinsPipelineScript(String scriptThatLogsAssertableStrings, GitSampleRepoRule sampleRepo) throws Exception {
        sampleRepo.init();
        sampleRepo.write("Jenkinsfile", scriptThatLogsAssertableStrings);
        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--message=files");
    }

}
