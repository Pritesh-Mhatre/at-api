/**
 *
 */
package com.dakshata.autotrader.api;

import java.util.List;
import java.util.Set;

import com.dakshata.constants.trading.*;
import com.dakshata.data.model.autotrader.web.AdjustHoldingsRequest;
import com.dakshata.data.model.autotrader.web.AdjustHoldingsResponse;
import com.dakshata.data.model.common.IOperationResponse;
import com.dakshata.trading.model.platform.PlatformHolding;
import com.dakshata.trading.model.platform.PlatformMargin;
import com.dakshata.trading.model.platform.PlatformOrder;
import com.dakshata.trading.model.platform.PlatformPosition;
import com.dakshata.trading.model.portfolio.IOrder;
import com.dakshata.trading.model.tv.order.TvOrder;
import com.dakshata.trading.model.tv.position.TvPosSqOff;

/**
 * Responsible for trading activities.
 *
 * @author PRITESH
 *
 */
public interface ITradingService {

	/**
	 * Provides live pseudo accounts available under your user.
	 *
	 * @return live pseudo accounts
	 */
	IOperationResponse<Set<String>> fetchLivePseudoAccounts();

	/**
	 * Places an order. For more information, please see <a href=
	 * "https://stocksdeveloper.in/documentation/api/place-advanced-order/">api
	 * docs</a>.
	 *
	 * @param order order object
	 * @return the order id given by your stock broker
	 */
	IOperationResponse<String> placeOrder(IOrder order);

	IOperationResponse<String> placeOrder(String apiKey, IOrder order);

	/**
	 * Places an order received from Trading View. This method is not supposed to be
	 * used by outside world.
	 *
	 * @param apiKey api key
	 * @param order  order object
	 * @return the order id given by your stock broker
	 */
	IOperationResponse<Boolean> placeTvOrder(String apiKey, TvOrder order);

	/**
	 * Places a regular order. For more information, please see <a href=
	 * "https://stocksdeveloper.in/documentation/api/place-regular-order/">api
	 * docs</a>.
	 *
	 * @param pseudoAccount pseudo account
	 * @param exchange      exchange
	 * @param symbol        symbol
	 * @param tradeType     trade type
	 * @param orderType     order type
	 * @param productType   product type
	 * @param quantity      quantity
	 * @param price         price
	 * @param triggerPrice  trigger price
	 * @return the order id given by your stock broker
	 */
	IOperationResponse<String> placeRegularOrder(final String pseudoAccount, String exchange, String symbol,
			TradeType tradeType, OrderType orderType, ProductType productType, int quantity, float price,
			float triggerPrice);

	/**
	 * Places a bracket order. For more information, please see <a href=
	 * "https://stocksdeveloper.in/documentation/api/place-bracket-order/">api
	 * docs</a>.
	 *
	 * @param pseudoAccount    pseudo account
	 * @param exchange         exchange
	 * @param symbol           symbol
	 * @param tradeType        trade type
	 * @param orderType        order type
	 * @param quantity         quantity
	 * @param price            price
	 * @param triggerPrice     trigger price
	 * @param target           target
	 * @param stoploss         stoploss
	 * @param trailingStoploss trailing stoploss
	 * @return the order id given by your stock broker
	 */
	IOperationResponse<String> placeBracketOrder(String pseudoAccount, String exchange, String symbol,
			TradeType tradeType, OrderType orderType, int quantity, float price, float triggerPrice, float target,
			float stoploss, float trailingStoploss);

	/**
	 * Places a cover order. For more information, please see <a href=
	 * "https://stocksdeveloper.in/documentation/api/place-cover-order/">api
	 * docs</a>.
	 *
	 * @param pseudoAccount pseudo account
	 * @param exchange      exchange
	 * @param symbol        symbol
	 * @param tradeType     trade type
	 * @param orderType     order type
	 * @param quantity      quantity
	 * @param price         price
	 * @param triggerPrice  trigger price
	 * @return the order id given by your stock broker
	 */
	IOperationResponse<String> placeCoverOrder(String pseudoAccount, String exchange, String symbol,
			TradeType tradeType, OrderType orderType, int quantity, float price, float triggerPrice);

