package org.shellupdate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public final class JarVerifier {

	// Flag for avoiding unnecessary self-integrity checking.
	private static boolean verifiedSelfIntegrity = false;

	// Provider's signing cert which is used to sign the jar.
	private static X509Certificate providerCert = null;

	public static void main(String[] args) {
		// Check for new updates.

	}

	/**
	 * Extracts ONE certificate chain from the specified certificate array which may contain multiple certificate chains, starting from index 'startIndex'.
	 */
	private static X509Certificate[] getAChain(Certificate[] certs, int startIndex) {
		if (startIndex > certs.length - 1) {
			return null;
		}

		int i;
		// Keep going until the next certificate is not the
		// issuer of this certificate.
		for (i = startIndex; i < certs.length - 1; i++) {
			if (!((X509Certificate) certs[i + 1]).getSubjectDN().equals(((X509Certificate) certs[i]).getIssuerDN())) {
				break;
			}
		}
		// Construct and return the found certificate chain.
		int certChainSize = (i - startIndex) + 1;
		X509Certificate[] ret = new X509Certificate[certChainSize];
		for (int j = 0; j < certChainSize; j++) {
			ret[j] = (X509Certificate) certs[startIndex + j];
		}
		return ret;
	}

	private URL jarURL = null;

	private JarFile jarFile = null;

	JarVerifier(URL jarURL) {
		this.jarURL = jarURL;
	}

	/**
	 * First, retrieve the jar file from the URL passed in constructor. Then, compare it to the expected X509Certificate. If everything went well and the certificates are the same, no exception is thrown.
	 *
	 * @param valueChange
	 */
	public void verifyAndLoad(X509Certificate targetCert, Path temp, ValueChange<Double> valueChange) throws IOException {
		// Sanity checking
		if (targetCert == null) {
			throw new SecurityException("Provider certificate is invalid");
		}

		try {
			if (jarFile == null) {
				jarFile = retrieveJarFileFromURL(jarURL);
			}
		} catch (Exception ex) {
			SecurityException se = new SecurityException();
			se.initCause(ex);
			throw se;
		}

		Vector<JarEntry> entriesVec = new Vector<>();

		// Ensure the jar file is signed.
		Manifest man = jarFile.getManifest();
		if (man == null) {
			throw new SecurityException("The provider is not signed");
		}

		// Ensure all the entries' signatures verify correctly
		byte[] buffer = new byte[8192];
		long totalSize = 1;
		long readSize = 0;

		Enumeration<JarEntry> entries = jarFile.entries();

		while (entries.hasMoreElements()) {
			JarEntry je = entries.nextElement();

			// Add directory
			if (je.isDirectory()) {
				Files.createDirectories(temp.resolve(je.getName()));
				continue;
			}

			long size = je.getSize();
			entriesVec.addElement(je);

			if (size == -1) {
				totalSize += 1000;
			} else {
				totalSize += size;
			}
		}

		Enumeration<JarEntry> e = entriesVec.elements();

		while (e.hasMoreElements()) {
			JarEntry je = e.nextElement();
			Path entryPath = temp.resolve(je.getName());
			if (je.getName().startsWith("META-INF")) {
				continue; // Skip any META files.
			}
			Files.createDirectories(temp.resolve(je.getName()).getParent());
			OutputStream out = Files.newOutputStream(entryPath, StandardOpenOption.CREATE);
			InputStream in = jarFile.getInputStream(je);

			// Read in each jar entry and copy to temp folder.
			// A security exception will be thrown if a
			// signature/digest check fails.
			int lenRead;
			while ((lenRead = in.read(buffer, 0, buffer.length)) != -1) {
				out.write(buffer, 0, lenRead);
				readSize += lenRead;
				valueChange.setValue((double) readSize / totalSize);
			}
			out.close();
			in.close();

			Files.setLastModifiedTime(entryPath, je.getLastModifiedTime());
		}

		// Get the list of signer certificates
		e = entriesVec.elements();

		while (e.hasMoreElements()) {
			JarEntry je = e.nextElement();

			// Every file must be signed except files in META-INF.
			Certificate[] certs = je.getCertificates();
			if ((certs == null) || (certs.length == 0)) {
				if (!je.getName().startsWith("META-INF")) {
					throw new SecurityException("The provider " + "has unsigned " + "class files.");
				}
			} else {
				// Check whether the file is signed by the expected
				// signer. The jar may be signed by multiple signers.
				// See if one of the signers is 'targetCert'.
				int startIndex = 0;
				X509Certificate[] certChain;
				boolean signedAsExpected = false;

				while ((certChain = getAChain(certs, startIndex)) != null) {
					if (certChain[0].equals(targetCert)) {
						// Stop since one trusted signer is found.
						signedAsExpected = true;
						break;
					}
					// Proceed to the next chain.
					startIndex += certChain.length;
				}

				if (!signedAsExpected) {
					throw new SecurityException("The provider " + "is not signed by a " + "trusted signer");
				}
			}
		}
	}

	/**
	 * Retrive the jar file from the specified url.
	 */
	private JarFile retrieveJarFileFromURL(URL url) throws PrivilegedActionException, MalformedURLException {
		JarFile jf = null;

		// Prep the url with the appropriate protocol.
		jarURL = url.getProtocol().equalsIgnoreCase("jar") ? url : new URL("jar:" + url.toString() + "!/");
		// Retrieve the jar file using JarURLConnection
		jf = AccessController.doPrivileged((PrivilegedExceptionAction<JarFile>) () -> {
			JarURLConnection conn = (JarURLConnection) jarURL.openConnection();
			// Always get a fresh copy, so we don't have to
			// worry about the stale file handle when the
			// cached jar is closed by some other application.
			conn.setUseCaches(false);
			return conn.getJarFile();
		});
		return jf;
	}

	// Close the jar file once this object is no longer needed.
	@Override
	protected void finalize() throws Throwable {
		jarFile.close();
	}
}
