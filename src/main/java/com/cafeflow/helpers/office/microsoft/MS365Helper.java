package com.cafeflow.helpers.office.microsoft;

import com.cafeflow.core.base.BaseHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MS365Helper extends BaseHelper {

    @Override
    protected String getServiceName() {
        return "ms_365";
    }

    // TODO: Implement Microsoft 365 (Office) APIs integration logic
}
