/*
 *  The MIT License
 *
 *  Copyright 2010 Sony Ericsson Mobile Communications. All rights reserved.
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


import hudson.Extension;
import hudson.model.Hudson;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

/**
 * Runtime Listner class which allows the user to rebuild the parameterized build.
 *
 * @author Shemeer S.
 */
@Extension
public class Rebuilder extends RunListener<Run> {

    /**
     * Rebuilder class constructor.
     */
    public Rebuilder() {
        super(Run.class);
    }

    @Override
    public void onCompleted(Run build, TaskListener listener) {
            for (RebuildValidator rebuildValidator : Hudson.getInstance().
                    getExtensionList(RebuildValidator.class)) {
                if (rebuildValidator.isApplicable(build)) {
                    return;
                }
            }
            RebuildAction rebuildAction = new RebuildAction();
            // TODO what is the purpose of this? If eligible, RebuildActionFactory would already be adding it anyway (without saving anything to XML).
            build.getActions().add(rebuildAction);
    }

}
