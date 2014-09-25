package com.ngage.beeldvan.utilities;

import java.util.Comparator;

import com.ngage.beeldvan.model.Locations;

public class CompareToSort implements Comparator<Locations>
	{

		public int compare(Locations l1, Locations l2)
			{


                return (int) (l1.getDistance()-l2.getDistance());

			}
	}
