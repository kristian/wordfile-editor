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
package lc.kra.eclipse.wordfileeditor.editor.parse;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import lc.kra.eclipse.wordfileeditor.editor.WordfileConfiguration;
import lc.kra.eclipse.wordfileeditor.wordfile.Wordfile;

/**
 * the WordfileRule is the main class to enable syntax highlighting based on wordfiles
 * @author Kristian Kraljic
 */
public class WordfileRule implements IRule {
	/**
	 * the end tag of HTML language(s)
	 */
	private final char TAG_END = '>';
	
	/** The word detector used by this rule. */
	protected IWordDetector fDetector;
	/** The table of predefined words and token for this rule. */
	protected Map<String,IToken> fWords = new HashMap<String,IToken>();
	/** The set of prefixes used for highlighting */
	protected Map<String,IToken> fPrefixes = new HashMap<String,IToken>();
	/** The set of single character tokens */
	protected Map<Character,IToken> fCharacters = new HashMap<Character,IToken>();
	
	/** Buffer used for pattern detection. */
	protected StringBuffer fBuffer= new StringBuffer();
	/** used word file */
	protected Wordfile fWordfile;
	/** set to null if it was not recognized, true/false if was definitely (not) recognized */
	protected Boolean fIsRecognized = null;
	
	/**
	 * the default token to return if no token was recognized
	 */
	private IToken fDefaultToken;
	/**
	 * is set to true if this language is tag based (so < and > will be ignored)
	 */
	private boolean fIsTagBased;

	/**
	 * the number of total parsed tokens and the number of tokens unrecognized (used for calculation the recognition factor)
	 */
	private int fTokenCount = 0,fTokenUnrecognized = 0;
	
	/**
	 * creates a new wordfile rule for a specific wordfile
	 * @param wordfile the wordfile to create the wordfile rul efor
	 */
	public WordfileRule(Wordfile wordfile) {
		fWordfile = wordfile;
		Wordfile.WordfileType type = wordfile.getType();
		fDefaultToken = new Token(new Color(Display.getCurrent(),wordfile.getColors()[Wordfile.COLOR_NORMAL_TEXT]));
		fIsTagBased = type.equals(Wordfile.WordfileType.HTML_LANG)
		            ||type.equals(Wordfile.WordfileType.XML_LANG);
		
		//initialize the word detector (at delimiters)
		final String SPACES = " \t\n\r",DELIMITERS = wordfile.getDelimiters()+wordfile.getStringChars()+SPACES;
		fDetector = new IWordDetector() {
			@Override public boolean isWordStart(char c) { return SPACES.indexOf(c)==-1; }
			@Override public boolean isWordPart(char c) { return DELIMITERS.indexOf(c)==-1; }
		};
		//initialize the rule for all code formats
		for(Wordfile.CodeFormat codeFormat:wordfile.getCodeFormats()) {
			IToken tokenType = new Token(new TextAttribute(WordfileConfiguration.convertColor(codeFormat.getColors()),WordfileConfiguration.convertColor(codeFormat.getColorsBack()),codeFormat.getFontStyle()));
			Set<String> keywords = codeFormat.getKeywords();
			for(String keyword:keywords)
				if(!keyword.trim().isEmpty())
					fWords.put(fWordfile.isNocase()?keyword.trim().toLowerCase():keyword.trim(),tokenType);
			for(String prefix:codeFormat.getPrefixes())
				if(!prefix.trim().isEmpty())
					fPrefixes.put(fWordfile.isNocase()?prefix.trim().toLowerCase():prefix.trim(),tokenType);
			for(char delimiter:DELIMITERS.toCharArray())
				if(keywords.contains(Character.toString(delimiter)))
					fCharacters.put(fWordfile.isNocase()?Character.toLowerCase(delimiter):delimiter,tokenType);
		}
	}
		
	/**
	 * uses the scanner to determine the next token
	 * @return a recognized token or Token.UNDEFINED
	 */
	public IToken evaluate(ICharacterScanner scanner) {		
		//scan a single character, if it is a word start go on
		int chr = scanner.read();
		if(chr==ICharacterScanner.EOF||!fDetector.isWordStart((char)chr)) {
			scanner.unread();
			return Token.UNDEFINED;
		}
		
		//get the next word candiate
		String candidate;
		if(fWordfile.getDelimiters().indexOf(chr)==-1) { //a delimter is always recognized as an own word
			fBuffer.setLength(0);
			do {
				fBuffer.append((char)chr);
				if(fIsTagBased&&chr==TAG_END) {
					scanner.read();
					break;
				} else chr = scanner.read();
			} while(chr!=ICharacterScanner.EOF&&fDetector.isWordPart((char)chr));
			scanner.unread();
			candidate = fBuffer.toString().trim();
		} else candidate = Character.toString((char)chr);
			
		//check if the word is recognized in any token
		if(fWordfile.isNocase())
			candidate = candidate.toLowerCase();
		
		IToken token;
		fTokenCount++;

		token = evaluateCandidate(candidate);
		if(!token.isUndefined())
			return token;
		
		token = evaluateCandidate(candidate+(char)chr);
		if(!token.isUndefined()) {
			scanner.read();
			return token;
		}
		
		return fDefaultToken;
	}
	
	/**
	 * this method evaluates a single token, if it is tag based the candiate is evaluated and < or > are stripped
	 * @param candidate the candidate to be evaluated
	 * @return an IToken or Token.UNDEFINED
	 */
	protected IToken evaluateCandidate(String candidate) {		
		IToken token;
		
		//if there is an explicit match return the explicit match
		if((token=fWords.get(candidate))!=null)
			return token;
		
		
		//if a prefix matches return the prefix
		for(String prefix:fPrefixes.keySet())
			if(candidate.startsWith(prefix))
				return fPrefixes.get(prefix);
		
		//if the language is tag based, try ot remove <, >, and />
		if(fIsTagBased) {
			if(!candidate.startsWith("<")) {
				if(candidate.startsWith("/"))
					 candidate = "<"+candidate;
				else candidate = "/"+candidate;
				return evaluateCandidate(candidate);
			}
			if(!candidate.endsWith(">"))
				return evaluateCandidate(candidate+">");
		}
		
		fTokenUnrecognized++;
		
		return Token.UNDEFINED;
	}


	/**
	 * returns the last read token into the buffer
	 * @param scanner the scanner to return the token to
	 */
	protected void unreadBuffer(ICharacterScanner scanner) {
		for(int i= fBuffer.length() - 1; i >= 0; i--) //unread each single character
			scanner.unread();
	}
	
	/**
	 * returns the recognition factor of this rule. If it has been recognized definitely
	 * Double.POSITIVE_INFINITY is returned, otherwise a value near to 1 is good
	 * @return
	 */
	public double getRecognitionFactor() {
		if(fIsRecognized!=null&&fIsRecognized.booleanValue()) return Double.POSITIVE_INFINITY;
		if(fTokenCount<=0)
			return 0;
		return ((double)(fTokenCount-fTokenUnrecognized)/fTokenCount);
	}
}
