package com.oposproject;

import java.io.Serializable;
import java.util.Random;

import scala.Tuple2;

public class DNA implements Serializable {
	public Genes Properties;

	/**
	 * Used as a cumulative probability; created based on the Energy
	 */
	public double Fitness = 0.0;

	private Random random;

	private void InitialSetup(Random random) {
		this.random = random;

		double energie = this.random.nextDouble(), aggressiveness = this.random.nextDouble();

		while (aggressiveness == energie) {
			aggressiveness = this.random.nextDouble();
		}

		Properties = new Genes(energie, aggressiveness);
	}

	public DNA(Random random) {
		InitialSetup(random);
	}

	public DNA(Genes genes, Random random) {
		Properties = genes;
		this.random = random;
	}

	public DNA(Random random, Tuple2<Double, Double> energyInterval, Tuple2<Double, Double> aggressivenessInterval) {
		this.random = random;
		Properties = new Genes(energyInterval, aggressivenessInterval, this.random);
	}

	public DNA(Random random, double energie, double aggressiveness) {
		if (energie == 0.0f && aggressiveness == 0.0f) {
			InitialSetup(random);
		} else {
			Properties = new Genes(energie, aggressiveness);
			this.random = random;
		}
	}
}