	/**
	 * Cancels all open orders for the given account. For more information, please
	 * see
	 * <a href="https://stocksdeveloper.in/documentation/api/cancel-all-orders/">api
	 * docs</a>.
	 *
	 * @param pseudoAccount pseudo account
	 * @return <code>true</code> on success, <code>false</code> otherwise
	 */
	IOperationResponse<Boolean> cancelAllOrders(String pseudoAccount);

	IOperationResponse<Boolean> cancelAllOrders(String apiKey, String pseudoAccount);

	/**
	 * Cancels an order. For more information, please see
	 * <a href="https://stocksdeveloper.in/documentation/api/cancel-order/">api
	 * docs</a>.
	 *
	 * @param pseudoAccount pseudo account
	 * @param platformId    platform id (id given to order by trading platform)
	 * @return <code>true</code> on success, <code>false</code> otherwise
	 */
	IOperationResponse<Boolean> cancelOrderByPlatformId(String pseudoAccount, String platformId);

	IOperationResponse<Boolean> cancelOrderByPlatformId(String apiKey, String pseudoAccount, String platformId);

	/**
	 * Used for exiting an open Bracket order or Cover order position. Cancels the
	 * child orders for the given parent order. For more information, please see
	 * <a href=
	 * "https://stocksdeveloper.in/documentation/api/cancel-child-orders/">api
	 * docs</a>.
	 *
	 * @param pseudoAccount pseudo account
	 * @param platformId    platform id (id given to order by trading platform)
	 * @return <code>true</code> on success, <code>false</code> otherwise
	 */
	IOperationResponse<Boolean> cancelChildOrdersByPlatformId(String pseudoAccount, String platformId);

	IOperationResponse<Boolean> cancelChildOrdersByPlatformId(String apiKey, String pseudoAccount, String platformId);

	/**
	 * Modifies the order as per the parameters passed.
	 *
	 * @param pseudoAccount pseudo account
	 * @param platformId    platform id (id given to order by trading platform)
	 * @param orderType     order type (pass null if you do not want to modify order
	 *                      type)
	 * @param quantity      quantity (pass zero if you do not want to modify
	 *                      quantity)
	 * @param price         price (pass zero if you do not want to modify price)
	 * @param triggerPrice  trigger price (pass zero if you do not want to modify
	 *                      trigger price)
	 * @return <code>true</code> on success, <code>false</code> otherwise
	 */
	IOperationResponse<Boolean> modifyOrderByPlatformId(final String pseudoAccount, final String platformId,
			final OrderType orderType, final Integer quantity, final Float price, final Float triggerPrice);

	IOperationResponse<Boolean> modifyOrderByPlatformId(final String apiKey, final String pseudoAccount,
			final String platformId, final OrderType orderType, final Integer quantity, final Float price,
			final Float triggerPrice);

	/**
	 * Submits a square-off position request.
	 *
	 * @param pseudoAccount pseudo account
	 * @param category      position category
	 * @param type          position type
	 * @param exchange      position exchange (broker independent exchange)
	 * @param symbol        position symbol (broker independent symbol)
	 * @return true on successful acceptance of square-off request, false otherwise
	 */
	IOperationResponse<Boolean> squareOffPosition(final String pseudoAccount, final PositionCategory category,
			final PositionType type, final String exchange, final String symbol, final boolean cancelOpenOrders);

	IOperationResponse<Boolean> squareOffPosition(final String apiKey, final String pseudoAccount,
			final PositionCategory category, final PositionType type, final String exchange, final String symbol,
			final boolean cancelOpenOrders);

