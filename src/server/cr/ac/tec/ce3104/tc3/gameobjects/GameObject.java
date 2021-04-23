package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.physics.Speed;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.networking.Command;
import cr.ac.tec.ce3104.tc3.resources.Sequence;

public abstract class GameObject {
    public GameObject(Sequence sequence, Position position) {
        this(sequence, position, 1, 1);
    }

    public GameObject(Sequence sequence, Position position, Integer horizontalRepeat, Integer verticalRepeat) {
        this.id = nextId++;
        this.position = position;
        this.sequence = sequence;
        this.horizontalRepeat = horizontalRepeat;
        this.verticalRepeat = verticalRepeat;
    }

    public Integer getId() {
        return this.id;
    }

    public Command makePutCommand() {
        return Command.cmdPut(this.id, this.position, this.speed, this.sequence);
    }

    public Integer[] getCollisionBox() {
        return new Integer[] {/*x1,y1,x2,y2*/};
    }

    public void onStartUp(){
        //maneja la instruccion inicial de dibujo
    }

    public void onTick(){
        //No es requisito implementar para todos
        //Maneja animaciones
    }

    public Boolean collides(GameObject gameObject){
        return false; //TODO
        /*Integer objectcoords[] = gameObject.getCollisionBox();/
        Boolean collides = false;
        Boolean rightCollision = (x1 <= objectcoords[0] && objectcoords[0] <= x2);
        Boolean leftCollision = (x1 <= objectcoords[2] && objectcoords[2]<= x2);
        Boolean upCollision = (y1 <= objectcoords[1] && objectcoords[1] <= y2);
        Boolean downCollision = (y1 <= objectcoords[3] && objectcoords[3] <= y2);
        if((rightCollision || leftCollision)&&(upCollision || downCollision))
            collides = true;  
        return collides;*/
    }

    private static Integer nextId = 0;

    private Integer id;
    private Position position;
    private Speed speed = Speed.stationary();
    private Sequence sequence;
    private Integer horizontalRepeat;
    private Integer verticalRepeat;
}
