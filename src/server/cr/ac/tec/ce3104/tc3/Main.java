package cr.ac.tec.ce3104.tc3;

import cr.ac.tec.ce3104.tc3.gameobjects.Fruit;
import cr.ac.tec.ce3104.tc3.gameobjects.FruitFactory;
import cr.ac.tec.ce3104.tc3.gameobjects.FruitType;
import cr.ac.tec.ce3104.tc3.util.CEList;

public class Main
{
	public static void main(String[] argv)
	{
		Fruit banana = FruitFactory.createFruit(0, 0, FruitType.BANANA);
		Fruit nispero = FruitFactory.createFruit(16, 0, FruitType.NISPERO);
		System.out.println(banana.collides(nispero));
	}
}
