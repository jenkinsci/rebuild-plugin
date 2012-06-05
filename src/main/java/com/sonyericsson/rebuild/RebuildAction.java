/*
 *  The MIT License
 *
 *  Copyright 2010 Sony Ericsson Mobile Communications.
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

import hudson.model.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Rebuild RootAction implementation class.
 * This class will basically reschedule the build with
 * existing parameters.
 *
 * @author Shemeer S;
 */
public class RebuildAction implements Action {
    /*
     * All the below transient variables
     * are declared only for backward
     * compatibility of the rebuild plugin.
     * */

    private transient String rebuildurl = "rebuild";
    private transient String parameters = "rebuildParam";
    private transient String p = "parameter";
    private AbstractBuild<?, ?> build;
    private transient ParametersDefinitionProperty pdp;


    /**
     * Getter method for pdp.
     *
     * @return pdp.
     */
    public ParametersDefinitionProperty getPdp() {
        return pdp;
    }

    /**
     * Getter method for build.
     *
     * @return build.
     */
    public AbstractBuild<?, ?> getBuild() {
        return build;
    }

    /**
     * Getter method for p.
     *
     * @return p.
     */
    public String getP() {
        return p;
    }

    /**
     * Getter method for parameters.
     *
     * @return parameters.
     */
    public String getParameters() {
        return parameters;
    }

    /**
     * Getter method for rebuildurl.
     *
     * @return rebuildurl.
     */
    public String getRebuildurl() {
        return rebuildurl;
    }

    /**
     * Method will return current project.
     *
     * @return currentProject.
     */
    public AbstractProject getProject() {

        if (build != null) {
            return build.getProject();
        }

        AbstractProject currentProject = null;
        StaplerRequest request = Stapler.getCurrentRequest();
        if (request != null) {
            currentProject = request.findAncestorObject(AbstractProject.class);
        }
        if (currentProject == null) {
            throw new NullPointerException("Current Project is null");
        }
        return currentProject;
    }

    @Override
    public String getIconFileName() {
        if (getProject().hasPermission(AbstractProject.BUILD)
                && getProject().isBuildable() && !(getProject().isDisabled())) {
            return "clock.gif";
        } else {
            return null;
        }
    }

    @Override
    public String getDisplayName() {
        AbstractProject project = getProject();
        if (project == null) {
            return null;
        }

        if (project.hasPermission(AbstractProject.BUILD)
                && project.isBuildable() && !(project.isDisabled())) {
            return "Rebuild";
        }

        return null;
    }

    @Override
    public String getUrlName() {

        AbstractProject project = null;
        if (build != null) {
            project = build.getProject();
        } else {
            StaplerRequest request = Stapler.getCurrentRequest();
            if (request != null) {
                project = request.findAncestorObject(AbstractProject.class);
            }
        }

        if (project == null) {
            return null;
        }

        if (project.hasPermission(AbstractProject.BUILD)
                && project.isBuildable() && !(project.isDisabled())) {
            return "rebuild";
        }

        return null;
    }

    /**
     * Saves the form to the configuration and disk.
     *
     * @param req StaplerRequest
     * @param rsp StaplerResponse
     * @throws ServletException     if something unfortunate happens.
     * @throws IOException          if something unfortunate happens.
     * @throws InterruptedException if something unfortunate happens.
     */
    public void doConfigSubmit(StaplerRequest req, StaplerResponse rsp)
            throws ServletException, IOException,
            InterruptedException {
        getProject().checkPermission(AbstractProject.BUILD);
        if (getProject().isBuildable() && !(getProject().isDisabled())) {
            if (!req.getMethod().equals("POST")) {
                // show the parameter entry form.
                req.getView(this, "index.jelly").forward(req, rsp);
                return;
            }
            build = req.findAncestorObject(AbstractBuild.class);
            ParametersDefinitionProperty paramDefProp = build.getProject().
                    getProperty(ParametersDefinitionProperty.class);
            List<ParameterValue> values = new ArrayList<ParameterValue>();
            JSONObject formData = req.getSubmittedForm();
            JSONArray a = JSONArray.fromObject(formData.get("parameter"));
            for (Object o : a) {
                JSONObject jo = (JSONObject) o;
                String name = jo.getString("name");
                ParameterValue parameterValue = null;
                if (paramDefProp != null) {
                    ParameterDefinition d = paramDefProp.getParameterDefinition(name);
                    if (d == null) {
                        throw new IllegalArgumentException("No such parameter"
                                + " definition: " + name);
                    }
                    parameterValue = d.createValue(req, jo);
                }
                if (parameterValue != null) {
                    values.add(parameterValue);
                }
            }
            Hudson.getInstance().getQueue().schedule(
                    build.getProject(), 0, new ParametersAction(values),
                    new CauseAction(new Cause.UserCause()));
            rsp.sendRedirect("../../");
        }
    }
}
