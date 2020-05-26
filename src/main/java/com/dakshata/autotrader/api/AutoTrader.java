/**
 *
 */
package com.dakshata.autotrader.api;

import static com.dakshata.constants.autotrader.IAutoTrader.API_KEY_HEADER;
import static com.dakshata.tools.internet.HttpStatus.toTextDefault;

import java.util.Set;

import com.dakshata.data.model.common.IOperationResponse;
import com.dakshata.data.model.common.OperationResponse;
import com.dakshata.trading.model.platform.IPlatformMargin;
import com.dakshata.trading.model.platform.IPlatformOrder;
import com.dakshata.trading.model.platform.IPlatformPosition;

import kong.unirest.Config;
import kong.unirest.HttpResponse;
import kong.unirest.UnirestInstance;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of AutoTrade API functions.
 *
 * @author PRITESH
 *
 */
@Slf4j
public class AutoTrader implements IAutoTrader {

	private static final String COMMAND_URI = "/command";

	private static final String TRADING_URI = "/trading";

	private static final String ACCOUNT_URI = "/account";

	private UnirestInstance client;

	private final String commandUrl, livePseudoAccountsUrl;

	private final String readPlatformOrdersUrl, readPlatformPositionsUrl, readPlatformMarginsUrl;

	/**
	 * Initialize the AutoTrader API with your private API key.
	 *
	 * @param apiKey     your private api key
	 * @param serviceUrl AutoTrader api service url
	 */
	public AutoTrader(final String apiKey, final String serviceUrl) {
		super();
		this.init(apiKey);
		this.commandUrl = serviceUrl + COMMAND_URI + "/execute";
		this.readPlatformOrdersUrl = serviceUrl + TRADING_URI + "/readPlatformOrders";
		this.readPlatformPositionsUrl = serviceUrl + TRADING_URI + "/readPlatformPositions";
		this.readPlatformMarginsUrl = serviceUrl + TRADING_URI + "/readPlatformMargins";
		this.livePseudoAccountsUrl = serviceUrl + ACCOUNT_URI + "/fetchLivePseudoAccounts";
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public IOperationResponse<Set<String>> fetchLivePseudoAccounts() {
		final HttpResponse<OperationResponse> response = this.client.get(this.livePseudoAccountsUrl)
				.asObject(OperationResponse.class);
		if (response.getStatus() != 200) {
			return this.processHttpError(response);
		}

		return response.getBody();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public IOperationResponse<? extends Object> execute(final String command) {
		final HttpResponse<OperationResponse> response = this.client.post(this.commandUrl).field("command", command)
				.asObject(OperationResponse.class);
		if (response.getStatus() != 200) {
			return this.processHttpError(response);
		}

		return response.getBody();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public IOperationResponse<Set<IPlatformOrder>> readPlatformOrders(final String pseudoAccount) {
		final HttpResponse<OperationResponse> response = this.client.post(this.readPlatformOrdersUrl)
				.field("pseudoAccount", pseudoAccount).asObject(OperationResponse.class);
		if (response.getStatus() != 200) {
			return this.processHttpError(response);
		}

		return response.getBody();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public IOperationResponse<Set<IPlatformPosition>> readPlatformPositions(final String pseudoAccount) {
		final HttpResponse<OperationResponse> response = this.client.post(this.readPlatformPositionsUrl)
				.field("pseudoAccount", pseudoAccount).asObject(OperationResponse.class);
		if (response.getStatus() != 200) {
			return this.processHttpError(response);
		}

		return response.getBody();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public IOperationResponse<Set<IPlatformMargin>> readPlatformMargins(final String pseudoAccount) {
		final HttpResponse<OperationResponse> response = this.client.post(this.readPlatformMarginsUrl)
				.field("pseudoAccount", pseudoAccount).asObject(OperationResponse.class);
		if (response.getStatus() != 200) {
			return this.processHttpError(response);
		}

		return response.getBody();
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
		try {
			if (this.client != null) {
				this.client.shutDown();
			}
		} catch (final Exception e) {
			log.error("Error while shutting down client: ", e);
		}
	}

	/**
	 * Graceful shutdown. Call when your application is about to terminate.
	 */
	@Override
	public void shutdown() {
		this.shutdownClient();
	}

	@SuppressWarnings("rawtypes")
	private final <T> IOperationResponse<T> processHttpError(final HttpResponse<OperationResponse> response) {
		final String message = toTextDefault(response.getStatus(), response.getStatusText());
		final Exception error = new Exception(response.getStatus() + ": " + message);
		return OperationResponse.<T>builder().error(error).build();
	}

}
