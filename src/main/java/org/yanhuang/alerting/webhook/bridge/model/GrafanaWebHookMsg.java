package org.yanhuang.alerting.webhook.bridge.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GrafanaWebHookMsg {
	private String title;
	private Long ruleId;
	private String ruleName;
	private String ruleUrl;
	private AlertState state;
	private List<EvalMatch> evalMatches;
	private Long orgId;
	private Long dashboardId;
	private Long panelId;
	private Map<String,String> tags;
	private String message;
	private String imageUrl;

	public enum AlertState{
		no_data,
		paused,
		alerting,
		ok,
		pending,
		unknown;
	}

	@Data
	public static class EvalMatch{
		private Double value;
		private String metric;
		private Map<String, String> tags;

	}
}


/*
example:
 <pre>
 {
	"dashboardId": 1,
	"evalMatches": [{
		"value": 100,
		"metric": "High value",
		"tags": null
	}, {
		"value": 200,
		"metric": "Higher Value",
		"tags": null
	}],
	"message": "Someone is testing the alert notification within Grafana.",
	"orgId": 0,
	"panelId": 1,
	"ruleId": 0,
	"ruleName": "Test notification",
	"ruleUrl": "http://localhost:3000/",
	"state": "alerting",
	"tags": {},
	"title": "[Alerting] Test notification"
}
 </pre>
 */