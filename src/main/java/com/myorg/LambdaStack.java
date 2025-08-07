package com.myorg;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.s3.IBucket;
import software.constructs.Construct;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.lambda.*;

public class LambdaStack extends Stack {
    public LambdaStack(Construct scope, String id) {
        super(scope, id);

        IBucket deploymentBucket = Bucket.fromBucketName(this, "JarBucket", "lambdahelloworldbucket1");
//         .removalPolicy(RemovalPolicy.DESTROY) //only for testing TODO: add them on?
//                .autoDeleteObjects(true)
//                .build();

        Function lambdaFunction = Function.Builder.create(this, "JavaLambdaDemo")
                .runtime(Runtime.JAVA_17)
                .handler("com.example.HelloLambdaDemo::handleRequest") //HelloLambdaDemo: package name?? TODO: correct package name
                .code(Code.fromBucket(deploymentBucket, "lambda-output/lambda.jar"))
                .build();

    }
}

