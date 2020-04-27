/**
 *
 */
package com.dakshata.autotrader.api;

import static com.dakshata.constants.autotrader.IAutoTrader.API_KEY_HEADER;

import com.dakshata.data.model.common.IOperationResponse;
import com.dakshata.data.model.common.OperationResponse;

import kong.unirest.Config;
import kong.unirest.HttpResponse;
import kong.unirest.UnirestInstance;

/**
 * Implementation of AutoTrade API functions.
 *
 * @author PRITESH
 *
 */
public class AutoTrader implements IAutoTrader {

	private final UnirestInstance client;

	private final String commandUrl;

	/**
	 * Initialize the AutoTrader API with your private API key.
	 *
	 * @param apiKey     your private api key
	 * @param serviceUrl AutoTrader api service url
	 */
	public AutoTrader(final String apiKey, final String serviceUrl) {
		super();
		final Config config = new Config();
		config.setDefaultHeader(API_KEY_HEADER, apiKey);
		this.client = new UnirestInstance(config);
		this.commandUrl = serviceUrl + "/command/execute";
	}

	/**
	 * Graceful shutdown. Call when your application is about to terminate.
	 */
	@Override
	public void shutdown() {
		this.client.shutDown();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public IOperationResponse<Boolean> execute(final String command) {
		final HttpResponse<OperationResponse> result = this.client.post(this.commandUrl).field("command", command)
				.asObject(OperationResponse.class);
		return result.getBody();
	}

}
