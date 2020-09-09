package org.yanhuang.alerting.webhook.bridge.model.wxwork;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * https://work.weixin.qq.com/api/doc/90000/90136/91770#%E6%96%87%E6%9C%AC%E7%B1%BB%E5%9E%8B
 * 最大文本长度：2048，编码方式：utf-8
 */
@Data
public class WxWorkRobotTextMsg implements WxWorkRobotMsg {
	private WxWorkRobotMsgType msgtype = WxWorkRobotMsgType.text;
	private TextBody text;

	@Data
	public static class TextBody {
		private String content;
		@JsonProperty("mentioned_list")
		private List<String> mentionedList;
		@JsonProperty("mentioned_mobile_list")
		private List<String> mentionedMobileList;
	}
}
