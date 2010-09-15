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

import hudson.model.AbstractBuild;
import hudson.model.Build;
import hudson.model.Cause;
import hudson.model.ParametersAction;
import hudson.model.Project;
import hudson.model.StringParameterValue;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.jvnet.hudson.test.HudsonTestCase;
import static org.junit.Assert.*;

/**
 * For testing the extension point.
 *
 * @author Gustaf Lundh &lt;gustaf.lundh@sonyericsson.com&gt;
 */
public class RebuildValidatorTest extends HudsonTestCase {
    /**
     * Tests with no extensions.
     *
     * @throws IOException IOException
     * @throws InterruptedException InterruptedException
     * @throws ExecutionException ExecutionException
     */
    public void testNoRebuildValidatorExtension()
            throws IOException, InterruptedException, ExecutionException {
        Project projectA = createFreeStyleProject("testFreeStyleA");
        Build buildA = (Build) projectA.scheduleBuild2(0, new Cause.UserCause(),
                new ParametersAction(new StringParameterValue("party", "megaparty"))).get();
        assertNotNull(buildA.getAction(RebuildAction.class));
    }

    /**
     * Tests with an extension returning isApplicable true.
     *
     * @throws IOException IOException
     * @throws InterruptedException InterruptedException
     * @throws ExecutionException ExecutionException
     */
    public void testRebuildValidatorExtensionIsApplicableTrue()
            throws IOException, InterruptedException, ExecutionException {
        hudson.getExtensionList(RebuildValidator.class).add(0, new ValidatorAlwaysApplicable());
        Project projectA = createFreeStyleProject("testFreeStyleB");
        Build buildA = (Build) projectA.scheduleBuild2(0, new Cause.UserCause(),
                new ParametersAction(new StringParameterValue("party", "megaparty"))).get();
        assertNull(buildA.getAction(RebuildAction.class));
    }

    /**
     * Tests with an extension returning isApplicable false.
     *
     * @throws IOException IOException
     * @throws InterruptedException InterruptedException
     * @throws ExecutionException ExecutionException
     */
    public void testRebuildValidatorExtensionIsApplicableFalse()
            throws IOException, InterruptedException, ExecutionException {
        hudson.getExtensionList(RebuildValidator.class).add(0, new ValidatorNeverApplicable());
        Project projectA = createFreeStyleProject("testFreeStyleC");
        Build buildA = (Build) projectA.scheduleBuild2(0, new Cause.UserCause(),
                new ParametersAction(new StringParameterValue("party", "megaparty"))).get();
        assertNotNull(buildA.getAction(RebuildAction.class));
    }

     /**
     * Tests with two extensions returning isApplicable true AND false.
     *
     * @throws IOException IOException
     * @throws InterruptedException InterruptedException
     * @throws ExecutionException ExecutionException
     */
    public void testRebuildValidatorExtensionIsApplicableTrueFalse()
            throws IOException, InterruptedException, ExecutionException {
        hudson.getExtensionList(RebuildValidator.class).add(0, new ValidatorAlwaysApplicable());
        hudson.getExtensionList(RebuildValidator.class).add(0, new ValidatorNeverApplicable());
        Project projectA = createFreeStyleProject("testFreeStyleC");
        Build buildA = (Build) projectA.scheduleBuild2(0, new Cause.UserCause(),
                new ParametersAction(new StringParameterValue("party", "megaparty"))).get();
        assertNull(buildA.getAction(RebuildAction.class));
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
