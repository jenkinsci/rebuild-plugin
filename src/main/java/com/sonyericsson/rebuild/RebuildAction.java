/*
 *  The MIT License
 *
 *  Copyright 2010 Sony Ericsson Mobile Communications. All rights reservered.
 *  Copyright 2012 Sony Mobile Communications AB. All rights reservered.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;

import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.matrix.MatrixRun;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BooleanParameterValue;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Hudson;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.PasswordParameterValue;
import hudson.model.RunParameterValue;
import hudson.model.StringParameterValue;
import hudson.model.SimpleParameterDefinition;

/**
 * Rebuild RootAction implementation class. This class will basically reschedule
 * the build with existing parameters.
 *
 * @author Shemeer S;
 */
public class RebuildAction implements Action {

    private static final String SVN_TAG_PARAM_CLASS = "hudson.scm.listtagsparameter.ListSubversionTagsParameterValue";
    /*
    * All the below transient variables are declared only for backward
    * compatibility of the rebuild plugin.
    */
    private transient String rebuildurl = "rebuild";
    private transient String parameters = "rebuildParam";
    private transient String p = "parameter";
    private transient AbstractBuild<?, ?> build;
    private transient ParametersDefinitionProperty pdp;
    private static final String PARAMETERIZED_URL = "parameterized";

