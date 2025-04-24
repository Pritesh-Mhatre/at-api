/**
 *
 */
package com.dakshata.autotrader.api;

import static com.dakshata.autotrader.api.AutoTraderClientConfig.defaultConfig;
import static com.dakshata.constants.autotrader.IAutoTrader.API_KEY_HEADER;
import static java.util.Collections.synchronizedMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.http.NoHttpResponseException;

import com.dakshata.constants.trading.*;
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

	private final TradingService tradingService;

	private final boolean autoRetryOnError;

	/**
	 * Initialize the AutoTrader API with your private API key.
	 *
	 * @param apiKey     your private api key
	 * @param serviceUrl AutoTrader api service url
	 */
	private AutoTrader(@NonNull final AutoTraderClientConfig config) {
		this.autoRetryOnError = config.isAutoRetryOnError();
		this.tradingService = new TradingService(config.getServiceUrl(), this.prepareClient(config));
	}

	public static final synchronized IAutoTrader createInstance(@NonNull final AutoTraderClientConfig config) {
		AutoTrader instance = INSTANCES.get(config.getApiKey());
		if (instance == null) {
			instance = new AutoTrader(config);
			INSTANCES.put(config.getApiKey(), instance);
		}
		return instance;
	}

	public static final IAutoTrader createInstance(@NonNull final String apiKey) {
		return createInstance(defaultConfig(apiKey));
	}

	@Override
	public IOperationResponse<Set<String>> fetchLivePseudoAccounts() {
		return this.executeWithRetry(() -> this.tradingService.fetchLivePseudoAccounts());
	}

	@Override
	public IOperationResponse<? extends Object> execute(@NonNull final String command) {
		return this.executeWithRetry(() -> this.tradingService.execute(command));
	}

	@Override
	public IOperationResponse<String> placeOrder(@NonNull final Order order) {
		return this.executeWithRetry(() -> this.tradingService.placeOrder(order));
	}

	@Override
	public IOperationResponse<String> placeRegularOrder(@NonNull final String pseudoAccount,
			final @NonNull String exchange, @NonNull final String symbol, @NonNull final TradeType tradeType,
			@NonNull final OrderType orderType, @NonNull final ProductType productType, final int quantity,
			final float price, final float triggerPrice) {
		return this.executeWithRetry(() -> this.tradingService.placeRegularOrder(pseudoAccount, exchange, symbol,
				tradeType, orderType, productType, quantity, price, triggerPrice));
	}

	@Override
	public IOperationResponse<String> placeBracketOrder(@NonNull final String pseudoAccount,
			@NonNull final String exchange, @NonNull final String symbol, @NonNull final TradeType tradeType,
			@NonNull final OrderType orderType, final int quantity, final float price, final float triggerPrice,
			final float target, final float stoploss, final float trailingStoploss) {
		return this.executeWithRetry(() -> this.tradingService.placeBracketOrder(pseudoAccount, exchange, symbol,
				tradeType, orderType, quantity, price, triggerPrice, target, stoploss, trailingStoploss));
	}

	@Override
	public IOperationResponse<String> placeCoverOrder(@NonNull final String pseudoAccount,
			@NonNull final String exchange, @NonNull final String symbol, @NonNull final TradeType tradeType,
			@NonNull final OrderType orderType, final int quantity, final float price, final float triggerPrice) {
		return this.executeWithRetry(() -> this.tradingService.placeCoverOrder(pseudoAccount, exchange, symbol,
				tradeType, orderType, quantity, price, triggerPrice));
	}

	@Override
	public IOperationResponse<Boolean> cancelAllOrders(final String pseudoAccount) {
		return this.executeWithRetry(() -> this.tradingService.cancelAllOrders(pseudoAccount));
	}

	@Override
	public IOperationResponse<Boolean> cancelOrderByPlatformId(@NonNull final String pseudoAccount,
			@NonNull final String platformId) {
		return this.executeWithRetry(() -> this.tradingService.cancelOrderByPlatformId(pseudoAccount, platformId));
	}

	@Override
	public IOperationResponse<Boolean> cancelChildOrdersByPlatformId(@NonNull final String pseudoAccount,
			@NonNull final String platformId) {
		return this
				.executeWithRetry(() -> this.tradingService.cancelChildOrdersByPlatformId(pseudoAccount, platformId));
	}

	@Override
	public IOperationResponse<Boolean> modifyOrderByPlatformId(final String pseudoAccount, final String platformId,
			final OrderType orderType, final Integer quantity, final Float price, final Float triggerPrice) {
		return this.executeWithRetry(() -> this.tradingService.modifyOrderByPlatformId(pseudoAccount, platformId,
				orderType, quantity, price, triggerPrice));
	}

	@Override
	public IOperationResponse<Boolean> squareOffPosition(final String pseudoAccount, final PositionCategory category,
			final PositionType type, final String exchange, final String symbol, boolean cancelOpenOrders) {
		return this.executeWithRetry(() -> this.tradingService.squareOffPosition(pseudoAccount, category, type,
				exchange, symbol, cancelOpenOrders));
	}

	@Override
	public IOperationResponse<Boolean> squareOffPortfolio(final String pseudoAccount, final PositionCategory category,
			boolean cancelOpenOrders) {
		return this.executeWithRetry(
				() -> this.tradingService.squareOffPortfolio(pseudoAccount, category, cancelOpenOrders));
	}

	@Override
	public IOperationResponse<Set<PlatformOrder>> readPlatformOrders(@NonNull final String pseudoAccount) {
		return this.executeWithRetry(() -> this.tradingService.readPlatformOrders(pseudoAccount));
	}

	@Override
	public IOperationResponse<Set<PlatformPosition>> readPlatformPositions(@NonNull final String pseudoAccount) {
		return this.executeWithRetry(() -> this.tradingService.readPlatformPositions(pseudoAccount));
	}

	@Override
	public IOperationResponse<Set<PlatformMargin>> readPlatformMargins(@NonNull final String pseudoAccount) {
		return this.executeWithRetry(() -> this.tradingService.readPlatformMargins(pseudoAccount));
	}

	@Override
	public IOperationResponse<Set<PlatformHolding>> readPlatformHoldings(@NonNull final String pseudoAccount) {
		return this.executeWithRetry(() -> this.tradingService.readPlatformHoldings(pseudoAccount));
	}

	@Override
	public IOperationResponse<String> autoTraderDesktopVersion() {
		return this.executeWithRetry(() -> this.tradingService.autoTraderDesktopVersion());
	}

	@Override
	public IOperationResponse<String> autoTraderDesktopMinVersion() {
		return this.executeWithRetry(() -> this.tradingService.autoTraderDesktopMinVersion());
	}

	/**
	 * Graceful shutdown. Call when your application is about to terminate.
	 */
	@Override
	public void shutdown() {
		this.shutdownClient(this.tradingService.getClient());
	}

	private final UnirestInstance prepareClient(final AutoTraderClientConfig atConfig) {
		final Config config = new Config();
		config.setDefaultHeader(API_KEY_HEADER, atConfig.getApiKey());
		// Spring boot uses Jackson by default, hence we use jackson here
		config.setObjectMapper(new JacksonObjectMapper());
		config.connectTimeout(atConfig.getConnectTimeout());
		config.socketTimeout(atConfig.getSocketTimeout());
		config.concurrency(atConfig.getMaxConnections(), atConfig.getMaxConnectionsPerRoute());
		// Disable ssl verification to improve performance (as we are connecting to our
		// own servers). This will not stop ssl connection, it will only skip
		// verification.
		config.verifySsl(false);
		return new UnirestInstance(config);
	}

	private void shutdownClient(final UnirestInstance client) {
		try {
			if (client != null) {
				client.shutDown();
			}
		} catch (final Exception e) {
			log.error("Error while shutting down client: ", e);
		}
	}

	private <R> R executeWithRetry(final Supplier<R> f) {
		try {
			return f.get();
		} catch (final Exception e) {
			if (this.shouldRetry(e)) {
				// Retry
				log.error("SD-ERR-048: Retrying on error: {}", e.getMessage());
				return f.get();
			} else {
				throw e;
			}
		}
	}

	private boolean shouldRetry(final Exception e) {
		if (!this.autoRetryOnError) {
			return false;
		}

		final Throwable c = e.getCause();

		// It is generally considered safe to retry on NoHttpResponseException
		// https://hc.apache.org/httpclient-legacy/exception-handling.html
		boolean result = (e instanceof NoHttpResponseException);
		if (!result && (c != null)) {
			result = (c instanceof NoHttpResponseException);
		}

		if (!result && (c.getCause() != null)) {
			result = (c.getCause() instanceof NoHttpResponseException);
		}

		return result;
	}

}
