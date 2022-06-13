/**
 *
 */
package com.dakshata.autotrader.api;

import static com.dakshata.autotrader.api.IAutoTrader.SERVER_URL;
import static kong.unirest.Config.DEFAULT_CONNECTION_TIMEOUT;
import static kong.unirest.Config.DEFAULT_SOCKET_TIMEOUT;

import lombok.Builder;
import lombok.Getter;

/**
 * Configuration for AutoTrader Web java api client.
 *
 * @author PRITESH
 *
 */
@Builder
@Getter
public class AutoTraderClientConfig {

	@Builder.Default
	private final int maxConnections = 250, maxConnectionsPerRoute = 200,
			connectTimeout = DEFAULT_CONNECTION_TIMEOUT * 3, socketTimeout = DEFAULT_SOCKET_TIMEOUT * 2;

	@Builder.Default
	private final boolean autoRetryOnError = true;

	@Builder.Default
	private final String serviceUrl = SERVER_URL;

	private final String apiKey;

	public static final AutoTraderClientConfig defaultConfig(final String apiKey) {
		return AutoTraderClientConfig.builder().apiKey(apiKey).build();
	}

}
