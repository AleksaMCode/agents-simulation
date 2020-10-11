package com.oposproject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

import scala.Tuple2;
import scala.Tuple3;

public class Shell {
	public List<GeneticAlgorithm> Simulations = new ArrayList<GeneticAlgorithm>();

	public static enum commandsChoice {
		create, list, add, run, exit, error
	};

	private String prompt = "> ";
	private String wrongCommand = "The command or its signature is wrong.";
	private String divider = "---------------------------------------------------------";

	private List<Tuple3<String, Integer, Integer>> commands = new ArrayList<Tuple3<String, Integer, Integer>>() {
		{
			add(new Tuple3<>("create", 1, 24));
			add(new Tuple3<>("list", 1, 1));
			add(new Tuple3<>("add", 4, 9));
			add(new Tuple3<>("run", 2, 2));
			add(new Tuple3<>("exit", 1, 1));
		}
	};

	private Boolean CommandCheck(String[] tokens, String currentCommand, CommandsChoice selectedCommand) {
		Integer i = 0;

		for (Tuple3<String, Integer, Integer> command : commands) {
			if (currentCommand.equals(commands.get(i++)._1())) {
				if (tokens.length > command._3()) {
					break;
				} else {
					if ((tokens.length == command._2() && tokens.length == command._3())
							|| (tokens.length == command._2() && currentCommand.equals("create"))) {
						selectedCommand.Set(commandsChoice.values()[--i]);
						break;
					} else if (tokens.length > command._2() && currentCommand.equals("create")) {
						if ((tokens.length == 21 && tokens[1] == "-S" && tokens[3] == "-A"
								&& (tokens[5].equals("-r1") || tokens[5].equals("-r3")) && tokens[6].equals("-p")
								&& tokens[9].equals("-e") && tokens[11].equals("-i") && tokens[13].equals("-k1")
								&& tokens[15].equals("-k2") && tokens[17].equals("-c1") && tokens[19].equals("-c2"))
								|| (tokens.length == 22 && tokens[1] == "-S" && tokens[3] == "-A" && tokens[5] == "-r2"
										&& tokens[7].equals("-p") && tokens[10].equals("-e") && tokens[12].equals("-i")
										&& tokens[14].equals("-k1") && tokens[16].equals("-k2")
										&& tokens[18].equals("-c1") && tokens[20].equals("-c2"))
								|| (tokens.length == 23 && tokens[1].equals("-S") && tokens[3].equals("-A")
										&& (tokens[5].equals("-r1") || tokens[5].equals("-r3"))
										&& tokens[6].equals("-pr") && tokens[11].equals("-e") && tokens[13].equals("-i")
										&& tokens[15].equals("-k1") && tokens[17].equals("-k2")
										&& tokens[19].equals("-c1") && tokens[21].equals("-c2"))
								|| (tokens.length == 24 && tokens[1].equals("-S") && tokens[3].equals("-A")
										&& tokens[5].equals("-r2") && tokens[7].equals("-pr") && tokens[12].equals("-e")
										&& tokens[14].equals("-i") && tokens[16].equals("-k1")
										&& tokens[18].equals("-k2") && tokens[20].equals("-c1")
										&& tokens[22].equals("-c2"))) {
							selectedCommand.Set(commandsChoice.values()[--i]);
							break;
						} else {
							break;
						}
					} else if (tokens.length >= command._2() && currentCommand.equals("add")) {
						if ((tokens.length == 4 && tokens[2].equals("-n"))
								|| (tokens.length == 7 && tokens[2].equals("-n") && tokens[4].equals("-p"))
								|| (tokens.length == 7 && tokens[2].equals("-n") && tokens[4].equals("-pr"))) {
							selectedCommand.Set(commandsChoice.values()[--i]);
							break;
						} else {
							break;
						}
					} else {
						break;
					}
				}
			}
		}

		if (selectedCommand.Get() == commandsChoice.error) {
			System.out.println(wrongCommand);
			return false;
		} else {
			return true;
		}
	}

