package com.cafeflow.helpers.marketing;

import com.cafeflow.core.base.BaseHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FacebookHelper extends BaseHelper {

    @Override
    protected String getServiceName() {
        return "facebook";
    }

    // TODO: Implement Facebook Graph API integration logic
}
