/**
 *
 */
package com.dakshata.autotrader.api;

import static com.dakshata.constants.ErrorCode.SYSTEM_FORBIDDEN;
import static com.dakshata.constants.autotrader.IAutoTrader.API_KEY_HEADER;
import static com.dakshata.constants.trading.Variety.REGULAR;
import static com.dakshata.tools.internet.HttpStatus.toTextDefault;
import static com.dakshata.tools.string.StringUtil.isEmpty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dakshata.constants.trading.*;
import com.dakshata.data.model.autotrader.web.AdjustHoldingsRequest;
import com.dakshata.data.model.autotrader.web.AdjustHoldingsResponse;
import com.dakshata.data.model.common.IOperationResponse;
import com.dakshata.data.model.common.OperationResponse;
import com.dakshata.trading.model.platform.PlatformHolding;
import com.dakshata.trading.model.platform.PlatformMargin;
import com.dakshata.trading.model.platform.PlatformOrder;
import com.dakshata.trading.model.platform.PlatformPosition;
import com.dakshata.trading.model.portfolio.IOrder;
import com.dakshata.trading.model.tv.order.TvOrder;
import com.dakshata.trading.model.tv.position.TvPosSqOff;

import kong.unirest.*;
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

	private final String readPlatformOrdersUrl, readPlatformPositionsUrl, readPlatformMarginsUrl,
			readPlatformHoldingsUrl;

	private final String placeOrderUrl, placeTvOrderUrl, placeRegularOrderUrl, placeCoverOrderUrl, placeBracketOrderUrl,
			placeAdvancedOrderUrl;

	private final String cancelOrderByPlatformIdUrl, modifyOrderByPlatformIdUrl;

	private final String cancelChildOrdersByPlatformIdUrl, cancelAllOrdersUrl;

	private final String squareOffPositionUrl, squareOffTvPositionUrl, squareOffPortfolioUrl, adjustHoldingsUrl;

	private final String autoTraderDesktopVersionUrl, autoTraderDesktopMinVersionUrl;

	@Getter
	@Setter
	private UnirestInstance client;

	public TradingService(final String serviceUrl, final UnirestInstance client, final boolean autoRetryOnError) {
		this.client = client;
		this.commandUrl = serviceUrl + COMMAND_URI + "/execute";
		this.readPlatformOrdersUrl = serviceUrl + TRADING_URI + "/readPlatformOrders";
		this.readPlatformPositionsUrl = serviceUrl + TRADING_URI + "/readPlatformPositions";
		this.readPlatformMarginsUrl = serviceUrl + TRADING_URI + "/readPlatformMargins";
		this.readPlatformHoldingsUrl = serviceUrl + TRADING_URI + "/readPlatformHoldings";
		this.placeOrderUrl = serviceUrl + TRADING_URI + "/placeOrder";
		this.placeTvOrderUrl = serviceUrl + TRADING_URI + "/placeTvOrder";
		this.placeRegularOrderUrl = serviceUrl + TRADING_URI + "/placeRegularOrder";
		this.placeCoverOrderUrl = serviceUrl + TRADING_URI + "/placeCoverOrder";
		this.placeBracketOrderUrl = serviceUrl + TRADING_URI + "/placeBracketOrder";
		this.placeAdvancedOrderUrl = serviceUrl + TRADING_URI + "/placeAdvancedOrder";
		this.cancelOrderByPlatformIdUrl = serviceUrl + TRADING_URI + "/cancelOrderByPlatformId";
		this.cancelChildOrdersByPlatformIdUrl = serviceUrl + TRADING_URI + "/cancelChildOrdersByPlatformId";
		this.cancelAllOrdersUrl = serviceUrl + TRADING_URI + "/cancelAllOrders";
		this.modifyOrderByPlatformIdUrl = serviceUrl + TRADING_URI + "/modifyOrderByPlatformId";
		this.livePseudoAccountsUrl = serviceUrl + ACCOUNT_URI + "/fetchLivePseudoAccounts";
		this.squareOffPositionUrl = serviceUrl + TRADING_URI + "/squareOffPosition";
		this.squareOffTvPositionUrl = serviceUrl + TRADING_URI + "/squareOffTvPosition";
		this.squareOffPortfolioUrl = serviceUrl + TRADING_URI + "/squareOffPortfolio";
		this.adjustHoldingsUrl = serviceUrl + TRADING_URI + "/adjustHoldings";
		this.autoTraderDesktopVersionUrl = serviceUrl + TRADING_URI + "/autoTraderDesktopVersion";
		this.autoTraderDesktopMinVersionUrl = serviceUrl + TRADING_URI + "/autoTraderDesktopMinVersion";
	}

	public TradingService(final String serviceUrl, final UnirestInstance client) {
		this(serviceUrl, client, false);
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

		if (response == null) {
			return OperationResponse.<Object>builder().error(new Exception("Null response received from server"))
					.build();
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
	public IOperationResponse<Boolean> placeTvOrder(@NonNull final String apiKey, final TvOrder order) {
		final HttpRequestWithBody request = this.client.post(this.placeTvOrderUrl);
		request.header(API_KEY_HEADER, apiKey);

		final HttpResponse<OperationResponse<Boolean>> response = request.header("Content-Type", "application/json")
				.body(order).asObject(new GenericType<OperationResponse<Boolean>>() {
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
		return this.modifyOrderGeneric(apiKey, pseudoAccount, platformId, orderType, quantity, price, triggerPrice,
				null);
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
	public IOperationResponse<Boolean> cancelAllOrders(final String pseudoAccount) {
		return this.cancelAllOrders(null, pseudoAccount);
	}

	@Override
	public IOperationResponse<Boolean> cancelAllOrders(final String apiKey, final String pseudoAccount) {
		return this.cancelGeneric(this.cancelAllOrdersUrl, apiKey, pseudoAccount, null);
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
	public IOperationResponse<Boolean> squareOffTvPosition(@NonNull final String apiKey,
			@NonNull final TvPosSqOff input) {
		final HttpRequestWithBody request = this.client.post(this.squareOffTvPositionUrl);
		request.header(API_KEY_HEADER, apiKey);

		final HttpResponse<OperationResponse<Boolean>> response = request.header("Content-Type", "application/json")
				.body(input).asObject(new GenericType<OperationResponse<Boolean>>() {
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
	public IOperationResponse<Set<PlatformOrder>> readPlatformOrders(@NonNull final String apiKey,
			@NonNull final String pseudoAccount) {
		final HttpResponse<OperationResponse<Set<PlatformOrder>>> response = this.client
				.post(this.readPlatformOrdersUrl).field("pseudoAccount", pseudoAccount).header(API_KEY_HEADER, apiKey)
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

	@Override
	public IOperationResponse<Set<PlatformHolding>> readPlatformHoldings(@NonNull final String pseudoAccount) {
		final HttpResponse<OperationResponse<Set<PlatformHolding>>> response = this.client
				.post(this.readPlatformHoldingsUrl).field("pseudoAccount", pseudoAccount)
				.asObject(new GenericType<OperationResponse<Set<PlatformHolding>>>() {
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

		final String message = (status == 403) ? "Either your API Key is wrong or the user might be disabled."
				: toTextDefault(status, response.getStatusText());

		final Exception error = new Exception(status + ": " + message);
		return OperationResponse.<T>builder().error(error).errorCode(SYSTEM_FORBIDDEN).build();
	}

	private IOperationResponse<Boolean> cancelGeneric(final String url, final String apiKey, final String pseudoAccount,
			final String platformId) {
		return this.cancelGeneric(url, apiKey, pseudoAccount, platformId, null);
	}

	private IOperationResponse<Boolean> cancelGeneric(final String url, final String apiKey, final String pseudoAccount,
			final String platformId, final String commandId) {
		final Map<String, Object> params = new HashMap<>();
		params.put("pseudoAccount", pseudoAccount);
		if (platformId != null) {
			params.put("platformId", platformId);
		}
		if (commandId != null) {
			params.put("commandId", commandId);
		}

		final HttpRequestWithBody request = this.client.post(url);
		if (!isEmpty(apiKey)) {
			request.header(API_KEY_HEADER, apiKey);
		}

		final HttpResponse<OperationResponse<Boolean>> response = request.fields(params)
				.asObject(new GenericType<OperationResponse<Boolean>>() {
				});

		return this.processResponse(response);
	}

	@Override
	public IOperationResponse<String> autoTraderDesktopVersion() {
		final HttpResponse<OperationResponse<String>> response = this.client.get(this.autoTraderDesktopVersionUrl)
				.asObject(new GenericType<OperationResponse<String>>() {
				});
		return this.processResponse(response);
	}

	@Override
	public IOperationResponse<String> autoTraderDesktopMinVersion() {
		final HttpResponse<OperationResponse<String>> response = this.client.get(this.autoTraderDesktopMinVersionUrl)
				.asObject(new GenericType<OperationResponse<String>>() {
				});
		return this.processResponse(response);
	}

	@Override
	public IOperationResponse<List<AdjustHoldingsResponse>> adjustHoldings(final String apiKey,
			final AdjustHoldingsRequest input) {
		final HttpRequestWithBody request = this.client.post(this.adjustHoldingsUrl);
		request.header(API_KEY_HEADER, apiKey);

		final HttpResponse<OperationResponse<List<AdjustHoldingsResponse>>> response = request
				.header("Content-Type", "application/json").body(input)
				.asObject(new GenericType<OperationResponse<List<AdjustHoldingsResponse>>>() {
				});

		return this.processResponse(response);
	}

	@Override
	public IOperationResponse<String> placeOrderMCA(final String apiKey, final String pseudoAccount,
			final String exchange, final String symbol, final TradeType tradeType, final OrderType orderType,
			final ProductType productType, final int quantity, final float price, final float triggerPrice,
			final Validity validity, final Boolean amo, final String publisherId, final String commandId) {
		final Map<String, Object> params = new HashMap<>();
		params.put("variety", REGULAR);
		params.put("pseudoAccount", pseudoAccount);
		params.put("exchange", exchange);
		params.put("symbol", symbol);
		params.put("tradeType", tradeType);
		params.put("orderType", orderType);
		params.put("productType", productType);
		params.put("quantity", quantity);
		params.put("price", price);
		params.put("triggerPrice", triggerPrice);
		params.put("validity", validity);
		params.put("amo", amo);
		params.put("publisherId", publisherId);
		params.put("commandId", commandId);

		final HttpResponse<OperationResponse<String>> response = this.client.post(this.placeAdvancedOrderUrl)
				.header(API_KEY_HEADER, apiKey).fields(params).asObject(new GenericType<OperationResponse<String>>() {
				});

		return this.processResponse(response);
	}

	@Override
	public IOperationResponse<Boolean> cancelOrderMCA(final String apiKey, final String pseudoAccount,
			final String platformId, final String commandId) {
		return this.cancelGeneric(this.cancelOrderByPlatformIdUrl, apiKey, pseudoAccount, platformId, commandId);
	}

	@Override
	public IOperationResponse<Boolean> modifyOrderMCA(final String apiKey, final String pseudoAccount,
			final String platformId, final OrderType orderType, final Integer quantity, final Float price,
			final Float triggerPrice, final String commandId) {
		return this.modifyOrderGeneric(apiKey, pseudoAccount, platformId, orderType, quantity, price, triggerPrice,
				commandId);
	}

	private IOperationResponse<Boolean> modifyOrderGeneric(final String apiKey, @NonNull final String pseudoAccount,
			@NonNull final String platformId, final OrderType orderType, final Integer quantity, final Float price,
			final Float triggerPrice, final String commandId) {
		final Map<String, Object> params = new HashMap<>();
		params.put("pseudoAccount", pseudoAccount);
		params.put("platformId", platformId);
		params.put("orderType", (orderType == null) ? "" : orderType.name());
		params.put("quantity", quantity);
		params.put("price", price);
		params.put("triggerPrice", triggerPrice);
		if (!isEmpty(commandId)) {
			params.put("commandId", commandId);
		}

		final HttpRequestWithBody request = this.client.post(this.modifyOrderByPlatformIdUrl);
		if (!isEmpty(apiKey)) {
			request.header(API_KEY_HEADER, apiKey);
		}

		final HttpResponse<OperationResponse<Boolean>> response = request.fields(params)
				.asObject(new GenericType<OperationResponse<Boolean>>() {
				});

		return this.processResponse(response);
	}

}
