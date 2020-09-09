package org.yanhuang.alerting.webhook.bridge.service.wxwork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yanhuang.alerting.webhook.bridge.configue.wxwork.AlarmRobotProperties;
import org.yanhuang.alerting.webhook.bridge.configue.wxwork.WxWorkProperties;
import org.yanhuang.alerting.webhook.bridge.model.GrafanaWebHookMsg;
import org.yanhuang.alerting.webhook.bridge.model.wxwork.WxWorkRobotMdMsg;
import org.yanhuang.alerting.webhook.bridge.model.wxwork.WxWorkRobotMsg;
import org.yanhuang.alerting.webhook.bridge.model.wxwork.WxWorkRobotTextMsg;
import org.yanhuang.alerting.webhook.bridge.utils.HttpClientWrapper;
import org.yanhuang.alerting.webhook.bridge.utils.JsonCodec;

import java.util.List;
import java.util.Optional;

@Service
public class AlarmRobotService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final WxWorkProperties wxWorkProperties;

	@Autowired
	public AlarmRobotService(WxWorkProperties wxWorkProperties) {
		this.wxWorkProperties = wxWorkProperties;
	}

	/**
	 * 向企业微信机器人通过
	 *
	 * @param webHookMsg 要发送的消息
	 * @param group      收件人组
	 */
	public void grafanaSend(GrafanaWebHookMsg webHookMsg, String group) {
		final List<AlarmRobotProperties> alarmRobots = wxWorkProperties.getAlarmRobots();
		final HttpClientWrapper httpClient = new HttpClientWrapper();
		final WxWorkRobotMdMsg textMsg = buildRobotMdMsg(webHookMsg);
		//先串行处理
		Optional.ofNullable(alarmRobots).orElse(List.of()).stream().filter(r -> filterGroups(group, r))
				.forEach(r -> grafanaSend(textMsg, httpClient, r, group));
	}

	private void grafanaSend(WxWorkRobotMsg textMsg, HttpClientWrapper httpClient, AlarmRobotProperties r,
	                         String group) {
		final long tsb = System.currentTimeMillis();
		try {
			logger.info("send grafana message to wxwork robot begin, message: {}, webhook: {}, group: {}",
					JsonCodec.toString(textMsg), r.getWebhookUrl(), group);
			httpClient.postJson(textMsg, r.getWebhookUrl(), null, wxWorkProperties.getProxy());
			logger.info("send grafana message to wxwork robot end, time: {}, webhook: {}, group: {}",
					System.currentTimeMillis() - tsb, r.getWebhookUrl(), group);
		} catch (Exception e) {
			logger.error("send grafana message to wxwork robot error end, time: {}, webhook: {}, group: {}",
					System.currentTimeMillis() - tsb, r.getWebhookUrl(), group, e);
		}
	}

	private WxWorkRobotMdMsg buildRobotMdMsg(GrafanaWebHookMsg msg) {
		final WxWorkRobotMdMsg mdMsg = new WxWorkRobotMdMsg();
		WxWorkRobotTextMsg.TextBody textBody = new WxWorkRobotTextMsg.TextBody();
		mdMsg.setMarkdown(textBody);
		String fontCss = "<font color=\"comment\">";
		if (msg.getState() == GrafanaWebHookMsg.AlertState.alerting) {
			fontCss = "<font color=\"warning\">";
		} else if (msg.getState() == GrafanaWebHookMsg.AlertState.ok) {
			fontCss = "<font color=\"info\">";
		}
		String content = "## " + fontCss + msg.getTitle() + "</font>\n" +
				"### desc: " + msg.getMessage() + "\n" +
				"### detail: " + JsonCodec.toString(msg.getEvalMatches()) + "\n" +
				"### state: " + msg.getState().name();
		textBody.setContent(content);
		textBody.setMentionedList(List.of("@all"));
		return mdMsg;
	}

	private boolean filterGroups(String group, AlarmRobotProperties r) {
		return r.getGroups() != null &&
				(r.getGroups().contains("all") || r.getGroups().contains(group));
	}

}
