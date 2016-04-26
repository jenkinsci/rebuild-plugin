package com.sonyericsson.rebuild.node;

public enum CanBeBuildOnTheSameNodeCheckResult {
    OK,
    NODELABEL_PARAMETER_PLUGIN_USED,
    NODE_NOT_EXISTS, // checked only if NodeLabel Parameter Plugin is not used in the job
    NODE_NOT_MATCHES_RESTRICT_LABEL
}
