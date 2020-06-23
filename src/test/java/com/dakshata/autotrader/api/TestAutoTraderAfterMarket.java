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
import com.dakshata.trading.model.portfolio.Order;
import com.dakshata.trading.model.trade.PseudoAccount;

/**
 * A test class to test AutoTrader API after trading hours.
 *
 * @author PRITESH
 *
 */
public class TestAutoTraderAfterMarket {

	private static IAutoTrader AT = AutoTrader.createInstance("b25f5e2f-93cb-430e-a81d-f960a490034f",
			"http://localhost:9017");

	// private static IAutoTrader AT =
	// AutoTrader.createInstance("75bf216e-a830-4ed1-a04f-fb42f330415c",
	// "https://stocksdeveloper.in:9017");

	@Tag("skip")
	@Test
	public void testPlaceOrder() {
		final PseudoAccount account = new PseudoAccount();
		account.setKey("MM01515");

		final Order order = Order.builder().account(account).tradeType(TradeType.BUY).orderType(OrderType.LIMIT)
				.exchange("NSE").symbol("SBIN").quantity(1).price(187.6f).productType(ProductType.INTRADAY).build();

		final IOperationResponse<String> response = AT.placeOrder(order);
		String orderId = null;
		if (response.success()) {
			orderId = response.getResult();
		}

		assertNotNull(response);
		assertNull(response.getMessage());
		assertTrue(response.success());
		assertNotNull(orderId);
	}

	@Tag("skip")
	@Test
	public void testPlaceRegularOrder() {
		final IOperationResponse<String> response = AT.placeRegularOrder("MM01515", "NSE", "BANKNIFTY_25-JUN-2020_FUT",
				TradeType.BUY, OrderType.MARKET, ProductType.INTRADAY, 20, 0f, 0f);
		String orderId = null;
		if (response.success()) {
			orderId = response.getResult();
		}
		assertNotNull(response);
		assertNull(response.getMessage());
		assertTrue(response.success());
		assertNotNull(orderId);
	}

	@Tag("skip")
	@Test
	public void testPlaceCoverOrder() {
		final IOperationResponse<String> response = AT.placeCoverOrder("MM01515", "NSE", "SBIN", TradeType.BUY,
				OrderType.LIMIT, 10, 180.5f, 180f);
		String orderId = null;
		if (response.success()) {
			orderId = response.getResult();
		}

		assertNotNull(response);
		assertNull(response.getMessage());
		assertTrue(response.success());
		assertNotNull(orderId);
	}

	@Tag("skip")
	@Test
	public void testPlaceBracketOrder() {
		final IOperationResponse<String> response = AT.placeBracketOrder("MM01515", "NSE", "SBIN", TradeType.BUY,
				OrderType.LIMIT, 10, 180.5f, 0f, 5f, 2.5f, 1f);
		String orderId = null;
		if (response.success()) {
			orderId = response.getResult();
		}

		assertNotNull(response);
		assertNull(response.getMessage());
		assertTrue(response.success());
		assertNotNull(orderId);
	}

}
