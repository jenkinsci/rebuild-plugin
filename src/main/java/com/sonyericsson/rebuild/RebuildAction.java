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

import com.sonyericsson.rebuild.node.CanBeBuildOnTheSameNodeCheckResult;
import com.sonyericsson.rebuild.node.NodeLabelParameterValue;
import hudson.Extension;
import hudson.matrix.MatrixRun;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BooleanParameterValue;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.PasswordParameterValue;
import hudson.model.Queue;
import hudson.model.Run;
import hudson.model.RunParameterValue;
import hudson.model.SimpleParameterDefinition;
import hudson.model.StringParameterValue;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.sonyericsson.rebuild.node.CanBeBuildOnTheSameNodeCheckResult.NODELABEL_PARAMETER_PLUGIN_USED;
import static com.sonyericsson.rebuild.node.CanBeBuildOnTheSameNodeCheckResult.NODE_NOT_EXISTS;
import static com.sonyericsson.rebuild.node.CanBeBuildOnTheSameNodeCheckResult.NODE_NOT_MATCHES_RESTRICT_LABEL;
import static com.sonyericsson.rebuild.node.CanBeBuildOnTheSameNodeCheckResult.OK;

/**
 * Rebuild RootAction implementation class. This class will basically reschedule
 * the build with existing parameters.
 *
 * @author Shemeer S;
 */
public class RebuildAction implements Action {

    public static final String REBUILD_ON_THE_SAME_NODE_PARAMETER_NAME = "com.sonyericsson.rebuild.rebuildOnTheSameNode";
    private static final String DEFAULT_NODE_NAME = "master";

    // names of the parameter values from NodeLabel Parameter Plugin
    private static final String CLASS_LABEL_PARAMETER_VALUE = "org.jvnet.jenkins.plugins.nodelabelparameter.LabelParameterValue";
    private static final String CLASS_NODE_PARAMETER_VALUE = "org.jvnet.jenkins.plugins.nodelabelparameter.NodeParameterValue";

