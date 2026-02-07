package com.cafeflow.helpers.office.google;

import com.cafeflow.core.base.BaseHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GDriveHelper extends BaseHelper {

    @Override
    protected String getServiceName() {
        return "gdrive";
    }

    // TODO: Implement Google Drive API integration
}
