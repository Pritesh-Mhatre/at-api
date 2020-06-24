/**
 *
 */
package com.dakshata.autotrader.api;

import java.time.LocalTime;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.dakshata.constants.trading.OrderType;
import com.dakshata.constants.trading.ProductType;
import com.dakshata.constants.trading.TradeType;
import com.dakshata.data.model.common.IOperationResponse;

/**
 * A test class to test AutoTrader API after trading hours.
 *
 * @author PRITESH
 *
 */
public class TestTooManyRequests {

	private static IAutoTrader AT = AutoTrader.createInstance("b25f5e2f-93cb-430e-a81d-f960a490034f",
			"http://localhost:9017");

	private static final String ACC = "FA8768";

	@Tag("skip")
	@Test
	public void testTooManyRequests() throws InterruptedException {

		// Do not run on production
		// Also it is strongly advised to reduce api limits to avoid killing brokers
		// server

		IntStream.range(1, 200).parallel().forEach(i -> {
			final IOperationResponse<String> response = AT.placeRegularOrder(ACC, "NSE", "SBIN", TradeType.BUY,
					OrderType.LIMIT, ProductType.INTRADAY, 1, 188.55f, 0f);
			System.out.println(i + "|" + LocalTime.now() + "|" + response.getMessage());
			try {
				Thread.sleep(10);
			} catch (final InterruptedException e) {
			}
		});

	}

}
