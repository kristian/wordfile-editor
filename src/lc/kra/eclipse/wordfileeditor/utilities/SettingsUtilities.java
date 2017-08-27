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
package lc.kra.eclipse.wordfileeditor.utilities;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.dialogs.PreferencesUtil;

import lc.kra.eclipse.wordfileeditor.WordfileEditorActivator;

/**
 * the SettingsUtilities provide simplified access to the PersistentProperty featrues of eclipse 
 * @author Kristian Kraljic
 */
public class SettingsUtilities {
	/**
	 * the WORDFILE_PROPERTY sets, which wordfile is currently active in one file (wordfile number)
	 *   may contain any positive number or UNDEFINED_WORDFILE, GENERAL_WORDFILE
	 */
	public static final String WORDFILE_PROPERTY = "WORDFILE";
	
	/**
	 * the CUSTOM_WORDFILES_PROPERTY property contains pathts to wordfiles separated with |
	 */
	public static final String CUSTOM_WORDFILES_PREFERENCE = "CUSTOM_WORDFILES";
	
	/**
	 * the DEFAULT_WORDFILE_PROPERTY property contains the wordfile (number) which is selected when opening a new file with the wordfile editor
	 *   may contain any positive number or UNDEFINED_WORDFILE
	 */
	public static final String DEFAULT_WORDFILE_PREFERENCE = "DEFAULT_WORDFILE";
	
	/**
	 * the AUTOMATIC_WORDFILE_PROPERTY property contains Boolean.TRUE/FALSE.toString() if the file extention should be determined automatically
	 */
	public static final String AUTOMATIC_WORDFILE_PROPERTY = "AUTOMATIC_WORDFILE";
	
	/**
	 * these constants are used to define the default wordfiles
	 */
	public static final int UNDEFINED_WORDFILE = -1,GENERAL_WORDFILE = -2;
	
    public static String getPreference(String preference) {
    	WordfileEditorActivator activator = WordfileEditorActivator.getDefault();
    	if(activator==null)
    		return null;
    	IPreferenceStore store = activator.getPreferenceStore();
    	String value = null;
    	if(store.contains(preference))
    		value = store.getString(preference);
    	if(value==null)
    		value = store.getDefaultString(preference);
    	if(IPreferenceStore.STRING_DEFAULT_DEFAULT.equals(value))
    		return null;
    	return value;
    }
    
    public static void setPreference(String preference,Object value) {
    	WordfileEditorActivator activator = WordfileEditorActivator.getDefault();
    	if(activator==null)
    		return;
    	IPreferenceStore store = activator.getPreferenceStore();
    	if(value!=null) {
    		   store.setValue(preference,value.toString());
    	} else store.setToDefault(preference);
    }
    
    /**
     * Copies a persisent property from one resource to another resource
     * @param source the source resource
     * @param target the target resource
     * @param property the property name
     * @throws CoreException
     */
    public static void copyPersistentProperty(IResource source,IResource target,String property) throws CoreException {
    	//set the property, get it from another resource (recuruse into)
    	setPersistentProperty(target,property,getPersistentProperty(source,property,null,true),false);
    }
    
    /**
     * @see getPersistentProperty(editor,property,default_value,true)
     */
    public static String getPersistentProperty(IEditorPart editor,String property,Object default_value) { return getPersistentProperty(editor,property,default_value,true); }
    /**
     * returns the persistent property of a resource which is held by an editor, if the file itself hasnt got the property also all parent files are checked
     * @param editor the editor to read the property from
     * @param property the property to read
     * @param default_value the value of the property if nothing was defined so far
     * @param recurse set recurse to read also the parent files
     * @return the property value or default_value
     */
    public static String getPersistentProperty(IEditorPart editor,String property,Object default_value,boolean recurse) { return getPersistentProperty(Utilities.getEditorFile(editor),property,default_value,recurse); }
    /**
     * @see getPersistentProperty(resource,property,default_value,true)
     */
    public static String getPersistentProperty(IResource resource,String property,Object default_value) { return getPersistentProperty(resource,property,default_value,true); }
    /**
     * returns the persistent property of a resource, if the file itself hasnt got the property also all parent files are checked
     * @param resource the resource to read the property from
     * @param property the property to read
     * @param default_value the value of the property if nothing was defined so far
     * @param recurse set recurse to read also the parent files
     * @return the property value or default_value
     */
    public static String getPersistentProperty(IResource resource,String property,Object default_value,boolean recurse) {
        IResource parent = resource;
        do {
            try {
                String value = parent.getPersistentProperty(new QualifiedName(new String(),property));
                if(value!=null)
                    return value;
            } catch(CoreException e) { } //try the next ressource
        } while(recurse&&(parent=parent.getParent())!=null);
        return default_value!=null?default_value.toString():null;
    }
    
    /**
     * @see setPersistentProperty(editor,property,value,true)
     */
    public static void setPersistentProperty(IEditorPart editor,String property,Object value) throws CoreException { setPersistentProperty(editor,property,value,true); }
    /**
     * sets the persistent property of a resource which is held by an editor
     * @param editor the editor to set the property to
     * @param property the property to read
     * @param recurse set recurse to navigate into subfolders and set the property to all files in that folder
     * @param value the value to be set
     * @throws CoreException
     */
    public static void setPersistentProperty(IEditorPart editor,String property,Object value,boolean recurse) throws CoreException { setPersistentProperty(Utilities.getEditorFile(editor),property,value,recurse); }
    /**
     * @see setPersistentProperty(resouce,property,value,true)
     */
    public static void setPersistentProperty(IResource resource,String property,Object value) throws CoreException { setPersistentProperty(resource,property,value,true); }
    /**
     * sets the persistent property of a resource
     * @param resource the resource to set the property to
     * @param property the property to read
     * @param recurse set recurse to navigate into subfolders and set the property to all files in that folder
     * @param value the value to be set
     * @throws CoreException
     */
    public static void setPersistentProperty(IResource resource,String property,Object value,boolean recurse) throws CoreException {
    	if(recurse) //if recruses set, go to each 
	    	if(resource instanceof IContainer)
	    		for(IResource child:((IContainer)resource).members()) {
	    			setPersistentProperty(child,property,value,true);
	    			return;
	    		}
    	resource.setPersistentProperty(new QualifiedName("",property),value!=null?value.toString():null);
    }
    
    /**
     * opens a preference page with a given id
     * @param preferencePageId the preference page id
     * @param block if the call should block on open
     */
	public static void showPreferencePage(String preferencePageId,boolean block) {
		PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(Guitilities.getActiveWorkbenchWindow(),preferencePageId,new String[]{preferencePageId},null);
		dialog.setBlockOnOpen(true);
		dialog.open();
	}	
}
