package com.oposproject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.Serializable;
import java.lang.Exception;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;

import scala.Tuple2;
import scala.Tuple3;
import scala.Tuple4;

public class GeneticAlgorithm implements Serializable {
	public List<Tuple2<Integer, Tuple2<Double, Double>>> Population = new ArrayList<>();

	private Random random = new Random();

	/**
	 * This is a current number of epoch.
	 */
	public int generationNumber = 0;
	/**
	 * Total number of epochs.
	 */
	public int epochNumber = 5;
	/**
	 * Used for increasing the epochNumber by the original epochNumber value when
	 * running simulation again.
	 */
	public int epochNumberConstant = 5;
	/**
	 * MutationRate is a probability p𝑚 (mutation probability).
	 */
	public double mutationRate = 0.1f;
	/**
	 * 1, 2, 3 or -1 for mix of 3 types of reproduction
	 */
	public int crossoverOption = -1;
	public int iterationNumber = 20;
	public int populationSize = 50;
	public int agentNumber = 20;
	public double k1 = 0.5f;
	public double k2 = 0.5f;
	public double c1 = 15;
	public double c2 = 15;

	public GeneticAlgorithm(Random random) {
		this.random = random;

		for (int i = 0; i < populationSize; ++i) {
			DNA agent = new DNA(random);
			Population.add(new Tuple2<>(i, new Tuple2<>(agent.Properties.Aggressiveness, agent.Properties.Energy)));
		}
	}

	public GeneticAlgorithm(Random random, int S, int A, int crossoverOption, double energy, double aggressiveness,
			int epochNumber, int iterationNumber, double k1, double k2, double c1, double c2) throws Exception {

		this.random = random;
		populationSize = S;
		agentNumber = A;
		this.crossoverOption = crossoverOption;

		if (epochNumber > 0 && iterationNumber > 0) {
			this.epochNumberConstant = this.epochNumber = epochNumber;
			this.iterationNumber = iterationNumber;
		} else {
			throw new Exception("Epoch and/or iteration number must be > 0.");
		}

		if (CheckCoefficientK(k1) && CheckCoefficientK(k2)) {
			if (CheckCoefficientC(c1) && CheckCoefficientC(c2)) {
				this.k1 = k1;
				this.k2 = k2;
				this.c1 = c1;
				this.c2 = c2;
			} else {
				throw new Exception("Param. c1 and/or c2 are not in range.");
			}
		} else {
			throw new Exception("Param. k1 and/or k2 are not in range.");
		}

		for (int i = 0; i < populationSize; ++i) {
			DNA agent = new DNA(random, energy, aggressiveness);
			Population.add(new Tuple2<>(i, new Tuple2<>(agent.Properties.Aggressiveness, agent.Properties.Energy)));
		}
	}

	public GeneticAlgorithm(Random random, int S, int A, int crossoverOption, Tuple2<Double, Double> energyInterval,
			Tuple2<Double, Double> aggressivenessInterval, int epochNumber, int iterationNumber, double k1, double k2,
			double c1, double c2) throws Exception {

		this.random = random;
		populationSize = S;
		agentNumber = A;
		this.crossoverOption = crossoverOption;

		if (epochNumber > 0 && iterationNumber > 0) {
			this.epochNumberConstant = this.epochNumber = epochNumber;
			this.iterationNumber = iterationNumber;
		} else {
			throw new Exception("Epoch and/or iteration number must be > 0.");
		}

		if (CheckCoefficientK(k1) && CheckCoefficientK(k2)) {
			if (CheckCoefficientC(c1) && CheckCoefficientC(c2)) {
				this.k1 = k1;
				this.k2 = k2;
				this.c1 = c1;
				this.c2 = c2;
			} else {
				throw new Exception("Param. c1 and/or c2 are not in range.");
			}
		} else {
			throw new Exception("Param. k1 and/or k2 are not in range.");
		}

		for (int i = 0; i < populationSize; ++i) {
			DNA agent = new DNA(random, energyInterval, aggressivenessInterval);
			Population.add(new Tuple2<>(i, new Tuple2<>(agent.Properties.Aggressiveness, agent.Properties.Energy)));
		}
	}

