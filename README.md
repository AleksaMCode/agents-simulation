# Agents simulation
**Selected Chapters from Operating System** course project, as taught at the **Faculty of Electrical Engineering Banja Luka**.
## Description
The task was to create a distributed implementation of [genetic algorithm](https://en.wikipedia.org/wiki/Genetic_algorithm) that would simulate agents interactions. Agents are a simple organisms with a predefined behaviour. They interract inside one epoch and based on the predifined selection rules at the end of an epoch we choose the fittest agents to continuo the simulation in the next epoch.
### Agents
At the start of the simulation, agents are given pseudo-random values for energy and aggressiveness properties in a range [0,1].
Characteristics | Description
------------ | -------------
Energy | It is used as to calculate the fitness of an agent. Energy can be a positive value in a range [0,+∞) with respect to the maximum value of double. Higher the energy value of an agent, greater the chance of agents reproduction at the end of an epoch. Energy is increased with each interraction.
Aggressiveness | Is used to determine the ratio of distribution of energy to the two agents during interaction. Aggressiveness can only have values in a range [0,1]. Every interaction introduces const. predefined energy value to the simulation, which is than distributed between two agents with respects to their aggressiveness values. Before adding the new energy values to the agents, energy is scaled according to this formula ![equation](http://www.sciweavers.org/upload/Tex2Img_1602335826/render.png), where ![sigmoid](http://www.sciweavers.org/upload/Tex2Img_1602336588/render.png) and ![a_i](http://www.sciweavers.org/upload/Tex2Img_1602336826/render.png) is a agents aggressiveness value. Koeficients ![k_i](http://www.sciweavers.org/upload/Tex2Img_1602336880/render.png) and ![c_i](http://www.sciweavers.org/upload/Tex2Img_1602337019/render.png) are simulation parameters.
Fitness | Fitness represents a probability of agents reproduction at the end of an epoch. It's calculated based on the energy of the agents in the simulation. It has to be proportionate to all the other fitness levels in the simulation. That means, that the sum of all probabilities must equal 1 when calculating [cumulative probability](https://en.wikipedia.org/wiki/Cumulative_distribution_function) aka. fitness.
### Epoch
Every epoch consist of an `iterationNumber` iterations. During iteration agents are paired randomly for interactions. Every agent must be paired. In case of an odd number of agents, one of the agents, chosen randomly, will interact  twice during the iteration. Epoch ends when all the iterations are completed. At the end of each epoch, `numberOfAgents` agents is chosen whose genes will be used to create new agents for the next epoch. Similar problem is explained [here](https://www.youtube.com/watch?v=YNMkADpvO4w).
### Reproduction
Agents are chosen randomly (total number of agents for reproduction is set in variable `numberOfAgents`), with a probability of choice being proportional to their energies. This is accomplished with cumulative probabilities, or fitness as it is referred to in this project. There are 3 ways of reproduction.
Type | Description
------------ | -------------
Type-1 | Two identical agents are created (agents with same Genes (Energy and Aggressiveness) based on the selected agent.
Type-2 | Chosen agent is copied in to two new agents with the same Genes. One of the Genes, Energy or Aggressiveness, chosen randomly, is mutated with a `mutationRate` probability with respects to its allowed intervals.
Type-3 | Two agents are chosen and their Genes are randomly combined.

These reproduction option are used when variable `crossoverOption` is set to 1, 2 or 3, respectively. There is also a 4th way of reproduction (comined reproduction) which is used when `crossoverOption` is set to -1. Each time a reprocustion occurs, the way of reproducing is chosen randomly between the 3 types.

## To-Do List
- [ ] Replace in memory agent collection with a csv file on hdfs.
