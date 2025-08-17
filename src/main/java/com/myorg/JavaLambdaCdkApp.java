package com.myorg;

import software.amazon.awscdk.App;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Environment;

public class JavaLambdaCdkApp {
    public static void main(final String[] args) {
        App app = new App();

        String lambdaConnectionArn = getEnvOrThrow("LAMBDA_CONNECTION_ARN");
        String cdkConnectionArn = getEnvOrThrow("CDK_CONNECTION_ARN");

//      get the account and region from cdk env (After running cdk deploy, default account and region will be auto-generated)
        String account = getEnvOrThrow("CDK_DEFAULT_ACCOUNT");
        String region = getEnvOrThrow("CDK_DEFAULT_REGION");

//        create a new environment object
        Environment deployEnv = Environment.builder()
                .account(account)
                .region(region)
                .build();

        new PipelineStack(app, "PipelineStack", StackProps.builder()
                .env(deployEnv) // 将环境信息传入
                .build(),
                lambdaConnectionArn,
                cdkConnectionArn);

        app.synth();
    }

     private static String getEnvOrThrow(String name) {
        String value = System.getenv(name);
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Environment variable '" + name + "' is required but was not found.");
        }
        return value;
    }
}

