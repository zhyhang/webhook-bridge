package org.yanhuang.alerting.webhook.bridge.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

@Slf4j
public class HttpClientConnectCloseTest {

	private CloseableHttpClient httpClient;

	private String url = "https://cn.bing.com/search?q=jdk+15+release+date&qs=SC&pq=jdk15+rel&sc=1-9&cvid" +
			"=E46F28FE4C5E49439F86386240FE3178&FORM=QBRE&sp=1";

	@Before
	public void setup() {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(10);
		cm.setDefaultMaxPerRoute(2);
		// Connection timeout is the timeout until a connection with the server is established.
		// ConnectionRequestTimeout used when requesting a connection from the connection manager.
		httpClient = HttpClients.custom().setConnectionManager(cm).build();
	}

	@After
	public void clear() {
		try {
			httpClient.close();
		} catch (IOException e) {
			//ignore
		}
	}

	/**
	 * connection will be released and returned to pool
	 * <pre>
	 * 20:07:09.282 [main] DEBUG org.apache.http.impl.conn.PoolingHttpClientConnectionManager - Connection [id: 0][route: {s}->https://cn.bing.com:443] can be kept alive indefinitely
	 * 20:07:09.282 [main] DEBUG org.apache.http.impl.conn.DefaultManagedHttpClientConnection - http-outgoing-0: set socket timeout to 0
	 * 20:07:09.282 [main] DEBUG org.apache.http.impl.conn.PoolingHttpClientConnectionManager - Connection released: [id: 0][route: {s}->https://cn.bing.com:443][total available: 1; route allocated: 1 of 2; total allocated: 1 of 10]
	 * </pre>
	 **/
	@Test
	public void testRightClose() {
		HttpGet httpGet = new HttpGet(url);
		try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
			final String responseText = EntityUtils.toString(response.getEntity());
			log.info("response text: {}", responseText == null || responseText.length() <= 64 ? responseText :
					responseText.substring(0, 64));
		} catch (Exception e) {
			log.error("request error", e);
		}
		log.info("-------before http client close--------------------------------------------------");
	}


	/**
	 * connection will be discarded
	 * <pre>
	 * 20:16:20.215 [main] DEBUG org.apache.http.impl.conn.DefaultManagedHttpClientConnection - http-outgoing-0: Close connection
	 * 20:16:20.217 [main] DEBUG org.apache.http.impl.execchain.MainClientExec - Connection discarded
	 * 20:16:20.217 [main] DEBUG org.apache.http.impl.conn.PoolingHttpClientConnectionManager - Connection released: [id: 0][route: {s}->https://cn.bing.com:443][total available: 0; route allocated: 0 of 2; total allocated: 0 of 10]	 * </pre>
	 **/
	@Test
	public void testEntityUnclose() {
		HttpGet httpGet = new HttpGet(url);
		try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
			final String responseText = response.getStatusLine().toString();
			log.info("response text: {}", responseText == null || responseText.length() <= 64 ? responseText :
					responseText.substring(0, 64));
		} catch (Exception e) {
			log.error("request error", e);
		}
		log.info("-------before http client close--------------------------------------------------");
	}

	/**
	 * connection will be keep alive, not return to pool
	 * <pre>
	 * 20:21:15.156 [main] DEBUG org.apache.http.impl.execchain.MainClientExec - Connection can be kept alive indefinitely
	 * </pre>
	 **/
	@Test
	public void testResponseUnclose() {
		HttpGet httpGet = new HttpGet(url);
		try {
			CloseableHttpResponse response = httpClient.execute(httpGet);
			final String responseText = response.getStatusLine().toString();
			log.info("response text: {}", responseText == null || responseText.length() <= 64 ? responseText :
					responseText.substring(0, 64));
		} catch (Exception e) {
			log.error("request error", e);
		}
		log.info("-------before http client close--------------------------------------------------");
	}

}