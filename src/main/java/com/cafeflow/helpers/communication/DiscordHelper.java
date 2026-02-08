package com.cafeflow.helpers.communication;

import com.cafeflow.core.base.BaseHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DiscordHelper extends BaseHelper {

    @Override
    protected String getServiceName() {
        return "discord";
    }

    // TODO: Implement Discord webhook/bot integration logic
}
