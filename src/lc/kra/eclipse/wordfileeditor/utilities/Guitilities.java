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


import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * this class contains some methods used for creating SWTDialogs
 * @see org.eclipse.debug.internal.ui.SWTFactory for reference
 * @author D043616
 */
public class Guitilities {
	/**
	 * returns the minimum width of a speicifc button with a given font
	 * @param button the button
	 * @return returns the minimum width hint
	 */
	public static int getButtonWidth(Button button) {
		button.setFont(JFaceResources.getDialogFont());
		return Math.max(new PixelConverter(button).convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH),button.computeSize(SWT.DEFAULT,SWT.DEFAULT, true).x);
	}
	
	/**
	 * sets the dimension of the button to it's minimum size
	 * @param button the button
	 */
	public static void setButtonDimension(Button button) {
		Object layoutData = button.getLayoutData();
		if(layoutData instanceof GridData) {
			GridData gridData = (GridData)layoutData;
			gridData.widthHint = getButtonWidth(button);	
			gridData.horizontalAlignment = GridData.FILL;	 
		}
	}		
	
	/**
	 * creates a new check button
	 * @param parent in this parent
	 * @param label with this label
	 * @param checked with this checked state
	 * @param horizontalSpan for a horizontal span
	 * @return a SWT button
	 */
	public static Button createCheckButton(Composite parent,String label,boolean checked,int horizontalSpan) {
		Button button = new Button(parent,SWT.CHECK);
		button.setFont(parent.getFont());
		button.setSelection(checked);
		if(label!=null) button.setText(label);
		
		GridData gridData = new GridData();
		gridData.horizontalSpan = horizontalSpan;
		button.setLayoutData(gridData);
		setButtonDimension(button);
		
		return button;
	}
	
	/**
	 * creates a new push button
	 * @param parent in this parent
	 * @param label with this label
	 * @return a SWT button
	 */
	public static Button createPushButton(Composite parent,String label) {
		Button button = new Button(parent,SWT.PUSH);
		button.setFont(parent.getFont());
		if(label!=null) button.setText(label);
		
		GridData gridData = new GridData();
		button.setLayoutData(gridData);	
		setButtonDimension(button);
		
		return button;	
	}
	
	/**
	 * creates a new link button
	 * @param parent in this parent
	 * @param label with this label
	 * @return a SWT button
	 */
	public static Button createFlatButton(Composite parent,String label) {
		Button button = new Button(parent,SWT.NONE|SWT.PUSH|SWT.FLAT);
		button.setFont(parent.getFont());
		button.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_BLUE));
		if(label!=null) button.setText(label);
		
		GridData gridData = new GridData();
		button.setLayoutData(gridData);	
		setButtonDimension(button);
		
		return button;	
	}
	
	/**
	 * creates a new radio button
	 * @param parent in this parent
	 * @param label with this label
	 * @return a SWT button
	 */
	public static Button createRadioButton(Composite parent,String label) {
		Button button = new Button(parent,SWT.RADIO);
		button.setFont(parent.getFont());
		if(label!=null) button.setText(label);
		
		GridData gridData = new GridData();
		button.setLayoutData(gridData);	
		setButtonDimension(button);
		
		return button;	
	}
	
	/**
	 * create and returns a new SWT label
	 * @param parent in this parent
	 * @param text with this text
	 * @param horizontalSpan and the horizontalSpan for a grid layout
	 * @return a SWT label
	 */
	public static Label createLabel(Composite parent,String text,int horizontalSpan) {
		Label label = new Label(parent,SWT.NONE);
		label.setFont(parent.getFont());
		label.setText(text);
		
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = horizontalSpan;
		gridData.grabExcessHorizontalSpace = false;
		label.setLayoutData(gridData);
		
		return label;
	}
	
	/**
	 * create and returns a new SWT label that is able to wrap text
	 * @param parent in this parent
	 * @param text with this text
	 * @param horizontalSpan and the horizontalSpan for a grid layout
	 * @param wrapWidth the width when the label starts wrapping
	 * @return a SWT label
	 */
	public static Label createWrapLabel(Composite parent,String text,int horizontalSpan,int wrapWidth) {
		Label label = new Label(parent,SWT.NONE|SWT.WRAP);
		label.setFont(parent.getFont());
		label.setText(text);
		
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = horizontalSpan;
		gridData.widthHint = wrapWidth;
		label.setLayoutData(gridData);
		
		return label;
	}
	
	/**
	 * creates a new default composite with a grid layout
	 * @param parent the parent to create the composite in
	 * @param columns the number of columns of the grid
	 * @param horizontalSpan the horizontalSpan of the grid layout
	 * @param gridStyle the style of the grid layout (fill)
	 * @return
	 */
	public static Composite createComposite(Composite parent,int columns,int horizontalSpan,int gridStyle) {
		Composite composite = new Composite(parent,SWT.NONE);
    	composite.setLayout(new GridLayout(columns,false));
    	composite.setFont(parent.getFont());
    	
    	GridData gridData = new GridData(gridStyle);
		gridData.horizontalSpan = horizontalSpan;
    	composite.setLayoutData(gridData);
    	
    	return composite;
	}

	/**
	 * creates and adds a vertical spacer to the parent component
	 * @param parent the parent composite
	 * @param lines the number of lines to be this spacer height
	 */
	public static void createVerticalSpacer(Composite parent,int lines) {
		Label label = new Label(parent, SWT.NONE);
		
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		Layout layout = parent.getLayout();
		if(layout instanceof GridLayout)
			gridData.horizontalSpan = ((GridLayout)parent.getLayout()).numColumns;
		gridData.heightHint = lines;
		label.setLayoutData(gridData);
	}
	
	/**
	 * creates and adds a horizontal spacer to the parent component
	 * @param parent the parent composite
	 * @param lines the number of lines to be this spacer wide
	 */
	public static void createHorizontalSpacer(Composite parent,int lines) {
		Label label = new Label(parent,SWT.NONE);
		
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = lines;
		label.setLayoutData(gridData);
	}
	
	/**
	 * creates a combo box with given items and style
	 * @param parent the parent to create the combo box in
	 * @param style the style of the combo box
	 * @param horizontalSpan the horizontalSpan int he grid layout
	 * @param items the items of thsi combo box or null
	 * @return the combo box
	 */
	public static Combo createCombo(Composite parent,int style,int horizontalSpan,String[] items) {
		Combo combo = new Combo(parent,style);
		combo.setFont(parent.getFont());
		if(items != null)
			combo.setItems(items);
		combo.setVisibleItemCount(30);
		combo.select(0);
		
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = horizontalSpan;
		combo.setLayoutData(gridData);
		
		return combo;
	}
	
	/**
	 * returns the active workbench windwos (if any) otherwise null
	 * @return a shell or null
	 */
	public static Shell getActiveWorkbenchWindow() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if(window==null) {
			IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
			if(windows.length!=0)
			   return windows[0].getShell();
		} else return window.getShell();
		return null;
	}
}