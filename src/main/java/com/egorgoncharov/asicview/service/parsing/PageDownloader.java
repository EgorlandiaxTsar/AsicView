package com.egorgoncharov.asicview.service.parsing;

import com.egorgoncharov.asicview.service.parsing.exception.InvalidIPv4AddressException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class PageDownloader {
    private static final Logger logger = LogManager.getLogger(PageDownloader.class);
    private final String user;
    private final String password;

    public PageDownloader(String user, String password) {
        this.user = user;
        this.password = password;
        disableSslVerification();
    }

    public Response getDocument(String ip, int preferredRequestTime) {
        long start = System.currentTimeMillis();
        logger.info("Web request started (target IP: '" + ip + "', start timestamp: " + start + ", username: '" + user + "', password: '" + password + "')");
        Connection connection = Jsoup.connect("https://" + ip + "/cgi-bin/luci/admin/status/btminerapi")
                .timeout(5000)
                .ignoreHttpErrors(true)
                .data("luci_username", user)
                .data("luci_password", password)
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((user + ":" + password).getBytes()));
        try {
            Document doc = connection.get();
            int requestTime = (int) (System.currentTimeMillis() - start);
            Response response = new Response(doc, connection.response().statusCode(), connection.response().statusMessage(), ip, requestTime, preferredRequestTime);
            String statusMark = response.isSuccessful() ? "SUCCESS" : "FAIL";
            logger.log(response.isSuccessful() ? Level.INFO : Level.WARN, "Web request terminated with status code " + connection.response().statusCode() + " " + connection.response().statusMessage() + " (target IP: '" + ip + "', request time: " + requestTime + "ms, status mark: '" + statusMark + "')");
            return response;
        } catch (SocketTimeoutException e) {
            logger.warn("Request timed out (target IP: '" + ip + "', request time: " + (System.currentTimeMillis() - start) + "ms, max: 5000ms)");
        } catch (ConnectException e) {
            logger.warn("Connection refused (target IP: '" + ip + "', request time: " + (System.currentTimeMillis() - start) + "ms, max: 5000ms)");
        } catch (MalformedURLException | UnknownHostException e) {
            logger.warn("Request failed due to bad IP (target IP: '" + ip + "')", e);
            return new Response(null, 400, "Bad IP", ip, (int) (System.currentTimeMillis() - start), preferredRequestTime);
        } catch (IOException e) {
            logger.error("Request failed due to exception (target IP: '" + ip + "')", e);
        }
        return new Response(null, 500, "Service Unavailable", ip, (int) (System.currentTimeMillis() - start), preferredRequestTime);
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    private static void disableSslVerification() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            logger.fatal("Internal application error, failed to disable ssl verification", e);
        }
    }

    private static void validateIPv4(String ipv4) throws InvalidIPv4AddressException {
        String[] ipv4Array = ipv4.split("\\.");
        if (ipv4Array.length == 4) {
            for (String s : ipv4Array) {
                if (!s.matches("\\d+")) {
                    throw new InvalidIPv4AddressException("Incorrect IPv4 address");
                }
            }
            return;
        }
        throw new IllegalArgumentException("Incorrect IPv4 address");
    }
}
