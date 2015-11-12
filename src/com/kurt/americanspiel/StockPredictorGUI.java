package com.kurt.americanspiel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

/**
 * Created by Kurt on 9/26/2015.
 */
public class StockPredictorGUI {
	private JTextField symbolField;
	private JButton calculateButton;
	private JPanel graphPanel;
	private JPanel mainPanel;
	private JPanel trainingGraphPanel;
	private JCheckBox openCheckBox;
	private JCheckBox closeCheckBox;
	private JPanel checkBoxPanel;
	private JPanel inputPanel;
	private JCheckBox showTrainingCheckBox;
	private JPanel avgCheckPanel;
	private JTextField checkTimes;
	private JPanel predictPanel;
	private JPanel networkPanel;
	private JTextField setsExpectedBox;
	private JLabel setsInLabel;
	private JTextField setsInBox;
	private JLabel setsExpectedLabel;
	private JLabel networkEpochsLabel;
	private JTextField netEpochBox;
	private JProgressBar progressBar1;
	private JTextField textField1;
	private JTextField textField2;
	private ChartPanel mainChart;
	private ChartPanel trainingChart;
	private ChartPanel futureChart;
	private XYPlot plot;
	private XYLineAndShapeRenderer renderer;
	StockPredictorChangeOOP predictor;

	public StockPredictorGUI() {
		calculateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
					@Override
					protected Void doInBackground() throws Exception {
						try {
							// does somewhat useful stuff
							String symbol = symbolField.getText();
							String verify = checkTimes.getText();
							int vCode = Integer.parseInt(verify);
							predictor = new StockPredictorChangeOOP(Integer.parseInt(setsInBox.getText()), Integer.parseInt(setsExpectedBox.getText()), 20, 0, Integer.parseInt(netEpochBox.getText()), vCode);

							predictor.setup(symbol, vCode);
							predictor.calculateBasic();
							predictor.calcAhead();
						} catch (Exception e) {

							e.printStackTrace();
						}
						return null;
					}

					@Override
					protected void done() {
						super.done();
						System.out.println("\nworker done");
						XYSeriesCollection dataset = new XYSeriesCollection();
						XYSeries openIn = new XYSeries("Open In");
						for (int i = 0; i < predictor.openCloseSets; i++) {
							openIn.add(new XYDataItem(i, predictor.openInP[i]));
						}
						XYSeries openOut = new XYSeries("Open Out");
						for (int i = predictor.openCloseSets; i < predictor.openCloseSets + predictor.expectedSets; i++) {
							openOut.add(new XYDataItem(i, predictor.openOut[i - predictor.openCloseSets]));
						}
						XYSeries closeIn = new XYSeries("Close In");
						for (int i = 0; i < predictor.openCloseSets; i++) {
							closeIn.add(new XYDataItem(i, predictor.closeInP[i]));
						}
						XYSeries closeOut = new XYSeries("Close Out");
						for (int i = predictor.openCloseSets; i < predictor.openCloseSets + predictor.expectedSets; i++) {
							closeOut.add(new XYDataItem(i, predictor.closeOut[i - predictor.openCloseSets]));
						}

						XYSeries openIdeal = new XYSeries("Open Ideal");
						for (int i = predictor.openCloseSets; i < predictor.openCloseSets + predictor.expectedSets; i++) {
							openIdeal.add(new XYDataItem(i, predictor.openExpected[i - predictor.openCloseSets]));
						}
						XYSeries closeIdeal = new XYSeries("Close Ideal");
						for (int i = predictor.openCloseSets; i < predictor.openCloseSets + predictor.expectedSets; i++) {
							closeIdeal.add(new XYDataItem(i, predictor.closeExpected[i - predictor.openCloseSets]));
						}

						dataset.addSeries(openIn); // 0
						dataset.addSeries(openOut); // 1
						dataset.addSeries(closeIn); // 2
						dataset.addSeries(closeOut); // 3
						dataset.addSeries(openIdeal); // 4
						dataset.addSeries(closeIdeal); // 5


						XYSeriesCollection trainingDataset = new XYSeriesCollection();
						XYSeries trainingVals = new XYSeries("Training Data In");
						for (int i = 0; i < predictor.trainingStats.size(); i++) {
							trainingVals.add(new XYDataItem(i, (double) predictor.trainingStats.get(i)));
						}

						trainingDataset.addSeries(trainingVals);


						trainingChart = new ChartPanel(createChart(trainingDataset, false));
						trainingGraphPanel.add(trainingChart);
						checkBoxPanel.setVisible(true);


						XYSeriesCollection futureDataset = new XYSeriesCollection();
						XYSeries openInF = new XYSeries("Open In");
						for (int i = 0; i < predictor.openCloseSets; i++) {
							openInF.add(new XYDataItem(i, predictor.openInAP[i]));
						}
						XYSeries openOutF = new XYSeries("Open Out");
						for (int i = predictor.openCloseSets; i < predictor.openCloseSets + predictor.expectedSets; i++) {
							openOutF.add(new XYDataItem(i, predictor.openOutA[i - predictor.openCloseSets]));
						}
						XYSeries closeInF = new XYSeries("Close In");
						for (int i = 0; i < predictor.openCloseSets; i++) {
							closeInF.add(new XYDataItem(i, predictor.closeInAP[i]));
						}
						XYSeries closeOutF = new XYSeries("Close Out");
						for (int i = predictor.openCloseSets; i < predictor.openCloseSets + predictor.expectedSets; i++) {
							closeOutF.add(new XYDataItem(i, predictor.closeOutA[i - predictor.openCloseSets]));
						}


						/*XYSeries closeIn = new XYSeries("Close In");
						for (int i = 0; i < predictor.openCloseSets; i++) {
							openIn.add(new XYDataItem(i, predictor.closeIn[i]) );

						}*/
						futureDataset.addSeries(openInF); // 0
						futureDataset.addSeries(openOutF); // 1
						futureDataset.addSeries(closeInF); // 2
						futureDataset.addSeries(closeOutF); // 3
						futureChart = new ChartPanel(createChart(futureDataset, true));
						predictPanel.add(futureChart);
						predictPanel.setVisible(true);
						futureChart.repaint();

						// must be last
						mainChart = new ChartPanel(createChart(dataset, true));
						graphPanel.add(mainChart);
						mainChart.repaint();
					}
				};