    private static final String SVN_TAG_PARAM_CLASS = "hudson.scm.listtagsparameter.ListSubversionTagsParameterValue";
    /*
     * All the below transient variables are declared only for backward
     * compatibility of the rebuild plugin.
     */
    private transient String rebuildurl = "rebuild";
    private transient String parameters = "rebuildParam";
    private transient String p = "parameter";
    private transient Run<?, ?> build;
    private transient ParametersDefinitionProperty pdp;
    private static final String PARAMETERIZED_URL = "parameterized";
    /**
     * Rebuild Descriptor.
     */
    @Extension
    public static final RebuildDescriptor DESCRIPTOR = new RebuildDescriptor();
    /**
     * RebuildAction constructor.
     */
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
    public Run<?, ?> getBuild() {
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
     * True if the password fields should be pre-filled.
     *
     * @return True if the password fields should be pre-filled.
     */
    public boolean isRememberPasswordEnabled() {
        return DESCRIPTOR.getRebuildConfiguration().isRememberPasswordEnabled();
    }

    /**
     * Method will return current project.
     *
     * @return currentProject.
     */
    public Job getProject() {
        if (build != null) {
            return build.getParent();
        }

        Job currentProject = null;
        StaplerRequest request = Stapler.getCurrentRequest();
        if (request != null) {
            currentProject = request.findAncestorObject(Job.class);
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
    @SuppressWarnings("unused") // "Rebuild" button
    public void doIndex(StaplerRequest request, StaplerResponse response) throws IOException, ServletException, InterruptedException {
        Run currentBuild = request.findAncestorObject(Run.class);
        if (currentBuild != null) {
            RebuildSettings settings = (RebuildSettings)getProject().getProperty(RebuildSettings.class);
            if (settings == null || !settings.getAutoRebuild()) {
                // show parameterized.jelly page (even if the job is not parameterized):
                // need to specify if the rebuild should be executed at the same node
                response.sendRedirect(PARAMETERIZED_URL);
            } else { // autoRebuild enabled
                boolean rebuildOnTheSameNode = settings.isSameNodeWhenAuto() && (canBeBuiltOnTheSameNode() == OK);
                // rebuild w/o showing parameterized.jelly page
                if (currentBuild.getAction(ParametersAction.class) != null) {
                    parameterizedRebuild(currentBuild, response, rebuildOnTheSameNode);
                } else {
                    nonParameterizedRebuild(currentBuild, response, rebuildOnTheSameNode);
                }
            }
        }
    }

    /**
     * Handles the rebuild request with parameter.
     *
     * @param currentBuild the build.
     * @param response StaplerResponse the response handler.
     * @param rebuildOnTheSameNode whether the rebuild should be executed on the same node that the prev build
     * @throws IOException          in case of Stapler issues
     */
    public void parameterizedRebuild(Run<?, ?> currentBuild, StaplerResponse response, boolean rebuildOnTheSameNode) throws IOException {
        Job project = getProject();
        if (project == null) {
            return;
        }
        project.checkPermission(Item.BUILD);
        if (isRebuildAvailable()) {
            List<Action> actions = copyBuildCausesAndAddUserCause(currentBuild);
            ParametersAction action = currentBuild.getAction(ParametersAction.class);
            List<ParameterValue> params = new ArrayList<ParameterValue>();
            if (action != null) {
                params.addAll(action.getParameters());
            }
            removeNodeLabelParameter(params);
            if (rebuildOnTheSameNode && !isNodeLabelParameterPluginUsed(action)) {
                NodeLabelParameterValue paramValue = createNodeLabelParamValueForPrevNode();
                params.add(paramValue);
            }
            if (!params.isEmpty()) {
                action = new ParametersAction(params); // getParameters() is unmodifiable so create new ParametersAction
            }
            if (action != null) {
                actions.add(action);
            }

            Hudson.getInstance().getQueue().schedule((Queue.Task) build.getParent(), 0, actions);
            response.sendRedirect("../../");
        }
    }

    private void removeNodeLabelParameter(List<ParameterValue> params) {
        for (Iterator<ParameterValue> iterator = params.iterator(); iterator.hasNext(); ) {
            ParameterValue param = iterator.next();
            if (param instanceof NodeLabelParameterValue) {
                iterator.remove();
            }
        }
    }

    private boolean isNodeLabelParameterPluginUsed(ParametersAction paramAction) {
        if (paramAction == null) {
            return false;
        }
        for (ParameterValue parameterValue : paramAction.getParameters()) {
            String clazz = parameterValue.getClass().getName();
            if (clazz.equals(CLASS_LABEL_PARAMETER_VALUE) || clazz.equals(CLASS_NODE_PARAMETER_VALUE)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Call this method while rebuilding
     * non parameterized build.     .
     *
     * @param currentBuild current build.
     * @param response     current response object.
     * @param rebuildOnTheSameNode whether the rebuild should be executed on the same node that the prev build
     * @throws ServletException     if something unfortunate happens.
     * @throws IOException          if something unfortunate happens.
     * @throws InterruptedException if something unfortunate happens.
     */
    public void nonParameterizedRebuild(Run currentBuild, StaplerResponse response, boolean rebuildOnTheSameNode)
            throws ServletException, IOException, InterruptedException {
        getProject().checkPermission(Item.BUILD);
        ParametersAction action = null;
        if (rebuildOnTheSameNode) {
            NodeLabelParameterValue paramValue = createNodeLabelParamValueForPrevNode();
            action = new ParametersAction(Collections.<ParameterValue>singletonList(paramValue));
        }
        List<Action> actions = constructRebuildCause(build, action);
        Hudson.getInstance().getQueue().schedule((Queue.Task) currentBuild.getParent(), 0, actions);
        response.sendRedirect("../../");
    }

    private NodeLabelParameterValue createNodeLabelParamValueForPrevNode() {
        return new NodeLabelParameterValue(getPrevBuildNodeName()); // nodeName used as a label
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
    @SuppressWarnings("unused") // "Rebuild" button
    public void doConfigSubmit(StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException, InterruptedException {
        Job project = getProject();
        if (project == null) {
            return;
        }
        project.checkPermission(Item.BUILD);
        if (isRebuildAvailable()) {
            if (!req.getMethod().equals("POST")) {
                // show the parameter entry form.
                req.getView(this, "index.jelly").forward(req, rsp);
                return;
            }
            build = req.findAncestorObject(Run.class);
            ParametersDefinitionProperty paramDefProp = build.getParent().getProperty(
                    ParametersDefinitionProperty.class);
            List<ParameterValue> values = new ArrayList<ParameterValue>();
            ParametersAction paramAction = build.getAction(ParametersAction.class);
            JSONObject formData = req.getSubmittedForm();
            if (!formData.isEmpty()) {
                JSONArray a = JSONArray.fromObject(formData.get("parameter"));
                for (Object o : a) {
                    JSONObject jo = (JSONObject)o;
                    String name = jo.getString("name");
                    ParameterValue parameterValue = getParameterValue(paramDefProp, name, paramAction, req, jo);
                    if (parameterValue != null) {
                        values.add(parameterValue);
                    }
                }
            }

            List<Action> actions = constructRebuildCause(build, new ParametersAction(values));
            Hudson.getInstance().getQueue().schedule((Queue.Task) build.getParent(), 0, actions);

            rsp.sendRedirect("../../");
        }
    }

    /**
     * Extracts the build causes and adds or replaces the {@link hudson.model.Cause.UserIdCause}. The result is a
     * list of all build causes from the original build (might be an empty list), plus a
     * {@link hudson.model.Cause.UserIdCause} for the user who started the rebuild.
     *
     * @param fromBuild the build to copy the causes from.
     * @return list with all original causes and a {@link hudson.model.Cause.UserIdCause}.
     */
    private List<Action> copyBuildCausesAndAddUserCause(Run<?, ?> fromBuild) {
        List<Cause> currentBuildCauses = fromBuild.getCauses();

        List<Action> actions = new ArrayList<Action>(currentBuildCauses.size());
        boolean hasUserCause = false;
        for (Cause buildCause : currentBuildCauses) {
            if (buildCause instanceof Cause.UserIdCause) {
                hasUserCause = true;
                actions.add(new CauseAction(new Cause.UserIdCause()));
            } else {
                actions.add(new CauseAction(buildCause));
            }
        }
        if (!hasUserCause) {
            actions.add(new CauseAction(new Cause.UserIdCause()));
        }

        return actions;
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
            build = request.findAncestorObject(Run.class);
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
        Job project = getProject();
        return project != null
                && project.hasPermission(Item.BUILD)
                && project.isBuildable()
                && project instanceof Queue.Task
                && !isMatrixRun() 
                && !isRebuildDisbaled();

    }

    private boolean isRebuildDisbaled() {
        RebuildSettings settings = (RebuildSettings)getProject().getProperty(RebuildSettings.class);
        
        if (settings != null && settings.getRebuildDisabled()) {
			return true;
		}
		return false;
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
        // handle 'rebuild on the same node' parameter
        if (REBUILD_ON_THE_SAME_NODE_PARAMETER_NAME.equals(parameterName)) {
            if ("true".equals(jo.getString("value"))) {
                return createNodeLabelParamValueForPrevNode();
            } else {
                return null; // do nothing
            }
        }

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

    /**
     * Method for constructing Rebuild cause.
     *
     * @param up AbstractBuild
     * @param paramAction ParametersAction.
     * @return actions List<Action>
     */
    private List<Action> constructRebuildCause(Run<?,?> up, ParametersAction paramAction) {
        List<Action> actions = copyBuildCausesAndAddUserCause(up);
        actions.add(new CauseAction(new RebuildCause(up)));
        if (paramAction != null) {
            actions.add(paramAction);
        }
        return actions;
    }


    // The following methods are called from parameterized.jelly page

    /**
     * @param value the parameter value to show to rebuild.
     * @return page for the parameter value, or null if no suitable option found.
     */
    @SuppressWarnings("unused") // used from parameterized.jelly
    public RebuildParameterPage getRebuildParameterPage(ParameterValue value) {
        for (RebuildParameterProvider provider: RebuildParameterProvider.all()) {
            RebuildParameterPage page = provider.getRebuildPage(value);
            if (page != null) {
                return page;
            }
        }

        // Check if we have a branched Jelly in the plugin.
        if (getClass().getResource(String.format("/%s/%s.jelly", getClass().getCanonicalName().replace('.', '/'), value.getClass().getSimpleName())) != null) {
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

    /**
     * Checks if the ParameterValue is an instance of {@link NodeLabelParameterValue}. Used to define whether the
     * parameter value should have it's input element on the parameterized.jelly page.
     */
    @SuppressWarnings("unused") // used from parameterized.jelly
    public boolean isNodeLabelParameterValue(ParameterValue parameterValue) {
        return parameterValue instanceof NodeLabelParameterValue;
    }

    /**
     * Checks if the rebuild can be executed on the same node that the original build.
     * This method is used to define whether 'Build on the same node' checkbox should be shown.
     */
    @SuppressWarnings("unused") // used from parameterized.jelly
    public CanBeBuildOnTheSameNodeCheckResult canBeBuiltOnTheSameNode() {
        AbstractBuild<?, ?> ab = (AbstractBuild<?, ?>) build;
        // check if whether the job uses NodeLabel Parameter Plugin
        if (isNodeLabelParameterPluginUsed(build.getAction(ParametersAction.class))) {
            // if NodeLabel Parameter Plugin is used 'Rebuild on the same node' checkbox is not shown
            return NODELABEL_PARAMETER_PLUGIN_USED;
        }

        Node previousNode = ab.getBuiltOn();
        if (previousNode == null) {
            return NODE_NOT_EXISTS;
        }

        AbstractProject<?, ?> project = ab.getProject();
        Label assignedLabel = project.getAssignedLabel();
        if (assignedLabel == null) {
            return OK; // job can be built on any node
        }

        if (assignedLabel.matches(previousNode)) {
            return OK;
        } else {
            return NODE_NOT_MATCHES_RESTRICT_LABEL;
        }
    }

    @SuppressWarnings("unused") // used from parameterized.jelly
    public boolean shouldShowRebuildOnTheSameNodeCheckbox(CanBeBuildOnTheSameNodeCheckResult res) {
        return res == OK;
    }

    @SuppressWarnings("unused") // used from parameterized.jelly
    public boolean shouldShowPrevNodeNotExistsWarning(CanBeBuildOnTheSameNodeCheckResult res) {
        return res == NODE_NOT_EXISTS;
    }

    @SuppressWarnings("unused") // used from parameterized.jelly
    public boolean shouldShowNodeLabelParameterPluginUsedWarning(CanBeBuildOnTheSameNodeCheckResult res) {
        return res == NODELABEL_PARAMETER_PLUGIN_USED;
    }

    public String getPrevBuildNodeName() {
        String nodeName = ((AbstractBuild<?, ?>) build).getBuiltOnStr();
        if (StringUtils.isEmpty(nodeName)) {
            // was built on master
            nodeName = DEFAULT_NODE_NAME;
        }
        return nodeName;
    }

    @SuppressWarnings("unused") // used from parameterized.jelly
    public String getPrevBuildNodeUrl() {
        return ((AbstractBuild<?, ?>) build).getBuiltOn().getSearchUrl();
    }
}
