package com.myorg;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.constructs.Construct;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.s3.IBucket;

public class LambdaStack extends Stack {
    public LambdaStack(Construct scope, String id) {
        super(scope, id);

//        //shouldn't use Bucket.fromBucketName since we need a new bucket here, rather than referring
//        Bucket deploymentBucket = Bucket.Builder.create(this, "LambdaJarBucket")
//                .bucketName("lambdahelloworldbucket1")
//                .removalPolicy(RemovalPolicy.DESTROY) // only for testing
//                .autoDeleteObjects(true)
//                .build();
        IBucket deploymentBucket = Bucket.fromBucketName(this, "LambdaBucket", "lambdahelloworldbucket1"); //lambdahelloworldbucket1: lambdaBucketName

        Function lambdaFunction = Function.Builder.create(this, "JavaLambdaDemo")
                .runtime(Runtime.JAVA_17)
                .handler("com.example.HelloLambdaDemo::handleRequest") //Java class
                .code(Code.fromBucket(deploymentBucket, "lambda-output/lambda.jar")) //key = the object key of the S3 bucket
                .build();

    }
}

