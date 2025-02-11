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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.Action;

import jakarta.servlet.ServletException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hudson.model.BooleanParameterValue;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Queue;
import hudson.model.Run;
import hudson.model.SimpleParameterDefinition;
import hudson.model.ParameterDefinition;
import hudson.model.PasswordParameterValue;
import hudson.model.RunParameterValue;
import hudson.model.StringParameterValue;
import jenkins.model.Jenkins;
import jenkins.model.RunAction2;
import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;
import org.kohsuke.stapler.interceptor.RequirePOST;

/**
 * Rebuild RootAction implementation class. This class will basically reschedule
 * the build with existing parameters.
 *
 * @author Shemeer S;
 */
public class RebuildAction extends AbstractRebuildAction implements RunAction2 {

    private static final String SVN_TAG_PARAM_CLASS = "hudson.scm.listtagsparameter.ListSubversionTagsParameterValue";
    private /* quasi-final */ Run<?, ?> run;
    /*
     * All the below transient variables are declared only for backward
     * compatibility of the rebuild plugin.
     */
    private static final String PARAMETERIZED_URL = "parameterized";
    /**
     * Rebuild Descriptor.
     */
    @Extension
    public static final RebuildDescriptor DESCRIPTOR = new RebuildDescriptor();
    /**
     * RebuildAction constructor.
     *
     * @param run The run.
     */
    public RebuildAction(Run<?, ?> run) {
        this.run = run;
    }

    /**
     * True if the password fields should be pre-filled.
     *
     * @return True if the password fields should be pre-filled.
     */
    public boolean isRememberPasswordEnabled() {
        return DESCRIPTOR.getRebuildConfiguration().isRememberPasswordEnabled();
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
        return "rebuild";
    }

    @Override
    public Run<?, ?> getRun() {
        return run;
    }

    /**
     * Decouple {@link #getUrlName()} from the actual URL, otherwise we cannot customize the target.
     * @return
     */
    @Override
    public String getTaskUrl() {
        if (isRebuildAvailable()) {
            if (!isRequiresPOST()) {
                return "rebuild/parameterized";
            }
            return "rebuild/"; // trailing / needed to prevent redirect to 405
        } else {
            return null;
        }
    }

    @Override
    public Job<?, ?> getProject() {
        return run.getParent();
    }

    public boolean isRequiresPOST() {
        if (run != null) {
            ParametersAction paramAction = run.getAction(ParametersAction.class);
            if (paramAction != null) {
                RebuildSettings settings = getProject().getProperty(RebuildSettings.class);
                return settings != null && settings.getAutoRebuild();
            }
        }
        return true;
    }

    /**
     * Handles the rebuild request and redirects to parameterized
     * and non parameterized build when needed.
     *
     * @param request  StaplerRequest2 the request.
     * @param response StaplerResponse2 the response handler.
     * @throws java.io.IOException          in case of Stapler issues
     */
    @RequirePOST
    public void doIndex(StaplerRequest2 request, StaplerResponse2 response) throws IOException {
        if (run != null) {
            ParametersAction paramAction = run.getAction(ParametersAction.class);
            if (paramAction != null) {
                RebuildSettings settings = getProject().getProperty(RebuildSettings.class);
                if (settings != null && settings.getAutoRebuild() || request.getParameter("autorebuild") != null) {
                    parameterizedRebuild(run, response);
                } else {
                    response.sendRedirect(PARAMETERIZED_URL);
                }
            } else {
                nonParameterizedRebuild(run, response);
            }
        }
    }
    /**
     * Handles the rebuild request with parameter.
     *
     * @param currentBuild the build.
     * @param response StaplerResponse2 the response handler.
     * @throws IOException          in case of Stapler issues
     */
    public void parameterizedRebuild(Run currentBuild, StaplerResponse2 response) throws IOException {
        Job project = getProject();
        project.checkPermission(Item.BUILD);
        if (isRebuildAvailable()) {

            List<Action> actions = constructRebuildActions(run, currentBuild.getAction(ParametersAction.class));

            Jenkins.get().getQueue().schedule2((Queue.Task)run.getParent(), 0, actions);
            response.sendRedirect("../../");
        }
    }
    /**
     * Call this method while rebuilding
     * non parameterized build.     .
     *
     * @param currentBuild current build.
     * @param response     current response object.
     * @throws IOException          if something unfortunate happens.
     */
    public void nonParameterizedRebuild(Run currentBuild, StaplerResponse2
            response) throws IOException {
        getProject().checkPermission(Item.BUILD);

        List<Action> actions = constructRebuildActions(run, null);
        Jenkins.get().getQueue().schedule2((Queue.Task)currentBuild.getParent(), 0, actions);
        response.sendRedirect("../../");
    }

