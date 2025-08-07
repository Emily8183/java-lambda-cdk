package com.myorg;

import software.amazon.awscdk.Stage;
import software.amazon.awscdk.StageProps;
import software.constructs.Construct;

public class LambdaAppStage extends Stage {
    public LambdaAppStage(final Construct scope, final String id) {
        super(scope, id);

        new LambdaStack(this, "LambdaStack");
    }
}

