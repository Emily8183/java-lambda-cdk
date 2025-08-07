package com.myorg;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.constructs.Construct;
import software.amazon.awscdk.pipelines.*;
import java.util.Map;

public class PipelineStack extends Stack{
    public PipelineStack(Construct scope, String id, StackProps props) {
        super(scope, id, props);

        CodePipeline pipeline = CodePipeline.Builder.create(this, "Pipeline")
                .pipelineName("JavaCdkLambdaPipeline")
                .synth(ShellStep.Builder.create("Synth")
                        .input(CodePipelineSource.connection("Emily8183/java-cdk-app","main",
                                ConnectionSourceOptions.builder()
                                        .connectionArn("arn:tbc") //TODO: 1/ set up env for arn 2/ arn connection
                                        .build()))
                        .additionalInputs(Map.of("lambda", CodePipelineSource.connection(
                                "Emily8183/java-lambda-demo", "main",
                                ConnectionSourceOptions.builder()
                                        .connectionArn("arn:tbc") //TODO
                                        .build())))
                        .commands(java.util.List.of(
                                "cd lambda",
                                "mvn clean package -DskipTests",
                                "aws s3 cp target/java-lambda-demo-1.0-SNAPSHOT.jar s3://lambdahelloworldbucket1/lambda-output/lambda.jar",
                                "cd ../cdk-pipeline",
                                "mvn compile",
                                "cdk synth"))
                        .build())
                .build();

        pipeline.addStage(new LambdaAppStage(this, "DeployLambda"));
    }
}