    /**
     * Saves the form to the configuration and disk.
     *
     * @param req StaplerRequest2
     * @param rsp StaplerResponse2
     * @throws ServletException     if something unfortunate happens.
     * @throws IOException          if something unfortunate happens.
     */
    public void doConfigSubmit(StaplerRequest2 req, StaplerResponse2 rsp) throws ServletException, IOException {
        Job project = getProject();
        project.checkPermission(Item.BUILD);
        if (isRebuildAvailable()) {
            if (!req.getMethod().equals("POST")) {
                // show the parameter entry form.
                req.getView(this, "index.jelly").forward(req, rsp);
                return;
            }
            ParametersDefinitionProperty paramDefProp = run.getParent().getProperty(
                    ParametersDefinitionProperty.class);
            List<ParameterValue> values = new ArrayList<>();
            ParametersAction paramAction = run.getAction(ParametersAction.class);
            JSONObject formData = req.getSubmittedForm();
            if (!formData.isEmpty()) {
                JSONArray a = JSONArray.fromObject(formData.get("parameter"));
                for (Object o : a) {
                    if (o instanceof JSONNull) {
                        continue;
                    }
                    JSONObject jo = (JSONObject)o;
                    String name = jo.getString("name");
                    try{
                        ParameterValue parameterValue = getParameterValue(paramDefProp, name, paramAction, req, jo);
                        if (parameterValue != null) {
                            values.add(parameterValue);
                        }
                    }catch(IllegalArgumentException e){
                        rsp.sendRedirect(PARAMETERIZED_URL + "?invalidParam=" + name);
                        return;
                    }
                }
            }
            for (ParameterValue source : paramAction.getParameters()) {
                boolean alreadyAdded = false;
                for (ParameterValue dest : values) {
                    if (source.getName().equals(dest.getName())) {
                        alreadyAdded = true;
                        break;
                    }
                }
                if (!alreadyAdded) {
                    values.add(source);
                }
            }

            List<Action> actions = constructRebuildActions(run, new ParametersAction(values));
            Jenkins.get().getQueue().schedule2((Queue.Task)run.getParent(), 0, actions);

            rsp.sendRedirect("../../");
        }
    }

    /**
     * Extracts the build causes and adds or replaces the {@link hudson.model.Cause.UserIdCause}. The result is a
     * list of all build causes from the original build (might be an empty list), plus a
     * {@link hudson.model.Cause.UserIdCause} for the user who started the rebuild, plus a
     * {@link RebuildCause} for this rebuild.
     *
     * @param fromBuild the build to copy the causes from.
     * @return list with all original causes and a {@link hudson.model.Cause.UserIdCause}.
     */
    private List<Cause> constructRebuildCauses(Run<?, ?> fromBuild) {
        List<Cause> currentBuildCauses = new ArrayList<>(fromBuild.getCauses());

        List<Cause> newBuildCauses = new ArrayList<>();
        for (Cause buildCause : currentBuildCauses) {
            if (!(buildCause instanceof Cause.UserIdCause) && !(buildCause instanceof RebuildCause)) {
                newBuildCauses.add(buildCause);
            }
        }

        newBuildCauses.add(new Cause.UserIdCause());
        newBuildCauses.add(new RebuildCause(fromBuild));

        return newBuildCauses;
    }

    /**
     * Loops over all the RebuildActionDispatchers and adds any actions to the rebuild that they want included.
     * Always copies the {@link hudson.model.ParametersAction} if it is present.
     *
     * @param fromBuild the build to copy the actions from
     * @param actions the list to append additional copied actions
     */
    private void copyRebuildDispatcherActions(Run<?, ?> fromBuild, List<Action> actions) {
        Set<Action> propagatingActions = new HashSet<>();

        // Get all RebuildActionsDispatchers that implement our extension point
        ExtensionList<RebuildActionDispatcher> rebuildActionDispatchers = RebuildActionDispatcher.all();

        for (RebuildActionDispatcher dispatcher : rebuildActionDispatchers) {
            propagatingActions.addAll(dispatcher.getPropagatingActions(fromBuild));
        }

        actions.addAll(propagatingActions);
    }

