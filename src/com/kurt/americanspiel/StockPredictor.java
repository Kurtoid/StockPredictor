package com.kurt.americanspiel;


import com.jimmoores.quandl.DataSetRequest;
import com.jimmoores.quandl.QuandlSession;
import com.jimmoores.quandl.Row;
import com.jimmoores.quandl.TabularResult;
import org.encog.Encog;
import org.encog.engine.network.activation.ActivationTANH;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.error.ATanErrorFunction;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.persist.EncogDirectoryPersistence;
import org.encog.util.arrayutil.NormalizationAction;
import org.encog.util.arrayutil.NormalizedField;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;


/**
 * Created by Kurt on 8/30/2015.
 * This program tires to predict stock values using a neural network
 */
public class StockPredictor {
	static int numToProcess = 360 * 9;


	static int openCloseSets = 10;
	static int expectedSets = 3;
	static int sampleReserve = 0;
	static int goldBack = 0;
	static int offset = sampleReserve + expectedSets;
	static int nInput = (openCloseSets * 2) + goldBack;

	public static void main(String[] args) {
		QuandlSession session = QuandlSession.create("KTPnhGwcsM22WuNTawNF");

		System.out.println("session started");
		TabularResult tabularResult = session.getDataSet(
				DataSetRequest.Builder.of("WIKI/AAPL").build());
		TabularResult gold = session.getDataSet(DataSetRequest.Builder.of("WGC/GOLD_DAILY_USD").build());
		System.out.println("Data built");


		/* TabularResult oilPrices = session.getDataSet(
		         DataSetRequest.Builder.of("OPEC/ORB").build());*/
		System.out.println(tabularResult.toPrettyPrintedString());
		System.out.println("data printed");

		ArrayList<Double> close = new ArrayList<>();
		ArrayList<Double> open = new ArrayList<>();
		ArrayList<Double> goldPrices = new ArrayList<>();
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
		NormalizedField openNorm = new NormalizedField(NormalizationAction.Normalize, "open", Collections.max(open), Collections.min(open), 1, -1);
		NormalizedField closeNorm = new NormalizedField(NormalizationAction.Normalize, "close", Collections.max(close), Collections.min(close), 1, -1);
		NormalizedField goldNorm = new NormalizedField(NormalizationAction.Normalize, "gold", Collections.max(goldPrices), Collections.min(goldPrices), 1, -1);


		double[][] values = new double[numToProcess - offset][nInput];
		double[][] expected = new double[numToProcess - offset][expectedSets * 2];
		for (int i = 0; i < numToProcess - offset; i++) {
			/*for each of these, j will always equal the second array slot
			manupulate the value for getting from array based on context*/

			// populate open values
			for (int j = 0; j < openCloseSets; j++) {
				values[i][j] = openNorm.normalize(open.get(openCloseSets - j + offset + i - 1));
			}

			// populate close values
			for (int j = openCloseSets; j < openCloseSets * 2; j++) {
				values[i][j] = closeNorm.normalize(close.get(((openCloseSets * 2) - (j)) + i + offset - 1));
			}

			// populate gold values
			for (int j = openCloseSets * 2; j < (openCloseSets * 2) + goldBack; j++) {
				values[i][j] = goldNorm.normalize(goldPrices.get((openCloseSets * 2) + goldBack + i + 1 + offset));
			}

			// populate expected: open
			for (int j = 0; j < expectedSets; j++) {
				expected[i][j] = openNorm.normalize(open.get(expectedSets - j + i - 1));
			}

			// populate expected close
			for (int j = expectedSets; j < expectedSets * 2; j++) {
				expected[i][j] = closeNorm.normalize(close.get((expectedSets * 2) - j + i - 1));
			}

		}
	/*	for (double[] value : values) {
			for (int j = 0; j < value.length; j++) {
				System.out.print(value[j]);
				if (j < value.length - 1) System.out.print(" ");
			}
			System.out.println();
		}*/

		BasicNetwork network = new BasicNetwork();
		network.addLayer(new BasicLayer(null, true, nInput));
		network.addLayer(new BasicLayer(new ActivationTANH(), true, (expectedSets * 2 + openCloseSets * 2) / 2));
		network.addLayer(new BasicLayer(new ActivationTANH(), true, (expectedSets * 2 + openCloseSets * 2) / 2));

		network.addLayer(new BasicLayer(new ActivationTANH(), false, expectedSets * 2));
		network.getStructure().finalizeStructure();
		network.reset();
		MLDataSet trainingSet = new BasicMLDataSet(values, expected);
		ResilientPropagation train = new ResilientPropagation(network, trainingSet);
		train.setThreadCount(4);

		train.setErrorFunction(new ATanErrorFunction());

		int epoch = 1;
		do {
			train.iteration();
			System.out.println(
					"Epoch # " + epoch + "Error:" + train.getError());
			epoch++;
		} while (epoch < 10000);
		train.finishTraining();
		System.out.println(" Neural Network Results : ");

		for (MLDataPair pair : trainingSet) {
			final MLData output = network.compute(pair.getInput());
			System.out.print("in:\t");
			System.out.print("opens: ");
			for (int i = 0; i < openCloseSets; i++) {
				System.out.print(openNorm.deNormalize(pair.getInput().getData(i)) + ", ");
			}
			System.out.println();

			System.out.print("close: ");
			for (int i = openCloseSets; i < openCloseSets * 2; i++) {
				System.out.print(closeNorm.deNormalize(pair.getInput().getData(i)) + ", ");
			}
			System.out.println();

			System.out.print("out:\t");

			System.out.print("open: ");
			for (int i = 0; i < expectedSets; i++) {
				System.out.print(openNorm.deNormalize(output.getData(i)) + ", ");
			}

			System.out.print("close: ");
			for (int i = expectedSets; i < expectedSets * 2; i++) {
				System.out.print(closeNorm.deNormalize(output.getData(i)) + ", ");
			}

			System.out.println();

			System.out.print("ideal:\t");
			System.out.print("open: ");
			for (int i = 0; i < expectedSets; i++) {
				System.out.print(openNorm.deNormalize(pair.getIdeal().getData(i)) + ", ");
			}

			System.out.print("close: ");
			for (int i = expectedSets; i < expectedSets * 2; i++) {
				System.out.print(closeNorm.deNormalize(pair.getIdeal().getData(i)) + ", ");
			}

			//two newlines
			System.out.println("\n");
		}
		Encog.getInstance().shutdown();
		EncogDirectoryPersistence.saveObject(new File("network.nn"), network);
		System.out.println("done");
		System.out.println("predicting");

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
		for(int i =0; i<expectedSets; i++){
			System.out.println("Open: ");
		}*/

	}


}



