package com.takipi.oss.dynajava;

import java.io.File;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.apigee.trireme.core.NodeEnvironment;
import io.apigee.trireme.core.NodeScript;
import io.apigee.trireme.core.ScriptFuture;
import io.apigee.trireme.core.ScriptStatus;

public class DynaliteJavaServer
{
	private DynaliteJavaConfig config;
	
	public DynaliteJavaServer(DynaliteJavaConfig config)
	{
		this.config = config;
	}
	
	public void start() throws Exception
	{
		List<ScriptFuture> scriptFutures = new ArrayList<ScriptFuture>();
		
		handleJDBCEndpoint();
		
		String scriptName = config.isDbPerTable() ? DynaliteJavaConfig.DYNAMITE_PROXY_MAIN : DynaliteJavaConfig.DYNALITE_MAIN;
		
		String[] args = buildArgs();
		
		File dynaliteProxyScriptFile = handleDynaliteScriptFile(scriptName);
		
		NodeScript script = getNodeScript(dynaliteProxyScriptFile, args);
		
		scriptFutures.add(script.execute());
		
		if (config.isDbPerTable())
		{
			int counter = 0;
			
			while (counter < config.getDynamiteCount())
			{
				File dynaliteScriptFile = handleDynaliteScriptFile(DynaliteJavaConfig.DYNALITE_MAIN);
				
				args = buildArgs();
				
				script = getNodeScript(dynaliteScriptFile, args);
				
				scriptFutures.add(script.execute());
				
				counter++;
			}
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
	
	private String[] buildArgs()
	{
		ArrayList<String> array = new ArrayList<>();
		
		array.addAll(Arrays.asList(new String [] {
				"--port", Integer.toString(config.getIncrementedPort()),
				"--jdbc", config.getJdbcEndpoint()}));
		
		if (config.getUser() != null)
		{
			array.add("--jdbcUser");
			array.add(config.getUser());
		}
		
		if (config.getPassword() != null)
		{
			array.add("--jdbcPassword");
			array.add(config.getPassword());
		}
		
		if (config.getDynamiteCount() > 1)
		{
			array.add("--dynamite");
			array.add(Integer.toString(config.getDynamiteCount()));
		}
		
		if(config.getTablesMappingPath() != null)
		{
			array.add("--tablesMappingPath");
			array.add(config.getTablesMappingPath());
		}

		array.add(config.isDbPerTable() ? "--dbPerTable" : "");
		
		String [] args = new String[array.size()];
		
		return array.toArray(args);
	}

	private File handleDynaliteScriptFile(String scriptName)
	{
		File scriptDir = new File(config.getDynaliteScriptDir());
		
		if (!scriptDir.exists())
		{
			throw new IllegalStateException(
					"Could find dynalite dev location in expected directory: " +
											scriptDir.getAbsolutePath());
		}
		
		File devNodeModules = new File(config.getDynaliteScriptDir(),
				DynaliteJavaConfig.NODE_MODULES);
		
		if (!devNodeModules.exists())
		{
			throw new IllegalStateException(
					"You must run 'npm install' for: " + scriptDir.getAbsolutePath());
		}
		
		return new File(scriptDir, scriptName);
	}
	
	private String handleJDBCEndpoint() throws Exception
	{
		// To do: if we want to support other vendors aside h2/mysql, we need to improve this:
		if (config.getJdbcEndpoint().startsWith("jdbc:h2:"))
		{
			registerDriver("org.h2.Driver");
		}
		else if (config.getJdbcEndpoint().startsWith("jdbc:mysql:"))
		{
			registerDriver("com.mysql.cj.jdbc.Driver");
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
	
	private NodeScript getNodeScript(File dynaliteScriptFile, String[] args) throws Exception
	{
		NodeEnvironment env = new NodeEnvironment();
		
		NodeScript script = env.createScript(DynaliteJavaConfig.DYNALITE_MAIN,
				dynaliteScriptFile, args);
		
		script.setNodeVersion(DynaliteJavaConfig.NODE_VERSION);

		return script;
	}
}
