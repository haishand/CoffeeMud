package com.planet_ink.coffee_mud.core;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.collections.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;



/*
   Copyright 2001-2015 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Resources
{
	private static final Resources[] rscs=new Resources[256];
	private static boolean 	 compress=false;
	private static Object propResourceSync=new Object();
	private static Map<String,Map<String,String>> propResources;

	/**
	 * Internal tree map that uses case-insensitive string keys.
	 */
	private final Map<String,Object> resources=new STreeMap<String,Object>(new Comparator<String>()
	{
		@Override
		public int compare(String o1, String o2)
		{
			if(o1==null)
			{
				if(o2==null)
					return 0;
				return -1;
			}
			else
			if(o2==null)
				return 1;
			return o1.compareToIgnoreCase(o2);
		}
	});

	/**
	 * Internal class that serves the purpose of both identifying
	 * a compressed text resource, and holding the bytes of that compression.
	 * @author Bo Zimmerman
	 *
	 */
	private static class CompressedResource
	{
		public byte[] data;
		
		/**
		 * Constructs a CompressedResource object from the given bytes
		 * @param d
		 */
		public CompressedResource(byte[] d) 
		{ 
			data=d;
		}
	}

	/**
	 * Constructs a new CMLib object for the current thread group.
	 */
	public Resources()
	{
		super();
		final char c=Thread.currentThread().getThreadGroup().getName().charAt(0);
		if(rscs[c]==null)
			rscs[c]=this;
	}

	/**
	 * Forces the current thread group to share a Resources object with the one at the given
	 * threadcode.  The one at the threadcode should already have been created before
	 * calling.
	 * @param threadCode the threadcode with an existing Resources
	 */
	public static void shareWith(char code)
	{
		if(Thread.currentThread().getThreadGroup().getName().charAt(0)==code)
			initialize();
		else
		if(rscs[code]!=null)
			rscs[Thread.currentThread().getThreadGroup().getName().charAt(0)]=rscs[code];
		else
		{
			initialize();
			rscs[code]=rscs[Thread.currentThread().getThreadGroup().getName().charAt(0)];

		}
	}

	/**
	 * Creates and returns a new Resources object for the current calling thread
	 * @return a new Resources object for the current calling thread
	 */
	public static final Resources initialize() 
	{ 
		return new Resources(); 
	}
	
	/**
	 * Returns the Resources instance tied to this particular thread group, or a new one if not yet created.
	 * @return the Resources instance tied to this particular thread group, or a new one if if not yet created.
	 */
	public static final Resources instance()
	{
		final Resources r=r();
		if(r==null)
			return new Resources();
		return r;
	}
	
	/**
	 * Returns the Resources instance tied to the given thread group, or null if not yet created.
	 * @param c the code for the thread group to return (0-255)
	 * @return the Resources instance tied to the given thread group, or null if not yet created.
	 */
	public static final Resources instance(final char c)
	{ 
		return rscs[c];
	}
	
	/**
	 * Returns the Resources instance tied to this particular thread group, or null if not yet created.
	 * @return the Resources instance tied to this particular thread group, or null if not yet created.
	 */
	private static final Resources r()
	{ 
		return rscs[Thread.currentThread().getThreadGroup().getName().charAt(0)];
	}

	/**
	 * Creates and returns a new Resources object for the current calling thread
	 * @return a new Resources object for the current calling thread
	 */
	public static final Resources newResources()
	{ 
		return new Resources();
	}

	/**
	 * Removes all resources for the current calling thread group
	 */
	public static final void clearResources()
	{
		r()._clearResources();
	}
	
	/**
	 * Saves any cached resource properties for the current calling thread group.
	 * Removes all resources for the current calling thread group.
	 */
	public static final void shutdown()
	{
		Resources.savePropResources();
		r()._clearResources();
	}

	/**
	 * Removes the current resources for the current calling thread group.
	 * @param ID the resource ID to remove, case insensitive as always
	 */
	public static final void removeResource(final String ID)
	{ 	
		r()._removeResource(ID);
	}
	
	/**
	 * Does a case-insensitive instring search of all resources for the
	 * current calling thread group and returns an iterator of all FULL keys
	 * that match.
	 * @param srch the instring string to search for
	 * @return an iterator of all matching full keys
	 */
	public static final Iterator<String> findResourceKeys(final String srch)
	{
		return r()._findResourceKeys(srch);
	}
	
	/**
	 * Returns the raw resource object for the given case-insensitive ID, from
	 * the resources for the current calling thread group.
	 * @param ID the resource ID to return
	 * @return the raw object at that resource ID, or null of not found
	 */
	public static final Object getResource(final String ID)
	{
		return r()._getResource(ID);
	}
	
	/**
	 * Adds or replaces a raw resource object at the given case-insensitive ID, into
	 * the resources for the current calling thread group.
	 * @param ID the resource ID to store the given object at
	 * @param obj the object to store at the given ID
	 */
	public static final void submitResource(final String ID, final Object obj)
	{
		r()._submitResource(ID,obj);
	}
	
	/**
	 * Checks the resources for the current calling thread group for a file resource
	 * of the given name.
	 * @param filename the resource filename to check for (/resources/<filename>)
	 * @return true if the file exists as a current resource, and false otherwise
	 */
	public static final boolean isFileResource(final String filename)
	{
		return r()._isFileResource(filename);
	}

	/**
	 * Returns the stringbuffer content for the given resource filename, from
	 * the resources for the current calling thread group.
	 * @param ID the resource ID to return
	 * @param filename the resource filename to return (/resources/<filename>)
	 * @param reportErrors if true, file errors will be logged
	 * @return the StringBuffer of the file at that resource ID, or null of not found
	 */
	public static final StringBuffer getFileResource(final String filename, final boolean reportErrors)
	{
		return r()._getFileResource(filename,reportErrors);
	}
	
	/**
	 * Saves the given stringbuffer of data to the given resource filename, to 
	 * the filesystem on behalf of the given user/player, without touching the cache.
	 * Returns false if the user was not permitted to save files at that location.
	 * @param filename the resource filename to save to (/resources/<filename>)
	 * @param whom the mob whose permissions to check, or null to always save
	 * @param myRsc the string data to store in the file
	 * @return true if the file was saved, or false if there were permission or other problems
	 */
	public static final boolean saveFileResource(final String filename, final MOB whom, final StringBuffer myRsc)
	{
		return r()._saveFileResource(filename,whom,myRsc);
	}
	
	/**
	 * Saves the given stringbuffer of data to the given resource filename, to 
	 * the filesystem while also updating the internal cache for the resources 
	 * of the calling threads thread group.
	 * Returns false if there was a filesystem error.
	 * @param filename the resource filename to save to (/resources/<filename>)
	 * @param obj the string data to store in the file, stringbuffer, byte array, etc
	 * @return true if the file was saved, or false if there were problems
	 */
	public static final boolean updateFileResource(final String filename, final Object obj)
	{
		return r()._updateFileResource(filename,obj);
	}
	
	/**
	 * Opens the given CMFile as a properties type file, ignoring comment lines, and looking
	 * for a property entry that matches <match>=<whatever>, removing it if found, and if found,
	 * re-saving the file.
	 * @param F the properties file to potentially modify
	 * @param match the property file entry to remove
	 * @return true if the property was removed, and false if nothing was done
	 */
	public static final boolean findRemoveProperty(final CMFile F, final String match)
	{
		return r()._findRemoveProperty(F,match);
	}

	/**
	 * Scans the given stringbuffer for the first occurrence of an end-of-line and
	 * returns the end of line character(s) encountered.  This could be \n, \r, \n\r, 
	 * or \r\n
	 * @param buf the stringbuffer to scan
	 * @return the end of line market
	 */
	public static final String getEOLineMarker(final StringBuffer buf)
	{
		for(int i=0;i<buf.length()-1;i++)
		{
			switch(buf.charAt(i))
			{
			case '\n':
				if(buf.charAt(i+1)=='\r')
					return "\n\r";
				return "\n";
			case '\r':
				if(buf.charAt(i+1)=='\n')
					return "\r\n";
				return "\r";
			}
		}
		return "\n\r";
	}

	/**
	 * Scans the given stringbuffer for end of line markers, and adds each line
	 * encountered to a string list, returning that list object.
	 * @param buf the stringbuffer to scan for lines
	 * @return a list of all the lines in the buffer
	 */
	public static final List<String> getFileLineVector(final StringBuffer buf)
	{
		final Vector<String> V=new Vector<String>();
		if(buf==null)
			return V;
		final StringBuffer str=new StringBuffer("");
		for(int i=0;i<buf.length();i++)
		{
			if(((buf.charAt(i)=='\n')&&(i<buf.length()-1)&&(buf.charAt(i+1)=='\r'))
			||((buf.charAt(i)=='\r')&&(i<buf.length()-1)&&(buf.charAt(i+1)=='\n')))
			{
				i++;
				V.addElement(str.toString());
				str.setLength(0);
			}
			else
			if((buf.charAt(i)=='\r')||(buf.charAt(i)=='\n'))
			{
				V.addElement(str.toString());
				str.setLength(0);
			}
			else
				str.append(buf.charAt(i));
		}
		if(str.length()>0)
			V.addElement(str.toString());
		V.trimToSize();
		return V;
	}

	/**
	 * Adds resources/ before the given path, and always adds a / at the end.
	 * @param path a path string
	 * @return resources/ before the given path, and always adds a / at the end
	 */
	public static final String buildResourcePath(final String path)
	{
		if((path==null)||(path.length()==0))
			return "resources/";
		return "resources/"+path+"/";
	}

	public static final void updateMultiList(String filename, final Map<String, List<String>> lists)
	{
		final StringBuffer str=new StringBuffer("");
		for(final String ml : lists.keySet())
		{
			final List<String> V=lists.get(ml);
			str.append(ml+"\r\n");
			if(V!=null)
			{
				for(int v=0;v<V.size();v++)
					str.append((V.get(v))+"\r\n");
			}
			str.append("\r\n");
		}
		String prefix="";
		if(filename.startsWith("::")||filename.startsWith("//"))
		{
			prefix=filename.substring(0,2);
			filename=filename.substring(2);
		}
		new CMFile(prefix+buildResourcePath(filename),null).saveText(str);
	}

	public static final boolean removeMultiLists(final String filename)
	{
		final String key = "PARSED_MULTI: "+filename.toUpperCase();
		removeResource(key);
		return true;
	}

	@SuppressWarnings("unchecked")
	public static final Map<String, List<String>> getCachedMultiLists(final String filename, boolean createIfNot)
	{
		final String key = "PARSED_MULTI: "+filename.toUpperCase();
		Map<String,List<String>> H=(Map<String,List<String>>)getResource(key);
		if(H==null)
		{
			H=Resources.getMultiLists(filename);
			if((H==null) && (createIfNot))
				H=new Hashtable<String,List<String>>();
			if(H!=null)
				Resources.submitResource(key,H);
		}
		return H;
	}

	@SuppressWarnings("unchecked")
	public static final boolean updateCachedMultiLists(final String filename)
	{
		final String key;
		if(filename.startsWith("::")||filename.startsWith("//"))
			key = "PARSED_MULTI: "+filename.substring(2).toUpperCase();
		else
			key = "PARSED_MULTI: "+filename.toUpperCase();
		final Map<String,List<String>> H=(Map<String,List<String>>)getResource(key);
		if(H==null)
			return false;
		updateMultiList(filename, H);
		return true;
	}

	public static final Map<String, List<String>> getMultiLists(String filename)
	{
		final Hashtable<String,List<String>> oldH=new Hashtable<String,List<String>>();
		List<String> V=new Vector<String>();
		try
		{
			String prefix="";
			if(filename.startsWith("::")||filename.startsWith("//"))
			{
				prefix=filename.substring(0,2);
				filename=filename.substring(2);
			}
			V=getFileLineVector(new CMFile(prefix+"resources/"+filename,null).text());
		}
		catch(final Exception e){}
		
		if((V!=null)&&(V.size()>0))
		{
			String journal="";
			List<String> set=new Vector<String>();
			for(int v=0;v<V.size();v++)
			{
				final String s=V.get(v);
				if(s.trim().length()==0)
					journal="";
				else
				if(journal.length()==0)
				{
					journal=s;
					set=new Vector<String>();
					oldH.put(journal,set);
				}
				else
					set.add(s);
			}
		}
		return oldH;
	}

	public static final String makeFileResourceName(final String filename)
	{
		if(filename==null)
			return "resources/";
		if(filename.startsWith("resources/")||filename.startsWith("/resources/"))
			return filename;
		if(filename.startsWith("/"))
			return "resources"+filename;
		return "resources/"+filename;
	}

	public static final void setCompression(final boolean truefalse)
	{   
		compress=truefalse;
	}

	public static final boolean _compressed()
	{
		return compress;
	}

	public final void _clearResources()
	{
		resources.clear();
	}

	public final Iterator<String> _findResourceKeys(final String srch)
	{
		final String lowerSrch=srch.toLowerCase();
		final boolean allOfThem=(lowerSrch.length()==0);
		
		return new FilteredIterator<String>(resources.keySet().iterator(), new Filterer<String>()
		{
			@Override
			public boolean passesFilter(String obj)
			{
				return (allOfThem) || ((obj != null) && (obj.toLowerCase().indexOf(lowerSrch)>=0));
			}
		});
	}

	public final Object _getResource(final String ID)
	{
		final Object O = resources.get(ID);
		if(O!=null)
		{
			if(compress && (O instanceof CompressedResource))
				return new StringBuffer(CMLib.encoder().decompressString(((CompressedResource)O).data));
			return O;
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public static final Object prepareObject(final Object obj)
	{
		if(obj instanceof Vector)
			((Vector)obj).trimToSize();
		if(obj instanceof DVector)
			((DVector)obj).trimToSize();
		if(!compress)
			return obj;
		if(obj instanceof StringBuffer)
			return CMLib.encoder().compressString(((StringBuffer)obj).toString());
		return obj;
	}

	public final Object _submitResource(final String ID, final Object obj)
	{
		if(ID==null)
			Log.errOut("Resources",new Exception("Null ID"));
		final Object prepared=prepareObject(obj);
		if(prepared != obj)
			resources.put(ID,new CompressedResource((byte[])prepared));
		else
			resources.put(ID,prepared);
		return prepared;
	}

	private final Object _updateResource(final String ID, final Object obj)
	{
		return _submitResource(ID, obj);
	}

	public final void _removeResource(final String ID)
	{
		resources.remove(ID);
	}

	public final boolean _isFileResource(final String filename)
	{
		if(_getResource(filename)!=null)
			return true;
		if(new CMFile(makeFileResourceName(filename),null).exists())
			return true;
		return false;
	}

	public final StringBuffer _toStringBuffer(final Object o)
	{
		if(o!=null)
		{
			if(o instanceof StringBuffer)
				return (StringBuffer)o;
			else
			if(o instanceof String)
				return new StringBuffer((String)o);
			else
			if(o instanceof StringBuilder)
				return new StringBuffer((StringBuilder)o);
		}
		return null;
	}

	public final StringBuffer _getFileResource(final String filename, final boolean reportErrors)
	{
		final Object rsc=_getResource(filename);
		if(rsc != null)
			return _toStringBuffer(rsc);
		final StringBuffer buf=new CMFile(makeFileResourceName(filename),null,reportErrors?CMFile.FLAG_LOGERRORS:0).text();
		if(!CMProps.getBoolVar(CMProps.Bool.FILERESOURCENOCACHE))
			_submitResource(filename,buf);
		return buf;
	}

	public final boolean _updateFileResource(final String filename, final Object obj)
	{
		if(!CMProps.getBoolVar(CMProps.Bool.FILERESOURCENOCACHE))
			_updateResource(CMFile.vfsifyFilename(filename), obj);
		return _saveFileResource(filename,null,_toStringBuffer(obj));
	}

	public final boolean _saveFileResource(String filename, final MOB whoM, final StringBuffer myRsc)
	{
		final boolean vfsFile=filename.trim().startsWith("::");
		final boolean localFile=filename.trim().startsWith("//");
		filename=CMFile.vfsifyFilename(filename);
		if(!filename.startsWith("resources/"))
			filename="resources/"+filename;
		filename=(vfsFile?"::":localFile?"//":"")+filename;
		return new CMFile(filename,whoM).saveRaw(myRsc);
	}

	public final boolean _findRemoveProperty(final CMFile F, final String match)
	{
		boolean removed=false;
		final StringBuffer text=F.textUnformatted();
		int x=text.toString().toUpperCase().indexOf(match.toUpperCase());
		while(x>=0)
		{
			if(((x==0)||(!Character.isLetterOrDigit(text.charAt(x-1))))
			&&(text.substring(x+match.length()).trim().startsWith("=")))
			{
				final int zb1=text.lastIndexOf("\n",x);
				final int zb2=text.lastIndexOf("\r",x);
				int zb=(zb2>zb1)?zb2:zb1;
				if(zb<0)
					zb=0; else zb++;
				final int ze1=text.indexOf("\n",x);
				final int ze2=text.indexOf("\r",x);
				int ze=ze2+1;
				if((ze1>zb)&&(ze1==ze2+1))
					ze=ze1+1;
				else
				if((ze2<0)&&(ze1>0))
					ze=ze1+1;
				if(ze<=0)
					ze=text.length();
				if(!text.substring(zb).trim().startsWith("#"))
				{
					text.delete(zb,ze);
					x=-1;
					removed=true;
				}
			}
			x=text.toString().toUpperCase().indexOf(match.toUpperCase(),x+1);
		}
		if(removed)
			F.saveRaw(text);
		return removed;
	}

	public static final Map<String,String> getAllPropResources(String section)
	{
		if(propResources==null)
		{
			synchronized(propResourceSync)
			{
				if(propResources==null)
				{
					final CMFile file=new CMFile("::/coffeemud_properties.ini",null,CMFile.FLAG_FORCEALLOW);
					propResources=new TreeMap<String,Map<String,String>>();
					if(file.exists())
					{
						Map<String,String> currSecMap=new TreeMap<String,String>();
						propResources.put("", currSecMap);
						final List<String> lines=Resources.getFileLineVector(file.text());
						for(String line : lines)
						{
							line=line.trim();
							if(line.startsWith("[")&&(line.endsWith("]")))
							{
								final String currentSection=line.substring(1, line.length()-1).toUpperCase().trim();
								if(propResources.containsKey(currentSection))
									currSecMap=propResources.get(currentSection);
								else
									currSecMap=new TreeMap<String,String>();
								propResources.put(currentSection, currSecMap);
							}
							else
							if(line.startsWith("#"))
								continue;
							else
							{
								final int eqSepIndex=line.indexOf('=');
								if(eqSepIndex<0)
									continue;
								try
								{
									final String key=line.substring(0,eqSepIndex);
									final String value=URLDecoder.decode(line.substring(eqSepIndex+1),"UTF-8");
									currSecMap.put(key.toUpperCase().trim(), value);
								}
								catch(final UnsupportedEncodingException e) { }
							}
						}
					}
				}
			}
		}
		if(section.length()>0)
			section=Thread.currentThread().getThreadGroup().getName().charAt(0)+section.toUpperCase().trim();
		synchronized(propResources)
		{
			if(!propResources.containsKey(section))
			{
				propResources.put(section, new TreeMap<String,String>());
			}
			return propResources.get(section);
		}
	}

	public static final boolean isPropResource(String section, String key)
	{
		final Map<String,String> secMap = getAllPropResources(section);
		key=key.toUpperCase().trim();
		synchronized(secMap)
		{
			return secMap.containsKey(key);
		}
	}

	public static final String getPropResource(String section, String key)
	{
		final Map<String,String> secMap = getAllPropResources(section);
		key=key.toUpperCase().trim();
		synchronized(secMap)
		{
			if(!secMap.containsKey(key))
				return "";
			return secMap.get(key);
		}
	}

	public static final void setPropResource(String section, String key, String value)
	{
		final Map<String,String> secMap = getAllPropResources(section);
		key=key.toUpperCase().trim();
		synchronized(secMap)
		{
			if((value==null)||(value.length()==0))
				secMap.remove(key);
			else
				secMap.put(key, value);
		}
	}

	public static final void savePropResources()
	{
		if(propResources!=null)
		{
			synchronized(propResourceSync)
			{
				if(propResources!=null)
				{
					final StringBuilder str=new StringBuilder("");
					for(final String section : propResources.keySet())
					{
						final Map<String,String> secMap=propResources.get(section);
						if(secMap.size()==0)
							continue;
						if(str.length()>0)
							str.append("\n");
						str.append("["+section+"]\n");
						for(final String key : secMap.keySet())
						{
							try
							{
								final String value=URLEncoder.encode(secMap.get(key),"UTF-8");
								str.append(key).append("=").append(value).append("\n");
							}
							catch (final UnsupportedEncodingException e) { }
						}
					}
					final CMFile file=new CMFile("::/coffeemud_properties.ini",null,CMFile.FLAG_FORCEALLOW);
					file.saveText(str);
				}
			}
		}
	}
}
