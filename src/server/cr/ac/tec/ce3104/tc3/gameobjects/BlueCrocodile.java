package cr.ac.tec.ce3104.tc3.gameobjects;

class BlueCrocodile extends Crocodile{
    Integer verticalSprites[]={27,28,29,30};
    Integer horizontalSprites[]={25,26};
    public BlueCrocodile(Integer x, Integer y){
        super(x,y);
        this.defSprite = horizontalSprites[0];
    }
    public void onTick(){

    }
}
