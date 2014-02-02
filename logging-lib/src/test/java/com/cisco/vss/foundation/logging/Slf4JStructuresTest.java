package com.cisco.vss.foundation.logging;

import com.cisco.vss.foundation.logging.stuctured.test.TransactionLoggingMarker;
import com.cisco.vss.foundation.logging.stuctured.test.TransactionMarker;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class Slf4JStructuresTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(Slf4JStructuresTest.class.getName());
	private static final Logger AUDITOR = LoggerFactory.getLogger("audit." + Slf4JStructuresTest.class.getName());

	TransactionMarker marker = new TransactionMarker("session id");
	TransactionMarker marker1 = new TransactionMarker("session id");
	TransactionMarker marker2 = new TransactionMarker("session id");
	TransactionMarker marker3 = new TransactionMarker("session id");
	TransactionMarker marker4 = new TransactionMarker("session id");

	private static final org.apache.log4j.Logger LOG4J_LOGGER = org.apache.log4j.Logger.getLogger("audit." + Slf4JStructuresTest.class.getName());

	@Before
	public void setup() {

		marker1.setSourceId("sourceId1").setSourceType("sourceType1");
		marker2.setSourceId("sourceId2").setSourceType("sourceType2");
		marker3.setSourceId("sourceId3").setSourceType("sourceType4");
		marker4.setSourceId("sourceId3").setSourceType("sourceType3");
	}

//	@BeforeClass
	public static void time() {
		long start = System.nanoTime();
		TransactionMarker newMarker = new TransactionMarker("session id");

		long end = System.nanoTime();

		long total = (end - start);

		System.out.println("total first: " + total);

		start = System.nanoTime();
		TransactionMarker newMarker2 = new TransactionMarker("session id");

		end = System.nanoTime();

		total = (end - start);

		System.out.println("total second: " + total);

		start = System.nanoTime();
		TransactionMarker newMarker3 = new TransactionMarker("session id");

		end = System.nanoTime();

		total = (end - start);

		System.out.println("total third: " + total);

		System.out.println("1: " + newMarker + ". 2: " + newMarker2);
	}

	@Test
	public void testStructureMatching() throws Exception {

		int iter = 10000;
		double iterD = iter;

		long total = 0;

		for (int i = 0; i < iter; i++) {
			total += runMe();
		}

		total = 0;
		iter = 10000;

		for (int i = 0; i < iter; i++) {
			total += runMe();
		}

		double avg = (total / iter);

		total = TimeUnit.NANOSECONDS.toMillis(total);

		System.out.println("total: " + total);

		avg = (total / iterD);

		System.out.println("average: " + avg);
	}

	private long runMe() {
		long start = System.nanoTime();

		AUDITOR.info(marker, "");
		AUDITOR.info(marker1, "");
		AUDITOR.info(marker2, "");
		AUDITOR.info(marker3, "");

		long end = System.nanoTime();

		long total = end - start;
		// System.out.println(total);
		return total;
	}

	@Test
	public void testStructure() throws Exception {

		// TransactionMarker marker = new TransactionMarker(null,
		// CABLoggingTransMsgType.REQ, null, "sourceId", "sourceType",
		// "destinationId", "destinationType", "messagePayload");
		// for (int i = 0; i < 500; i++) {
		marker.setSourceId("my source id");
		marker.setSourceType("my source type");
		marker.setBase("my base");
		AUDITOR.info(marker, "transaction starting");
		AUDITOR.info(marker1, "transaction starting");
		AUDITOR.info(marker2, "transaction starting");
		AUDITOR.info(marker3, "transaction starting");
		AUDITOR.info(marker4, "transaction starting");
		AUDITOR.info("transaction starting");
		
		
		TransactionLoggingMarker marker5 = new TransactionLoggingMarker();
		AUDITOR.info(marker5,"transaction starting");
		
		marker5 = new TransactionLoggingMarker();
		marker5.setStart();
		AUDITOR.info(marker5,"transaction starting");
		
		// AUDITOR.info("what's up?");
		//
		// LOGGER.info("hello {}. This is a test for {}", new String[]
		// {"Infra_User","Formatted message"});
		//
		//
		// TransactionMarker2 marker2 = new TransactionMarker2("Yoni",
		// 2045);
		//
		// AUDITOR.info(marker2, "transaction starting yet again");
		//
		// LOG4J_LOGGER.info("What's up");
		// }

		// Thread.sleep(5000);

	}

	@Test
	public void testJSONLIsts() {

		TransactionMarker marker = new TransactionMarker("session id");
		marker.setDestinationId("destinationId").setDestinationType("destinationType").setSourceType("eventType");
		AUDITOR.info(marker, "");

	}

}
