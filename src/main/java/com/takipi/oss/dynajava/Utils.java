package com.takipi.oss.dynajava;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class Utils
{
	private static File createFileEntry(ZipEntry ze, String dirName) throws IOException
	{
		File entry = null;
		String fullPathName = dirName + File.separator + ze.getName();
		entry = new File(fullPathName);
		
		if (ze.isDirectory())
		{
			entry.mkdirs();
		}
		else
		{
			File parentDir = new File(entry.getParent());
			parentDir.mkdirs();
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

	    if(!dir.exists())
	    {
	        dir.mkdirs();
	    }

	    try
	    {
	        ZipInputStream zis = new ZipInputStream(is);
	        ZipEntry ze = zis.getNextEntry();

	        while(ze != null)
	        {
	            File newFile = createFileEntry(ze, destDir);
	            //System.out.println("Unzipping to " + newFile.getAbsolutePath());
	            if (! ze.isDirectory()) {
	                copyFileContent(zis, newFile);
	            }
	            zis.closeEntry();
	            ze = zis.getNextEntry();
	        }

	        zis.closeEntry();
	        zis.close();
	        is.close();
	    } catch (java.util.zip.ZipException e)
	    {
	        e.printStackTrace();
	        return false;
	    } catch (IOException e)
	    {
	        e.printStackTrace();
	        return false;
	    }
	    
	    return true;
	}
}
