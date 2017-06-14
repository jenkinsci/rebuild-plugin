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
package uk.co.bbc.mobileci.promoterebuild;

import hudson.matrix.MatrixRun;
import hudson.model.*;
import hudson.plugins.git.RevisionParameterAction;
import jenkins.model.ParameterizedJobMixIn;
import net.sf.json.JSONObject;
import org.eclipse.jgit.transport.URIish;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Rebuild RootAction implementation class. This class will basically reschedule
 * the build with existing parameters.
 *
 * @author Shemeer S;
 */
public class PromoteRebuildAction implements Action {

    private static final String SVN_TAG_PARAM_CLASS = "hudson.scm.listtagsparameter.ListSubversionTagsParameterValue";
    /*
     * All the below transient variables are declared only for backward
     * compatibility of the rebuild plugin.
     */
    private transient String rebuildurl = "promoterebuild";
    private transient String parameters = "promoterebuildParam";
    private transient String p = "parameter";
    private transient Run<?, ?> build;
    private transient ParametersDefinitionProperty pdp;

    /**
     * RebuildAction constructor.
     */
    public PromoteRebuildAction() {
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
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        if (isRebuildAvailable()) {
            return "promoterebuild";
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
    public void doIndex(StaplerRequest request, StaplerResponse response) throws IOException, ServletException, InterruptedException {
        Run currentBuild = request.findAncestorObject(Run.class);
        if (currentBuild != null) {
            ParametersAction paramAction = currentBuild.getAction(ParametersAction.class);
            if (paramAction != null) {
                    parameterizedRebuild(currentBuild, response);
            } else {
                nonParameterizedRebuild(currentBuild, response);
            }
        }
    }
    /**
     * Handles the rebuild request with parameter.
     *
     * @param currentBuild the build.
     * @param response StaplerResponse the response handler.
     * @throws IOException          in case of Stapler issues
     */
    public void parameterizedRebuild(final Run currentBuild, StaplerResponse response) throws IOException {
        Job project = getProject();
        if (project == null) {
            return;
        }
        project.checkPermission(Item.BUILD);
        if (isRebuildAvailable()) {

            ParametersAction action = currentBuild.getAction(ParametersAction.class);
            List<Action> actions = constructRebuildCause(build, action);

            new ParameterizedJobMixIn() {
                @Override protected Job asJob() {
                    return currentBuild.getParent();
                }
            }.scheduleBuild2(0, actions.toArray(new Action[actions.size()]));
            response.sendRedirect("../../");
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
    public void nonParameterizedRebuild(final Run currentBuild, StaplerResponse
            response) throws ServletException, IOException, InterruptedException {
        getProject().checkPermission(Item.BUILD);

        List<Action> actions = constructRebuildCause(build, null);

        new ParameterizedJobMixIn() {
            @Override protected Job asJob() {
                return currentBuild.getParent();
            }
        }.scheduleBuild2(0, actions.toArray(new Action[actions.size()]));

        response.sendRedirect("../../");
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
        List currentBuildCauses = fromBuild.getCauses();

        List<Action> actions = new ArrayList<Action>(currentBuildCauses.size());
        boolean hasUserCause = false;
        for (Object buildCause : currentBuildCauses) {
            if (buildCause instanceof Cause.UserIdCause) {
                hasUserCause = true;
                actions.add(new CauseAction(new Cause.UserIdCause()));
            } else {
                actions.add(new CauseAction((Cause)buildCause));
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
                && !isMatrixRun() ;

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
     * @param up AbsstractBuild
     * @param paramAction ParametersAction.
     * @return actions List<Action>
     */
    private List<Action> constructRebuildCause(Run up, ParametersAction paramAction) {
        List<Action> actions = copyBuildCausesAndAddUserCause(up);

        PromoteRebuildCauseAction promoteRebuildCauseAction = new PromoteRebuildCauseAction(up);
        actions.add(promoteRebuildCauseAction);
        if (paramAction != null) {
            actions.add(paramAction);
        }

        String buildHash = promoteRebuildCauseAction.getPromoteRebuildCause().getBuildHash();
        URIish buildRemote = null;
        try {
            buildRemote = new URIish(promoteRebuildCauseAction.getPromoteRebuildCause().getBuildRemote());
        } catch (URISyntaxException ignored) {
        }
        RevisionParameterAction revisionParameterAction = new RevisionParameterAction(buildHash, buildRemote);
        actions.add(revisionParameterAction);

        return actions;
    }

}