	public GeneticAlgorithm(Random random, int S, int A, int crossoverOption, double mutationRate, double energy,
			double aggressiveness, int epochNumber, int iterationNumber, double k1, double k2, double c1, double c2)
			throws Exception {
		this(random, S, A, crossoverOption, energy, aggressiveness, epochNumber, iterationNumber, k1, k2, c1, c2);
		SetMutationRate(mutationRate);
	}

	public GeneticAlgorithm(Random random, int S, int A, int crossoverOption, double mutationRate,
			Tuple2<Double, Double> energyInterval, Tuple2<Double, Double> aggressivenessInterval, int epochNumber,
			int iterationNumber, double k1, double k2, double c1, double c2) throws Exception {
		this(random, S, A, crossoverOption, energyInterval, aggressivenessInterval, epochNumber, iterationNumber, k1,
				k2, c1, c2);
		SetMutationRate(mutationRate);
	}

	private Boolean CheckCoefficientK(double k) {
		return k >= 0 ? (k <= 1 ? true : false) : false;
	}

	private Boolean CheckCoefficientC(double c) {
		return c >= 1 ? true : false;
	}

	private Boolean CheckProbability(double probability) {
		return probability >= 0.0 ? (probability <= 1.0 ? true : false) : false;
	}

	private void SetMutationRate(double mutationRate) throws Exception {
		if (CheckProbability(mutationRate)) {
			this.mutationRate = mutationRate;
		} else {
			throw new Exception("Mutation probability is not in range.");
		}
	}

	public Boolean IsSimulationOver() {
		return generationNumber < epochNumber;
	}

	private String DisplayNumbersWithOrdinals(int num) {
		String number = Integer.toString(num);
		if (number.endsWith("11"))
			return number + "th";
		if (number.endsWith("12"))
			return number + "th";
		if (number.endsWith("13"))
			return number + "th";
		if (number.endsWith("1"))
			return number + "st";
		if (number.endsWith("2"))
			return number + "nd";
		if (number.endsWith("3"))
			return number + "rd";
		return number + "th";
	}

	public void GeneticInfo() {
		System.out.println(
				DisplayNumbersWithOrdinals(generationNumber) + ". gen. with population size: " + populationSize);
		System.out.println("Number of agents selected for reproduction: " + agentNumber);
		if (crossoverOption == 2) {
			System.out.println("Mutation Rate: " + mutationRate);
		}
		System.out.println("Simulation is " + (IsSimulationOver() ? "not over." : "over."));
		if (!IsSimulationOver() && !Population.isEmpty()) {
			Population.forEach(System.out::println);
		}
	}

	public void DumpData(List<Tuple2<Integer, Tuple2<Double, Double>>> agentsList) {
		List<Tuple2<Integer, Tuple2<Double, Double>>> population = new ArrayList<>();
		agentsList.forEach(agent -> population.add(new Tuple2<>(agent._1, new Tuple2<>(agent._2._1, agent._2._2))));
		Population = population;
	}

	public void Collision(JavaSparkContext sc) {
//	    JavaRDD<String> input = sc.textFile("hdfs://hadoop:9000/FILENAME");
		JavaPairRDD<Integer, Tuple2<Double, Double>> agentsRdd = sc.parallelize(Population)
				.mapToPair(value -> new Tuple2<>(value._1, new Tuple2<>(value._2._1, value._2._2)));

		while (generationNumber < epochNumber) {
			try {
				agentsRdd = SurvivalOfTheFittest(agentsRdd, k1, k2, c1, c2, sc, 5.25);
				agentsRdd = NewGeneration(agentsRdd, agentNumber, crossoverOption, sc);
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
				return;
			}
		}

		DumpData(agentsRdd.collect());
	}

