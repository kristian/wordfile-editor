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
package lc.kra.eclipse.wordfileeditor.annotation;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;

/**
 * this is the base class for all annotation providers used by the @see WordfileAnnotationDocumentListener
 * @author Kristian Kraljic
 */
public abstract class WordfileAnnotationProvider {
	/**
	 * holds the annotation model of this annotation provider
	 */
	private IAnnotationModel model;
	/**
	 * creates a new annotation provider for an annotation model
	 * @param model the model for this annotation provider
	 */
	public WordfileAnnotationProvider(IAnnotationModel model) { this.model = model; }
	
	/**
	 * creates an new annotation for this provider. The object is the parameter of the annotation
	 * @param object the object as an parameter of the annotation
	 * @return An appropriate annotation for this model
	 */
	public abstract Annotation createAnnotation(Object object);
	/**
	 * this method should return a map of grouped position lists for annotations, therefore the document should be checked and all annotations contained in this document should be returned 
	 * @param document the document which is checked
	 * @return a map of grouped position lists for all annotations
	 */
	public abstract Map<Object,List<Position>> getAnnotationGroups(IDocument document);
	
	/**
	 * returns the annotation model
	 * @return the model
	 */
	public IAnnotationModel getAnnotationModel() { return model; }
}
