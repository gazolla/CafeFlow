package com.cafeflow.helpers.communication;

import com.cafeflow.core.base.BaseHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SlackHelper extends BaseHelper {

    @Override
    protected String getServiceName() {
        return "slack";
    }

    // TODO: Implement Slack notification logic
}
