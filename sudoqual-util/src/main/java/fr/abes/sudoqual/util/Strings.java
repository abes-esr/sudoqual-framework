package fr.abes.sudoqual.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * String Utils static methods.
 * 
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public final class Strings {
	
	private Strings() {}

	/**
	 * Reads the content of the specified {@link Reader} and writes it
	 * in a new {@link StringBuilder}.
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public static StringBuilder toStringBuilder(final Reader reader) throws IOException {
		StringBuilder sb = new StringBuilder();
		if (reader != null) {
			final BufferedReader br = new BufferedReader(reader);
			
			String str = null;
			boolean isFirst = true;
			while((str = br.readLine()) != null) {
				if(isFirst) {
					isFirst = false;
				} else {
					sb.append('\n');
				}
				sb.append(str);
			}
		}
		return sb;
	}
	
	/**
	 * Reads the content of the specified {@link Reader} and return it
	 * as a {@link String}.
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public static String toString(final Reader reader) throws IOException {
		return toStringBuilder(reader).toString();
	}
	
	
	/**
	 * Reads the content of the specified {@link InputStream} and writes it
	 * in a new {@link StringBuilder}.
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public static StringBuilder toStringBuilder(final InputStream inputStream) throws IOException {
		return toStringBuilder(new InputStreamReader(inputStream));
	}
	
	/**
	 * Reads the content of the specified {@link InputStream} and return it
	 * as a {@link String}.
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public static String toString(final InputStream inputStream) throws IOException {
		return toStringBuilder(inputStream).toString();
	}
	
	/**
	 * Converts the specified {@link String} as an {@link InputStream} using the 
	 * specfied {@link Charset}.
	 * @param s
	 * @param charset
	 * @return
	 */
	public static InputStream toInputStream(final String s, final Charset charset) {
		return new ByteArrayInputStream( s.getBytes(charset) );
	}
	
	/**
	 * Counts occurrences of specified char in the specified String. 
	 * @param s the String to look in
	 * @param c the char to look for
	 * @return the number of occurrences of c in s.
	 */
	public static int countChar(String s, char c) {
		int cpt=0;
		if(s != null) {
    		for(char current : s.toCharArray()) {
    			if(current == c) {
    				++cpt;
    			}
    		}
		}
		return cpt;
	}
	
	/**
	 * Repeat a String pattern.
	 * @param s the pattern to repeat
	 * @param nbTime number of time the pattern must be repeat
	 * @return a new String containing nbTime patterns. 
	 */
	public static String repeat(String pattern, int nbTime) {
		StringBuilder sb = new StringBuilder();
		return repeat(sb, pattern, nbTime).toString();
	}
	
	/**
	 * Append several times a pattern to the specified StringBuilder
	 * @param sb the StringBuilder in which append pattern
	 * @param pattern the pattern to append several times
	 * @param nbTime the number of repeat
	 * @return the specified StringBuilder itself.
	 */
	public static StringBuilder repeat(StringBuilder sb, String pattern, int nbTime) {
		for(int i=0; i<nbTime; ++i) {
			sb.append(pattern);
		}
		return sb; 
	}
}
