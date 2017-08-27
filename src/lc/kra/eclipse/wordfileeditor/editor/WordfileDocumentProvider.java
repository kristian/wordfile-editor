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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;

import lc.kra.eclipse.wordfileeditor.editor.parse.WordfilePartitionScanner;
import lc.kra.eclipse.wordfileeditor.editor.parse.WordfilePartitioner;
import lc.kra.eclipse.wordfileeditor.wordfile.Wordfile;

/**
 * Provides a Document for a Configuration
 * @author Kristian Kraljic
 */
public class WordfileDocumentProvider extends FileDocumentProvider { //example TextFileDocumentProvider
	/**
	 * the wordfile configuration this WordfileDocumentProvider uses
	 */
    public Wordfile wordfile;
    /**
     * the content type of all wordfile edited files
     */
    public static final IContentType WORDFILE_CONTENT_TYPE = Platform.getContentTypeManager().getContentType(WordfileEditor.class.getName()+".contentType");
    
    /**
     * creates an initial WordfileDocumentProvider for plugin purpose
     */
    public WordfileDocumentProvider() { this(null); }
    
    /**
     * creates a new WordfileDocumentProvider for a configuration
     * @param configuration the configuraiton to use
     */
    public WordfileDocumentProvider(Wordfile wordfile) {
    	this.wordfile = wordfile;
    }
    
    @Override protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = super.createDocument(element);
		if(document!=null) {
	        IDocumentPartitioner partitioner;
	        (partitioner=new WordfilePartitioner(new WordfilePartitionScanner(wordfile),WordfileConfiguration.getContentTypes())).connect(document);
	        document.setDocumentPartitioner(partitioner);
	        return document;
		} else return null;
    }
	
    /**
     * the content type is always the @see WORDFILE_CONTENT_TYPE
     */
    @Override public IContentType getContentType(Object element) throws CoreException {
    	return WORDFILE_CONTENT_TYPE;
    }
}