	/**
	 * Collision function. One epoch execution.
	 */
	public JavaPairRDD<Integer, Tuple2<Double, Double>> SurvivalOfTheFittest(
			JavaPairRDD<Integer, Tuple2<Double, Double>> PopulationRdd, double k1, double k2, double c1, double c2,
			JavaSparkContext sc, double collisionEnergy) throws Exception {
		if ((k1 >= 0.0f && k1 <= 1.0f) && (k2 >= 0.0f && k2 <= 1.0f) && c1 > 0 && c2 > 0) {
			for (int i = 1; i <= iterationNumber; ++i) {
				JavaPairRDD<Integer, Tuple2<Double, Double>> populationCopyRdd = PopulationRdd;
				List<Integer> indexes = new ArrayList<>();

				for (int j = populationSize; j > 0; j -= 2) {
					int agent1Index;
					int agent2Index;

					if (j == 1) {
						// if populationSize isn't an even number, agent1Index will be chosen randomly
						// with possible repetition
						// agent2Index will remain an unique index
						agent1Index = random.nextInt(populationSize);
						agent2Index = random.nextInt(populationSize);

						while (indexes.contains(agent2Index) || agent1Index == agent2Index) {
							agent2Index = random.nextInt(populationSize);
						}
					} else if (!indexes.isEmpty()) {
						do {
							agent1Index = random.nextInt(populationSize);
						} while (indexes.contains(agent1Index));
						indexes.add(agent1Index);

						do {
							agent2Index = random.nextInt(populationSize);
						} while (indexes.contains(agent2Index));
						indexes.add(agent2Index);
					} else { // used for picking the first two agents
						agent1Index = random.nextInt(populationSize);
						do {
							agent2Index = random.nextInt(populationSize);
						} while (agent1Index == agent2Index);

						indexes.add(agent1Index);
						indexes.add(agent2Index);
					}

					JavaPairRDD<Integer, Tuple2<Double, Double>> twoAgentsRdd;

					// index (aggressiveness,energy)
					int agent1Indx = agent1Index, agent2Indx = agent2Index;

					twoAgentsRdd = populationCopyRdd
							.filter(value -> value._1 == agent1Indx ? true : value._1 == agent2Indx ? true : false);

					double totalAggressiveness = twoAgentsRdd.map(x -> x._2._1).reduce((x, y) -> x + y);

					// index (aggressiveness,energySplit)
					// for the two selected agents we don't need the old energy value at the moment
					twoAgentsRdd = twoAgentsRdd
							.mapToPair(value -> new Tuple2<>(value._1,
									new Tuple2<>(value._2._1, collisionEnergy * value._2._1 / totalAggressiveness)))
							// index (aggressiveness,energySplit,scaleFactor)
							.mapToPair(value -> new Tuple2<>(value._1,
									new Tuple3<>(value._2._1, value._2._2,
											1 - (k1 * Sigmoid(c1 * (value._2._1 - k2))
													+ (1 - k1) * Sigmoid(c1 * (value._2._1 - k2))))))
							// index (aggressiveness,energyToAdd) -> energyToAdd is a value that needs to be
							// added to the old energy value
							.mapToPair(value -> new Tuple2<>(value._1,
									new Tuple2<>(value._2._1(), value._2._2() * value._2._3())));

					PopulationRdd = PopulationRdd.mapToPair(x -> new Tuple2<>(new Tuple2<>(x._1, x._2._1), x._2._2))
							.union(twoAgentsRdd.mapToPair(x -> new Tuple2<>(new Tuple2<>(x._1, x._2._1), x._2._2)))
							.reduceByKey((x, y) -> x + y)
							.mapToPair(x -> new Tuple2<>(x._1._1, new Tuple2<>(x._1._2, x._2)));

					List<Tuple2<Integer, Tuple2<Double, Double>>> agentList = PopulationRdd.collect();
					PopulationRdd = sc.parallelize(agentList)
							.mapToPair(value -> new Tuple2<>(value._1, new Tuple2<>(value._2._1, value._2._2)));
				}
			}
		} else {
			throw new Exception("Parametar k/c is not in range.");
		}
		return PopulationRdd;
	}

	private double Sigmoid(double x) {
		return 1.0f / (1 + Math.exp(-x));
	}

