/**
 *
 */
package com.dakshata.autotrader.api;

import static com.dakshata.constants.autotrader.IAutoTrader.API_KEY_HEADER;
import static com.dakshata.tools.internet.HttpStatus.toTextDefault;
import static com.dakshata.tools.string.StringUtil.isEmpty;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.dakshata.constants.trading.OrderType;
import com.dakshata.constants.trading.PositionCategory;
import com.dakshata.constants.trading.PositionType;
import com.dakshata.constants.trading.ProductType;
import com.dakshata.constants.trading.TradeType;
import com.dakshata.data.model.common.IOperationResponse;
import com.dakshata.data.model.common.OperationResponse;
import com.dakshata.trading.model.platform.PlatformMargin;
import com.dakshata.trading.model.platform.PlatformOrder;
import com.dakshata.trading.model.platform.PlatformPosition;
import com.dakshata.trading.model.portfolio.IOrder;

import kong.unirest.GenericType;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.HttpResponse;
import kong.unirest.UnirestInstance;
import kong.unirest.UnirestParsingException;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Responsible for trading activities.
 *
 * @author PRITESH
 *
 */
public class TradingService implements ITradingService {

	private static final String COMMAND_URI = "/command";

	private static final String TRADING_URI = "/trading";

	private static final String ACCOUNT_URI = "/account";

	private final String commandUrl, livePseudoAccountsUrl;

	private final String readPlatformOrdersUrl, readPlatformPositionsUrl, readPlatformMarginsUrl;

	private final String placeOrderUrl, placeRegularOrderUrl, placeCoverOrderUrl, placeBracketOrderUrl;

	private final String cancelOrderByPlatformIdUrl, modifyOrderByPlatformIdUrl;

	private final String cancelChildOrdersByPlatformIdUrl;

	private final String squareOffPositionUrl, squareOffPortfolioUrl;

	@Getter
	@Setter
	private UnirestInstance client;

	public TradingService(final String serviceUrl, final UnirestInstance client) {
		super();
		this.client = client;
		this.commandUrl = serviceUrl + COMMAND_URI + "/execute";
		this.readPlatformOrdersUrl = serviceUrl + TRADING_URI + "/readPlatformOrders";
		this.readPlatformPositionsUrl = serviceUrl + TRADING_URI + "/readPlatformPositions";
		this.readPlatformMarginsUrl = serviceUrl + TRADING_URI + "/readPlatformMargins";
		this.placeOrderUrl = serviceUrl + TRADING_URI + "/placeOrder";
		this.placeRegularOrderUrl = serviceUrl + TRADING_URI + "/placeRegularOrder";
		this.placeCoverOrderUrl = serviceUrl + TRADING_URI + "/placeCoverOrder";
		this.placeBracketOrderUrl = serviceUrl + TRADING_URI + "/placeBracketOrder";
		this.cancelOrderByPlatformIdUrl = serviceUrl + TRADING_URI + "/cancelOrderByPlatformId";
		this.cancelChildOrdersByPlatformIdUrl = serviceUrl + TRADING_URI + "/cancelChildOrdersByPlatformId";
		this.modifyOrderByPlatformIdUrl = serviceUrl + TRADING_URI + "/modifyOrderByPlatformId";
		this.livePseudoAccountsUrl = serviceUrl + ACCOUNT_URI + "/fetchLivePseudoAccounts";
		this.squareOffPositionUrl = serviceUrl + ACCOUNT_URI + "/squareOffPosition";
		this.squareOffPortfolioUrl = serviceUrl + ACCOUNT_URI + "/squareOffPortfolio";
	}

	@Override
	public IOperationResponse<Set<String>> fetchLivePseudoAccounts() {
		final HttpResponse<OperationResponse<Set<String>>> response = this.client.get(this.livePseudoAccountsUrl)
				.asObject(new GenericType<OperationResponse<Set<String>>>() {
				});

		return this.processResponse(response);
	}

	@Override
	public IOperationResponse<? extends Object> execute(@NonNull final String command) {
		final HttpResponse<OperationResponse<? extends Object>> response = this.client.post(this.commandUrl)
				.field("command", command).asObject(new GenericType<OperationResponse<? extends Object>>() {
				});
		if (response.getStatus() != 200) {
			if (response.getParsingError().isPresent()) {
				return OperationResponse.<Object>builder().error(response.getParsingError().get()).build();
			} else {
				return this.processHttpError(response);
			}
		}

		return response.getBody();
	}

	@Override
	public IOperationResponse<String> placeOrder(@NonNull final IOrder order) {
		return this.placeOrder(null, order);
	}

