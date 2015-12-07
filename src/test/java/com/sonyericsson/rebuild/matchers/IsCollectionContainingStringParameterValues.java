package com.sonyericsson.rebuild.matchers;

import hudson.model.ParameterValue;
import org.hamcrest.Description;
import org.junit.internal.matchers.TypeSafeMatcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class IsCollectionContainingStringParameterValues extends TypeSafeMatcher<Collection<ParameterValue>> {

    private final List<StringParameterValueMatcher> matchers;

    public static IsCollectionContainingStringParameterValues hasStringParamValues(StringParameterValueMatcher... items) {
        return new IsCollectionContainingStringParameterValues(items);
    }

    IsCollectionContainingStringParameterValues(StringParameterValueMatcher... items) {
        this.matchers = new ArrayList<StringParameterValueMatcher>(items.length);
        for (StringParameterValueMatcher matcher : items) {
            matchers.add(matcher);
        }
    }

    @Override
    public boolean matchesSafely(Collection<ParameterValue> items) {
        Map<StringParameterValueMatcher, Boolean> resultMap = new HashMap<StringParameterValueMatcher, Boolean>();
        for (StringParameterValueMatcher stringParameterValueMatcher : matchers) {
            resultMap.put(stringParameterValueMatcher, false);
            for (ParameterValue item : items) {
                if (stringParameterValueMatcher.matches(item)) {
                    resultMap.replace(stringParameterValueMatcher, true);
                    break;
                }
            }
        }
        return !resultMap.values().contains(false);
    }

    @Override
    public void describeTo(Description description) {
        description.appendList("<[", ", ", "]>", matchers);
    }
}
