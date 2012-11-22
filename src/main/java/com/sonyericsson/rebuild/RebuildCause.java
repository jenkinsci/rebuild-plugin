package com.sonyericsson.rebuild;

import hudson.console.ModelHyperlinkNote;
import hudson.model.Cause;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.User;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.Arrays;

/**
* Created with IntelliJ IDEA.
* User: psamoshkin
* Date: 11/22/12
* Time: 1:23 PM
* To change this template use File | Settings | File Templates.
*/
@ExportedBean
public class RebuildCause extends Cause {

    private int originalBuild;
    private String originalUrl;
    private String authenticationName;

    public RebuildCause(Run<?, ?> originalBuild) {
        super();
        this.originalUrl = originalBuild.getParent().getUrl();
        this.originalBuild = originalBuild.getNumber();
        this.authenticationName = Jenkins.getAuthentication().getName();
    }

    @Exported(visibility=3)
    public int getOriginalBuild() {
        return originalBuild;
    }

    @Exported(visibility=3)
    public String getOriginalUrl() {
        return originalUrl;
    }

    @Exported(visibility=3)
    public String getUserName() {
        User u = User.get(authenticationName, false);
        return u != null ? u.getDisplayName() : authenticationName;
    }



    @Override
    public String getShortDescription() {
        return Messages.RebuildCause_ShortDescription(getOriginalBuild(), getUserName());
}

    @Override
    public void print(TaskListener listener) {
        listener.getLogger().println(Messages.RebuildCause_ShortDescription(
                ModelHyperlinkNote.encodeTo('/' + originalUrl + originalBuild, Integer.toString(getOriginalBuild())),
                ModelHyperlinkNote.encodeTo("/user",getUserName())));
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof RebuildCause && Arrays.equals(new Object[]{originalUrl, originalBuild, getUserName()},
                new Object[]{((RebuildCause) o).getOriginalUrl(), ((RebuildCause) o).getOriginalBuild(), ((RebuildCause) o).getUserName()});
    }

    @Override
    public int hashCode() {
        return 295 + (this.getUserName() != null ? this.getUserName().hashCode() : 0)
                + (this.getOriginalUrl() != null ? this.getOriginalUrl().hashCode() : 0)
                + this.getOriginalBuild();
    }
}
