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

import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

import org.eclipse.swt.graphics.RGB;

import lc.kra.eclipse.wordfileeditor.WordfileEditorActivator;

/**
 * reads, parses and provides access to wordfiles
 * @author Kristian Kraljic
 */
public class Wordfile {
	/**
	 * contains all supported word file types by the standard wordfile specification
	 * @author Kristian Kraljic
	 */
	public enum WordfileType {
		UNSPECIFIED,CARAMASK_LANG,C_LANG,COBOL_LANG,FORTRAN_LANG,PASCAL_LANG,PERL_LANG,PLB_LANG,VB_LANG,VBSCRIPT_LANG,ASP_LANG,CSHARP_LANG,CSS_LANG,LATEX_LANG,HTML_LANG,JAVA_LANG,JSCRIPT_LANG,ECMA_LANG,PHP_LANG,PYTHON_LANG,XML_LANG,MASM_LANG,AASM_LANG,NASM_LANG,SQL_LANG;
		/**
		 * checks if there is an wordfile type with this name
		 * @param type the type to check
		 * @return true if it exists
		 */
		public static boolean isWordfileType(String type) {
			try { WordfileType.valueOf(type); }
			catch(Exception e) { return false; }
			return true;
		}
	};
	/**
	 * a set of all loaded wordfiles
	 */
	protected static Set<Wordfile> wordfiles;
	/**
	 * try to read all wordfiles (build-in) in the wordfiles folder
	 */
	static { //load default wordfiles
		wordfiles = new HashSet<Wordfile>();
		Object resource = null;
		Enumeration<?> enumerator = null;
		//if this thing is run in a plugin use the Activator methods otherwise use normal File-methods
		if(WordfileEditorActivator.getDefault()!=null)
			 enumerator = WordfileEditorActivator.getDefault().getBundle().findEntries("/","*.wordfile",true);
		else enumerator = Collections.enumeration(Arrays.asList(new File(Wordfile.class.getResource("wordfiles/").getFile()).listFiles()));
		while(enumerator!=null&&enumerator.hasMoreElements()&&(resource=enumerator.nextElement())!=null)
			if(resource.toString().endsWith(".wordfile"))
				//try to instanciate and add the wordfiles to the wordfiles set
				try { new Wordfile((resource instanceof URL)?((URL)resource).openStream():new FileInputStream(((File)resource))); }
				catch(Exception e) {
					System.err.println("error while loading default wordfile '"+(resource.toString().lastIndexOf('/')!=-1?resource.toString().substring(resource.toString().lastIndexOf('/')):resource.toString())+"' ("+e.getClass().getSimpleName()+"): "+e.getMessage());
				}
	}
	
	/**
	 * color constants of text
	 */
	public static final int COLOR_NORMAL_TEXT = 0;
	public static final int COLOR_COMMENT = 1;
	public static final int COLOR_ALTERNATE_BLOCK_COMMENT = 2;
	public static final int COLOR_STRING = 3;
	public static final int COLOR_NUMBER = 4;
	/**
	 * the default text styling
	 */
	private static final RGB[] COLORS_DEFAULT = new RGB[5];
	private static final RGB[] COLORS_BACK_DEFAULT = new RGB[5];
	private static final boolean[] COLORS_AUTO_BACK_DEFAULT = new boolean[]{true,true,true,true,true};
	private static final int[] FONT_STYLE_DEFAULT = new int[]{0,0,0,0,0};
	static {
		COLORS_DEFAULT[COLOR_NORMAL_TEXT] = new RGB(0,0,0);
		COLORS_DEFAULT[COLOR_COMMENT] = new RGB(0,128,128);
		COLORS_DEFAULT[COLOR_ALTERNATE_BLOCK_COMMENT] = new RGB(0,128,128);
		COLORS_DEFAULT[COLOR_STRING] = new RGB(128,128,128);
		COLORS_DEFAULT[COLOR_NUMBER] = new RGB(255,0,0);
		COLORS_BACK_DEFAULT[COLOR_NORMAL_TEXT] = COLORS_BACK_DEFAULT[COLOR_COMMENT] = COLORS_BACK_DEFAULT[COLOR_ALTERNATE_BLOCK_COMMENT] = COLORS_BACK_DEFAULT[COLOR_STRING] = COLORS_BACK_DEFAULT[COLOR_NUMBER] = new RGB(255,255,255);
	}
	
	/**
	 * delimiters in wordfiles and the escape characters
	 */
	private static final String WORD_DELIMITER = " ";
	private static final String STRING_DELIMITER = "\"";
	private static final String ESCAPE_DELIMITER = "\\";

	/**
	 * the line ends in a wordfile
	 */
	private static final String CARRAGE_RETURN = "\r";
	private static final String LINE_FEED = "\n";

	/**
	 * the assignment character
	 */
	private static final String ASSIGN = "=";

	/**
	 * the string tokenizer for one single wordfile and the currently read token
	 */
	private StringTokenizer tokens;
	private String token;

	/**
	 * the wordfile number
	 */
	protected int number;
	/**
	 * the wordfile name
	 */
	protected String name;
	/**
	 * the wordfile type
	 */
	protected WordfileType type;

	/**
	 * is the wordfile case insenstive, does it use quotes?
	 */
	protected boolean nocase, noquote, enableMLS, disableMLS, enableSpellasYouType;
	/**
	 * see wordfile documentation
	 */
	protected String blockCommentOn,blockCommentOff,blockCommentOnAlt,blockCommentOffAlt,lineComment,lineCommentAlt,lineCommentValidColumns,lineCommentPrecedingChars,escapeChar,validColumns,stringChars,stringLiteralPrefix,delimiters,regexType;

	/**
	 * see wordfile documentation
	 */
	protected Set<String> fileNames,fileExtensions,identStrings,identStringsSOL,unidentStrings,openBraceStrings,closeBraceStrings,openFoldStrings,closeFoldStrings,openCommentFoldStrings,closeCommentFoldStrings,ignoreFoldStrings,ignoreStringsSOL,markerCharacters;
	/**
	 * see wordfile documentation
	 */
	protected Set<String> functionString,memberString,variableString;

