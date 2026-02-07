package com.cafeflow.helpers.devops.database;

import com.cafeflow.core.base.BaseHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PostgreSQLHelper extends BaseHelper {

    @Override
    protected String getServiceName() {
        return "postgresql";
    }

    // TODO: Implement PostgreSQL specific helper logic
}
