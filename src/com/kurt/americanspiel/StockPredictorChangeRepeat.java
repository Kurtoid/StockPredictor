package com.kurt.americanspiel;


import com.jimmoores.quandl.DataSetRequest;
import com.jimmoores.quandl.QuandlSession;
import com.jimmoores.quandl.Row;
import com.jimmoores.quandl.TabularResult;
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

import java.util.ArrayList;
import java.util.Iterator;


/**
 * Created by Kurt on 8/30/2015.
 * This program tires to predict stock values using a neural network
 */
public class StockPredictorChangeRepeat {
	static int numToProcess = 360 * 9;


	static int openCloseSets = 10;
	static int expectedSets = 4;
	static int sampleReserve = 3;
	static int otherBack = 5;
	static int offset = sampleReserve + expectedSets;
	static ArrayList<Double> close = new ArrayList<>();
	static ArrayList<Double> open = new ArrayList<>();
	static ArrayList<Double> goldPrices = new ArrayList<>();
	static int nInput = (openCloseSets * 2) + otherBack;
	static double[] openIn = new double[openCloseSets];
	static double[] closeIn = new double[openCloseSets];
	static double[] openOut = new double[expectedSets];
	static double[] closeOut = new double[expectedSets];
	static double[] closeExpected = new double[expectedSets];
	static double[] openExpected = new double[expectedSets];
	static ArrayList<Double> trainingStats = new ArrayList<>();
	static ArrayList<BasicNetwork> networks = new ArrayList<>();
	static int vCode = 4;
	static double[] newValsA = new double[nInput];
	static double[] openInA = new double[openCloseSets];
	static double[] closeInA = new double[openCloseSets];
	static double[] openOutA = new double[expectedSets];
	static double[] closeOutA = new double[expectedSets];

	public static void calculateBasic() {
		try {

			System.out.println("sending out of sample data to jpanel");

			double[][] newVals = new double[1][nInput];
			for (int j = 0; j < openCloseSets; j++) {
				double oldVal = open.get((openCloseSets - j + expectedSets));
				double newVal = (open.get(openCloseSets - j + expectedSets - 1));
				newVals[0][j] = ((newVal - oldVal) / oldVal);
			}
			System.out.println("new vals assigned");
			// populate close values
			for (int j = openCloseSets; j < openCloseSets * 2; j++) {
				double oldVal = close.get((openCloseSets * 2) - (j) + expectedSets);
				double newVal = close.get((((openCloseSets * 2) - (j)) + expectedSets - 1));
				newVals[0][j] = ((newVal - oldVal) / oldVal);
			}
			System.out.println("new close assigned");
			// populate gold values
			for (int j = openCloseSets * 2; j < (openCloseSets * 2) + otherBack; j++) {
				double oldVal = goldPrices.get((openCloseSets * 2) + otherBack + 2 + expectedSets);
				double newVal = goldPrices.get((openCloseSets * 2) + otherBack + 1 + expectedSets);
				newVals[0][j] = ((newVal - oldVal) / oldVal);
			}
			double[][] newExpected = new double[1][expectedSets * 2];
			for (int j = 0; j < expectedSets; j++) {
				double oldVal = open.get(expectedSets - j);
				double newVal = (open.get(expectedSets - j - 1));
				newExpected[0][j] = ((newVal - oldVal) / oldVal);
			}
			System.out.println("expected open");
			// populate expected close

			for (int j = expectedSets; j < expectedSets * 2; j++) {
				double oldVal = close.get((expectedSets * 2) - j);
				double newVal = close.get((expectedSets * 2) - j - 1);
				newExpected[0][j] = ((newVal - oldVal) / oldVal);
			}

			System.out.println("done");
			MLDataSet pair = new BasicMLDataSet(newVals, newExpected);
			ArrayList<MLData> output = new ArrayList<>();
			//MLData output = network.compute(pair.get(0).getInput());
			for (int i = 0; i < vCode; i++) {
				output.add(networks.get(i).compute(pair.get(0).getInput()));
				System.out.println("computing");
			}

			System.out.print("in:\t");
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
				//System.out.print((output.getData(i)) + ", ");
				//closeOut[i - networkExpectedSets] = output.getData(i);
			}

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
		/*
double[] vals = new double[8];
		vals[0] = openNorm.normalize(open.get(2));
		vals[1] = openNorm.normalize(open.get(1));
		vals[2] = openNorm.normalize(open.get(0));
		vals[3] = closeNorm.normalize(close.get(2));
		vals[4] = closeNorm.normalize(close.get(1));
		vals[5] = closeNorm.normalize(close.get(0));
		vals[6] = goldNorm.normalize(goldPrices.get(1));
		vals[7] = goldNorm.normalize(goldPrices.get(0));

		MLData out = network.compute(new BasicMLData(vals));
		System.out.println("open: " + openNorm.deNormalize(out.getData(0)) + ", close: " + closeNorm.deNormalize(out.getData(1)));*/

		/*for (int j = 0; j <= openCloseSets; j += 2) {
			vals[j] = openNorm.normalize(open.get(j));
			vals[j + 1] = closeNorm.normalize(close.get(j));
		}

		vals[(openCloseSets * 2)] = goldNorm.normalize(goldPrices.get(0));
		vals[(openCloseSets * 2) + 1] = goldNorm.normalize(goldPrices.get(1));
		MLData out = network.compute(new BasicMLData(vals));
		for(int i =0; i<networkExpectedSets; i++){
			System.out.println("Open: ");
		}*/

	}

