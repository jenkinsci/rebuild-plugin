package uk.co.bbc.mobileci.promoterebuild.pipeline;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.plugins.git.GitSCM;
import jenkins.plugins.git.GitSCMSource;
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.libs.FolderLibraries;
import org.jenkinsci.plugins.workflow.libs.LibraryConfiguration;
import org.jenkinsci.plugins.workflow.libs.SCMRetriever;
import org.jenkinsci.plugins.workflow.libs.SCMSourceRetriever;
import org.jenkinsci.plugins.workflow.steps.scm.GitSampleRepoRule;
import org.jenkinsci.plugins.workflow.steps.scm.GitStep;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Copyright © 2017 Media Applications Technologies. All rights reserved.
 */
public class PromotedJobWithPipelineLibs {

    @ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();

    @Rule
    public JenkinsRule story = new JenkinsRule();

    @Rule public GitSampleRepoRule sampleJobRepo = new GitSampleRepoRule();
    @Rule public GitSampleRepoRule sampleLibsRepo = new GitSampleRepoRule();

    @Test
    public void dontFearTheHash() throws Exception {

        sampleLibsRepo.init();
        String libCode = "void printSomething() {\n" +
                "  echo 'I am a lib!'\n" +
                "}";
        sampleLibsRepo.write("vars/print.groovy", libCode);
        sampleLibsRepo.git("add", "vars");
        sampleLibsRepo.git("commit", "--message=init");

        String scriptThatLoadsLibs =
                "node {\n" +
                        "  checkout scm\n" +
                        "  print.printSomething()\n" +
                        "}\n";

        sampleJobRepo.init();
        sampleJobRepo.write("Jenkinsfile", scriptThatLoadsLibs);
        sampleJobRepo.git("add", "Jenkinsfile");
        sampleJobRepo.git("commit", "--message=files");

        Folder f = story.jenkins.createProject(Folder.class, "f");
        LibraryConfiguration libs = new LibraryConfiguration("libs", new SCMRetriever(new GitStep(sampleLibsRepo.toString()).createSCM()));
        libs.setDefaultVersion("master");
        libs.setImplicit(true);
        libs.setAllowVersionOverride(true);
        f.getProperties().add(new FolderLibraries(Collections.singletonList(libs)));

        WorkflowJob p = f.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsScmFlowDefinition(new GitStep(sampleJobRepo.toString()).createSCM(), "Jenkinsfile"));
        WorkflowRun b = story.waitForCompletion(p.scheduleBuild2(0).get());
        story.assertLogContains("I am a lib!", b);

        b = promoteBuildNumber(p, 1);
        story.assertBuildStatusSuccess(b);
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
