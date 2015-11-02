package org.shellupdate;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Version {

	public static Version latestVersion(Version a, Version b) {
		if (a.major == b.major) {
			return new Version(a.major, Math.max(a.minor, b.minor), Math.max(a.build, b.build), a.beta && b.beta);
		} else if (a.major > b.major) {
			return new Version(a.major, a.minor, Math.max(a.build, b.build), a.beta && b.beta);
		} else {
			return new Version(b.major, b.minor, Math.max(a.build, b.build), a.beta && b.beta);
		}
	}

	private static void checkVersion(int version) {
		if (version < 0 || version > 1000) {
			throw new IllegalArgumentException("Illegal version number");
		}
	}

	public int major = 1;
	public int minor;

	public int build = 1;

	public boolean beta;

	public Version() {

	}

	public Version(int major, int minor, int build, boolean beta) {
		checkVersion(major);
		checkVersion(minor);
		checkVersion(build);
		this.major = major;
		this.minor = minor;
		this.build = build;
		this.beta = beta;
	}

	/**
	 * @return the build num of this version
	 */
	public int getBuild() {
		return build;
	}

	/**
	 * @return the major num of this version
	 */
	public int getMajor() {
		return major;
	}

	/**
	 * @return the minor num of this version
	 */
	public int getMinor() {
		return minor;
	}

	/**
	 * @return whether if this is a beta version.
	 */
	public boolean isBeta() {
		return beta;
	}

	public void readVersion(DataInputStream dis) throws IOException {
		this.major = dis.readInt();
		this.minor = dis.readInt();
		this.build = dis.readInt();
		this.beta = dis.readBoolean();
	}

	/**
	 * @param beta whether if this version is a beta version
	 */
	public void setBeta(boolean beta) {
		this.beta = beta;
	}

	/**
	 * @param build the build num to set
	 */
	public void setBuild(int build) {
		this.build = build;
	}

	/**
	 * @param major the major num to set
	 */
	public void setMajor(int major) {
		this.major = major;
	}

	/**
	 * @param minor the minor num to set
	 */
	public void setMinor(int minor) {
		this.minor = minor;
	}

	public void writeVersion(DataOutputStream dos) throws IOException {
		dos.writeInt(major);
		dos.writeInt(minor);
		dos.writeInt(build);
		dos.writeBoolean(beta);
	}

}