	public static void calcAhead() {

		for (int j = 0; j < openCloseSets; j++) {
			double oldVal = open.get((openCloseSets - j));
			double newVal = (open.get(openCloseSets - j - 1));
			newValsA[j] = ((newVal - oldVal) / oldVal);
		}
		System.out.println("new vals assigned");
		// populate close values
		for (int j = openCloseSets; j < openCloseSets * 2; j++) {
			double oldVal = close.get((openCloseSets * 2) - (j));
			double newVal = close.get((((openCloseSets * 2) - (j)) - 1));
			newValsA[j] = ((newVal - oldVal) / oldVal);
		}
		System.out.println("new close assigned");
		// populate gold values
		for (int j = openCloseSets * 2; j < (openCloseSets * 2) + otherBack; j++) {
			double oldVal = goldPrices.get((openCloseSets * 2) + otherBack + 2);
			double newVal = goldPrices.get((openCloseSets * 2) + otherBack + 1);
			newValsA[j] = ((newVal - oldVal) / oldVal);


		}
		System.out.println("done");
		MLData pair = new BasicMLData(newValsA);
		ArrayList<MLData> output = new ArrayList<>();
		//MLData output = network.compute(pair.get(0).getInput());
		for (int i = 0; i < vCode; i++) {
			output.add(networks.get(i).compute(pair));
			System.out.println("computing");
		}

		System.out.print("in:\t");
		System.out.print("opens: ");
		for (int i = 0; i < openCloseSets; i++) {

			System.out.print(pair.getData(i) + ", ");


			openInA[i] = pair.getData(i);
		}
		System.out.println();

		System.out.print("close: ");
		for (int i = openCloseSets; i < openCloseSets * 2; i++) {
			System.out.print((pair.getData(i)) + ", ");
			closeInA[i - openCloseSets] = pair.getData(i);
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
			openOutA[i] = d / vCode;
		}

		System.out.print("close: ");
		for (int i = expectedSets; i < expectedSets * 2; i++) {
			double d = 0;
			for (int j = 0; j < vCode; j++) {
				d = d + output.get(j).getData(i);
			}
			System.out.print(d / vCode + ", ");
			closeOutA[i - expectedSets] = d / vCode;
			//System.out.print((output.getData(i)) + ", ");
			//closeOut[i - networkExpectedSets] = output.getData(i);
		}

	}

