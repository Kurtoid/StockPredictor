package com.kurt.americanspiel;

import com.jimmoores.quandl.DataSetRequest;
import com.jimmoores.quandl.QuandlSession;
import com.jimmoores.quandl.Row;
import com.jimmoores.quandl.TabularResult;
import org.apache.commons.lang3.ArrayUtils;
import org.encog.engine.network.activation.ActivationTANH;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.error.ATanErrorFunction;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.util.csv.CSVFormat;
import org.encog.util.simple.EncogUtility;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Kurt on 8/30/2015. This program tires to predict stock values
 * using a neural network
 */
public class StockPredictorChangeOOP {

	public StockPredictorChangeOOP(int oCS, int eS, int sR, int oB, int E, int vc) {
		openCloseSets = oCS;
		expectedSets = eS;
		sampleReserve = sR;
		otherBack = oB;
		EPOCHS = E;
		vCode = vc;

		offset = sampleReserve + expectedSets;
		nInput = (openCloseSets * 2) + otherBack;
		openIn = new double[openCloseSets];
		closeIn = new double[openCloseSets];

		//permanant
		openInP = new double[openCloseSets];
		closeInP = new double[openCloseSets];
		networkIn = new double[openCloseSets];


		openOut = new double[expectedSets];
		closeOut = new double[expectedSets];

		expected = new double[1][expectedSets * 2];
		openExpected = new double[expectedSets];
		closeExpected = new double[expectedSets];

		newVals = new double[1][nInput];

		openInA = new double[openCloseSets];
		closeInA = new double[openCloseSets];

		//permanant
		openInAP = new double[openCloseSets];
		closeInAP = new double[openCloseSets];

		networkInA = new double[openCloseSets];

		// set from output when compute done
		openOutA = new double[expectedSets];
		closeOutA = new double[expectedSets];


		newValsA = new double[nInput];


	}

	public double pOpenSeed;
	public double pCloseSeed;

	public double aOpenSeed;
	public double aCloseSeed;


	int numToProcess = 360 * 4;

	public int openCloseSets = 14;
	public int expectedSets = 4;
	public int sampleReserve = 20;
	public int otherBack = 0;
	public int EPOCHS = 3000;
	public int offset;
	ArrayList<Double> close = new ArrayList<>();
	ArrayList<Double> open = new ArrayList<>();
	ArrayList<Double> goldPrices = new ArrayList<>();
	int nInput;

	ArrayList<Double> trainingStats = new ArrayList<>();
	ArrayList<BasicNetwork> networks = new ArrayList<>();
	int vCode = 4;
	//  double[] newValsA = new double[nInput];
	//  double[] openInA = new double[openCloseSets];
	//  double[] closeInA = new double[openCloseSets];
	//  double[] openOutA = new double[expectedSets];
	//  double[] closeOutA = new double[expectedSets];

	double[] openIn;
	double[] closeIn;

	//permanant
	double[] openInP;
	double[] closeInP;

	double[] networkIn;

	// set from output when compute done
	double[] openOut;
	double[] closeOut;

	double[][] expected;

	double[] openExpected;
	double[] closeExpected;

	double[][] newVals;

	public void assignVals() {
		for (int i = 0; i < openCloseSets; i++) {
			newVals[0][i] = openIn[i];
		}

		for (int i = openCloseSets; i < openCloseSets * 2; i++) {
			newVals[0][i] = closeIn[i - openCloseSets];
		}
	}


