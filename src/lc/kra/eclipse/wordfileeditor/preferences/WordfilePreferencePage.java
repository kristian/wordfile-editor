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
package lc.kra.eclipse.wordfileeditor.preferences;

import static lc.kra.eclipse.wordfileeditor.utilities.SettingsUtilities.AUTOMATIC_WORDFILE_PROPERTY;
import static lc.kra.eclipse.wordfileeditor.utilities.SettingsUtilities.CUSTOM_WORDFILES_PREFERENCE;
import static lc.kra.eclipse.wordfileeditor.utilities.SettingsUtilities.DEFAULT_WORDFILE_PREFERENCE;
import static lc.kra.eclipse.wordfileeditor.utilities.SettingsUtilities.UNDEFINED_WORDFILE;
import static lc.kra.eclipse.wordfileeditor.utilities.SettingsUtilities.getPreference;
import static lc.kra.eclipse.wordfileeditor.utilities.SettingsUtilities.setPreference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import lc.kra.eclipse.wordfileeditor.WordfileEditorActivator;
import lc.kra.eclipse.wordfileeditor.decorator.WordfileDecorator;
import lc.kra.eclipse.wordfileeditor.utilities.Guitilities;
import lc.kra.eclipse.wordfileeditor.wordfile.CustomWordfile;
import lc.kra.eclipse.wordfileeditor.wordfile.Wordfile;

/**
 * this class creates the preference page for the WordfileEditor
 * new Wordfiles can be added here. Also the slot tags can be customized.
 * @author Kristian Kraljic
 */
