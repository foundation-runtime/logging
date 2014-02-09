package com.cisco.vss.foundation.logging.structured;

import com.cisco.vss.foundation.logging.FondationLoggerConstants;
import com.cisco.vss.foundation.logging.FoundationLoggingEvent;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;
import org.apache.commons.lang3.text.WordUtils;
import org.jdom2.Element;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import javax.tools.*;
import javax.tools.JavaFileObject.Kind;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.security.SecureClassLoader;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractFoundationLoggingMarker implements FoundationLoggingMarker {

	private boolean isInit = false;

	private static final long serialVersionUID = 6354894754547315308L;

	protected Map<String, Object> userFields = new HashMap<String, Object>();

	private static final Map<Class<? extends FoundationLoggingMarker>, Multiset<Field>> markerClassFields = new ConcurrentHashMap<Class<? extends FoundationLoggingMarker>, Multiset<Field>>();

	private static HashMap<Class<? extends FoundationLoggingMarker>, Class<FoundationLoggingMarkerFormatter>> markersMap = new HashMap<Class<? extends FoundationLoggingMarker>, Class<FoundationLoggingMarkerFormatter>>();

	public static HashMap<String, Element> markersXmlMap = new HashMap<String, Element>();

	private FoundationLoggingMarkerFormatter formatter;

	private static Logger LOGGER = LoggerFactory.getLogger(AbstractFoundationLoggingMarker.class);

	public AbstractFoundationLoggingMarker() {
		super();
		// create new instance of a formatter specific to this marker
		// implementation.
		// if it is the first time we create a marker of this class - we
		// generate a new formatter implementation
		FoundationLoggingMarkerFormatter newFormatter = getFormatter(getClass());
		newFormatter.setMarker(this);
		setFormatter(newFormatter);
	}

	private static FoundationLoggingMarkerFormatter getFormatter(Class<? extends FoundationLoggingMarker> markerClass) {
		try {

			generateAndUpdateFormatterInMap(markerClass);

			Class<FoundationLoggingMarkerFormatter> formatterClass = markersMap.get(markerClass);
			FoundationLoggingMarkerFormatter newFormatter = null;
			if (formatterClass == null) {
				newFormatter = new DefaultMarkerFormatter();
			} else {
				newFormatter = formatterClass.newInstance();
			}

			return newFormatter;
		} catch (Exception e) {
			LOGGER.error("Can't find a proxied class for: " + markerClass, e);
			throw new IllegalArgumentException(e);
		}
	}

	private static void generateAndUpdateFormatterInMap(Class<? extends FoundationLoggingMarker> markerClass) {

		if (markersMap.get(markerClass) == null) {

			Element markerElement = markersXmlMap.get(markerClass.getName());

			StringBuilder builder = new StringBuilder();
			buildClassPrefix(markerClass, builder);

			boolean shouldGenereate = buildFormat(markerElement, markerClass, builder);

			if (shouldGenereate) {

				buildClassSuffix(builder);
				String newClassSource = builder.toString();

				synchronized (markerClass) {

					LOGGER.trace("The generated class is: {}", newClassSource);
					Class<FoundationLoggingMarkerFormatter> formatterClass = generateMarkerClass(newClassSource, markerClass.getName() + "Formatter");
					markersMap.put(markerClass, formatterClass);
					
				}
			} else {
				LOGGER.debug("Not generating any specific markers for {} as it doesn't contain any annotations", markerClass);
			}

		}
	}

	@Override
	public String getName() {
		if (!isInit) {
			populateUserFieldMap();
		}
		return this.getClass().getSimpleName();
	}

	@Override
	public void add(Marker reference) {
	}

	@Override
	public boolean remove(Marker reference) {
		return false;
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public boolean hasReferences() {
		return false;
	}

	@Override
	public Iterator iterator() {
		return null;
	}

	@Override
	public boolean contains(Marker other) {
		return false;
	}

	@Override
	public boolean contains(String name) {
		return false;
	}

	@Override
	public String valueOf(String userFieldName) {

		String value = null;

		if (FondationLoggerConstants.TRANSACTION_NAME.toString().equals(userFieldName)) {
			value = getName();
		} else if (FondationLoggerConstants.ALL_VALUES.toString().equals(userFieldName)) {
			value = buildAllValues();
		} else {
			value = userFields.get(userFieldName) != null ? userFields.get(userFieldName).toString() : null;
		}

		return value;

	}

	private String buildAllValues() {

		boolean first = true;

		StringBuilder builder = new StringBuilder("{");

		Set<Entry<String, Object>> entrySet = userFields.entrySet();
		for (Entry<String, Object> entry : entrySet) {
			String key = entry.getKey();
			Object value = entry.getValue();

			if (!FoundationLoggingMarker.NO_OPERATION.equals(value)) {
				if (first) {
					first = false;
				} else {
					builder.append(",");
				}
				builder.append("\"");
				builder.append(key);
				builder.append("\"");
				builder.append(":");
				if (value instanceof String) {
					builder.append("\"");
					value = ((String) value).replaceAll("\"", "'");
					builder.append(value);
					builder.append("\"");
				} else {
					builder.append(value);
				}
			}
		}
		builder.append("}");
		return builder.toString();
	}

	protected void populateUserFieldMap() {

		Class<? extends AbstractFoundationLoggingMarker> clazz = this.getClass();
		Multiset<Field> fieldList = markerClassFields.get(clazz);

		if (fieldList == null) {

			fieldList = ConcurrentHashMultiset.create();
			
			Class<?> cls = clazz;
			
			while (AbstractFoundationLoggingMarker.class.isAssignableFrom(cls)){
				Field[] declaredFields = cls.getDeclaredFields();						

				for (Field field : declaredFields) {

					if (field.isAnnotationPresent(UserField.class)) {
						field.setAccessible(true);
						fieldList.add(field);
					}
				}
				markerClassFields.put(clazz, fieldList);
				cls = cls.getSuperclass();
			}

			
		}

		for (Field field : fieldList) {

			try {

				Object value = field.get(this);

				UserField userField = field.getAnnotation(UserField.class);
				if (value == null && userField.suppressNull()) {
					value = FoundationLoggingMarker.NO_OPERATION;
				}
				userFields.put(field.getName(), value == null ? "null" : value);
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException(e);
			}
		}

	}

	@Override
	public FoundationLoggingMarkerFormatter getFormatter() {
		return formatter;
	}

	public void setFormatter(FoundationLoggingMarkerFormatter formatter) {
		this.formatter = formatter;
	}

	private static Class<FoundationLoggingMarkerFormatter> generateMarkerClass(String sourceCode, String newClassName) {

		try {

			// We get an instance of JavaCompiler. Then
			// we create a file manager
			// (our custom implementation of it)
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			JavaFileManager fileManager = new ClassFileManager(compiler.getStandardFileManager(null, null, null));

			// Dynamic compiling requires specifying
			// a list of "files" to compile. In our case
			// this is a list containing one "file" which is in our case
			// our own implementation (see details below)
			List<JavaFileObject> jfiles = new ArrayList<JavaFileObject>();
			jfiles.add(new DynamicJavaSourceCodeObject(newClassName, sourceCode));

			List<String> optionList = new ArrayList<String>();
			// set compiler's classpath to be same as the runtime's
			optionList.addAll(Arrays.asList("-classpath", System.getProperty("java.class.path")));

			// We specify a task to the compiler. Compiler should use our file
			// manager and our list of "files".
			// Then we run the compilation with call()
			compiler.getTask(null, fileManager, null, optionList, null, jfiles).call();

			// Creating an instance of our compiled class and
			// running its toString() method
			Class<FoundationLoggingMarkerFormatter> clazz = (Class<FoundationLoggingMarkerFormatter>) fileManager.getClassLoader(null).loadClass(newClassName);

			return clazz;
		} catch (Exception e) {
			throw new UnsupportedOperationException("can't create class of: " + newClassName, e);
		}
	}

	private static <T extends FoundationLoggingMarker> void buildClassPrefix(Class<T> markerClass, StringBuilder builder) {
		builder.append("\npackage ").append(markerClass.getPackage().getName()).append(";\n\n");
		builder.append("import ").append(FoundationLoggingMarkerFormatter.class.getName()).append(";\n");
		builder.append("import ").append(FoundationLoggingMarker.class.getName()).append(";\n");
		builder.append("import ").append(FoundationLoggingEvent.class.getName()).append(";\n");

		builder.append("public class ").append(markerClass.getSimpleName()).append("Formatter").append(" implements FoundationLoggingMarkerFormatter ").append(" {\n\n");
		builder.append("private FoundationLoggingMarker loggingMarker;\n\n");
		builder.append("	\n@Override\n" + "	public void setMarker(FoundationLoggingMarker marker) {\r\n" + "		this.loggingMarker = marker;\r\n" + "	}");
		builder.append("\n@Override\npublic String getFormat(FoundationLoggingEvent foundationLoggingEvent) {\n").append(markerClass.getSimpleName()).append(" marker = (").append(markerClass.getSimpleName()).append(")loggingMarker;\n");
	}

	private static void buildClassSuffix(StringBuilder builder) {
		builder.append("}\n}");
	}

	private static boolean buildFormat(Element markerElement, Class<? extends FoundationLoggingMarker> clazz, StringBuilder builder) {

		if (markerElement == null) {
			return buildFromAnnotations(clazz, builder);
		} else {
			return buildFromXml(markerElement, builder);
		}

	}

	private static boolean buildFromXml(Element markerElement, StringBuilder builder) {

		Element defaultAppender = markerElement.getChild("defaultAppender");

		List<Element> appenders = markerElement.getChildren("appender");

		if (appenders != null) {
			for (Element appender : appenders) {
				String appenderId = appender.getAttributeValue("id");
				if (appenderId == null) {
					LOGGER.error("the appender element must have an id poiting to a valid appender name");
				} else {
					buildFromAppenderElement(appender, builder, false, appenderId);
				}
			}
		}

		if (defaultAppender == null) {

			LOGGER.error("The marker element must contain a 'defaultAppender' element");
			builder.append("return null;");

		} else {

			buildFromAppenderElement(defaultAppender, builder, true, "DEFAULT");
		}

		return true;
	}

	private static void buildFromAppenderElement(Element appenderElement, StringBuilder builder, boolean isDefault, String appenderName) {

		if (!isDefault) {
			builder.append("if (\"").append(appenderName).append("\".equals(").append("foundationLoggingEvent.getAppenderName())){\n");
		}

		Element criteriaElement = appenderElement.getChild("criteria");

		if (criteriaElement != null) {

			List<Element> criterionList = criteriaElement.getChildren("criterion");

			if (criterionList != null) {

				for (Element criterionElement : criterionList) {

					String result = criterionElement.getAttributeValue("format");
					List<Element> fieldList = criterionElement.getChildren("field");

					if (fieldList != null) {

						for (int i = 0; i < fieldList.size(); i++) {
							Element fieldElement = fieldList.get(i);

							String key = fieldElement.getAttributeValue("name");
							String value = fieldElement.getAttributeValue("equals");

							String getterField = "marker.get" + WordUtils.capitalize(key) + "()";

							if (i == 0) {
								builder.append("if (").append(getterField).append(" != null && \"").append(value).append("\".equals(").append(getterField).append(".toString())");
							} else {
								builder.append(" && ").append(getterField).append(" != null && \"").append(value).append("\".equals(").append(getterField).append(".toString())");
							}

						}

						builder.append(")\n\treturn \"").append(result).append("\";\n");
					}

				}
			}
		} else {
			LOGGER.info("The marker element does not contain a 'criteria' element");
		}

		String defaultFormat = appenderElement.getAttributeValue("defaultFormat");
		if (defaultFormat == null) {
			LOGGER.error("The marker element must contain a 'defaultFormat' element");
		}
		builder.append("return \"" + defaultFormat + "\";");

		if (!isDefault) {
			builder.append("\n}\n");
		}
	}

	private static boolean buildFromAnnotations(Class<? extends FoundationLoggingMarker> clazz, StringBuilder builder) {
		boolean shouldGenerate = false;

		final DefaultFormat defaultFormat = clazz.getAnnotation(DefaultFormat.class);

		ConditionalFormats conditionalFormats = clazz.getAnnotation(ConditionalFormats.class);
		if (conditionalFormats != null) {

			shouldGenerate = true;

			if (defaultFormat == null) {
				throw new IllegalArgumentException("when using conditionals - you must also speicfy a DefaultFormat annotation");
			}
			ConditionalFormat[] formats = conditionalFormats.value();
			if (formats != null) {
				for (ConditionalFormat conditionalFormat : formats) {
					final ConditionalFormat format = conditionalFormat;
					buldConditionalFormat(format, builder);
				}
			}
		}

		if (defaultFormat != null) {
			shouldGenerate = true;
			buildDefaultFormat(defaultFormat, builder);
		}

		return shouldGenerate;
	}

	private static void buldConditionalFormat(ConditionalFormat format, final StringBuilder builder) {
		String result = format.format();
		FieldCriterion[] criteria = format.criteria();
		if (criteria != null) {

			for (int i = 0; i < criteria.length; i++) {

				FieldCriterion criterion = criteria[i];

				String key = criterion.name();
				String value = criterion.value();

				String getterField = "marker.get" + WordUtils.capitalize(key) + "()";

				if (i == 0) {
					builder.append("if (").append(getterField).append(" != null && \"").append(value).append("\".equals(").append(getterField).append(".toString())");
				} else {
					builder.append(" && ").append(getterField).append(" != null && \"").append(value).append("\".equals(").append(getterField).append(".toString())");
				}
			}

			builder.append(")\n\treturn \"").append(result).append("\";\n");
		}

	}

	private static void buildDefaultFormat(DefaultFormat defaultFormat, final StringBuilder builder) {
		builder.append("return \"" + defaultFormat.value() + "\";");
	}

	/**
	 * Scan all the class path and look for all classes that have the Format
	 * Annotations.
	 */
	public static void scanClassPathForFormattingAnnotations() {

		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

		// scan classpath and filter out classes that don't begin with "com.nds"
		Reflections ndsReflections = new Reflections("com.nds");

		Set<Class<?>> annotated = ndsReflections.getTypesAnnotatedWith(DefaultFormat.class);

        Reflections ciscoReflections = new Reflections("com.cisco");

        annotated.addAll(ciscoReflections.getTypesAnnotatedWith(DefaultFormat.class));

		for (Class<?> markerClass : annotated) {

			// if the marker class is indeed implementing FoundationLoggingMarker
			// interface
			if (FoundationLoggingMarker.class.isAssignableFrom(markerClass)) {

				final Class<? extends FoundationLoggingMarker> clazz = (Class<? extends FoundationLoggingMarker>) markerClass;

				executorService.execute(new Runnable() {

					@Override
					public void run() {

						if (markersMap.get(clazz) == null) {
							try {
								// generate formatter class for this marker
								// class
								generateAndUpdateFormatterInMap(clazz);
							} catch (Exception e) {
								LOGGER.trace("problem generating formatter class from static scan method. error is: " + e.toString());
							}
						}

					}
				});

			} else {// if marker class does not implement FoundationLoggingMarker
					// interface, log ERROR

				// verify the LOGGER was initialized. It might not be as this
				// Method is called in a static block
				if (LOGGER == null) {
					LOGGER = LoggerFactory.getLogger(AbstractFoundationLoggingMarker.class);
				}
				LOGGER.error("Formatter annotations should only appear on foundationLoggingMarker implementations");
			}
		}

		executorService.shutdown();
		// try {
		// executorService.awaitTermination(15, TimeUnit.SECONDS);
		// } catch (InterruptedException e) {
		// LOGGER.error("creation of formatters has been interrupted");
		// }
	}

	/**
	 * Creates a dynamic source code file object
	 * 
	 * This is an example of how we can prepare a dynamic java source code for
	 * compilation. This class reads the java code from a string and prepares a
	 * JavaFileObject
	 * 
	 */
	private static class DynamicJavaSourceCodeObject extends SimpleJavaFileObject {
		private String sourceCode;

		/**
		 * Converts the name to an URI, as that is the format expected by
		 * JavaFileObject
		 * 
		 * 
		 * @param name
		 *            qualified name given to the class file
		 * @param code
		 *            the source code string
		 */
		protected DynamicJavaSourceCodeObject(String name, String code) {
			super(URI.create("string:///" + name.replaceAll("\\.", "/") + Kind.SOURCE.extension), Kind.SOURCE);
			this.sourceCode = code;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
			return sourceCode;
		}

	}

	public static class JavaClassObject extends SimpleJavaFileObject {

		/**
		 * Byte code created by the compiler will be stored in this
		 * ByteArrayOutputStream so that we can later get the byte array out of
		 * it and put it in the memory as an instance of our class.
		 */
		protected final ByteArrayOutputStream bos = new ByteArrayOutputStream();

		/**
		 * Registers the compiled class object under URI containing the class
		 * full name
		 * 
		 * @param name
		 *            Full name of the compiled class
		 * @param kind
		 *            Kind of the data. It will be CLASS in our case
		 */
		public JavaClassObject(String name, Kind kind) {
			super(URI.create("string:///" + name.replace('.', '/') + kind.extension), kind);
		}

		/**
		 * Will be used by our file manager to get the byte code that can be put
		 * into memory to instantiate our class
		 * 
		 * @return compiled byte code
		 */
		public byte[] getBytes() {
			return bos.toByteArray();
		}

		/**
		 * Will provide the compiler with an output stream that leads to our
		 * byte array. This way the compiler will write everything into the byte
		 * array that we will instantiate later
		 */
		@Override
		public OutputStream openOutputStream() throws IOException {
			return bos;
		}
	}

	public static class ClassFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
		/**
		 * Instance of JavaClassObject that will store the compiled bytecode of
		 * our class
		 */
		private JavaClassObject jclassObject;

		/**
		 * Will initialize the manager with the specified standard java file
		 * manager
		 * 
		 * @param standardManager
		 */
		public ClassFileManager(StandardJavaFileManager standardManager) {
			super(standardManager);
		}

		/**
		 * Will be used by us to get the class loader for our compiled class. It
		 * creates an anonymous class extending the SecureClassLoader which uses
		 * the byte code created by the compiler and stored in the
		 * JavaClassObject, and returns the Class for it
		 */
		@Override
		public ClassLoader getClassLoader(Location location) {
			return new SecureClassLoader() {
				@Override
				protected Class<?> findClass(String name) throws ClassNotFoundException {
					byte[] b = jclassObject.getBytes();
					return super.defineClass(name, jclassObject.getBytes(), 0, b.length);
				}
			};
		}

		/**
		 * Gives the compiler an instance of the JavaClassObject so that the
		 * compiler can write the byte code into it.
		 */
		@Override
		public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException {
			jclassObject = new JavaClassObject(className, kind);
			return jclassObject;
		}
	}

}