	IOperationResponse<Boolean> squareOffTvPosition(String apiKey, TvPosSqOff input);

	/**
	 * Submits a square-off portfolio request.
	 *
	 * @param pseudoAccount pseudo account
	 * @param category      position category (DAY or NET portfolio to consider)
	 * @return true on successful acceptance of square-off request, false otherwise
	 */
	IOperationResponse<Boolean> squareOffPortfolio(final String pseudoAccount, final PositionCategory category,
			final boolean cancelOpenOrders);

	IOperationResponse<Boolean> squareOffPortfolio(final String apiKey, final String pseudoAccount,
			final PositionCategory category, final boolean cancelOpenOrders);

	/**
	 * This function executes a given command. The command can be operations like
	 * place, modify & cancel order etc. This is primarily used for AutoTrader
	 * desktop application which receives commands in csv format.
	 *
	 * @param command command in comma separated value (csv) format
	 * @return returns the result of the command
	 */
	IOperationResponse<? extends Object> execute(String command);

	/**
	 * Read trading platform orders from the trading account mapped to the given
	 * pseudo account.
	 *
	 * @param pseudoAccount pseudo account id
	 * @return orders trading platform orders
	 */
	IOperationResponse<Set<PlatformOrder>> readPlatformOrders(final String pseudoAccount);

	IOperationResponse<Set<PlatformOrder>> readPlatformOrders(String apiKey, final String pseudoAccount);

	/**
	 * Read trading platform positions from the trading account mapped to the given
	 * pseudo account.
	 *
	 * @param pseudoAccount pseudo account id
	 * @return positions trading platform positions
	 */
	IOperationResponse<Set<PlatformPosition>> readPlatformPositions(final String pseudoAccount);

	/**
	 * Read trading platform margins from the trading account mapped to the given
	 * pseudo account.
	 *
	 * @param pseudoAccount pseudo account id
	 * @return margins trading platform margins
	 */
	IOperationResponse<Set<PlatformMargin>> readPlatformMargins(final String pseudoAccount);

	/**
	 * Read trading platform holdings from the trading account mapped to the given
	 * pseudo account.
	 *
	 * @param pseudoAccount pseudo account id
	 * @return holdings trading platform holdings
	 */
	IOperationResponse<Set<PlatformHolding>> readPlatformHoldings(final String pseudoAccount);

	/**
	 * Used for selling or adding to holdings.
	 *
	 * @param apiKey  api key
	 * @param request sell holdings request
	 * @return order ids
	 */
	IOperationResponse<List<AdjustHoldingsResponse>> adjustHoldings(final String apiKey,
			final AdjustHoldingsRequest request);

	/**
	 * Gets the latest version of at-desktop client.
	 *
	 * @return at-desktop latest version
	 */
	IOperationResponse<String> autoTraderDesktopVersion();

	/**
	 * Gets the minimum version required of at-desktop client.
	 *
	 * @return at-desktop minimum version
	 */
	IOperationResponse<String> autoTraderDesktopMinVersion();

	/**
	 * This is for internal use. It is used by master-child order copying process.
	 */
	IOperationResponse<String> placeOrderMCA(String apiKey, String pseudoAccount, String exchange, String symbol,
			TradeType tradeType, OrderType orderType, ProductType productType, int quantity, float price,
			float triggerPrice, Validity validity, Boolean amo, String publisherId, String commandId);

	/**
	 * This is for internal use. It is used by master-child order copying process.
	 */
	IOperationResponse<Boolean> cancelOrderMCA(String apiKey, String pseudoAccount, String platformId,
			String commandId);

	/**
	 * This is for internal use. It is used by master-child order copying process.
	 */
	IOperationResponse<Boolean> modifyOrderMCA(final String apiKey, final String pseudoAccount, final String platformId,
			final OrderType orderType, final Integer quantity, final Float price, final Float triggerPrice,
			String commandId);

}
