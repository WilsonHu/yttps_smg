package com.eservice.iot;

import com.eservice.iot.service.TokenService;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author HT
 */
@EnableScheduling
@SpringBootApplication
@ComponentScan
public class IotApplication {
	@Value("${park_base_url}")
	private String PARK_BASE_URL;
	@Bean
	public RestTemplate restTemplate() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
		TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
		SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
		SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new String[] { "TLSv1.2"},null, new HostnameVerifier(){
			@Override
			public boolean verify(String hostname, SSLSession session) {
				//解决证书跟访问域名不一致的问题
				//Certificate for <192.169.0.106> doesn't match any of the subject alternative
				return true;
			}
		});
		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setHttpClient(httpClient);

		RestTemplate  restTemplate = PARK_BASE_URL.contains("https") ? new RestTemplate(requestFactory) : new RestTemplate();
		StringHttpMessageConverter stringHttpMessageConverter=new StringHttpMessageConverter(Charset.forName("UTF-8"));
		ByteArrayHttpMessageConverter byteArrayHttpMessageConverter = new ByteArrayHttpMessageConverter();

		List<HttpMessageConverter<?>> list=new ArrayList<>();
		list.add(stringHttpMessageConverter);
		list.add(byteArrayHttpMessageConverter);
		restTemplate.setMessageConverters(list);
		return restTemplate;
	}

	@Bean
	public TokenService tokenService() {
		return new TokenService();
	}

	public static void main(String[] args) {
		SpringApplication.run(IotApplication.class, args);
	}
}
