package eg.edu.alexu.csd.oop.game.characters.falling;

public class BigBall extends FallingCharacter {

  @Override
  public Object clone() {
    BigBall o = new BigBall();
    o.setX(this.getX());
    o.setY(this.getY());
    o.setColor(this.getColor());
    o.setVisible(this.isVisible);
    o.setImage(this.getSpriteImages()[0]);

    return o;
  }

}
