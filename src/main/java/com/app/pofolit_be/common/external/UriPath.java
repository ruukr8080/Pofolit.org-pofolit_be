package com.app.pofolit_be.common.external;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "exclude.path")
public class UriPath {

    private String[] exception = new String[0];
    private String[] filter = new String[0];
}
