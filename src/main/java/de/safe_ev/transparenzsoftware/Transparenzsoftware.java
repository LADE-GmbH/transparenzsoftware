package de.safe_ev.transparenzsoftware;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import de.safe_ev.transparenzsoftware.i18n.Translator;
import de.safe_ev.transparenzsoftware.output.ConsoleFileProcessor;
import de.safe_ev.transparenzsoftware.verification.VerificationParserFactory;

public class Transparenzsoftware {

	private final static Logger LOGGER = LogManager.getLogger(Transparenzsoftware.class);
	private static boolean testEnvironment;

	static void main(String[] args, boolean testEnvironment) throws Exception {
		Transparenzsoftware.testEnvironment = testEnvironment;
		main(args);
	}

	public static void main(String[] args) throws Exception {

		final VerificationParserFactory factory = new VerificationParserFactory();
		final CommandLineParser commandLineParser = new DefaultParser();
		final Options options = setUpCliOptions();
		try {
			final CommandLine commandLine = commandLineParser.parse(options, args);
			if (commandLine.hasOption("v")) {
				setUpVerboseLogging();
			}
			if (commandLine.hasOption("l")) {
				final String optionValue = commandLine.getOptionValue("l");
				Translator.init(optionValue);
			}
			String filePath = null;
			if (commandLine.hasOption("f")) {
				filePath = commandLine.getOptionValue("f");
			}

			if (commandLine.hasOption("h")) {
				LOGGER.debug("print help");
				printHelp(options);
				return;
			}

			if (filePath == null) {
				printHelp(options);
				System.err.println(Translator.get("error.no.input.file"));
				exit(0);
				return;
			}

			String outputPath = null;
			boolean overwrite = false;
			if (commandLine.hasOption("o")) {
				outputPath = commandLine.getOptionValue("o");
			}
			if (commandLine.hasOption("w")) {
				overwrite = true;
			}
			LOGGER.info("Read in file " + filePath);

			final ConsoleFileProcessor fileProcessor = new ConsoleFileProcessor(factory);
			final boolean result = fileProcessor.processFile(filePath, outputPath, overwrite);
			exit(result ? 2 : 0);

		} catch (final ParseException e) {
			LOGGER.error(Translator.get("error.invalid.input.parameters"));
			System.err.println(Translator.get("error.invalid.input.parameters"));
			printHelp(options);
		}

	}

	private static void exit(int code) {
		if (!testEnvironment) {
			System.exit(code);
		} else {
			LOGGER.info(String.format("App started and resulted and exited with code %s", code));
		}
	}

	private static void printHelp(Options options) {
		final HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("transparenzsoftware", options);
	}

	private static void setUpVerboseLogging() {
		final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		final Configuration config = ctx.getConfiguration();
		final LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
		loggerConfig.setLevel(Level.DEBUG);
		ctx.updateLoggers();
	}

	private static Options setUpCliOptions() {
		final Options options = new Options();
		options.addOption("v", "verbose", false, "Enables verbose logging in the stdout");
		options.addOption("f", "file", true, "Path to a file which should be read and processed.");
		options.addOption("l", "locale", true,
				"Choose the language for messages. Currently english (en_EN) and german (de_DE) are supported.");
		options.addOption("o", "output", true,
				"File where the output should be written to. If the file does not exist, the app will try to create it.");
		options.addOption("w", "write", false, "Overwrite the output file if it already exists.");
		options.addOption("h", "help", false, "Print this help page.");
		return options;
	}

}

