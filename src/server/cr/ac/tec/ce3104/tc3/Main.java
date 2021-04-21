package cr.ac.tec.ce3104.tc3;

import cr.ac.tec.ce3104.tc3.gameobjects.EnvironmentObject;
import cr.ac.tec.ce3104.tc3.gameobjects.Fruit;
import cr.ac.tec.ce3104.tc3.gameobjects.FruitFactory;
import cr.ac.tec.ce3104.tc3.gameobjects.FruitType;
import cr.ac.tec.ce3104.tc3.gameobjects.Platform;
import cr.ac.tec.ce3104.tc3.gameobjects.PlatformType;
import cr.ac.tec.ce3104.tc3.util.CEList;

public class Main
{
	public static void main(String[] argv)
	{
		CEList<EnvironmentObject> environmentObjects = new CEList<>();
		int screen[] = {256,240};
		int lenghtUnit=8;
		CEList<String> commands = new CEList<>();
        Integer ypos[] = new Integer[screen[1]/lenghtUnit];
        Integer xpos[] = new Integer[screen[0]/lenghtUnit];
        for(int y = 0, i=0; y<screen[1]; y+=lenghtUnit, i++){
            ypos[i] = y; //posiciones horizontales de 0 a 248
        }
        for(int x = 0, i=0; x<screen[0]; x+=lenghtUnit, i++){
            xpos[i] = x; //posiciones horizontales de 0 a 248
        }


        for(int i = 0; i<xpos.length;++i){//agua
            environmentObjects.add(new Platform(xpos[i],ypos[29], PlatformType.WATER1));
            environmentObjects.add(new Platform(xpos[i],ypos[28], PlatformType.WATER2));
		}

		for(int i=0; i< xpos.length;i++){
			System.out.println(String.format("index: %3d, x %3d ----- y %3d", i, xpos[i], ypos[i]));
		}
		//Server.getInstance().StartUp();
	}
}