	public void calculateBasic() {
		try {

			System.out.println("sending out of sample data to jpanel");

			for (int j = 0; j < openCloseSets; j++) {
				double oldVal = open.get((openCloseSets - j + expectedSets));
				double newVal = (open.get(openCloseSets - j + expectedSets - 1));
				//newVals[0][j] = ((newVal - oldVal) / oldVal);
				openIn[j] = ((newVal - oldVal) / oldVal);
				openInP[j] = ((newVal - oldVal) / oldVal);
			}
			// System.out.println("new vals assigned");
			// populate close values
			for (int j = openCloseSets; j < openCloseSets * 2; j++) {
				double oldVal = close.get((openCloseSets * 2) - (j) + expectedSets);
				double newVal = close.get((((openCloseSets * 2) - (j)) + expectedSets - 1));
				//newVals[0][j] = ((newVal - oldVal) / oldVal);
				closeIn[j - openCloseSets] = ((newVal - oldVal) / oldVal);
				closeInP[j - openCloseSets] = ((newVal - oldVal) / oldVal);
				System.out.println("closeinP j is " + (j - openCloseSets) + " and set to " + ((newVal - oldVal) / oldVal));
			}
			// System.out.println("new close assigned");
			// populate gold values
			for (int j = openCloseSets * 2; j < (openCloseSets * 2) + otherBack; j++) {
				double oldVal = goldPrices.get((openCloseSets * 2) + otherBack + 2 + expectedSets);
				double newVal = goldPrices.get((openCloseSets * 2) + otherBack + 1 + expectedSets);
				//newVals[0][j] = ((newVal - oldVal) / oldVal);
			}
			for (int j = 0; j < expectedSets; j++) {
				double oldVal = open.get(expectedSets - j);
				double newVal = (open.get(expectedSets - j - 1));
				expected[0][j] = ((newVal - oldVal) / oldVal);
			}
			System.out.println("expected open");
			// populate expected close

			for (int j = expectedSets; j < expectedSets * 2; j++) {
				double oldVal = close.get((expectedSets * 2) - j);
				double newVal = close.get((expectedSets * 2) - j - 1);
				expected[0][j] = ((newVal - oldVal) / oldVal);
			}

			// all good above

			System.out.println("done");
			MLDataSet pair = new BasicMLDataSet(newVals, expected);

			// MLData output = network.compute(pair.get(0).getInput());
			for (int j = 0; j < expectedSets; j++) {
				double openOutT = 0;
				double closeOutT = 0;
				ArrayList<MLData> output = new ArrayList<>();
				for (int i = 0; i < vCode; i++) {
					assignVals();
					pair = new BasicMLDataSet(newVals, expected);
					output.add(networks.get(0).compute(pair.get(0).getInput()));
					System.out.println("computing");
					openOutT += output.get(i).getData(0);
					closeOutT += output.get(i).getData(1);


				}
				openIn = ArrayUtils.remove(openIn, 0);
				openIn = ArrayUtils.add(openIn, openOutT / vCode);
				openOut[j] = openOutT / vCode;

				closeIn = ArrayUtils.remove(closeIn, 0);
				closeIn = ArrayUtils.add(closeIn, closeOutT / vCode);
				closeOut[j] = closeOutT / vCode;
			}
			// output from closein openin newexpected and outs
			/*System.out.print("in:\t");
			System.out.print("opens: ");
			for (int i = 0; i < openCloseSets; i++) {

				System.out.print(pair.get(0).getInput().getData(i) + ", ");
				openIn[i] = pair.get(0).getInput().getData(i);
			}
			System.out.println();

			System.out.print("close: ");
			for (int i = openCloseSets; i < openCloseSets * 2; i++) {
				System.out.print((pair.get(0).getInput().getData(i)) + ", ");
				closeIn[i - openCloseSets] = pair.get(0).getInput().getData(i);
			}
			System.out.println();

			System.out.print("out:\t");

			System.out.print("open: ");
			for (int i = 0; i < expectedSets; i++) {
				double d = 0;
				for (int j = 0; j < vCode; j++) {
					d += output.get(j).getData(i);
					System.out.println(d + " " + output.get(j).getData(i));
				}
				System.out.print(d / vCode + ", ");
				openOut[i] = d / vCode;
			}

			System.out.print("close: ");
			for (int i = expectedSets; i < expectedSets * 2; i++) {
				double d = 0;
				for (int j = 0; j < vCode; j++) {
					d = d + output.get(j).getData(i);
				}
				System.out.print(d / vCode + ", ");
				closeOut[i - expectedSets] = d / vCode;
				// System.out.print((output.getData(i)) + ", ");
				// closeOut[i - expectedSets] = output.getData(i);
			}
*/
			System.out.println();

			System.out.print("ideal:\t");
			System.out.print("open: ");
			for (int i = 0; i < expectedSets; i++) {
				System.out.print((pair.get(0).getIdeal().getData(i)) + ", ");
				openExpected[i] = pair.get(0).getIdeal().getData(i);
			}

			System.out.print("close: ");
			for (int i = expectedSets; i < expectedSets * 2; i++) {
				System.out.print((pair.get(0).getIdeal().getData(i)) + ", ");
				closeExpected[i - expectedSets] = pair.get(0).getIdeal().getData(i);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}


	}


	double[] openInA;
	double[] closeInA;

	//permanant
	double[] openInAP;
	double[] closeInAP;

	double[] networkInA;

	// set from output when compute done
	double[] openOutA;
	double[] closeOutA;


	double[] newValsA;


	public void assignValsA() {
		for (int i = 0; i < openCloseSets; i++) {
			newValsA[i] = openInA[i];
		}

		for (int i = openCloseSets; i < openCloseSets * 2; i++) {
			newValsA[i] = closeInA[i - openCloseSets];
		}
	}

	public void calcAhead() {


		System.out.println("sending out of sample data to jpanel");
		pOpenSeed = open.get(openCloseSets);
		pCloseSeed = close.get(openCloseSets);

		for (int j = 0; j < openCloseSets; j++) {
			double oldVal = open.get((openCloseSets - j));
			double newVal = (open.get(openCloseSets - j - 1));
			//newVals[0][j] = ((newVal - oldVal) / oldVal);
			openInA[j] = ((newVal - oldVal) / oldVal);
			openInAP[j] = ((newVal - oldVal) / oldVal);
		}
		// System.out.println("new vals assigned");
		// populate close values
		for (int j = openCloseSets; j < openCloseSets * 2; j++) {
			double oldVal = close.get((openCloseSets * 2) - (j));
			double newVal = close.get((((openCloseSets * 2) - (j)) - 1));
			//newVals[0][j] = ((newVal - oldVal) / oldVal);
			closeInA[j - openCloseSets] = ((newVal - oldVal) / oldVal);
			closeInAP[j - openCloseSets] = ((newVal - oldVal) / oldVal);
			System.out.println("closeinP j is " + (j - openCloseSets) + " and set to " + ((newVal - oldVal) / oldVal));
		}
		// System.out.println("new close assigned");
		// populate gold values
		for (int j = openCloseSets * 2; j < (openCloseSets * 2) + otherBack; j++) {
			double oldVal = goldPrices.get((openCloseSets * 2) + otherBack + 2);
			double newVal = goldPrices.get((openCloseSets * 2) + otherBack + 1);
			//newVals[0][j] = ((newVal - oldVal) / oldVal);
		}
		System.out.println("expected open");
		// populate expected close

		// all good above

		System.out.println("done");
		MLData pair = new BasicMLData(newValsA);
		ArrayList<MLData> output = new ArrayList<>();
		// MLData output = network.compute(pair.get(0).getInput());


		for (int j = 0; j < expectedSets; j++) {
			double openOutT = 0;
			double closeOutT = 0;
			output = new ArrayList<>();
			for (int i = 0; i < vCode; i++) {
				//for (int i = 0; i < vCode; i++) {
				assignValsA();
				pair = new BasicMLData(newValsA);
				output.add(networks.get(i).compute(pair));
				System.out.println("computing");

				openOutT += (output.get(i).getData(0));
				closeOutT += output.get(i).getData(1);
				System.out.println("OpenoutT is " + output.get(i).getData(0));
			}

			openInA = ArrayUtils.remove(openInA, 0);
			openInA = ArrayUtils.add(openInA, openOutT / vCode);
			openOutA[j] = openOutT / vCode;
			closeInA = ArrayUtils.remove(closeInA, 0);
			closeInA = ArrayUtils.add(closeInA, closeOutT / vCode);
			closeOutA[j] = closeOutT / vCode;

		}


	}

	public void setup(String symbol, int tCode) {
		vCode = tCode;

		QuandlSession session = QuandlSession.create("KTPnhGwcsM22WuNTawNF");

		System.out.println("session started");
		TabularResult tabularResult = session.getDataSet(DataSetRequest.Builder.of(symbol).build());
		TabularResult gold = session.getDataSet(DataSetRequest.Builder.of("OPEC/ORB").build());
		System.out.println("Data built: stocks:" + tabularResult.size() + ", other: " + gold.size());

	/*
	 * TabularResult oilPrices = session.getDataSet(
	 * DataSetRequest.Builder.of("OPEC/ORB").build());
	 */
		System.out.println(tabularResult.toPrettyPrintedString());
		System.out.println("data printed");

		// ArrayList<Double> oil = new ArrayList<>();
		Iterator<Row> iterator = tabularResult.iterator();
		while (iterator.hasNext()) {
			Row row = iterator.next();
			if (!symbol.equals("BTCE/USDBTC")) {
				close.add(row.getDouble("Close"));
				open.add(row.getDouble("Open"));
			}else{
				close.add(row.getDouble("High"));
				open.add(row.getDouble("Low"));
				numToProcess=tabularResult.size()-40;
			}

			// System.out.println(row.getLocalDate("Date").toString());

		}
		iterator = gold.iterator();
		while (iterator.hasNext()) {
			Row row = iterator.next();
			goldPrices.add(row.getDouble("Value"));
		}


		double[][] values = new double[numToProcess - offset][nInput];
		double[][] expected = new double[numToProcess - offset][expectedSets * 2];
		for (int i = 0; i < numToProcess - offset; i++) {
		/*
		 * for each of these, j will always equal the second array slot
	     * manupulate the value for getting from array based on context
	     * percent change: divide by old value : ((new - old) / old)*100 in
	     * this case, old is current + 1
	     */
			System.out.println(i);
			// populate open values
			for (int j = 0; j < openCloseSets; j++) {
				double oldVal = open.get((openCloseSets - j + offset + i));
				double newVal = (open.get(openCloseSets - j + offset + i - 1));
				values[i][j] = ((newVal - oldVal) / oldVal);
			}

			// populate close values
			for (int j = openCloseSets; j < openCloseSets * 2; j++) {
				double oldVal = close.get((openCloseSets * 2) - (j) + offset);
				double newVal = close.get((((openCloseSets * 2) - (j)) + i + offset - 1));
				values[i][j] = ((newVal - oldVal) / oldVal);
			}

			// populate gold values
			for (int j = openCloseSets * 2; j < (openCloseSets * 2) + otherBack; j++) {
				double oldVal = goldPrices.get(((openCloseSets * 2) + otherBack) - j + i + 1 + offset);
				double newVal = goldPrices.get(((openCloseSets * 2) + otherBack) - j + i + offset);
				values[i][j] = ((newVal - oldVal) / oldVal);
				//System.out.println(i + " " + j);
			}

			// populate expected: open
			for (int j = 0; j < expectedSets; j++) {
				double oldVal = open.get(expectedSets - j + i + sampleReserve);
				double newVal = (open.get(expectedSets - j + i - 1 + sampleReserve));
				expected[i][j] = ((newVal - oldVal) / oldVal);
			}

			// populate expected close
			for (int j = expectedSets; j < expectedSets * 2; j++) {
				double oldVal = close.get((expectedSets * 2) - j + i + sampleReserve);
				double newVal = close.get((expectedSets * 2) - j + i - 1 + sampleReserve);
				expected[i][j] = ((newVal - oldVal) / oldVal);
			}

		}
		for (double[] value : values) {
			for (int j = 0; j < value.length; j++) {
				System.out.print(value[j]);
				/*i*//*f (j < value.length - 1)
					System.out.print(" ");*/
			}
			//System.out.println();
		}
		MLDataSet trainingSet = new BasicMLDataSet(values, expected);

		for (int k = 0; k < vCode; k++) {
			BasicNetwork network = new BasicNetwork();
			network.addLayer(new BasicLayer(null, true, nInput));
			network.addLayer(new BasicLayer(new ActivationTANH(), true, (expectedSets + openCloseSets + otherBack) / 3));
			network.addLayer(new BasicLayer(new ActivationTANH(), true, (expectedSets + openCloseSets + otherBack) / 3));
			network.addLayer(new BasicLayer(new ActivationTANH(), false, expectedSets * 2));
			network.getStructure().finalizeStructure();
			network.reset();
			trainingSet = new BasicMLDataSet(values, expected);
			ResilientPropagation train = new ResilientPropagation(network, trainingSet);
			train.setThreadCount(4);
			train.setErrorFunction(new ATanErrorFunction());
			//EncogDirectoryPersistence.saveObject(new File("trainingSet" + k + ".egb"), train);
			EncogUtility.saveCSV(new File("trainingSet" + k + ".egb"), CSVFormat.DECIMAL_POINT, trainingSet);
			int epoch = 1;
			do {
				train.iteration();
				if (epoch % 10 == 0) {
					System.out.println("Epoch # " + epoch + "Error:" + train.getError());

				}
				trainingStats.add(train.getError());
				epoch++;
			} while (epoch < EPOCHS);
			train.finishTraining();
		/*	EncogDirectoryPersistence.saveObject(new File("network" + k + ".eg"),
					network);*/
			networks.add(network);
		}
		System.out.println("Neural Network Results : ");

		for (MLDataPair pair : trainingSet) {
			final MLData output = networks.get(0).compute(pair.getInput());
			System.out.print("in:\t");
			System.out.print("opens: ");
			for (int i = 0; i < openCloseSets; i++) {
				System.out.print(pair.getInput().getData(i) + ", ");
			}
			System.out.println();

			System.out.print("close: ");
			for (int i = openCloseSets; i < openCloseSets * 2; i++) {
				System.out.print((pair.getInput().getData(i)) + ", ");
			}
			System.out.println();

			System.out.print("out:\t");

			System.out.print("open: ");
			for (int i = 0; i < expectedSets; i++) {
				System.out.print((output.getData(i)) + ", ");
			}

			System.out.print("close: ");
			for (int i = expectedSets; i < expectedSets * 2; i++) {
				System.out.print((output.getData(i)) + ", ");
			}

			System.out.println();

			System.out.print("ideal:\t");
			System.out.print("open: ");
			for (int i = 0; i < expectedSets; i++) {
				System.out.print((pair.getIdeal().getData(i)) + ", ");
			}

			System.out.print("close: ");
			for (int i = expectedSets; i < expectedSets * 2; i++) {
				System.out.print((pair.getIdeal().getData(i)) + ", ");
			}

			// two newlines
			System.out.println("\n");
		}


		System.out.println("done");
	}

}