    public RebuildAction() {
    }

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
        if (isRebuildAvailable()) {
            return "clock.gif";
        } else {
            return null;
        }
    }

    @Override
    public String getDisplayName() {
        if (isRebuildAvailable()) {
            return "Rebuild";
        } else {
            return null;
        }
    }

    @Override
    public String getUrlName() {
        if (isRebuildAvailable()) {
            return "rebuild";
        } else {
            return null;
        }
    }

    /**
     * Handles the rebuild request and redirects to parameterized
     * and non parameterized build when needed.
     *
     * @param request  StaplerRequest the request.
     * @param response StaplerResponse the response handler.
     * @throws IOException          in case of Stapler issues
     * @throws ServletException     if something unfortunate happens.
     * @throws InterruptedException if something unfortunate happens.
     */
    public void doIndex(StaplerRequest request, StaplerResponse response) throws
            IOException, ServletException, InterruptedException {
        AbstractBuild currentBuild = request.findAncestorObject(AbstractBuild.class);
        if (currentBuild != null) {
            ParametersAction paramAction = currentBuild.getAction(ParametersAction.class);
            if (paramAction != null) {
                response.sendRedirect(PARAMETERIZED_URL);
            } else {
                nonParameterizedRebuild(currentBuild, response);
            }
        }
    }

    /**
     * Call this method while rebuilding
     * non parameterized build.     .
     *
     * @param currentBuild current build.
     * @param response     current response object.
     * @throws ServletException     if something unfortunate happens.
     * @throws IOException          if something unfortunate happens.
     * @throws InterruptedException if something unfortunate happens.
     */
    public void nonParameterizedRebuild(AbstractBuild currentBuild, StaplerResponse
            response) throws ServletException, IOException, InterruptedException {
        getProject().checkPermission(AbstractProject.BUILD);
        Hudson.getInstance().getQueue().schedule(currentBuild.getProject(), 0, null,
                new CauseAction(new Cause.UserIdCause()));
        response.sendRedirect("../../");
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
    public void doConfigSubmit(StaplerRequest req, StaplerResponse rsp) throws
            ServletException, IOException, InterruptedException {
        getProject().checkPermission(AbstractProject.BUILD);
        if (isRebuildAvailable()) {
            if (!req.getMethod().equals("POST")) {
                // show the parameter entry form.
                req.getView(this, "index.jelly").forward(req, rsp);
                return;
            }
            build = req.findAncestorObject(AbstractBuild.class);
            ParametersDefinitionProperty paramDefProp = build.getProject().getProperty(
                    ParametersDefinitionProperty.class);
            List<ParameterValue> values = new ArrayList<ParameterValue>();
            ParametersAction paramAction = build.getAction(ParametersAction.class);
            JSONObject formData = req.getSubmittedForm();
            if (!formData.isEmpty()) {
                JSONArray a = JSONArray.fromObject(formData.get("parameter"));
                for (Object o : a) {
                    JSONObject jo = (JSONObject) o;
                    String name = jo.getString("name");
                    ParameterValue parameterValue = getParameterValue(paramDefProp, name, paramAction, req, jo);
                    if (parameterValue != null) {
                        values.add(parameterValue);
                    }
                }
            }
            Hudson.getInstance().getQueue().schedule(build.getProject(), 0, new ParametersAction(values),
                    new CauseAction(new Cause.UserIdCause()));
            rsp.sendRedirect("../../");
        }
    }

    /**
     * Method for checking whether current build is sub job(MatrixRun) of Matrix
     * build.
     *
     * @return boolean
     */
    public boolean isMatrixRun() {
        StaplerRequest request = Stapler.getCurrentRequest();
        if (request != null) {
            build = request.findAncestorObject(AbstractBuild.class);
            if (build != null && build instanceof MatrixRun) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method for checking,whether the rebuild functionality would be available
     * for build.
     *
     * @return boolean
     */
    public boolean isRebuildAvailable() {
        return getProject() != null && getProject().hasPermission(AbstractProject.BUILD)
                && getProject().isBuildable() && !(getProject().isDisabled()) && !isMatrixRun();
    }

    /**
     * Method for getting the ParameterValue instance from ParameterDefinition
     * or ParamterAction.
     *
     * @param paramDefProp  ParametersDefinitionProperty
     * @param parameterName Name of the Parameter.
     * @param paramAction   ParametersAction
     * @param req           StaplerRequest
     * @param jo            JSONObject
     * @return ParameterValue instance of subclass of ParameterValue
     */
    public ParameterValue getParameterValue(ParametersDefinitionProperty paramDefProp,
                                            String parameterName, ParametersAction paramAction, StaplerRequest req, JSONObject jo) {
        ParameterDefinition paramDef;
        // this is normal case when user try to rebuild a parameterized job.
        if (paramDefProp != null) {
            paramDef = paramDefProp.getParameterDefinition(parameterName);
            if (paramDef != null) {
                // The copy artifact plugin throws an exception when using createValue(req, jo)
                // If the parameter comes from the copy artifact plugin, then use the single argument createValue
                if (jo.toString().contains("BuildSelector") || jo.toString().contains("WorkspaceSelector")){
                    SimpleParameterDefinition parameterDefinition = (SimpleParameterDefinition) paramDefProp.getParameterDefinition(parameterName);
                    return parameterDefinition.createValue(jo.getString("value"));
                }
                return paramDef.createValue(req, jo);
            }
        }
        /*
         * when user try to rebuild a build that was invoked by
         * parameterized trigger plugin in that case ParameterDefinition
         * is null for that parametername that is paased by parameterize
         * trigger plugin,so for handling that scenario, we need to
         * create an instance of that specific ParameterValue with
         * passed parameter value by form.
         *
         * In contrast to all other parameterActions, ListSubversionTagsParameterValue uses "tag" instead of "value"
         */
        if (jo.containsKey("value")) {
            return cloneParameter(paramAction.getParameter(parameterName), jo.getString("value"));
        } else {
            return cloneParameter(paramAction.getParameter(parameterName), jo.getString("tag"));
        }
    }

    /**
     * Method for replacing the old parametervalue with new parameter value
     *
     * @param oldValue ParameterValue
     * @param newValue The value that is submitted by user using form.
     * @return ParameterValue
     */
    private ParameterValue cloneParameter(ParameterValue oldValue, String newValue) {
        if (oldValue instanceof StringParameterValue) {
            return new StringParameterValue(oldValue.getName(), newValue, oldValue.getDescription());
        } else if (oldValue instanceof BooleanParameterValue) {
            return new BooleanParameterValue(oldValue.getName(), Boolean.valueOf(newValue),
                    oldValue.getDescription());
        } else if (oldValue instanceof RunParameterValue) {
            return new RunParameterValue(oldValue.getName(), newValue, oldValue.getDescription());
        } else if (oldValue instanceof PasswordParameterValue) {
            return new PasswordParameterValue(oldValue.getName(), newValue,
                    oldValue.getDescription());
        } else if (oldValue.getClass().getName().equals(SVN_TAG_PARAM_CLASS)) {
            /**
             * getClass().getName() to avoid dependency on svn plugin.
             */
            return new StringParameterValue(oldValue.getName(), newValue, oldValue.getDescription());
        }
        throw new IllegalArgumentException("Unrecognized parameter type: " + oldValue.getClass());
    }
}
