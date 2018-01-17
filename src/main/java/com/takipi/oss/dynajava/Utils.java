package com.takipi.oss.dynajava;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Utils
{
	private final static Logger logger = LoggerFactory.getLogger(Utils.class);
	
	private static File createFileEntry(ZipEntry ze, String dirName) throws IOException
	{
		File entry = new File(dirName, ze.getName());
		
		if (ze.isDirectory())
		{
			entry.mkdirs();
		}
		else
		{
			entry.getParentFile().mkdirs();
		}
		
		return entry;
	}
	
	private static void copyFileContent(ZipInputStream zipInput, File toFile) throws IOException
	{
		FileOutputStream fos = new FileOutputStream(toFile);
		byte[] buffer = new byte[1024];
		int len;
		
		while ((len = zipInput.read(buffer)) > 0)
		{
			fos.write(buffer, 0, len);
		}
		
		fos.close();
	}
	
	public static boolean unzip(InputStream is, String destDir)
	{
		File dir = new File(destDir);
		dir.mkdirs();
		
		try
		{
			ZipInputStream zis = new ZipInputStream(is);
			ZipEntry ze = zis.getNextEntry();
			
			while(ze != null)
			{
				File newFile = createFileEntry(ze, destDir);
				
				if (! ze.isDirectory()) 
				{
					copyFileContent(zis, newFile);
				}
				
				zis.closeEntry();
				ze = zis.getNextEntry();
			}
			
			zis.closeEntry();
			zis.close();
			is.close();
		}
		catch (Exception e)
		{
			logger.error("Error unzipping", e);
			return false;
		} 
		
		return true;
	}
	
	public static File createTempDirectory(String prefix) throws IOException
	{
		final File temp;
		
		temp = File.createTempFile(prefix, Long.toString(System.nanoTime()));
		
		if ((!(temp.delete())) || 
			(!(temp.mkdir())))
		{
			throw new IOException("Failed to create temp directory: " + temp.getAbsolutePath());
		}
		
		return (temp);
	}
	
	public static boolean createDirectory(String dirName)
	{
		File dir = new File(dirName);
		
		if (dir.exists())
		{
			return dir.isDirectory();
		}
		
		return dir.mkdirs();
	}
}