	public static void setup(String symbol, int tCode) {
		vCode = tCode;
		QuandlSession session = QuandlSession.create("KTPnhGwcsM22WuNTawNF");

		System.out.println("session started");
		TabularResult tabularResult = session.getDataSet(
				DataSetRequest.Builder.of("WIKI/" + symbol).build());
		TabularResult gold = session.getDataSet(DataSetRequest.Builder.of("OPEC/ORB").build());
		System.out.println("Data built: stocks:" + tabularResult.size() + ", other: " + gold.size());


		/* TabularResult oilPrices = session.getDataSet(
		         DataSetRequest.Builder.of("OPEC/ORB").build());*/
		System.out.println(tabularResult.toPrettyPrintedString());
		System.out.println("data printed");


		// ArrayList<Double> oil = new ArrayList<>();
		Iterator<Row> iterator = tabularResult.iterator();
		while (iterator.hasNext()) {
			Row row = iterator.next();
			close.add(row.getDouble("Close"));
			open.add(row.getDouble("Open"));
			//  System.out.println(row.getLocalDate("Date").toString());

		}
		iterator = gold.iterator();
		while (iterator.hasNext()) {
			Row row = iterator.next();
			goldPrices.add(row.getDouble("Value"));
		}
		/*NormalizedField openNorm = new NormalizedField(NormalizationAction.Normalize, "open", Collections.max(open), Collections.min(open), 1, -1);
		NormalizedField closeNorm = new NormalizedField(NormalizationAction.Normalize, "close", Collections.max(close), Collections.min(close), 1, -1);
		NormalizedField goldNorm = new NormalizedField(NormalizationAction.Normalize, "gold", Collections.max(goldPrices), Collections.min(goldPrices), 1, -1);*/


		double[][] values = new double[numToProcess - offset][nInput];
		double[][] expected = new double[numToProcess - offset][expectedSets * 2];
		for (int i = 0; i < numToProcess - offset; i++) {
			/*for each of these, j will always equal the second array slot
			manupulate the value for getting from array based on context
			percent change: divide by old value : ((new - old) / old)*100
			in this case, old is current + 1
			*/

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
				System.out.println(i + " " + j);
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
				if (j < value.length - 1) System.out.print(" ");
			}
			System.out.println();
		}
		MLDataSet trainingSet = new BasicMLDataSet(values, expected);

		for (int k = 0; k < vCode; k++) {
			BasicNetwork network = new BasicNetwork();
			network.addLayer(new BasicLayer(null, true, nInput));
			network.addLayer(new BasicLayer(new ActivationTANH(), true, (expectedSets + openCloseSets + otherBack) / 3));
			network.addLayer(new BasicLayer(new ActivationTANH(), true, (expectedSets + openCloseSets + otherBack) / 3));
			network.addLayer(new BasicLayer(new ActivationTANH(), true, (expectedSets + openCloseSets + otherBack) / 3));
			network.addLayer(new BasicLayer(new ActivationTANH(), false, expectedSets * 2));
			network.getStructure().finalizeStructure();
			network.reset();
			trainingSet = new BasicMLDataSet(values, expected);
			ResilientPropagation train = new ResilientPropagation(network, trainingSet);
			train.setThreadCount(4);
			train.setErrorFunction(new ATanErrorFunction());

			int epoch = 1;
			do {
				train.iteration();
				System.out.println(
						"Epoch # " + epoch + "Error:" + train.getError());
				trainingStats.add(train.getError());
				epoch++;
			} while (epoch < 3000);
			train.finishTraining();
			networks.add(network);
		}
		System.out.println(" Neural Network Results : ");

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

			//two newlines
			System.out.println("\n");
		}
		//EncogDirectoryPersistence.saveObject(new File("network.nn"), network);
		System.out.println("done");
	}


}



