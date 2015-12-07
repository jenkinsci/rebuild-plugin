package com.sonyericsson.rebuild.matchers;

import hudson.model.ParameterValue;
import org.hamcrest.Description;
import org.junit.internal.matchers.TypeSafeMatcher;

import java.util.Collection;

public final class IsNotCollectionContainingStringParameterValues extends TypeSafeMatcher<Collection<ParameterValue>> {

    private final IsCollectionContainingStringParameterValues delegate;

    public static IsNotCollectionContainingStringParameterValues hasNoStringParamValues(StringParameterValueMatcher... items) {
        return new IsNotCollectionContainingStringParameterValues(items);
    }

    private IsNotCollectionContainingStringParameterValues(StringParameterValueMatcher... items) {
        delegate = new IsCollectionContainingStringParameterValues(items);
    }

    @Override
    public boolean matchesSafely(Collection<ParameterValue> items) {
        return !delegate.matchesSafely(items);
    }

    @Override
    public void describeTo(Description description) {
        delegate.describeTo(description);
    }
}