	public void WinShell() {
		Random random = new Random();
		Logger.getLogger("org.apache").setLevel(Level.WARN);

		SparkConf conf = new SparkConf().setAppName("OPOS project" + random.nextInt()).setMaster("local[*]");
//		  SparkConf conf = new SparkConf().setMaster("local").setAppName("MyApp")
//			        .set("spark.hadoop.dfs.client.use.datanode.hostname", "true");
		JavaSparkContext sc = new JavaSparkContext(conf);

		while (true) {
			String currentCommand = "";
			System.out.print(prompt);
			Scanner scanner = new Scanner(System.in);
			String[] tokens = scanner.nextLine().split(" ");
			currentCommand = tokens[0];
			commandsChoice selectedCommand = commandsChoice.error;

			CommandsChoice command = new CommandsChoice(selectedCommand);

			if (!CommandCheck(tokens, currentCommand, command)) {
				continue;
			} else {
				selectedCommand = command.Get();
				switch (selectedCommand) {
				case create: {
					try {
						if (tokens.length == 1) {
							Simulations.add(new GeneticAlgorithm(random));
						} else if (tokens.length == 21) {
							int crossoverOption = tokens[5] == "-r1" ? 1 : 3;

							Simulations.add(new GeneticAlgorithm(random, Integer.parseInt(tokens[2]),
									Integer.parseInt(tokens[4]), crossoverOption, Double.parseDouble(tokens[7]),
									Double.parseDouble(tokens[8]), Integer.parseInt(tokens[10]),
									Integer.parseInt(tokens[12]), Double.parseDouble(tokens[14]),
									Double.parseDouble(tokens[16]), Double.parseDouble(tokens[18]),
									Double.parseDouble(tokens[20])));
						} else if (tokens.length == 23) {
							int crossoverOption = tokens[5] == "-r1" ? 1 : 3;

							Simulations.add(new GeneticAlgorithm(random, Integer.parseInt(tokens[2]),
									Integer.parseInt(tokens[4]), crossoverOption,
									new Tuple2<>(Double.parseDouble(tokens[7]), Double.parseDouble(tokens[8])),
									new Tuple2<>(Double.parseDouble(tokens[9]), Double.parseDouble(tokens[10])),
									Integer.parseInt(tokens[12]), Integer.parseInt(tokens[14]),
									Double.parseDouble(tokens[16]), Double.parseDouble(tokens[18]),
									Double.parseDouble(tokens[20]), Double.parseDouble(tokens[22])));
						} else if (tokens.length == 24) {
							Simulations.add(new GeneticAlgorithm(random, Integer.parseInt(tokens[2]),
									Integer.parseInt(tokens[4]), 2, Double.parseDouble(tokens[6]),
									new Tuple2<>(Double.parseDouble(tokens[8]), Double.parseDouble(tokens[9])),
									new Tuple2<>(Double.parseDouble(tokens[10]), Double.parseDouble(tokens[11])),
									Integer.parseInt(tokens[13]), Integer.parseInt(tokens[15]),
									Double.parseDouble(tokens[17]), Double.parseDouble(tokens[19]),
									Double.parseDouble(tokens[21]), Double.parseDouble(tokens[23])));
						} else if (tokens.length == 22) {
							Simulations.add(new GeneticAlgorithm(random, Integer.parseInt(tokens[2]),
									Integer.parseInt(tokens[4]), 2, Double.parseDouble(tokens[6]),
									Double.parseDouble(tokens[8]), Double.parseDouble(tokens[9]),
									Integer.parseInt(tokens[11]), Integer.parseInt(tokens[13]),
									Double.parseDouble(tokens[15]), Double.parseDouble(tokens[17]),
									Double.parseDouble(tokens[19]), Double.parseDouble(tokens[21])));
						}
					} catch (Exception e) {
						System.out.print(e.getMessage());
					}

					/*
					 * threads.add(new Thread(new Runnable() { public void run() { int simNumb =
					 * Simulations.size(); Simulations.get(simNumb - 1).Collision();
					 * System.out.println("\nSimulation " + simNumb + " is done.");
					 * System.out.println(prompt); } }));
					 */
					
					// Run simulation on a new thread !!
					Thread tr = new Thread() {
						public void run() {
							int simNumb = Simulations.size();
							Simulations.get(simNumb - 1).Collision(sc);
							System.out.println("\nSimulation " + simNumb + " is done.");
							System.out.println(prompt);
						}
					};
					tr.start();

//					threads.get(threads.size() - 1).start();
					break;
				}
				case list: {
					int i = 1;

					if (!Simulations.isEmpty()) {
						for (GeneticAlgorithm sim : Simulations) {
							System.out.println("Simulation " + i++ + ":");
							sim.GeneticInfo();
							System.out.println(divider);
						}
						break;
					} else {
						System.out.println("There are no simulations!");
						break;
					}
				}
				case add: {
					int simNumber = Integer.parseInt(tokens[1]);

					if (simNumber <= 0 || simNumber > Simulations.size()) {
						System.out.println("Simulation with number " + simNumber + " doesn't exist.");
					} else {
						if (!Simulations.get(simNumber - 1).IsSimulationOver()) {

							int n = Integer.parseInt(tokens[3]);

							if (tokens.length == 4) {
								for (int i = 0, index = Simulations.get(simNumber - 1).populationSize; i < n; ++i) {
									DNA agent = new DNA(random);
									Simulations.get(simNumber - 1).Population.add(new Tuple2<>(index++,
											new Tuple2<>(agent.Properties.Aggressiveness, agent.Properties.Energy)));
								}
							} else if (tokens.length == 7) {
								for (int i = 0, index = Simulations.get(simNumber - 1).populationSize; i < n; ++i) {
									DNA agent = new DNA(random, Double.parseDouble(tokens[5]),
											Double.parseDouble(tokens[6]));
									Simulations.get(simNumber - 1).Population.add(new Tuple2<>(index++,
											new Tuple2<>(agent.Properties.Aggressiveness, agent.Properties.Energy)));
								}
							} else {
								for (int i = 0, index = Simulations.get(simNumber - 1).populationSize; i < n; ++i) {
									DNA agent = new DNA(random,
											new Tuple2<>(Double.parseDouble(tokens[5]), Double.parseDouble(tokens[6])),
											new Tuple2<>(Double.parseDouble(tokens[7]), Double.parseDouble(tokens[8])));
									Simulations.get(simNumber - 1).Population.add(new Tuple2<>(index++,
											new Tuple2<>(agent.Properties.Aggressiveness, agent.Properties.Energy)));
								}
							}
							Simulations.get(simNumber - 1).populationSize += n;
						} else {
							System.out.println("Can't add more agents to simulation " + simNumber
									+ ". Please wait for simulation to finish first!");
						}
					}
					break;
				}
				case run: {
					int simNumber = Integer.parseInt(tokens[1]);

					if (simNumber <= 0 || simNumber > Simulations.size()) {
						System.out.println("Simulation with number " + simNumber + " doesn't exist.");
					} else {
						if (!Simulations.get(simNumber - 1).IsSimulationOver()) {
							// Increasing the epochNumber by the original epochNumber
							Simulations.get(simNumber - 1).epochNumber += Simulations
									.get(simNumber - 1).epochNumberConstant;
							// Run simulation on a new thread !!
							Thread tr = new Thread() {
								public void run() {
									Simulations.get(simNumber - 1).Collision(sc);
									System.out.println("\nSimulation " + simNumber + " is done.");
									System.out.println(prompt);
								}
							};
							tr.start();
						} else {
							System.out.println("Simulation " + simNumber + " is already in progress!");
						}
					}
					break;
				}
				case exit: {
					sc.close();
					return;
				}
				}
			}
		}
	}
}