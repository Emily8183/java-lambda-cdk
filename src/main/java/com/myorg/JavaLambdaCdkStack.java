//Updated to LambdaStack
//
// package com.myorg;
//
//import software.amazon.awscdk.services.lambda.Function;
//import software.amazon.awscdk.services.lambda.Runtime;
//import software.amazon.awscdk.services.s3.Bucket;
//import software.amazon.awscdk.services.lambda.Code;
//import software.amazon.awscdk.services.lambda.FunctionProps;
//
//import software.constructs.Construct;
//import software.amazon.awscdk.RemovalPolicy;
//import software.amazon.awscdk.Stack;
//import software.amazon.awscdk.StackProps;
//
//import software.amazon.awscdk.services.codebuild.*;
//import software.amazon.awscdk.services.iam.*;
//
//// import software.amazon.awscdk.Duration;
//// import software.amazon.awscdk.services.sqs.Queue;
//
//import java.util.Map;
//
//public class JavaLambdaCdkStack extends Stack {
//
//    //two constructors, with and without props
//    public JavaLambdaCdkStack(final Construct scope, final String id) {
//        this(scope, id, null);
//    }
//
//    public JavaLambdaCdkStack(final Construct scope, final String id, final StackProps props) {
//        super(scope, id, props);
//
//        Bucket deploymentBucket = Bucket.Builder.create(this, "LambdaJarBucket") //construct ID -> logical ID
//            .bucketName("lambdahelloworldbucket1") //bucket name
//            .removalPolicy(RemovalPolicy.DESTROY) //only for testing
//            .autoDeleteObjects(true)
//            .build();
//
//        Project lambdaBuildProject = Project.Builder.create(this, "LambdaCodeBuild")
//            .projectName("lambda-java-build")
////            .source(Source.gitHub(GitHubSourceProps.builder() <= outdated
//                .source(Source.connection(ConnectionSourceProps.builder()
//                    .connectionArn("arn")
//                        .owner("Emily8183")
//                        .repo("java-lambda-demo")
//                .build()))
//            .environment(BuildEnvironment.builder()
//                .buildImage(LinuxBuildImage.STANDARD_7_0)
//                .computeType(ComputeType.SMALL)
//                .build())
//            .buildSpec(BuildSpec.fromSourceFilename("buildspec.yml"))
//            .build();
//
//        //grant codebuild to upload this lambda to S3
//        deploymentBucket.grantWrite(lambdaBuildProject);
//
//        lambdaBuildProject.getNode().addDependency(deploymentBucket);
//
//        lambdaFunction.getNode().addDependency(lambdaBuildProject);
//
//    }
//}
