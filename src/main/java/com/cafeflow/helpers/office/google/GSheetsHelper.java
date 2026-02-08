package com.cafeflow.helpers.office.google;

import com.cafeflow.core.base.BaseHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GSheetsHelper extends BaseHelper {

    @Override
    protected String getServiceName() {
        return "google_sheets";
    }

    // TODO: Implement Google Sheets API integration logic
}
