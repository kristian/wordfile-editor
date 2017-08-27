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
package lc.kra.eclipse.wordfileeditor.editor;

import static lc.kra.eclipse.wordfileeditor.utilities.SettingsUtilities.AUTOMATIC_WORDFILE_PROPERTY;
import static lc.kra.eclipse.wordfileeditor.utilities.SettingsUtilities.DEFAULT_WORDFILE_PREFERENCE;
import static lc.kra.eclipse.wordfileeditor.utilities.SettingsUtilities.GENERAL_WORDFILE;
import static lc.kra.eclipse.wordfileeditor.utilities.SettingsUtilities.UNDEFINED_WORDFILE;
import static lc.kra.eclipse.wordfileeditor.utilities.SettingsUtilities.WORDFILE_PROPERTY;
import static lc.kra.eclipse.wordfileeditor.utilities.SettingsUtilities.getPersistentProperty;
import static lc.kra.eclipse.wordfileeditor.utilities.SettingsUtilities.getPreference;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;

import lc.kra.eclipse.wordfileeditor.annotation.WordfileAnnotationDocumentListener;
import lc.kra.eclipse.wordfileeditor.annotation.WordfileFoldAnnotationProvider;
import lc.kra.eclipse.wordfileeditor.utilities.Utilities;
import lc.kra.eclipse.wordfileeditor.wordfile.Wordfile;

/**
 * The WordfileEditor highlights text using a custom wordfile
 * @author Kristian Kraljic
 */
public class WordfileEditor extends TextEditor {
	/**
	 * the wordfile this editor is highligthed with
	 */
    protected Wordfile wordfile;
    /**
     * the wordfile configuration
     */
    protected WordfileConfiguration configuration;
    /**
     * the document provider for this editor
     */
    protected WordfileDocumentProvider provider;
    
    /**
     * the ProjectionSupport is used if the wordfile supports code folding 
     */
    protected ProjectionSupport projection;
    /**
     * the PorjectionViewer of this TextEditor
     */
    protected ProjectionViewer projectionViewer;

    /**
     * creates a new wordfile editor without using a specific wordfile
     * following rules apply to select a wordfile:
     *  1. file properties are checked
     *      - if any (existing) wordfile is chosen, this wordfile is used
     *  2. preferences are checked
     *      - if any default (exsting) wordfile is chosen -> the wordfile is used
     *      - if a general highlighting is selected -> general is used
     *      - if automatic mode or no mode was selected -> automatic determination is tiggered
     *  3. it is checked if any existing wordfile matches the file extension
     *  4. general highlighting is used
     */
    public WordfileEditor() { this(null); }

    /**
     * creates a new wordfile editor, using the wordfile for highlighting
     * @param wordfile the wordfile to used
     */
    public WordfileEditor(Wordfile wordfile) {
    	this.wordfile = wordfile;
    	setKeyBindingScopes(new String[]{WordfileEditor.class.getName()+".editorScope"}); //set the key binding scope
    	setDocumentProvider(new WordfileDocumentProvider(wordfile));
    }
    
    @Override public void init(IEditorSite site,IEditorInput input) throws PartInitException {
		if (!(input instanceof IFileEditorInput))
			throw new PartInitException("Invalid Input: Must be IFileEditorInput");
		
		if(!hasWordfile())
			wordfile = determineWordfile(((IFileEditorInput)input).getFile());
		configuration = new WordfileConfiguration(wordfile,getPreferenceStore());
		
    	setDocumentProvider(new WordfileDocumentProvider(wordfile));
    	setSourceViewerConfiguration(configuration);
        
		super.init(site,input);
		updatePartName(input);
    }
    
    /**
     * TODO To be implemented in future to refresh (re-init) this control
     */
    public void refresh() {}
    
    /**
     * Returns true if the file is assigned to a WordfileEditor by default
     * @param file The file to be checked
     * @return true if it is assigned by default, false if not or it can not be determined
     */
    public static boolean isWordfileEditorAssigned(IFile file) throws CoreException {
		IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
		IContentDescription description = file.getContentDescription();
		IEditorDescriptor descriptor = description==null?registry.getDefaultEditor(file.getName()):registry.getDefaultEditor(file.getName(),description.getContentType());
		if(descriptor!=null)
			 return (descriptor.getId().equals(WordfileEditor.class.getName()));
		else return false;
	}
    