    /**
     * Method for getting the ParameterValue instance from ParameterDefinition
     * or ParamterAction.
     *
     * @param paramDefProp  ParametersDefinitionProperty
     * @param parameterName Name of the Parameter.
     * @param paramAction   ParametersAction
     * @param req           StaplerRequest2
     * @param jo            JSONObject
     * @return ParameterValue instance of subclass of ParameterValue
     */
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE") // see https://github.com/spotbugs/spotbugs/issues/651
    public ParameterValue getParameterValue(ParametersDefinitionProperty paramDefProp,
            String parameterName, ParametersAction paramAction, StaplerRequest2 req, JSONObject jo) {
        ParameterDefinition paramDef;
        // this is normal case when user try to rebuild a parameterized job.
        if (paramDefProp != null) {
            paramDef = paramDefProp.getParameterDefinition(parameterName);
            if (paramDef != null) {
                // The copy artifact plugin throws an exception when using createValue(req, jo)
                // If the parameter comes from the copy artifact plugin, then use the single argument createValue
                if (jo.toString().contains("BuildSelector") || jo.toString().contains("WorkspaceSelector")) {
                    SimpleParameterDefinition parameterDefinition =
                            (SimpleParameterDefinition)paramDefProp.getParameterDefinition(parameterName);
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
            return new BooleanParameterValue(oldValue.getName(), Boolean.parseBoolean(newValue),
                    oldValue.getDescription());
        } else if (oldValue instanceof RunParameterValue) {
            return new RunParameterValue(oldValue.getName(), newValue, oldValue.getDescription());
        } else if (oldValue instanceof PasswordParameterValue) {
            return new PasswordParameterValue(oldValue.getName(), newValue,
                    oldValue.getDescription());
        } else if (oldValue.getClass().getName().equals(SVN_TAG_PARAM_CLASS)) {
            /*
             * getClass().getName() to avoid dependency on svn plugin.
             */
            return new StringParameterValue(oldValue.getName(), newValue, oldValue.getDescription());
        }
        throw new IllegalArgumentException("Unrecognized parameter type: " + oldValue.getClass());
    }
    /**
     * Method for constructing Rebuild actions.
     *
     * @param up AbstractBuild
     * @param paramAction ParametersAction.
     * @return actions List<Action>
     */
    private List<Action> constructRebuildActions(Run up, ParametersAction paramAction) {
        List<Cause> causes = constructRebuildCauses(up);
        List<Action> actions = new ArrayList<>();
        actions.add(new CauseAction(causes));
        copyRebuildDispatcherActions(up, actions);
        if (paramAction != null) {
            actions.add(paramAction);
        }
        return actions;
    }

    /**
     * @param value the parameter value to show to rebuild.
     * @return page for the parameter value, or null if no suitable option found.
     */
    public RebuildParameterPage getRebuildParameterPage(ParameterValue value) {
        for (RebuildParameterProvider provider: RebuildParameterProvider.all()) {
            RebuildParameterPage page = provider.getRebuildPage(value);
            if (page != null) {
                return page;
            }
        }

        // Check if we have a branched Jelly in the plugin.
        if (getClass()
                        .getResource(String.format(
                                "/%s/%s.jelly",
                                getClass().getCanonicalName().replace('.', '/'),
                                value.getClass().getSimpleName()))
                != null) {
            // No provider available, use an existing view provided by rebuild plugin.
            return new RebuildParameterPage(
                    getClass(),
                    String.format("%s.jelly", value.getClass().getSimpleName())
                    );

        }
        // Else we return that we haven't found anything.
        // So Jelly fallback could occur.
        return null;
    }

    @Override
    public void onAttached(Run<?, ?> r) {
        this.run = r;
    }

    @Override
    public void onLoad(Run<?, ?> r) {
        this.run = r;
    }
}
