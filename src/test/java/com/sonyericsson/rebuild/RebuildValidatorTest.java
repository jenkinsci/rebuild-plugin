/*
 *  The MIT License
 *
 *  Copyright 2010 Sony Ericsson Mobile Communications. All rights reserved.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package com.sonyericsson.rebuild;

import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Build;
import hudson.model.ParameterValue;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Project;
import hudson.model.Run;
import hudson.model.StringParameterDefinition;
import hudson.model.StringParameterValue;

import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestExtension;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * For testing the extension point.
 *
 * @author Gustaf Lundh &lt;gustaf.lundh@sonyericsson.com&gt;
 */
public class RebuildValidatorTest {
    /**
     * Sleep delay value.
     */
    public static final int DELAY = 100;
    
    @Rule
    public JenkinsRule j = new JenkinsRule();


    /**
     * Tests with no extensions.
     *
     * @throws IOException
     *             IOException
     * @throws InterruptedException
     *             InterruptedException
     * @throws ExecutionException
     *             ExecutionException
     */
    @Test
    public void testNoRebuildValidatorExtension() throws IOException,
            InterruptedException, ExecutionException {
        Project projectA = j.createFreeStyleProject("testFreeStyleA");
        Build buildA = (Build) projectA.scheduleBuild2(
                0,
                new Cause.UserIdCause(),
                new ParametersAction(new StringParameterValue("party",
                        "megaparty"))).get();
        assertNotNull(buildA.getAction(RebuildAction.class));
    }

    /**
     * Tests with an extension returning isApplicable true.
     *
     * @throws IOException
     *             IOException
     * @throws InterruptedException
     *             InterruptedException
     * @throws ExecutionException
     *             ExecutionException
     */
    @Test
    public void testRebuildValidatorExtensionIsApplicableTrue()
            throws IOException, InterruptedException, ExecutionException {
        j.getInstance().getExtensionList(RebuildValidator.class).add(0,
                new ValidatorAlwaysApplicable());
        Project projectA = j.createFreeStyleProject("testFreeStyleB");
        Build buildA = (Build) projectA.scheduleBuild2(
                0,
                new Cause.UserIdCause(),
                new ParametersAction(new StringParameterValue("party",
                        "megaparty"))).get();
        assertNull(buildA.getAction(RebuildAction.class));
    }

    /**
     * Tests with an extension returning isApplicable false.
     *
     * @throws IOException
     *             IOException
     * @throws InterruptedException
     *             InterruptedException
     * @throws ExecutionException
     *             ExecutionException
     */
    @Test
    public void testRebuildValidatorExtensionIsApplicableFalse()
            throws IOException, InterruptedException, ExecutionException {
        j.getInstance().getExtensionList(RebuildValidator.class).add(0,
                new ValidatorNeverApplicable());
        Project projectA = j.createFreeStyleProject("testFreeStyleC");
        Build buildA = (Build) projectA.scheduleBuild2(
                0,
                new Cause.UserIdCause(),
                new ParametersAction(new StringParameterValue("party",
                        "megaparty"))).get();
        assertNotNull(buildA.getAction(RebuildAction.class));
    }

    /**
     * Tests with two extensions returning isApplicable true AND false.
     *
     * @throws IOException
     *             IOException
     * @throws InterruptedException
     *             InterruptedException
     * @throws ExecutionException
     *             ExecutionException
     */
    @Test
    public void testRebuildValidatorExtensionIsApplicableTrueFalse()
            throws IOException, InterruptedException, ExecutionException {
        j.getInstance().getExtensionList(RebuildValidator.class).add(0,
                new ValidatorAlwaysApplicable());
        j.getInstance().getExtensionList(RebuildValidator.class).add(0,
                new ValidatorNeverApplicable());
        Project projectA = j.createFreeStyleProject("testFreeStyleC");
        Build buildA = (Build) projectA.scheduleBuild2(
                0,
                new Cause.UserIdCause(),
                new ParametersAction(new StringParameterValue("party",
                        "megaparty"))).get();
        assertNull(buildA.getAction(RebuildAction.class));
    }

    /**
     * Creates a new freestyle project and checks if the rebuild action is
     * available on the project level.
     *
     * @throws Exception
     *             Exception
     */
    @Test
    public void testWhenProjectWithoutParamsThenRebuildProjectAvailable()
            throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();

