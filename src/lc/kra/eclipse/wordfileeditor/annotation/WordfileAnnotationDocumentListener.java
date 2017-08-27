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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.ui.IEditorPart;

/**
 * this class can be registered to an editor, checks for changes of the document and then calls the provider classes to get a new set of annotations. The new set of annotations is then replacing old annotations in the editor
 * @author Kristian Kraljic
 */
public class WordfileAnnotationDocumentListener implements IDocumentListener {
	/**
	 * the editor this annotation document listener should be installed to
	 */
	protected IEditorPart editor;
	
	/**
	 * a list of @see WordfileAnnotationProvider classes. All these providers will be called if the document is changed for new annotations
	 */
	private List<WordfileAnnotationProvider> providers = new ArrayList<WordfileAnnotationProvider>();
	/**
	 * a list of the currently set annotations for one annotation provider.
	 */
	private HashMap<WordfileAnnotationProvider,Set<Annotation>> annotations = new HashMap<WordfileAnnotationProvider,Set<Annotation>>();
	
	/**
	 * the job used to update the markers in the file (started with some dalay)
	 */
	private Map<IDocument,AnnotationJob> annotationJobs; 
	
	/**
	 * creates a new WordfileAnnotationDocumentListener for one editor (the listener will NOT be added to the editor automatically!) 
	 * @param editor the editor which this listenr should be used for
	 */
	public WordfileAnnotationDocumentListener(IEditorPart editor) {
		this.editor = editor;
		annotationJobs = new HashMap<IDocument,AnnotationJob>();
	}
	
	/**
	 * the updateAnnotations method can be called to replace the annotations with newly fetched annotations from the annotation providers
	 * @param document the document with the actual content
	 */
	public void updateAnnotations(IDocument document) {
		//iterate all providers and ask for a list of annotations
		for(WordfileAnnotationProvider provider:providers) {
			Map<Annotation,Position> annotations = new HashMap<Annotation,Position>();
			Map<Object,List<Position>> groups = provider.getAnnotationGroups(document);
			if(groups==null) return;
			//create new annotations
			for(Entry<Object,List<Position>> group:groups.entrySet())
				for(Position position:group.getValue())
					annotations.put(provider.createAnnotation(group.getKey()),position);
			//delete the old annotations and set the new annotations
			try {
				((AnnotationModel)provider.getAnnotationModel()).replaceAnnotations(this.annotations.get(provider).toArray(new Annotation[0]),annotations);
				this.annotations.put(provider,annotations.keySet());
			} catch(Exception e) { }
		}
	}
	/**
	 * the updateAnnotationsDelayed calls the @see updateAnnotations method after a short delay of 750ms when no change was made in the document in that period
	 * @param document
	 */
	public void updateAnnotationsDelayed(IDocument document) {
		AnnotationJob annotationJob = annotationJobs.get(document);
		if(annotationJob==null) {
			annotationJob = new AnnotationJob(document);
			annotationJobs.put(document,annotationJob);
		}
		annotationJob.cancel();
		annotationJob.schedule(750);
	}
	
	public void updateDocument(IDocument document) { updateAnnotationsDelayed(document); }
	@Override public void documentAboutToBeChanged(DocumentEvent event) {}
	@Override public void documentChanged(DocumentEvent event) { this.updateDocument(event.getDocument()); }
	
	public void addAnnotationProvider(WordfileAnnotationProvider provider) {
		annotations.put(provider,new HashSet<Annotation>());
		providers.add(provider);
	}
	public void removeAnnotationProvider(WordfileAnnotationProvider provider) {
		providers.remove(provider);
		annotations.remove(provider);
	}
	public List<WordfileAnnotationProvider> getAnnotationProviders() { return providers; }
	
	/**
	 * The annotation job is called to update the annotations in one file (document)
	 * @author D043616
	 */
	private class AnnotationJob extends WorkspaceJob {
		/** the document this annotation job is running for */
		private IDocument document;
		
		/**
		 * cereates a new AnnotationJob for a specific document
		 * @param document the documetn to be checked
		 */
		public AnnotationJob(IDocument document) {
			super("WordfileEditor Annotation Scanner");
			this.document = document;
		}
		/**
		 * when it runs it updates the annotations
		 */
		@Override public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
			updateAnnotations(document);
			return Status.OK_STATUS;
		}
		/**
		 * getAdapter is enhanced. It will return IDocument if IDocument.class is the adapter.
		 */
		@Override @SuppressWarnings("unchecked") public <T> T getAdapter(Class<T> adapter) {
	        if(IDocument.class.equals(adapter))
	        	return (T)document;
	        else return super.getAdapter(adapter);
		}
	}
}