				worker.execute();


			}
		});
		symbolField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				calculateButton.doClick();
			}
		});
		openCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (openCheckBox.isSelected()) {
					renderer.setSeriesVisible(0, true);
					renderer.setSeriesVisible(1, true);
					renderer.setSeriesVisible(4, true);
				}
				if (!openCheckBox.isSelected()) {
					renderer.setSeriesVisible(0, false);
					renderer.setSeriesVisible(1, false);
					renderer.setSeriesVisible(4, false);
				}
			}
		});
		closeCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (closeCheckBox.isSelected()) {
					renderer.setSeriesVisible(2, true);
					renderer.setSeriesVisible(3, true);
					renderer.setSeriesVisible(5, true);
				}
				if (!closeCheckBox.isSelected()) {
					renderer.setSeriesVisible(2, false);
					renderer.setSeriesVisible(3, false);
					renderer.setSeriesVisible(5, false);
				}
			}
		});
		showTrainingCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (showTrainingCheckBox.isSelected()) {
					trainingGraphPanel.setVisible(true);
				}
				if (!showTrainingCheckBox.isSelected()) {
					trainingGraphPanel.setVisible(false);
				}
			}
		});
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		JFrame frame = new JFrame("StockPredictorGUI");
		frame.setContentPane(new StockPredictorGUI().mainPanel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(new Dimension(500, 300));
		frame.setVisible(true);


	}


	private JFreeChart createChart(XYDataset dataset, boolean showLabels) {

		// create the chart...
		JFreeChart chart = ChartFactory.createXYLineChart(
				"Stock Data", // chart title
				"Day", // domain axis label
				"Change", // range axis label
				dataset,  // initial series
				PlotOrientation.VERTICAL, // orientation
				true, // include legend
				true, // tooltips?
				false // URLs?
		);

		// set chart background
		chart.setBackgroundPaint(Color.white);

		// set a few custom plot features
		plot = (XYPlot) chart.getPlot();
		//plot.setBackgroundPaint(new Color(0xffffe0));
		//plot.setDomainGridlinesVisible(true);
		//plot.setDomainGridlinePaint(Color.lightGray);
		//plot.setRangeGridlinePaint(Color.lightGray);

		// set the plot's axes to display integers
		//TickUnitSource ticks = NumberAxis.createIntegerTickUnits();
		//NumberAxis domain = (NumberAxis) plot.getDomainAxis();
		//domain.setStandardTickUnits(ticks);
		//NumberAxis range = (NumberAxis) plot.getRangeAxis();
		//range.setStandardTickUnits(ticks);

		// render shapes and lines
		renderer =
				new XYLineAndShapeRenderer(true, showLabels);
		plot.setRenderer(renderer);
		//renderer.setBaseShapesVisible(true);
		//renderer.setBaseShapesFilled(true);

		// set the renderer's stroke
		Stroke stroke = new BasicStroke(
				3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
		renderer.setBaseOutlineStroke(stroke);

		// label the points
		if (showLabels) {
			NumberFormat format = NumberFormat.getNumberInstance();
			format.setMaximumFractionDigits(5);
			XYItemLabelGenerator generator =
					new StandardXYItemLabelGenerator(
							StandardXYItemLabelGenerator.DEFAULT_ITEM_LABEL_FORMAT,
							format, format);
			renderer.setBaseItemLabelGenerator(generator);
			renderer.setBaseItemLabelsVisible(true);
		}

		return chart;
	}


}
