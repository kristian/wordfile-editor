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

import org.eclipse.jface.text.rules.FastPartitioner;

/**
 * The WordfilePartitioner is used to partition a wordile using a wordfile scanner 
 * @author Kristian Kraljic
 */
public class WordfilePartitioner extends FastPartitioner {
	/**
	 * the wordfile scanner used
	 */
	public final WordfilePartitionScanner wordfileScanner;
	/**
	 * creates a new WordfilePartitioner
	 * @param wordfileScanner
	 * @param legalContentTypes
	 */
	public WordfilePartitioner(WordfilePartitionScanner wordfileScanner,String[] legalContentTypes) {
		super(wordfileScanner,legalContentTypes);
		this.wordfileScanner = wordfileScanner;
	}
}
