package com.myorg;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.codebuild.*;
import software.amazon.awscdk.services.codepipeline.*;
import software.amazon.awscdk.services.codepipeline.actions.*;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;
//import software.amazon.awscdk.services.codepipeline.Pipeline;
//import software.amazon.awscdk.services.codepipeline.StageProps;
//import software.amazon.awscdk.services.codepipeline.actions.CodeBuildAction;

public class PipelineStack extends Stack{
    public PipelineStack(final Construct scope, final String id, final StackProps props, final String lambdaConnectionArn, final String cdkConnectionArn) {
        super(scope, id, props);

        Artifact lambdaSourceOutput = new Artifact();
        Artifact cdkSourceOutput = new Artifact();
        Artifact lambdaBuildOutput = new Artifact("LambdaBuildOutput"); //assign the artifact as LambdaBuildOutput

        //Lambda Build Project
        Project lambdaBuildProject = Project.Builder.create(this, "LambdaBuild")
                .projectName("LambdaBuildProject")
                .environment(BuildEnvironment.builder()
                        .buildImage(LinuxBuildImage.STANDARD_7_0) //LinuxBuildImage.STANDARD_7_0: AWS提供的托管镜像
                        .computeType(ComputeType.SMALL)
                        .build())
                .buildSpec(BuildSpec.fromSourceFilename("buildspec.yml"))
                .build();

        //CDK Build Project
        Project cdkBuildProject = Project.Builder.create(this, "CdkBuild")
                .projectName("cdkBuildProject")
                .environment(BuildEnvironment.builder()
                        .buildImage(LinuxBuildImage.STANDARD_7_0)
                        .computeType(ComputeType.SMALL)
                        .build())
                .buildSpec(BuildSpec.fromObject(new java.util.HashMap<String, Object>() {{ //TODO: move this part to buildspec.yml
                    put("version", "0.2");
                    put("phases", new java.util.HashMap<String, Object>() {{
                        put("install", new java.util.HashMap<String, Object>() {{
                            put("commands", List.of("npm install -g aws-cdk"));
                        }});
                        put("build", new java.util.HashMap<String, Object>() {{
                            put("commands", List.of(
                                    "mvn compile",
                                    "cdk deploy --require-approval never"
                            ));
                        }});
                    }});
                }}))
                .build();

        Pipeline pipeline = Pipeline.Builder.create(this, "PipelineJavaTest")
                .pipelineName("JavaLambdaPipeline")
                .stages(List.of(
                        //source stage
                        StageProps.builder()
                                .stageName("Source")
                                .actions(List.of(CodeStarConnectionsSourceAction.Builder.create()
                                        .actionName("Checkout_Lambda")
                                        .owner("Emily8183")
                                        .repo("java-lambda-demo")
                                        .branch("main")
                                        .connectionArn(lambdaConnectionArn) //TODO: add on arn
                                        .output(lambdaSourceOutput) //the new artifact
                                        .build()))
                                .build(),
                        //build stage
                        StageProps.builder()
                                .stageName("BuildAndDeploy")
                                .actions(List.of(CodeBuildAction.Builder.create()
                                        .actionName("BuildAndDeploy_Lambda")
                                        .project(cdkBuildProject)
                                        .input(cdkSourceOutput) //这个input的是source stage所输出的artifact //TODO: double check
                                        .build()))
                                .build()
                ))
                .build();
        }

}
