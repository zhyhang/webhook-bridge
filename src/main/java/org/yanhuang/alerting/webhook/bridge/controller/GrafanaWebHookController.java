package org.yanhuang.alerting.webhook.bridge.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.yanhuang.alerting.webhook.bridge.model.GrafanaWebHookMsg;
import org.yanhuang.alerting.webhook.bridge.service.wxwork.AlarmRobotService;
import org.yanhuang.alerting.webhook.bridge.utils.JsonCodec;

@RestController
public class GrafanaWebHookController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final AlarmRobotService alarmRobotService;

	@Autowired
	public GrafanaWebHookController(AlarmRobotService alarmRobotService) {
		this.alarmRobotService = alarmRobotService;
	}

	@ResponseStatus(value = HttpStatus.OK)
	@PostMapping("/grafana/wxwork/robot/{group}")
	public void transferToWxWorkRobot(@PathVariable("group") String group, @RequestBody GrafanaWebHookMsg webHookMsg) {
		try {
			if (webHookMsg != null) {
				logger.info("grafana original webhook message: {}, group: {} ", JsonCodec.toString(webHookMsg), group);
				alarmRobotService.grafanaSend(webHookMsg, group);
			} else {
				logger.warn("grafana original webhook message is empty, group: {}", group);
			}
		} catch (Exception e) {
			logger.error("transfer grafana webhook request error", e);
		}
	}
}
