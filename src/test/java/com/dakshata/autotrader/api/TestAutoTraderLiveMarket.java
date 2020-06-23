/**
 *
 */
package com.dakshata.autotrader.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
public class TestAutoTraderLiveMarket {

	private static IAutoTrader AT = AutoTrader.createInstance("b25f5e2f-93cb-430e-a81d-f960a490034f",
			"http://localhost:9017");

	// private static IAutoTrader AT =
	// AutoTrader.createInstance("75bf216e-a830-4ed1-a04f-fb42f330415c",
	// "https://stocksdeveloper.in:9017");

	private static final String ACC = "FA8768";

	@Tag("skip")
	@Test
	public void testCancelOrder() throws InterruptedException {
		final IOperationResponse<String> response = AT.placeRegularOrder(ACC, "NSE", "SBIN", TradeType.BUY,
				OrderType.LIMIT, ProductType.INTRADAY, 1, 188.55f, 0f);
		String orderId = null;
		if (response.success()) {
			orderId = response.getResult();
		}
		assertNotNull(response);
		assertNull(response.getMessage());
		assertTrue(response.success());
		assertNotNull(orderId);

		Thread.sleep(2000);

		final IOperationResponse<Boolean> cancelResponse = AT.cancelOrderByPlatformId(ACC, orderId);
		assertNotNull(cancelResponse);
		assertTrue(cancelResponse.success());
		assertNotNull(cancelResponse.getResult());
		assertTrue(cancelResponse.getResult());

	}

}
