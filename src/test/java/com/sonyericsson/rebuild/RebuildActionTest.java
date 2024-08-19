package com.sonyericsson.rebuild;

import hudson.model.BooleanParameterValue;
import hudson.model.Build;
import hudson.model.Cause;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Project;
import hudson.model.StringParameterValue;
import hudson.model.TextParameterValue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

@RunWith(Enclosed.class)
public class RebuildActionTest {

    @RunWith(Parameterized.class)
    public static class RebuildActionRebuildParameterPageTest {
        @Rule
        public JenkinsRule j = new JenkinsRule();
        private Project projectA;
        private Build buildA;
        private RebuildAction rebuildAction;

        private ParameterValue parameterValue;
        private String expectedJellyResourceName;

        public RebuildActionRebuildParameterPageTest(ParameterValue parameterValue, String expectedJellyResourceName) {
            this.parameterValue = parameterValue;
            this.expectedJellyResourceName = expectedJellyResourceName;
        }

        @Before
        public void setUp() throws IOException, ExecutionException, InterruptedException {
            projectA = j.createFreeStyleProject("testFreeStyleA");
            buildA = (Build) projectA.scheduleBuild2(
                    0,
                    new Cause.UserIdCause(),
                    new ParametersAction()).get();
            rebuildAction = buildA.getAction(RebuildAction.class);
        }

        @Test
        public void getRebuildParameterPage() {
            RebuildParameterPage parameterPage = rebuildAction.getRebuildParameterPage(parameterValue);
            assertEquals(expectedJellyResourceName, Optional.ofNullable(parameterPage).map(RebuildParameterPage::getPage).orElse(null));
        }

        @Parameterized.Parameters
        public static Collection<Object[]> getTestParameters() {
            return Arrays.asList(new Object[][]{
                    {new StringParameterValue("stringParam", "stringParam"), "StringParameterValue.jelly"},
                    {new BooleanParameterValue("booleanParam", true), "BooleanParameterValue.jelly"},
                    {new TextParameterValue("textParam", "textParam"), "TextParameterValue.jelly"},
                    {new NotExistsJellyParameterValue("notExistsJellyParam"), null},
                    {new ExistsJellySubParameterValue("existsJellySubParam", "existsJellySubParam"), "StringParameterValue.jelly"}
            });
        }

        private static class NotExistsJellyParameterValue extends ParameterValue {
            public NotExistsJellyParameterValue(String name) {
                super(name);
            }
        }

        private static class ExistsJellySubParameterValue extends StringParameterValue {
            public ExistsJellySubParameterValue(String name, String value) {
                super(name, value);
            }
        }
    }
}