	/**
	 * the colors used to highlight the areas
	 */
	protected RGB[] colors,colorsBack;
	/**
	 * use automated background colors?
	 */
	protected boolean[] colorsAutoBack;
	/**
	 * the default fontStyles to use
	 */
	protected int[] fontStyle;

	/**
	 * codeFormats are the keyword areas in the wordfile (used to highlight the keywords)
	 */
	protected Set<CodeFormat> codeFormats = new HashSet<CodeFormat>();

	/**
	 * read and parse a wordfile
	 * @param stream the stream to read the wordfile from
	 * @throws IOException
	 * @throws ParseException
	 */
	public Wordfile(InputStream stream) throws IOException,ParseException { this(stream,false); }
	/**
	 * read and parse a wordfile
	 * @param stream the stream to read the wordfile from
	 * @param strict if strict mode is enabled all errors will be returned
	 * @throws IOException
	 * @throws ParseException
	 */
	public Wordfile(InputStream stream,boolean strict) throws IOException,ParseException {
		int position = 0; String source = readStream(stream);
		while(true)
			try { parse(source,position); wordfiles.add(this); break; }
			catch(ParseException parent) {
				int offset = parent.getErrorOffset();
				ParseException extend = new ParseException(parent.getMessage()+" at character "+offset,offset);
				if(strict||offset<=position) { //if it is strict, or we stayed at the same position raise an error
					extend.setStackTrace(parent.getStackTrace());
					throw extend;
				} else System.err.println(extend.getMessage());
				if(offset!=source.length()) {
					position = parent.getErrorOffset();
					continue;
				} else break;
			}
	}

