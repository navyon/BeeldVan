package org.BvDH.CityTalk.utilities;

import java.util.Comparator;

import org.BvDH.CityTalk.model.Locations;

public class CompareToSort implements Comparator<Locations>
	{

		public int compare(Locations l1, Locations l2)
			{


                return (int) (l1.getDistance()-l2.getDistance());

			}
	}