    /**
     * Returns the current configured wordfile
     * @param editor the editor to read the properties from or null (only preferences are checked)
     * @return a wordfile if any was determined, or null if general highlighting is to be used
     */
    public static Wordfile determineWordfile(IFile file) {
    	if(file!=null) {
	    	String property = getPersistentProperty(file,WORDFILE_PROPERTY,Integer.toString(UNDEFINED_WORDFILE));
	    	if(Utilities.isNumeric(property)) {
	    		int wordfileNumber = Integer.parseInt(property);
	    		if(wordfileNumber>=0) {
	    			Wordfile wordfile = Wordfile.getWordfile(wordfileNumber);
	    			if(wordfile!=null)
	    				return wordfile;
	    		} else if(wordfileNumber==GENERAL_WORDFILE)
	    			return null; //general
	    	}
	    	if(Boolean.TRUE.toString().equals(getPreference(AUTOMATIC_WORDFILE_PROPERTY))) {
	    		String name = file.getName();
	    		int index = name.lastIndexOf('.');
	    		if(index!=-1) { //the file has an extension
					Wordfile wordfile = Wordfile.getWordfile(name.substring(index+1));
					if(wordfile!=null)
						return wordfile;
	    		}
	    	}
    	}
    	
    	String preference = getPreference(DEFAULT_WORDFILE_PREFERENCE);
    	if(Utilities.isNumeric(preference)) {
    		int wordfileNumber = Integer.parseInt(preference);
    		if(wordfileNumber>=0) {
				Wordfile wordfile = Wordfile.getWordfile(wordfileNumber);
				if(wordfile!=null)
					return wordfile;
    		} else return null; //general
    	}
    	
    	return null;
    }
    
    /**
     * @return returns if the editor uses a wordfile (or if it is generally highighted)
     */
    public boolean hasWordfile() { return wordfile!=null; }
    /**
     * @return returns the wordfile of this editor or null
     */
    public Wordfile getWordfile() { return wordfile; }
    /**
     * @return the used WordfileConfiguration
     */
    public WordfileConfiguration getWordfileConfiguration() { return configuration; }
    /**
     * returns the recognition factor of the used wordfile
     * @return the recognition factor for the language of this wordfile (if more words have been recognized the factor is near to 1)
     */
    public double getRecognitionFactor() { return getWordfileConfiguration().getWordfileScanner().getWordfileRule().getRecognitionFactor(); }

    /**
     * if the wordfile is using code folding a projection annotatin model is returned
     * @return an ProjectionAnnotationModel or null
     */
    public ProjectionAnnotationModel getProjectionAnnotationModel() {
        return projectionViewer!=null&&projectionViewer.isProjectionMode()?projectionViewer.getProjectionAnnotationModel():null;
    }
    
    /**
     * updates the part name after initialization
     * @param input the editor input to read the filename from
     */
    private void updatePartName(IEditorInput input) {
        StringBuilder title = new StringBuilder();
        if(input!=null)
        	 title.append(input.getName());
        else title.append(getPartName());
		if(hasWordfile())
			title.append(" (").append(getWordfile().getName()).append(')');
		setPartName(title.toString());
	}
    
    /**
     * creates the part control for this editor, installs an ProjectionAnnotationModel if the wordfile uses code folding
     */
    public void createPartControl(Composite parent) {
    	WordfileAnnotationDocumentListener annotationListener = new WordfileAnnotationDocumentListener(this);
    	
        super.createPartControl(parent);
        projectionViewer = (ProjectionViewer)getSourceViewer();
        projection = new ProjectionSupport(projectionViewer,getAnnotationAccess(),getSharedColors());
        projection.install();
        
        if(wordfile==null||wordfile.getOpenFoldStrings()==null||wordfile.getCloseFoldStrings()==null) {
        	if(projectionViewer.isProjectionMode()) {
        		ProjectionAnnotationModel model = projectionViewer.getProjectionAnnotationModel();
            	model.expandAll(0,projectionViewer.getDocument().getLength());
            	projectionViewer.disableProjection();
        	}
        } else {
        	projectionViewer.enableProjection();
			annotationListener.addAnnotationProvider(new WordfileFoldAnnotationProvider(getProjectionAnnotationModel(),wordfile.getOpenFoldStrings().toArray(new String[0]),wordfile.getCloseFoldStrings().toArray(new String[0])));
        }
        
        if(annotationListener.getAnnotationProviders().size()!=0) {
			IDocument document = Utilities.getEditorDocument(this);
			annotationListener.updateDocument(document);
			document.addDocumentListener(annotationListener);
        }
    }
    
    /**
     * creates a source viewer for this editor (implies code folding)
     */
    protected ISourceViewer createSourceViewer(Composite parent,IVerticalRuler ruler, int styles) {
    	ProjectionViewer viewer = new ProjectionViewer(parent,ruler,getOverviewRuler(),isOverviewRulerVisible(),styles);
    	getSourceViewerDecorationSupport(viewer);
    	return viewer;
    }
    
    /**
     * Static method to call {@link #refresh} on all open WordfileEditor instances.
     */
    public static void doRefresh() {
    	for(IWorkbenchWindow window:PlatformUI.getWorkbench().getWorkbenchWindows())
    		for(IWorkbenchPage page:window.getPages())
    			for(IEditorReference reference:page.getEditorReferences()) {
    				IEditorPart editor = reference.getEditor(true);
    				if(editor instanceof WordfileEditor)
    					((WordfileEditor)editor).refresh();
    			}
    }
}
