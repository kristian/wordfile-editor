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
package lc.kra.eclipse.wordfileeditor.wordfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;

/**
 * CustomWordfile is a adapter class used for wordfiles which are not build-into an bundle and added by the user
 * @author Kristian Kraljic
 */
public class CustomWordfile extends Wordfile {
	public final File file;
	/**
	 * reads a custom wordfile and parses it
	 * @param file the wordfile to be read
	 * @throws IOException
	 * @throws ParseException
	 */
	public CustomWordfile(File file) throws IOException,ParseException { this(file,false); }
	/**
	 * reads a custom wordfile and parses it
	 * @param file the wordfile to be read
	 * @param strict if strict mode is enabled all errors are returned
	 * @throws IOException
	 * @throws ParseException
	 */
	public CustomWordfile(File file,boolean strict) throws IOException,ParseException {		
		super(new FileInputStream(file),strict);
		this.file = file;
	}
}
