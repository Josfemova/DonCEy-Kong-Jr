package cr.ac.tec.ce3104.tc3.gameobjects;
class RedCrocodile extends Crocodile{
    Integer verticalSprites[]={33,34,35};
    Integer horizontalSprites[]={31,32};
    public RedCrocodile(Integer x, Integer y){
        super(x,y);
        this.defSprite = verticalSprites[0];
    }
}
