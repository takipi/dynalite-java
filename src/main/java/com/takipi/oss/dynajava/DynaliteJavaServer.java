package com.takipi.oss.dynajava;

import java.io.File;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;

import io.apigee.trireme.core.NodeEnvironment;
import io.apigee.trireme.core.NodeScript;
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
		handleJDBCEndpoint();
		
		File dynaliteScriptFile = handleDynaliteScriptFile();
		
		NodeEnvironment env = new NodeEnvironment();
		String [] args = buildArgs();
		NodeScript script = env.createScript(DynaliteJavaConfig.DYNALITE_MAIN,
				dynaliteScriptFile, args);
		
		script.setNodeVersion(DynaliteJavaConfig.NODE_VERSION);
		ScriptStatus status = script.execute().get();
		
		if (!status.isOk())
		{
			throw new IllegalStateException(
					"Error starting dynalite. Code: " + status.getExitCode() + ", Cause: " + status.getCause());
		}
	}
	
	private String[] buildArgs()
	{
		ArrayList<String> array = new ArrayList<>();
		
		array.addAll(Arrays.asList(new String [] {
				"--port", Integer.toString(config.getPort()),
				"--jdbc", config.getJdbcEndpoint()}));
		
		if (config.getUser() != null)
		{
			array.add("--user");
			array.add(config.getUser());
		}
		
		if (config.getPassword() != null)
		{
			array.add("--password");
			array.add(config.getPassword());
		}

		String [] args = new String[array.size()];
		
		return array.toArray(args);
	}

	private File handleDynaliteScriptFile()
	{
		File scriptDir = new File(config.getDynaliteScriptDir());
		if (!scriptDir.exists())
		{
			throw new IllegalStateException(
					"Could find dynalite dev location in expected directory: " +
											scriptDir.getAbsolutePath());
		}
		
		File devNodeModules = new File(config.getDynaliteScriptDir(), DynaliteJavaConfig.NODE_MODULES);
		
		if (!devNodeModules.exists())
		{
			throw new IllegalStateException(
					"You must run 'npm install' for: " + scriptDir.getAbsolutePath());
		}
		
		return new File(scriptDir, DynaliteJavaConfig.DYNALITE_MAIN);
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
}
