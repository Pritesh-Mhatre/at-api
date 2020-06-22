/**
 *
 */
package com.dakshata.autotrader.api;

import static com.dakshata.constants.autotrader.IAutoTrader.API_KEY_HEADER;
import static com.dakshata.tools.internet.HttpStatus.toTextDefault;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.dakshata.constants.trading.OrderType;
import com.dakshata.constants.trading.ProductType;
import com.dakshata.constants.trading.TradeType;
import com.dakshata.data.model.common.IOperationResponse;
import com.dakshata.data.model.common.OperationResponse;
import com.dakshata.trading.model.platform.PlatformMargin;
import com.dakshata.trading.model.platform.PlatformOrder;
import com.dakshata.trading.model.platform.PlatformPosition;
import com.dakshata.trading.model.portfolio.Order;

import kong.unirest.Config;
import kong.unirest.GenericType;
import kong.unirest.HttpResponse;
import kong.unirest.JacksonObjectMapper;
import kong.unirest.UnirestInstance;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of AutoTrade API functions. Only create one instance of this
 * class per API key.
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

	private final String placeOrderUrl, placeRegularOrderUrl, placeCoverOrderUrl, placeBracketOrderUrl;

	private final String cancelOrderByPlatformIdUrl;

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
		this.placeOrderUrl = serviceUrl + TRADING_URI + "/placeOrder";
		this.placeRegularOrderUrl = serviceUrl + TRADING_URI + "/placeRegularOrder";
		this.placeCoverOrderUrl = serviceUrl + TRADING_URI + "/placeCoverOrder";
		this.placeBracketOrderUrl = serviceUrl + TRADING_URI + "/placeBracketOrder";
		this.cancelOrderByPlatformIdUrl = serviceUrl + TRADING_URI + "/cancelOrderByPlatformId";
		this.livePseudoAccountsUrl = serviceUrl + ACCOUNT_URI + "/fetchLivePseudoAccounts";
	}

	@Override
	public IOperationResponse<Set<String>> fetchLivePseudoAccounts() {
		final HttpResponse<OperationResponse<Set<String>>> response = this.client.get(this.livePseudoAccountsUrl)
				.asObject(new GenericType<OperationResponse<Set<String>>>() {
				});
		if (response.getStatus() != 200) {
			return this.processHttpError(response);
		}

		return response.getBody();
	}

	@Override
	public IOperationResponse<? extends Object> execute(final String command) {
		final HttpResponse<OperationResponse<? extends Object>> response = this.client.post(this.commandUrl)
				.field("command", command).asObject(new GenericType<OperationResponse<? extends Object>>() {
				});
		if (response.getStatus() != 200) {
			return this.processHttpError(response);
		}

		return response.getBody();
	}

	@Override
	public IOperationResponse<String> placeOrder(final Order order) {
		final HttpResponse<OperationResponse<String>> response = this.client.post(this.placeOrderUrl).body(order)
				.asObject(new GenericType<OperationResponse<String>>() {
				});
		if (response.getStatus() != 200) {
			return this.processHttpError(response);
		}

		return response.getBody();
	}

	@Override
	public IOperationResponse<String> placeRegularOrder(final String pseudoAccount, final String exchange,
			final String symbol, final TradeType tradeType, final OrderType orderType, final ProductType productType,
			final int quantity, final float price, final float triggerPrice) {
		final Map<String, Object> params = new HashMap<>();
		params.put("pseudoAccount", pseudoAccount);
		params.put("exchange", exchange);
		params.put("symbol", symbol);
		params.put("tradeType", tradeType);
		params.put("orderType", orderType);
		params.put("productType", productType);
		params.put("quantity", quantity);
		params.put("price", price);
		params.put("triggerPrice", triggerPrice);

		return this.postOrder(this.placeRegularOrderUrl, params);
	}

	@Override
	public IOperationResponse<String> placeBracketOrder(final String pseudoAccount, final String exchange,
			final String symbol, final TradeType tradeType, final OrderType orderType, final int quantity,
			final float price, final float triggerPrice, final float target, final float stoploss,
			final float trailingStoploss) {
		final Map<String, Object> params = new HashMap<>();
		params.put("pseudoAccount", pseudoAccount);
		params.put("exchange", exchange);
		params.put("symbol", symbol);
		params.put("tradeType", tradeType);
		params.put("orderType", orderType);
		params.put("quantity", quantity);
		params.put("price", price);
		params.put("triggerPrice", triggerPrice);
		params.put("target", target);
		params.put("stoploss", stoploss);
		params.put("trailingStoploss", trailingStoploss);

		return this.postOrder(this.placeBracketOrderUrl, params);
	}

	@Override
	public IOperationResponse<String> placeCoverOrder(final String pseudoAccount, final String exchange,
			final String symbol, final TradeType tradeType, final OrderType orderType, final int quantity,
			final float price, final float triggerPrice) {
		final Map<String, Object> params = new HashMap<>();
		params.put("pseudoAccount", pseudoAccount);
		params.put("exchange", exchange);
		params.put("symbol", symbol);
		params.put("tradeType", tradeType);
		params.put("orderType", orderType);
		params.put("quantity", quantity);
		params.put("price", price);
		params.put("triggerPrice", triggerPrice);

		return this.postOrder(this.placeCoverOrderUrl, params);
	}

	@Override
	public IOperationResponse<Boolean> cancelOrderByPlatformId(final String pseudoAccount, final String platformId) {
		final Map<String, Object> params = new HashMap<>();
		params.put("pseudoAccount", pseudoAccount);
		params.put("platformId", platformId);

		final HttpResponse<OperationResponse<Boolean>> response = this.client.post(this.cancelOrderByPlatformIdUrl)
				.fields(params).asObject(new GenericType<OperationResponse<Boolean>>() {
				});
		if (response.getStatus() != 200) {
			return this.processHttpError(response);
		}

		return response.getBody();
	}

	@Override
	public IOperationResponse<Set<PlatformOrder>> readPlatformOrders(final String pseudoAccount) {
		final HttpResponse<OperationResponse<Set<PlatformOrder>>> response = this.client
				.post(this.readPlatformOrdersUrl).field("pseudoAccount", pseudoAccount)
				.asObject(new GenericType<OperationResponse<Set<PlatformOrder>>>() {
				});
		if (response.getStatus() != 200) {
			return this.processHttpError(response);
		}

		return response.getBody();
	}

	@Override
	public IOperationResponse<Set<PlatformPosition>> readPlatformPositions(final String pseudoAccount) {
		final HttpResponse<OperationResponse<Set<PlatformPosition>>> response = this.client
				.post(this.readPlatformPositionsUrl).field("pseudoAccount", pseudoAccount)
				.asObject(new GenericType<OperationResponse<Set<PlatformPosition>>>() {
				});
		if (response.getStatus() != 200) {
			return this.processHttpError(response);
		}

		return response.getBody();
	}

	@Override
	public IOperationResponse<Set<PlatformMargin>> readPlatformMargins(final String pseudoAccount) {
		final HttpResponse<OperationResponse<Set<PlatformMargin>>> response = this.client
				.post(this.readPlatformMarginsUrl).field("pseudoAccount", pseudoAccount)
				.asObject(new GenericType<OperationResponse<Set<PlatformMargin>>>() {
				});
		if (response.getStatus() != 200) {
			return this.processHttpError(response);
		}

		return response.getBody();
	}

	@Override
	public synchronized void setApiKey(final String apiKey) {
		this.init(apiKey);
	}

	/**
	 * Graceful shutdown. Call when your application is about to terminate.
	 */
	@Override
	public void shutdown() {
		this.shutdownClient();
	}

	private void init(final String apiKey) {
		this.shutdownClient();
		final Config config = new Config();
		config.setDefaultHeader(API_KEY_HEADER, apiKey);
		// Spring boot uses Jackson by default, hence we use jackson here
		config.setObjectMapper(new JacksonObjectMapper());
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

	private IOperationResponse<String> postOrder(final String url, final Map<String, Object> params) {
		final HttpResponse<OperationResponse<String>> response = this.client.post(url).fields(params)
				.asObject(new GenericType<OperationResponse<String>>() {
				});
		if (response.getStatus() != 200) {
			return this.processHttpError(response);
		}

		return response.getBody();
	}

	private final <T> IOperationResponse<T> processHttpError(final HttpResponse<?> response) {
		final int status = response.getStatus();

		final String message = (status == 403) ? "Your API Key is wrong. Please check settings."
				: toTextDefault(status, response.getStatusText());

		final Exception error = new Exception(status + ": " + message);
		return OperationResponse.<T>builder().error(error).build();
	}

}
