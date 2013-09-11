/*
 *  The MIT License
 *
 *  Copyright 2013 Rino Kadijk.
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

import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * This class holds the configuration values for the rebuild action.
 */
public class RebuildConfiguration implements Describable {
    /**
     * password remember boolean variable.
     */
    private boolean rememberPasswordEnabled;
    /**
     * Constructs a new configuration object.
     *
     * @param rememberPasswordEnabled indicates whether the password field
     * should be pre-filled or empty when rebuilding
     * a job with a password parameter.
     */
    @DataBoundConstructor
    public RebuildConfiguration(boolean rememberPasswordEnabled) {
        this.rememberPasswordEnabled = rememberPasswordEnabled;
    }

    @Override
    public String toString() {
        return "RebuildConfiguration{"
                + "rememberPasswordEnabled=" + rememberPasswordEnabled
                + '}';
    }

    @Override
    public Descriptor getDescriptor() {
        return Hudson.getInstance().getDescriptorOrDie(RebuildConfiguration.class);
    }

    /**
     * True if the password field should be pre-filled.
     *
     * @return true if the password field should be pre-filled.
     */
    public boolean isRememberPasswordEnabled() {
        return rememberPasswordEnabled;
    }
    /**
     * Set the password remember field.
     *
     * @param rememberPasswordEnabled boolean.
     */
    public void setRememberPasswordEnabled(boolean rememberPasswordEnabled) {
        this.rememberPasswordEnabled = rememberPasswordEnabled;
    }
}