public class WordfilePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
    /**
     * the images for locked and custom wordfiles
     */
    private static Image IMAGE_LIST,IMAGE_LOCK;
    static {
    	WordfileEditorActivator activator = WordfileEditorActivator.getDefault();
    	if(activator!=null) {
    		ImageRegistry registry = activator.getImageRegistry();
    		IMAGE_LIST = registry.get("/icons/list-small.png");
    		IMAGE_LOCK = registry.get("/icons/lock-small.png");
    	}
    }
    
    /**
     * all buttons in the proeprty page
     */
    private Button addButton,removeButton,defaultButton,downloadButton;
    
    /**
     * all build in wordfiles
     */
    private final Wordfile[] wordfiles = Wordfile.getNativeWordfiles().toArray(new Wordfile[0]);
    /**
     * all custom added wordfiles
     */
    private List<CustomWordfile> customWordfiles = new ArrayList<CustomWordfile>();
    /**
     * the table in which the wordfiles are displayed
     */
    private Table wordfileTable;
    
    /**
     * checked if the wordfile should be determined automatically
     */
    private Button automaticCheck;
    
    /**
     * Constructor for WordfilePreferencePage.
     */
    public WordfilePreferencePage() {
        super();
    }

    /**
     * initialized the page
     */
	@Override public void init(IWorkbench workbench) {}
	
    /**
     * the third section contains the wordfile configuration adn controlls to add and remove wordfiles
     * @param parent
     */
    private Composite addFirstSection(Composite parent) { //wordfiles
    	Composite topComposite = Guitilities.createComposite(parent,2,1,GridData.FILL_BOTH);
		
    	Guitilities.createWrapLabel(topComposite,"These perferences are used in the WordfileEditor. If the automatic determination is disabled or no wordfile can be determined, the default wordfile selected below is used.",2,300);
    	Guitilities.createVerticalSpacer(topComposite,2);
    	Guitilities.createWrapLabel(topComposite,"Available 'wordfiles' in the WordfileEditor:",2,300);
		
    	createTable(topComposite);

    	Composite buttonComposite = Guitilities.createComposite(topComposite,1,1,GridData.FILL_VERTICAL);
		GridLayout layout = (GridLayout)buttonComposite.getLayout();
		layout.marginHeight = 0;
		
		
		defaultButton = Guitilities.createPushButton(buttonComposite,"Default");
		defaultButton.setEnabled(false);
		defaultButton.addListener(SWT.Selection,new Listener() {
			@Override public void handleEvent(Event event) {
				for(TableItem item:wordfileTable.getItems())
					item.setText(1,new String());
				wordfileTable.getSelection()[0].setText(1,"Yes");
				defaultButton.setEnabled(false);
			}
		});
		Guitilities.createLabel(buttonComposite,"Wordfile:",1);
		
		addButton = Guitilities.createPushButton(buttonComposite,"Add...");
		addButton.addListener(SWT.Selection,new Listener() {
			@Override public void handleEvent(Event event) {
				FileDialog dialog = new FileDialog(getShell(),SWT.NULL);
				String path = dialog.open();
				if(path!=null) {
					MessageDialog message = null;
					try {
						File file = new File(path);
						CustomWordfile wordfile = new CustomWordfile(file);
						for(Wordfile existing:wordfiles)
							if(existing.getNumber()==wordfile.getNumber())
								throw new Exception("There is already a default wordfile with an equivalent wordfile number. Please add another wordlist or change the number of this wordfile ("+wordfile.getNumber()+").");
						for(Wordfile existing:customWordfiles)
							if(existing.getNumber()==wordfile.getNumber())
								throw new Exception("There is already a custom wordfile with an equivalent wordfile number. Please add another wordlist or change the number of this wordfile ("+wordfile.getNumber()+").");
						addCustomWordfile(wordfile);
					} catch(IOException e) { message = new MessageDialog(getShell(),"Can't Read Wordfile",null,"An error occoured while reading the wordfile:\n"+e.getMessage(),MessageDialog.ERROR,new String[]{"OK"},0); }
                   catch(ParseException e) { message = new MessageDialog(getShell(),"Can't Parse Wordfile",null,"An error occoured while parsing the wordfile:\n"+e.getMessage(),MessageDialog.ERROR,new String[]{"OK"},0); }
                   		catch(Exception e) { message = new MessageDialog(getShell(),"Can't Use Wordfile",null,"An error occoured with the wordfile:\n"+e.getMessage(),MessageDialog.ERROR,new String[]{"OK"},0); }
                   if(message!=null) {
                	 message.setBlockOnOpen(true);
                	 message.open();
                   }
				}
			}
		});
		
		removeButton = Guitilities.createPushButton(buttonComposite,"Remove");
		removeButton.setEnabled(false);
		removeButton.addListener(SWT.Selection,new Listener() {
			@Override public void handleEvent(Event event) {
				int index = wordfileTable.getSelectionIndex(),listIndex = index-(wordfileTable.getItemCount()-customWordfiles.size());
				customWordfiles.remove(listIndex);
				if(!wordfileTable.getItem(index).getText(1).isEmpty())
					wordfileTable.getItem(0).setText(1,"Yes");
				wordfileTable.remove(index);
			}
		});
		
		Guitilities.createVerticalSpacer(buttonComposite,5);
		
        downloadButton = Guitilities.createFlatButton(buttonComposite,"Download More...");
        downloadButton.addListener(SWT.Selection,new Listener() {
			@Override public void handleEvent(Event event) {
				Program.launch("http://www.ultraedit.com/downloads/extras.html#wordfiles");
			}
		});
        
		automaticCheck = Guitilities.createCheckButton(topComposite,"Determine wordfile association automatically if possible",Boolean.TRUE.toString().equals(getPreference(AUTOMATIC_WORDFILE_PROPERTY)),2);
		((GridData)automaticCheck.getLayoutData()).horizontalIndent = 10;
		
		applyDialogFont(topComposite);
		return topComposite;
    }
    
    private void createTable(Composite topComposite) {
       	wordfileTable = new Table(topComposite,SWT.SINGLE|SWT.VIRTUAL|SWT.BORDER|SWT.FULL_SELECTION);
        wordfileTable.setLinesVisible(true);
        wordfileTable.setHeaderVisible(true);
        wordfileTable.setEnabled(true);

        TableColumn column;
        column = new TableColumn(wordfileTable,SWT.LEFT);
        column.setWidth(140);
        column.setResizable(true);
        column.setText("Wordfile");
        column = new TableColumn(wordfileTable,SWT.LEFT);
        column.setWidth(60);
        column.setResizable(false);
        column.setText("Default");
        
        TableItem generalItem = new TableItem(wordfileTable,SWT.NONE);
        generalItem.setImage(new Image[]{IMAGE_LOCK,null});
        generalItem.setText(new String[]{"General",getDefaultWordfile()<0?"Yes":null});
        generalItem.setGrayed(true);
        
        for(Wordfile wordfile:wordfiles) {
            TableItem wordfileItem = new TableItem(wordfileTable,SWT.NONE);
            wordfileItem.setImage(new Image[]{IMAGE_LOCK,null});
            wordfileItem.setText(new String[]{wordfile.getName(),wordfile.getNumber()==getDefaultWordfile()?"Yes":null});
        }
        String customLanguages = getPreference(CUSTOM_WORDFILES_PREFERENCE);
        if(customLanguages!=null&&!customLanguages.isEmpty())
	        for(String filename:customLanguages.split("\\|"))
	        	try { addCustomWordfile(filename); }
	        	catch(Exception e) { e.printStackTrace(); };
        
	    boolean hasDefault = false;
	    for(TableItem item:wordfileTable.getItems())
	    	if(!item.getText(1).isEmpty()) {
	    		hasDefault = true;
	    		break;
	    	}
	    if(!hasDefault)
	    	generalItem.setText(1,"Yes");
	    	
		wordfileTable.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				TableItem item = (TableItem)event.item;
				removeButton.setEnabled(item.getImage(0)!=IMAGE_LOCK);
				defaultButton.setEnabled(item.getText(1).isEmpty());
			}
		});
		
		GridData gd = new GridData();
		gd.horizontalAlignment = gd.verticalAlignment = GridData.FILL;
		wordfileTable.setLayoutData(gd);
	}

	/**
     * adds a custom wordfile
     * @param filename the filename of the custom wordfile
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ParseException
     */
    private void addCustomWordfile(String filename) throws FileNotFoundException, IOException, ParseException {
		CustomWordfile wordfile = new CustomWordfile(new File(filename));
		addCustomWordfile(wordfile);
	}
    /**
     * adds a custom wordfile 
     * @param wordfile the wordfile which should be added
     */
    private void addCustomWordfile(CustomWordfile wordfile) {
    	TableItem item = new TableItem(wordfileTable,SWT.NONE);
        item.setImage(new Image[]{IMAGE_LIST,null});
        item.setText(new String[]{wordfile.getName(),wordfile.getNumber()==getDefaultWordfile()?"Yes":null});
        customWordfiles.add(wordfile);
    }
    
    /**
     * returns the currently selected default wordfile
     * @param resource the resource to read the property from
     * @return the number of the default wordfile or -1
     */
    private int getDefaultWordfile() {
        try {
        	return Integer.parseInt(getPreference(DEFAULT_WORDFILE_PREFERENCE));
        } catch(NumberFormatException e) { return -1; }
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
        while(wordfileTable.getItemCount()-1>wordfiles.length)
        	wordfileTable.remove(wordfileTable.getItemCount()-1);
        for(TableItem item:wordfileTable.getItems())
        	item.setText(1,new String());
        wordfileTable.getItem(0).setText(1,"Yes");
        customWordfiles.clear();
        automaticCheck.setSelection(true);
    }

    /**
     * set the persistant properties
     */
    public boolean performOk() {
        Wordfile defaultWordfile = null;
        for(int index=0;index<wordfiles.length;index++)
        	if(!wordfileTable.getItem(index+1).getText(1).isEmpty())
        		defaultWordfile = wordfiles[index];
        for(int index=0;index<customWordfiles.size();index++)
        	if(!wordfileTable.getItem(wordfiles.length+index+1).getText(1).isEmpty())
        		defaultWordfile = customWordfiles.get(index);
        if(defaultWordfile!=null)
        	 setPreference(DEFAULT_WORDFILE_PREFERENCE,Integer.toString(defaultWordfile.getNumber()));
        else setPreference(DEFAULT_WORDFILE_PREFERENCE,Integer.toString(UNDEFINED_WORDFILE)); //general
        
        StringBuilder customLanguages = new StringBuilder();
        for(CustomWordfile wordfile:customWordfiles) {
        	if(customLanguages.length()!=0)
        		customLanguages.append('|');
        	customLanguages.append(wordfile.file.toString());
        }
        setPreference(CUSTOM_WORDFILES_PREFERENCE,customLanguages.toString());
        setPreference(AUTOMATIC_WORDFILE_PROPERTY,Boolean.toString(automaticCheck.getSelection()));
        WordfileDecorator.doRefresh();
        
        return true;
    }
}
