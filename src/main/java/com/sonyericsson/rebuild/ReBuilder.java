/*********************************************************************
 *  ____                      _____      _                           *
 * / ___|  ___  _ __  _   _  | ____|_ __(_) ___ ___ ___  ___  _ __   *
 * \___ \ / _ \| '_ \| | | | |  _| | '__| |/ __/ __/ __|/ _ \| '_ \  *
 *  ___) | (_) | | | | |_| | | |___| |  | | (__\__ \__ \ (_) | | | | *
 * |____/ \___/|_| |_|\__, | |_____|_|  |_|\___|___/___/\___/|_| |_| *
 *                    |___/                                          *
 *                                                                   *
 *********************************************************************
 * Copyright 2010 Sony Ericsson Mobile Communications AB.            *
 * All rights, including trade secret rights, reserved.              *
 ********************************************************************/

package com.sonyericsson.rebuild;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Build;
import hudson.model.ParametersAction;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.tasks.Builder;

@Extension
public class ReBuilder extends RunListener<Run> {

    public ReBuilder() {
        super(Run.class);
    }

    @Override
    public void onCompleted(Run r, TaskListener listener) {
        if (r instanceof Build) {
            Build build = (Build) r;
            if (build.getAction(ParametersAction.class) != null) {
                ParametersAction p = build.getAction(ParametersAction.class);
                EnvVars env = new EnvVars();
                p.buildEnvVars(build, env);
                RebuildAction rebuildAction = new RebuildAction(env);
                build.getActions().add(rebuildAction);
            }
        }
    }
}