	/**
	 * @param numberOfAgents Number of agents, parameter A - number selected for
	 *                       reproduction.
	 */
	public JavaPairRDD<Integer, Tuple2<Double, Double>> NewGeneration(
			JavaPairRDD<Integer, Tuple2<Double, Double>> PopulationRdd, int numberOfAgents, int crossoverOption,
			JavaSparkContext sc) throws Exception {
		if (populationSize <= 0 && numberOfAgents > populationSize) {
			throw new Exception("Error: param A > S");
		}

		// index (aggressiveness,energy,fitness)
		JavaPairRDD<Integer, Tuple3<Double, Double, Double>> populationWithFitnessRdd = Normalization(PopulationRdd,
				sc);
//		populationWithFitnessRdd.collect().forEach(System.out::println); // ~ 3 min.

		if (crossoverOption == -1) {
			crossoverOption = random.nextInt(3) + 1; // range is [1,4) = [1,3]
		}

		List<Integer> smithIndex = new ArrayList<Integer>();

		// adding unique agents from population to smith list (A <= S)
		// adding unique index values to agent index array
		for (int i = 0; i < numberOfAgents; ++i) {
			int index = ChooseParent(populationWithFitnessRdd, random.nextDouble());
			if (smithIndex.isEmpty() == false) {
				if (smithIndex.contains(index)) {
					while (smithIndex.contains(index)) {
						index = ChooseParent(populationWithFitnessRdd, random.nextDouble());
					}
					smithIndex.add(index);
				} else if (!smithIndex.contains(index)) {
					smithIndex.add(index);
				}
			} else {
				smithIndex.add(index);
			}
		}

		// RDD of unique agents, total number numberOfAgents (A param)
		// smith is a ref. to the Matrix movie
		JavaPairRDD<Integer, Tuple2<Double, Double>> smithRdd = populationWithFitnessRdd
				.filter(value -> smithIndex.contains(value._1) ? true : false)
				.mapToPair(value -> new Tuple2<>(value._1, new Tuple2<>(value._2._1(), value._2._2())));

		JavaPairRDD<Integer, Tuple2<Double, Double>> newPopulation = null;

		for (int i = 1, indexValue = 0; i <= populationSize; i += 2) {
			int index = smithIndex.get(random.nextInt(numberOfAgents));

			if ((newPopulation != null && i + 1 <= populationSize) || newPopulation == null) {
				if (crossoverOption == 3) {
					JavaPairRDD<Integer, Tuple2<Double, Double>> parentRdd = smithRdd
							.filter(value -> value._1 == index ? true : false);

					int index2;

					do {
						index2 = smithIndex.get(random.nextInt(numberOfAgents));
					} while (index == index2);

					int index2Final = index2;
					parentRdd = parentRdd.union(smithRdd.filter(value -> value._1 == index2Final ? true : false));

					JavaPairRDD<Integer, Tuple2<Double, Double>> newAgentRdd = CrossoverTwoParents(parentRdd, index,
							index2, indexValue++);

					if (newPopulation == null) {
						newPopulation = newAgentRdd;
					} else {
						newPopulation = newPopulation.union(newAgentRdd);
					}

					int indexValueFinal = indexValue;
					newAgentRdd = newAgentRdd
							.mapToPair(value -> new Tuple2<>(indexValueFinal, new Tuple2<>(value._2._1, value._2._2)));
					newPopulation = newPopulation.union(newAgentRdd);
					indexValue++;
				} else if (crossoverOption == 1) {
					int indexValueFinal = indexValue;

					JavaPairRDD<Integer, Tuple2<Double, Double>> parentRdd = smithRdd
							.filter(value -> value._1 == index ? true : false)
							.mapToPair(value -> new Tuple2<>(indexValueFinal, new Tuple2<>(value._2._1, value._2._2)));

					indexValue++;
					int indexValueFinal2 = indexValue;

					JavaPairRDD<Integer, Tuple2<Double, Double>> newAgentRdd = parentRdd.union(parentRdd.mapToPair(
							value -> new Tuple2<>(indexValueFinal2, new Tuple2<>(value._2._1, value._2._2))));

					indexValue++;
					newAgentRdd = Mutate(newAgentRdd, mutationRate);
					if (newPopulation == null) {
						newPopulation = newAgentRdd;
					} else {
						newPopulation = newPopulation.union(newAgentRdd);
					}
				} else if (crossoverOption == 2) {
					int indexValueFinal = indexValue;

					JavaPairRDD<Integer, Tuple2<Double, Double>> parentRdd = smithRdd
							.filter(value -> value._1 == index ? true : false)
							.mapToPair(value -> new Tuple2<>(indexValueFinal, new Tuple2<>(value._2._1, value._2._2)));

					indexValue++;
					int indexValueFinal2 = indexValue;

					if (newPopulation == null) {
						newPopulation = parentRdd;
						parentRdd = parentRdd.mapToPair(
								value -> new Tuple2<>(indexValueFinal2, new Tuple2<>(value._2._1, value._2._2)));
						newPopulation = newPopulation.union(parentRdd);
					} else {
						newPopulation = newPopulation.union(parentRdd);
						parentRdd = parentRdd.mapToPair(
								value -> new Tuple2<>(indexValueFinal2, new Tuple2<>(value._2._1, value._2._2)));
						newPopulation = newPopulation.union(parentRdd);
					}

					indexValue++;
				}
			} else { // if number of new agents == S - 1
				int indexValueFinal = indexValue;

				JavaPairRDD<Integer, Tuple2<Double, Double>> parentRdd = smithRdd
						.filter(value -> value._1 == index ? true : false)
						.mapToPair(value -> new Tuple2<>(indexValueFinal, new Tuple2<>(value._2._1, value._2._2)));

				newPopulation = newPopulation.union(parentRdd);
				break;
			}
		}

		generationNumber++;

		return sc.parallelize(newPopulation.collect())
				.mapToPair(value -> new Tuple2<>(value._1, new Tuple2<>(value._2._1, value._2._2)));
	}

