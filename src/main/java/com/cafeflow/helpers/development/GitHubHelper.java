package com.cafeflow.helpers.development;

import com.cafeflow.core.base.BaseHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GitHubHelper extends BaseHelper {

    @Override
    protected String getServiceName() {
        return "github";
    }

    // TODO: Implement GitHub API integration
}
