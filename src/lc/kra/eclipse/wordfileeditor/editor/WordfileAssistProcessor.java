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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;

import lc.kra.eclipse.wordfileeditor.WordfileEditorActivator;
import lc.kra.eclipse.wordfileeditor.utilities.Utilities;
import lc.kra.eclipse.wordfileeditor.wordfile.Wordfile;
import lc.kra.eclipse.wordfileeditor.wordfile.Wordfile.CodeFormat;
import lc.kra.eclipse.wordfileeditor.wordfile.Wordfile.WordfileType;

/**
 * The WordfileAssistProcessor is used to enable context help while developing 
 * @author Kristian Kraljic
 */
public class WordfileAssistProcessor implements IContentAssistProcessor {
	/**
	 * the images HashMap stores and buffers images generated for the completion proposals
	 * (the dots in front of the completion proposal are colored like they are later highlighted)
	 */
	private static HashMap<RGB,Image> images = new HashMap<RGB,Image>();
	
	/**
	 * the wordfile used for this assist processor
	 */
	private Wordfile wordfile;
	/**
	 * creates a new WordfileAssistProcessor with a specific wordfile
	 * @param wordfile the wordfile to use
	 */
	public WordfileAssistProcessor(Wordfile wordfile) {
		this.wordfile = wordfile;
	}

	/**
	 * computes a completion proposal for the actual offset in the text viewer
	 * @return returns a collection of completion proposals
	 */
	@Override public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,int offset) {
		//if no wordfile was specified no completion proposal can be given
		if(wordfile==null)
			return new ICompletionProposal[]{};
		
		//scan the offset for the current word (therefore go back until a delimiter was reached)
		final String SPACES = " \t\n\r",DELIMITERS = wordfile.getDelimiters()+wordfile.getStringChars()+SPACES;
		String content = viewer.getDocument().get();
		int wordOffset = offset; StringBuilder wordBuilder = new StringBuilder();
		while(--wordOffset>=0) {
			char wordChar = content.charAt(wordOffset);
			if(DELIMITERS.indexOf(wordChar)!=-1)
				break;
			wordBuilder.insert(0,wordChar);
		}
		String word = wordBuilder.toString().toLowerCase();
		
		//check for words matching the completion proposal
		viewer.getDocument().get().substring(offset);
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		for(CodeFormat format:wordfile.getCodeFormats())
			for(String keyword:format.getKeywords()) {
				boolean propose = word.isEmpty();
				if(!propose) propose = keyword.toLowerCase().startsWith(word);
				if(!propose&&wordfile.getType().equals(WordfileType.HTML_LANG))
					if(propose = keyword.toLowerCase().startsWith('<'+word))
						word = '<'+word;
				if(!propose) continue;
				
				Image image = images.get(format.getColors());
				if(image==null) {
					RGB color = format.getColors();
					image = Utilities.changeHue(WordfileEditorActivator.getDefault().getImageRegistry().get("/icons/assist.png"),color.getHSB()[0]);
					images.put(color,image);
				}
				proposals.add(new CompletionProposal(keyword,offset-word.length(),word.length(),keyword.length(),image,keyword,null,null));
			}
		
		if(proposals.isEmpty())
			return null;
		//sort and return the proposals array
		Collections.sort(proposals,new Comparator<ICompletionProposal>() {
			@Override public int compare(ICompletionProposal proposal_a, ICompletionProposal proposal_b) {
				return proposal_a.getDisplayString().compareTo(proposal_b.getDisplayString());
			}
		});
		return proposals.toArray(new ICompletionProposal[0]);
	}

	/**
	 * this method was implemented empty @see IContentAssistProcessor#computeContextInformation
	 */
	@Override public IContextInformation[] computeContextInformation(ITextViewer viewer,int offset) { return new IContextInformation[]{}; }

	/**
	 * this method was implemented empty @see IContentAssistProcessor#getCompletionProposalAutoActivationCharacters
	 */
	@Override public char[] getCompletionProposalAutoActivationCharacters() { return null; }

	/**
	 * this method was implemented empty @see IContentAssistProcessor#getContextInformationAutoActivationCharacters
	 */
	@Override public char[] getContextInformationAutoActivationCharacters() { return null; }

	/**
	 * this method was implemented empty @see IContentAssistProcessor#getErrorMessage
	 */
	@Override public String getErrorMessage() { return null; }

	/**
	 * this method was implemented empty @see IContentAssistProcessor#getContextInformationValidator
	 */
	@Override public IContextInformationValidator getContextInformationValidator() { return null; }
}
