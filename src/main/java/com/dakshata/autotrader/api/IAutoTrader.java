/**
 *
 */
package com.dakshata.autotrader.api;

import java.util.Set;

import com.dakshata.data.model.common.IOperationResponse;
import com.dakshata.trading.model.platform.PlatformMargin;
import com.dakshata.trading.model.platform.PlatformOrder;
import com.dakshata.trading.model.platform.PlatformPosition;

/**
 * AutoTrader API instance.
 *
 * @author PRITESH
 *
 */
public interface IAutoTrader {

	/**
	 * Provides live pseudo accounts available under your user.
	 *
	 * @return live pseudo accounts
	 */
	IOperationResponse<Set<String>> fetchLivePseudoAccounts();

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
	 * Graceful shutdown. Call when your application is about to exit.
	 */
	void shutdown();

	/**
	 * Allows the library user to change API key using code. This is rarely needed.
	 *
	 * @param apiKey api key
	 */
	void setApiKey(String apiKey);

}
