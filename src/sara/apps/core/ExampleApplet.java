package sara.apps.core;

import sara.Applet;
import sara.annotations.Independent;

@Independent(names={"Example Applet", "Example"})
public class ExampleApplet extends Applet {

	public ExampleApplet(Applet parent, String[] args) {
		super(parent, "Example applet!");
	}

	@Override
	protected void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void update() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onClose() {
		// TODO Auto-generated method stub
		
	}

}
