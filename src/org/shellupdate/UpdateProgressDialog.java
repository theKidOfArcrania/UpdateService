package org.shellupdate;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class UpdateProgressDialog extends JFrame {
	private static final long serialVersionUID = 428557334255651290L;

	public static void main(String[] args) {
		UpdateProgressDialog dlg = new UpdateProgressDialog();
		dlg.setVisible(true);
	}

	private final JPanel contentPanel = new JPanel();
	private final JLabel lblKeyStorePass;
	private final JLabel lblUpdaterPass;
	private final JPasswordField txtKeyStorePass;
	private final JPasswordField txtUpdaterPass;
	private final JLabel lblUpdatedVersion;
	private final JButton cmdLoad;
	private final JPanel panel;
	private JTextField txtVersion = null;
	private final JButton cmdVersion;
	private final JLabel lblUpdateName;

	private final JTextField txtUpdateName;

	private File newVersion;

	/**
	 * Create the dialog.
	 */
	public UpdateProgressDialog() {
		setResizable(false);
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 0, 200, 0 };
		gbl_contentPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_contentPanel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
		contentPanel.setLayout(gbl_contentPanel);

		lblKeyStorePass = new JLabel("Key Store Password:");
		GridBagConstraints gbc_lblKeyStorePass = new GridBagConstraints();
		gbc_lblKeyStorePass.anchor = GridBagConstraints.WEST;
		gbc_lblKeyStorePass.insets = new Insets(5, 10, 10, 10);
		gbc_lblKeyStorePass.gridx = 0;
		gbc_lblKeyStorePass.gridy = 0;
		contentPanel.add(lblKeyStorePass, gbc_lblKeyStorePass);

		txtKeyStorePass = new JPasswordField();
		GridBagConstraints gbc_txtKeyStorePass = new GridBagConstraints();
		gbc_txtKeyStorePass.insets = new Insets(5, 0, 10, 0);
		gbc_txtKeyStorePass.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtKeyStorePass.gridx = 1;
		gbc_txtKeyStorePass.gridy = 0;
		contentPanel.add(txtKeyStorePass, gbc_txtKeyStorePass);
		txtKeyStorePass.setColumns(10);

		lblUpdaterPass = new JLabel("Updater Password:");
		GridBagConstraints gbc_lblUpdaterPass = new GridBagConstraints();
		gbc_lblUpdaterPass.anchor = GridBagConstraints.WEST;
		gbc_lblUpdaterPass.insets = new Insets(0, 10, 5, 10);
		gbc_lblUpdaterPass.gridx = 0;
		gbc_lblUpdaterPass.gridy = 1;
		contentPanel.add(lblUpdaterPass, gbc_lblUpdaterPass);

		txtUpdaterPass = new JPasswordField();
		GridBagConstraints gbc_txtUpdaterPass = new GridBagConstraints();
		gbc_txtUpdaterPass.insets = new Insets(0, 0, 5, 0);
		gbc_txtUpdaterPass.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtUpdaterPass.gridx = 1;
		gbc_txtUpdaterPass.gridy = 1;
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
		txtVersion.setEditable(false);
		GridBagConstraints gbc_txtVersion = new GridBagConstraints();
		gbc_txtVersion.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtVersion.gridx = 0;
		gbc_txtVersion.gridy = 0;
		panel.add(txtVersion, gbc_txtVersion);
		txtVersion.setColumns(10);

		cmdVersion = new JButton("...");
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
		GridBagConstraints gbc_txtUpdateName = new GridBagConstraints();
		gbc_txtUpdateName.anchor = GridBagConstraints.NORTH;
		gbc_txtUpdateName.insets = new Insets(0, 0, 5, 0);
		gbc_txtUpdateName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtUpdateName.gridx = 1;
		gbc_txtUpdateName.gridy = 3;
		contentPanel.add(txtUpdateName, gbc_txtUpdateName);
		txtUpdateName.setColumns(10);

		cmdLoad = new JButton("Load");
		cmdLoad.addActionListener(e -> {
			char[] keyStorePass = txtKeyStorePass.getPassword();
			char[] updaterPass = txtUpdaterPass.getPassword();

			if (keyStorePass.length == 0 || updaterPass.length == 0) {
				Arrays.fill(keyStorePass, '0');
				Arrays.fill(updaterPass, '0');
				JOptionPane.showMessageDialog(this, "Passwords must not be blank.", "Updater", JOptionPane.WARNING_MESSAGE);
				return;
			}

			if (newVersion == null) {
				Arrays.fill(keyStorePass, '0');
				Arrays.fill(updaterPass, '0');
				JOptionPane.showMessageDialog(this, "Please select a new program version.", "Updater", JOptionPane.WARNING_MESSAGE);
				return;
			}

			if (txtUpdateName.getText().isEmpty()) {
				Arrays.fill(keyStorePass, '0');
				Arrays.fill(updaterPass, '0');
				JOptionPane.showMessageDialog(this, "Please enter an update name.", "Updater", JOptionPane.WARNING_MESSAGE);
				return;
			}
			ProgressDialog progDlg = new ProgressDialog("Updater", this);
			progDlg.setVisible(true);
			Thread update = new Thread(() -> Updater.addUpdate(progDlg, txtUpdateName.getText(), newVersion, keyStorePass, updaterPass));
			update.setDaemon(true);
			update.start();
		});
		GridBagConstraints gbc_cmdLoad = new GridBagConstraints();
		gbc_cmdLoad.anchor = GridBagConstraints.EAST;
		gbc_cmdLoad.gridx = 1;
		gbc_cmdLoad.gridy = 4;
		contentPanel.add(cmdLoad, gbc_cmdLoad);
		pack();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
}
