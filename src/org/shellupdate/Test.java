package org.shellupdate;

import net.jimmc.jshortcut.JShellLink;

public class Test {
	public static void main(String a[]) {
		Test sc = new Test();
		sc.createDesktopShortcut();
	}

	JShellLink link;
	String filePath;

	public Test() {
		try {
			link = new JShellLink();
			filePath = ".project";
		} catch (Exception e) {
		}
	}

	public void createDesktopShortcut() {
		try {
			link.setFolder(JShellLink.getDirectory("desktop"));
			link.setName("Stuff");
			link.setPath(filePath);
			link.setIconLocation("java.exe");
			link.save();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
