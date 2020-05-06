package Messages;

import java.io.Serializable;

public abstract class Message implements Serializable {
    protected String text;
    public String getString(){ return text;}
}
