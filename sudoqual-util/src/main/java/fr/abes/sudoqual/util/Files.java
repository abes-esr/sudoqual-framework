package fr.abes.sudoqual.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Clément Sipieter (INRIA) {@literal <clement@6pi.fr>}
 *
 */
public final class Files {

	private Files() {

	}

	/**
	 * Writes data in a file located by the given pathFile with the specified encoding.
	 * @param pathFile
	 * @param data
	 * @param encoding
	 * @throws IOException
	 */
	public static void writeFile(String pathFile, String data, Charset encoding) throws IOException {
		writeFile(new File(pathFile), data, encoding);
	}
	
	/**
	 * Writes data in the specified file with the specified encoding.
	 * @param file
	 * @param data
	 * @param encoding
	 * @throws IOException
	 */
	public static void writeFile(File file, String data, Charset encoding) throws IOException {

		try (OutputStream os = new FileOutputStream(file);
		     Writer w = new OutputStreamWriter(os, encoding);
		     Writer writer = new BufferedWriter(w)) {
			writer.write(data);
		}
	}

	/**
	 * Reads a file and returns a String representing the file.
	 * 
	 * @param path
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	public static String readFile(String pathFile, Charset encoding) throws IOException {
		InputStream is = new FileInputStream(pathFile);
		return readInputStream(is, encoding);
	}

	/**
	 * Reads a file and returns a String representing the file
	 * 
	 * @param path
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	public static String readGZIPFile(String pathFile, Charset encoding) throws IOException {
		GZIPInputStream is = new GZIPInputStream(new FileInputStream(pathFile));
		return readInputStream(is, encoding);
	}

	/**
	 * Writes a file
	 * 
	 * @param path
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	public static void writeGZIPFile(String pathFile, Charset encoding, String data) throws IOException {
		try (GZIPOutputStream zip = new GZIPOutputStream(new FileOutputStream(new File(pathFile)))) {
			try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(zip, encoding))) {
    			writer.write(data);
    			writer.newLine();
			}
		}
	}

	/**
	 * Reads a file and returns a String representing the file
	 * 
	 * @param path
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	public static String readFile(File file, Charset encoding) throws IOException {
		InputStream is = new FileInputStream(file);
		return readInputStream(is, encoding);
	}

	/**
	 * Reads a file and returns a String representing the file
	 * 
	 * @param path
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	public static String readGZIPFile(File file, Charset encoding) throws IOException {
		GZIPInputStream is = new GZIPInputStream(new FileInputStream(file));
		return readInputStream(is, encoding);
	}

	/**
	 * Reads an InputString and returns a String representing this stream
	 * 
	 * @param path
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	public static String readInputStream(InputStream is, Charset encoding) throws IOException {
		final char[] buffer = new char[1024];
		final StringBuilder out = new StringBuilder();
		try (Reader in = new InputStreamReader(is, encoding)) {
			int rsz;
			do {
				rsz = in.read(buffer, 0, buffer.length);
				if (rsz >= 0)
					out.append(buffer, 0, rsz);
			} while (rsz >= 0);
		}
		is.close();
		return out.toString();
	}

}
