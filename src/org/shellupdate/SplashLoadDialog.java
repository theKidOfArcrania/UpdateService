package org.shellupdate;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LinearGradientPaint;
import java.awt.Window;
import java.awt.image.BufferedImage;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

@SuppressWarnings("serial")
public class SplashLoadDialog extends JDialog implements ProgressViewer {

	public static void main(String[] args) {
		try {
			SplashLoadDialog dialog = new SplashLoadDialog(null, ImageHelper.loadImage("org/shellupdate/About.png"));
			dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
			Thread loader = new Thread(() -> {
				for (int i = 0; i <= 100; i++) {
					dialog.setProgress(i);
					try {
						Thread.sleep((int) Math.pow(10, Math.random() * 3));
					} catch (Exception e) {
					}
				}
				dialog.setProgress(1000);
			});
			loader.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private final JPanel contentPanel = new JPanel();
	private final JPanel pnlSplash;
	private final JPanel pnlProgress;
	private final JLabel lblProgressText;
	private long lastUpdate = 0;
	private int prog = 0;

	public SplashLoadDialog(Window owner, BufferedImage splash) {
		super(owner);
		setUndecorated(true);
		setResizable(false);
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(null);
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 500, 0 };
		gbl_contentPanel.rowHeights = new int[] { 300, 10, 0, 0 };
		gbl_contentPanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 1.0, 1.0, 0.0, Double.MIN_VALUE };
		contentPanel.setLayout(gbl_contentPanel);

		pnlSplash = new JPanel() {
			@Override
			public void paint(Graphics g) {
				g.drawImage(splash, 0, 0, this.getWidth(), this.getHeight(), this);
			}
		};
		GridBagConstraints gbc_pnlSplash = new GridBagConstraints();
		gbc_pnlSplash.fill = GridBagConstraints.BOTH;
		gbc_pnlSplash.gridx = 0;
		gbc_pnlSplash.gridy = 0;
		contentPanel.add(pnlSplash, gbc_pnlSplash);
		GridBagLayout gbl_pnlSplash = new GridBagLayout();
		gbl_pnlSplash.columnWidths = new int[] { 0 };
		gbl_pnlSplash.rowHeights = new int[] { 0 };
		gbl_pnlSplash.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_pnlSplash.rowWeights = new double[] { Double.MIN_VALUE };
		pnlSplash.setLayout(gbl_pnlSplash);

		pnlProgress = new JPanel() {
			@Override
			public void paint(Graphics g) {
				Graphics2D g2D = (Graphics2D) g;
				float[] points = { 0f, 1f };
				Color[] red = { new Color(200, 72, 52), new Color(255, 210, 210) };
				Color[] blue = { new Color(52, 102, 200), new Color(210, 210, 255) };

				float barWidth = this.getWidth() / 100f * prog;

				LinearGradientPaint bar = new LinearGradientPaint(0, 0, barWidth + .001f, 0, points, red);
				LinearGradientPaint back = new LinearGradientPaint(barWidth, 0, this.getWidth() + .001f, 0, points, blue);

				g2D.setPaint(back);
				g2D.fillRect(0, 0, getWidth(), getHeight());
				g2D.setPaint(bar);
				g2D.fillRect(0, 0, (int) barWidth, getHeight());

			}
		};
		GridBagConstraints gbc_pnlProgress = new GridBagConstraints();
		gbc_pnlProgress.fill = GridBagConstraints.BOTH;
		gbc_pnlProgress.gridx = 0;
		gbc_pnlProgress.gridy = 1;
		contentPanel.add(pnlProgress, gbc_pnlProgress);
		GridBagLayout gbl_pnlProgress = new GridBagLayout();
		gbl_pnlProgress.columnWidths = new int[] { 0 };
		gbl_pnlProgress.rowHeights = new int[] { 0 };
		gbl_pnlProgress.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_pnlProgress.rowWeights = new double[] { Double.MIN_VALUE };
		pnlProgress.setLayout(gbl_pnlProgress);

		lblProgressText = new JLabel("Loading...");
		lblProgressText.setForeground(new Color(255, 255, 255));
		lblProgressText.setBackground(new Color(32, 178, 170));
		lblProgressText.setOpaque(true);
		GridBagConstraints gbc_lblProgressText = new GridBagConstraints();
		gbc_lblProgressText.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblProgressText.gridx = 0;
		gbc_lblProgressText.gridy = 2;
		contentPanel.add(lblProgressText, gbc_lblProgressText);

		pack();
	}

	@Override
	public void finish() {
		this.dispose();
	}

	@Override
	public int getProgress() {
		return prog;
	}

	@Override
	public String getProgressText() {
		return lblProgressText.getText();
	}

	@Override
	public void setProgress(int percent) {
		long currentUpdate = System.currentTimeMillis();
		prog = Math.min(100, Math.max(0, percent));
		if (currentUpdate - lastUpdate > 10) {
			pnlProgress.repaint();
			lastUpdate = currentUpdate;
		}
	}

	@Override
	public void setProgressText(String progress) {
		lblProgressText.setText(progress);
	}

}
