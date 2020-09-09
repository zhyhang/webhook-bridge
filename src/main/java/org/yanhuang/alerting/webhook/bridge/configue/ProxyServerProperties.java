package org.yanhuang.alerting.webhook.bridge.configue;

import lombok.Data;

/**
 *代理服务器配置
 */
@Data
public class ProxyServerProperties {
	private String protocol;
	private String host;
	private int port;
	private String userName;
	private String password;
	private String bypassHosts;
}
