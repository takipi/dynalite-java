
package com.takipi.oss.dynajava;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
	public final static String TEMPDIR_OPTION_STR = "tempdir";
	public final static String SKIP_DYNALITE_EXTRACTION_OPTION_STR = "skipExtract";
	public final static String DYNAMITE_COUNT_OPTION_STR = "dynamite";
	public final static String TABLES_MAPPING_PATH = "tablesMappingPath";
	public final static String DB_PER_TABLE = "dbPerTable";
	public final static String CONNECTION_POOL_SIZE = "connectionPoolSize";
	
	private final static Logger logger = LoggerFactory.getLogger(DynaliteJavaMain.class);
	
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
		
		if (!isDynaliteScriptValid(config.getDynaliteScriptDir()))
		{
			String targetDir = config.getTempdir();
			
			if (targetDir == null)
			{
				try
				{
					File tempFile = Utils.createTempDirectory(("dynalite"));
					targetDir = tempFile.getAbsolutePath();
				} catch (IOException e)
				{
					logger.error(e.getMessage());
					logger.error("Cannot create scripts directory. Exiting");
					
					return;
				}
			}
			
			config.setDynaliteScriptDir(targetDir);
			
			if (!Utils.createDirectory(targetDir))
			{
				logger.error("Cannot create directory: {}", targetDir);
				
				return;
			}
			
			if (!config.getSkipExtraction())
			{
				extractDynaliteScriptZip(config.getDynaliteScriptDir());
			}
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
	
	private boolean isDynaliteScriptValid(String dirName)
	{
		if (dirName == null)
		{
			return false;
		}
		
		File dir = new File(dirName);
		
		if (!dir.exists())
		{
			return false;
		}
		
		return (new File(dirName + File.separator + "cli.js")).exists();
	}
	
	private boolean extractDynaliteScriptZip(String dynaliteScriptDest)
	{
		InputStream dynaZipStream = this.getClass().getResourceAsStream(DynaliteJavaConfig.DYNALITE_SCRIPT_ZIP_PATH);
		return Utils.unzip(dynaZipStream, dynaliteScriptDest);
	}

	private DynaliteJavaConfig parseCmdlineOptions(final String[] args)
	{
		DynaliteJavaConfig config = null;
		
		Options options = createOptions();
		
		CommandLine cmdLine = null;
		
		try 
		{
			cmdLine = parseOptions(options, args);
		
			if (cmdLine == null)
			{
				return null;
			}
			
			if (cmdLine.hasOption(HELP_OPTION_STR))
			{
				printUsage(options);
				
				return null;
			}
		}
		catch (ParseException parseException)
		{
			logger.error("Unable to parse command-line arguments " +	Arrays.toString(args) + " due to: " + parseException);
			printUsage(options);
			
			return null;
		}
		
		config = fillConfig(cmdLine);
		
		return config;
	}
	
	private void printUsage(Options options)
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java -jar <jar_file>", options, true);
	}
	
	private DynaliteJavaConfig fillConfig(CommandLine cmdLine)
	{
		int port = -1;
		DynaliteJavaConfig config = new DynaliteJavaConfig();
		
		if (cmdLine.hasOption(PORT_OPTION_STR))
		{
			String portStr = cmdLine.getOptionValue(PORT_OPTION_STR);
			
			try 
			{
				port = Integer.parseInt(portStr);
			} 
			catch (Exception e) 
			{
				System.out.println("Error parsing port number: " + portStr);
			}
			
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
		
		String dynaliteRdbmsPasswordEnv = System.getenv("DYNALITE_RDBMS_PASSWORD");
		
		if ((dynaliteRdbmsPasswordEnv != null) &&
			!(dynaliteRdbmsPasswordEnv.isEmpty()))
		{
			config.setPassword(dynaliteRdbmsPasswordEnv);
		}
		
		if (cmdLine.hasOption(PASSWORD_OPTION_STR))
		{
			config.setPassword(cmdLine.getOptionValue(PASSWORD_OPTION_STR));
		}
		
		if (cmdLine.hasOption(TEMPDIR_OPTION_STR))
		{
			String tempdir = cmdLine.getOptionValue(TEMPDIR_OPTION_STR);
			config.setTempdir(tempdir);
		}
		
		if (cmdLine.hasOption(DYNALITE_SCRIPT_DIR_OPTION_STR))
		{
			String dynaliteScriptDir = cmdLine.getOptionValue(DYNALITE_SCRIPT_DIR_OPTION_STR);
			
			if (!isDynaliteScriptValid(dynaliteScriptDir))
			{
				logger.error("Dynalite script directory is not valid");
				
				return null;
			}
			
			config.setDynaliteScriptDir(dynaliteScriptDir);
		}
		
		if (cmdLine.hasOption(SKIP_DYNALITE_EXTRACTION_OPTION_STR))
		{
			config.setSkipExtraction(true);
		}
		
		if(cmdLine.hasOption(DB_PER_TABLE))
		{
			config.setDbPerTable(true);
		}
		
		if(cmdLine.hasOption(CONNECTION_POOL_SIZE))
		{
			String connectionPoolSizeStr = cmdLine.getOptionValue(CONNECTION_POOL_SIZE);
			
			try 
			{
				int connectionPoolSize = Integer.parseInt(connectionPoolSizeStr);
				config.setConnectionPoolSize(connectionPoolSize);
			} 
			catch (Exception e) 
			{
				System.out.println("Error parsing connection pools size number: " + connectionPoolSizeStr);
			}
		}
		
		if (cmdLine.hasOption(DYNAMITE_COUNT_OPTION_STR))
		{
			String dynamiteCountStr = cmdLine.getOptionValue(DYNAMITE_COUNT_OPTION_STR);
			int dynamiteCount;
			
			try 
			{
				dynamiteCount = Integer.parseInt(dynamiteCountStr);
			} 
			catch (Exception e) 
			{
				System.out.println("Error parsing dynamite count number: " + dynamiteCountStr);
				dynamiteCount = 1;
			}
			
			config.setDynamiteCount(dynamiteCount);
		}
		
		if (cmdLine.hasOption(TABLES_MAPPING_PATH))
		{
			config.setTableNamesMappingFile(cmdLine.getOptionValue(TABLES_MAPPING_PATH));
		}
		
		return config;
	}
	
	private boolean isValidJdbcEndpoint(String jdbcEndpoint)
	{
		if (jdbcEndpoint == null)
		{
			return false;
		}
		
		if ((jdbcEndpoint.startsWith("jdbc:h2:")) ||
			(jdbcEndpoint.startsWith("jdbc:mysql:")) ||
			(jdbcEndpoint.startsWith("jdbc:postgresql")))
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
	
	private CommandLine parseOptions(Options options, String[] args) throws ParseException
	{
		CommandLineParser cmdLineParser = new DefaultParser();
		CommandLine commandLine = cmdLineParser.parse(options, args);
		
		return commandLine;
	}
	
	private Options createOptions()
	{
		Option helpOption = Option.builder(HELP_OPTION_STR)
				.required(false)
				.hasArg(false)
				.desc("Shows this message")
				.build();
		Option portOption = Option.builder(PORT_OPTION_STR)
				.required(false)
				.hasArg(true)
				.desc("Dynalite listening port")
				.build();
		Option jdbcEndpointOption = Option.builder(JDBC_ENDPOINT_OPTION_STR)
				.required(false)
				.hasArg(true)
				.desc("JDBC URL for Dynalite backend storage")
				.build();
		Option userOption = Option.builder(USER_OPTION_STR)
				.required(false)
				.hasArg(true)
				.desc("Backend database user")
				.build();
		Option passwordOption = Option.builder(PASSWORD_OPTION_STR)
				.required(false)
				.hasArg(true)
				.desc("Backend database password")
				.build();
		Option dynaliteScriptDirOption = Option.builder(DYNALITE_SCRIPT_DIR_OPTION_STR)
				.required(false)
				.hasArg(true)
				.desc("Dynalite script directory (verify '" + DynaliteJavaConfig.DYNALITE_MAIN + "' exists under this directory)")
				.build();
		Option tmpdirOption = Option.builder(TEMPDIR_OPTION_STR)
				.required(false)
				.hasArg(true)
				.desc("Temporary directory for Dynalite application")
				.build();
		Option skipDynaliteExtractionOption = Option.builder(SKIP_DYNALITE_EXTRACTION_OPTION_STR)
				.required(false)
				.hasArg(false)
				.desc("Skip dynalite extraction")
				.build();
		Option dbPerTable = Option.builder(DB_PER_TABLE)
				.required(false)
				.hasArg(false)
				.desc("Save each table in its own database")
				.build();
		Option dynamiteOption = Option.builder(DYNAMITE_COUNT_OPTION_STR)
				.required(false)
				.hasArg(true)
				.type(Integer.class)
				.desc("Enable dynamite")
				.build();
		Option connectionPoolsSizeOption = Option.builder(CONNECTION_POOL_SIZE)
				.required(false)
				.hasArg(true)
				.type(Integer.class)
				.desc("Connection pools size")
				.build();		
		Option tablesMappingPath = Option.builder(TABLES_MAPPING_PATH)
				.required(false)
				.hasArg(true)
				.desc("Json path of table names mapping")
				.build();
		
		Options options = new Options();
		options.addOption(helpOption);
		options.addOption(portOption);
		options.addOption(jdbcEndpointOption);
		options.addOption(userOption);
		options.addOption(passwordOption);
		options.addOption(dynaliteScriptDirOption);
		options.addOption(tmpdirOption);
		options.addOption(skipDynaliteExtractionOption);
		options.addOption(dynamiteOption);
		options.addOption(dbPerTable);
		options.addOption(connectionPoolsSizeOption);
		options.addOption(tablesMappingPath);
		
		return options;
	}
}
