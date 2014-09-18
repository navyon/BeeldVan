package org.BvDH.CityTalk.utilities;

import java.util.Comparator;

import org.BvDH.CityTalk.model.Locations;

public class CompareToSort implements Comparator<Locations>
	{

		public int compare(Locations l1, Locations l2)
			{

				String distance1 = l1.getName();
				String distance2 = l2.getName();

				return distance1.compareTo(distance2);
			}
	}
