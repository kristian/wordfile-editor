/**
 * Copyright (c) 2017 Kristian Kraljic
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package lc.kra.eclipse.wordfileeditor;

import static lc.kra.eclipse.wordfileeditor.utilities.SettingsUtilities.AUTOMATIC_WORDFILE_PROPERTY;
import static lc.kra.eclipse.wordfileeditor.utilities.SettingsUtilities.CUSTOM_WORDFILES_PREFERENCE;
import static lc.kra.eclipse.wordfileeditor.utilities.SettingsUtilities.DEFAULT_WORDFILE_PREFERENCE;
import static lc.kra.eclipse.wordfileeditor.utilities.SettingsUtilities.UNDEFINED_WORDFILE;
import static lc.kra.eclipse.wordfileeditor.utilities.SettingsUtilities.getPreference;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import lc.kra.eclipse.wordfileeditor.wordfile.CustomWordfile;

/**
 * The activator class controls the plug-in life cycle
 */
public class WordfileEditorActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "WordfileEditor"; //$NON-NLS-1$

	// The shared instance
	private static WordfileEditorActivator plugin;
	
	/**
	 * The constructor
	 */
	public WordfileEditorActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		ImageRegistry registry = getImageRegistry();		
		Enumeration<?> enumerator = WordfileEditorActivator.getDefault().getBundle().findEntries("/icons","*.png",true);
		Object resource = null;
		while(enumerator!=null&&enumerator.hasMoreElements()&&(resource=enumerator.nextElement())!=null)
			try {
				if(resource instanceof URL)
					 registry.put(((URL)resource).getFile(), ImageDescriptor.createFromURL((URL)resource));
				else registry.put(((File)resource).getPath(), ImageDescriptor.createFromFile(WordfileEditorActivator.class, ((File)resource).getPath()));
			} catch(Exception e) { System.err.println("error while loading image '"+resource.toString()+"' ("+e.getClass().getSimpleName()+"): "+e.getMessage()); }
		
        String customLanguages = getPreference(CUSTOM_WORDFILES_PREFERENCE);
        if(customLanguages!=null&&!customLanguages.isEmpty())
	        for(String filename:customLanguages.split("\\|"))
	        	try {
	        		 new CustomWordfile(new File(filename));
	        	} catch(Exception e) { e.printStackTrace(); };
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static WordfileEditorActivator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	/** 
	 * Initializes a preference store with default preference values 
	 * for this plug-in.
	 */
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		store.setDefault(AUTOMATIC_WORDFILE_PROPERTY,Boolean.TRUE.toString());
		store.setDefault(DEFAULT_WORDFILE_PREFERENCE,UNDEFINED_WORDFILE);
		store.setDefault(CUSTOM_WORDFILES_PREFERENCE,new String());
	}
}
