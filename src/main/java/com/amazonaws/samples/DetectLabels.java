package com.amazonaws.samples;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.io.File;
import java.io.PrintWriter;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Label;

/**
 * run this class lol
 *
 * basic structure inspired by
 * https://www.javacodegeeks.com/2018/09/amazon-aws-rekognition-tutorial.html
 *
 * for group project on ethics of facial recognition with aden siebel
 *
 * @author xander koo
 *
 */

public class DetectLabels {

	/**
	 *
	 * @param args[0]: filepath of img directory
	 *
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("Please provide at least one argument.");
			return;
		}
		DetectLabels detectLabels = new DetectLabels();
		detectLabels.run(args);
	}

	// Hash set to access all relevant labels
	public static final HashSet<String> LABELS_SET;
	static {
		HashSet<String> set = new HashSet<String>();

		// General labels
		set.add("Face");
		set.add("Person");
		set.add("Head");

		// Gendered labels
		set.add("Female");
		set.add("Woman");
		set.add("Lady");
		set.add("Girl");
		set.add("Man");
		set.add("Boy");

		LABELS_SET = new HashSet<String>(Collections.unmodifiableSet(set));
	}

	// Array list to access all relevant labels
	public static final ArrayList<String> LABELS_LIST;
	static {
		ArrayList<String> list = new ArrayList<String>();

		// General labels
		list.add("Face");
		list.add("Person");
		list.add("Head");

		// Gendered labels
		list.add("Female");
		list.add("Woman");
		list.add("Lady");
		list.add("Girl");
		list.add("Man");
		list.add("Boy");

		LABELS_LIST = list;

	}

	/**
	 * Gets labels and writes outputs to .csv and .txt files
	 *
	 * @param args[1] directory of image files
	 */
	public void run(String[] args) {

		if (args.length == 0) {
			System.err.println("Please specify a directory.");
			return;
		}

		AmazonRekognition rekognition = ClientFactory.createClient();

		try {

			// Create writer for .csv output, and create label headers
			PrintWriter csvWriter = new PrintWriter(new File("output.csv"));
			csvWriter.print("img name,");
			for (String s : LABELS_LIST) {
				csvWriter.print(s + ",");
			}
			csvWriter.println();

			// Writer for text file
			PrintWriter txtWriter = new PrintWriter("output.txt", "UTF-8");

			// Get the image file directory
			File dir = new File(args[0]);
			File[] dirList = dir.listFiles();

			// Sort files
			Arrays.sort(dirList, new Comparator<File>() {
				public int compare(File a, File b) {
					return a.getName().compareTo(b.getName());
				}
			});

			// For every image in the directory...
			for (File f : dirList) {

				String imgPath = f.getPath();

				// Only processes .jpg files
				if (imgPath.endsWith(".jpg")) {

					byte[] bytes;

					try {
						bytes = Files.readAllBytes(Paths.get(imgPath));
					} catch (IOException e) {
						System.err.println("Failed to load image: " + e.getMessage());
						csvWriter.close();
						txtWriter.close();
						return;
					}

					/*
					 * Some wacky AWS stuff idk
					 */
					ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
					DetectLabelsRequest request = new DetectLabelsRequest().withImage(new Image().withBytes(byteBuffer))
							.withMaxLabels(10000) // display at most 10000 labels per image
							.withMinConfidence((float) 0); // display labels above 0.0% confidence
					DetectLabelsResult result = rekognition.detectLabels(request);

					// Gets a list of labels for the image
					List<Label> labels = result.getLabels();

					// Write the image path
					csvWriter.print(imgPath + ",");
					txtWriter.println(imgPath);
					System.out.println(imgPath);

					// Store the relevant labels for the image
					HashMap<String, Float> labelsMap = new HashMap<String, Float>();

					for (Label label : labels) {
						if (LABELS_SET.contains(label.getName())) {
							labelsMap.put(label.getName(), label.getConfidence());
						}
					}

					// Write it to the .csv and .txt files
					for (String l : LABELS_LIST) {
						if (labelsMap.containsKey(l)) {
							csvWriter.print(labelsMap.get(l) + ",");
							txtWriter.println(l + ": " + labelsMap.get(l));
							System.out.println(l + ": " + labelsMap.get(l));
						} else {
							csvWriter.print("0,");
							txtWriter.println(l + ":");
							System.out.println(l + ":");
						}
					}

					csvWriter.println();
					txtWriter.println();
					System.out.println();
				}
			}

			csvWriter.close();
			txtWriter.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
