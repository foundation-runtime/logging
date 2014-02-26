/**
 * 
 */
package com.cisco.oss.foundation.logging;

import com.cisco.oss.foundation.application.exception.ApplicationException;
import com.cisco.oss.foundation.application.exception.ErrorCode;
import com.cisco.oss.foundation.logging.appender.FoundationFileRollingAppender;
import org.apache.log4j.*;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * 
 * @author Yair Ogen
 */
public class TestUsingLog4j {

	private static final Logger LOGGER = Logger.getLogger(TestUsingLog4j.class);
	// private static final int NUMBER_OF_ITER = 1;
	private static final int NUMBER_OF_ITER = 500;

	@BeforeClass
	public static void init() {
		// FoundationLogger.init();
	}

	@Test
	public void myTest() {

		Integer one = ApplicationState.setState(Level.INFO, "persistent data message");
		ApplicationState.setState(Level.WARN, "persistent data message - WARN");

		for (int i = 0; i < NUMBER_OF_ITER; i++) {
			// MDC.put("catTerm", CategoryTerm.Log);
			LOGGER.info("First message");
			// LOGGER.info(createMessageWrapper(CategoryTerm.Audit,
			// "First message using Audit category term"));
			LOGGER.error("error with out stack trace");
			LOGGER.error("error with stack trace", new ApplicationException("DUMMY APPLICATION EXCEPTION", new IllegalArgumentException("DUMMY EXCEPTION"), new ErrorCode("CL_UNIT_TEST", 1357)));
			// LOGGER.error(createMessageWrapper(CategoryTerm.Audit,
			// "error with stack trace and Audit category"), new
			// IllegalArgumentException());
			LOGGER.fatal("Fatal message test!!!");

			if (i == 500) {
				ApplicationState.updateState(one, Level.INFO, "persistent data message was updated");
				ApplicationState.updateState(one, Level.INFO, "persistent data message was updated");
			} else if (i == 750) {
				ApplicationState.removeState(one);
				ApplicationState.removeState(Integer.valueOf(112234));
			}

			LOGGER.info("After sleeping");

			// (new MyTest2()).printMe();
		}

//		try {
//			Thread.sleep(60000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
	}

	@Test
	@Ignore
	public void testFoundationHierarchyEventListener() {
		FoundationHierarchyEventListener eventListener = new FoundationHierarchyEventListener();
		FoundationFileRollingAppender appender = (FoundationFileRollingAppender) LogManager.getRootLogger().getAppender("rollingfile");
		appender.setMaxRollFileCount(0);
		appender.setLayout(new PatternLayout());
		eventListener.addAppenderEvent(LogManager.getRootLogger(), appender);
		appender.setMaxRollFileCount(12);
		appender.setLayout(new SimpleLayout());
		eventListener.addAppenderEvent(LogManager.getRootLogger(), appender);
		FoundationLogger.log4jConfigProps.remove(FondationLoggerConstants.Foundation_FILE_ARCHIVE.toString());
		eventListener.addAppenderEvent(LogManager.getRootLogger(), appender);
		FoundationLogger.log4jConfigProps.setProperty(FondationLoggerConstants.Foundation_FILE_ARCHIVE.toString(), "true");
		System.setProperty("os.name", "windows");
		eventListener.addAppenderEvent(LogManager.getRootLogger(), appender);
		System.setProperty("os.name", "linux");
		eventListener.addAppenderEvent(LogManager.getRootLogger(), appender);
		FoundationLogger.log4jConfigProps.setProperty(FondationLoggerConstants.Foundation_FILE_ARCHIVE.toString(), "false");
		eventListener.addAppenderEvent(LogManager.getRootLogger(), appender);
	}

	@Test
	public void testFoundationLogger() {
		LogManager.getRootLogger().setLevel(null);
		System.setProperty("os.name", "hp-ux");
		FoundationLogger.init();
	}

	@Test
	public void multiline() {
		LOGGER.info("regular row");
		for (int i = 0; i < 10; i++) {
			LOGGER.info("12345634587589072578234905782348905723489057239048572903485723904857239048570\n" + "twertuwpertuiwperotuiwoprituweoprituwopertupweroituwpeortuiweoprituweoprituwpeortuiweroptuweroptuweopru\n" + "vnzc.xm,vnz,.cmvnzcm,.vnz,.cmvnz,.cmvnz.,mcvnz,.mcvnzm,.xcvn,.cvmzn");
		}

		LOGGER.info("********************");
		String conversionPattern = "%d{yyyy/MM/dd HH:mm:ss.SSS}{UTC} [%c{1}] [%t]:  %p: %X{flowCtxt} %pm{1r5} %errorcode %throwable{short} %n";
//		run(conversionPattern);

		LOGGER.info("********************");
		conversionPattern = "%d{yyyy/MM/dd HH:mm:ss.SSS}{UTC} [%c{1}] [%t]:  %p: %X{flowCtxt} %pm{15} %errorcode %throwable{short} %n";
//		run(conversionPattern);

		LOGGER.info("********************");
		conversionPattern = "%d{yyyy/MM/dd HH:mm:ss.SSS}{UTC} [%c{1}] [%t]:  %p: %X{flowCtxt} %pm{0} %errorcode %throwable{short} %n";
//		run(conversionPattern);
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void run(String conversionPattern) {
		FileAppender appender = (FileAppender) Logger.getRootLogger().getAppender("rollingfile");
		FoundationLoggingPatternLayout layout = new FoundationLoggingPatternLayout();
		layout.setConversionPattern(conversionPattern);
		appender.setLayout(layout);
		appender.activateOptions();
		for (int i = 0; i < 10; i++) {
			LOGGER.info("12345634587589072578234905782348905723489057239048572903485723904857239048570\n" + "twertuwpertuiwperotuiwoprituweoprituwopertupweroituwpeortuiweoprituweoprituwpeortuiweroptuweroptuweopru\n" + "vnzc.xm,vnz,.cmvnzcm,.vnz,.cmvnz,.cmvnz.,mcvnz,.mcvnzm,.xcvn,.cvmzn");
		}
	}

}
