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
package lc.kra.eclipse.wordfileeditor.properties;

import static lc.kra.eclipse.wordfileeditor.utilities.SettingsUtilities.CUSTOM_WORDFILES_PREFERENCE;
import static lc.kra.eclipse.wordfileeditor.utilities.SettingsUtilities.GENERAL_WORDFILE;
import static lc.kra.eclipse.wordfileeditor.utilities.SettingsUtilities.UNDEFINED_WORDFILE;
import static lc.kra.eclipse.wordfileeditor.utilities.SettingsUtilities.WORDFILE_PROPERTY;
import static lc.kra.eclipse.wordfileeditor.utilities.SettingsUtilities.getPersistentProperty;
import static lc.kra.eclipse.wordfileeditor.utilities.SettingsUtilities.getPreference;
import static lc.kra.eclipse.wordfileeditor.utilities.SettingsUtilities.setPersistentProperty;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.dialogs.PropertyPage;

import lc.kra.eclipse.wordfileeditor.decorator.WordfileDecorator;
import lc.kra.eclipse.wordfileeditor.editor.WordfileEditor;
import lc.kra.eclipse.wordfileeditor.utilities.Guitilities;
import lc.kra.eclipse.wordfileeditor.utilities.SettingsUtilities;
import lc.kra.eclipse.wordfileeditor.wordfile.CustomWordfile;
import lc.kra.eclipse.wordfileeditor.wordfile.Wordfile;

/**
 * this class creates the property page for the WordfileEditor
 * new Wordfiles can be added here. Also the slot tags can be customized.
 * @author Kristian Kraljic
 */
public class WordfilePropertyPage extends PropertyPage {    
    /**
     * all build in wordfiles
     */
    private final Wordfile[] wordfiles = Wordfile.getNativeWordfiles().toArray(new Wordfile[0]);
    /**
     * all custom added wordfiles
     */
    private List<CustomWordfile> customWordfiles = new ArrayList<CustomWordfile>();
    /**
     * the combo in which the wordfiles are displayed
     */
    private Combo wordfileCombo;
    /**
     * the button refering to the preference age
     */
    private Button preferenceButton;
    
    /**
     * Constructor for WordfilePropertyPage.
     */
    public WordfilePropertyPage() {
        super();
    }
    
    /**
     * the third section contains the wordfile configuration adn controlls to add and remove wordfiles
     * @param parent
     */
    private Composite addFirstSection(Composite parent) { //wordfiles
    	Composite topComposite = Guitilities.createComposite(parent,2,1,GridData.FILL_BOTH);
		
    	Guitilities.createWrapLabel(topComposite,"This page allows you to manage which wordfile is associated with the currently selected resource.",2,300);
    	Guitilities.createVerticalSpacer(topComposite,2);
    	Guitilities.createWrapLabel(topComposite,MessageFormat.format("Wordfile configuration for ''{0}'':",getResource().getName()),2,300);
  
    	wordfileCombo = Guitilities.createCombo(topComposite,SWT.DROP_DOWN|SWT.READ_ONLY|SWT.BORDER,2,new String[]{"Automatically select","General"});
    	addWordfiles();
        
        preferenceButton = Guitilities.createFlatButton(topComposite,"Configure available 'wordfiles'");
        ((GridData)preferenceButton.getLayoutData()).horizontalIndent = 10;
        preferenceButton.addListener(SWT.Selection,new Listener() {
			@Override public void handleEvent(Event event) {
				SettingsUtilities.showPreferencePage(WordfileEditor.class.getName()+".preferencePage",true);
		        while(wordfileCombo.getItemCount()>2)
		        	wordfileCombo.remove(2);
		        addWordfiles();
			}
		});
        
		applyDialogFont(topComposite);
		return topComposite;
    }

