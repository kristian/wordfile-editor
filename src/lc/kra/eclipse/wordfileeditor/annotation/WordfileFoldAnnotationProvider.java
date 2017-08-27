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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;

/**
 * this annotation provider provides annotations for code folding for a variaty of code styles
 * @author Kristian Kraljic
 */
public class WordfileFoldAnnotationProvider extends WordfileAnnotationProvider {
	/**
	 * the strings which introduce a foldable area
	 */
	private String[] foldStrings,
	/**
	 * the strings which end a foldable area
	 */
	                 unfoldStrings;
	
	/**
	 * creates a new WordfileFoldAnnotationProvider for a projection model, with certain fold and unfold strings
	 * @param model the ProjectionAnnotationModel used
	 * @param foldStrings the strings a fold begins
	 * @param unfoldStrings the strings a fold ends
	 */
	public WordfileFoldAnnotationProvider(ProjectionAnnotationModel model,String[] foldStrings,String[] unfoldStrings) {
		super(model);
		this.foldStrings = foldStrings;
		this.unfoldStrings = unfoldStrings;
	}
	
	/**
	 * create a new projection annotation for this model
	 */
	@Override public Annotation createAnnotation(Object object) { return new ProjectionAnnotation(); }
	/**
	 * checks for the position of fold/unfold strings and returns all areas between them recursivley
	 */
	@Override public Map<Object,List<Position>> getAnnotationGroups(IDocument document) {
		if(foldStrings!=null&&foldStrings.length!=0&&unfoldStrings!=null&&unfoldStrings.length==foldStrings.length) {
			Map<Object,List<Position>> annotations = new HashMap<Object,List<Position>>();
			annotations.put(new Object(),getRiskyFolds(document.get()));
			return annotations;
		} else return null;
	}
	
	/**
	 * returns a list of positions in which folding is possible
	 * @param content the content to be searched for fold/unfold strings
	 * @return a list of positions
	 * @throws ParseException
	 */
	final protected List<Position> getFolds(String content) throws ParseException {
		List<Position> folds = new ArrayList<Position>();
		//check the content recursively
		getRecursiveFolds(content,folds,0,-1,0);
		//remove found areas which cover just one single line, because code folding makes no sense for this
		removeSingleLines(content,folds);
		return folds;
	}
	/**
	 * returns a list of positions in which folding is possible, ignoring that maybe a closing brace is missing (returns as many fold areas as possible) 
	 * @param content the content to be searched for fold/unfold strings
	 * @return a list of positions
	 */
	final protected List<Position> getRiskyFolds(String content) {
		List<Position> folds = new ArrayList<Position>();
		//check the content recursively, ignore the parse error
		try { getRecursiveFolds(content,folds,0,-1,0); }
		catch(ParseException e) { }
		//remove found areas which cover just one single line, because code folding makes no sense for this
		removeSingleLines(content,folds);
		return folds;
	}
	/**
	 * removes single lines from a position list. Therefore the substrings are checked for new line characters. If no \n or \r character was found, the entry is removed
	 * @param content the content to be searched in
	 * @param folds the position of folds which is modified if there were single liens fond
	 */
	private void removeSingleLines(String content,List<Position> folds) {
		Iterator<Position> iterator = folds.iterator();
		while(iterator.hasNext()) {
			Position positon = iterator.next();
			//get the offset string
			String value = content.substring(positon.offset,positon.offset+positon.length);
			//no new line? remove
			if(value.indexOf('\n')==-1&&value.indexOf('\r')==-1)
				iterator.remove();
		}
	}
	
	/**
	 * the main method to search recursively for fold-areas
	 * @param content the content which should be searched for fold areas
	 * @param folds the folds which have been found already
	 * @param depth the current depth of the recursion
	 * @param foldIndex the index of the foldString[foldIndex] (a matching unfoldString[foldIndex] is searched then)
	 * @param offset the current offset of the substring range
	 * @return the position of the last unfold string
	 * @throws ParseException
	 */
	private int getRecursiveFolds(String content,List<Position> folds,int depth,int foldIndex,int offset) throws ParseException {
		int unfoldPosition = Integer.MAX_VALUE;
		if(depth!=0) { //if we are not on the first level, search for the end fold string 
			unfoldPosition = content.indexOf(unfoldStrings[foldIndex]);
			if(unfoldPosition==-1) throw new ParseException("unfold string missing",-1);
		}
		
		//search for an inner fold
		int nextFoldIndex = -1,nextFoldPosition = Integer.MAX_VALUE;
		for(int testFoldIndex=0;testFoldIndex<foldStrings.length;testFoldIndex++) {
			int testFoldPosition = content.indexOf(foldStrings[testFoldIndex]);
			if(testFoldPosition!=-1&&nextFoldPosition>testFoldPosition) {
				nextFoldIndex = testFoldIndex;
				nextFoldPosition = testFoldPosition;
			}
		}
		
		if(unfoldPosition>nextFoldPosition) { //fold in fold
			int nextOffset = nextFoldPosition+foldStrings[nextFoldIndex].length(); 
			int nextRelativeUnfoldPosition = getRecursiveFolds(content.substring(nextOffset),folds,depth+1,nextFoldIndex,offset+nextOffset);
			folds.add(new Position(offset+nextFoldPosition,nextRelativeUnfoldPosition+foldStrings[nextFoldIndex].length()+unfoldStrings[nextFoldIndex].length()));
			//next one on same level
			int nextRelativeOffset = nextOffset+nextRelativeUnfoldPosition+unfoldStrings[nextFoldIndex].length();
			return getRecursiveFolds(content.substring(nextRelativeOffset),folds,depth,foldIndex,offset+nextRelativeOffset)+nextRelativeOffset;
		} else return unfoldPosition;
	}
}
