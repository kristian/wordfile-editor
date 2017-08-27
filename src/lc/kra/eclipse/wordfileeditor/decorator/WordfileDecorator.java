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
package lc.kra.eclipse.wordfileeditor.decorator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IDecoratorManager;

import lc.kra.eclipse.wordfileeditor.WordfileEditorActivator;
import lc.kra.eclipse.wordfileeditor.editor.WordfileEditor;
import lc.kra.eclipse.wordfileeditor.wordfile.Wordfile;

/**
 * Decorator class to provide language-specific icons for all wordfile files.
 * @author Kristian Kraljic
 */
public class WordfileDecorator extends LabelProvider implements ILabelDecorator {
	/**
	 * Provides a graphical representation of the file by overlaying an
	 * icon for each language onto the normal file icon.
	 */
	@Override public Image decorateImage(Image image, Object element) {
		IFile file = getFile(element);
		if(file==null)
			return null;
		Wordfile wordfile = WordfileEditor.determineWordfile(file); //get the wordfile
		if(wordfile==null||wordfile.getFileExtensions()==null)
			return null;
		ImageRegistry registry = WordfileEditorActivator.getDefault().getImageRegistry();
		ImageDescriptor descriptor = null;
		//read the approprate image
		for(String extention:wordfile.getFileExtensions())
			if((descriptor=registry.getDescriptor("/icons/decorations/"+extention.toLowerCase()+".png"))!=null)
				break;
		//and set the decoration
		if(descriptor!=null) {
			//create a overlay image
			ImageDescriptor[] overlays = new ImageDescriptor[] {null,null,null,null};
			overlays[IDecoration.BOTTOM_LEFT] = descriptor;
			DecorationOverlayIcon decorator = new DecorationOverlayIcon(image,overlays,new Point(image.getImageData().width,image.getImageData().height));
			return decorator.createImage();
		} else return null;
	}

	/**
	 * Provide a textual representation for the ProjectExplorer,
	 * ProjectNavigator, …. Returns the filename plus the language
	 * in parenthesis.
	 * @return text for this element (filename plus language)
	 */
	@Override public String decorateText(String text,Object element) {
		IFile file = getFile(element);
		if(file==null)
			return null;
		Wordfile wordfile = WordfileEditor.determineWordfile(file);
		return file.getName()+" ("+(wordfile!=null?wordfile.getName():"General")+")";
	}
	
	/**
	 * returns an IFile if the element is an IFile and the default editor is an WordfileEditor
	 * @param element the element to be checked
	 * @return and IFile or null
	 */
	private static IFile getFile(Object element) {
		if(!(element instanceof IFile))
			return null;
		IFile file = (IFile)element;
		try {
			if(WordfileEditor.isWordfileEditorAssigned(file))
				return file;
		} catch (CoreException e) { return null; }
		return null;
	}
	
	/**
	 * Fires a LabelProviderChanged event to update all the icons. Should
	 * be called when the underlying properties, such as the language,
	 * changes.
	 */
	public void refresh() {
		final LabelProviderChangedEvent event = new LabelProviderChangedEvent(this);
		Display.getDefault().asyncExec(new Runnable() {
			@Override public void run() { fireLabelProviderChanged(event); }
		});
	}

	/**
	 * Static method to call {@link #refresh} on the LabelDecorator instance.
	 */
	public static void doRefresh() {
		IDecoratorManager decoratorManager = WordfileEditorActivator.getDefault().getWorkbench().getDecoratorManager();
		WordfileDecorator decorator = (WordfileDecorator)decoratorManager.getLabelDecorator(WordfileEditor.class.getName()+".decorator");
		if(decorator!=null)
			decorator.refresh();
	}
}