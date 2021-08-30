/*
 *  The MIT License
 *
 *  Copyright 2013 Joel Johnson, Oleg Nenashev and contributors.
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

import hudson.console.ModelHyperlinkNote;
import hudson.model.Cause;
import hudson.model.Run;
import hudson.model.TaskListener;

import jenkins.model.Jenkins;

/**
 * A cause specifying that the build was a rebuild of another build. Extends
 * UpstreamCause so that a lot of the magic that Jenkins does with Upstream
 * builds is inherited (linking, etc).
 *
 * @author Joel Johnson
 * @author Oleg Nenashev
 */
public class RebuildCause extends Cause.UpstreamCause {
    /**
     * RebuildCause constructor.
     * @param  up Run.
     */
    public RebuildCause(Run<?, ?> up) {
        super(up);
    }

    @Override
    public String getShortDescription() {
        return Messages.Cause_RebuildCause_ShortDescription(getUpstreamBuild());
    }
    /**
     * Method returns ShortDescriptionHTML.
     *
     * @return String description.
     */
    public String getShortDescritptionHTML() {
        return Messages.Cause_RebuildCause_ShortDescriptionHTML(getUpstreamBuild(),
                Jenkins.getInstance().getRootUrl() + getUpstreamUrl() + getUpstreamBuild());
    }
    /**
     * Method calculate the indent.
     *
     * @param listener TaskListener.
     * @param depth int.
     */
    private void indent(TaskListener listener, int depth) {
            for (int i = 0; i < depth; i++) {
                listener.getLogger().print(' ');
            }
        }

    @Override
    public void print(TaskListener listener) {
        print(listener, 0);
    }
    /**
     * Method will print the log.
     *
     * @param listener TaskListener.
     * @param depth int.
     */
    private void print(TaskListener listener, int depth) {
        indent(listener, depth);
        listener.getLogger().println(Messages.Cause_RebuildCause_ShortDescription(
               ModelHyperlinkNote.encodeTo('/' + getUpstreamUrl()
               + getUpstreamBuild(), Integer.toString(getUpstreamBuild()))));
    }
}
