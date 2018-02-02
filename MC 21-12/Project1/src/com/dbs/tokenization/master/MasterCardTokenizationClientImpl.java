package com.dbs.tokenization.master;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.dbs.tokenization.input.ActionType;
import com.mastercard.api.core.ApiConfig;
import com.mastercard.api.core.exception.ApiException;
import com.mastercard.api.core.model.HttpMethod;
import com.mastercard.api.core.security.CryptographyInterceptor;
import com.mastercard.api.core.security.oauth.OAuthAuthentication;

public class MasterCardTokenizationClientImpl implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -5214999442842362087L;
    private static String masterCertLoc;
    private static String trustStoreLoc;
    private static String proxyKeyStoreType;
    private static String proxyScheme;
    private static String proxyAuthUserID;
    private static String proxyAuthPass;
    private static String consumerKey;
    private static boolean isDebugEnabled;
    private static String keyAlias;
    private static String keyPassword;
    private static String proxyHost;
    private static int proxyPort;
    private static String targetHost;
    private static int targetPort;
    private static String environment;
    private static String PROD = "PROD";
    private static final String ENVIRONMENT_IDENTIFIER = "#env/";
    private static String HEADER_SEPARATOR = ";";

    public void initialize() {

        /*
         * masterCertLoc = com.tibco.pe.plugin.PluginProperties.getProperty(
         * "tibco.clientVar.LocalResources/Common/MasterCard/MasterCardCertPath"
         * );
         *
         * trustStoreLoc = com.tibco.pe.plugin.PluginProperties.getProperty(
         * "tibco.clientVar.LocalResources/Common/MasterCard/TrustStorePath");
         *
         * consumerKey = com.tibco.pe.plugin.PluginProperties.getProperty(
         * "tibco.clientVar.LocalResources/Common/MasterCard/ConsumerKey");
         *
         * isDebugEnabled = (com.tibco.pe.plugin.PluginProperties .getProperty(
         * "tibco.clientVar.LocalResources/Common/MasterCard/DebugEnabled") ==
         * "true") ? true : false;
         *
         * keyAlias = com.tibco.pe.plugin.PluginProperties.getProperty(
         * "tibco.clientVar.LocalResources/Common/MasterCard/KeyAlias");
         *
         * keyPassword = com.tibco.pe.plugin.PluginProperties.getProperty(
         * "tibco.clientVar.LocalResources/Common/MasterCard/KeyPassword");
         *
         * proxyHost = com.tibco.pe.plugin.PluginProperties.getProperty(
         * "tibco.clientVar.LocalResources/Common/MasterCard/ProxyHost");
         *
         * proxyPort =
         * Integer.parseInt(com.tibco.pe.plugin.PluginProperties.getProperty(
         * "tibco.clientVar.LocalResources/Common/MasterCard/ProxyPort"));
         *
         * targetHost = com.tibco.pe.plugin.PluginProperties.getProperty(
         * "tibco.clientVar.LocalResources/Common/MasterCard/SPHost");
         *
         * targetPort =
         * Integer.parseInt(com.tibco.pe.plugin.PluginProperties.getProperty(
         * "tibco.clientVar.LocalResources/Common/MasterCard/SPPort"));
         *
         * environment = com.tibco.pe.plugin.PluginProperties.getProperty(
         * "tibco.clientVar.LocalResources/Common/MasterCard/Environment");
         *
         * proxyKeyStoreType = com.tibco.pe.plugin.PluginProperties.getProperty(
         * "tibco.clientVar.LocalResources/Common/MasterCard/TrustStoreType");
         *
         * proxyScheme = com.tibco.pe.plugin.PluginProperties.getProperty(
         * "tibco.clientVar.LocalResources/Common/MasterCard/ProxyScheme");
         *
         * proxyAuthUserID = com.tibco.pe.plugin.PluginProperties.getProperty(
         * "tibco.clientVar.LocalResources/Common/MasterCard/ProxyUserName");
         *
         * proxyAuthPass = com.tibco.pe.plugin.PluginProperties.getProperty(
         * "tibco.clientVar.LocalResources/Common/MasterCard/ProxyPassword");
         */

        masterCertLoc = "src/test_mdes-1510739082-sandbox.p12";

        trustStoreLoc = "src/test_mdes-1510739082-sandbox.p12";

        consumerKey = "qHrmPGIhIqogAb3DckoAoK0OJlMem70TQU3bhGUdca69b66e!ab933609bbdb466fb8d395deae8925f60000000000000000";

        isDebugEnabled = true;

        keyAlias = "keyalias";

        keyPassword = "keystorepassword";

        proxyHost = "10.5.127.23";

        proxyPort = 8080;

        targetHost = "sandbosx.api.mastercard.com";

        targetPort = 443;

        environment = "PROD";

        proxyKeyStoreType = "jks";

        proxyScheme = "http";

        proxyAuthUserID = "vishistakommera";

        proxyAuthPass = "Dbs1234567";

        System.out.println("Master Card location: " + masterCertLoc);
        System.out.println("trust store location: " + trustStoreLoc);
        System.out.println("consumer key: " + consumerKey);
        System.out.println("Debug: " + isDebugEnabled);
        System.out.println("keyAlias: " + keyAlias);
        System.out.println("keypassword: " + keyPassword);
        System.out.println("proxyHost: " + proxyHost + " proxyport: " + proxyPort);
        System.out.println("Host: " + targetHost + " proxyport: " + targetPort);
        System.out.println("Environment: " + environment);
        System.out.println("proxyKeyStoreType" + proxyKeyStoreType);
        System.out.println("proxyScheme" + proxyScheme);
        System.out.println("proxyAuthUserID" + proxyAuthUserID);
        System.out.println("proxyAuthPass" + proxyAuthPass);
    }

    public String prepareAndSendRequest(String payLoad, String action) throws ApiException, Exception {

        initialize();

        if (isDebugEnabled) {
            System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
            System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
            System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "DEBUG");
            System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "ERROR");
        } else {
            java.util.logging.Logger.getLogger("org.apache.http.wire").setLevel(Level.OFF);
            java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.OFF);

            String[] propertiesToRemove = new String[] { "org.apache.commons.logging.Log",
                    "org.apache.commons.logging.simplelog.showdatetime",
                    "org.apache.commons.logging.simplelog.log.httpclient.wire",
                    "org.apache.commons.logging.simplelog.log.org.apache.http" };
            for (String property : propertiesToRemove) {
                System.getProperties().keySet().remove(property);
            }
        }

        HttpHost proxy = null;

        InputStream is = new FileInputStream(masterCertLoc);

        OAuthAuthentication authentication = new OAuthAuthentication(consumerKey, is, keyAlias, keyPassword);

        HttpClientBuilder builder = HttpClientBuilder.create();

        builder.useSystemProperties();

        builder.disableCookieManagement();

        if (proxyScheme.equalsIgnoreCase("https")) {

            proxy = new HttpHost(proxyHost, proxyPort, proxyScheme);
            System.setProperty("javax.net.ssl.trustStore", trustStoreLoc);
            System.setProperty("javax.net.ssl.trustStoreType", proxyKeyStoreType);

        } else {
            proxy = new HttpHost(proxyHost, proxyPort);
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(new AuthScope("proxyHost", proxyPort),
                    new UsernamePasswordCredentials(proxyAuthUserID, proxyAuthPass));
            builder.setDefaultCredentialsProvider(credentialsProvider);
        }
        builder.setProxy(proxy);

        CloseableHttpClient httpClient = builder.build();
        try {

            HttpHost target = new HttpHost(targetHost);
            builder.useSystemProperties();

            builder.disableCookieManagement();
            String actionUrl = ActionType.getActionTypeValue(action);

            if (actionUrl.contains(ENVIRONMENT_IDENTIFIER)) {
                String context = "";
                if (environment != null && PROD.equals(environment)) {
                    context = "";
                } else {
                    context = "mtf/";
                }
                actionUrl = actionUrl.replace(ENVIRONMENT_IDENTIFIER, context);

            }

            HttpPost request = new HttpPost("https://" + targetHost + ":" + targetPort + actionUrl);

            HttpEntity createEntity = new StringEntity(payLoad, ContentType.APPLICATION_JSON);

            request.setEntity(createEntity);
            request.setHeader(createEntity.getContentType());

            request.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString());

            URI uri = null;

            if (action != null) {
                uri = new URI("https://" + targetHost + actionUrl);
            }
            CryptographyInterceptor interceptor = ApiConfig.getCryptographyInterceptor(uri.getPath());
            HttpMethod httpMethod = HttpMethod.POST;

            authentication.sign(uri, httpMethod, ContentType.APPLICATION_JSON, payLoad, request);

            ResponseHandler<ApiControllerResponse> responseHandler = createResponseHandler();

            ApiControllerResponse apiResponse = httpClient.execute(target, request, responseHandler);
            /*
             * if (httpResponse.getStatusLine().getStatusCode() < 300) {
             * HttpEntity entity = httpResponse.getEntity(); if (entity != null)
             * { return EntityUtils.toString(entity); } } else { Map<String,
             * Object> response = null; HttpEntity entity =
             * httpResponse.getEntity(); if (entity != null) { // return
             * EntityUtils.toString(entity); response = (Map<String, Object>)
             * JSONValue.parse(EntityUtils.toString(entity)); List list =
             * convertToList(response); list.get(1); }
             *
             * throw new Exception(httpResponse.getStatusLine().getStatusCode()
             * + "-" + httpResponse.getStatusLine().getReasonPhrase()); }
             */

            if (apiResponse.hasPayload()) {

                Object response = JSONValue.parse(apiResponse.getPayload());
                Map<String, Object> map = null;
                if (response instanceof JSONObject) {
                    map = (Map<String, Object>) response;
                }
                Map<String, Object> map1 = (Map<String, Object>) map.get("Errors");
                JSONArray jsonObj = (JSONArray) map1.get("Error");
                Map<String, Object> obj = (Map<String, Object>) jsonObj.get(0);
                String reasonCode = (String) obj.get("ReasonCode");
                if (apiResponse.getStatus() < 300) {
                    /*
                     * if (operationConfig.getAction() == Action.list) {
                     *
                     * Map<String, Object> map = new HashMap<String, Object>();
                     * List list = null;
                     *
                     * // arizzini: if the response is an object we need to //
                     * convert this into a map if (response instanceof
                     * JSONObject) { list = convertToList((Map<? extends String,
                     * ? extends Object>) response); } // arizzini: if the
                     * response is an array we need simply // case to a List of
                     * Maps. else { list = ((List<Map<? extends String, ?
                     * extends Object>>) response); } map.put("list", list);
                     * return map; } else {
                     */

                    /*
                     * Map<String, Object> map = null; if (response instanceof
                     * JSONObject) { map = (Map<String, Object>) response;
                     *
                     * if (interceptor == null) { return map; } else { return
                     * interceptor.decrypt(map); }
                     *
                     * } else { map = new HashMap<String, Object>();
                     * map.put("list", (response)); return map; }
                     */

                    return apiResponse.getPayload();

                } else {
                    throw new ApiException(apiResponse.getStatus(), response);
                }
            }

        } catch (ApiException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            httpClient.close();
        }
        return null;

    }

    private List convertToList(Map<? extends String, ? extends Object> response) {
        List list = new ArrayList();

        if (response.keySet().iterator().hasNext()) {
            String key = response.keySet().iterator().next();
            Map<? extends String, ? extends Object> level1 = response.get(key) instanceof Map
                    ? (Map<? extends String, ? extends Object>) response.get(key) : null;

            if (level1 != null && level1.keySet().iterator().hasNext()) {
                key = level1.keySet().iterator().next();
                list = level1.get(key) instanceof List ? (List) level1.get(key) : new ArrayList<Object>();
            }
        }

        return list;
    }

    ResponseHandler<ApiControllerResponse> createResponseHandler() {
        return new ResponseHandler<ApiControllerResponse>() {
            public ApiControllerResponse handleResponse(HttpResponse httpResponse) throws IOException {
                ApiControllerResponse apiResponse = new ApiControllerResponse();
                apiResponse.setHttpResponse(httpResponse);

                StatusLine statusLine = httpResponse.getStatusLine();
                apiResponse.setStatus(statusLine.getStatusCode());
                HttpEntity entity = httpResponse.getEntity();

                // arizzini: entity == null when HTTP 200
                if (entity != null) {
                    String payload = EntityUtils.toString(entity, ContentType.APPLICATION_JSON.getCharset());

                    // arizzini: if we have content, we try to parse it
                    if (!payload.isEmpty()) {
                        String responseContentType;
                        Header header = httpResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE);
                        if (header == null) {
                            throw new IllegalStateException("Unknown content type. Missing Content-Type header");
                        } else {
                            if (header.getValue().contains(HEADER_SEPARATOR)) {
                                String parts[] = header.getValue().split(HEADER_SEPARATOR);
                                responseContentType = parts[0];
                            } else {
                                responseContentType = header.getValue();
                            }
                        }

                        if (ContentType.parse(responseContentType).getMimeType()
                                .equals(ContentType.APPLICATION_JSON.getMimeType())) {
                            apiResponse.setPayload(payload);
                        } else {
                            throw new IOException("Response was not " + ContentType.APPLICATION_JSON.getMimeType()
                                    + ", it was: " + responseContentType + ". Unable to process payload. "
                                    + "\nResponse: [ " + payload + " + ]");
                        }
                    } else {
                        // arizzini: 200 with no content like a delete.
                        apiResponse.setPayload("");
                    }
                } else {
                    // arizzini: 204 with no content like a delete.
                    apiResponse.setPayload("");
                }

                return apiResponse;

            }
        };
    }

    protected class ApiControllerResponse {
        private HttpResponse httpResponse;
        private String payload;
        private int status;

        public HttpResponse getHttpResponse() {
            return httpResponse;
        }

        public void setHttpResponse(HttpResponse httpResponse) {
            this.httpResponse = httpResponse;
        }

        public String getPayload() {
            return payload;
        }

        public void setPayload(String payload) {
            this.payload = payload;
        }

        public boolean hasPayload() {
            return payload != null && payload.length() > 0;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }

    public static void main(String[] args) {
        MasterCardTokenizationClientImpl imp = new MasterCardTokenizationClientImpl();
        String payLoad = "{\"TokenSuspendRequest\":{\"TokenUniqueReference\":\"DWSPMC00000000010906a349d9ca4eb1a4d53e3c90a11d9c\",\"ReasonCode\":\"T\",\"AuditInfo\":{\"UserId\":\"A1435477\",\"UserName\":\"John Smith\",\"Organization\":\"Solid Bank Inc\"}}}";
        String out;
        try {
            out = imp.prepareAndSendRequest(payLoad, "SUSPEND");
            System.out.println("Success" + out);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            out = e.toString();
            e.getMessage();
            System.out.println("exception" + out);
        }

    }

}
