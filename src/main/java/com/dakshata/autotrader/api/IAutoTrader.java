/**
 *
 */
package com.dakshata.autotrader.api;

import com.dakshata.data.model.common.IOperationResponse;

/**
 * AutoTrader API instance.
 *
 * @author PRITESH
 *
 */
public interface IAutoTrader {

	/**
	 * This function executes a given command. The command can be operations like
	 * place, modify & cancel order etc. This is primarily used for AutoTrader
	 * desktop application which receives commands in csv format.
	 *
	 * @param command command in comma separated value (csv) format
	 * @return returns true on success, false otherwise
	 */
	IOperationResponse<Boolean> execute(String command);

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
