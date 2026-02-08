package com.cafeflow.helpers.office.google;

import com.cafeflow.core.base.BaseHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GDocsHelper extends BaseHelper {

    @Override
    protected String getServiceName() {
        return "google_docs";
    }

    // TODO: Implement Google Docs/Slides/Forms API integration logic
}
