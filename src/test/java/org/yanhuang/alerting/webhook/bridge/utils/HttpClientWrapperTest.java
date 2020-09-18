package org.yanhuang.alerting.webhook.bridge.utils;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HttpClientWrapperTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private String url = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=test";
    private String proxy = "http://proxy.xxx.cn:8080";
    private String user = "userxx";
    private String password = "passxx";

    public void testUserPassProxyGet() {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10000).setSocketTimeout(10000)
                .setProxy(HttpHost.create(proxy))
                .build();
        HttpPost httpReq = new HttpPost(url);
        httpReq.setConfig(requestConfig);
        logger.info("begin request {}", url);
        logger.info("request config: {}", requestConfig);
        HttpClientContext clientContext = HttpClientContext.create();
        CredentialsProvider credentialsProvider = buildCredential();
        logger.info("credentialsProvider: {}", credentialsProvider);
        clientContext.setCredentialsProvider(credentialsProvider);
        try (var response = httpClient.execute(httpReq,clientContext)) {
            if (response.getStatusLine() != null) {
                logger.info("response status {} for request {}", response.getStatusLine(), url);
                if (response.getStatusLine().getStatusCode() >= 400) {
                    String respText = "";
                    if (response.getEntity() != null) {
                        respText = EntityUtils.toString(response.getEntity());
                    }
                    throw new RuntimeException("response code: " + response.getStatusLine().getStatusCode() + " content: " + respText);
                }
            }
            EntityUtils.consume(response.getEntity());
        } catch (Exception e) {
            logger.error("request {} error", url, e);
        }finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                logger.error("http client close error", e);
            }
        }
    }

    private CredentialsProvider buildCredential() {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope(HttpHost.create(proxy)),
                new UsernamePasswordCredentials(user, password));
        return credentialsProvider;
    }

    public static void main(String[] args) {
        HttpClientWrapperTest test = new HttpClientWrapperTest();
        test.testUserPassProxyGet();
    }

}