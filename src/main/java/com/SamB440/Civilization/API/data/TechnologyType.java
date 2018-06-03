package com.SamB440.Civilization.API.data;

public enum TechnologyType {
	ANIMAL_HUSBANDRY("animalhusbandry"), 
	WRITING("writing"), 
	POTTERY("pottery"), 
	MINING("mining"), 
	WHEEL("wheel");
	
	private String name;
	
	TechnologyType(String name)
	{
		this.name = name;
	}
	
	/**
	 * @return the advancement name
	 */
	public String getName()
	{
		return name;
	}
}