	/**
	 * parse the top level of the wordfile therefore, tokenize the wordfile and try to parseit
	 * @param source the source of the wordfile
	 * @param position the position start parsing the source
	 * @throws ParseException
	 */
	protected void parse(String source,int position) throws ParseException {
		tokens = new StringTokenizer(source);
		if(position>0) tokens.setCurrentPosition(position);
		try {
			//in this parse block the first token has to begin with a / anything else is wrong
			while(tokens.hasMoreTokens()&&nextCommand()!=null) {
				     if(token.startsWith("/")) parseCommand();
				else if(token.isEmpty()||token.equals(LINE_FEED)) continue; //ignore
				else if(token.startsWith(";")||token.startsWith("--")) nextLine(); //skip line
				else if(position!=0) continue; //we start in the middle of somewhere, we skip until we reach a valid first token
				else throw new ParseException("literal '/' or ';' or '--' expected at begin of line",tokens.getCurrentPosition());
				position = 0;
			}
		} catch(NoSuchElementException e) { throw new ParseException("unexpected end of file",source.length()); }
	}
	/**
	 * parses all different commands used in a wordfile
	 * @throws ParseException
	 */
	private void parseCommand() throws ParseException  {
		String string;
		     if(token.startsWith("/L")&&token.length()>2&&Character.isDigit(token.charAt(2))) parseL(true);
		else if(token.startsWith("/C")&&token.length()>2&&Character.isDigit(token.charAt(2))) parseC();
		else if(token.equalsIgnoreCase("/Colors"))
			if(nextCommand().equals(ASSIGN)) {
				String[] colors = nextLine().split(",");
				if(colors.length<5)
					throw new ParseException("at least fife Colors expected after /Colors command",tokens.getCurrentPosition());
				this.colors = new RGB[]{convertColor(colors[0]),convertColor(colors[1]),convertColor(colors[2]),convertColor(colors[3]),convertColor(colors[4])};
			} else if(token.equals("Back"))
				if(nextCommand().equals(ASSIGN)) {
					String[] colorsBack = nextLine().split(",");
					if(colorsBack.length<5)
						throw new ParseException("at least fife ColorsBack expected after /Colors Back command",tokens.getCurrentPosition());
					this.colorsBack = new RGB[]{convertColor(colorsBack[0]),convertColor(colorsBack[1]),convertColor(colorsBack[2]),convertColor(colorsBack[3]),convertColor(colorsBack[4])};
				} else throw new ParseException("literal '=' expected after /Colors Back command",tokens.getCurrentPosition());
			else if(token.equals("Auto"))
				if(nextCommand().equals("Back"))
					if(nextCommand().equals(ASSIGN)) {
						String[] colorsAutoBack = nextLine().split(",");
						if(colorsAutoBack.length<5)
							throw new ParseException("at least fife ColorsAutoBack expected after /Colors Auto Back command",tokens.getCurrentPosition());
						this.colorsAutoBack = new boolean[]{convertBoolean(colorsAutoBack[0]),convertBoolean(colorsAutoBack[1]),convertBoolean(colorsAutoBack[2]),convertBoolean(colorsAutoBack[3]),convertBoolean(colorsAutoBack[4])};
					} else throw new ParseException("literal '=' expected after /Colors Auto Back command",tokens.getCurrentPosition());
				else throw new ParseException("literal 'Back' expected after /Colors Auto command",tokens.getCurrentPosition());
			else throw new ParseException("literal '=' or 'Back' or 'Auto' expected after /Colors command",tokens.getCurrentPosition());
		else if(token.equalsIgnoreCase("/Font"))
			if(nextCommand().equals("Style"))
				if(nextCommand().equals(ASSIGN)) {
					String[] fontStyle = nextLine().split(",");
					if(fontStyle.length<5)
						throw new ParseException("at least fife FontStyle expected after /Font Style command",tokens.getCurrentPosition());
					this.fontStyle = new int[]{convertFontStyle(fontStyle[0]),convertFontStyle(fontStyle[1]),convertFontStyle(fontStyle[2]),convertFontStyle(fontStyle[3]),convertFontStyle(fontStyle[4])};
				} else throw new ParseException("literal '=' expected after /Font Style command",tokens.getCurrentPosition());
			else throw new ParseException("literal 'Style' expected after /Font command",tokens.getCurrentPosition());
		else if(token.equalsIgnoreCase("/Delimiters")) {
			if(nextCommand().equals(ASSIGN))
				 delimiters = nextLine();
			else throw new ParseException("literal '=' expected after /Delimiters command",tokens.getCurrentPosition());
		} else if(token.equalsIgnoreCase("/Indent"))
			if(nextCommand().equals("Strings"))
				if(nextCommand().equals(ASSIGN)) {
					identStrings = new LinkedHashSet<String>();
					while(!(string=nextString(false)).equals(LINE_FEED))
						identStrings.add(string);
				} else if(token.equals("SOL")) {
					identStringsSOL = new LinkedHashSet<String>();
					while(!(string=nextString(true)).equals(LINE_FEED))
						identStringsSOL.add(string);					
				} else throw new ParseException("literal '=' or 'SOL' expected after /Ident Strings command",tokens.getCurrentPosition());
			else throw new ParseException("literal 'Strings' expected after /Indent command",tokens.getCurrentPosition());
		else if(token.equalsIgnoreCase("/Unindent"))
			if(nextCommand().equals("Strings"))
				if(nextCommand().equals(ASSIGN)) {
					unidentStrings = new LinkedHashSet<String>();
					while(!(string=nextString(true)).equals(LINE_FEED))
						unidentStrings.add(string);
				} else throw new ParseException("literal '=' expected after /Unindent Strings command",tokens.getCurrentPosition());
			else throw new ParseException("literal 'Strings' expected after /Unindent command",tokens.getCurrentPosition());
		else if(token.equalsIgnoreCase("/Open"))
			if(nextCommand().equals("Brace"))
				if(nextCommand().equals("Strings"))
					if(nextCommand().equals(ASSIGN)) {
						openBraceStrings = new HashSet<String>();
						while(!(string=nextString(true)).equals(LINE_FEED))
							openBraceStrings.add(string);
					} else throw new ParseException("literal '=' expected after /Open Brace Strings command",tokens.getCurrentPosition());
				else throw new ParseException("literal 'Strings' expected after /Open Brace command",tokens.getCurrentPosition());
			else if(token.equals("Fold"))
				if(nextCommand().equals("Strings"))
					if(nextCommand().equals(ASSIGN)) {
						openFoldStrings = new HashSet<String>();
						while(!(string=nextString(true)).equals(LINE_FEED))
							openFoldStrings.add(string);
					} else throw new ParseException("literal '=' expected after /Open Fold Strings",tokens.getCurrentPosition());
				else throw new ParseException("literal 'Strings' expected after /Open Fold command",tokens.getCurrentPosition());
			else if(token.equals("Comment"))
				if(nextCommand().equals("Fold"))
					if(nextCommand().equals("Strings"))
						if(nextCommand().equals(ASSIGN)) {
							openCommentFoldStrings = new HashSet<String>();
							while(!(string=nextString(true)).equals(LINE_FEED))
								openCommentFoldStrings.add(string);
						} else throw new ParseException("literal '=' expected after /Open Comment Fold Strings",tokens.getCurrentPosition());
					else throw new ParseException("literal 'Strings' expected after /Open Comment Fold command",tokens.getCurrentPosition());
				else throw new ParseException("literal 'Fold' expected after /Open Comment command",tokens.getCurrentPosition());
			else throw new ParseException("literal 'Brace' or 'Fold' or 'Comment' expected after /Open command",tokens.getCurrentPosition());
		else if(token.equalsIgnoreCase("/Close"))
			if(nextCommand().equals("Brace"))
				if(nextCommand().equals("Strings"))
					if(nextCommand().equals(ASSIGN)) {
						closeBraceStrings = new HashSet<String>();
						while(!(string=nextString(true)).equals(LINE_FEED))
							closeBraceStrings.add(string);
					} else throw new ParseException("literal '=' expected after /Close Brace Strings",tokens.getCurrentPosition());
				else throw new ParseException("literal 'Strings' expected after /Close Brace command",tokens.getCurrentPosition());
			else if(token.equals("Fold"))
				if(nextCommand().equals("Strings"))
					if(nextCommand().equals(ASSIGN)) {
						closeFoldStrings = new HashSet<String>();
						while(!(string=nextString(true)).equals(LINE_FEED))
							closeFoldStrings.add(string);
					} else throw new ParseException("literal '=' expected after /Close Fold Strings",tokens.getCurrentPosition());
				else throw new ParseException("literal 'Strings' expected after /Close Fold command",tokens.getCurrentPosition());
			else if(token.equals("Comment"))
				if(nextCommand().equals("Fold"))
					if(nextCommand().equals("Strings"))
						if(nextCommand().equals(ASSIGN)) {
							closeCommentFoldStrings = new HashSet<String>();
							while(!(string=nextString(true)).equals(LINE_FEED))
								closeCommentFoldStrings.add(string);
						} else throw new ParseException("literal '=' expected after /Open Comment Fold Strings",tokens.getCurrentPosition());
					else throw new ParseException("literal 'Strings' expected after /Open Comment Fold command",tokens.getCurrentPosition());
				else throw new ParseException("literal 'Fold' expected after /Open Comment command",tokens.getCurrentPosition());
			else throw new ParseException("literal 'Brace' or 'Fold' or 'Comment' expected after /Close command",tokens.getCurrentPosition());
		else if(token.equalsIgnoreCase("/Ignore"))
			if(nextCommand().equals("Fold"))
				if(nextCommand().equals("Strings"))
					if(nextCommand().equals(ASSIGN)) {
						ignoreFoldStrings = new HashSet<String>();
						while(!(string=nextString(true)).equals(LINE_FEED))
							ignoreFoldStrings.add(string);
					} else throw new ParseException("literal '=' expected after /Ignore Fold Strings",tokens.getCurrentPosition());
				else throw new ParseException("literal 'Strings' expected after /Ignore Fold command",tokens.getCurrentPosition());
			else if(token.equals("Strings"))
				if(nextCommand().equals("SOL"))
					if(nextCommand().equals(ASSIGN)) {
						ignoreStringsSOL = new HashSet<String>();
						while(!(string=nextString(true)).equals(LINE_FEED))
							ignoreStringsSOL.add(string);
					} else throw new ParseException("literal '=' expected after /Ignore String SOL",tokens.getCurrentPosition());
				else throw new ParseException("literal 'SOL' expected after /Ignore Strings command",tokens.getCurrentPosition());
			else throw new ParseException("literal 'Fold' or 'Strings' expected after /Ignore command",tokens.getCurrentPosition());
		else if(token.equalsIgnoreCase("/Function"))
			if(nextCommand().equals("String"))
				if(nextCommand().equals(ASSIGN)||nextToken().equals(ASSIGN)) { //assign or number (we don't check number)
					if(functionString==null)
						functionString = new HashSet<String>();
					functionString.add(nextLine().trim());
				} else throw new ParseException("literal '=' expected after /Function String command",tokens.getCurrentPosition());
			else throw new ParseException("literal 'String' expected after /Function command",tokens.getCurrentPosition());
		else if(token.equalsIgnoreCase("/Member"))
			if(nextCommand().equals("String"))
				if(nextCommand().equals(ASSIGN)||nextCommand().equals(ASSIGN)) { //assign or number (we don't check number)
					if(memberString==null)
						memberString = new HashSet<String>();
					while(!(string=nextString(false)).equals(LINE_FEED))
						memberString.add(string);
				} else throw new ParseException("literal '=' expected after /Member String command",tokens.getCurrentPosition());
			else throw new ParseException("literal 'String' expected after /Member command",tokens.getCurrentPosition());
		else if(token.equalsIgnoreCase("/Variable"))
			if(nextCommand().equals("String"))
				if(nextCommand().equals(ASSIGN)||nextCommand().equals(ASSIGN)) { //assign or number (we don't check number)
					if(variableString==null)
						variableString = new HashSet<String>();
					while(!(string=nextString(false)).equals(LINE_FEED))
						variableString.add(string);
				} else throw new ParseException("literal '=' expected after /Variable String command",tokens.getCurrentPosition());
			else throw new ParseException("literal 'String' expected after /Variable command",tokens.getCurrentPosition());
		else if(token.equalsIgnoreCase("/Marker"))
			if(nextCommand().equals("Characters"))
				if(peekToken().equals(STRING_DELIMITER)||nextCommand().equals(ASSIGN)) {
					markerCharacters = new HashSet<String>();
					while(!(string=nextString(false)).equals(LINE_FEED))
						markerCharacters.add(string);
				} else throw new ParseException("literal '=' expected after /Marker Characters command",tokens.getCurrentPosition());
			else throw new ParseException("literal 'Characters' expected after /Marker command",tokens.getCurrentPosition());
		else if(token.equalsIgnoreCase("/Regexp"))
			if(nextCommand().equals("Type"))
				if(nextCommand().equals(ASSIGN)) regexType = nextWord();
				else throw new ParseException("literal '=' expected after /Regexp Type command",tokens.getCurrentPosition());
			else throw new ParseException("literal 'Type' expected after /Regexp command",tokens.getCurrentPosition());
		else if(token.startsWith("/TG")||token.startsWith("//")) nextLine(); //skip
		else parseL(false);
	}

