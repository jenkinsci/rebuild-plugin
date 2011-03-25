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

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BooleanParameterValue;
import hudson.model.Cause;
import hudson.model.Cause.UpstreamCause;
import hudson.model.CauseAction;
import hudson.model.FileParameterValue;
import hudson.model.Hudson;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.PasswordParameterValue;
import hudson.model.RunParameterValue;
import hudson.model.StringParameterValue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Rebuild RootAction implementation class.
 * This class will basically reschedule the build with
 * existing parameters.
 *
 * @author Shemeer S;
 */
public class RebuildAction implements Action {

    @Override
    public String getIconFileName() {
        return "clock.gif";
    }

    @Override
    public String getDisplayName() {
        return "Rebuild";
    }

    @Override
    public String getUrlName() {
        return "rebuild";
    }

    /**
     * Saves the form to the configuration and disk.
     * @param req StaplerRequest
     * @param rsp StaplerResponse
     * @throws ServletException if something unfortunate happens.
     * @throws IOException if something unfortunate happens.
     * @throws InterruptedException if something unfortunate happens.
     */
    public void doConfigSubmit(StaplerRequest req, StaplerResponse rsp)
            throws ServletException, IOException,
            InterruptedException {
        if (!req.getMethod().equals("POST")) {
            // show the parameter entry form.
            req.getView(this, "index.jelly").forward(req, rsp);
            return;
        }
        AbstractBuild<?, ?> build = req.findAncestorObject(AbstractBuild.class);
        ParametersDefinitionProperty pdp = build.getProject().
                getProperty(ParametersDefinitionProperty.class);
        ParametersAction paramAction = build.getAction(ParametersAction.class);
        List<ParameterValue> values = new ArrayList<ParameterValue>();
        JSONObject formData = req.getSubmittedForm();
        JSONArray a = JSONArray.fromObject(formData.get("parameter"));
        for (Object o : a) {
            JSONObject jo = (JSONObject) o;
            String name = jo.getString("name");
            String value = jo.getString("value");
            ParameterValue originalValue = paramAction.getParameter(name);
            ParameterValue parameterValue = null;

            // we special-case file parameters because they can not be passed build-to-build.
            if (originalValue instanceof FileParameterValue) {
                if (pdp != null) {
                    ParameterDefinition d = pdp.getParameterDefinition(name);
                    if (d == null) {
                        throw new IllegalArgumentException("No such parameter definition: " + name);
                    }
                    parameterValue = d.createValue(req, jo);
                }
            } else {
                parameterValue = cloneParameter(originalValue, value);
            }
            if (parameterValue != null) {
                values.add(parameterValue);
            }
        }
        Hudson.getInstance().getQueue().schedule(
                build.getProject(), 0, new ParametersAction(values), new CauseAction(new Cause.UserCause()));
        rsp.sendRedirect("../../");
    }

    private ParameterValue cloneParameter(ParameterValue oldValue, String newValue) {
        if (oldValue instanceof StringParameterValue) {
            return new StringParameterValue(oldValue.getName(), newValue, oldValue.getDescription());
        } else if (oldValue instanceof BooleanParameterValue) {
            return new BooleanParameterValue(oldValue.getName(), Boolean.valueOf(newValue), oldValue.getDescription());
        } else if (oldValue instanceof RunParameterValue) {
            return new RunParameterValue(oldValue.getName(), newValue, oldValue.getDescription());
        } else if (oldValue instanceof PasswordParameterValue) {
            return new PasswordParameterValue(oldValue.getName(), newValue, oldValue.getDescription());
        }
        throw new IllegalArgumentException("Unrecognized parameter type: " + oldValue.getClass());
    }
}
