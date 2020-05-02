/**
 *
 */
package com.dakshata.autotrader.api;

import static com.dakshata.constants.autotrader.IAutoTrader.API_KEY_HEADER;
import static com.dakshata.tools.internet.HttpStatus.toTextDefault;

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

	private UnirestInstance client;

	private final String commandUrl;

	/**
	 * Initialize the AutoTrader API with your private API key.
	 *
	 * @param apiKey     your private api key
	 * @param serviceUrl AutoTrader api service url
	 */
	public AutoTrader(final String apiKey, final String serviceUrl) {
		super();
		this.init(apiKey);
		this.commandUrl = serviceUrl + "/command/execute";
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public IOperationResponse<String> execute(final String command) {
		final HttpResponse<OperationResponse> result = this.client.post(this.commandUrl).field("command", command)
				.asObject(OperationResponse.class);
		if (result.getStatus() != 200) {
			final String message = toTextDefault(result.getStatus(), result.getStatusText());
			return new OperationResponse<>(new Exception(result.getStatus() + ": " + message));
		}

		return result.getBody();
	}

	@Override
	public synchronized void setApiKey(final String apiKey) {
		this.init(apiKey);
	}

	private void init(final String apiKey) {
		this.shutdownClient();
		final Config config = new Config();
		config.setDefaultHeader(API_KEY_HEADER, apiKey);
		this.client = new UnirestInstance(config);
	}

	public void shutdownClient() {
		if (this.client != null) {
			this.client.shutDown();
		}
	}

	/**
	 * Graceful shutdown. Call when your application is about to terminate.
	 */
	@Override
	public void shutdown() {
		this.shutdownClient();
	}

}
