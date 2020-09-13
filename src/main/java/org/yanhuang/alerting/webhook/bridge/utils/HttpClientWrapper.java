package org.yanhuang.alerting.webhook.bridge.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yanhuang.alerting.webhook.bridge.configue.ProxyServerProperties;

import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Properties;

/**
 * wrapper http client, support some convenience methods
 */
public class HttpClientWrapper {
    private static final CloseableHttpClient HC;

    public static final int CONNECT_TIMEOUT = 60 * 1000;

    private static final RequestConfig defaultRc = RequestConfig.custom()
            .setConnectionRequestTimeout(60 * 1000)
            .setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(CONNECT_TIMEOUT).build();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    static {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(1024);
        cm.setDefaultMaxPerRoute(128);
        // Connection timeout is the timeout until a connection with the server is established.
        // ConnectionRequestTimeout used when requesting a connection from the connection manager.
        HC = HttpClients.custom().setConnectionManager(cm).setDefaultRequestConfig(defaultRc).build();
    }

    private final int connectTimeoutMillis;

    private final int socketReadTimeoutMillis;

    public HttpClientWrapper() {
        this(CONNECT_TIMEOUT, CONNECT_TIMEOUT);
    }

    public HttpClientWrapper(int connectTimeoutMillis, int socketReadTimeoutMillis) {
        if (connectTimeoutMillis <= 0) {
            this.connectTimeoutMillis = CONNECT_TIMEOUT;
        } else {
            this.connectTimeoutMillis = connectTimeoutMillis;
        }
        if (socketReadTimeoutMillis <= 0) {
            this.socketReadTimeoutMillis = CONNECT_TIMEOUT;
        } else {
            this.socketReadTimeoutMillis = socketReadTimeoutMillis;
        }
    }

    /**
     * 调用http接口，请求和返回均为json格式
     *
     * @param request               请求对象（可序列化为json，如果是String，则直接使用）
     * @param url                   接口url
     * @param headers               附加的header
     * @param proxyServerProperties 代码服务器配置信息
     * @return 响应的json string，响应为非法状态时，则为null
     */
    public String postJson(Object request, String url, Properties headers,
                           ProxyServerProperties proxyServerProperties) {
        return postJson(request, url, headers, null, null, proxyServerProperties);
    }

    public String postJson(Object request, String url, Properties headers, String user, String password,
                           ProxyServerProperties proxyServerProperties) {
        final String requestText = null == request ? "" : request instanceof CharSequence ? request.toString() :
                JsonCodec.toString(request);
        Properties newHeads = headers != null ? new Properties(headers) : new Properties();
        newHeads.put("content-type", "application/json; charset=UTF-8");
        newHeads.put("accept", "application/json");
        return invoke(url, "POST", newHeads, requestText, user, password, proxyServerProperties);
    }