        FreeStyleBuild build = project.scheduleBuild2(0).get();

        RebuildLastCompletedBuildAction action = build.getProject().getAction(
                RebuildLastCompletedBuildAction.class);
        assertNotNull(action);
    }

    /**
     * Creates a new freestyle project and builds the project with a string
     * parameter. If the build is succesful, a rebuild of the last build is
     * done. The rebuild on the project level should point to the last build
     *
     * @throws Exception
     *             Exception
     */
    @Test
    public void testWhenProjectWithNoParamsDefinedThenRebuildofBuildWithParamsShouldShowParams()
            throws Exception {
        System.setProperty("hudson.model.ParametersAction.keepUndefinedParameters", "true");
        FreeStyleProject project = j.createFreeStyleProject();

        // Build (#1)
        project.scheduleBuild2(0, new Cause.UserIdCause(),
                new ParametersAction(new StringParameterValue("name", "ABC")))
                .get();
        HtmlPage rebuildConfigPage = j.createWebClient().getPage(project,
                "1/rebuild");
        WebAssert.assertElementPresentByXPath(rebuildConfigPage,
                "//div[@name='parameter']/input[@value='ABC']");
    }

    /**
     * Creates a new freestyle project and builds the project with a string
     * parameter. If the build is successful, a rebuild of the last build is
     * done, with autorebuild. The rebuild should have the same parameter values
     * as the original build.
     *
     * @throws Exception
     *             Exception
     */
    @Test
    public void testAutoRebuildAsRequestParameter()
            throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        project.addProperty(new ParametersDefinitionProperty(
                new StringParameterDefinition("name", "defaultValue")));

        // Build (#1)
        project.scheduleBuild2(0, new Cause.UserIdCause(),
                new ParametersAction(new StringParameterValue("name", "ABC")))
                .get();
        HtmlPage rebuildConfigPage = j.createWebClient().getPage(project,
                "1/rebuild?autorebuild");
        Run r = project.getBuild("2");
        assertNotNull(r);
        ParametersAction paramAction = r.getAction(ParametersAction.class);
        assertNotNull(paramAction);
        assertEquals("ABC", paramAction.getParameter("name").getValue());
    }

    /**
     * Creates a new freestyle project and builds the project with a string
     * parameter. If the build is succesful, a rebuild of the last build is
     * done. The rebuild on the project level should point to the last build
     *
     * @throws Exception
     *             Exception
     */
    @Test
    public void testWhenProjectWithParamsThenRebuildProjectExecutesRebuildOfLastBuild()
            throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        project.addProperty(new ParametersDefinitionProperty(
                new StringParameterDefinition("name", "defaultValue")));

        // Build (#1)
        project.scheduleBuild2(0, new Cause.UserIdCause(),
                new ParametersAction(new StringParameterValue("name", "test")))
                .get();
        HtmlPage rebuildConfigPage = j.createWebClient().getPage(project,
                "1/rebuild");
        // Rebuild (#2)
        j.submit(rebuildConfigPage.getFormByName("config"));

        HtmlPage projectPage = j.createWebClient().getPage(project);
        WebAssert.assertLinkPresentWithText(projectPage, "Rebuild Last");

        HtmlAnchor rebuildHref = projectPage.getAnchorByText("Rebuild Last");
        assertEquals("Rebuild Last should point to the second build", "/jenkins/"
                + project.getUrl() + "lastCompletedBuild/rebuild",
                rebuildHref.getHrefAttribute());
    }

    /**
     * Creates a new freestyle project and rebuild. Check that the RebuildCause
     * has been set to the new build. Check also that a UserIdCause is added.
     *
     * @throws Exception
     *             Exception
     */
    @Test
    public void testWhenProjectWithCauseThenCauseIsCopiedAndUserIdCauseAdded()
            throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        project.addProperty(new ParametersDefinitionProperty(
                new StringParameterDefinition("name", "defaultValue")));

        // Build (#1)
        project.scheduleBuild2(0, new Cause.RemoteCause("host", "note"),
                new ParametersAction(new StringParameterValue("name", "test")))
                .get();
        HtmlPage rebuildConfigPage = j.createWebClient().getPage(project,
                "1/rebuild");
        // Rebuild (#2)
        j.submit(rebuildConfigPage.getFormByName("config"));

        j.createWebClient().getPage(project).getAnchorByText("Rebuild Last")
                .click();

        while (project.isBuilding()) {
            Thread.sleep(DELAY);
        }
        boolean hasRebuildCause = false;
        boolean hasRemoteCause = false;
        boolean hasUserIdCause = false;
        for (Action action : project.getLastCompletedBuild().getAllActions()) {
            if (action instanceof CauseAction) {
                for(Cause cause : ((CauseAction)action).getCauses()) {
                    if (cause.getClass().equals(RebuildCause.class)) {
                        hasRebuildCause = true;
                    }
                    if (cause.getClass().equals(Cause.RemoteCause.class)) {
                        hasRemoteCause = true;
                    }
                    if (cause.getClass().equals(Cause.UserIdCause.class)) {
                        hasUserIdCause = true;
                    }
                }

            }
        }
        assertTrue("Build should have user, remote and rebuild causes",
                hasRebuildCause && hasRemoteCause && hasUserIdCause);
    }

    /**
     * Creates a new freestyle project and rebuild, then rebuilds the rebuilt build run.
     * Check that there is a single instance of RebuildCause and UserIdCause
     *
     * @throws Exception
     *             Exception
     */
    @Test
    public void testWhenProjectWithChainedRebuildsThenSingleUserIdCauseAndReplayCause()
            throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        project.addProperty(new ParametersDefinitionProperty(
                new StringParameterDefinition("name", "defaultValue")));

        // Build (#1)
        project.scheduleBuild2(0, new Cause.RemoteCause("host", "note"),
                new ParametersAction(new StringParameterValue("name", "test")))
                .get();

        // Rebuild (#2)
        WebClient webClient = j.createWebClient();
        HtmlPage rebuildConfigPage1 = webClient.getPage(project, "1/rebuild");
        j.submit(rebuildConfigPage1.getFormByName("config"));

        j.createWebClient().getPage(project).getAnchorByText("Rebuild Last")
                .click();

        while (project.isBuilding()) {
            Thread.sleep(DELAY);
        }

        // Rebuild (#3)
        HtmlPage rebuildConfigPage2 = webClient.getPage(project, "2/rebuild");
        j.submit(rebuildConfigPage2.getFormByName("config"));

        j.createWebClient().getPage(project).getAnchorByText("Rebuild Last")
                .click();

        while (project.isBuilding()) {
            Thread.sleep(DELAY);
        }

        int userIdCauseCount = 0, rebuildCauseCount = 0;

        for (Action action : project.getLastCompletedBuild().getAllActions()) {
            if (action instanceof CauseAction) {
                for(Cause cause : ((CauseAction)action).getCauses()) {
                    if (cause instanceof Cause.UserIdCause) {
                        ++userIdCauseCount;
                    } else if(cause instanceof RebuildCause) {
                        ++rebuildCauseCount;
                    }
                }
            }
        }

        assertEquals("Multiple chained rebuilds should contain a single UserIdCause",
                    1, userIdCauseCount);
        assertEquals("Multiple chained rebuilds should contain a single RebuildCause",
                    1, rebuildCauseCount);
    }


    /**
     * Creates a new freestyle project, builds it and ensures the rebuild action
     * is available on the project level.
     *
     * @throws Exception
     *             Exception
     */
    @Test
    public void testWhenProjectWithoutParamsThenRebuildProjectEnabled()
            throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        project.scheduleBuild2(0);
        RebuildSettings settings = new RebuildSettings(false, false);
        project.addProperty(settings);
        project.save();
        HtmlPage projectPage = j.createWebClient().getPage(project);
        WebAssert.assertLinkPresentWithText(projectPage, "Rebuild Last");

    }

    /**
     * Creates a new freestyle project, builds it and ensures the rebuild action
     * isn't available on the project level.
     *
     * @throws Exception
     *             Exception
     */
    @Test
    public void testWhenProjectWithoutParamsThenRebuildProjectIsDisabled()
            throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        project.scheduleBuild2(0);
        RebuildSettings settings = new RebuildSettings(false, true);
        project.addProperty(settings);
        project.save();
        HtmlPage projectPage = j.createWebClient().getPage(project);
        WebAssert.assertLinkNotPresentWithText(projectPage, "Rebuild Last");

    }

    /**
     * Implementing an Extension always returning isApplicable false.
     */
    public static class ValidatorNeverApplicable extends RebuildValidator {

        @Override
        public boolean isApplicable(AbstractBuild build) {
            return false;
        }
    }

    /**
     * Implementing an Extension always returning isApplicable true.
     */
    public static class ValidatorAlwaysApplicable extends RebuildValidator {

        @Override
        public boolean isApplicable(AbstractBuild build) {
            return true;
        }
    }

    /**
     * Creates a new freestyle project and build with a parameter value whose
     * type is unknown to rebuild plugin. Rebuild and verify that an no
     * exception occurs and page is displayed correctly.
     * 
     * {@link RebuildableParameterValue}.
     *
     * @throws Exception
     *             Exception
     */
    @Test
    public void testRebuildUnsupportedUnknownParameterValue() throws Exception {
        WebClient wc = j.createWebClient();
        FreeStyleProject project = j.createFreeStyleProject();
        project.addProperty(new ParametersDefinitionProperty(
                new UnsupportedUnknownParameterDefinition("param1",
                        "defaultValue")));

        j.assertBuildStatusSuccess(project.scheduleBuild2(0,
                new Cause.RemoteCause("host", "note"),
                new ParametersAction(new UnsupportedUnknownParameterValue(
                        "param1", "value1"))));
        FreeStyleBuild build = project.getLastBuild();
        // it is trying to fallback and use the
        HtmlPage page = wc.getPage(build, "rebuild");
        // Check the hardcoded description is showing properly.
        assertTrue(page.asNormalizedText().contains(
                "Configuration page for UnsupportedUnknownParameterValue"));
    }

    /**
     * Creates a new freestyle project and build with a parameter value whose
     * type is unknown to rebuild plugin. Verify that rebuild succeeds if that
     * parameter value supports {@link RebuildableParameterValue}.
     *
     * @throws Exception
     *             Exception
     */
    @Test
    public void testRebuildSupportedUnknownParameterValue() throws Exception {
        WebClient wc = j.createWebClient();
        FreeStyleProject project = j.createFreeStyleProject();
        project.addProperty(new ParametersDefinitionProperty(
                new SupportedUnknownParameterDefinition("param1",
                        "defaultValue")));

        j.assertBuildStatusSuccess(project
                .scheduleBuild2(0, new Cause.RemoteCause("host", "note"),
                        new ParametersAction(
                                new SupportedUnknownParameterValue("param1",
                                        "value1"))));
        FreeStyleBuild build = project.getLastBuild();
        HtmlPage page = wc.getPage(build, "rebuild");
        assertTrue(page.asNormalizedText(),
                page.asNormalizedText().contains("This is a mark for test"));
    }

    @Test
    public void testRebuildDispatcherExtensionActionIsCopied() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        project.addProperty(new ParametersDefinitionProperty(
                new StringParameterDefinition("name", "defaultValue")));

        // Build (#1)
        project.scheduleBuild2(0, new Cause.RemoteCause("host", "note"),
                new ParametersAction(new StringParameterValue("name", "test")),
                new RebuildDispatcherTestAction(true))
                .get();
        HtmlPage rebuildConfigPage = j.createWebClient().getPage(project,
                "1/rebuild");
        // Rebuild (#2)
        j.submit(rebuildConfigPage.getFormByName("config"));

        j.createWebClient().getPage(project).getAnchorByText("Rebuild Last")
                .click();

        while (project.isBuilding()) {
            Thread.sleep(DELAY);
        }
        boolean hasRebuildDispatcherTestAction = false;
        for (Action action : project.getLastCompletedBuild().getAllActions()) {
            if (action instanceof RebuildDispatcherTestAction) {
                hasRebuildDispatcherTestAction = true;
            }
        }
        assertTrue("Build should have rebuildDispatcherTestAction", hasRebuildDispatcherTestAction);
    }

    @Test
    public void testRebuildDispatcherExtensionActionIsNotCopied() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        project.addProperty(new ParametersDefinitionProperty(
                new StringParameterDefinition("name", "defaultValue")));

        // Build (#1)
        project.scheduleBuild2(0, new Cause.RemoteCause("host", "note"),
                new ParametersAction(new StringParameterValue("name", "test")),
                new RebuildDispatcherTestAction(false))
                .get();
        HtmlPage rebuildConfigPage = j.createWebClient().getPage(project,
                "1/rebuild");
        // Rebuild (#2)
        j.submit(rebuildConfigPage.getFormByName("config"));

        j.createWebClient().getPage(project).getAnchorByText("Rebuild Last")
                .click();

        while (project.isBuilding()) {
            Thread.sleep(DELAY);
        }

        boolean hasRebuildDispatcherTestAction = false;
        for (Action action : project.getLastCompletedBuild().getAllActions()) {
            if (action instanceof RebuildDispatcherTestAction) {
                hasRebuildDispatcherTestAction = true;
            }
        }
        assertFalse("Build should not have rebuildDispatcherTestAction", hasRebuildDispatcherTestAction);
    }

    /**
     * A parameter value rebuild plugin does not know.
     */
    public static class UnsupportedUnknownParameterValue extends
            StringParameterValue {
        private static final long serialVersionUID = 3182218854913929L;

        public UnsupportedUnknownParameterValue(String name, String value) {
            super(name, value);
        }
    }

    public static class UnsupportedUnknownParameterDefinition extends
            StringParameterDefinition {
        private static final long serialVersionUID = 1014662680565914672L;

        @DataBoundConstructor
        public UnsupportedUnknownParameterDefinition(String name,
                String defaultValue) {
            super(name, defaultValue);
        }

        @Override
        public ParameterValue createValue(String value) {
            return new UnsupportedUnknownParameterValue(this.getName(), value);
        }

        @Override
        public StringParameterValue getDefaultParameterValue() {
            return new UnsupportedUnknownParameterValue(this.getName(),
                    this.getDefaultValue());
        }

        @Extension
        public static class DescriptorImpl extends ParameterDescriptor {
            @NonNull
            @Override
            public String getDisplayName() {
                return "UnsupportedUnknownParameterDefinition";
            }
        }

    }

    /**
     * A parameter value rebuild plugin does not know, but supported by
     * {@link TestRebuildParameterProvider}.
     */
    public static class SupportedUnknownParameterValue extends
            StringParameterValue {
        private static final long serialVersionUID = 114922627975966439L;

        public SupportedUnknownParameterValue(String name, String value) {
            super(name, value);
        }
    }

    public static class SupportedUnknownParameterDefinition extends
            StringParameterDefinition {
        private static final long serialVersionUID = 1014662680565914672L;

        @DataBoundConstructor
        public SupportedUnknownParameterDefinition(String name,
                String defaultValue) {
            super(name, defaultValue);
        }

        @Override
        public ParameterValue createValue(String value) {
            return new SupportedUnknownParameterValue(this.getName(), value);
        }

        @Override
        public StringParameterValue getDefaultParameterValue() {
            return new SupportedUnknownParameterValue(this.getName(),
                    this.getDefaultValue());
        }

        @Extension
        public static class DescriptorImpl extends ParameterDescriptor {
            @NonNull
            @Override
            public String getDisplayName() {
                return "SupportedUnknownParameterDefinition";
            }
        }

    }

    /**
     * Provides a view for {@link SupportedUnknownParameterValue} when
     * rebuilding.
     */
    @TestExtension
    public static class TestRebuildParameterProvider extends
            RebuildParameterProvider {
        @Override
        public RebuildParameterPage getRebuildPage(ParameterValue value) {
            if (!(value instanceof SupportedUnknownParameterValue)) {
                return null;
            }
            return new RebuildParameterPage(SupportedUnknownParameterValue.class, "rebuild.groovy");
        }
    }

    public static class RebuildDispatcherTestAction implements Action {
        public final boolean shouldBeCopied;

        public RebuildDispatcherTestAction(boolean shouldBeCopied) {
            this.shouldBeCopied = shouldBeCopied;
        }

        @Override
        public String getIconFileName() {
            return null;
        }
        @Override
        public String getDisplayName() {
            return null;
        }
        @Override
        public String getUrlName() {
            return null;
        }
    }

    @Extension
    public static class RebuildActionDispatcherTestImpl extends RebuildActionDispatcher {
        @Override
        public Set<Action> getPropagatingActions(Run r) {
            Set<Action> dispatcherActions = new HashSet<>();

            for (Action action : r.getAllActions()) {
                if (action instanceof RebuildDispatcherTestAction) {
                    RebuildDispatcherTestAction testAction = (RebuildDispatcherTestAction) action;

                    if (testAction.shouldBeCopied) {
                        dispatcherActions.add(action);
                    }
                }
            }

            return dispatcherActions;
        }
    }
}
