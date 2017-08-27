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
package lc.kra.eclipse.wordfileeditor.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IEditorStatusLine;

/**
 * the Utilities methods provide acces to commonly used functions
 * @author Kristian Kraljic
 */
public class Utilities {
	/**
	 * checks if a string is numeric (by pasing it to an integer)
	 * @param number the string to be checked
	 * @return weather it is numeric or not
	 */
	public static boolean isNumeric(String number) {
		try { Integer.parseInt(number); }
		catch(NumberFormatException e) { return false; }
		return true;
	}
	
	/**
	 * changes the hue of an image to another hue value
	 * @param source the image source
	 * @param hue the hue to set the image hue to
	 * @return the new image
	 */
	public static Image changeHue(Image source,float hue) {
		//initialize a new graphics context and copy the image
		Device display = Display.getCurrent();
		Image target = new Image(display,source.getBounds());
	    GC graphics = new GC(target);
		graphics.drawImage(source,0,0);
		graphics.dispose();
		
		//get the image data
		ImageData data = target.getImageData();
		PaletteData palette = data.palette;
		RGB transparentRGB = new RGB(255,0,255);
		data.transparentPixel = palette.getPixel(transparentRGB);
		//and set each single pixel (beside the tranparent ones) to the new hue value
		for(int y=0;y<target.getBounds().height;y++)
			for(int x=0;x<target.getBounds().width;x++) {
				RGB rgb = palette.getRGB(data.getPixel(x,y));
				if(rgb.equals(transparentRGB))
					continue;
				float[] hsb = rgb.getHSB();
				data.setPixel(x,y,palette.getPixel(new RGB(hue,hsb[1],hsb[2])));
			}
		
		return new Image(display,data);
	}
	
	/**
	 * Sets a message to the status line of the eclipse editor
	 * @param error If set the message gets displayed red
	 * @param message The message to be set
	 * @return returns true on success
	 */
	public static boolean setStatusMessage(boolean error,String message) {
		IEditorStatusLine statusLine = getStatusLineManager();
		if(statusLine!=null) {
			statusLine.setMessage(error,message,null);
			return true;
		} else return false;
	}
	
	/**
	 * returns the status line manager of the active workbench window or null
	 * @return IEditorStatusLine or null
	 */
	private static IEditorStatusLine getStatusLineManager() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if(window==null)
			return null;
		IWorkbenchPage page = window.getActivePage();
		if(page==null)
			return null;
		IEditorPart editor = page.getActiveEditor();
		if(editor==null)
			return null;
		return (IEditorStatusLine)editor.getAdapter(IEditorStatusLine.class);
	}

	/**
	 * returns the document of an text editor
	 * @param editor the editor to read the document from
	 * @return the IDocument
	 */
	public static IDocument getEditorDocument(AbstractTextEditor editor) {
		IDocumentProvider provider = editor.getDocumentProvider();
		if(provider!=null)
			 return provider.getDocument(editor.getEditorInput());
		else return null;
	}

	/**
	 * gets the editor file (if the editor was openened as a file), otherwise it returns null 
	 * @param editor the editor to read the file from
	 * @return an IFile or null
	 */
	public static IFile getEditorFile(IEditorPart editor) {
		IEditorInput input = editor.getEditorInput();
		if(input instanceof IFileEditorInput)
			 return ((IFileEditorInput)input).getFile();
		else return null; 
	}
	
	/**
	 * returns the file of an document (it is required that an editor is opened, holding this document, with a file source)
	 * @param document the document ot get the file from
	 * @return the IFile or null
	 */
	public static IFile getDocumentFile(IDocument document) {
		IFile file = null;
		//iterate over all active editors
		for(IEditorReference reference:PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences()) {
			//get the editor part
        	IEditorPart editor = reference.getEditor(false);
        	IDocument contestant = null;
        	if(!(editor instanceof AbstractTextEditor)) {
	        	try {
	        		//check if the editor supports the getTextViewer method
	        		Method method = editor.getClass().getMethod("getTextViewer");
	        		Object result = method.invoke(editor);
	        		if(result instanceof ITextViewer)
	        			contestant = ((ITextViewer)result).getDocument();
	        	} catch(Exception e) {} //ignore...
        	} else contestant = Utilities.getEditorDocument((AbstractTextEditor)editor);
        	if(contestant!=null&&contestant.equals(document))
        		if((file=Utilities.getEditorFile(editor))!=null)
        			break;
        }
    	return file;
	}
	
	/**
	 * returns the size of a file
	 * @param file the file to get the size for
	 * @return returns the size in bytes
	 */
	public static long getFileSize(IFile file) { return file.getLocation().toFile().length(); }
	
	/**
	 * reads the contents of an stream into a string and closes it afterwards
	 * @param input the stream to read
	 * @return a string with the file content
	 * @throws IOException
	 */
	public static String asString(InputStream input) throws IOException { return asString(input,true); }
	/**
	 * reads the contents of an stream into a string
	 * @param input the stream to read
	 * @param close true if the input stream should be closed after reading
	 * @return a string with the file content
	 * @throws IOException
	 */
	public static String asString(InputStream input,boolean close) throws IOException {
		StringBuffer output = new StringBuffer();
		byte[] buffer = new byte[4096];
		for(int read;(read=input.read(buffer))!=-1;)
			output.append(new String(buffer,0,read));
		if(close) input.close();
		return output.toString();
	}
	
	/**
	 * counts the occurences of a needle in a haystack string
	 * @param haystack the string to be searched in
	 * @param needle the char to search for
	 * @return returns the number of occurences in the string
	 */
	public static int countOccurrences(String haystack,char needle) {
		int count=0,index=0;
		while((index=haystack.indexOf(needle,index))!=-1)
			{ ++index; ++count; }
		return count;
	}
}