	private JavaPairRDD<Integer, Tuple2<Double, Double>> CrossoverTwoParents(
			JavaPairRDD<Integer, Tuple2<Double, Double>> parentRdd, int index1, int index2, int newIndexValue)
			throws Exception {

		int choice = random.nextInt(4);

		switch (choice) {
		case 0:
			return parentRdd.filter(value -> value._1 == index1 ? true : false)
					.mapToPair(value -> new Tuple2<>(newIndexValue, new Tuple2<>(value._2._1, value._2._2)));
		case 1:
			return parentRdd.filter(value -> value._1 == index2 ? true : false)
					.mapToPair(value -> new Tuple2<>(newIndexValue, new Tuple2<>(value._2._1, value._2._2)));
		case 2:
			return parentRdd.mapToPair(value -> {
				if (value._1 == index1) {
					return new Tuple2<>(1, new Tuple2<>(0.0, value._2._2));
				} else {
					return new Tuple2<>(1, new Tuple2<>(value._2._1, 0.0));
				}
			}).reduceByKey((x, y) -> new Tuple2<>(x._1 + y._1, x._2 + y._2))
					.mapToPair(value -> new Tuple2<>(newIndexValue, new Tuple2<>(value._2._1, value._2._2)));
		case 3:
			return parentRdd.mapToPair(value -> {
				if (value._1 == index1) {
					return new Tuple2<>(1, new Tuple2<>(value._2._1, 0.0));
				} else {
					return new Tuple2<>(1, new Tuple2<>(0.0, value._2._2));
				}
			}).reduceByKey((x, y) -> new Tuple2<>(x._1 + y._1, x._2 + y._2))
					.mapToPair(value -> new Tuple2<>(newIndexValue, new Tuple2<>(value._2._1, value._2._2)));
		default:
			throw new Exception("Error with random function.");
		}
	}

	private JavaPairRDD<Integer, Tuple2<Double, Double>> Mutate(JavaPairRDD<Integer, Tuple2<Double, Double>> agentsRdd,
			double mutationRate) {
		double p = random.nextDouble();

		if (p < mutationRate) {
			return agentsRdd.mapToPair(value -> new Tuple2<>(value._1, new Tuple2<>(p, value._2._2)));
		} else {
			return agentsRdd.mapToPair(value -> new Tuple2<>(value._1, new Tuple2<>(value._2._1, p)));
		}
	}

	private int ChooseParent(JavaPairRDD<Integer, Tuple3<Double, Double, Double>> population, double probability) {
		return population.filter(value -> value._2._3() >= probability ? true : false).first()._1;
	}

