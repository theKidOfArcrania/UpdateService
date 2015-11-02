package org.shellupdate.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;

public class ProgressDialog extends JDialog implements ProgressViewer {
	private final JPanel contentPanel = new JPanel();

	private final JLabel lblProgress;
	private final JProgressBar pbrLoading;

	/**
	 * @wbp.parser.constructor
	 */
	public ProgressDialog(Window owner, String title) {
		super(owner);
		setEnabled(false);
		setTitle(title);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 400, 0 };
		gbl_contentPanel.rowHeights = new int[] { 0, 0, 0 };
		gbl_contentPanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		this.setIconImage(null);
		contentPanel.setLayout(gbl_contentPanel);
		lblProgress = new JLabel("Loading...");
		GridBagConstraints gbc_lblProgress = new GridBagConstraints();
		gbc_lblProgress.anchor = GridBagConstraints.WEST;
		gbc_lblProgress.insets = new Insets(20, 10, 5, 10);
		gbc_lblProgress.gridx = 0;
		gbc_lblProgress.gridy = 0;
		contentPanel.add(lblProgress, gbc_lblProgress);

		pbrLoading = new JProgressBar();
		GridBagConstraints gbc_pbrLoading = new GridBagConstraints();
		gbc_pbrLoading.insets = new Insets(0, 10, 50, 10);
		gbc_pbrLoading.fill = GridBagConstraints.BOTH;
		gbc_pbrLoading.gridx = 0;
		gbc_pbrLoading.gridy = 1;
		contentPanel.add(pbrLoading, gbc_pbrLoading);

		pack();
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent evt) {
				setLocationRelativeTo(null);
			}
		});
	}

	@Override
	public void finish() {
		this.dispose();
	}

	@Override
	public int getProgress() {
		return pbrLoading.getValue();
	}

	@Override
	public String getProgressText() {
		return lblProgress.getText();
	}

	@Override
	public void setProgress(int percent) {
		pbrLoading.setValue(Math.min(100, Math.max(0, percent)));
	}

	@Override
	public void setProgressText(String progress) {
		lblProgress.setText(progress);
	}
}