    /**
     * adds the wordfiles to the wordfile combo and selects the currently chosen one
     */
	private void addWordfiles() {
    	int wordfileNumber = 0;
    	try { wordfileNumber = Integer.parseInt(getPersistentProperty(getResource(),WORDFILE_PROPERTY,Integer.toString(UNDEFINED_WORDFILE))); }
    	catch(NumberFormatException e) {
    		wordfileNumber = UNDEFINED_WORDFILE;
    	}
	    
    	wordfileCombo.select(0);
		if(wordfileNumber==GENERAL_WORDFILE)
    		wordfileCombo.select(1);
        for(Wordfile wordfile:wordfiles) {
        	wordfileCombo.add(wordfile.getName());
        	if(wordfile.getNumber()==wordfileNumber)
        		wordfileCombo.select(wordfileCombo.getItemCount()-1);
        }
        String customLanguages = getPreference(CUSTOM_WORDFILES_PREFERENCE);
        if(customLanguages!=null&&!customLanguages.isEmpty())
	        for(String filename:customLanguages.split("\\|"))
	        	try {
	        		if(addCustomWordfile(filename).getNumber()==wordfileNumber)
	            		wordfileCombo.select(wordfileCombo.getItemCount()-1);
	        	} catch(Exception e) { e.printStackTrace(); }
	}

    /**
	 * returns the resource this property page.
	 * @return resource
	 */
	protected IResource getResource() {
		Object element = getElement();
		if(element instanceof IResource)
			return (IResource)element;
		if(element instanceof IAdaptable)
			return (IResource)((IAdaptable)element).getAdapter(IResource.class);
		return null;
	}
	
    /**
     * adds a custom wordfile
     * @param filename the filename of the custom wordfile
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ParseException
     */
    private CustomWordfile addCustomWordfile(String filename) throws FileNotFoundException, IOException, ParseException {
		CustomWordfile wordfile = new CustomWordfile(new File(filename));
		addCustomWordfile(wordfile);
		return wordfile;
	}
    /**
     * adds a custom wordfile 
     * @param wordfile the wordfile which should be added
     */
    private void addCustomWordfile(CustomWordfile wordfile) {
    	wordfileCombo.add(wordfile.getName());
        customWordfiles.add(wordfile);
    }

	/**
     * @see PreferencePage#createContents(Composite)
     */
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent,SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        composite.setLayout(gridLayout);
        GridData gridData = new GridData(GridData.FILL);
        gridData.grabExcessHorizontalSpace = true;
        composite.setLayoutData(gridData);
        addFirstSection(composite);
        return composite;
    }

    /**
     * set the default tags, remove all custom wordfiles
     */
    protected void performDefaults() {
        super.performDefaults();
        while(wordfileCombo.getItemCount()-2>wordfiles.length)
        	wordfileCombo.remove(wordfileCombo.getItemCount()-1);
        wordfileCombo.select(0);
        customWordfiles.clear();
    }

    /**
     * set the persistant properties
     */
    public boolean performOk() {
    	IResource resource = getResource();
    	try {
    		int index = wordfileCombo.getSelectionIndex();
	    	switch(index) {
	    	case 0: setPersistentProperty(resource,WORDFILE_PROPERTY,UNDEFINED_WORDFILE); break;
	    	case 1: setPersistentProperty(resource,WORDFILE_PROPERTY,GENERAL_WORDFILE); break;
	    	default:
	    		index -= 2;
	    		if(index<0)
	    			return false;
	    		if(wordfiles.length>index)
	    			 setPersistentProperty(resource,WORDFILE_PROPERTY,wordfiles[index].getNumber());
	    		else setPersistentProperty(resource,WORDFILE_PROPERTY,customWordfiles.get(index-wordfiles.length).getNumber());
	    		break;
	    	}
    	} catch(CoreException e) {
    		e.printStackTrace();
    		return false;
    	}
    	WordfileEditor.doRefresh();
        WordfileDecorator.doRefresh();
        return true;
    }
}
