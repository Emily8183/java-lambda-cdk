package com.myorg;

import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.FunctionProps;

import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
// import software.amazon.awscdk.Duration;
// import software.amazon.awscdk.services.sqs.Queue;

import java.util.Map;

public class JavaLambdaCdkStack extends Stack {

    //two constructors, with and without props
    public JavaLambdaCdkStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public JavaLambdaCdkStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        Function lambdaFunction = new Function(this, "JavaLambdaDemo", FunctionProps.builder()
                .runtime(Runtime.JAVA_17)
                .handler("com.example.HelloLambdaDemo::handleRequest")
                .code(Code.fromAsset("lambda-assets/java-lambda-demo-1.0-SNAPSHOT.jar"))
                .functionName("java-lambda-cdk")
                .build()
        );
    }
}
