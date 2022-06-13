/**
 *
 */
package com.dakshata.autotrader.api;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.Tag;

import com.dakshata.constants.trading.OrderType;
import com.dakshata.constants.trading.PositionCategory;
import com.dakshata.constants.trading.PositionType;
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

	private static IAutoTrader AT = AutoTrader.createInstance(AutoTraderClientConfig.builder()
			.apiKey("b25f5e2f-93cb-430e-a81d-f960a490034f").serviceUrl("http://localhost:9017").build());

	// private static IAutoTrader AT =
	// AutoTrader.createInstance("75bf216e-a830-4ed1-a04f-fb42f330415c",
	// "https://stocksdeveloper.in:9017");

	private static final String[] ACC = { "FA8768", "159401", "DN4048", "MM01515" };

	private static final float livePrice = 272.65f;

	@Tag("skip")
	// @Test
	public void testCancelOrder() throws InterruptedException {
		for (final String acc : ACC) {
			System.out.println("Placing limit order in: " + acc);
			final IOperationResponse<String> response = AT.placeRegularOrder(acc, "NSE", "SBIN", TradeType.BUY,
					OrderType.LIMIT, ProductType.INTRADAY, 1, livePrice - 1.8f, 0f);
			assertNotNull(response);
			assertTrue(response.success());
			final String orderId = response.getResult();
			assertNotNull(orderId);

			Thread.sleep(2000);

			System.out.println("Cancelling limit order in: " + acc);
			final IOperationResponse<Boolean> cancelResponse = AT.cancelOrderByPlatformId(acc, orderId);
			assertNotNull(cancelResponse);
			assertTrue(cancelResponse.success());
			assertNotNull(cancelResponse.getResult());
			assertTrue(cancelResponse.getResult());
		}
	}

	@Tag("skip")
	// @Test
	public void testSquareOffPosition() throws InterruptedException {
		for (final String acc : ACC) {
			System.out.println("Placing market order in: " + acc);
			final IOperationResponse<String> response = AT.placeRegularOrder(acc, "NSE", "SBIN", TradeType.BUY,
					OrderType.MARKET, ProductType.INTRADAY, 1, 0f, 0f);
			assertNotNull(response);
			assertTrue(response.success());
			final String orderId = response.getResult();
			assertNotNull(orderId);

			Thread.sleep(2000);

			System.out.println("Squaring off position in: " + acc);
			final IOperationResponse<Boolean> sqOffResponse = AT.squareOffPosition(acc, PositionCategory.NET,
					PositionType.MIS, "NSE", "SBIN");
			assertNotNull(sqOffResponse);
			assertTrue(sqOffResponse.success());
			assertNotNull(sqOffResponse.getResult());
			assertTrue(sqOffResponse.getResult());
		}
	}

	@Tag("skip")
	// @Test
	public void testReadOrders() {
		this.testRead("orders", AT::readPlatformOrders);
	}

	@Tag("skip")
	// @Test
	public void testReadMargins() {
		this.testRead("margins", AT::readPlatformMargins);
	}

	@Tag("skip")
	// @Test
	public void testReadPositions() {
		this.testRead("positions", AT::readPlatformPositions);
	}

	public <T> void testRead(final String type, final Function<String, IOperationResponse<Set<T>>> reader) {
		for (final String acc : ACC) {
			System.out.println("Reading " + type + " from: " + acc);
			final IOperationResponse<Set<T>> response = reader.apply(acc);
			assertNotNull(response);
			assertTrue(response.success());
			final Set<T> part = response.getResult();
			assertNotNull(part);
			assertNotEquals(0, part.size());
		}
	}

}
