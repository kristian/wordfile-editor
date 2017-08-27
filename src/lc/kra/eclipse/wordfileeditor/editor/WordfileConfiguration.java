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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

import lc.kra.eclipse.wordfileeditor.editor.parse.NonRuleBasedDamagerRepairer;
import lc.kra.eclipse.wordfileeditor.editor.parse.WordfilePartitionScanner;
import lc.kra.eclipse.wordfileeditor.editor.parse.WordfileScanner;
import lc.kra.eclipse.wordfileeditor.wordfile.Wordfile;

/**
 * The WordfileConfiguration is used to specify all ContentTypes, the ContentAssist, and the Presentation Recoilers
 * @author Kristian Kraljic
 */
public class WordfileConfiguration extends TextSourceViewerConfiguration {
	/**
	 * the @see WordfileEditor this Configuration is used for 
	 */
	public final Wordfile wordfile;
	/**
	 * the WordfileScanner to highlight the Source using the @see Wordfile 
	 */
	private WordfileScanner scanner;
	
	/**
	 * Creates a new WordfileConfiguration
	 * @param wordfile to determine the presentation reconciler
	 */
	public WordfileConfiguration(Wordfile wordfile) { this(wordfile,null); }
	/**
	 * Creates a new WordfileConfiguration using a specific preference store (likely of the editor)
	 * @param wordfile to determine the presentation reconciler
	 * @param preferenceStore the preference store to be used
	 */
	public WordfileConfiguration(Wordfile wordfile,IPreferenceStore preferenceStore) {
		super(preferenceStore);
		this.wordfile = wordfile;
		scanner = new WordfileScanner(wordfile);
	}
	
	/**
	 * Returns the Scanner used
	 * @return the WordfileScanner
	 */
	public WordfileScanner getWordfileScanner() { return scanner; }
	
	/**
	 * @return a list of Content-Types used in the highlighting
	 */
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) { return getContentTypes(); }
	
	/**
	 * @return a list of Content-Types used in the highlighting
	 */
	public static String[] getContentTypes() {
		List<String> types = new ArrayList<String>();
		for(Wordfile.WordfileType type:Wordfile.WordfileType.values())
			types.add(type.toString());
		types.addAll(Arrays.asList(new String[]{IDocument.DEFAULT_CONTENT_TYPE,WordfilePartitionScanner.CONTENT_TYPE_SINGLE_LINE_COMMENT,
				                                                               WordfilePartitionScanner.CONTENT_TYPE_MULTI_LINE_COMMENT}));
		return types.toArray(new String[0]);
	}
	
	/**
	 * Creates an WordfileAssistProcessor and returns the ContentAssistant for it
	 * @return if a wordfile is used a ContentAssist for this wordfile is returned
	 */
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
	    ContentAssistant assistant = new ContentAssistant();
	    IContentAssistProcessor assist = new WordfileAssistProcessor(wordfile);
	    assistant.setContentAssistProcessor(assist,IDocument.DEFAULT_CONTENT_TYPE);
	    assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));
	    return assistant;
	}
	
	/**
	 * @return Returns the IPresentationReconciler for a s SourceViewer
	 */
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();
		
		DefaultDamagerRepairer dr; NonRuleBasedDamagerRepairer ndr;
		
		dr = new DefaultDamagerRepairer(scanner);
		reconciler.setDamager(dr,IDocument.DEFAULT_CONTENT_TYPE);
        reconciler.setRepairer(dr,IDocument.DEFAULT_CONTENT_TYPE);
        
		if(wordfile!=null) { //if a wordfile is used, return additional areas
			ndr = new NonRuleBasedDamagerRepairer(new TextAttribute(convertColor(wordfile.getColors()[Wordfile.COLOR_COMMENT]),convertColor(wordfile.getColorsBack()[Wordfile.COLOR_COMMENT]),wordfile.getFontStyle()[Wordfile.COLOR_COMMENT]));
			reconciler.setDamager(ndr,WordfilePartitionScanner.CONTENT_TYPE_SINGLE_LINE_COMMENT);
			reconciler.setRepairer(ndr,WordfilePartitionScanner.CONTENT_TYPE_SINGLE_LINE_COMMENT);
	
			ndr = new NonRuleBasedDamagerRepairer(new TextAttribute(convertColor(wordfile.getColors()[Wordfile.COLOR_ALTERNATE_BLOCK_COMMENT]),convertColor(wordfile.getColorsBack()[Wordfile.COLOR_ALTERNATE_BLOCK_COMMENT]),wordfile.getFontStyle()[Wordfile.COLOR_ALTERNATE_BLOCK_COMMENT]));
			reconciler.setDamager(ndr,WordfilePartitionScanner.CONTENT_TYPE_MULTI_LINE_COMMENT);
			reconciler.setRepairer(ndr,WordfilePartitionScanner.CONTENT_TYPE_MULTI_LINE_COMMENT);
			
			if(wordfile.getType().equals(Wordfile.WordfileType.HTML_LANG)) { //especially if it is HTML, inner languages need to be displayed
				dr = new DefaultDamagerRepairer(new WordfileScanner(Wordfile.getWordfile(Wordfile.WordfileType.CSS_LANG)));
				reconciler.setDamager(dr,Wordfile.WordfileType.CSS_LANG.toString());
		        reconciler.setRepairer(dr,Wordfile.WordfileType.CSS_LANG.toString());
		        
		        dr = new DefaultDamagerRepairer(new WordfileScanner(Wordfile.getWordfile(Wordfile.WordfileType.JSCRIPT_LANG)));
		        reconciler.setDamager(dr,Wordfile.WordfileType.JSCRIPT_LANG.toString());
		        reconciler.setRepairer(dr,Wordfile.WordfileType.JSCRIPT_LANG.toString());
		        
		        dr = new DefaultDamagerRepairer(new WordfileScanner(Wordfile.getWordfile(Wordfile.WordfileType.PHP_LANG)));
		        reconciler.setDamager(dr,Wordfile.WordfileType.PHP_LANG.toString());
		        reconciler.setRepairer(dr,Wordfile.WordfileType.PHP_LANG.toString());
			}
		}
		
		return reconciler;
	}
	
	/**
	 * at the moment this method is not used until HTMLWidget is avaialble ot be displayed as an information control
	 */
	public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer) {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent,new DefaultInformationControl.IInformationPresenter() {
					@Override public String updatePresentation(Display display,String infoText,TextPresentation presentation,int maxWidth,int maxHeight) {
						return null;
					}
				});
			}
		};
	}

	/**
	 * converts  an RGB value to a color
	 * @param rgb the RGB value
	 * @return a color for this RGB value
	 */
	public static Color convertColor(RGB rgb) { return new Color(Display.getCurrent(),rgb); }
}