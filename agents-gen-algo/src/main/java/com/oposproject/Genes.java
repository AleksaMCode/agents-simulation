package com.oposproject;

import java.io.Serializable;
import java.util.Random;

import scala.Tuple2;

public class Genes implements Serializable {
	/**
    * Used as a probability that is at times normalized.
    * The sum of the probabilities of all outcomes must equal 1 when calculating cumulative probability aka. fitness.
    * Energy can be any positive value [0,+∞) with respect to maximum value of double.
    */
    public double Energy =  0.0;
    public double Aggressiveness = 0.0;

    public Genes(double Energy, double Aggressiveness)
    {
        if (Energy > 0.0f && CheckParam(Aggressiveness))
        {
            this.Energy = Energy;
            this.Aggressiveness = Aggressiveness;
        }
        else
        {
        	this.Energy = 0.2;
        	this.Aggressiveness = 0.5;
        }
    }

    public Genes(Tuple2<Double, Double> energyInterval, Tuple2<Double, Double> aggressivenessInterval, Random random)
    {
        if (CheckParam(aggressivenessInterval._1) && CheckParam(aggressivenessInterval._2) && CheckInterval(aggressivenessInterval._1, aggressivenessInterval._2)
            && aggressivenessInterval._1 >= 0.0f && aggressivenessInterval._2 >= 0.0f && CheckInterval(aggressivenessInterval._1, energyInterval._2))
        {
            this.Energy = random.nextDouble() * (energyInterval._2 - energyInterval._1) + energyInterval._1;
            this.Aggressiveness = random.nextDouble() * (aggressivenessInterval._2 - aggressivenessInterval._1) + aggressivenessInterval._1;
        }
        else
        {
        	this.Energy = random.nextDouble();
        	this.Aggressiveness = random.nextDouble();

            while (this.Aggressiveness == this.Energy)
            {
                this.Aggressiveness = random.nextDouble();
            }
        }
    }

    private Boolean CheckParam(double param)
    {
        return param >= 0.0 ? (param <= 1.0 ? true : false) : false;
    }

    private Boolean CheckInterval(double a, double b)
    {
        return a <= b;
    }
}