package com.takipi.oss.dynajava;

import java.io.File;

public class DynaliteJavaConfig
{
	public static final String NODE_VERSION = "0.12";
	public static final String DYNALITE_MAIN = "cli.js";
	public static final String NODE_MODULES = "node_modules";
	
	private int port;
	private String jdbcEndpoint;
	private String user;
	private String password;
	private File dynaliteScriptDir;
	
	public DynaliteJavaConfig(int port, String jdbcEndpoint, String user, String password, File dynaliteScriptDir)
	{
		this.port = port;
		this.jdbcEndpoint = jdbcEndpoint;
		this.user = user;
		this.password = password;
		this.dynaliteScriptDir = dynaliteScriptDir;
	}
	
	public DynaliteJavaConfig()
	{
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
		// TODO Auto-generated method stub
		return user;
	}

	public String getPassword()
	{
		// TODO Auto-generated method stub
		return password;
	}

	public File getDynaliteScriptDir()
	{
		// TODO Auto-generated method stub
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

	public void setDynaliteScriptDir(File dynaliteScriptDir)
	{
		this.dynaliteScriptDir = dynaliteScriptDir;
	}
	
}
