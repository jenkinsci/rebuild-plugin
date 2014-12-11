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

import hudson.ExtensionPoint;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.Run;

import java.io.Serializable;

/**
 * Extension point for allowing disabling of the rebuild-action, in case
 * other plug-ins provides similar functionality.
 *
 * @author Gustaf Lundh &lt;gustaf.lundh@sonyericsson.com&gt;
 */
public abstract class RebuildValidator implements Serializable, ExtensionPoint {
    /**
     * Method for acknowledge that another plug-ins wants handle the Rebuild functionality itself.
     *
     * @param build Build to use when verifying applicability
     * @return true if the plug-in provides its own rebuild functionality. E.g. disable the rebuild action.
     */
    public /*abstract*/ boolean isApplicable(Run build) {
        if (Util.isOverridden(RebuildValidator.class, getClass(), "isApplicable", AbstractBuild.class) && build instanceof AbstractBuild) {
            return isApplicable((AbstractBuild) build);
        } else {
            throw new AbstractMethodError("you must override the new overload of isApplicable");
        }
    }

    @Deprecated
    public boolean isApplicable(AbstractBuild build) {
        return isApplicable((Run) build);
    }
}
