/*
 *  The MIT License
 *
 *  Copyright 2010 Sony Ericsson Mobile Communications. All rights reserved.
 *  Copyright (c) 2010, Manufacture Francaise des Pneumatiques Michelin, Romain Seguy
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
import hudson.model.AbstractProject;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersAction;
import hudson.model.ParameterValue;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import java.util.LinkedHashSet;
import java.util.Set;

@Extension
public class Rebuilder extends RunListener<Run> {

    /**
     * Contains the set of {@link ParameterDefinition} classes that are used by
     * the build.
     */
    private Set<Class> buildParamDefinitionClasses;

    public Rebuilder() {
        super(Run.class);
        buildParamDefinitionClasses = new LinkedHashSet<Class>();
    }

    /**
     * As there is no way in the Hudson API to link back a {@link ParameterValue}
     * to the {@link ParameterDefinition} which it was created from, we need to
     * analyze the build parameters when the build starts for use in the
     * {@code onCompleted()} method.
     */
    @Override
    public void onStarted(Run r, TaskListener listener) {
        if (r instanceof AbstractBuild) {
            AbstractProject project = ((AbstractBuild) r).getProject();
            ParametersDefinitionProperty paramDefinitionProperty = (ParametersDefinitionProperty) project.getProperty(ParametersDefinitionProperty.class);

            if(paramDefinitionProperty != null) {
                for (ParameterDefinition paramDefinition: paramDefinitionProperty.getParameterDefinitions()) {
                    buildParamDefinitionClasses.add(paramDefinition.getClass());
                }
            }
        }
    }

    @Override
    public void onCompleted(Run r, TaskListener listener) {
        if (r instanceof AbstractBuild) {
            AbstractBuild build = (AbstractBuild) r;
            if (build.getAction(ParametersAction.class) != null) {
                for (Class buildParamDefinitionClass: buildParamDefinitionClasses) {
                    if(RebuildConfig.getInstance().isBlocking(buildParamDefinitionClass.getName())) {
                        return;
                    }
                }

                ParametersAction p = build.getAction(ParametersAction.class);
                EnvVars env = new EnvVars();
                p.buildEnvVars(build, env);
                RebuildAction rebuildAction = new RebuildAction(env);
                build.getActions().add(rebuildAction);
            }
        }
    }

}
