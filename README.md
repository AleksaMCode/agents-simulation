# Agents simulation
**Selected Chapters from Operating System** course project, as taught at the **Faculty of Electrical Engineering Banja Luka**.
## Description
The task was to create a distributed implementation of [genetic algorithm](https://en.wikipedia.org/wiki/Genetic_algorithm) that would simulate agents interactions. Agents are a simple organisms with a predefined behaviour. They interract inside one epoch and based on the predifined selection rules at the end of an epoch we choose the fittest agents to continue the simulation in the next epoch.
### Agents
At the start of the simulation, agents are given pseudo-random values for energy and aggressiveness properties in a range [0,1].
Characteristics | Description
------------ | -------------
Energy | It is used as to calculate the fitness of an agent. Energy can be a positive value in a range [0,+∞) with respect to the maximum value of double. Higher the energy value of an agent, greater the chance of agents reproduction at the end of an epoch. Energy is increased with each interraction.
Aggressiveness | Is used to determine the ratio of distribution of energy to the two agents during interaction. Aggressiveness can only have values in a range [0,1]. Every interaction introduces const. predefined energy value to the simulation, which is than distributed between two agents with respects to their aggressiveness values. Before adding the new energy values to the agents, energy is scaled according to this formula ![scaling_equation](http://www.sciweavers.org/upload/Tex2Img_1602335826/render.png), </br>where ![sigmoid](http://www.sciweavers.org/upload/Tex2Img_1602336588/render.png) and ![a_i](http://www.sciweavers.org/upload/Tex2Img_1602336826/render.png) is agents aggressiveness value. Koeficients ![k_i](http://www.sciweavers.org/upload/Tex2Img_1602336880/render.png) and ![c_i](http://www.sciweavers.org/upload/Tex2Img_1602337019/render.png) are simulation parameters.
Fitness | Fitness represents a probability ([cumulative probability](https://en.wikipedia.org/wiki/Cumulative_distribution_function)) of agents reproduction at the end of an epoch. It's calculated based on the energy of the agents in the simulation. It has to be proportionate to all the other fitness levels in the simulation. When calculating cumulative probability sum of all probabilities must equal 1.
### Epoch
Every epoch consist of an `iterationNumber` iterations. During iteration agents are paired randomly for interactions. Every agent must be paired. In case of an odd number of agents, one of the agents, chosen randomly, will interact  twice during the iteration. Epoch ends when all the iterations are completed. At the end of each epoch, `numberOfAgents` agents is chosen whose genes will be used to create new agents for the next epoch. Similar problem is explained [here](https://www.youtube.com/watch?v=YNMkADpvO4w).
### Reproduction
Agents are chosen randomly (total number of agents for reproduction is set in variable `numberOfAgents`), with a probability of choice being proportional to their energies. This is accomplished with cumulative probabilities, or fitness as it is referred to in this project. There are 3 ways of reproduction.
Type | Description
------------ | -------------
Type-1 | Two identical agents are created (agents with same Genes (Energy and Aggressiveness) based on the selected agent.
Type-2 | Chosen agent is copied in to two new agents with the same Genes. One of the Genes, Energy or Aggressiveness, chosen randomly, is mutated with a `mutationRate` probability with respects to its allowed intervals.
Type-3 | Two agents are chosen and their Genes are randomly combined.

## Implementation
### Population RDD
Population, or the agent collection, is 'stored' inside of an JavaPairRDD where **key** is an Integer which represents an agent index in population and **value** is a scala Tuple2 which holds two double values, Energy and Aggressiveness.
### Collision
Collision is implemented in *SurvivalOfTheFittest* method. Two agents are chosen randomly for collision, while keeping track that no agent is picked twice. This is accomplished by keeping a track of the used index values in a separate index list (`List<Integer> indexes`). Exception is, as stated above, when we have an odd number of agents. Then one of the agents can be picked twice. After picking two unique agents, we create an JavaPairRDD `twoAgentsRdd` which we create by filtering `PopulationRdd`.
```java
twoAgentsRdd = populationCopyRdd.filter(value -> value._1 == agent1Indx ? true : value._1 == agent2Indx ? true : false);
```
After we calculate the total amount of aggressiveness. We do this by mapping the `twoAgentsRdd` in to a rdd and then we reduce it to a single double value.
```java
double totalAggressiveness = twoAgentsRdd.map(x -> x._2._1).reduce((x, y) -> x + y);
```
We use the `totalAggressiveness` variable to create an energy split between the two agents. As stated above, aggressiveness is used to distributed predefined energy value to two agents. Then we proceed by mapping `twoAgentsRdd` into a pair RDD while keepeing track of the *index*, *aggressiveness* and the *energy split* value. Energy split value is calculated as ![energySplit](http://www.sciweavers.org/upload/Tex2Img_1602346910/render.png).
```java
twoAgentsRdd.mapToPair(value -> new Tuple2<>(value._1,new Tuple2<>(value._2._1, collisionEnergy * value._2._1 / totalAggressiveness)))
```
Then we calculate the scaling factor by using scaling equation we defined earlier.
```java
.mapToPair(value -> new Tuple2<>(value._1,new Tuple3<>(value._2._1, value._2._2,1 - (k1 * Sigmoid(c1 * (value._2._1 - k2))+ (1 - k1) * Sigmoid(c1 * (value._2._1 - k2))))))
```
After that we need to combine the scaling factor with an energy split. We do that by multiplying  the two values.
```java
.mapToPair(value -> new Tuple2<>(value._1,new Tuple2<>(value._2._1(), value._2._2() * value._2._3())))
```
Finally we need to add the new energy value to the old energy of an agent.
* Mapp the `PopulationRdd` on itself, while changing the **key** to scala Tuple, which now holds index and aggressiveness and the **value** to Double, which is now only agents energy value. We do this so we can later to reduce by key on the Pair RDD.
* We add the `twoAgentsRdd` to the `PopulationRdd` by using `.union`.
* We do reduction by key, while adding the values part together.
* Finally, we map everything to the initial form of `PopulationRdd`.
```java
PopulationRdd = PopulationRdd.mapToPair(x -> new Tuple2<>(new Tuple2<>(x._1, x._2._1), x._2._2))
			     .union(twoAgentsRdd.mapToPair(x -> new Tuple2<>(new Tuple2<>(x._1, x._2._1), x._2._2)))
			     .reduceByKey((x, y) -> x + y)
			     .mapToPair(x -> new Tuple2<>(x._1._1, new Tuple2<>(x._1._2, x._2)));
```
### Reproduction
Reproduction is implemented in *NewGeneration* method. Before creating new agents we need to normalize and create culmulative probability in *Normalization* method. After that we chooise, with a probability of a choose, unique indexes from the population and store in `smithIndex` list, after which we create a pair RDD called `smithRdd` which only contains agents with indexes from a list `smithIndex`. We do this by filtering out the values we need from `populationWithFitnessRdd`. `populationWithFitnessRdd` is the same as `PopulationRdd` just with an 'extra fieled' containing the fitness value (instead of the scala's Tuple2 as **value** we now use Tuple3).
```java
JavaPairRDD<Integer, Tuple2<Double, Double>> smithRdd = populationWithFitnessRdd
		.filter(value -> smithIndex.contains(value._1) ? true : false)
		.mapToPair(value -> new Tuple2<>(value._1, new Tuple2<>(value._2._1(), value._2._2())));
```
After that, we create a new population containg the genes of the selected agents. Reproduction types are stored in a variable `crossoverOption` as 1, 2 or 3, respectively. There is also a 4th way of reproduction (combined reproduction) which is used when `crossoverOption` is set to -1. Each time a new generation is created, the way of reproducing is chosen randomly between the 3 types.
### Normalization
First we calculate the total amount of energy. We do this by mapping the `PopulationRdd` into a RDD that contains only energy values and then we reduce it to a single, double value, by adding all of the energy values. We will use `totalEnergy` to calculate normalized energy. Normalized energy is calculated by dividing the orignal energy with a total amount of energy. Sum of all normalized energies must equal 1.
```java
double totalEnergy = PopulationRdd.map(x -> x._2._2).reduce((x, y) -> x + y);
```
After that we create `PopulationEnergyNormalizedRdd` which is a `PopulationRdd` with an 'extra field' *energyNormalized*. The RDD is also sorted in respects to normalized energy.
```java
JavaPairRDD<Integer, Tuple3<Double, Double, Double>> PopulationEnergyNormalizedRdd = PopulationRdd
		.mapToPair(value -> new Tuple2<>(value._2._2 / totalEnergy, new Tuple3<>(value._1, value._2._1, value._2._2)))
		.sortByKey()
		.mapToPair(value -> new Tuple2<>(value._2._1(), new Tuple3<>(value._2._2(), value._2._3(), value._1)));
```
The normalized energy is only used to calculate fitness values, after which is deleted. The value of energy is never changed.
The fitness (cumulative probability) is defined as the sum of all the normalized energies (probabilities) up to chosen index. Fitness for one agent is calculated by filtering out every agent with an index value that is smaller or equal to chosen index, after which we map pair RDD to a single value RDD which contains only fitness values. Then we reduce an RDD to a value of type double.
```java
double fitness = populationWithFitnessRdd
	.filter(x -> x._1 <= index ? true : false)
	.map(x -> x._2._3())
	.reduce((x, y) -> x + y);
```
Then we create an agent with a new fitness value.
```java
JavaPairRDD<Integer, Tuple4<Double, Double, Double, Double>> selectedRdd = populationWithFitnessRdd
		.filter(value -> value._1 == index ? true : false)
		.mapToPair(value -> new Tuple2<>(value._1, new Tuple4<>(value._2._1(), value._2._2(), value._2._3(), fitness)));
```
The only thing that is left to do is to add `selectedRdd` to `populationWithFitnessRdd`, after which we add the fitness values of two duplicate agents in RDD. We do that by ***reducing by key*** while adding the **values** from the reduced entries.
```java
populationWithFitnessRdd = populationWithFitnessRdd
				.union(selectedRdd)
				.mapToPair(value -> new Tuple2<>(new Tuple4<>(value._1, value._2._1(), value._2._2(), value._2._3()), value._2._4()))
				.reduceByKey((x, y) -> x + y)
				.mapToPair(value -> new Tuple2<>(value._1._1(), new Tuple4<>(value._1._2(), value._1._3(), value._1._4(), value._2)))
				.sortByKey();
```
Here is a simple example of how to create a table whicjh will help us in picking a random value based on probability.
element | probability | normalized probability | cumulative probability
------------ | ------------- | ------------- | -------------
0 | 0.1 | 0.04 | 0.04
1 | 0.5 | 0.2 | 0.24
2 | 0.7 | 0.28 | 0.52
3 | 0.3 | 0.12 | 0.64
4 | 0.9 | 0.36 | 1.0

Sum of all probabilities is 2.5, which is not in range [0,1]. Because of that we must normalize probabilities. The reason why we don't change our energie values, is because energies in this project are in range [0,+∞). We only use the normalized energies to calculate  fitness or cumulative probability. The cumulative probability is defined as the sum of all the probabilities up to that point. Now, we create a random value between 0 and 1. If it lies between 0 and 0.04, you've picked an element 0. If it lies between 0.04 and 0.24, you've picked and element 1, and so on.
In our project we pick a random value based on the fitness when we pick out agents for reproduction. The picking is done in a method called `ChooseParent`.

## Usage	

## To-Do List
- [ ] Replace in memory agent collection with a csv file on hdfs.
