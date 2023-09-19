package edu.hniu.imchatroom.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "lux")
public class SysEntityProperties {
    private String address;
    private String chatroomName;
    private Map<String, Map<String, String>> entities;
}
