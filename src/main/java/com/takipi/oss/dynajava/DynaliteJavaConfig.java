package com.takipi.oss.dynajava;

public class DynaliteJavaConfig
{
	public static final String NODE_VERSION = "0.10";
	public static final String DYNAMITE_PROXY_MAIN = "proxy.js";
	public static final String DYNALITE_MAIN = "cli.js";
	public static final String NODE_MODULES = "node_modules";
	public static final String DYNALITE_SCRIPT_ZIP_PATH = "/dynalite.zip";
	
	public static final String DYNALITE_DEFAULT_JDBC_STR = "jdbc:h2:tcp://localhost:9999/dynalite;MODE=mysql;MVCC=true";
	public static final int DYNALITE_DEFAULT_PORT = 4000;
	
	private int port;
	private String jdbcEndpoint;
	private String user;
	private String password;
	private String dynaliteScriptDir;
	private String tempdir;
	private boolean skipExtraction;
	private int dynamiteCount;
	
	public DynaliteJavaConfig()
	{
		setDefault();
	}

	private void setDefault()
	{
		this.port = DYNALITE_DEFAULT_PORT;
		this.jdbcEndpoint = DYNALITE_DEFAULT_JDBC_STR;
		this.tempdir = null;
		this.skipExtraction = false;
		this.dynamiteCount = 1;
	}

	public int getPort()
	{
		return port;
	}

	public String getJdbcEndpoint()
	{
		return jdbcEndpoint;
	}

	public String getUser()
	{
		return user;
	}

	public String getPassword()
	{
		return password;
	}
	
	public String getDynaliteScriptDir()
	{
		return dynaliteScriptDir;
	}
	
	public boolean getSkipExtraction()
	{
		return skipExtraction;
	}
	
	public void setPort(int port)
	{
		this.port = port;
	}

	public void setJdbcEndpoint(String jdbcEndpoint)
	{
		this.jdbcEndpoint = jdbcEndpoint;
	}

	public void setUser(String user)
	{
		this.user = user;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public void setDynaliteScriptDir(String dynaliteScriptDir)
	{
		this.dynaliteScriptDir = dynaliteScriptDir;
	}

	public void setTempdir(String tempdir)
	{
		this.tempdir = tempdir;
	}
	
	public void setSkipExtraction(boolean skipExtraction) 
	{
		this.skipExtraction = skipExtraction;
	}

	public String getTempdir()
	{
		return tempdir;
	}
	
	public void setDynamiteCount(int dynamiteCount)
	{
		this.dynamiteCount = dynamiteCount;
	}
	
	public int getDynamiteCount()
	{
		return dynamiteCount;
	}
}
