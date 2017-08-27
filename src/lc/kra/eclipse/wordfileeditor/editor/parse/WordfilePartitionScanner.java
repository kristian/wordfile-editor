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

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

import lc.kra.eclipse.wordfileeditor.wordfile.Wordfile;

/**
 * The WordfilePartitionScanner is used to partition the code in different sections
 * @author Kristian Kraljic
 */
public class WordfilePartitionScanner extends RuleBasedPartitionScanner {
	/**
	 * The block for single line comments
	 */
	public static final String CONTENT_TYPE_SINGLE_LINE_COMMENT = "SINGLE_LINE_COMMENT";
	/**
	 * The block for multi line comments
	 */
	public static final String CONTENT_TYPE_MULTI_LINE_COMMENT = "SINGLE_LINE_COMMENT";
	
	/**
	 * creates a new partition scanner to be used in a WordfileEditor
	 * @param editor the editor to initialize the scanner for
	 */
	public WordfilePartitionScanner(Wordfile wordfile) { initializeRules(wordfile); }
	
	/**
	 * Creates @link{IPredicateRule}'s to partition this document 
	 * @param editor
	 */
	public void initializeRules(Wordfile wordfile) {
		List<IPredicateRule> rules = new ArrayList<IPredicateRule>();
		
		//further partitions are only needed if the editor uses a wordfile
		if(wordfile!=null) {
			//the multi line comment
			IToken tokenCommentMultiLine = new Token(CONTENT_TYPE_MULTI_LINE_COMMENT);
			if(wordfile.getBlockCommentOn()!=null&&wordfile.getBlockCommentOff()!=null)
				rules.add(new MultiLineRule(wordfile.getBlockCommentOn(),wordfile.getBlockCommentOff(),tokenCommentMultiLine));
			
			//the single line comment
			IToken tokenCommentSingleLine = new Token(CONTENT_TYPE_SINGLE_LINE_COMMENT);
			if(wordfile.getLineComment()!=null)
				rules.add(new SingleLineRule(wordfile.getLineComment(),"\n",tokenCommentSingleLine));
			if(wordfile.getLineCommentAlt()!=null)
				rules.add(new SingleLineRule(wordfile.getLineCommentAlt(),"\n",tokenCommentSingleLine));
			
			//for html further sections are needed to highlight CSS, JavaScript and PHP
			if(wordfile.getType().equals(Wordfile.WordfileType.HTML_LANG)) {
				rules.add(new MultiLineRule("<style","</style>",new Token(Wordfile.WordfileType.CSS_LANG.toString())));
				rules.add(new MultiLineRule("<script","</script>",new Token(Wordfile.WordfileType.JSCRIPT_LANG.toString())));
				rules.add(new MultiLineRule("<?","?>",new Token(Wordfile.WordfileType.PHP_LANG.toString())));
			}
		}
		
		setPredicateRules(rules.toArray(new IPredicateRule[0]));
	}
}
