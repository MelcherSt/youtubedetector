package nl.melcher.ytdetect;

import nl.melcher.ytdetect.tui.InputHandler;


public class Application {

	public static void main(String[] args) {

		InputHandler inputHandler = new InputHandler();
		inputHandler.handle(args);
	}
}
