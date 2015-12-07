package com.sonyericsson.rebuild.matchers;

import hudson.model.StringParameterValue;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public final class StringParameterValueMatcher extends BaseMatcher<StringParameterValue> {

    private final StringParameterValue thisParameterValue;

    StringParameterValueMatcher(StringParameterValue thisParameterValue) {
        this.thisParameterValue = thisParameterValue;
    }

    public static StringParameterValueMatcher equalToStringParamValue(String name, String value) {
        return new StringParameterValueMatcher(new StringParameterValue(name, value));
    }

    @Override
    public boolean matches(Object parameterValue) {
        return parameterValue instanceof StringParameterValue
                && thisParameterValue.getName().equals(((StringParameterValue)parameterValue).getName())
                && thisParameterValue.value.equals(((StringParameterValue)parameterValue).value);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(thisParameterValue.toString());
    }
}
