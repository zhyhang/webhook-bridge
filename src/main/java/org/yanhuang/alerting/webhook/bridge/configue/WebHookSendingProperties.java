package org.yanhuang.alerting.webhook.bridge.configue;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.yanhuang.alerting.webhook.bridge.configue.wxwork.WxWorkProperties;

@Data
public class WebHookSendingProperties {
	private WxWorkProperties wxwork;
}
