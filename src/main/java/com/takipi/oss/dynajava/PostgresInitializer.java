package com.takipi.oss.dynajava;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostgresInitializer
{
	private static Logger logger = LoggerFactory.getLogger(PostgresInitializer.class);
	
	protected static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	private static final String DEFAULT_DB_NAME = "postgres";
	private static final String CREATE_DATABASE = "CREATE DATABASE ";
	
	public static boolean createDatabaseIfNotExist(String jdbcUrl, String userName, String password)
	{
		Statement stat = null;
		Connection conn = null;
		
		String dbName = getDbNameFromUrl(jdbcUrl);
		String protocolAndPort = getProtocolAndDomainFromUrl(jdbcUrl);
		
		if (dbName == null ||
			protocolAndPort == null)
		{
			logger.warn("Unable to parse jdbc url: {}", jdbcUrl);
			
			// return true because maybe the user have a weird jdbc which is actually
			//	work. so no reason to stop tomcat initialization for it
			//
			return true;
		}
		
		try
		{
			logger.info("Making sure database {} exists, with jdbc url {} ", dbName, protocolAndPort);
			
			conn = getRawConnection(protocolAndPort, dbName, userName, password);
			
			if (conn != null)
			{
				logger.debug("database {} already exists", dbName);
				return true;
			}
			
			conn = getRawConnection(protocolAndPort, DEFAULT_DB_NAME, userName, password);
			
			if (conn == null)
			{
				logger.error("Unable to connec to either {} or {}", dbName, DEFAULT_DB_NAME);
				return false;
			}
			
			stat = conn.createStatement();
			
			String createSql = CREATE_DATABASE + dbName;
			stat.executeUpdate(createSql);
			
			return true;
		}
		catch (Exception e)
		{
			logger.error("Caught an exception while creating database {}, if not exists", dbName, e);
			
			return false;
		}
		finally
		{
			try
			{
				if (stat != null)
				{
					stat.close();
				}
			}
			catch (Exception e)
			{
				logger.error("Error closing connection", e);
			}
			
			try
			{
				if (conn != null)
				{
					conn.close();
				}
			}
			catch (Exception e)
			{
				logger.error("Error closing connection", e);
			}
		}
	}
	
	private static Connection getRawConnection(String protocolAndPort, String dbName, String userName, String password)
	{
		String actualJdbcUrl = protocolAndPort + "/" + dbName;
		
		try
		{
			return DriverManager.getConnection(actualJdbcUrl, userName, password);
		}
		catch (SQLException e)
		{
			// e.getMessage() on purpose, it may happen and it is fine
			//
			logger.warn("Error creating SQL connection with url {}", actualJdbcUrl, e.getMessage());
			
			return null;
		}
	}
	
	private static String getDbNameFromUrl(String jdbcUrl)
	{
		Pattern pattern = Pattern.compile("jdbc:postgresql:?:\\/\\/[^:]*:\\d+\\/([^?]+)(\\?[\\S]+)?");
		
		try
		{
			Matcher matcher = pattern.matcher(jdbcUrl);
			
			if (matcher.find())
			{
				return matcher.group(1);
			}
		}
		catch (Exception e)
		{
			logger.error("Failed to get database name from url ", jdbcUrl, e.getMessage());
		}
		
		return null;
	}
	
	private static String getProtocolAndDomainFromUrl(String jdbcUrl)
	{
		Pattern pattern = Pattern.compile("(jdbc:postgresql:?://[^:]*:\\d+)/?(\\S+)?");
		
		try
		{
			Matcher matcher = pattern.matcher(jdbcUrl);
			
			if (matcher.find())
			{
				return matcher.group(1);
			}
		}
		catch (Exception e)
		{
			logger.error("Failed to get protocol And domain from url {} ", jdbcUrl, e.getMessage());
		}
		
		return null;
	}
}
