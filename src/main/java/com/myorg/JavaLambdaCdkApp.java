package com.myorg;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import java.util.Arrays;

public class JavaLambdaCdkApp {
    public static void main(final String[] args) {
        App app = new App();

        new PipelineStack(app, "JavaPipelineStack", StackProps.builder() //TODO: check the stack name
                .build());

        app.synth();
    }
}

