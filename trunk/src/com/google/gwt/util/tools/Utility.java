package com.google.gwt.util.tools;


/*
 * Copyright 2006 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A smattering of useful functions.
 */
public final class Utility {

	/**
	 * Per thread MD5 instance.
	 */
	private static final ThreadLocal<MessageDigest> perThreadMd5 = new ThreadLocal<MessageDigest>() {
		@Override
		protected MessageDigest initialValue() {
			try {
				return MessageDigest.getInstance("MD5");
			} catch (final NoSuchAlgorithmException e) {
				return null;
			}
		};
	};

	public static char[] HEX_CHARS = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
		'E', 'F' };

	private static String sInstallPath = null;

	/**
	 * Helper that ignores exceptions during close, because what are you going
	 * to do?
	 */
	public static void close(final InputStream is) {
		try {
			if (is != null) {
				is.close();
			}
		} catch (final IOException e) {}
	}

	/**
	 * Helper that ignores exceptions during close, because what are you going
	 * to do?
	 */
	public static void close(final OutputStream os) {
		try {
			if (os != null) {
				os.close();
			}
		} catch (final IOException e) {}
	}

	/**
	 * Helper that ignores exceptions during close, because what are you going
	 * to do?
	 */
	public static void close(final RandomAccessFile f) {
		if (f != null) {
			try {
				f.close();
			} catch (final IOException e) {}
		}
	}

	/**
	 * Helper that ignores exceptions during close, because what are you going
	 * to do?
	 */
	public static void close(final Reader reader) {
		try {
			if (reader != null) {
				reader.close();
			}
		} catch (final IOException e) {}
	}

	/**
	 * Helper that ignores exceptions during close, because what are you going
	 * to do?
	 */
	public static void close(final Writer writer) {
		try {
			if (writer != null) {
				writer.close();
			}
		} catch (final IOException e) {}
	}

	private static void computeInstallationPath() {
		try {
			String override = System.getProperty("gwt.devjar");
			if (override == null) {
				final String partialPath = Utility.class.getName().replace('.', '/').concat(".class");
				final URL url = Utility.class.getClassLoader().getResource(partialPath);
				if (url != null && "jar".equals(url.getProtocol())) {
					final String path = url.toString();
					final String jarPath = path.substring(path.indexOf("file:"), path.lastIndexOf('!'));
					final File devJarFile = new File(URI.create(jarPath));
					if (!devJarFile.isFile()) {
						throw new IOException("Could not find jar file; " + devJarFile.getCanonicalPath()
								+ " does not appear to be a valid file");
					}

					final String dirPath = jarPath.substring(0, jarPath.lastIndexOf('/') + 1);
					final File installDirFile = new File(URI.create(dirPath));
					if (!installDirFile.isDirectory()) {
						throw new IOException("Could not find installation directory; "
								+ installDirFile.getCanonicalPath() + " does not appear to be a valid directory");
					}

					sInstallPath = installDirFile.getCanonicalPath().replace(File.separatorChar, '/');
				} else {
					throw new IOException("Cannot determine installation directory; apparently not running from a jar");
				}
			} else {
				override = override.replace('\\', '/');
				final int pos = override.lastIndexOf('/');
				if (pos < 0) {
					sInstallPath = "";
				} else {
					sInstallPath = override.substring(0, pos);
				}
			}
		} catch (final IOException e) {
			throw new RuntimeException("Installation problem detected, please reinstall GWT", e);
		}
	}

	/**
	 * @param parent
	 *            Parent directory
	 * @param fileName
	 *            New file name
	 * @param overwrite
	 *            Is overwriting an existing file allowed?
	 * @return Handle to the file
	 * @throws IOException
	 *             If the file cannot be created, or if the file already existed
	 *             and overwrite was false.
	 */
	public static File createNormalFile(final File parent, final String fileName, final boolean overwrite, final boolean ignore)
	throws IOException {
		final File file = new File(parent, fileName);
		if (file.createNewFile()) {
			System.out.println("Created file " + file);
			return file;
		}

		if (!file.exists() || file.isDirectory()) {
			throw new IOException(file.getPath() + " : could not create normal file.");
		}

		if (ignore) {
			System.out.println(file + " already exists; skipping");
			return null;
		}

		if (!overwrite) {
			throw new IOException(file.getPath()
					+ " : already exists; please remove it or use the -overwrite or -ignore option.");
		}

		System.out.println("Overwriting existing file " + file);
		return file;
	}

	/**
	 * @param parent
	 *            Parent directory of the requested directory.
	 * @param dirName
	 *            Requested name for the directory.
	 * @param create
	 *            Create the directory if it does not already exist?
	 * @return A {@link File} representing a directory that now exists.
	 * @throws IOException
	 *             If the directory is not found and/or cannot be created.
	 */
	public static File getDirectory(final File parent, final String dirName, final boolean create) throws IOException {
		final File dir = new File(parent, dirName);
		final boolean alreadyExisted = dir.exists();

		if (create) {
			// No need to check mkdirs result because we check for dir.exists()
			dir.mkdirs();
		}

		if (!dir.exists() || !dir.isDirectory()) {
			if (create) {
				throw new IOException(dir.getPath() + " : could not create directory.");
			} else {
				throw new IOException(dir.getPath() + " : could not find directory.");
			}
		}

		if (create && !alreadyExisted) {
			System.out.println("Created directory " + dir);
		}

		return dir;
	}

	/**
	 * @param dirPath
	 *            Requested path for the directory.
	 * @param create
	 *            Create the directory if it does not already exist?
	 * @return A {@link File} representing a directory that now exists.
	 * @throws IOException
	 *             If the directory is not found and/or cannot be created.
	 */
	public static File getDirectory(final String dirPath, final boolean create) throws IOException {
		return getDirectory(null, dirPath, create);
	}

	/**
	 * Gets the contents of a file from the class path as a String. Note: this
	 * method is only guaranteed to work for resources in the same class loader
	 * that contains this {@link Utility} class.
	 * 
	 * @param partialPath
	 *            the partial path to the resource on the class path
	 * @return the contents of the file
	 * @throws IOException
	 *             if the file could not be found or an error occurred while
	 *             reading it
	 */
	public static String getFileFromClassPath(final String partialPath) throws IOException {
		final InputStream in = Utility.class.getClassLoader().getResourceAsStream(partialPath);
		try {
			if (in == null) {
				throw new FileNotFoundException(partialPath);
			}
			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			streamOut(in, os, 1024);
			return new String(os.toByteArray(), "UTF-8");
		} finally {
			close(in);
		}
	}

	public static String getInstallPath() {
		if (sInstallPath == null) {
			computeInstallationPath();
		}
		return sInstallPath;
	}

	/**
	 * Generate MD5 digest.
	 * 
	 * @param input
	 *            input data to be hashed.
	 * @return MD5 digest.
	 */
	public static byte[] getMd5Digest(final byte[] input) {
		final MessageDigest md5 = perThreadMd5.get();
		md5.reset();
		md5.update(input);
		return md5.digest();
	}

	/**
	 * A 4-digit hex result.
	 */
	public static void hex4(final char c, final StringBuffer sb) {
		sb.append(HEX_CHARS[(c & 0xF000) >> 12]);
		sb.append(HEX_CHARS[(c & 0x0F00) >> 8]);
		sb.append(HEX_CHARS[(c & 0x00F0) >> 4]);
		sb.append(HEX_CHARS[c & 0x000F]);
	}

	/**
	 * Creates a randomly-named temporary directory.
	 * 
	 * @param baseDir
	 *            base directory to contain the new directory. May be
	 *            {@code null}, in which case the directory given by the
	 *            {@code java.io.tmpdir} system property will be used.
	 * @param prefix
	 *            the initial characters of the new directory name
	 * @return a newly-created temporary directory; the caller must delete this
	 *         directory (either when done or on VM exit)
	 */
	public static File makeTemporaryDirectory(File baseDir, final String prefix) throws IOException {
		if (baseDir == null) {
			baseDir = new File(System.getProperty("java.io.tmpdir"));
		}
		// No need to check the result of this mkdirs call because
		// we will detect the subsequent failure
		baseDir.mkdirs();

		// Try this a few times due to non-atomic delete+mkdir operations.
		for (int tries = 0; tries < 3; ++tries) {
			final File result = File.createTempFile(prefix, null, baseDir);
			if (!result.delete()) {
				throw new IOException("Couldn't delete temporary file " + result.getAbsolutePath()
						+ " to replace with a directory.");
			}
			if (result.mkdirs()) {
				// Success.
				return result;
			}
		}
		throw new IOException("Couldn't create temporary directory after 3 tries in " + baseDir.getAbsolutePath());
	}

	public static void streamOut(final File file, final OutputStream out, final int bufferSize) throws IOException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			streamOut(fis, out, bufferSize);
		} finally {
			com.google.gwt.util.tools.Utility.close(fis);
		}
	}

	public static void streamOut(final InputStream in, final OutputStream out, final int bufferSize) throws IOException {
		assert (bufferSize >= 0);

		final byte[] buffer = new byte[bufferSize];
		int bytesRead = 0;
		while (true) {
			bytesRead = in.read(buffer);
			if (bytesRead >= 0) {
				// Copy the bytes out.
				out.write(buffer, 0, bytesRead);
			} else {
				// End of input stream.
				return;
			}
		}
	}

	/**
	 * Returns a string representation of the byte array as a series of
	 * hexadecimal characters.
	 * 
	 * @param bytes
	 *            byte array to convert
	 * @return a string representation of the byte array as a series of
	 *         hexadecimal characters
	 */
	public static String toHexString(final byte[] bytes) {
		final char[] hexString = new char[2 * bytes.length];
		int j = 0;
		for (final byte b : bytes) {
			hexString[j++] = HEX_CHARS[(b & 0xF0) >> 4];
			hexString[j++] = HEX_CHARS[b & 0x0F];
		}

		return new String(hexString);
	}

	public static void writeTemplateFile(final File file, final String contents, final Map<String, String> replacements)
	throws IOException {

		String replacedContents = contents;
		final Set<Entry<String, String>> entries = replacements.entrySet();
		for (final Entry<String, String> entry : entries) {
			final String replaceThis = entry.getKey();
			String withThis = entry.getValue();
			withThis = withThis.replaceAll("\\\\", "\\\\\\\\");
			withThis = withThis.replaceAll("\\$", "\\\\\\$");
			replacedContents = replacedContents.replaceAll(replaceThis, withThis);
		}

		final PrintWriter pw = new PrintWriter(file);
		final LineNumberReader lnr = new LineNumberReader(new StringReader(replacedContents));
		for (String line = lnr.readLine(); line != null; line = lnr.readLine()) {
			pw.println(line);
		}
		close(pw);
	}

}
