package com.myorg;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.codebuild.*;
import software.amazon.awscdk.services.codepipeline.*;

import software.amazon.awscdk.services.codepipeline.actions.*;
import software.amazon.awscdk.services.codebuild.BuildEnvironmentVariable;
import software.constructs.Construct;

import software.amazon.awscdk.services.s3.IBucket;
import software.amazon.awscdk.services.s3.Bucket;

import java.util.List;
import java.util.Map;


public class PipelineStack extends Stack{
    public PipelineStack(final Construct scope, final String id, final StackProps props, final String lambdaConnectionArn, final String cdkConnectionArn) {
        super(scope, id, props);

        //output the original versions
        Artifact lambdaSourceOutput = new Artifact("LambdaSourceOutput"); //assign the artifact's name
        Artifact cdkSourceOutput = new Artifact("cdkSourceOutput");

        //output Lambda JAR
        Artifact lambdaBuildOutput = new Artifact("LambdaBuildOutput");

        //Lambda Build Project
        Project lambdaBuildProject = Project.Builder.create(this, "LambdaBuildProject")
                .projectName("LambdaBuildProject")
                .environment(BuildEnvironment.builder()
                        .buildImage(LinuxBuildImage.STANDARD_7_0) //LinuxBuildImage.STANDARD_7_0: AWS提供的托管镜像
                        .computeType(ComputeType.SMALL)
                        .build())
                // .buildSpec(BuildSpec.fromSourceFilename("buildspec.yml")) //error: If the Project's source is NoSource, you need to provide a concrete buildSpec
                .buildSpec(BuildSpec.fromObject(Map.of( //TODO: why can't use BuildSpec.fromSourceFilename？
                        "version", "0.2",
                        "phases", Map.of(
                                "build", Map.of(
                                        "commands", List.of(
                                                "echo Building Lambda with Maven...",
                                                "mvn clean package -DskipTests",
                                                "mkdir -p output",
                                                "cp target/java-lambda-demo-1.0-SNAPSHOT.jar output/lambda.jar",
                                                "aws s3 cp output/lambda.jar s3://lambdahelloworldbucket08171013/lambda-output/lambda.jar"//TODO: can't find s3 bucket if we don't deploy LambdaStack separately
                                        )
                                )
                        ),
                        "artifacts", Map.of(
                                "base-directory", "output",
                                "files", List.of("lambda.jar")
                        )
                )))
                .build();

        IBucket deploymentBucket = Bucket.fromBucketName(this, "LambdaJarBucket", "lambdahelloworldbucket08171013");
        deploymentBucket.grantWrite(lambdaBuildProject);

        //CDK Build and Deploy Project
        Project cdkBuildProject = Project.Builder.create(this, "CdkBuildProject")
                .projectName("cdkBuildDeployProject")
                .environment(BuildEnvironment.builder()
                        .buildImage(LinuxBuildImage.STANDARD_7_0)
                        .computeType(ComputeType.SMALL)
                        // 直接为CodeBuild项目配置环境变量, 安全地存储在CloudFormation模板和CodeBuild服务中
                        .environmentVariables(Map.of(
                                        "LAMBDA_CONNECTION_ARN", BuildEnvironmentVariable.builder()
                                                .value(lambdaConnectionArn) // 使用从构造函数(JavaLambdaCdkApp)传入的值
                                                .build(),
                                        "CDK_CONNECTION_ARN", BuildEnvironmentVariable.builder()
                                                .value(cdkConnectionArn) // 使用从构造函数传入的值
                                                .build()
                        ))
                        .build())
                .buildSpec(BuildSpec.fromObject(Map.of( //TODO: move this part to buildspec.yml
                    "version", "0.2",
                    "phases", Map.of(
                            "install", Map.of(
                                    "runtime-versions", Map.of("java", "corretto17"), // 明确Java版本
                                    "commands", List.of(
                                            "npm install -g aws-cdk"
                                    )
                            ),
                            "build", Map.of(
                                    "commands", List.of( //interact with LambdaBuildOutput
//                                            "echo '--- Verifying file structure ---'",
//                                            "pwd",      // Prints the current directory (should be /codebuild/src/...)
//                                            "ls -lR",   // Lists all files and folders so you can see the structure
//                                            "echo '--- Verification complete ---'",

                                            "mvn package -DskipTests", //构建java cdk项目，"mvn compile"也可以。如果只是“mvn package", 会生成完整的target/
                                            "cdk deploy --require-approval never" //deploy cdk to AWS, must add "require-approval never" //TODO: check details
                                    )
                            )
                    )
                )))
                .build();

        Pipeline.Builder.create(this, "JavaLambdaPipelineDemo")
                .pipelineName("JavaLambdaPipelineDemo")
                .stages(List.of(
                        //Stage1: source stage (to get the two repos)
                        StageProps.builder()
                                .stageName("Source")
                                .actions(List.of(
                                        //this part is to get the lambda origin code from Lambda repo
                                        CodeStarConnectionsSourceAction.Builder.create()
                                            .actionName("Checkout_Lambda")
                                            .owner("Emily8183")
                                            .repo("java-lambda-demo")
                                            .branch("main")
                                            .connectionArn(lambdaConnectionArn) //TODO: to replace
                                            .output(lambdaSourceOutput)
                                            .build(),
                                        //this part is to get cdk_infra code from this repo
                                        CodeStarConnectionsSourceAction.Builder.create()
                                            .actionName("Checkout_CDK")
                                            .owner("Emily8183")
                                            .repo("java-lambda-cdk")
                                            .branch("main")
                                            .connectionArn(cdkConnectionArn) //TODO: to replace
                                            .output(cdkSourceOutput)
                                            .build()
                                ))
                                .build(),

                        //Stage2: build stage
                        StageProps.builder()
                                .stageName("Build")
                                .actions(List.of(
                                        CodeBuildAction.Builder.create()
                                            .actionName("Build_Lambda") //build only, deploy in the next step
                                            .project(lambdaBuildProject)
                                            .input(lambdaSourceOutput) //这个input的是source stage所输出的artifact,即Lambda源码
                                                .outputs(List.of(lambdaBuildOutput)) // 输出是构建好的JAR包
                                            .build()
                                ))
                                .build(),
                        
                        //Stage 3: deploy cdk
                        StageProps.builder()
                                .stageName("Deploy")
                                .actions(List.of(
                                        CodeBuildAction.Builder.create()
                                                .actionName("Deploy_CDK_Stack")
                                                .project(cdkBuildProject)
                                                // 需要两个输入: CDK源码和Lambda的JAR包, 解决方案：a primary input => .input(), and an extra input => .extraInputs()
                                              .input(cdkSourceOutput) //each action can only have one primary input
                                                .extraInputs(List.of(lambdaBuildOutput))
                                                .build()
                                ))
                                .build()
                ))
                .build();
        }

}