	public JavaPairRDD<Integer, Tuple3<Double, Double, Double>> CalcultateFitness(
			JavaPairRDD<Integer, Tuple3<Double, Double, Double>> PopulationEnergyNormalizedRdd) {

		// index (aggressiveness,energy,probabilityNormalized,fitness)
		JavaPairRDD<Integer, Tuple4<Double, Double, Double, Double>> populationWithFitnessRdd = PopulationEnergyNormalizedRdd
				.mapToPair(value -> value._1 == 49
						? new Tuple2<>(value._1, new Tuple4<>(value._2._1(), value._2._2(), value._2._3(), 1.0))
						: new Tuple2<>(value._1, new Tuple4<>(value._2._1(), value._2._2(), value._2._3(), 0.0)));

		for (int i = 0; i < populationSize - 1; ++i) {
			int index = i;
			double fitness = populationWithFitnessRdd.filter(x -> x._1 <= index ? true : false).map(x -> x._2._3())
					.reduce((x, y) -> x + y);

			JavaPairRDD<Integer, Tuple4<Double, Double, Double, Double>> selectedRdd = populationWithFitnessRdd
					.filter(value -> value._1 == index ? true : false).mapToPair(value -> new Tuple2<>(value._1,
							new Tuple4<>(value._2._1(), value._2._2(), value._2._3(), fitness)));

			populationWithFitnessRdd = populationWithFitnessRdd.union(selectedRdd)
					// (index,aggressiveness,energy,probabilityNormalized) fitness
					.mapToPair(value -> new Tuple2<>(
							new Tuple4<>(value._1, value._2._1(), value._2._2(), value._2._3()), value._2._4()))
					.reduceByKey((x, y) -> x + y)
					// index (aggressiveness,energy,probabilityNormalized,fitness)
					.mapToPair(value -> new Tuple2<>(value._1._1(),
							new Tuple4<>(value._1._2(), value._1._3(), value._1._4(), value._2)))
					.sortByKey();
		}

		return populationWithFitnessRdd
				.mapToPair(x -> new Tuple2<>(x._1, new Tuple3<>(x._2._1(), x._2._2(), x._2._4())));
	}

	/**
	 * This Normalization function doesn't change energy value, but it uses
	 * normalized energy values for fitness calculation (cumulative probability).
	 */
	public JavaPairRDD<Integer, Tuple3<Double, Double, Double>> Normalization(
			JavaPairRDD<Integer, Tuple2<Double, Double>> PopulationRdd, JavaSparkContext sc) {
		double totalEnergy = PopulationRdd.map(x -> x._2._2).reduce((x, y) -> x + y);

		// index (aggressiveness,energy,energyNormalized)
		// The sum of the probabilities (energy) of all outcomes must equal 1
		JavaPairRDD<Integer, Tuple3<Double, Double, Double>> PopulationEnergyNormalizedRdd = PopulationRdd
				// energyNormalized (index,aggressiveness,energy)
				.mapToPair(value -> new Tuple2<>(value._2._2 / totalEnergy,
						new Tuple3<>(value._1, value._2._1, value._2._2)))
				.sortByKey()
				.mapToPair(value -> new Tuple2<>(value._2._1(), new Tuple3<>(value._2._2(), value._2._3(), value._1)));

		List<Tuple2<Integer, Tuple3<Double, Double, Double>>> agentList = PopulationEnergyNormalizedRdd.collect();
		List<Tuple2<Integer, Tuple3<Double, Double, Double>>> agentListNewIndex = new ArrayList<>();
		int index = 0;
		for (Tuple2<Integer, Tuple3<Double, Double, Double>> agent : agentList) {
			agentListNewIndex.add(new Tuple2<>(index++, new Tuple3<>(agent._2._1(), agent._2._2(), agent._2._3())));
		}

		PopulationEnergyNormalizedRdd = sc.parallelize(agentListNewIndex)
				.mapToPair(value -> new Tuple2<>(value._1, new Tuple3<>(value._2._1(), value._2._2(), value._2._3())));

		return sc.parallelize((CalcultateFitness(PopulationEnergyNormalizedRdd).collect()))
				.mapToPair(value -> new Tuple2<>(value._1, new Tuple3<>(value._2._1(), value._2._2(), value._2._3())));
	}
}