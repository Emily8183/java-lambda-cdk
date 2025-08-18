package com.myorg;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.constructs.Construct;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.s3.IBucket;

public class LambdaStack extends Stack {
    public LambdaStack(final Construct scope, final String id, final StackProps props ) {
        super(scope, id);

        //shouldn't use Bucket.fromBucketName since we need a new bucket here, rather than referring
        Bucket lambdaJarBucket = Bucket.Builder.create(this, "LambdaJarBucket")
                .bucketName("lambdahelloworldbucket08171013")
                .removalPolicy(RemovalPolicy.DESTROY) // only for testing
                .autoDeleteObjects(true)
                .build();

//      IBucket deploymentBucket = Bucket.fromBucketName(this, "LambdaBucket", "lambdahelloworldbucket1");

        Function lambdaFunction = Function.Builder.create(this, "JavaLambdaDemo")
                .runtime(Runtime.JAVA_17)
                .handler("com.example.HelloLambdaDemo::handleRequest") //Java class
//                .code(Code.fromAsset("LambdaBuildOutput/lambda.jar")) //key = the object key of the S3 bucket
                .code(Code.fromBucket(lambdaJarBucket, "lambda-output/lambda.jar")) //TODO: why lambda-output?
                .build();

    }
}

