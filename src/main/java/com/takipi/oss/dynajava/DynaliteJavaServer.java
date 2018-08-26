package com.takipi.oss.dynajava;

import java.io.File;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.apigee.trireme.core.NodeEnvironment;
import io.apigee.trireme.core.NodeScript;
import io.apigee.trireme.core.ScriptFuture;
import io.apigee.trireme.core.ScriptStatus;

public class DynaliteJavaServer
{
	private final static Logger logger = LoggerFactory.getLogger(DynaliteJavaServer.class);
	
	private DynaliteJavaConfig config;
	
	public DynaliteJavaServer(DynaliteJavaConfig config)
	{
		this.config = config;
	}
	
	public void start() throws Exception
	{
		makeSureNodeModulesInstalled(new File(config.getDynaliteScriptDir()));
		handleJDBCEndpoint();
		
		List<ScriptFuture> scriptFutures = new ArrayList<ScriptFuture>();
		
		if (isDynamiteEnabled()) 
		{
			scriptFutures.add(startDynamiteProxy(config.getPort()));
			scriptFutures.addAll(startDynamiteInstances(config.getPort() + 1));
		}
		else
		{
			scriptFutures.add(startDynaliteInstance(config.getPort()));
		}
		
		for (ScriptFuture future : scriptFutures)
		{
			ScriptStatus status = future.get();
			
			if (!status.isOk())
			{
				throw new IllegalStateException(
						"Error starting dynalite. Code: " + status.getExitCode() + ", Cause: " + status.getCause());
			}
		}
	}
	
	private boolean isDynamiteEnabled()
	{
		return config.getDynamiteCount() > 1;
	}
	
	private ScriptFuture startDynamiteProxy(int port) throws Exception
	{
		String[] args = new String[] {
			"--port", Integer.toString(port),
			"--dynamite", Integer.toString(config.getDynamiteCount()),
			"--tablesMappingPath", config.getTablesMappingPath()
		};
		
		return executeNodeScript(DynaliteJavaConfig.DYNAMITE_PROXY_MAIN, args);
	}
	
	private List<ScriptFuture> startDynamiteInstances(int portBase) throws Exception
	{
		List<ScriptFuture> result = new ArrayList<ScriptFuture>();
		
		for (int i = 0; i < config.getDynamiteCount(); i++)
		{
			String[] args = new String[] {
				"--port", Integer.toString(portBase + i),
				"--jdbcUser", (config.getUser() == null ? "" : config.getUser()),
				"--jdbcPassword", (config.getPassword() == null ? "" : config.getPassword()),
				"--jdbc", config.getJdbcEndpoint(),
				"--connectionPoolMaxSize", Integer.toString(config.getConnectionPoolSize()),
				config.isDbPerTable() ? "--dbPerTable" : ""
			};
			
			result.add(executeNodeScript(DynaliteJavaConfig.DYNALITE_MAIN, args));
		}
		
		return result;
	}
	
	private ScriptFuture startDynaliteInstance(int port) throws Exception
	{
		String[] args = new String[] {
			"--port", Integer.toString(port),
			"--jdbcUser", (config.getUser() == null ? "" : config.getUser()),
			"--jdbcPassword", (config.getPassword() == null ? "" : config.getPassword()),
			"--jdbc", config.getJdbcEndpoint(),
			config.isDbPerTable() ? "--dbPerTable" : ""
		};
		
		return executeNodeScript(DynaliteJavaConfig.DYNALITE_MAIN, args);
	}
	
	private ScriptFuture executeNodeScript(String scriptName, String[] args) throws Exception
	{
		File scriptFile = new File(config.getDynaliteScriptDir(), scriptName);
		
		NodeEnvironment env = new NodeEnvironment();
		NodeScript script = env.createScript(scriptFile.getName(), scriptFile, args);
		
		script.setNodeVersion(DynaliteJavaConfig.NODE_VERSION);
		
		logger.info("Executing {} with args {}. (loc: {})", scriptName, args, scriptFile.getAbsolutePath());
		
		return script.execute();
	}
	
	private String handleJDBCEndpoint() throws Exception
	{
		// To do: if we want to support other vendors aside h2/mysql, we need to improve this:
		//
		if (config.getJdbcEndpoint().startsWith("jdbc:h2:"))
		{
			registerDriver("org.h2.Driver");
		}
		else if (config.getJdbcEndpoint().startsWith("jdbc:mysql:"))
		{
			registerDriver("com.mysql.cj.jdbc.Driver");
		}
		else if (config.getJdbcEndpoint().startsWith("jdbc:postgresql:"))
		{
			registerDriver("org.postgresql.Driver");
			
			PostgresInitializer.createDatabaseIfNotExist(config.getJdbcEndpoint(),
				config.getUser(), config.getPassword());
		}
		else
		{
			throw new IllegalStateException("Unsupported jdbc: " + config.getJdbcEndpoint());
		}
		
		return config.getJdbcEndpoint();
	}
	
	private void registerDriver(String driverName) throws Exception
	{
		java.sql.Driver driver = (java.sql.Driver)(Class
				.forName(driverName, true, DynaliteJavaServer.class.getClassLoader()).newInstance());
		DriverManager.registerDriver(driver);
	}
	
	private void makeSureNodeModulesInstalled(File dynaliteScriptsDir) 
	{
		File devNodeModules = new File(dynaliteScriptsDir, DynaliteJavaConfig.NODE_MODULES);
		
		if (!devNodeModules.exists())
		{
			throw new IllegalStateException("You must run 'npm install' for: " + dynaliteScriptsDir.getAbsolutePath());
		}
	}
}
