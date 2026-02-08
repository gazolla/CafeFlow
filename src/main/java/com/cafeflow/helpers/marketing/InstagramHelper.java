package com.cafeflow.helpers.marketing;

import com.cafeflow.core.base.BaseHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InstagramHelper extends BaseHelper {

    @Override
    protected String getServiceName() {
        return "instagram";
    }

    // TODO: Implement Instagram Basic Display/Graph API integration logic
}
