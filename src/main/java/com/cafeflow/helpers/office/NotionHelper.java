package com.cafeflow.helpers.office;

import com.cafeflow.core.base.BaseHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotionHelper extends BaseHelper {

    @Override
    protected String getServiceName() {
        return "notion";
    }

    // TODO: Implement Notion API integration logic
}
