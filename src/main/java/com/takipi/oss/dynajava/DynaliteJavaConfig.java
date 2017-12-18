package com.takipi.oss.dynajava;

public class DynaliteJavaConfig
{
	public static final String NODE_VERSION = "0.12";
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

	public DynaliteJavaConfig(int port, String jdbcEndpoint, String user, String password, String dynaliteScriptDir)
	{
		this.port = port;
		this.jdbcEndpoint = jdbcEndpoint;
		this.user = user;
		this.password = password;
		this.dynaliteScriptDir = dynaliteScriptDir;
		this.tempdir = null;
	}
	
	public DynaliteJavaConfig()
	{
		setDefault();
	}

	private void setDefault()
	{
		this.port = DYNALITE_DEFAULT_PORT;
		this.jdbcEndpoint = DYNALITE_DEFAULT_JDBC_STR;
		this.tempdir = null;
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

	public String getTempdir()
	{
		return tempdir;
	}


	
}
