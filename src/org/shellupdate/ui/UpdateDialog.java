package org.shellupdate.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.io.File;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.shellupdate.Updater;
import org.shellupdate.Version;

public class UpdateDialog extends JFrame {
	public class NumberLimitedDocument extends PlainDocument {
		private final int limit;

		NumberLimitedDocument(int limit) {
			super();
			this.limit = limit;
		}

		@Override
		public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
			if (str == null) {
				return;
			}

			if (!str.matches("\\d*")) {
				Toolkit.getDefaultToolkit().beep();
				return;
			}

			if ((getLength() + str.length()) <= limit) {
				super.insertString(offset, str, attr);
			} else {
				Toolkit.getDefaultToolkit().beep();
			}
		}
	}

	private static final long serialVersionUID = 428557334255651290L;

	private final JPanel contentPanel = new JPanel();
	private final JLabel lblKeyStorePass;
	private final JLabel lblUpdaterPass;
	private final JPasswordField txtKeyStorePass;
	private final JPasswordField txtUpdaterPass;
	private final JLabel lblUpdatedVersion;
	private JButton cmdLoad;
	private final JPanel panel;
	private JTextField txtVersion = null;
	private final JButton cmdVersion;

	private final JLabel lblUpdateName;

	private JTextField txtUpdateName;
	private File newVersion;
	private JLabel lblVersionNum;
	private JPanel pnlVersion;
	private JTextField txtVerMajor;
	private JLabel lblVerSep;
	private JTextField txtVerMinor;
	private JLabel lblVerSep2;
	private JTextField txtVerUpdate;

	private JCheckBox chkBeta;
	private boolean verifying = true;

	/**
	 * Create the dialog.
	 */
	public UpdateDialog() {
		DocumentListener changeList = new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent e) {
				validateBlanks();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				validateBlanks();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				validateBlanks();
			}

			private void validateBlanks() {
				if (verifying) {
					char[] keyStorePass = txtKeyStorePass.getPassword();
					char[] updaterPass = txtUpdaterPass.getPassword();

					cmdLoad.setEnabled(keyStorePass.length > 0 && updaterPass.length > 0);
					Arrays.fill(keyStorePass, '0');
					Arrays.fill(updaterPass, '0');
				} else {
					cmdLoad.setEnabled(!(newVersion == null || txtUpdateName.getText().isEmpty() || txtVerMajor.getText().isEmpty()
							|| txtVerMinor.getText().isEmpty() || txtVerUpdate.getText().isEmpty()));
				}

			}

		};
		setResizable(false);
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 0, 200, 0 };
		gbl_contentPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gbl_contentPanel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		contentPanel.setLayout(gbl_contentPanel);

		lblKeyStorePass = new JLabel("Key Store Password:");
		GridBagConstraints gbc_lblKeyStorePass = new GridBagConstraints();
		gbc_lblKeyStorePass.anchor = GridBagConstraints.WEST;
		gbc_lblKeyStorePass.insets = new Insets(5, 10, 10, 10);
		gbc_lblKeyStorePass.gridx = 0;
		gbc_lblKeyStorePass.gridy = 0;
		contentPanel.add(lblKeyStorePass, gbc_lblKeyStorePass);

		txtKeyStorePass = new JPasswordField();
		txtKeyStorePass.addActionListener(e -> {
			if (cmdLoad.isEnabled()) {
				verifyDialog();
			}
		});
		GridBagConstraints gbc_txtKeyStorePass = new GridBagConstraints();
		gbc_txtKeyStorePass.insets = new Insets(5, 0, 10, 0);
		gbc_txtKeyStorePass.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtKeyStorePass.gridx = 1;
		gbc_txtKeyStorePass.gridy = 0;
		contentPanel.add(txtKeyStorePass, gbc_txtKeyStorePass);
		txtKeyStorePass.getDocument().addDocumentListener(changeList);
		txtKeyStorePass.setColumns(10);

		lblUpdaterPass = new JLabel("Updater Password:");
		GridBagConstraints gbc_lblUpdaterPass = new GridBagConstraints();
		gbc_lblUpdaterPass.anchor = GridBagConstraints.WEST;
		gbc_lblUpdaterPass.insets = new Insets(0, 10, 5, 10);
		gbc_lblUpdaterPass.gridx = 0;
		gbc_lblUpdaterPass.gridy = 1;
		contentPanel.add(lblUpdaterPass, gbc_lblUpdaterPass);

		txtUpdaterPass = new JPasswordField();
		txtUpdaterPass.addActionListener(e -> {
			if (cmdLoad.isEnabled()) {
				verifyDialog();
			}
		});
		GridBagConstraints gbc_txtUpdaterPass = new GridBagConstraints();
		gbc_txtUpdaterPass.insets = new Insets(0, 0, 5, 0);
		gbc_txtUpdaterPass.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtUpdaterPass.gridx = 1;
		gbc_txtUpdaterPass.gridy = 1;
		txtUpdaterPass.getDocument().addDocumentListener(changeList);
		contentPanel.add(txtUpdaterPass, gbc_txtUpdaterPass);

		lblUpdatedVersion = new JLabel("New Version:");
		GridBagConstraints gbc_lblUpdatedVersion = new GridBagConstraints();
		gbc_lblUpdatedVersion.anchor = GridBagConstraints.WEST;
		gbc_lblUpdatedVersion.insets = new Insets(0, 10, 5, 5);
		gbc_lblUpdatedVersion.gridx = 0;
		gbc_lblUpdatedVersion.gridy = 2;
		contentPanel.add(lblUpdatedVersion, gbc_lblUpdatedVersion);

		panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 1;
		gbc_panel.gridy = 2;
		contentPanel.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0 };
		gbl_panel.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		txtVersion = new JTextField();
		txtVersion.setEnabled(false);
		txtVersion.setEditable(false);
		GridBagConstraints gbc_txtVersion = new GridBagConstraints();
		gbc_txtVersion.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtVersion.gridx = 0;
		gbc_txtVersion.gridy = 0;
		panel.add(txtVersion, gbc_txtVersion);
		txtVersion.setColumns(10);

		cmdVersion = new JButton("...");
		cmdVersion.setEnabled(false);
		cmdVersion.addActionListener(e -> {
			JDialog dlg = new JDialog(this, true);

			JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.dir")));
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.addChoosableFileFilter(new FileNameExtensionFilter("Jar Upddates (*.jar)", "jar"));
			chooser.setDialogTitle("Load New Version");
			chooser.showOpenDialog(dlg);
			if (chooser.getSelectedFile() == null) {
				return;
			}
			newVersion = chooser.getSelectedFile();
			txtVersion.setText(newVersion.toString());

			if (!txtVersion.isEnabled()) {
				txtUpdateName.setEnabled(true);
				txtUpdateName.requestFocusInWindow();

				txtVersion.setEnabled(true);
				txtVerMajor.setEnabled(true);
				txtVerMinor.setEnabled(true);
				txtVerUpdate.setEnabled(true);
				chkBeta.setEnabled(true);

				Version latest = Updater.getLastestVersion();
				txtVerMajor.setText(Integer.toString(latest.getMajor()));
				txtVerMinor.setText(Integer.toString(latest.getMinor()));
				txtVerUpdate.setText(Integer.toString(latest.getBuild()));
			}
		});

		cmdVersion.setPreferredSize(new Dimension(23, 23));
		cmdVersion.setMinimumSize(new Dimension(23, 23));
		cmdVersion.setMaximumSize(new Dimension(23, 23));
		GridBagConstraints gbc_cmdVersion = new GridBagConstraints();
		gbc_cmdVersion.gridx = 1;
		gbc_cmdVersion.gridy = 0;
		panel.add(cmdVersion, gbc_cmdVersion);

		lblUpdateName = new JLabel("Update Name:");
		GridBagConstraints gbc_lblUpdateName = new GridBagConstraints();
		gbc_lblUpdateName.anchor = GridBagConstraints.WEST;
		gbc_lblUpdateName.insets = new Insets(0, 10, 5, 5);
		gbc_lblUpdateName.gridx = 0;
		gbc_lblUpdateName.gridy = 3;
		contentPanel.add(lblUpdateName, gbc_lblUpdateName);

		txtUpdateName = new JTextField();
		txtUpdateName.getDocument().addDocumentListener(changeList);
		txtUpdateName.setEnabled(false);
		GridBagConstraints gbc_txtUpdateName = new GridBagConstraints();
		gbc_txtUpdateName.anchor = GridBagConstraints.NORTH;
		gbc_txtUpdateName.insets = new Insets(0, 0, 5, 0);
		gbc_txtUpdateName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtUpdateName.gridx = 1;
		gbc_txtUpdateName.gridy = 3;
		contentPanel.add(txtUpdateName, gbc_txtUpdateName);
		txtUpdateName.setColumns(10);

		cmdLoad = new JButton("Verify");
		cmdLoad.setEnabled(false);
		cmdLoad.addActionListener(e -> verifyDialog());

		lblVersionNum = new JLabel("Version Num:");
		GridBagConstraints gbc_lblVersionNum = new GridBagConstraints();
		gbc_lblVersionNum.anchor = GridBagConstraints.WEST;
		gbc_lblVersionNum.insets = new Insets(0, 10, 5, 5);
		gbc_lblVersionNum.gridx = 0;
		gbc_lblVersionNum.gridy = 4;
		contentPanel.add(lblVersionNum, gbc_lblVersionNum);

		pnlVersion = new JPanel();
		GridBagConstraints gbc_pnlVersion = new GridBagConstraints();
		gbc_pnlVersion.insets = new Insets(0, 0, 5, 0);
		gbc_pnlVersion.fill = GridBagConstraints.BOTH;
		gbc_pnlVersion.gridx = 1;
		gbc_pnlVersion.gridy = 4;
		contentPanel.add(pnlVersion, gbc_pnlVersion);
		GridBagLayout gbl_pnlVersion = new GridBagLayout();
		gbl_pnlVersion.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gbl_pnlVersion.rowHeights = new int[] { 0, 0 };
		gbl_pnlVersion.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_pnlVersion.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		pnlVersion.setLayout(gbl_pnlVersion);

		txtVerMajor = new JTextField();
		txtVerMajor.setEnabled(false);
		txtVerMajor.setDocument(new NumberLimitedDocument(3));
		txtVerMajor.getDocument().addDocumentListener(changeList);
		GridBagConstraints gbc_txtVerMajor = new GridBagConstraints();
		gbc_txtVerMajor.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtVerMajor.insets = new Insets(0, 0, 0, 5);
		gbc_txtVerMajor.gridx = 0;
		gbc_txtVerMajor.gridy = 0;
		pnlVersion.add(txtVerMajor, gbc_txtVerMajor);
		txtVerMajor.setColumns(2);

		lblVerSep = new JLabel(".");
		GridBagConstraints gbc_lblVerSep = new GridBagConstraints();
		gbc_lblVerSep.insets = new Insets(0, 0, 0, 5);
		gbc_lblVerSep.anchor = GridBagConstraints.EAST;
		gbc_lblVerSep.gridx = 1;
		gbc_lblVerSep.gridy = 0;
		pnlVersion.add(lblVerSep, gbc_lblVerSep);

		txtVerMinor = new JTextField();
		txtVerMinor.setEnabled(false);
		txtVerMinor.setDocument(new NumberLimitedDocument(3));
		txtVerMinor.getDocument().addDocumentListener(changeList);
		GridBagConstraints gbc_txtVerMinor = new GridBagConstraints();
		gbc_txtVerMinor.insets = new Insets(0, 0, 0, 5);
		gbc_txtVerMinor.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtVerMinor.gridx = 2;
		gbc_txtVerMinor.gridy = 0;
		pnlVersion.add(txtVerMinor, gbc_txtVerMinor);
		txtVerMinor.setColumns(2);

		lblVerSep2 = new JLabel(".");
		GridBagConstraints gbc_lblVerSep2 = new GridBagConstraints();
		gbc_lblVerSep2.anchor = GridBagConstraints.EAST;
		gbc_lblVerSep2.insets = new Insets(0, 0, 0, 5);
		gbc_lblVerSep2.gridx = 3;
		gbc_lblVerSep2.gridy = 0;
		pnlVersion.add(lblVerSep2, gbc_lblVerSep2);

		txtVerUpdate = new JTextField();
		txtVerUpdate.setEnabled(false);
		txtVerUpdate.setDocument(new NumberLimitedDocument(3));
		txtVerUpdate.getDocument().addDocumentListener(changeList);

		GridBagConstraints gbc_txtVerUpdate = new GridBagConstraints();
		gbc_txtVerUpdate.insets = new Insets(0, 0, 0, 5);
		gbc_txtVerUpdate.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtVerUpdate.gridx = 4;
		gbc_txtVerUpdate.gridy = 0;
		pnlVersion.add(txtVerUpdate, gbc_txtVerUpdate);
		txtVerUpdate.setColumns(2);

		chkBeta = new JCheckBox("Beta");
		chkBeta.setEnabled(false);
		GridBagConstraints gbc_chkBeta = new GridBagConstraints();
		gbc_chkBeta.anchor = GridBagConstraints.WEST;
		gbc_chkBeta.gridx = 5;
		gbc_chkBeta.gridy = 0;
		pnlVersion.add(chkBeta, gbc_chkBeta);
		GridBagConstraints gbc_cmdLoad = new GridBagConstraints();
		gbc_cmdLoad.anchor = GridBagConstraints.EAST;
		gbc_cmdLoad.gridx = 1;
		gbc_cmdLoad.gridy = 5;
		contentPanel.add(cmdLoad, gbc_cmdLoad);
		pack();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	private void verifyDialog() {

		char[] keyStorePass = txtKeyStorePass.getPassword();
		char[] updaterPass = txtUpdaterPass.getPassword();
		if (verifying) {
			if (Updater.verifyUpdateID(this, keyStorePass, updaterPass)) {
				verifying = false;
				cmdLoad.setEnabled(false);
				txtKeyStorePass.setEnabled(false);
				txtUpdaterPass.setEnabled(false);
				cmdVersion.setEnabled(true);
				cmdLoad.setText("Add Update");
				JOptionPane.showMessageDialog(this, "Access granted.", "Updater", JOptionPane.INFORMATION_MESSAGE);
			}
		} else {
			ProgressDialog progDlg = new ProgressDialog(this, "Updater");

			// Parse versioning.
			int major = Integer.parseInt(txtVerMajor.getText());
			int minor = Integer.parseInt(txtVerMinor.getText());
			int build = Integer.parseInt(txtVerUpdate.getText());
			boolean beta = chkBeta.isSelected();
			Version version = new Version(major, minor, build, beta);

			Thread update = new Thread(() -> Updater.addUpdate(progDlg, txtUpdateName.getText(), newVersion, version, keyStorePass, updaterPass));
			update.setDaemon(true);
			update.start();
			progDlg.setModal(true);
			progDlg.setLocationRelativeTo(null);
			progDlg.setVisible(true);
		}

	}
}
