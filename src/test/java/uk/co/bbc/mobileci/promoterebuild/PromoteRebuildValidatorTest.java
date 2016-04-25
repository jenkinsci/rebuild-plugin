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
package uk.co.bbc.mobileci.promoterebuild;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.Extension;
import hudson.model.*;
import junit.framework.Assert;
import org.jvnet.hudson.test.HudsonTestCase;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * For testing the extension point.
 *
 * @author Gustaf Lundh &lt;gustaf.lundh@sonyericsson.com&gt;
 */
public class PromoteRebuildValidatorTest extends HudsonTestCase {
	/**
	 * Sleep delay value.
	 */
	public static final int DELAY = 100;

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
	public void testNoRebuildValidatorExtension() throws IOException,
			InterruptedException, ExecutionException {
		Project projectA = createFreeStyleProject("testFreeStyleA");
		Build buildA = (Build) projectA.scheduleBuild2(
				0,
				new Cause.UserIdCause(),
				new ParametersAction(new StringParameterValue("party",
						"megaparty"))).get();
		assertNotNull(buildA.getAction(PromoteRebuildAction.class));
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
	public void testRebuildValidatorExtensionIsApplicableTrue()
			throws IOException, InterruptedException, ExecutionException {
		hudson.getExtensionList(PromoteRebuildValidator.class).add(0,
				new ValidatorAlwaysApplicable());
		Project projectA = createFreeStyleProject("testFreeStyleB");
		Build buildA = (Build) projectA.scheduleBuild2(
				0,
				new Cause.UserIdCause(),
				new ParametersAction(new StringParameterValue("party",
						"megaparty"))).get();
		assertNull(buildA.getAction(PromoteRebuildAction.class));
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
	public void testRebuildValidatorExtensionIsApplicableFalse()
			throws IOException, InterruptedException, ExecutionException {
		hudson.getExtensionList(PromoteRebuildValidator.class).add(0,
				new ValidatorNeverApplicable());
		Project projectA = createFreeStyleProject("testFreeStyleC");
		Build buildA = (Build) projectA.scheduleBuild2(
				0,
				new Cause.UserIdCause(),
				new ParametersAction(new StringParameterValue("party",
						"megaparty"))).get();
		assertNotNull(buildA.getAction(PromoteRebuildAction.class));
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
	public void testRebuildValidatorExtensionIsApplicableTrueFalse()
			throws IOException, InterruptedException, ExecutionException {
		hudson.getExtensionList(PromoteRebuildValidator.class).add(0,
				new ValidatorAlwaysApplicable());
		hudson.getExtensionList(PromoteRebuildValidator.class).add(0,
				new ValidatorNeverApplicable());
		Project projectA = createFreeStyleProject("testFreeStyleC");
		Build buildA = (Build) projectA.scheduleBuild2(
				0,
				new Cause.UserIdCause(),
				new ParametersAction(new StringParameterValue("party",
						"megaparty"))).get();
		assertNull(buildA.getAction(PromoteRebuildAction.class));
	}



    public void testWhenProjectWithParamsThenRebuildProjectExecutesRebuildOfLastBuildWithoutAskingForInput()
            throws Exception {
        FreeStyleProject project = createFreeStyleProject();
        project.addProperty(new ParametersDefinitionProperty(
                new StringParameterDefinition("name", "defaultValue")));

        // Build (#1)
        FreeStyleBuild freeStyleBuild = project.scheduleBuild2(0, new Cause.UserIdCause(),
                new ParametersAction(new StringParameterValue("name", "test")))
                .get();
        HtmlPage rebuildConfigPage = createWebClient().getPage(project,
                "1/promoterebuild");

        int number = project.getLastCompletedBuild().getNumber();
        Assert.assertEquals("Build number should have incremented", 2, number);
    }


	/**
	 * Creates a new freestyle project and rebuild. Check that the PromoteRebuildAction
	 * has been set to the new build. Check also that a UserIdCause is added.
	 *
	 * @throws Exception
	 *             Exception
	 */
	public void REVISIT_testWhenProjectWithCauseThenCauseIsCopiedAndUserCauseAdded()
			throws Exception {
		FreeStyleProject project = createFreeStyleProject();
		project.addProperty(new ParametersDefinitionProperty(
                new StringParameterDefinition("name", "defaultValue")));

		// Build (#1)
		project.scheduleBuild2(0, new Cause.RemoteCause("host", "note"),
				new ParametersAction(new StringParameterValue("name", "test")))
				.get();

        // Rebuild (#2)
		HtmlPage rebuildConfigPage = createWebClient().getPage(project,
				"1/promoterebuild");

		while (project.isBuilding()) {
			Thread.sleep(DELAY);
		}
		List<Action> actions = project.getLastCompletedBuild().getActions();
		boolean hasPromoteAction = false;
		boolean hasRemoteCause = false;
		boolean hasUserIdCause = false;
		for (Action action : actions) {
			if (action instanceof CauseAction) {
				CauseAction causeAction = (CauseAction) action;
				if (causeAction.getCauses().get(0).getClass()
						.equals(Cause.RemoteCause.class)) {
					hasRemoteCause = true;
				}
				if (causeAction.getCauses().get(0).getClass()
						.equals(Cause.UserIdCause.class)) {
					hasUserIdCause = true;
				}
			} else if(action instanceof PromoteRebuildCauseAction) {
                hasPromoteAction = true;
            }
		}

		assertTrue("Build should have user, remote and promote causes",
                hasPromoteAction && hasRemoteCause && hasUserIdCause);
	}



	/**
	 * Implementing an Extension always returning isApplicable false.
	 */
	public static class ValidatorNeverApplicable extends PromoteRebuildValidator {

		@Override
		public boolean isApplicable(AbstractBuild build) {
			return false;
		}
	}

	/**
	 * Implementing an Extension always returning isApplicable true.
	 */
	public static class ValidatorAlwaysApplicable extends PromoteRebuildValidator {

		@Override
		public boolean isApplicable(AbstractBuild build) {
			return true;
		}
	}

	/**
	 * Creates a new freestyle project and build with a parameter value whose
	 * type is unknown to rebuild plugin. Verify that rebuild succeeds if that
	 * parameter value supports {@link RebuildableParameterValue}.
	 *
	 * @throws Exception
	 *             Exception
	 */
	public void testRebuildSupportedUnknownParameterValue() throws Exception {
		WebClient wc = createWebClient();
		FreeStyleProject project = createFreeStyleProject();
		project.addProperty(new ParametersDefinitionProperty(
				new SupportedUnknownParameterDefinition("param1",
						"defaultValue")));

        //Check the build worked at all, before we promote
		assertBuildStatusSuccess(project
				.scheduleBuild2(0, new Cause.RemoteCause("host", "note"),
						new ParametersAction(
								new SupportedUnknownParameterValue("param1",
										"value1"))));

        //PROMOTE
		FreeStyleBuild build = project.getLastBuild();
		HtmlPage page = wc.getPage(build, "promoterebuild");


        //CHECK IT RAN
        int number = project.getLastCompletedBuild().getNumber();
        Assert.assertEquals("Build number should have incremented", 2, number);

        //Find all the paramters passed in the action and check our paramater is there
        List<Action> actions = project.getLastCompletedBuild().getActions();
        for (Action action : actions) {
            if(action instanceof ParametersAction) {
                ParametersAction parametersAction = (ParametersAction) action;
                int size = parametersAction.getParameters().size();
                assertEquals("Should be paramaters passed but was " + parametersAction.getParameters(), 1, size);

                ParameterValue param1 = parametersAction.getParameter("param1");
                Assert.assertTrue("expecting paramter value to be instanceof SupportedUnknownParameterValue but was " + parametersAction.getParameters(), (param1 instanceof SupportedUnknownParameterValue));
                SupportedUnknownParameterValue supportedUnknownParameterValue = (SupportedUnknownParameterValue) param1;
                Assert.assertEquals("SupportedUnknownParameterDefinition value should be passed","value1",supportedUnknownParameterValue.value);
            }
        }
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
			@Override
			public String getDisplayName() {
				return "SupportedUnknownParameterDefinition";
			}
		}

	}

}
