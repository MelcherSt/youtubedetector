package nl.melcher.ytdetect.tui.handler;

import nl.melcher.ytdetect.tui.InvalidArgumentsException;

import java.util.List;

/**
 * Command line handler interface
 */
public interface IHandler {

	void handle(List<String> args) throws InvalidArgumentsException;
}
