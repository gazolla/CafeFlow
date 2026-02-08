package com.cafeflow.helpers.communication;

import com.cafeflow.core.base.BaseHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WhatsAppHelper extends BaseHelper {

    @Override
    protected String getServiceName() {
        return "whatsapp";
    }

    // TODO: Implement WhatsApp API integration logic
}