	/**
	 * parses the /L command explicitely
	 * @throws ParseException
	 */
	private void parseL(boolean next) throws ParseException  {
		if(next) {
			String number = token.substring(2);
			if(!isNumeric(number))
				throw new ParseException("unexpected parameter of command /L, wordfile number expected",tokens.getCurrentPosition());
			this.number = Integer.parseInt(number.trim());
			if(peekToken().startsWith(STRING_DELIMITER))
				this.name = nextString(false);
			this.type = WordfileType.UNSPECIFIED;
		} else token = token.substring(1);
		while((next?nextCommand():token)!=null) {
			     if(token.equals(LINE_FEED)) break;
			else if(token.equalsIgnoreCase("Case")) nocase = false;
			else if(token.equalsIgnoreCase("Nocase")) nocase = true;
			else if(token.equalsIgnoreCase("Quote")) noquote = false;
			else if(token.equalsIgnoreCase("Noquote")) noquote = true;
			else if(token.equalsIgnoreCase("EnableMLS")) enableMLS = true;
			else if(token.equalsIgnoreCase("DisableMLS")) disableMLS = true;
			else if(token.equalsIgnoreCase("EnableSpellasYouType")) enableSpellasYouType = true;
			else if(WordfileType.isWordfileType(token.toUpperCase()))
				this.type = WordfileType.valueOf(token.toUpperCase());
			else if(token.endsWith("_LANG")||token.endsWith("_DEB"))
				continue; //unknown wordfile type
			else if(token.equalsIgnoreCase("Block"))
				if(nextCommand().equals("Comment"))
					if(nextCommand().equals("On"))
						     if(nextCommand().equals(ASSIGN)) blockCommentOn = nextWord();
						else if(token.equals("Alt"))
							if(nextCommand().equals(ASSIGN)) blockCommentOnAlt = nextWord();
							else throw new ParseException("literal '=' expected after /Block Comment On Alt",tokens.getCurrentPosition());
						else throw new ParseException("literal '=' or 'Alt' expected after /Block Comment On",tokens.getCurrentPosition());
					else if(token.equals("Off"))
						     if(nextCommand().equals(ASSIGN)) blockCommentOff = nextWord();
						else if(token.equals("Alt"))
							if(nextCommand().equals(ASSIGN)) blockCommentOffAlt = nextWord();
							else throw new ParseException("literal '=' expected after /Block Comment Off Alt",tokens.getCurrentPosition());
						else throw new ParseException("literal '=' or 'Alt' expected after /Block Comment Off",tokens.getCurrentPosition());
					else throw new ParseException("literal 'On' or 'Off' expected after /Block Comment",tokens.getCurrentPosition());
				else throw new ParseException("literal 'Comment' expected",tokens.getCurrentPosition());
			else if(token.equalsIgnoreCase("Line")||token.equalsIgnoreCase("1Line"))
				if(nextCommand().equals("Comment"))
					     if(nextCommand().equals(ASSIGN)) lineComment = nextWord();
					else if(token.equals("Alt"))
						if(nextCommand().equals(ASSIGN)) lineCommentAlt = nextWord();
						else throw new ParseException("literal '=' expected after /Line Comment Alt",tokens.getCurrentPosition());
					else if(token.equals("Valid"))
						if(nextCommand().equals("Columns"))
							if(nextCommand().equals(ASSIGN)) lineCommentValidColumns = nextWord();
							else throw new ParseException("literal '=' expected after /Line Comment Valid Columns",tokens.getCurrentPosition());
						else throw new ParseException("literal 'Columns' expected after /Line Comment Valid",tokens.getCurrentPosition());
					else if(token.equals("Num"))
						if(nextCommand().equals(ASSIGN)) {
							lineComment = nextWord();
							for(int length=lineComment.length();length>=0;length--)
								if(length!=0) {
									try {
										int lineCommentLength = Integer.parseInt(lineComment.substring(0,length));
										StringBuffer extendedLineComment = new StringBuffer();
										extendedLineComment.append(lineComment.substring(length));
										while(extendedLineComment.length()<lineCommentLength)
											extendedLineComment.append(' ');
										lineComment = extendedLineComment.toString(); 
										break;
									} catch(NumberFormatException e) {}
								} else throw new ParseException("number expected after /Line Comment Num",tokens.getCurrentPosition());
						} else throw new ParseException("literal '=' expected after /Line Comment Num",tokens.getCurrentPosition());
					else if(token.equals("Preceding"))
						if(nextCommand().equals("Chars"))
							if(nextCommand().equals(ASSIGN)) lineCommentPrecedingChars = nextWord();
							else throw new ParseException("literal '=' expected after /Line Preceding Chars",tokens.getCurrentPosition());
						else throw new ParseException("literal 'Chars' expected",tokens.getCurrentPosition());
					else throw new ParseException("literal '=' or 'Alt' or 'Num' or 'Valid' or 'Preceding' expected",tokens.getCurrentPosition());
				else throw new ParseException("literal 'Comment' expected",tokens.getCurrentPosition());
			else if(token.equalsIgnoreCase("Escape"))
				if(nextCommand().equals("Char"))
					if(nextCommand().equals(ASSIGN)) escapeChar = nextWord();
					else throw new ParseException("literal '=' expected after /Escape Chars",tokens.getCurrentPosition());
				else throw new ParseException("literal 'Char' expected",tokens.getCurrentPosition());
			else if(token.equalsIgnoreCase("Valid"))
				if(nextCommand().equals("Columns"))
					if(nextCommand().equals(ASSIGN)) validColumns = nextWord();
					else throw new ParseException("literal '=' expected after /Valid Columns",tokens.getCurrentPosition());
				else throw new ParseException("literal 'Columns' expected",tokens.getCurrentPosition());
			else if(token.equalsIgnoreCase("String"))
				if(nextCommand().equals("Chars"))
					if(nextCommand().equals(ASSIGN)) stringChars = nextWord();
					else throw new ParseException("literal '=' expected after /String Chars",tokens.getCurrentPosition());
				else if(token.equals("Literal"))
					if(nextCommand().equals("Prefix"))
						if(nextCommand().equals(ASSIGN)) stringLiteralPrefix = nextWord();
						else throw new ParseException("literal '=' expected after /String Literal Prefix",tokens.getCurrentPosition());
					else throw new ParseException("literal 'Prefix' expected",tokens.getCurrentPosition());
				else throw new ParseException("literal 'Chars' or 'Literal' expected after /String",tokens.getCurrentPosition());
			else if(token.equalsIgnoreCase("File"))
				if(nextCommand().equals("Names"))
					if(nextCommand().equals(ASSIGN)) {
						fileNames = new LinkedHashSet<String>();
						while(!nextWord().equals(LINE_FEED))
							fileNames.add(token.toUpperCase());
						break;
					} else throw new ParseException("literal '=' expected after /File Names",tokens.getCurrentPosition());
				else if(token.equals("Extensions"))
					if(nextCommand().equals(ASSIGN)) {
						fileExtensions = new LinkedHashSet<String>();
						while(!nextWord().equals(LINE_FEED))
							fileExtensions.add(token.toUpperCase());
						break;
					} else throw new ParseException("literal '=' expected after /File Extensions",tokens.getCurrentPosition());
				else throw new ParseException("literal 'Names' or 'Extensions' expected",tokens.getCurrentPosition());
			else throw new ParseException("unexpected token '"+token+"'",tokens.getCurrentPosition());
			next = true;
		}
	}
	/**
	 * cats the /C command explicitely
	 * @throws ParseException
	 */
	private void parseC() throws ParseException  {
		CodeFormat codeFormat = new CodeFormat();
		String number = token.substring(2);
		if(!isNumeric(number))
			throw new ParseException("unexpected parameter of command /C, code format number expected",tokens.getCurrentPosition());
		codeFormat.number = Integer.parseInt(number);
		if(peekToken().startsWith(STRING_DELIMITER))
			 codeFormat.name = nextString(false);
		else codeFormat.name = new String();
		String word = peekWord();
		if(word.matches("[A-Z_]+"))
			 codeFormat.type = nextWord();
		else codeFormat.type = Long.toString(Math.abs(new Random().nextLong()),36).toUpperCase();
		while(nextCommand()!=null)
			     if(token.equals(LINE_FEED)) break;
			else if(token.equals("Colors"))
				if(nextCommand().equals(ASSIGN)) {
					String colors = nextWord();
					if(!isNumeric(colors))
						throw new ParseException("Colors needs to be numeric",tokens.getCurrentPosition());
					codeFormat.colors = convertColor(colors);
				} else if(token.equals("Back"))
					if(nextCommand().equals(ASSIGN)) {
						String colorsBack = nextWord();
						if(!isNumeric(colorsBack))
							throw new ParseException("ColorsBack needs to be numeric",tokens.getCurrentPosition());
						codeFormat.colorsBack = convertColor(colorsBack);
					} else throw new ParseException("literal '=' expected",tokens.getCurrentPosition());
				else if(token.equals("Auto"))
					if(nextCommand().equals("Back"))
						if(nextCommand().equals(ASSIGN)) {
							String colorsAutoBack = nextWord();
							if(!isNumeric(colorsAutoBack))
								throw new ParseException("ColorsAutoBack needs to be numeric",tokens.getCurrentPosition());
							codeFormat.colorsAutoBack = convertBoolean(colorsAutoBack);
						} else throw new ParseException("literal '=' expected",tokens.getCurrentPosition());
					else throw new ParseException("literal 'Back' expected",tokens.getCurrentPosition());
				else throw new ParseException("literal '=' or 'Back' or 'Auto' expected",tokens.getCurrentPosition());
			else if(token.equals("Font"))
				if(nextCommand().equals("Style"))
					if(nextCommand().equals(ASSIGN)) {
						String fontStyle = nextWord();
						if(!isNumeric(fontStyle))
							throw new ParseException("FontStyle needs to be numeric",tokens.getCurrentPosition());
						codeFormat.fontStyle = convertFontStyle(fontStyle);
					} else throw new ParseException("literal '=' expected",tokens.getCurrentPosition());
				else throw new ParseException("literal 'Style' expected",tokens.getCurrentPosition());
			else codeFormat.name += token;
		Set<String> words = codeFormat.keywords; boolean lastLineFeed = true;
		do {
			try {
				String token = peekToken();
				if(token.startsWith("/C")&&token.length()>2&&Character.isDigit(token.charAt(2)))
					break;
			} catch(NoSuchElementException e) { break; } //expected end of string
			if(!nextWord().equals(LINE_FEED)) {
				if(lastLineFeed&&"**".equals(token))
					 words = codeFormat.prefixes;
				else words.add(token); //word is a sub-range of token, so this should work for sure
				lastLineFeed = false;
			} else { words = codeFormat.keywords; lastLineFeed = true; }
		} while(true);
		codeFormats.add(codeFormat);
	}
	
