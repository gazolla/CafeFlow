package com.cafeflow.helpers.office.microsoft;

import com.cafeflow.core.base.BaseHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MSGraphHelper extends BaseHelper {

    @Override
    protected String getServiceName() {
        return "ms_graph";
    }

    // TODO: Implement Microsoft Graph API integration logic
}