	@Override
	public IOperationResponse<String> placeOrder(final String apiKey, final IOrder order) {
		final HttpRequestWithBody request = this.client.post(this.placeOrderUrl);
		if (!isEmpty(apiKey)) {
			request.header(API_KEY_HEADER, apiKey);
		}

		final HttpResponse<OperationResponse<String>> response = request.header("Content-Type", "application/json")
				.body(order).asObject(new GenericType<OperationResponse<String>>() {
				});

		return this.processResponse(response);
	}

	@Override
	public IOperationResponse<String> placeRegularOrder(@NonNull final String pseudoAccount,
			final @NonNull String exchange, @NonNull final String symbol, @NonNull final TradeType tradeType,
			@NonNull final OrderType orderType, @NonNull final ProductType productType, final int quantity,
			final float price, final float triggerPrice) {
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
	public IOperationResponse<String> placeBracketOrder(@NonNull final String pseudoAccount,
			@NonNull final String exchange, @NonNull final String symbol, @NonNull final TradeType tradeType,
			@NonNull final OrderType orderType, final int quantity, final float price, final float triggerPrice,
			final float target, final float stoploss, final float trailingStoploss) {
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
	public IOperationResponse<String> placeCoverOrder(@NonNull final String pseudoAccount,
			@NonNull final String exchange, @NonNull final String symbol, @NonNull final TradeType tradeType,
			@NonNull final OrderType orderType, final int quantity, final float price, final float triggerPrice) {
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
	public IOperationResponse<Boolean> modifyOrderByPlatformId(@NonNull final String pseudoAccount,
			@NonNull final String platformId, final OrderType orderType, final Integer quantity, final Float price,
			final Float triggerPrice) {
		return this.modifyOrderByPlatformId(null, pseudoAccount, platformId, orderType, quantity, price, triggerPrice);
	}

	@Override
	public IOperationResponse<Boolean> modifyOrderByPlatformId(final String apiKey, @NonNull final String pseudoAccount,
			@NonNull final String platformId, final OrderType orderType, final Integer quantity, final Float price,
			final Float triggerPrice) {
		final Map<String, Object> params = new HashMap<>();
		params.put("pseudoAccount", pseudoAccount);
		params.put("platformId", platformId);
		params.put("orderType", (orderType == null) ? "" : orderType.name());
		params.put("quantity", quantity);
		params.put("price", price);
		params.put("triggerPrice", triggerPrice);

		final HttpRequestWithBody request = this.client.post(this.modifyOrderByPlatformIdUrl);
		if (!isEmpty(apiKey)) {
			request.header(API_KEY_HEADER, apiKey);
		}

		final HttpResponse<OperationResponse<Boolean>> response = request.fields(params)
				.asObject(new GenericType<OperationResponse<Boolean>>() {
				});

		return this.processResponse(response);
	}

	@Override
	public IOperationResponse<Boolean> cancelOrderByPlatformId(@NonNull final String pseudoAccount,
			@NonNull final String platformId) {
		return this.cancelOrderByPlatformId(null, pseudoAccount, platformId);
	}

	@Override
	public IOperationResponse<Boolean> cancelOrderByPlatformId(final String apiKey, final String pseudoAccount,
			final String platformId) {
		return this.cancelGeneric(this.cancelOrderByPlatformIdUrl, apiKey, pseudoAccount, platformId);
	}

	@Override
	public IOperationResponse<Boolean> cancelChildOrdersByPlatformId(final String pseudoAccount,
			final String platformId) {
		return this.cancelChildOrdersByPlatformId(null, pseudoAccount, platformId);
	}

	@Override
	public IOperationResponse<Boolean> cancelChildOrdersByPlatformId(final String apiKey, final String pseudoAccount,
			final String platformId) {
		return this.cancelGeneric(this.cancelChildOrdersByPlatformIdUrl, apiKey, pseudoAccount, platformId);
	}

	@Override
	public IOperationResponse<Boolean> squareOffPosition(final String pseudoAccount, final PositionCategory category,
			final PositionType type, final String exchange, final String symbol) {
		return this.squareOffPosition(null, pseudoAccount, category, type, exchange, symbol);
	}

	@Override
	public IOperationResponse<Boolean> squareOffPosition(final String apiKey, final String pseudoAccount,
			final PositionCategory category, final PositionType type, final String exchange, final String symbol) {
		final Map<String, Object> params = new HashMap<>();
		params.put("pseudoAccount", pseudoAccount);
		params.put("category", category);
		params.put("type", type);
		params.put("exchange", exchange);
		params.put("symbol", symbol);

		final HttpRequestWithBody request = this.client.post(this.squareOffPositionUrl);
		if (!isEmpty(apiKey)) {
			request.header(API_KEY_HEADER, apiKey);
		}

		final HttpResponse<OperationResponse<Boolean>> response = request.fields(params)
				.asObject(new GenericType<OperationResponse<Boolean>>() {
				});

		return this.processResponse(response);
	}

	@Override
	public IOperationResponse<Boolean> squareOffPortfolio(final String pseudoAccount, final PositionCategory category) {
		return this.squareOffPortfolio(null, pseudoAccount, category);
	}

	@Override
	public IOperationResponse<Boolean> squareOffPortfolio(final String apiKey, final String pseudoAccount,
			final PositionCategory category) {
		final Map<String, Object> params = new HashMap<>();
		params.put("pseudoAccount", pseudoAccount);
		params.put("category", category);

		final HttpRequestWithBody request = this.client.post(this.squareOffPortfolioUrl);
		if (!isEmpty(apiKey)) {
			request.header(API_KEY_HEADER, apiKey);
		}

		final HttpResponse<OperationResponse<Boolean>> response = request.fields(params)
				.asObject(new GenericType<OperationResponse<Boolean>>() {
				});

		return this.processResponse(response);
	}

	@Override
	public IOperationResponse<Set<PlatformOrder>> readPlatformOrders(@NonNull final String pseudoAccount) {
		final HttpResponse<OperationResponse<Set<PlatformOrder>>> response = this.client
				.post(this.readPlatformOrdersUrl).field("pseudoAccount", pseudoAccount)
				.asObject(new GenericType<OperationResponse<Set<PlatformOrder>>>() {
				});

		return this.processResponse(response);
	}

	@Override
	public IOperationResponse<Set<PlatformPosition>> readPlatformPositions(@NonNull final String pseudoAccount) {
		final HttpResponse<OperationResponse<Set<PlatformPosition>>> response = this.client
				.post(this.readPlatformPositionsUrl).field("pseudoAccount", pseudoAccount)
				.asObject(new GenericType<OperationResponse<Set<PlatformPosition>>>() {
				});

		return this.processResponse(response);
	}

	@Override
	public IOperationResponse<Set<PlatformMargin>> readPlatformMargins(@NonNull final String pseudoAccount) {
		final HttpResponse<OperationResponse<Set<PlatformMargin>>> response = this.client
				.post(this.readPlatformMarginsUrl).field("pseudoAccount", pseudoAccount)
				.asObject(new GenericType<OperationResponse<Set<PlatformMargin>>>() {
				});

		return this.processResponse(response);
	}

	private IOperationResponse<String> postOrder(final String url, final Map<String, Object> params) {
		final HttpResponse<OperationResponse<String>> response = this.client.post(url).fields(params)
				.asObject(new GenericType<OperationResponse<String>>() {
				});

		return this.processResponse(response);
	}

	private final <T> IOperationResponse<T> processResponse(final HttpResponse<OperationResponse<T>> response) {
		if (response == null) {
			return OperationResponse.<T>builder().error(new Exception("Null response received from server")).build();
		}
		if (!response.isSuccess()) {
			// Process parsing errors only when http status code is 200, otherwise for other
			// errors there will always be parsing errors
			if ((response.getStatus() == 200) && response.getParsingError().isPresent()) {
				return this.processParsingError(response.getParsingError().get());
			} else {
				return this.processHttpError(response);
			}
		}

		// If no errors, then return original response
		return response.getBody();
	}

	private final <T> IOperationResponse<T> processParsingError(final UnirestParsingException pe) {
		return OperationResponse.<T>builder().error(pe).build();
	}

	private final <T> IOperationResponse<T> processHttpError(final HttpResponse<?> response) {
		final int status = response.getStatus();

		final String message = (status == 403) ? "Your API Key is wrong. Please check settings."
				: toTextDefault(status, response.getStatusText());

		final Exception error = new Exception(status + ": " + message);
		return OperationResponse.<T>builder().error(error).build();
	}

	private IOperationResponse<Boolean> cancelGeneric(final String url, final String apiKey, final String pseudoAccount,
			final String platformId) {
		final Map<String, Object> params = new HashMap<>();
		params.put("pseudoAccount", pseudoAccount);
		params.put("platformId", platformId);

		final HttpRequestWithBody request = this.client.post(url);
		if (!isEmpty(apiKey)) {
			request.header(API_KEY_HEADER, apiKey);
		}

		final HttpResponse<OperationResponse<Boolean>> response = request.fields(params)
				.asObject(new GenericType<OperationResponse<Boolean>>() {
				});

		return this.processResponse(response);
	}

}