	/**
	 * converts the color definition as it is used in wordfiles to java RGB colors
	 * @param token tje token to be parsed
	 * @return a RGB value
	 * @throws ParseException
	 */
	private RGB convertColor(String token) throws ParseException {
		if(!isNumeric(token))
			throw new ParseException("expected numeric color value",tokens.getCurrentPosition());
		int color = Integer.parseInt(token.trim()),blue = (color & 0xFF0000)>>16,green = (color & 0x00FF00)>>8,red = color & 0x0000FF;
		return new RGB(red,green,blue);
	}
	/**
	 * converts the font style as it is used in wordfiles to java font styles
	 * @param token  the token to be checked
	 * @return a Font format (Font.BOLD, Font.ITALIC, 3 (Underline) or Font.PLAIN)
	 * @throws ParseException
	 */
	private int convertFontStyle(String token) throws ParseException {
		if(!isNumeric(token))
			throw new ParseException("expected numeric color value",tokens.getCurrentPosition());
		switch(Integer.parseInt(token.trim())) {
		case 1: return Font.BOLD;
		case 2: return Font.ITALIC;
		case 3: return 3;  //underline
		default: return Font.PLAIN; }
	}
	
	/**
	 * converts a boolean value as it is used in wordfiles to java booleans
	 * @param token the bool value to check
	 * @return the boolish value
	 */
	private boolean convertBoolean(String token) { return "1".equals(token); }

