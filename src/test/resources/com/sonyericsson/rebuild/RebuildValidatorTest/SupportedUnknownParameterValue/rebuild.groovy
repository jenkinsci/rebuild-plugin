package com.sonyericsson.rebuild.RebuildValidatorTest.SupportedUnknownParameterValue;

f = namespace("lib/form")

// "it" will be overridden inside nexted tags.
String itName = it.name
String itValue = it.value

f.entry(title: it.name, description: it.description) {
    div(name: "parameter") {
        input(type: "hidden", name: "name", value: itName)
        input(type: "text", name: "value", value: itValue)
        text("This is a mark for test")
    }
}

