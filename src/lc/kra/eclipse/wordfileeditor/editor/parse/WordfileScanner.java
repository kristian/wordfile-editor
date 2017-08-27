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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.NumberRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import lc.kra.eclipse.wordfileeditor.editor.WordfileConfiguration;
import lc.kra.eclipse.wordfileeditor.wordfile.Wordfile;

/**
 * the WordfileScanner partitions the document based on wordfile information
 * @author Kristian Kraljic
 */
public class WordfileScanner extends RuleBasedScanner {
	/**
	 * the WordfileRule used to highlight single partitions
	 */
	private WordfileRule rule;
	/**
	 * all rules used to highlight the document with
	 */
	private IRule[] rules;
	
	/**
	 * creates a new WordfileScanner and sets the rules to it
	 * @param wordfile
	 */
	public WordfileScanner(Wordfile wordfile) { setWordfile(wordfile); }
	
	/**
	 * sets a new wordfile to the scanner
	 * @param wordfile the wordfile to set
	 */
	public void setWordfile(Wordfile wordfile) {
		if(wordfile!=null) { //if a wordfile is specified, scan the document based on this wordfile
			setDefaultReturnToken(new Token(new TextAttribute(WordfileConfiguration.convertColor(wordfile.getColors()[Wordfile.COLOR_NORMAL_TEXT]))));
			
			List<IRule> rules = new ArrayList<IRule>();
			//add a rule for each string char in the wordfile
			if(!wordfile.isNoquote()&&wordfile.getStringChars()!=null)
				for(char chr:wordfile.getStringChars().toCharArray())
					if(wordfile.getEscapeChar()!=null)
						 rules.add(new SingleLineRule(Character.toString(chr),Character.toString(chr),new Token(new TextAttribute(WordfileConfiguration.convertColor(wordfile.getColors()[Wordfile.COLOR_STRING]))),wordfile.getEscapeChar().charAt(0)));
					else rules.add(new SingleLineRule(Character.toString(chr),Character.toString(chr),new Token(new TextAttribute(WordfileConfiguration.convertColor(wordfile.getColors()[Wordfile.COLOR_STRING])))));
			//add a number rule
			rules.add(new NumberRule(new Token((new TextAttribute(WordfileConfiguration.convertColor(wordfile.getColors()[Wordfile.COLOR_NUMBER]))))));
			//add the wordfile rule
			rules.add(rule=new WordfileRule(wordfile));
			//add rules which apply to whitespaces
			rules.add(new WhitespaceRule(new IWhitespaceDetector() {
	            @Override public boolean isWhitespace(char c) {
	            	return (c == ' ' || c == '\t' || c == '\n' || c == '\r');
	            }
	        }));
			
			setRules(this.rules=rules.toArray(new IRule[0]));
		} else {
			setDefaultReturnToken(new Token(new TextAttribute(new Color(Display.getCurrent(),0,0,0),new Color(Display.getCurrent(),255,255,255),0)));
			
			List<IRule> rules = new ArrayList<IRule>();
			//add the default string rule
			rules.add(new SingleLineRule("\"","\"",new Token(new TextAttribute(new Color(Display.getCurrent(),128,128,128)))));
			//add a number rule (default red)
			rules.add(new NumberRule(new Token((new TextAttribute(new Color(Display.getCurrent(),255,0,0))))));
			//add rules which apply to whitespaces
			rules.add(new WhitespaceRule(new IWhitespaceDetector() {
	            @Override public boolean isWhitespace(char c) {
	            	return (c == ' ' || c == '\t' || c == '\n' || c == '\r');
	            }
	        }));
			
			setRules(this.rules=rules.toArray(new IRule[0]));
		}
	}

	/**
	 * @return returns the document for this scanner
	 */
	public IDocument getDocument() { return fDocument; }
	/**
	 * @return returns the Wordfile rule
	 */
	public WordfileRule getWordfileRule() { return rule; }
	/**
	 * returns all rules used to highlight this document
	 * @return an arrry of rules
	 */
	public IRule[] getRules() { return rules; }
	
	/*@Override public IToken nextToken() {
		if(super.nextToken()!=Token.EOF) {
			return new Token(new TextAttribute(randomColor())));
		} else return Token.EOF;
	} private Color randomColor() {
		Random random = new Random();
		return WordfileConfiguration.convertColor(new RGB(random.nextInt(256),random.nextInt(256),random.nextInt(256)));
	} */
}