	/**
	 * nextToken() returns the next token of the tokenizer, as follows:
	 *   /C2"Name Me" ATTRIBUTE_VALUE is separated into:
	 *     /C2, ", Name, Me, ", ATTRIBUTE_VALUE
	 * line feeds, escape and string delimiters are returned as single tokens
	 * @return
	 */
	private String peekToken() { return tokens.peekToken(CARRAGE_RETURN+WORD_DELIMITER,STRING_DELIMITER+ESCAPE_DELIMITER+LINE_FEED); }
	private String nextToken() { return token=tokens.nextToken(CARRAGE_RETURN+WORD_DELIMITER,STRING_DELIMITER+ESCAPE_DELIMITER+LINE_FEED); }

	/**
	 * nextWord() returns the next word of the tokenizer, as follows:
	 *   /C2"Name Me" ATTRIBUTE_VALUE is separated into:
	 *     /C2"Name, Me", ATTRIBUTE_VALUE
	 * line feeds are returned a single tokens
	 * @return
	 */
	private String peekWord() { return tokens.peekToken(CARRAGE_RETURN+WORD_DELIMITER,LINE_FEED); }
	private String nextWord() { return token=tokens.nextToken(CARRAGE_RETURN+WORD_DELIMITER,LINE_FEED); }
	
	/**
	 * nextCommand() returns the next command of the tokenizer, as follows: (= is treated as token)
	 *   /C2"Name Me" ATTRIBUTE_VALUE Foo = Bar Baz= Qux is separated into:
	 *     /C2, Name Me, ATTRIBUTE_VALUE, Foo, =, Bar, Baz, =, Qux
	 * line feeds are returned a single tokens
	 * @return
	 * @throws ParseException 
	 */
	private String nextCommand() throws ParseException {
		if(peekToken().equals(STRING_DELIMITER))
			 return token=nextString(false);
		else return token=tokens.nextToken(CARRAGE_RETURN+WORD_DELIMITER,STRING_DELIMITER+ESCAPE_DELIMITER+LINE_FEED+ASSIGN);
	}
	
