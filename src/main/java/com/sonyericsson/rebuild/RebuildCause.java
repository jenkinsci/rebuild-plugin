package com.sonyericsson.rebuild;

import hudson.model.Cause;
import hudson.model.Run;

/**
 * A cause specifying that the build was a rebuild of another build.
 * Extends UpstreamCause so that a lot of the magic that Jenkins does with Upstream builds is inherited (linking, etc).
 *
 * User: Joel Johnson
 * Date: 1/30/13
 * Time: 2:23 PM
 */
public class RebuildCause extends Cause.UpstreamCause {
	public RebuildCause(Run<?, ?> up) {
		super(up);
	}
}
