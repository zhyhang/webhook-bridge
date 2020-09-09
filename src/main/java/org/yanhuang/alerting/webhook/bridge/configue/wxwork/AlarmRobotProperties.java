package org.yanhuang.alerting.webhook.bridge.configue.wxwork;

import lombok.Data;

import java.util.Set;

@Data
public class AlarmRobotProperties {

	private Set<String> groups = Set.of("all");

	private String webhookUrl;

	private String msgTemplate;

}