	/**
	 * nextString() returns the next string of the tokenizer, as follows:
	 *   /C2"Name" ATTRIBUTE_VALUE is separated into:
	 *     /C2, Name, ATTRIBUTE_VALUE
	 * line feeds are returned a single tokens
	 * @return
	 */
	private String nextString(boolean strict) throws ParseException {
		if(!(token=tokens.nextToken(CARRAGE_RETURN+WORD_DELIMITER,STRING_DELIMITER+LINE_FEED)).equals(STRING_DELIMITER))
			return token; //could be line feed or normal token
		StringBuilder string = new StringBuilder();
		while(true) {
			tokens.setDelimiters(CARRAGE_RETURN,ESCAPE_DELIMITER+STRING_DELIMITER+LINE_FEED);
			if(!tokens.hasMoreTokens()||(token=tokens.nextToken()).equals(LINE_FEED))
				if(strict)
					 throw new ParseException("string literal is not properly closed by a double-quote",tokens.getCurrentPosition());
				else return token=string.toString();
			if(token.equals(STRING_DELIMITER)) return token=string.toString();
			if(token.equals(ESCAPE_DELIMITER)) {
				if(tokens.peekToken().equals(STRING_DELIMITER))
					 string.append(tokens.nextToken());
				else string.append(ESCAPE_DELIMITER);
			} else string.append(token);
		}
	}

	/**
	 * nextLine() returns the next line of the tokenizer, as follows:
	 *   /C2"Name" ATTRIBUTE_VALUE will not be separated.
	 * @return
	 */
	private String nextLine() {
		token = tokens.nextToken(CARRAGE_RETURN,LINE_FEED);
		if(peekToken().equals(LINE_FEED))
			tokens.nextToken(); //read line feed
		return token;
	}

	/**
	 * checks if a string is a number (by parsing it to an int)
	 * @param number the string to be checked
	 * @return true if it can be cast to an integer
	 */
	private static boolean isNumeric(String number) { try { Integer.parseInt(number.trim()); return true; } catch(NumberFormatException e) { return false; } }
	
	/**
	 * reads an whole string to a string
	 * @param stream the stream to be read
	 * @return the content of the stream as a string
	 * @throws java.io.IOException
	 */
    private static String readStream(InputStream stream) throws java.io.IOException{
        StringBuffer data = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        char[] buffer = new char[1024]; int read=0;
        while((read=reader.read(buffer))!=-1){
            data.append(String.valueOf(buffer,0,read));
            buffer = new char[1024];
        }
        reader.close();
        return data.toString();
    }

    /**
     * this class represents certain keywords in the wordfile 
     * @author Kristian Kraljic
     */
    public static class CodeFormat {
    	protected int number;
    	protected String name;
    	protected String type;

    	protected RGB colors,colorsBack;
    	protected boolean colorsAutoBack=true;
    	protected int fontStyle=0;

    	protected Set<String> keywords = new HashSet<String>();
    	protected Set<String> prefixes = new HashSet<String>();
    	
    	private static final RGB[] COLORS_DEFAULT = new RGB[5];
    	private static final RGB COLORS_BACK_DEFAULT = new RGB(255,255,255);
    	static {
    		COLORS_DEFAULT[0] = new RGB(0,0,255);
    		COLORS_DEFAULT[1] = new RGB(255,0,0);
    		COLORS_DEFAULT[2] = new RGB(255,128,0);
    		COLORS_DEFAULT[3] = new RGB(0,128,0);
    		COLORS_DEFAULT[4] = new RGB(128,64,64);
    	}
    	
		/**
		 * @return the number
		 */
		public int getNumber() {
			return number;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return the type
		 */
		public String getType() {
			return type;
		}

		/**
		 * @return the colors
		 */
		public RGB getColors() {
			if(colors!=null)
				 return colors;
			else return COLORS_DEFAULT[(getNumber()-1)%COLORS_DEFAULT.length];
		}

		/**
		 * @return the colorsBack
		 */
		public RGB getColorsBack() {
			if(colorsBack!=null)
				 return colorsBack;
			else return COLORS_BACK_DEFAULT;
		}

		/**
		 * @return the colorsAutoBack
		 */
		public boolean isColorsAutoBack() {
			return colorsAutoBack;
		}

		/**
		 * @return the fontStyle
		 */
		public int getFontStyle() {
			return fontStyle;
		}

		/**
		 * @return the keywords
		 */
		public Set<String> getKeywords() {
			return keywords;
		}

		/**
		 * @return the prefixes
		 */
		public Set<String> getPrefixes() {
			return prefixes;
		}
		
		@Override public int hashCode() { return ("wordfile-codeformat"+this.type+this.number).hashCode(); }
		@Override public boolean equals(Object object) {
			if(!(object instanceof CodeFormat)) return false;
			CodeFormat codeFormat = ((CodeFormat)object);
			return (this.type.equals(codeFormat.type)&&this.number==codeFormat.number);
		}
    }

	/**
	 * @return the wordfiles
	 */
	public static Set<Wordfile> getWordfiles() {
		return wordfiles;
	}

	/**
	 * @return the wordfiles which are in the plugin by default
	 */
	public static Set<Wordfile> getNativeWordfiles() {
		Set<Wordfile> wordfiles = new HashSet<Wordfile>();
		for(Wordfile wordfile:Wordfile.wordfiles)
			if(wordfile.getClass()==Wordfile.class) //do not use instanceof, we do not want to get subclasses of wrodfile!
				wordfiles.add(wordfile);
		return wordfiles;
	}
	
