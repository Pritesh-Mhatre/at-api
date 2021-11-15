/**
 *
 */
package com.dakshata.autotrader.api;

import static com.dakshata.constants.autotrader.IAutoTrader.API_KEY_HEADER;
import static java.util.Collections.synchronizedMap;
import static kong.unirest.Config.DEFAULT_CONNECTION_TIMEOUT;
import static kong.unirest.Config.DEFAULT_SOCKET_TIMEOUT;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.dakshata.constants.trading.OrderType;
import com.dakshata.constants.trading.PositionCategory;
import com.dakshata.constants.trading.PositionType;
import com.dakshata.constants.trading.ProductType;
import com.dakshata.constants.trading.TradeType;
import com.dakshata.data.model.common.IOperationResponse;
import com.dakshata.trading.model.platform.PlatformHolding;
import com.dakshata.trading.model.platform.PlatformMargin;
import com.dakshata.trading.model.platform.PlatformOrder;
import com.dakshata.trading.model.platform.PlatformPosition;
import com.dakshata.trading.model.portfolio.Order;

import kong.unirest.Config;
import kong.unirest.JacksonObjectMapper;
import kong.unirest.UnirestInstance;
import lombok.NonNull;
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

	private static final Map<String, AutoTrader> INSTANCES = synchronizedMap(new HashMap<>());

	private static final int MAX_CONNECTIONS = 250;

	private static final int MAX_CONN_PER_ROUTE = 200;

	private final TradingService tradingService;

	/**
	 * Initialize the AutoTrader API with your private API key.
	 *
	 * @param apiKey     your private api key
	 * @param serviceUrl AutoTrader api service url
	 */
	private AutoTrader(final String apiKey, final String serviceUrl) {
		super();
		this.tradingService = new TradingService(serviceUrl, this.prepareClient(apiKey));
	}

	public static final synchronized IAutoTrader createInstance(@NonNull final String apiKey,
			@NonNull final String serviceUrl) {
		AutoTrader instance = INSTANCES.get(apiKey);
		if (instance == null) {
			instance = new AutoTrader(apiKey, serviceUrl);
			INSTANCES.put(apiKey, instance);
		}

		return instance;
	}

	@Override
	public IOperationResponse<Set<String>> fetchLivePseudoAccounts() {
		return this.tradingService.fetchLivePseudoAccounts();
	}

	@Override
	public IOperationResponse<? extends Object> execute(@NonNull final String command) {
		return this.tradingService.execute(command);
	}

	@Override
	public IOperationResponse<String> placeOrder(@NonNull final Order order) {
		return this.tradingService.placeOrder(order);
	}

	@Override
	public IOperationResponse<String> placeRegularOrder(@NonNull final String pseudoAccount,
			final @NonNull String exchange, @NonNull final String symbol, @NonNull final TradeType tradeType,
			@NonNull final OrderType orderType, @NonNull final ProductType productType, final int quantity,
			final float price, final float triggerPrice) {
		return this.tradingService.placeRegularOrder(pseudoAccount, exchange, symbol, tradeType, orderType, productType,
				quantity, price, triggerPrice);
	}

	@Override
	public IOperationResponse<String> placeBracketOrder(@NonNull final String pseudoAccount,
			@NonNull final String exchange, @NonNull final String symbol, @NonNull final TradeType tradeType,
			@NonNull final OrderType orderType, final int quantity, final float price, final float triggerPrice,
			final float target, final float stoploss, final float trailingStoploss) {
		return this.tradingService.placeBracketOrder(pseudoAccount, exchange, symbol, tradeType, orderType, quantity,
				price, triggerPrice, target, stoploss, trailingStoploss);
	}

	@Override
	public IOperationResponse<String> placeCoverOrder(@NonNull final String pseudoAccount,
			@NonNull final String exchange, @NonNull final String symbol, @NonNull final TradeType tradeType,
			@NonNull final OrderType orderType, final int quantity, final float price, final float triggerPrice) {
		return this.tradingService.placeCoverOrder(pseudoAccount, exchange, symbol, tradeType, orderType, quantity,
				price, triggerPrice);
	}

	@Override
	public IOperationResponse<Boolean> cancelAllOrders(final String pseudoAccount) {
		return this.tradingService.cancelAllOrders(pseudoAccount);
	}

	@Override
	public IOperationResponse<Boolean> cancelOrderByPlatformId(@NonNull final String pseudoAccount,
			@NonNull final String platformId) {
		return this.tradingService.cancelOrderByPlatformId(pseudoAccount, platformId);
	}

	@Override
	public IOperationResponse<Boolean> cancelChildOrdersByPlatformId(@NonNull final String pseudoAccount,
			@NonNull final String platformId) {
		return this.tradingService.cancelChildOrdersByPlatformId(pseudoAccount, platformId);
	}

	@Override
	public IOperationResponse<Boolean> modifyOrderByPlatformId(final String pseudoAccount, final String platformId,
			final OrderType orderType, final Integer quantity, final Float price, final Float triggerPrice) {
		return this.tradingService.modifyOrderByPlatformId(pseudoAccount, platformId, orderType, quantity, price,
				triggerPrice);
	}

	@Override
	public IOperationResponse<Boolean> squareOffPosition(final String pseudoAccount, final PositionCategory category,
			final PositionType type, final String exchange, final String symbol) {
		return this.tradingService.squareOffPosition(pseudoAccount, category, type, exchange, symbol);
	}

	@Override
	public IOperationResponse<Boolean> squareOffPortfolio(final String pseudoAccount, final PositionCategory category) {
		return this.tradingService.squareOffPortfolio(pseudoAccount, category);
	}

	@Override
	public IOperationResponse<Set<PlatformOrder>> readPlatformOrders(@NonNull final String pseudoAccount) {
		return this.tradingService.readPlatformOrders(pseudoAccount);
	}

	@Override
	public IOperationResponse<Set<PlatformPosition>> readPlatformPositions(@NonNull final String pseudoAccount) {
		return this.tradingService.readPlatformPositions(pseudoAccount);
	}

	@Override
	public IOperationResponse<Set<PlatformMargin>> readPlatformMargins(@NonNull final String pseudoAccount) {
		return this.tradingService.readPlatformMargins(pseudoAccount);
	}

	@Override
	public IOperationResponse<Set<PlatformHolding>> readPlatformHoldings(@NonNull final String pseudoAccount) {
		return this.tradingService.readPlatformHoldings(pseudoAccount);
	}

	@Override
	public synchronized void setApiKey(@NonNull final String apiKey) {
		this.shutdownClient(this.tradingService.getClient());
		final UnirestInstance client = this.prepareClient(apiKey);
		this.tradingService.setClient(client);
	}

	@Override
	public IOperationResponse<String> autoTraderDesktopVersion() {
		return this.tradingService.autoTraderDesktopVersion();
	}

	@Override
	public IOperationResponse<String> autoTraderDesktopMinVersion() {
		return this.tradingService.autoTraderDesktopMinVersion();
	}

	/**
	 * Graceful shutdown. Call when your application is about to terminate.
	 */
	@Override
	public void shutdown() {
		this.shutdownClient(this.tradingService.getClient());
	}

	private final UnirestInstance prepareClient(final String apiKey) {
		final Config config = new Config();
		config.setDefaultHeader(API_KEY_HEADER, apiKey);
		// Spring boot uses Jackson by default, hence we use jackson here
		config.setObjectMapper(new JacksonObjectMapper());
		config.connectTimeout(DEFAULT_CONNECTION_TIMEOUT * 3);
		config.socketTimeout(DEFAULT_SOCKET_TIMEOUT * 2);
		config.concurrency(MAX_CONNECTIONS, MAX_CONN_PER_ROUTE);
		// Disable ssl verification to improve performance (as we are connecting to our
		// own servers). This will not stop ssl connection, it will only skip
		// verification.
		config.verifySsl(false);
		return new UnirestInstance(config);
	}

	public void shutdownClient(final UnirestInstance client) {
		try {
			if (client != null) {
				client.shutDown();
			}
		} catch (final Exception e) {
			log.error("Error while shutting down client: ", e);
		}
	}
}
