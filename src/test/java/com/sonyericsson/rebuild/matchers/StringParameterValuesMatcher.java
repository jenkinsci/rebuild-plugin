package com.sonyericsson.rebuild.matchers;

import hudson.model.ParameterValue;
import hudson.model.StringParameterValue;
import org.hamcrest.Description;
import org.junit.internal.matchers.TypeSafeMatcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StringParameterValuesMatcher extends TypeSafeMatcher<Collection<ParameterValue>> {

    private final List<StringParameterValue> thisParameters;

    public static StringParameterValuesMatcher hasStringParamValues(StringParameterValue... parameters) {
        return new StringParameterValuesMatcher(parameters);
    }

    StringParameterValuesMatcher(StringParameterValue... parameters) {
        this.thisParameters = new ArrayList<StringParameterValue>(parameters.length);
        for (StringParameterValue parameter : parameters) {
            thisParameters.add(parameter);
        }
    }

    @Override
    public boolean matchesSafely(Collection<ParameterValue> parameters) {
        Map<StringParameterValue, Boolean> resultMap = new HashMap<StringParameterValue, Boolean>();
        for (StringParameterValue stringParameterValue : thisParameters) {
            resultMap.put(stringParameterValue, false);
            for (ParameterValue parameter : parameters) {
                if (matchesInternal(stringParameterValue, parameter)) {
                    resultMap.put(stringParameterValue, true);
                    break;
                }
            }
        }
        return !resultMap.values().contains(false);
    }

    private boolean matchesInternal(StringParameterValue thisParameter, ParameterValue parameter) {
        return parameter instanceof StringParameterValue
                && thisParameter.getName().equals((parameter).getName())
                && thisParameter.value.equals(((StringParameterValue)parameter).value);
    }

    @Override
    public void describeTo(Description description) {
        description.appendValueList("<[", ", ", "]>", thisParameters);
    }
}