	/**
	 * @return the (first) wordfile with a specific type
	 */
	public static Wordfile getWordfile(WordfileType type) {
		for(Wordfile wordfile:wordfiles)
			if(wordfile.getType().equals(type))
				return wordfile;
		return null;
	}
	/**
	 * @return the wordfiles with a specific type
	 */
	public static Wordfile[] getWordfiles(WordfileType type) {
		List<Wordfile> wordfiles = new ArrayList<Wordfile>();
		for(Wordfile wordfile:Wordfile.wordfiles)
			if(wordfile.getType().equals(type))
				wordfiles.add(wordfile);
		return wordfiles.toArray(new Wordfile[0]);
	}

	/**
	 * @return the wordfile with a specific number or null
	 */
	public static Wordfile getWordfile(int number) {
		for(Wordfile wordfile:wordfiles)
			if(wordfile.getNumber()==number)
				return wordfile;
		return null;
	}
	
	/**
	 * @return the (first) wordfile with a specific file extension
	 */
	public static Wordfile getWordfile(String fileExtension) {
		if(!wordfiles.isEmpty()) {
			fileExtension = fileExtension.toUpperCase();
			for(Wordfile wordfile:wordfiles)
				if(wordfile.getFileExtensions().contains(fileExtension))
					return wordfile;
		}
		return null;
	}
	/**
	 * @return the wordfiles with a specific file extension
	 */
	public static Wordfile[] getWordfiles(String fileExtension) {
		List<Wordfile> wordfiles = new ArrayList<Wordfile>();
		if(!wordfiles.isEmpty()) {
			fileExtension = fileExtension.toUpperCase();
			for(Wordfile wordfile:Wordfile.wordfiles)
				if(wordfile.getFileExtensions().contains(fileExtension))
					wordfiles.add(wordfile);
		}
		return wordfiles.toArray(new Wordfile[0]);
	}
	
	/**
	 * @return the number
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the type
	 */
	public WordfileType getType() {
		return type;
	}

	/**
	 * @return the nocase
	 */
	public boolean isNocase() {
		return nocase;
	}

	/**
	 * @return the noquote
	 */
	public boolean isNoquote() {
		return noquote;
	}

	/**
	 * @return the enableMLS
	 */
	public boolean isEnableMLS() {
		return enableMLS;
	}

	/**
	 * @return the blockCommentOn
	 */
	public String getBlockCommentOn() {
		return blockCommentOn;
	}

	/**
	 * @return the blockCommentOff
	 */
	public String getBlockCommentOff() {
		return blockCommentOff;
	}

	/**
	 * @return the lineComment
	 */
	public String getLineComment() {
		return lineComment;
	}

	/**
	 * @return the lineCommentAlt
	 */
	public String getLineCommentAlt() {
		return lineCommentAlt;
	}

	/**
	 * @return the escapeChar
	 */
	public String getEscapeChar() {
		return escapeChar;
	}

	/**
	 * @return the stringChars
	 */
	public String getStringChars() {
		if(stringChars!=null)
			return stringChars;
		return "\"";
	}

	/**
	 * @return the delimiters
	 */
	public String getDelimiters() {
		if(delimiters!=null)
			 return delimiters;
		else return " ";
	}

	/**
	 * @return the fileExtensions
	 */
	public Set<String> getFileExtensions() {
		return fileExtensions;
	}

	/**
	 * @return the identStrings
	 */
	public Set<String> getIdentStrings() {
		return identStrings;
	}

	/**
	 * @return the unidentStrings
	 */
	public Set<String> getUnidentStrings() {
		return unidentStrings;
	}

	/**
	 * @return the openBraceStrings
	 */
	public Set<String> getOpenBraceStrings() {
		return openBraceStrings;
	}

	/**
	 * @return the closeBraceStrings
	 */
	public Set<String> getCloseBraceStrings() {
		return closeBraceStrings;
	}

	/**
	 * @return the openFoldStrings
	 */
	public Set<String> getOpenFoldStrings() {
		return openFoldStrings;
	}

	/**
	 * @return the closeFoldStrings
	 */
	public Set<String> getCloseFoldStrings() {
		return closeFoldStrings;
	}

	/**
	 * @return the functionString
	 */
	public Set<String> getFunctionString() {
		return functionString;
	}

	/**
	 * @return the memberString
	 */
	public Set<String> getMemberString() {
		return memberString;
	}

	/**
	 * @return the variableString
	 */
	public Set<String> getVariableString() {
		return variableString;
	}

	/**
	 * @return the colors
	 */
	public RGB[] getColors() {
		if(colors!=null&&colors.length==5)
			 return colors;
		else return COLORS_DEFAULT;
	}

	/**
	 * @return the colorsBack
	 */
	public RGB[] getColorsBack() {
		if(colorsBack!=null&&colorsBack.length==5)
			 return colorsBack;
		else return COLORS_BACK_DEFAULT;
	}

	/**
	 * @return the colorsAutoBack
	 */
	public boolean[] getColorsAutoBack() {
		if(colorsAutoBack!=null&&colorsAutoBack.length==5)
			 return colorsAutoBack;
		else return COLORS_AUTO_BACK_DEFAULT;
	}

	/**
	 * @return the fontStyle
	 */
	public int[] getFontStyle() {
		if(fontStyle!=null&&fontStyle.length==5)
			 return fontStyle;
		else return FONT_STYLE_DEFAULT;
	}

	/**
	 * @return the codeFormats
	 */
	public Set<CodeFormat> getCodeFormats() {
		return codeFormats;
	}

	/**
	 * calculates a hascode for this wordfile
	 * @return a unique hashCode
	 */
	@Override public int hashCode() { return ("wordfile"+this.type+this.number).hashCode(); }
	/**
	 * checks if two wordfiles equal each other
	 * @return true if they are the same
	 */
	@Override public boolean equals(Object object) {
		if(!(object instanceof Wordfile)) return false;
		Wordfile wordfile = ((Wordfile)object);
		if(this.type==null||wordfile.type==null) return false;
		return (this.type.equals(wordfile.type)&&this.number==wordfile.number);
	}
}
