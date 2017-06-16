package uk.co.bbc.mobileci.promoterebuild.pipeline;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.plugins.git.BranchSpec;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.SubmoduleConfig;
import hudson.plugins.git.UserRemoteConfig;
import hudson.plugins.git.extensions.GitSCMExtension;
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
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;


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
    public void jobWithPipelineLibsCanBePromoted() throws Exception {

        sampleLibsRepo.init();
        String libScript =
                "void printSomething() {\n" +
                    "  echo 'I am a lib!'\n" +
                    "}\n";
        sampleLibsRepo.write("vars/print.groovy", libScript);
        sampleLibsRepo.git("add", "vars");
        sampleLibsRepo.git("commit", "--message=init");
        sampleLibsRepo.git("tag", "1.0.0");

        sampleJobRepo.init();
        String pipelineScript =
                "@Library('libs') _\n" +
                        "node {\n" +
                        "  checkout scm\n" +
                        "  print.printSomething()\n" +
                        "}\n";
        sampleJobRepo.write("Jenkinsfile", pipelineScript);
        sampleJobRepo.git("add", "Jenkinsfile");
        sampleJobRepo.git("commit", "--message=files");

        Folder f = story.jenkins.createProject(Folder.class, "f");
        LibraryConfiguration libs = new LibraryConfiguration("libs",
                new SCMRetriever(
                        new GitSCM(Collections.singletonList(new UserRemoteConfig(sampleLibsRepo.fileUrl(), null, null, null)),
                                Collections.singletonList(new BranchSpec("refs/tags/${library.libs.version}")),
                                false, Collections.<SubmoduleConfig>emptyList(), null, null, Collections.<GitSCMExtension>emptyList())));
        libs.setDefaultVersion("1.0.0");
        libs.setImplicit(false);
        libs.setAllowVersionOverride(true);
        f.getProperties().add(new FolderLibraries(Collections.singletonList(libs)));

        WorkflowJob p = f.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsScmFlowDefinition(new GitStep(sampleJobRepo.toString()).createSCM(), "Jenkinsfile"));

        WorkflowRun b = story.waitForCompletion(p.scheduleBuild2(0).get());
        story.assertLogContains("I am a lib!", b);

        b = promoteBuildNumber(p, 1);
        story.assertBuildStatusSuccess(b);
    }

    @Test
    public void pipelineScriptWithLibAlwaysUsesDefaultVersion() throws Exception {

        sampleLibsRepo.init();
        String libScript = "void printSomething() {\n" +
                "  echo 'I am the first lib!'\n" +
                "}";
        sampleLibsRepo.write("vars/print.groovy", libScript);
        sampleLibsRepo.git("add", "vars");
        sampleLibsRepo.git("commit", "--message=init");
        sampleLibsRepo.git("tag", "1.0.0");

        sampleJobRepo.init();
        String pipelineScript =
                "@Library('libs') _\n" +
                        "node {\n" +
                        "  checkout scm\n" +
                        "  print.printSomething()\n" +
                        "}\n";
        sampleJobRepo.write("Jenkinsfile", pipelineScript);
        sampleJobRepo.git("add", "Jenkinsfile");
        sampleJobRepo.git("commit", "--message=init");

        Folder f = story.jenkins.createProject(Folder.class, "f");
        LibraryConfiguration libs = new LibraryConfiguration("libs",
                new SCMRetriever(
                        new GitSCM(Collections.singletonList(new UserRemoteConfig(sampleLibsRepo.fileUrl(), null, null, null)),
                                Collections.singletonList(new BranchSpec("refs/tags/${library.libs.version}")),
                                false, Collections.<SubmoduleConfig>emptyList(), null, null, Collections.<GitSCMExtension>emptyList())));
        libs.setDefaultVersion("1.0.0");
        libs.setImplicit(false);
        libs.setAllowVersionOverride(true);
        f.getProperties().add(new FolderLibraries(Collections.singletonList(libs)));

        WorkflowJob p = f.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsScmFlowDefinition(new GitStep(sampleJobRepo.toString()).createSCM(), "Jenkinsfile"));
        WorkflowRun b = story.waitForCompletion(p.scheduleBuild2(0).get());
        story.assertLogContains("I am the first lib!", b);

        libScript = "void printSomething() {\n" +
                "  echo 'I am the second lib!'\n" +
                "}";
        sampleLibsRepo.write("vars/print.groovy", libScript);
        sampleLibsRepo.git("add", "vars");
        sampleLibsRepo.git("commit", "--message=update");
        b = story.waitForCompletion(p.scheduleBuild2(0).get());
        story.assertLogContains("I am the first lib!", b);

        b = promoteBuildNumber(p, 1);
        story.assertLogContains("I am the first lib!", b);
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