    private String invoke(String url, String method, Properties headers, String requestText, String user,
                          String password, ProxyServerProperties proxyServerProperties) {
        HttpRequestBase httpUriRequest;
        if ("GET".equalsIgnoreCase(method)) {
            httpUriRequest = new HttpGet(url);
        } else if ("PATCH".equalsIgnoreCase(method)) {
            httpUriRequest = new HttpPost(url);
            httpUriRequest.setHeader("x-patch-override-post", "true");
            if (requestText != null) {
                ((HttpPost) httpUriRequest).setEntity(new StringEntity(requestText, StandardCharsets.UTF_8));
            }
        } else if ("POST".equalsIgnoreCase(method)) {
            httpUriRequest = new HttpPost(url);
            if (requestText != null) {
                ((HttpPost) httpUriRequest).setEntity(new StringEntity(requestText, StandardCharsets.UTF_8));
            }
        } else if ("PUT".equalsIgnoreCase(method)) {
            httpUriRequest = new HttpPut(url);
            if (requestText != null) {
                ((HttpPut) httpUriRequest).setEntity(new StringEntity(requestText, StandardCharsets.UTF_8));
            }
        } else if ("DELETE".equalsIgnoreCase(method)) {
            httpUriRequest = new HttpDelete(url);
        } else {
            throw new RuntimeException("method " + method + " is not support");
        }
        //附加参数中的header到http request中
        if (headers != null) {
            Enumeration<Object> keys = headers.keys();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                String value = headers.getProperty(key);
                if (value != null) {
                    httpUriRequest.setHeader(key, value);
                }
            }
        }
        //设置请求参数：超时、代理
        final RequestConfig requestConfig = buildRequestConfig(proxyServerProperties);
        logger.info("request config: {} of {}", requestConfig, url);
        httpUriRequest.setConfig(requestConfig);
        //设置认证消息
        HttpClientContext context = HttpClientContext.create();
        final CredentialsProvider credentialsProvider = buildCredential(url, user, password, proxyServerProperties);
        if (credentialsProvider != null) {
            logger.info("request credentialsProvider: {} of {}", credentialsProvider, url);
            context.setCredentialsProvider(credentialsProvider);
        }
        try (CloseableHttpResponse response = HC.execute(httpUriRequest, context)) {
            HttpEntity entity = response.getEntity();
            String responseText = null;
            if (response.getStatusLine() != null && response.getStatusLine().getStatusCode() < 400) {
                if (entity != null) {
                    responseText = EntityUtils.toString(entity, StandardCharsets.UTF_8.name());
                } else {
                    responseText = "";
                }
            } else {
                final int code = response.getStatusLine() != null ? response.getStatusLine().getStatusCode() : -1;
                final String responseErrText = entity != null ? EntityUtils.toString(entity) : null;
                logger.error("response error code: {}, content: {} of {}", code, responseErrText, url);
            }
            return responseText;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CredentialsProvider buildCredential(String url, String user, String password,
                                                ProxyServerProperties proxyServerProperties) {
        CredentialsProvider credentialsProvider = null;
        if (user != null && !user.isBlank()) {
            credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(new AuthScope(HttpHost.create(url)),
                    new UsernamePasswordCredentials(user, password));
        }

        if (isValidProxyUser(proxyServerProperties)) {
            if (credentialsProvider == null) {
                credentialsProvider = new BasicCredentialsProvider();
            }
            credentialsProvider.setCredentials(new AuthScope(parseHttpHost(proxyServerProperties)),
                    new UsernamePasswordCredentials(proxyServerProperties.getUserName(),
                            proxyServerProperties.getPassword()));
        }
        return credentialsProvider;
    }

    private HttpHost parseHttpHost(ProxyServerProperties proxyServerProperties) {
        return new HttpHost(proxyServerProperties.getHost(), proxyServerProperties.getPort(),
                proxyServerProperties.getProtocol());
    }

    private RequestConfig buildRequestConfig(ProxyServerProperties proxyServerProperties) {
        if (isValidProxyHost(proxyServerProperties)) {
            return RequestConfig.custom()
                    .setConnectionRequestTimeout(this.connectTimeoutMillis)
                    .setConnectTimeout(this.connectTimeoutMillis).setSocketTimeout(this.socketReadTimeoutMillis)
                    .setProxy(parseHttpHost(proxyServerProperties))
                    .build();
        } else {
            return RequestConfig.custom()
                    .setConnectionRequestTimeout(this.connectTimeoutMillis)
                    .setConnectTimeout(this.connectTimeoutMillis).setSocketTimeout(this.socketReadTimeoutMillis)
                    .build();
        }
    }

    private boolean isValidProxyHost(ProxyServerProperties proxyServerProperties) {
        return proxyServerProperties != null && proxyServerProperties.getHost() != null && !proxyServerProperties.getHost().isBlank();
    }

    private boolean isValidProxyUser(ProxyServerProperties proxyServerProperties) {
        return proxyServerProperties != null && proxyServerProperties.getUserName() != null && !proxyServerProperties.getUserName().isBlank();
    }
}
