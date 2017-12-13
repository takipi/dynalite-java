
package com.takipi.oss.dynajava;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DynaliteJavaMain
{
	public final static String HELP_OPTION_STR = "help";
	public final static String PORT_OPTION_STR = "port";
	public final static String JDBC_ENDPOINT_OPTION_STR = "jdbcEndpoint";
	public final static String USER_OPTION_STR = "user";
	public final static String PASSWORD_OPTION_STR = "password";
	public final static String DYNALITE_SCRIPT_DIR_OPTION_STR = "dynaliteScriptDir";
	
	private final static Logger logger = LoggerFactory.getLogger(DynaliteJavaConfig.class.getName());
	
	public static void main(String[] args) throws Exception
	{
		DynaliteJavaMain dynaliteJavaMain = new DynaliteJavaMain();
		
		dynaliteJavaMain.start(args);		
	}
	
	private void start(final String[] args)
	{
		DynaliteJavaConfig config = this.parseCmdlineOptions(args);
		
		if (config == null)
		{
			return;
		}
		
		DynaliteJavaServer dynaliteJavaServer = new DynaliteJavaServer(config);
		
		try
		{
			dynaliteJavaServer.start();
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
		}
	}
	
	private DynaliteJavaConfig parseCmdlineOptions(final String[] args)
	{
		DynaliteJavaConfig config = null;
		
		Options options = createOptions();
		Options helpOptions = createHelpOptions();
		
		CommandLine cmdLine = null;
		
		try {
			cmdLine = parseOptions(helpOptions, args);
		
			if ((cmdLine == null) ||
				(cmdLine.hasOption(HELP_OPTION_STR)))
			{
				printUsage(helpOptions, options);
				
				return null;
			}
		
			cmdLine = parseOptions(options, args);
		
			if (cmdLine == null)
			{
				return null;
			}
		}
		catch (ParseException parseException)
		{
			logger.error("Unable to parse command-line arguments " +	Arrays.toString(args) + " due to: " + parseException);
			printUsage(helpOptions, options);
			
			return null;
		}
		
		config = fillConfig(cmdLine);
		
		return config;
	}
	
	private void printUsage(Options helpOptions, Options options)
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java -jar " + this.getClass().getName(), options, true);
		System.out.println("or");
		System.out.println("  java -jar " + this.getClass().getName() + " -" + helpOptions.getOption("help").getOpt());
	}
	
	private DynaliteJavaConfig fillConfig(CommandLine cmdLine)
	{
		int port;
		DynaliteJavaConfig config = new DynaliteJavaConfig();
		
		if (cmdLine.hasOption(PORT_OPTION_STR))
		{
			port = Integer.parseInt(cmdLine.getOptionValue(PORT_OPTION_STR));
			
			if (isValidPort(port))
			{
				config.setPort(port);
			}
			else
			{
				logger.error("Not a valid port: " + cmdLine.getOptionValue(PORT_OPTION_STR));
				
				return null;
			}
		}
		
		if (cmdLine.hasOption(JDBC_ENDPOINT_OPTION_STR))
		{
			String jdbcEndpoint = cmdLine.getOptionValue(JDBC_ENDPOINT_OPTION_STR);
			if (isValidJdbcEndpoint(jdbcEndpoint))
			{
				config.setJdbcEndpoint(jdbcEndpoint);
			}
			else
			{
				logger.error("Not a valid jdbcEndpoint: " + cmdLine.getOptionValue(JDBC_ENDPOINT_OPTION_STR));
				
				return null;
			}
		}
		
		if (cmdLine.hasOption(USER_OPTION_STR))
		{
			config.setUser(cmdLine.getOptionValue(USER_OPTION_STR));
		}
		
		if (cmdLine.hasOption(PASSWORD_OPTION_STR))
		{
			config.setPassword(cmdLine.getOptionValue(PASSWORD_OPTION_STR));
		}
		
		if (cmdLine.hasOption(DYNALITE_SCRIPT_DIR_OPTION_STR))
		{
			String dynaliteScriptDir = cmdLine.getOptionValue(DYNALITE_SCRIPT_DIR_OPTION_STR);
			if (isValidDynaliteScriptDir(dynaliteScriptDir))
			{
				config.setDynaliteScriptDir(new File(dynaliteScriptDir));
			}
			else
			{
				logger.error("Not a valid dynaliteScriptDir: " + cmdLine.getOptionValue(DYNALITE_SCRIPT_DIR_OPTION_STR));
				
				return null;
			}
		}
		
		return config;
	}
	
	private boolean isValidDynaliteScriptDir(String dynaliteScriptDir)
	{
		return Files.exists(Paths.get(dynaliteScriptDir + "/" + DynaliteJavaConfig.DYNALITE_MAIN));
	}
	
	private boolean isValidJdbcEndpoint(String jdbcEndpoint)
	{
		if ((jdbcEndpoint.startsWith("jdbc:h2:")) ||
			(jdbcEndpoint.startsWith("jdbc:mysql:")))
		{
			return true;
		}
		
		return false;
	}
	
	private boolean isValidPort(int port)
	{
		return ((port > 0) &&
				(port <= 65535));
	}
	
	private CommandLine parseOptions(Options options, final String[] args) throws ParseException
	{
		final CommandLineParser cmdLineParser = new DefaultParser();
		CommandLine commandLine = cmdLineParser.parse(options, args);
		
		return commandLine;
	}
	
	private Options createOptions()
	{
		final Option portOption = Option.builder(PORT_OPTION_STR)
				.required()
				.hasArg(true)
				.desc("Dynalite listening port")
				.build();
		final Option jdbcEndpointOption = Option.builder(JDBC_ENDPOINT_OPTION_STR)
				.required()
				.hasArg(true)
				.desc("JDBC URL for Dynalite backend storage")
				.build();
		final Option userOption = Option.builder(USER_OPTION_STR)
				.required()
				.hasArg(true)
				.desc("Backend database user")
				.build();
		final Option passwordOption = Option.builder(PASSWORD_OPTION_STR)
				.required()
				.hasArg(true)
				.desc("Backend database password")
				.build();
		final Option dynaliteScriptDirOption = Option.builder(DYNALITE_SCRIPT_DIR_OPTION_STR)
				.required()
				.hasArg(true)
				.desc("Dynalite script directory (verify '" + DynaliteJavaConfig.DYNALITE_MAIN + "' exists under this directory)")
				.build();
		
		final Options options = new Options();
		options.addOption(portOption);
		options.addOption(jdbcEndpointOption);
		options.addOption(userOption);
		options.addOption(passwordOption);
		options.addOption(dynaliteScriptDirOption);
		
		return options;
	}
	
	private Options createHelpOptions()
	{
		final Option helpOption = Option.builder(HELP_OPTION_STR)
				.required(false)
				.hasArg(false)
				.desc("Shows this message")
				.build();
		final Option portOption = Option.builder(PORT_OPTION_STR)
				.required(false)
				.hasArg(true)
				.desc("Set Listening port")
				.build();
		final Option jdbcEndpointOption = Option.builder(JDBC_ENDPOINT_OPTION_STR)
				.required(false)
				.hasArg(true)
				.desc("Set jdbcEndpoint")
				.build();
		final Option userOption = Option.builder(USER_OPTION_STR)
				.required(false)
				.hasArg(true)
				.desc("Set user")
				.build();
		final Option passwordOption = Option.builder(PASSWORD_OPTION_STR)
				.required(false)
				.hasArg(true)
				.desc("Set password")
				.build();
		final Option dynaliteScriptDirOption = Option.builder(DYNALITE_SCRIPT_DIR_OPTION_STR)
				.required(false)
				.hasArg(true)
				.desc("Set dynaliteScriptDir")
				.build();
		
		final Options options = new Options();
		options.addOption(portOption);
		options.addOption(jdbcEndpointOption);
		options.addOption(userOption);
		options.addOption(passwordOption);
		options.addOption(dynaliteScriptDirOption);
		options.addOption(helpOption);
		
		return options;
	}
	
}
