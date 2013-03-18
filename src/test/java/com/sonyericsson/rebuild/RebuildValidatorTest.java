/*
 *  The MIT License
 *
 *  Copyright 2013 Sony Ericsson Mobile Communications. All rights reserved.
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
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Build;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersAction;
import hudson.model.Project;
import hudson.model.StringParameterValue;
import org.jvnet.hudson.test.HudsonTestCase;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * For testing the extension point.
 *
 * @author Gustaf Lundh &lt;gustaf.lundh@sonyericsson.com&gt;
 */
public class RebuildValidatorTest extends HudsonTestCase {

    /**
     * Tests with no extensions.
     *
     * @throws IOException          IOException
     * @throws InterruptedException InterruptedException
     * @throws ExecutionException   ExecutionException
     */
    public void testNoRebuildValidatorExtension()
            throws IOException, InterruptedException, ExecutionException {
        Project projectA = createFreeStyleProject("testFreeStyleA");
        Build buildA = (Build) projectA.scheduleBuild2(0, new Cause.UserIdCause(),
                new ParametersAction(new StringParameterValue("party",
                        "megaparty")))
                .get();
        assertNotNull(buildA.getAction(RebuildAction.class));
    }

    /**
     * Tests with an extension returning isApplicable true.
     *
     * @throws IOException          IOException
     * @throws InterruptedException InterruptedException
     * @throws ExecutionException   ExecutionException
     */
    public void testRebuildValidatorExtensionIsApplicableTrue()
            throws IOException, InterruptedException, ExecutionException {
        hudson.getExtensionList(RebuildValidator.class).add(0, new ValidatorAlwaysApplicable());
        Project projectA = createFreeStyleProject("testFreeStyleB");
        Build buildA = (Build) projectA.scheduleBuild2(0, new Cause.UserIdCause(),
                new ParametersAction(new StringParameterValue("party",
                        "megaparty")))
                .get();
        assertNull(buildA.getAction(RebuildAction.class));
    }

    /**
     * Tests with an extension returning isApplicable false.
     *
     * @throws IOException          IOException
     * @throws InterruptedException InterruptedException
     * @throws ExecutionException   ExecutionException
     */
    public void testRebuildValidatorExtensionIsApplicableFalse()
            throws IOException, InterruptedException, ExecutionException {
        hudson.getExtensionList(RebuildValidator.class).add(0, new ValidatorNeverApplicable());
        Project projectA = createFreeStyleProject("testFreeStyleC");
        Build buildA = (Build) projectA.scheduleBuild2(0, new Cause.UserIdCause(),
                new ParametersAction(new StringParameterValue("party",
                        "megaparty")))
                .get();
        assertNotNull(buildA.getAction(RebuildAction.class));
    }

    /**
     * Tests with two extensions returning isApplicable true AND false.
     *
     * @throws IOException          IOException
     * @throws InterruptedException InterruptedException
     * @throws ExecutionException   ExecutionException
     */
    public void testRebuildValidatorExtensionIsApplicableTrueFalse()
            throws IOException, InterruptedException, ExecutionException {
        hudson.getExtensionList(RebuildValidator.class).add(0, new ValidatorAlwaysApplicable());
        hudson.getExtensionList(RebuildValidator.class).add(0, new ValidatorNeverApplicable());
        Project projectA = createFreeStyleProject("testFreeStyleC");
        Build buildA = (Build) projectA.scheduleBuild2(0, new Cause.UserIdCause(),
                new ParametersAction(new StringParameterValue("party",
                        "megaparty")))
                .get();
        assertNull(buildA.getAction(RebuildAction.class));
    }

    /**
     * Creates a new freestyle project and checks if the rebuild action is available on the project level.
     *
     * @throws Exception Exception
     */
    public void testWhenProjectWithoutParamsThenRebuildProjectAvailable()
            throws Exception {
        FreeStyleProject project = createFreeStyleProject();

        FreeStyleBuild build = project.scheduleBuild2(0).get();

        RebuildLastCompletedBuildAction action = build.getProject().getAction(RebuildLastCompletedBuildAction.class);
        assertNotNull(action);
    }

    /**
     * Creates a new freestyle project and builds the project with a string parameter.
     * If the build is succesful, a rebuild of the last build is done.
     * The rebuild on the project level should point to the last build
     *
     * @throws Exception Exception
     */
    public void testWhenProjectWithParamsThenRebuildProjectExecutesRebuildOfLastBuild()
            throws Exception {
        FreeStyleProject project = createFreeStyleProject();

        // Build (#1)
        project.scheduleBuild2(0, new Cause.UserIdCause(), new ParametersAction(
                new StringParameterValue("name", "test"))).get();
        HtmlPage rebuildConfigPage = createWebClient().getPage(project, "1/rebuild");
        // Rebuild (#2)
        submit(rebuildConfigPage.getFormByName("config"));

        HtmlPage projectPage = createWebClient().getPage(project);
        WebAssert.assertLinkPresentWithText(projectPage, "Rebuild Last");

        HtmlAnchor rebuildHref = projectPage.getAnchorByText("Rebuild Last");
        assertEquals("Rebuild Last should point to the second build", rebuildHref.getHrefAttribute(),
                "/" + project.getUrl() + "2/rebuild");
    }

    /**
     * Creates a new freestyle project and rebuild. Check that the causes of the original build are copied
     * to the new build. Check also that a UserIdCause is added.
     *
     * @throws Exception Exception
     */
    public void testWhenProjectWithCauseThenCauseIsCopiedAndUserCauseAdded()
            throws Exception {
        FreeStyleProject project = createFreeStyleProject();

        // Build (#1)
        project.scheduleBuild2(0, new Cause.RemoteCause("host", "note"), new ParametersAction(
                new StringParameterValue("name", "test"))).get();
        HtmlPage rebuildConfigPage = createWebClient().getPage(project, "1/rebuild");
        // Rebuild (#2)
        submit(rebuildConfigPage.getFormByName("config"));

        createWebClient().getPage(project).getAnchorByText("Rebuild Last").click();

        while (project.isBuilding()) {
            Thread.sleep(100);
        }
        List<Action> actions = project.getLastCompletedBuild().getActions();
        boolean hasUserCause = false;
        boolean hasRemoteCause = false;
        for (Action action : actions) {
            if (action instanceof CauseAction) {
                CauseAction causeAction = (CauseAction) action;
                if (causeAction.getCauses().get(0).getClass().equals(Cause.UserIdCause.class)) {
                    hasUserCause = true;
                }
                if (causeAction.getCauses().get(0).getClass().equals(Cause.RemoteCause.class)) {
                    hasRemoteCause = true;
                }
            }
        }
        assertTrue("Build should have user cause and remote cause", hasUserCause && hasRemoteCause);
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
}
