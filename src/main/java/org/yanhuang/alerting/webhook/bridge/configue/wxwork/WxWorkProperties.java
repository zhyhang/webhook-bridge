package org.yanhuang.alerting.webhook.bridge.configue.wxwork;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.yanhuang.alerting.webhook.bridge.configue.ProxyServerProperties;

import java.util.List;


@Data
@Component
@ConfigurationProperties("app.sending.wxwork")
public class WxWorkProperties {

	private List<AlarmRobotProperties> alarmRobots;
	private ProxyServerProperties proxy;

}
