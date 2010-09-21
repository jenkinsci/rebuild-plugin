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

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.FileParameterValue;
import hudson.model.Hudson;
import hudson.model.ParametersAction;
import hudson.model.ParameterValue;
import hudson.model.Run;
import hudson.model.RunParameterValue;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

@Extension
public class Rebuilder extends RunListener<Run> {

    public Rebuilder() {
        super(Run.class);
    }

    @Override
    public void onCompleted(Run r, TaskListener listener) {
        if (r instanceof AbstractBuild) {
            AbstractBuild build = (AbstractBuild) r;
            for (RebuildValidator rebuildValidator : Hudson.getInstance().
                    getExtensionList(RebuildValidator.class)) {
                if (rebuildValidator.isApplicable(build)) {
                    return;
                }
            }
            if (build.getAction(ParametersAction.class) != null) {
                ParametersAction p = build.getAction(ParametersAction.class);
                EnvVars env = new EnvVars();
                p.buildEnvVars(build, env);
                boolean rebuildStatus = true;

                for (ParameterValue parameter: p.getParameters()) {
                    if (parameter instanceof RunParameterValue || parameter instanceof FileParameterValue) {
                        rebuildStatus = false;
                        break;
                    }
                }

                if (rebuildStatus) {
                    RebuildAction rebuildAction = new RebuildAction(env);
                    build.getActions().add(rebuildAction);
                }
            }
        }
    }
}
